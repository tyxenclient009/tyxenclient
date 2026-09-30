//! Launch pipeline.
//!
//! Two commands:
//!   - `launch_preview` — pure: show the exact JVM command before running
//!     (errors when no usable Java is present — it never downloads).
//!   - `launch_game`    — spawn in a background thread. If no Java runtime
//!     is configured/found, the thread provisions one automatically
//!     (Temurin JRE matched to the instance's MC version) while narrating
//!     progress into the log stream, then boots the game.
//!     Every line goes to `tyx://game-log` events + `logs/tyx-launch.log`.
//!
//! If the real client jar (`versions/<v>/<v>.jar`) is present the actual JVM
//! is spawned with piped stdout/stderr. Otherwise *simulation mode* runs
//! (still validates Java, still updates last-played) so the whole UX is
//! testable before the asset downloader lands.

use crate::instances::InstanceMeta;
use crate::util::instance_dir;
use serde::{Deserialize, Serialize};
use std::io::{BufRead, BufReader};
use tauri::{AppHandle, Emitter};

#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LaunchRequest {
    pub instance_id: String,
    pub username: String,
    pub uuid: String,
    #[serde(default)]
    pub token: String,
    pub ram_mb: u32,
    pub java_path: String,
    pub jvm_args: String,
    pub res_w: u32,
    pub res_h: u32,
    /// Direct-connect: appended as `--server <host> --port <port>`.
    /// Empty = main menu as usual. (Modrinth-style instant server join.)
    #[serde(default)]
    pub server: String,
    #[serde(default)]
    pub port: u16,
    /// Saved launcher servers → merged into the instance's servers.dat so
    /// they already show in the in-game Multiplayer list. Never fatal.
    #[serde(default)]
    pub servers: Vec<crate::servers::ServerEntry>,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct LaunchPreview {
    pub java: String,
    pub args: Vec<String>,
    pub workdir: String,
    pub ready: bool,
    pub summary: String,
}

#[derive(Debug, Clone, Serialize)]
pub struct GameLog {
    pub stream: String, // "sys" | "stdout" | "stderr"
    pub line: String,
    /// Which instance this line belongs to ("" = pre-launch/global).
    /// Lets the UI route logs + running state per game (Modrinth-style
    /// multi-launch: several instances can run side by side).
    #[serde(default)]
    pub instance_id: String,
}

/// Running games, keyed by pid (Modrinth-style: many instances at once).
/// Lets the UI show per-instance "Running" state and offer per-game Stop.
static RUNNING: std::sync::OnceLock<std::sync::Mutex<std::collections::HashMap<u32, String>>> =
    std::sync::OnceLock::new();
fn running(
) -> &'static std::sync::Mutex<std::collections::HashMap<u32, String>> {
    RUNNING.get_or_init(|| std::sync::Mutex::new(std::collections::HashMap::new()))
}
/// Instances with a launch thread active (downloading / verifying /
/// booting, no pid yet). Per-instance so hammering Play on ONE slow
/// instance can't stack downloads — while a DIFFERENT instance launches
/// freely in parallel.
static LAUNCHING: std::sync::OnceLock<std::sync::Mutex<std::collections::HashSet<String>>> =
    std::sync::OnceLock::new();
fn launching() -> &'static std::sync::Mutex<std::collections::HashSet<String>> {
    LAUNCHING.get_or_init(|| std::sync::Mutex::new(std::collections::HashSet::new()))
}

/// Clears this instance's LAUNCHING entry when its launch thread ends.
struct LaunchGuard {
    instance_id: String,
}
impl Drop for LaunchGuard {
    fn drop(&mut self) {
        if let Ok(mut set) = launching().lock() {
            set.remove(&self.instance_id);
        }
    }
}

fn launch_in_progress(instance_id: &str) -> bool {
    launching()
        .lock()
        .map(|s| s.contains(instance_id))
        .unwrap_or(false)
}

fn running_file(app: &AppHandle) -> Option<std::path::PathBuf> {
    crate::util::data_root(app).ok().map(|r| r.join("running.json"))
}

fn track_running(app: &AppHandle, pid: u32, instance_id: &str) {
    if let Ok(mut map) = running().lock() {
        map.insert(pid, instance_id.to_string());
    }
    // Persisted too: a launcher restart must not orphan running games
    // behind a dead "no game is running" Stop button. Stored as an array
    // so several games survive a restart.
    if let Some(path) = running_file(app) {
        let list: Vec<serde_json::Value> = running()
            .lock()
            .map(|m| {
                m.iter()
                    .map(|(p, i)| serde_json::json!({ "pid": p, "instanceId": i }))
                    .collect()
            })
            .unwrap_or_default();
        let _ = std::fs::write(path, serde_json::json!(list).to_string());
    }
}

fn clear_running(app: &AppHandle, pid: u32) {
    if let Ok(mut map) = running().lock() {
        map.remove(&pid);
    }
    if let Some(path) = running_file(app) {
        // Rewrite the survivors (or drop the file when none remain).
        let rest: Vec<serde_json::Value> = running()
            .lock()
            .map(|m| {
                m.iter()
                    .map(|(p, i)| serde_json::json!({ "pid": p, "instanceId": i }))
                    .collect()
            })
            .unwrap_or_default();
        if rest.is_empty() {
            let _ = std::fs::remove_file(path);
        } else {
            let _ = std::fs::write(path, serde_json::json!(rest).to_string());
        }
    }
}

/// Is this OS pid still alive? Best-effort (used to verify kills and to
/// resurrect tracking after a launcher restart).
fn pid_alive(pid: u32) -> bool {
    #[cfg(windows)]
    {
        let out = std::process::Command::new("tasklist")
            .args(["/FI", &format!("PID eq {pid}"), "/FO", "CSV", "/NH"])
            .output();
        match out {
            Ok(o) => {
                let text = String::from_utf8_lossy(&o.stdout);
                text.lines().any(|l| l.contains(&format!(",\"{pid}\",")))
            }
            Err(_) => true, // can't probe — assume alive, never strand Stop
        }
    }
    #[cfg(not(windows))]
    {
        std::process::Command::new("kill")
            .args(["-0", &pid.to_string()])
            .output()
            .map(|o| o.status.success())
            .unwrap_or(true)
    }
}

#[derive(Debug, Clone)]
struct RunningSnap {
    pid: u32,
    instance_id: String,
}

/// Memory first, pid-file second (launcher restart), liveness-verified.
/// A dead pid entry self-heals instead of blocking future launches.
fn running_snapshot(app: &AppHandle, instance_id: &str) -> Option<RunningSnap> {
    if let Ok(map) = running().lock() {
        if let Some((pid, inst)) = map.iter().find(|(_, i)| i.as_str() == instance_id) {
            return Some(RunningSnap { pid: *pid, instance_id: inst.clone() });
        }
    }
    // Pid file may hold several games (array) or the legacy single object.
    let path = running_file(app)?;
    let raw = std::fs::read_to_string(&path).ok()?;
    let v: serde_json::Value = serde_json::from_str(&raw).ok()?;
    let entries: Vec<(u32, String)> = if let Some(arr) = v.as_array() {
        arr.iter()
            .filter_map(|e| {
                Some((
                    e.get("pid")?.as_u64()? as u32,
                    e.get("instanceId")?.as_str()?.to_string(),
                ))
            })
            .collect()
    } else {
        vec![(
            v.get("pid")?.as_u64()? as u32,
            v.get("instanceId")?.as_str()?.to_string(),
        )]
    };
    // Resurrect live ones into memory; prune the dead (file rewrite keeps
    // survivors only, drops the file when none remain).
    let mut live: Vec<(u32, String)> = Vec::new();
    for (pid, inst) in entries {
        if pid_alive(pid) {
            if inst == instance_id {
                if let Ok(mut map) = running().lock() {
                    map.insert(pid, inst.clone());
                }
                return Some(RunningSnap { pid, instance_id: inst });
            }
            live.push((pid, inst));
        }
    }
    if let Ok(mut map) = running().lock() {
        for (pid, inst) in &live {
            map.entry(*pid).or_insert_with(|| inst.clone());
        }
    }
    if live.is_empty() {
        let _ = std::fs::remove_file(path);
    } else {
        let rest: Vec<serde_json::Value> = live
            .iter()
            .map(|(p, i)| serde_json::json!({ "pid": p, "instanceId": i }))
            .collect();
        let _ = std::fs::write(path, serde_json::json!(rest).to_string());
    }
    // Requested instance not found among the live ones.
    if let Ok(map) = running().lock() {
        if let Some((pid, inst)) = map.iter().find(|(_, i)| i.as_str() == instance_id) {
            return Some(RunningSnap { pid: *pid, instance_id: inst.clone() });
        }
    }
    None
}

/// Every tracked game (memory + pruned pid file), liveness-verified.
fn all_running(app: &AppHandle) -> Vec<RunningSnap> {
    let mut out: Vec<RunningSnap> = running()
        .lock()
        .map(|m| {
            m.iter()
                .map(|(p, i)| RunningSnap { pid: *p, instance_id: i.clone() })
                .collect()
        })
        .unwrap_or_default();
    if let Some(path) = running_file(app) {
        if let Ok(raw) = std::fs::read_to_string(&path) {
            if let Ok(v) = serde_json::from_str::<serde_json::Value>(&raw) {
                let entries: Vec<(u32, String)> = if let Some(arr) = v.as_array() {
                    arr.iter()
                        .filter_map(|e| {
                            Some((
                                e.get("pid")?.as_u64()? as u32,
                                e.get("instanceId")?.as_str()?.to_string(),
                            ))
                        })
                        .collect()
                } else if let (Some(p), Some(i)) = (
                    v.get("pid").and_then(|n| n.as_u64()),
                    v.get("instanceId").and_then(|s| s.as_str()),
                ) {
                    vec![(p as u32, i.to_string())]
                } else {
                    Vec::new()
                };
                for (pid, inst) in entries {
                    if pid_alive(pid) && !out.iter().any(|s| s.pid == pid) {
                        out.push(RunningSnap { pid, instance_id: inst });
                    }
                }
            }
        }
    }
    out
}

fn kill_pid(pid: u32) {
    #[cfg(windows)]
    {
        let _ = std::process::Command::new("taskkill")
            .args(["/F", "/PID", &pid.to_string()])
            .output();
    }
    #[cfg(not(windows))]
    {
        let _ = std::process::Command::new("kill")
            .args(["-9", &pid.to_string()])
            .output();
    }
}

/// Stop game(s): one instance when `instance_id` is given, everything
/// running when omitted (console Stop-all). The launch thread's `wait()`
/// then observes the exit and the UI flips back via game-log.
/// Verified by liveness probe — a stale pid reports success instead of a
/// dead Stop button, and a failed kill says so honestly.
#[tauri::command]
pub fn stop_game(app: AppHandle, instance_id: Option<String>) -> Result<String, String> {
    let targets: Vec<RunningSnap> = match instance_id.as_deref().map(str::trim) {
        None | Some("") => all_running(&app),
        Some(id) => running_snapshot(&app, id).into_iter().collect(),
    };
    if targets.is_empty() {
        return Err("No game is running.".into());
    }
    let mut stopped: Vec<String> = Vec::new();
    let mut failed: Vec<String> = Vec::new();
    for g in targets {
        // Clear first so a wedged kill can never strand the UI on Stop.
        clear_running(&app, g.pid);
        kill_pid(g.pid);
        std::thread::sleep(std::time::Duration::from_millis(500));
        if pid_alive(g.pid) {
            // Still up — put tracking back so Stop stays available for retry.
            track_running(&app, g.pid, &g.instance_id);
            failed.push(format!("pid {} (still running)", g.pid));
        } else {
            emit_inst(&app, &g.instance_id, "sys", "[tyx] stopped by user (process exited)");
            stopped.push(g.instance_id);
        }
    }
    if stopped.is_empty() {
        return Err(format!(
            "couldn't stop {} — close the game window(s) manually, then press Play.",
            failed.join(", ")
        ));
    }
    Ok(stopped.join(", "))
}

fn split_args(raw: &str) -> Vec<String> {
    let mut out = Vec::new();
    let mut cur = String::new();
    let mut quoted = false;
    for c in raw.chars() {
        match c {
            '"' => quoted = !quoted,
            c if c.is_whitespace() && !quoted => {
                if !cur.is_empty() {
                    out.push(std::mem::take(&mut cur));
                }
            }
            _ => cur.push(c),
        }
    }
    if !cur.is_empty() {
        out.push(cur);
    }
    out
}

fn read_meta(dir: &std::path::Path) -> Result<InstanceMeta, String> {
    let raw = std::fs::read_to_string(dir.join("instance.json")).map_err(|e| e.to_string())?;
    serde_json::from_str(&raw).map_err(|e| e.to_string())
}

/// The runtime the version json itself declares (`javaVersion.majorVersion`).
/// Authoritative — version-string mappings go stale (e.g. Mojang's 26.x
/// scheme), the json does not.
fn required_java_major(dir: &std::path::Path, version: &str) -> Option<u32> {
    let raw =
        std::fs::read_to_string(dir.join("versions").join(version).join(format!("{version}.json"))).ok()?;
    let v: serde_json::Value = serde_json::from_str(&raw).ok()?;
    v.get("javaVersion")?
        .get("majorVersion")?
        .as_u64()
        .map(|n| n as u32)
}

fn resolve_java_sync(req: &LaunchRequest) -> Result<String, String> {
    // Sync contexts (pure preview): explicit path only. Full detection +
    // auto-install happen async (launch thread / java_ensure command).
    if !req.java_path.trim().is_empty() {
        let p = std::path::Path::new(req.java_path.trim());
        if p.exists() {
            return Ok(p.to_string_lossy().to_string());
        }
        return Err(format!("configured Java not found: {}", req.java_path.trim()));
    }
    Err("No Java configured — launch once (auto-installs) or set one in Settings".to_string())
}

fn build_preview(app: &AppHandle, req: &LaunchRequest, java: &str) -> Result<LaunchPreview, String> {
    let dir = instance_dir(app, &req.instance_id)?;
    let meta = read_meta(&dir)?;

    let ram = req.ram_mb.clamp(512, 32768);
    let client_jar = dir
        .join("versions")
        .join(&meta.version)
        .join(format!("{}.jar", meta.version));
    let ready = client_jar.exists();

    let natives = dir.join("natives").to_string_lossy().to_string();
    let libs = if ready {
        client_jar.to_string_lossy().to_string()
    } else {
        "<libraries-not-downloaded-yet>".to_string()
    };

    let mut args: Vec<String> = vec![
        format!("-Xms{}M", 512.min(ram)),
        format!("-Xmx{ram}M"),
    ];
    args.extend(split_args(&req.jvm_args));
    args.extend(vec![
        format!("-Djava.library.path={natives}"),
        "-cp".into(),
        libs,
        "net.minecraft.client.main.Main".into(),
        "--username".into(),
        req.username.clone(),
        "--uuid".into(),
        req.uuid.clone(),
        "--accessToken".into(),
        "0".into(),
        "--version".into(),
        meta.version.clone(),
        "--gameDir".into(),
        dir.to_string_lossy().to_string(),
        "--assetsDir".into(),
        dir.join("assets").to_string_lossy().to_string(),
        "--assetIndex".into(),
        meta.version.clone(),
        "--width".into(),
        req.res_w.max(640).to_string(),
        "--height".into(),
        req.res_h.max(360).to_string(),
    ]);

    Ok(LaunchPreview {
        summary: format!(
            "{} {} ({}) as {} with {} MB",
            meta.name, meta.version, meta.loader, req.username, ram
        ),
        java: java.to_string(),
        args,
        workdir: dir.to_string_lossy().to_string(),
        ready,
    })
}

#[tauri::command]
pub fn launch_preview(app: AppHandle, req: LaunchRequest) -> Result<LaunchPreview, String> {
    let java = resolve_java_sync(&req)?;
    build_preview(&app, &req, &java)
}

/// Same event, tagged with the owning instance so the UI can route
/// running-state (and future per-game consoles) with several games up.
fn emit_inst(app: &AppHandle, instance_id: &str, stream: &str, line: impl Into<String>) {
    let _ = app.emit(
        "tyx://game-log",
        GameLog {
            stream: stream.into(),
            line: line.into(),
            instance_id: instance_id.to_string(),
        },
    );
}

fn append_log(dir: &std::path::Path, line: &str) {
    use std::fmt::Write as _;
    let path = dir.join("logs").join("tyx-launch.log");
    let mut existing = std::fs::read_to_string(&path).unwrap_or_default();
    let _ = writeln!(existing, "{line}");
    if existing.len() > 262_144 {
        let skip = existing.len() - 262_144;
        if let Some(pos) = existing[skip..].find('\n') {
            existing = existing[skip + pos + 1..].to_string();
        }
    }
    let _ = std::fs::write(path, existing);
}

#[tauri::command]
pub fn launch_game(app: AppHandle, req: LaunchRequest) -> Result<u32, String> {
    let handle = app.clone();
    let launch_instance = req.instance_id.clone();
    std::thread::spawn(move || {
        // Same instance twice only orphans a JVM behind a dead Stop
        // button. Refuse loudly instead — but a DIFFERENT instance may
        // launch in parallel (Modrinth-style multi-launch).
        if let Some(g) = running_snapshot(&handle, &req.instance_id) {
            emit_inst(&handle, &req.instance_id,
                "stderr",
                format!(
                    "[tyx] instance {} is already running (pid {}) — stop it before launching it again.",
                    g.instance_id, g.pid
                ),
            );
            return;
        }
        // Same during the slow phases (download/verify have no pid yet):
        // one launch per instance at a time, extras get a clear message.
        if launch_in_progress(&req.instance_id) {
            emit_inst(&handle, &req.instance_id,
                "stderr",
                format!(
                    "[tyx] instance {} is already launching — watch the Console window instead of pressing Play again.",
                    req.instance_id
                ),
            );
            return;
        }
        if let Ok(mut set) = launching().lock() {
            set.insert(req.instance_id.clone());
        }
        let _guard = LaunchGuard { instance_id: launch_instance };
        let dir = match instance_dir(&handle, &req.instance_id) {
            Ok(d) => d,
            Err(e) => {
                emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] {e}"));
                return;
            }
        };
        let meta = match read_meta(&dir) {
            Ok(m) => m,
            Err(e) => {
                emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] cannot read instance: {e}"));
                return;
            }
        };

        // ── Java: explicit override → full detect → auto-install ────────
        let rt = tauri::async_runtime::handle();
        let mut java = if !req.java_path.trim().is_empty() {
            let p = req.java_path.trim().to_string();
            if !std::path::Path::new(&p).exists() {
                emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] configured Java not found: {p}"));
                emit_inst(&handle, &req.instance_id, "stderr", "[tyx] fix the path in instance Settings, or clear it for auto mode.");
                return;
            }
            p
        } else {
            let detected = rt.block_on(crate::java::java_detect(handle.clone()));
            match detected.into_iter().next() {
                Some(j) => j.path,
                None => {
                    let major = crate::java::java_major_for_mc(&meta.version);
                    emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] no Java found — installing Temurin JRE {major} automatically (~200 MB, one-time)…"));
                    match rt.block_on(crate::java::java_ensure(handle.clone(), major)) {
                        Ok(j) => {
                            emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] Java ready: {}", j.path));
                            j.path
                        }
                        Err(e) => {
                            emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] automatic Java install failed: {e}"));
                            emit_inst(&handle, &req.instance_id, "stderr", "[tyx] install Temurin manually and set it in Settings → Runtime.");
                            return;
                        }
                    }
                }
            }
        };

        let mut preview = match build_preview(&handle, &req, &java) {
            Ok(p) => p,
            Err(e) => {
                emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] {e}"));
                return;
            }
        };

        // Mark last-played immediately so Home re-sorts without waiting.
        if let Ok(raw) = std::fs::read_to_string(dir.join("instance.json")) {
            if let Ok(mut m) = serde_json::from_str::<InstanceMeta>(&raw) {
                m.last_played = Some(crate::util::now_millis());
                if let Ok(s) = serde_json::to_string_pretty(&m) {
                    let _ = std::fs::write(dir.join("instance.json"), s);
                }
            }
        }

        emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] {}", preview.summary));
        emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] java: {}", preview.java));
        append_log(&dir, &format!("[tyx] launch: {}", preview.summary));

        // Saved servers → in-game Multiplayer list (merge, never fatal).
        if !req.servers.is_empty() {
            match crate::servers::sync_servers_dat(&dir, &req.servers) {
                Ok(n) => emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] multiplayer list synced ({n} servers)")),
                Err(e) => emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] servers.dat sync skipped: {e}")),
            }
        }

        // No console window flashing for the probe on Windows.
        let mut probe_cmd = std::process::Command::new(&preview.java);
        probe_cmd.arg("-version");
        #[cfg(windows)]
        {
            use std::os::windows::process::CommandExt;
            probe_cmd.creation_flags(0x08000000);
        }
        match probe_cmd.output() {
            Ok(o) => {
                let text = format!(
                    "{}{}",
                    String::from_utf8_lossy(&o.stdout),
                    String::from_utf8_lossy(&o.stderr)
                );
                for line in text.lines().take(3) {
                    emit_inst(&handle, &req.instance_id, "sys", format!("[java] {line}"));
                }
            }
            Err(e) => {
                emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] cannot run Java: {e}"));
                return;
            }
        }

        // ── Game files: download once, verify every launch ─────────────
        let client_jar_probe = dir
            .join("versions")
            .join(&meta.version)
            .join(format!("{}.jar", meta.version));
        if !client_jar_probe.exists() {
            emit_inst(&handle, &req.instance_id,
                "sys",
                format!(
                    "[tyx] first launch — downloading game files for {} (~300–600 MB, one-time)…",
                    meta.version
                ),
            );
            emit_inst(&handle, &req.instance_id,
                "sys",
                "[tyx] watch the progress bar + [dl] lines below for overall % · MB · speed · ETA — this takes a few minutes on first run, do not close.",
            );
        } else {
            emit_inst(&handle, &req.instance_id,
                "sys",
                format!(
                    "[tyx] verifying game files for {} (fast when cached)…",
                    meta.version
                ),
            );
        }
        let files = match rt.block_on(crate::assets::ensure_assets(
            &handle,
            &req.instance_id,
            &meta.version,
        )) {
            Ok(f) => f,
            Err(e) => {
                emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] game download failed: {e}"));
                emit_inst(&handle, &req.instance_id, "stderr", "[tyx] check your connection and press Play again.");
                return;
            }
        };

        // ── Authoritative Java check: the resolved version json wins ────
        // A stale mapping or a user override pointing at the wrong major
        // boots straight into "Unrecognized option" JVM death — detect it
        // here and self-heal to the declared runtime instead.
        let required =
            required_java_major(&dir, &meta.version).unwrap_or_else(|| crate::java::java_major_for_mc(&meta.version));
        if crate::java::java_major_of_path(&java) != Some(required) {
            let have = crate::java::java_major_of_path(&java)
                .map(|v| v.to_string())
                .unwrap_or_else(|| "none".into());
            emit_inst(&handle, &req.instance_id,
                "sys",
                format!("[tyx] version needs Java {required} (found {have}) — switching automatically…"),
            );
            match rt.block_on(crate::java::java_ensure(handle.clone(), required)) {
                Ok(j) => {
                    emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] Java ready: {}", j.path));
                    java = j.path;
                    preview = match build_preview(&handle, &req, &java) {
                        Ok(p) => p,
                        Err(e) => {
                            emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] {e}"));
                            return;
                        }
                    };
                }
                Err(e) => {
                    emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] automatic Java install failed: {e}"));
                    emit_inst(&handle, &req.instance_id, "stderr", "[tyx] install the required Java manually and set it in Settings → Runtime.");
                    return;
                }
            }
        }

        // ── Real JVM command from version.json ──────────────────────────
        let vars = crate::assets::LaunchVars {
            username: req.username.clone(),
            uuid: req.uuid.clone(),
            token: if req.token.is_empty() {
                "0".into()
            } else {
                req.token.clone()
            },
            res_w: req.res_w,
            res_h: req.res_h,
        };
        let workdir = dir.to_string_lossy().to_string();
        let (mut version_jvm, main_class, game_args) =
            match crate::assets::build_launch_args(&files, &workdir, &vars) {
                Ok(t) => t,
                Err(e) => {
                    emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] cannot build launch command: {e}"));
                    return;
                }
            };

        let ram = req.ram_mb.clamp(512, 32768);
        let mut full_args: Vec<String> = vec![
            format!("-Xms{}M", 512.min(ram)),
            format!("-Xmx{ram}M"),
        ];
        full_args.extend(split_args(&req.jvm_args));
        full_args.append(&mut version_jvm);
        full_args.push(main_class);
        full_args.extend(game_args);

        // Direct server join (Home → Servers → Join).
        let target = req.server.trim();
        if !target.is_empty() {
            let port = if req.port == 0 { 25565 } else { req.port };
            full_args.push("--server".into());
            full_args.push(target.to_string());
            full_args.push("--port".into());
            full_args.push(port.to_string());
            emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] auto-joining {target}:{port}…"));
        }

        emit_inst(&handle, &req.instance_id, "sys", "[tyx] starting real client…");
        let mut spawn_cmd = std::process::Command::new(&preview.java);
        spawn_cmd
            .args(&full_args)
            .current_dir(&workdir)
            .stdout(std::process::Stdio::piped())
            .stderr(std::process::Stdio::piped());
        #[cfg(windows)]
        {
            use std::os::windows::process::CommandExt;
            spawn_cmd.creation_flags(0x08000000);
        }
        let mut child = match spawn_cmd.spawn() {
            Ok(c) => c,
            Err(e) => {
                emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] spawn failed: {e}"));
                return;
            }
        };
        emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] pid {} instance {}", child.id(), req.instance_id));
        track_running(&handle, child.id(), &req.instance_id);

        let emit_inst_id = req.instance_id.clone();
        let emit_inst_id_err = emit_inst_id.clone();
        if let Some(out) = child.stdout.take() {
            let h = handle.clone();
            let d = dir.clone();
            std::thread::spawn(move || {
                for line in BufReader::new(out).lines().map_while(Result::ok) {
                    emit_inst(&h, &emit_inst_id, "stdout", line.clone());
                    append_log(&d, &line);
                }
            });
        }
        if let Some(err) = child.stderr.take() {
            let h = handle.clone();
            let d = dir.clone();
            let emit_inst_id = emit_inst_id_err;
            std::thread::spawn(move || {
                for line in BufReader::new(err).lines().map_while(Result::ok) {
                    emit_inst(&h, &emit_inst_id, "stderr", line.clone());
                    append_log(&d, &line);
                }
            });
        }
        let pid = child.id();
        match child.wait() {
            Ok(status) => emit_inst(&handle, &req.instance_id, "sys", format!("[tyx] process exited: {status}")),
            Err(e) => emit_inst(&handle, &req.instance_id, "stderr", format!("[tyx] wait failed: {e}")),
        }
        clear_running(&handle, pid);
    });

    Ok(0)
}
