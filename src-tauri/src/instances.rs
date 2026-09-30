//! Instance store — each instance is an isolated folder:
//!
//!   instances/<id>/
//!     instance.json   – metadata + per-instance settings
//!     loader.json     – loader install record (Phase 2: recorded, not executed)
//!     mods/ resourcepacks/ shaderpacks/ saves/ screenshots/ logs/ versions/
//!
//! All commands are plain std::fs + serde_json so they compile with zero
//! extra crates beyond Tauri itself.

use crate::util::{instance_dir, instances_root, now_millis, slugify};
use serde::{Deserialize, Serialize};
use tauri::AppHandle;

pub const SUBDIRS: &[&str] = &[
    "mods",
    "resourcepacks",
    "shaderpacks",
    "saves",
    "screenshots",
    "logs",
    "versions",
];

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct InstanceSettings {
    pub ram_mb: u32,
    pub java_path: String,
    pub jvm_args: String,
    pub res_w: u32,
    pub res_h: u32,
}

impl Default for InstanceSettings {
    fn default() -> Self {
        Self {
            ram_mb: 4096,
            java_path: String::new(), // empty = auto-detect at launch
            jvm_args: String::new(),
            res_w: 1280,
            res_h: 720,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct InstanceMeta {
    pub id: String,
    pub name: String,
    pub version: String,
    pub loader: String,
    pub loader_version: String,
    pub icon: String,
    pub created: u64,
    pub last_played: Option<u64>,
    pub path: String,
    pub settings: InstanceSettings,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FileEntry {
    pub name: String,
    pub size: u64,
    pub modified: u64,
    pub is_dir: bool,
}

fn read_meta(dir: &std::path::Path) -> Option<InstanceMeta> {
    let raw = std::fs::read_to_string(dir.join("instance.json")).ok()?;
    serde_json::from_str(&raw).ok()
}

#[tauri::command]
pub fn instances_list(app: AppHandle) -> Result<Vec<InstanceMeta>, String> {
    let root = instances_root(&app)?;
    let mut out = Vec::new();
    let entries = std::fs::read_dir(&root).map_err(|e| e.to_string())?;
    for e in entries.flatten() {
        if e.path().join("instance.json").exists() {
            if let Some(m) = read_meta(&e.path()) {
                out.push(m);
            }
        }
    }
    out.sort_by(|a, b| b.created.cmp(&a.created));
    Ok(out)
}

#[tauri::command]
pub fn instance_create(
    app: AppHandle,
    name: String,
    version: String,
    loader: String,
    icon: String,
) -> Result<InstanceMeta, String> {
    let name = name.trim();
    if name.is_empty() {
        return Err("Instance name is empty".into());
    }
    if version.trim().is_empty() {
        return Err("Minecraft version is empty".into());
    }
    let id = format!("{}-{}", slugify(name), now_millis() % 1_000_000);
    let dir = instance_dir(&app, &id)?;
    std::fs::create_dir_all(&dir).map_err(|e| e.to_string())?;
    for sub in SUBDIRS {
        std::fs::create_dir_all(dir.join(sub)).map_err(|e| e.to_string())?;
    }
    let meta = InstanceMeta {
        id: id.clone(),
        name: name.to_string(),
        version: version.trim().to_string(),
        loader: if loader.is_empty() {
            "Vanilla".to_string()
        } else {
            loader
        },
        loader_version: String::new(),
        icon: if icon.is_empty() { "pickaxe".into() } else { icon },
        created: now_millis(),
        last_played: None,
        path: dir.to_string_lossy().to_string(),
        settings: InstanceSettings::default(),
    };
    std::fs::write(
        dir.join("instance.json"),
        serde_json::to_string_pretty(&meta).map_err(|e| e.to_string())?,
    )
    .map_err(|e| e.to_string())?;
    Ok(meta)
}

#[tauri::command]
pub fn instance_delete(app: AppHandle, id: String) -> Result<(), String> {
    let dir = instance_dir(&app, &id)?;
    if !dir.exists() {
        return Ok(());
    }
    std::fs::remove_dir_all(&dir).map_err(|e| e.to_string())
}

#[tauri::command]
pub fn instance_touch(app: AppHandle, id: String) -> Result<(), String> {
    let dir = instance_dir(&app, &id)?;
    let raw = std::fs::read_to_string(dir.join("instance.json")).map_err(|e| e.to_string())?;
    let mut meta: InstanceMeta = serde_json::from_str(&raw).map_err(|e| e.to_string())?;
    meta.last_played = Some(now_millis());
    std::fs::write(
        dir.join("instance.json"),
        serde_json::to_string_pretty(&meta).map_err(|e| e.to_string())?,
    )
    .map_err(|e| e.to_string())?;
    Ok(())
}

#[tauri::command]
pub fn instance_update_settings(
    app: AppHandle,
    id: String,
    settings: InstanceSettings,
) -> Result<InstanceMeta, String> {
    if settings.ram_mb < 512 || settings.ram_mb > 32768 {
        return Err("RAM must be between 512 and 32768 MB".into());
    }
    let dir = instance_dir(&app, &id)?;
    let raw = std::fs::read_to_string(dir.join("instance.json")).map_err(|e| e.to_string())?;
    let mut meta: InstanceMeta = serde_json::from_str(&raw).map_err(|e| e.to_string())?;
    meta.settings = settings;
    std::fs::write(
        dir.join("instance.json"),
        serde_json::to_string_pretty(&meta).map_err(|e| e.to_string())?,
    )
    .map_err(|e| e.to_string())?;
    Ok(meta)
}

/// List files of one instance sub-folder (mods / saves / screenshots / logs …).
#[tauri::command]
pub fn instance_list_files(
    app: AppHandle,
    id: String,
    kind: String,
) -> Result<Vec<FileEntry>, String> {
    let allowed = ["mods", "resourcepacks", "shaderpacks", "saves", "screenshots", "logs", "versions"];
    if !allowed.contains(&kind.as_str()) {
        return Err("invalid folder kind".into());
    }
    let dir = instance_dir(&app, &id)?.join(&kind);
    if !dir.exists() {
        return Ok(vec![]);
    }
    let mut out = Vec::new();
    for e in std::fs::read_dir(&dir).map_err(|e| e.to_string())?.flatten() {
        let md = match e.metadata() {
            Ok(m) => m,
            Err(_) => continue,
        };
        let modified = md
            .modified()
            .ok()
            .and_then(|t| t.duration_since(std::time::UNIX_EPOCH).ok())
            .map(|d| d.as_millis() as u64)
            .unwrap_or(0);
        out.push(FileEntry {
            name: e.file_name().to_string_lossy().to_string(),
            size: md.len(),
            modified,
            is_dir: md.is_dir(),
        });
    }
    out.sort_by(|a, b| a.name.to_lowercase().cmp(&b.name.to_lowercase()));
    Ok(out)
}

/// Delete one file inside an instance sub-folder (e.g. remove a mod jar).
#[tauri::command]
pub fn instance_delete_file(
    app: AppHandle,
    id: String,
    kind: String,
    name: String,
) -> Result<(), String> {
    // Allowlist the sub-folder — otherwise `kind=../../..` escapes the
    // instance dir and this becomes an arbitrary file/dir delete.
    const ALLOWED_DELETE: &[&str] = &[
        "mods",
        "resourcepacks",
        "shaderpacks",
        "saves",
        "screenshots",
        "logs",
        "config",
    ];
    if !ALLOWED_DELETE.contains(&kind.as_str()) {
        return Err("invalid folder kind".into());
    }
    if name.is_empty() || name.contains('/') || name.contains('\\') || name.contains("..") {
        return Err("invalid file name".into());
    }
    let dir = instance_dir(&app, &id)?.join(&kind);
    let target = dir.join(&name);
    if !target.exists() {
        return Ok(());
    }
    if target.is_dir() {
        std::fs::remove_dir_all(&target).map_err(|e| e.to_string())
    } else {
        std::fs::remove_file(&target).map_err(|e| e.to_string())
    }
}

/// Enable/disable a mod jar (Modrinth parity): toggles the `.disabled`
/// suffix — `sodium.jar` ↔ `sodium.jar.disabled`. Directories refused.
#[tauri::command]
pub fn instance_toggle_file(
    app: AppHandle,
    id: String,
    kind: String,
    name: String,
) -> Result<String, String> {
    if name.is_empty() || name.contains('/') || name.contains('\\') || name.contains("..") {
        return Err("invalid file name".into());
    }
    let allowed = ["mods", "resourcepacks", "shaderpacks"];
    if !allowed.contains(&kind.as_str()) {
        return Err("toggle only applies to mods/resourcepacks/shaderpacks".into());
    }
    let dir = instance_dir(&app, &id)?.join(&kind);
    let src = dir.join(&name);
    if !src.exists() || src.is_dir() {
        return Err("file not found".into());
    }
    let next = if name.ends_with(".disabled") {
        name.trim_end_matches(".disabled").to_string()
    } else if name.ends_with(".off") {
        name.trim_end_matches(".off").to_string()
    } else {
        format!("{name}.disabled")
    };
    if next.contains('/') || next.contains('\\') || next.contains("..") {
        return Err("invalid file name".into());
    }
    std::fs::rename(&src, dir.join(&next)).map_err(|e| e.to_string())?;
    Ok(next)
}

/// Absolute path of the instance folder (frontend opens it natively).
/// Optional `subdir` opens a known child folder (mods, saves, …), creating
/// it first so Explorer always has something to show.
#[tauri::command]
pub fn instance_folder(app: AppHandle, id: String, subdir: Option<String>) -> Result<String, String> {
    let dir = instance_dir(&app, &id)?;
    let target = match subdir.as_deref().map(str::trim) {
        None | Some("") => dir,
        Some(s) => {
            if !ALLOWED_SUBDIR.contains(&s) || s.contains('/') || s.contains('\\') || s.contains("..") {
                return Err("unknown folder".into());
            }
            let d = dir.join(s);
            std::fs::create_dir_all(&d).map_err(|e| e.to_string())?;
            d
        }
    };
    Ok(target.to_string_lossy().to_string())
}

const ALLOWED_SUBDIR: &[&str] = &[
    "mods",
    "resourcepacks",
    "shaderpacks",
    "saves",
    "screenshots",
    "logs",
    "config",
    "versions",
    "libraries",
    "assets",
];

fn clean_filename(name: &str) -> Result<String, String> {
    let n = name.trim();
    if n.is_empty() || n.contains('/') || n.contains('\\') || n.contains("..") {
        return Err("invalid file name".into());
    }
    Ok(n.to_string())
}

fn open_native(path: &std::path::Path, select_file: bool) -> Result<(), String> {
    #[cfg(windows)]
    {
        if select_file && path.is_file() {
            std::process::Command::new("explorer")
                .arg("/select,")
                .arg(path)
                .spawn()
                .map_err(|e| format!("couldn't open Explorer: {e}"))?;
        } else {
            let dir = if path.is_file() {
                path.parent().unwrap_or(path).as_os_str().to_owned()
            } else {
                path.as_os_str().to_owned()
            };
            std::process::Command::new("explorer")
                .arg(dir)
                .spawn()
                .map_err(|e| format!("couldn't open Explorer: {e}"))?;
        }
        return Ok(());
    }
    #[cfg(target_os = "macos")]
    {
        if select_file && path.is_file() {
            std::process::Command::new("open")
                .args(["-R", &path.to_string_lossy()])
                .spawn()
                .map_err(|e| format!("couldn't reveal in Finder: {e}"))?;
        } else {
            std::process::Command::new("open")
                .arg(path)
                .spawn()
                .map_err(|e| format!("couldn't open Finder: {e}"))?;
        }
        return Ok(());
    }
    #[cfg(not(any(windows, target_os = "macos")))]
    {
        let target = if select_file && path.is_file() {
            path.parent().unwrap_or(path).to_path_buf()
        } else {
            path.to_path_buf()
        };
        std::process::Command::new("xdg-open")
            .arg(target)
            .spawn()
            .map_err(|e| format!("couldn't open file manager: {e}"))?;
        return Ok(());
    }
}

/// Open an instance folder (or reveal one file) with the OS file manager.
/// Resolves everything server-side so the frontend never handles absolute
/// paths and shell-plugin scopes can't break it. Errors are returned —
/// never swallowed — so buttons can toast the real reason.
#[tauri::command]
pub fn open_instance_path(
    app: AppHandle,
    id: String,
    subdir: Option<String>,
    filename: Option<String>,
) -> Result<String, String> {
    let dir = instance_dir(&app, &id)?;
    if !dir.exists() {
        return Err("instance folder not found on disk".into());
    }
    let mut target = match subdir.as_deref().map(str::trim) {
        None | Some("") => dir,
        Some(s) => {
            if !ALLOWED_SUBDIR.contains(&s) || s.contains('/') || s.contains('\\') || s.contains("..") {
                return Err("unknown folder".into());
            }
            let d = dir.join(s);
            std::fs::create_dir_all(&d).map_err(|e| e.to_string())?;
            d
        }
    };
    let select_file = match filename.as_deref().map(str::trim) {
        None | Some("") => false,
        Some(n) => {
            let clean = clean_filename(n)?;
            target = target.join(clean);
            true
        }
    };
    // For plain folders the target now exists (created above). For files,
    // fall back to opening the parent when the file is gone.
    if select_file && !target.exists() {
        let parent = target.parent().map(|p| p.to_path_buf());
        if let Some(p) = parent {
            open_native(&p, false)?;
            return Ok(p.to_string_lossy().to_string());
        }
        return Err("file not found".into());
    }
    open_native(&target, select_file)?;
    Ok(target.to_string_lossy().to_string())
}

/// Icon image bytes as a data-URL (`file:…` icons only, "" otherwise).
/// Served from Rust so the frontend never touches the asset protocol
/// (no scope hassles, no WebView cache staleness).
#[tauri::command]
pub fn instance_icon_data(app: AppHandle, id: String) -> Result<String, String> {
    let dir = instance_dir(&app, &id)?;
    let raw = std::fs::read_to_string(dir.join("instance.json")).map_err(|e| e.to_string())?;
    let meta: InstanceMeta = serde_json::from_str(&raw).map_err(|e| e.to_string())?;
    let name = match meta.icon.strip_prefix("file:") {
        Some(n) => n.trim(),
        None => return Ok(String::new()),
    };
    // Strict filename guard: single plain image name, no traversal.
    if name.contains('/') || name.contains('\\') || name.contains("..") {
        return Err("invalid icon reference".into());
    }
    let lower = name.to_lowercase();
    let mime = if lower.ends_with(".png") {
        "image/png"
    } else if lower.ends_with(".jpg") || lower.ends_with(".jpeg") {
        "image/jpeg"
    } else if lower.ends_with(".webp") {
        "image/webp"
    } else {
        return Err("unsupported icon format".into());
    };
    let bytes = std::fs::read(dir.join(name)).map_err(|e| e.to_string())?;
    if bytes.len() > 2 * 1024 * 1024 {
        return Err("icon file too large".into());
    }
    Ok(format!("data:{mime};base64,{B64}", B64 = base64_encode(&bytes)))
}

/// Any instance file's bytes as a data-URL (screenshots viewer, …).
/// Same guards as the icon path: subdir allowlist, clean filename,
/// images only, ≤8 MB (screenshots run big).
#[tauri::command]
pub fn instance_file_data(
    app: AppHandle,
    id: String,
    kind: String,
    name: String,
) -> Result<String, String> {
    let dir = instance_dir(&app, &id)?;
    let sub = kind.trim();
    if !ALLOWED_SUBDIR.contains(&sub) || sub.contains('/') || sub.contains('\\') || sub.contains("..") {
        return Err("unknown folder".into());
    }
    let clean = clean_filename(&name)?;
    let lower = clean.to_lowercase();
    let mime = if lower.ends_with(".png") {
        "image/png"
    } else if lower.ends_with(".jpg") || lower.ends_with(".jpeg") {
        "image/jpeg"
    } else if lower.ends_with(".webp") {
        "image/webp"
    } else {
        return Err("preview supports images only".into());
    };
    let bytes = std::fs::read(dir.join(sub).join(clean)).map_err(|e| e.to_string())?;
    if bytes.len() > 8 * 1024 * 1024 {
        return Err("file too large to preview".into());
    }
    Ok(format!("data:{mime};base64,{B64}", B64 = base64_encode(&bytes)))
}

fn base64_encode(bytes: &[u8]) -> String {
    const ALPHABET: &[u8; 64] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    let mut out = String::with_capacity(bytes.len() / 3 * 4 + 4);
    for chunk in bytes.chunks(3) {
        let b0 = chunk[0] as u32;
        let b1 = *chunk.get(1).unwrap_or(&0) as u32;
        let b2 = *chunk.get(2).unwrap_or(&0) as u32;
        let n = (b0 << 16) | (b1 << 8) | b2;
        out.push(ALPHABET[((n >> 18) & 63) as usize] as char);
        out.push(ALPHABET[((n >> 12) & 63) as usize] as char);
        out.push(if chunk.len() > 1 { ALPHABET[((n >> 6) & 63) as usize] as char } else { '=' });
        out.push(if chunk.len() > 2 { ALPHABET[(n & 63) as usize] as char } else { '=' });
    }
    out
}

/// Set the instance icon. Two shapes:
///   - builtin id / legacy emoji → stored as-is (shown as custom SVG);
///   - `file:<absolute image path>` → validated (png/jpg/webp, ≤2 MB),
///     copied into the instance dir as `icon-<millis>.png` (unique name =
///     no WebView cache staleness), old `icon-*.png` cleaned up.
#[tauri::command]
pub fn instance_set_icon(app: AppHandle, id: String, icon: String) -> Result<InstanceMeta, String> {
    let dir = instance_dir(&app, &id)?;
    let raw = std::fs::read_to_string(dir.join("instance.json")).map_err(|e| e.to_string())?;
    let mut meta: InstanceMeta = serde_json::from_str(&raw).map_err(|e| e.to_string())?;

    let icon = icon.trim();
    if icon.is_empty() {
        return Err("empty icon".into());
    }
    let stored = if let Some(src) = icon.strip_prefix("file:") {
        let src_path = std::path::Path::new(src.trim());
        let ext = src_path
            .extension()
            .and_then(|e| e.to_str())
            .unwrap_or("")
            .to_lowercase();
        if !["png", "jpg", "jpeg", "webp"].contains(&ext.as_str()) {
            return Err("icon must be a PNG, JPG or WebP image".into());
        }
        let size = std::fs::metadata(src_path).map_err(|e| e.to_string())?.len();
        if size > 2 * 1024 * 1024 {
            return Err("icon file too large (max 2 MB)".into());
        }
        // Drop previous custom icons so the folder never accumulates them.
        if let Ok(rd) = std::fs::read_dir(&dir) {
            for e in rd.flatten() {
                let n = e.file_name().to_string_lossy().to_string();
                if n.starts_with("icon-") && n.ends_with(".png") {
                    let _ = std::fs::remove_file(e.path());
                }
            }
        }
        let dest_name = format!("icon-{}.png", crate::util::now_millis());
        std::fs::copy(src_path, dir.join(&dest_name)).map_err(|e| e.to_string())?;
        format!("file:{dest_name}")
    } else {
        if icon.chars().count() > 32 {
            return Err("invalid icon".into());
        }
        icon.to_string()
    };

    meta.icon = stored;
    std::fs::write(
        dir.join("instance.json"),
        serde_json::to_string_pretty(&meta).map_err(|e| e.to_string())?,
    )
    .map_err(|e| e.to_string())?;
    Ok(meta)
}
