//! Tyx Launcher — Rust backend (full-stack: phases 1–5).
//!
//! Module map:
//!   util       – app-data paths, ids, timestamps
//!   instances  – isolated `.minecraft` CRUD + file browsing
//!   minecraft  – MC version fallback list + loader install record
//!   java       – runtime detection (std only)
//!   auth       – offline UUID derivation (MS/Xbox HTTP lives in frontend)
//!   launch     – JVM command preview + spawn with log streaming
//!
//! Deliberately dependency-free beyond Tauri + serde: networking (Mojang,
//! Modrinth, Microsoft OAuth) runs from the frontend via fetch, so this
//! backend compiles with the stock Tauri toolchain and stays small.

#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use tauri::Manager;

mod assets;
mod auth;
mod instances;
mod java;
mod launch;
mod loader;
mod minecraft;
mod modpack;
mod servers;
mod util;

use serde::Serialize;

#[derive(Serialize)]
struct LauncherInfo {
    version: String,
    phase: u8,
    backend: &'static str,
}

#[tauri::command]
fn launcher_info() -> LauncherInfo {
    LauncherInfo {
        version: env!("CARGO_PKG_VERSION").to_string(),
        phase: 5,
        backend: "tauri-v2",
    }
}

/// Kill the OS-drawn 1px accent outline around our borderless window.
///
/// Windows (DWM) draws a 1px border in the user's accent color around
/// top-level windows — pink on this machine because that's the OS accent,
/// NOT our app. Two layers of fix:
///   - Win11+: DWMWA_COLOR_NONE (0xFFFFFFFE) = border fully GONE.
///   - All versions (incl. Win10, where the color API fails with
///     E_INVALIDARG): claim the whole window as client area
///     (WM_NCCALCSIZE → 0) so DWM has no non-client area left to draw the
///     border on. Drag/resize keep working through the app's own Tauri
///     drag region + resize handles; maximized size is clamped to the work
///     area so the taskbar stays visible.
/// Zero glass margins + strip WS_BORDER + re-apply on focus/resize.
#[cfg(windows)]
fn paint_hwnd(hwnd: windows::Win32::Foundation::HWND) {
    use windows::Win32::{
        Graphics::Dwm::{
            DwmExtendFrameIntoClientArea, DwmSetWindowAttribute, DWMWA_BORDER_COLOR,
            DWMWA_WINDOW_CORNER_PREFERENCE, DWMWCP_DONOTROUND,
        },
        UI::{
            Controls::MARGINS,
            WindowsAndMessaging::{
                GetWindowLongW, SetWindowLongW, SetWindowPos, GWL_STYLE, SWP_FRAMECHANGED,
                SWP_NOMOVE, SWP_NOSIZE, SWP_NOZORDER, WS_BORDER,
            },
        },
    };
    // DWMWA_COLOR_NONE = no border at all (Win11+). Fallback #26302b if
    // the OS rejects NONE (Win10: E_INVALIDARG, can't remove, just recolor attempt).
    const COLOR_NONE: u32 = 0xFFFFFFFE;
    const FALLBACK: u32 = 0x002B3026; // #26302b → COLORREF 0x00BBGGRR
    let border = unsafe {
        DwmSetWindowAttribute(
            hwnd,
            DWMWA_BORDER_COLOR,
            &COLOR_NONE as *const _ as _,
            std::mem::size_of::<u32>() as u32,
        )
    };
    let border_note = if border.is_err() {
        let fb = unsafe {
            DwmSetWindowAttribute(
                hwnd,
                DWMWA_BORDER_COLOR,
                &FALLBACK as *const _ as _,
                std::mem::size_of::<u32>() as u32,
            )
        };
        format!("none-failed,fallback={fb:?}")
    } else {
        "none-ok".to_string()
    };
    // Win11 draws its own rounded window corners + border OVER our content —
    // the corner pixels glow white/accent around our glass. Opt out: square
    // OS corners, our CSS rounding defines the shape instead. Win10 ignores
    // this attribute (harmless E_INVALIDARG).
    let corner_pref = DWMWCP_DONOTROUND;
    let corners = unsafe {
        DwmSetWindowAttribute(
            hwnd,
            DWMWA_WINDOW_CORNER_PREFERENCE,
            &corner_pref as *const _ as _,
            std::mem::size_of_val(&corner_pref) as u32,
        )
    };
    let margins = MARGINS {
        cxLeftWidth: 0,
        cxRightWidth: 0,
        cyTopHeight: 0,
        cyBottomHeight: 0,
    };
    let frame = unsafe { DwmExtendFrameIntoClientArea(hwnd, &margins) };
    // Drop the thin-border style bit (THICKFRAME stays, so edges remain
    // resizable) and force DWM to recalc the frame without it.
    let style_msg = unsafe {
        let style = GetWindowLongW(hwnd, GWL_STYLE) as u32;
        let cleaned = style & !WS_BORDER.0;
        if cleaned != style {
            SetWindowLongW(hwnd, GWL_STYLE, cleaned as i32);
            let _ = SetWindowPos(
                hwnd,
                None,
                0,
                0,
                0,
                0,
                SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED,
            );
            "style-cleaned"
        } else {
            "no-thin-border-bit"
        }
    };
    // Proof-of-run marker (debug builds only — never write temp files in release).
    let sub = strip_os_frame(hwnd);
    #[cfg(debug_assertions)]
    let _ = std::fs::write(
        std::env::temp_dir().join("tyx-border.txt"),
        format!("dwm_border_color {border_note} corners={corners:?} extend_frame result={frame:?} {style_msg} subclass={sub}"),
    );
    #[cfg(not(debug_assertions))]
    let _ = sub;
}

/// Window proc: removes the OS frame (and with it the accent border) by
/// reporting zero non-client area, and clamps maximized size to the work
/// area so the taskbar stays visible. Everything else passes through to
/// the previous proc (wry/Tauri) untouched.
#[cfg(windows)]
unsafe extern "system" fn frameless_wnd_proc(
    hwnd: windows::Win32::Foundation::HWND,
    msg: u32,
    wparam: windows::Win32::Foundation::WPARAM,
    lparam: windows::Win32::Foundation::LPARAM,
) -> windows::Win32::Foundation::LRESULT {
    use windows::Win32::{
        Foundation::LRESULT,
        Graphics::Gdi::{GetMonitorInfoW, MonitorFromWindow, MONITORINFO, MONITOR_DEFAULTTONEAREST},
        UI::WindowsAndMessaging::{
            CallWindowProcW, DefWindowProcW, MINMAXINFO, WM_GETMINMAXINFO, WM_NCCALCSIZE, WNDPROC,
        },
    };
    match msg {
        WM_NCCALCSIZE => LRESULT(0),
        WM_GETMINMAXINFO => {
            let mon = MonitorFromWindow(hwnd, MONITOR_DEFAULTTONEAREST);
            let mut mi: MONITORINFO = std::mem::zeroed();
            mi.cbSize = std::mem::size_of::<MONITORINFO>() as u32;
            if GetMonitorInfoW(mon, &mut mi).as_bool() {
                let info = &mut *(lparam.0 as *mut MINMAXINFO);
                info.ptMaxPosition.x = mi.rcWork.left;
                info.ptMaxPosition.y = mi.rcWork.top;
                info.ptMaxSize.x = mi.rcWork.right - mi.rcWork.left;
                info.ptMaxSize.y = mi.rcWork.bottom - mi.rcWork.top;
                return LRESULT(0);
            }
            let old: WNDPROC = std::mem::transmute(OLD_PROC.load(std::sync::atomic::Ordering::SeqCst));
            if old.is_some() {
                CallWindowProcW(old, hwnd, msg, wparam, lparam)
            } else {
                DefWindowProcW(hwnd, msg, wparam, lparam)
            }
        }
        _ => {
            let old: WNDPROC = std::mem::transmute(OLD_PROC.load(std::sync::atomic::Ordering::SeqCst));
            if old.is_some() {
                CallWindowProcW(old, hwnd, msg, wparam, lparam)
            } else {
                DefWindowProcW(hwnd, msg, wparam, lparam)
            }
        }
    }
}

/// Previous window proc (wry/Tauri's), chained from ours.
#[cfg(windows)]
static OLD_PROC: std::sync::atomic::AtomicUsize = std::sync::atomic::AtomicUsize::new(0);

/// Hook the window proc once (self-healing: re-hooks if anything detached
/// us), then force a frame recalc. Needs no manifest/comctl32 — plain
/// GWLP_WNDPROC chaining. Returns a status string for the proof file.
#[cfg(windows)]
fn strip_os_frame(hwnd: windows::Win32::Foundation::HWND) -> String {
    use std::sync::atomic::Ordering;
    use windows::Win32::{
        Foundation::GetLastError,
        UI::WindowsAndMessaging::{
            GetWindowLongPtrW, SetWindowLongPtrW, SetWindowPos, GWLP_WNDPROC, SWP_FRAMECHANGED,
            SWP_NOMOVE, SWP_NOSIZE, SWP_NOZORDER,
        },
    };
    let current = unsafe { GetWindowLongPtrW(hwnd, GWLP_WNDPROC) };
    if current == frameless_wnd_proc as usize as isize {
        return "already-hooked".to_string();
    }
    let prev = unsafe { SetWindowLongPtrW(hwnd, GWLP_WNDPROC, frameless_wnd_proc as usize as isize) };
    if prev == 0 {
        let err = unsafe { GetLastError() };
        return format!("hook-failed:{err:?}");
    }
    OLD_PROC.store(prev as usize, Ordering::SeqCst);
    unsafe {
        let _ = SetWindowPos(
            hwnd,
            None,
            0,
            0,
            0,
            0,
            SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED,
        );
    }
    "hooked".to_string()
}

fn main() {
    tauri::Builder::default()
        .manage(auth::AuthAutoState::default())
        .setup(|app| {
            // Kill the DWM accent seam before first paint.
            #[cfg(windows)]
            if let Some(win) = app.get_webview_window("main") {
                if let Ok(hwnd) = win.hwnd() {
                    paint_hwnd(hwnd);
                }
            }
            Ok(())
        })
        // Re-apply whenever the MAIN window gains focus OR resizes: the OS can
        // restore its standard frame when the window is shown/restyled,
        // undoing setup(). Idempotent and cheap — keeps the seam gone.
        // NOTE: main window ONLY. Other windows (e.g. game-console) keep
        // their own decorations — stripping them kills native min/max/close.
        .on_window_event(|window, event| {
            if window.label() != "main" {
                return;
            }
            if matches!(
                event,
                tauri::WindowEvent::Focused(true) | tauri::WindowEvent::Resized(_)
            ) {
                #[cfg(windows)]
                if let Ok(hwnd) = window.hwnd() {
                    paint_hwnd(hwnd);
                }
            }
        })
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_http::init())
        .plugin(tauri_plugin_store::Builder::new().build())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_process::init())
        .plugin(tauri_plugin_updater::Builder::new().build())
        .invoke_handler(tauri::generate_handler![
            launcher_info,
            instances::instances_list,
            instances::instance_create,
            instances::instance_delete,
            instances::instance_touch,
            instances::instance_update_settings,
            instances::instance_list_files,
            instances::instance_delete_file,
            instances::instance_toggle_file,
            instances::instance_folder,
            instances::open_instance_path,
            instances::instance_set_icon,
            instances::instance_icon_data,
            instances::instance_file_data,
            minecraft::mc_versions_fallback,
            minecraft::loader_record,
            java::java_detect,
            java::java_ensure,
            auth::offline_uuid,
            auth::ms_auth_begin,
            auth::ms_auth_wait,
            auth::ms_auth_cancel,
            auth::ms_auto_login,
            auth::ms_auth_exchange,
            auth::ms_device_start,
            auth::ms_device_poll,
            auth::ms_refresh,
            auth::ms_login_with_xbox,
            auth::ms_profile,
            auth::ms_equip_cape,
            auth::ms_equip_skin,
            launch::launch_preview,
            launch::launch_game,
            launch::stop_game,
            modpack::modpack_install,
            modpack::modpack_export,
            servers::server_ping,
        ])
        .run(tauri::generate_context!())
        .expect("failed to run Tyx Launcher");
}
