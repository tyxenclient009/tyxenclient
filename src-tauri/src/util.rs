//! Shared path helpers — all launcher data lives under the Tauri app-data dir:
//!   <app_data>/tyx/instances/<id>/…
//! Browser preview (no Tauri) never reaches this code; the frontend falls
//! back to localStorage instead (see src/lib/backend.ts).

use std::path::PathBuf;
use tauri::{AppHandle, Manager};

/// `<app_data>/tyx` — created on demand.
pub fn data_root(app: &AppHandle) -> Result<PathBuf, String> {
    let base = app
        .path()
        .app_data_dir()
        .map_err(|e| format!("app_data_dir unavailable: {e}"))?;
    Ok(base.join("tyx"))
}

/// `<app_data>/tyx/instances`
pub fn instances_root(app: &AppHandle) -> Result<PathBuf, String> {
    let dir = data_root(app)?.join("instances");
    std::fs::create_dir_all(&dir).map_err(|e| format!("cannot create {dir:?}: {e}"))?;
    Ok(dir)
}

/// `<app_data>/tyx/instances/<id>` — sanitises `id` to stop path traversal.
pub fn instance_dir(app: &AppHandle, id: &str) -> Result<PathBuf, String> {
    if id.is_empty()
        || id.contains('/')
        || id.contains('\\')
        || id.contains("..")
    {
        return Err("invalid instance id".into());
    }
    Ok(instances_root(app)?.join(id))
}

/// Millis since epoch (for ids / timestamps) — std only, no chrono needed.
pub fn now_millis() -> u64 {
    use std::time::{SystemTime, UNIX_EPOCH};
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_millis() as u64)
        .unwrap_or(0)
}

/// Filesystem-safe slug from a display name.
pub fn slugify(name: &str) -> String {
    let mut s: String = name
        .to_lowercase()
        .chars()
        .map(|c| if c.is_ascii_alphanumeric() { c } else { '-' })
        .collect();
    while s.contains("--") {
        s = s.replace("--", "-");
    }
    let s = s.trim_matches('-').to_string();
    if s.is_empty() {
        "instance".to_string()
    } else {
        s.chars().take(40).collect()
    }
}
