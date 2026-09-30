//! Minecraft metadata helpers.
//!
//! Network (Mojang version manifest, Fabric/Quilt meta, Modrinth) is fetched
//! from the *frontend* via fetch — no extra Rust HTTP crates needed.
//! This module provides:
//!   - a curated offline fallback version list (used when Mojang is unreachable)
//!   - a loader install *record* (real jar installation lands with the
//!     Phase-2 downloader; recording first keeps create-flow unblocked)

use crate::util::instance_dir;
use serde::{Deserialize, Serialize};
use tauri::AppHandle;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct McVersion {
    pub id: String,
    pub kind: String, // "release" | "snapshot"
}

/// Curated fallback — newest-first. The UI prefers the live Mojang manifest
/// and only uses this offline.
#[tauri::command]
pub fn mc_versions_fallback() -> Vec<McVersion> {
    const RELEASES: &[&str] = &[
        "1.21.1", "1.21", "1.20.6", "1.20.4", "1.20.1", "1.19.4", "1.19.2", "1.18.2",
        "1.17.1", "1.16.5", "1.12.2", "1.8.9",
    ];
    RELEASES
        .iter()
        .map(|v| McVersion {
            id: v.to_string(),
            kind: "release".into(),
        })
        .collect()
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LoaderRecord {
    pub loader: String,
    pub mc_version: String,
    pub loader_version: String,
    pub installed_at: u64,
}

/// Record the chosen loader inside the instance (loader.json).
/// Full installer (download + patch) reuses this record in the next iteration.
// NOTE: snake_case IPC keys (Tauri camelCases them by default).
#[tauri::command(rename_all = "snake_case")]
pub fn loader_record(
    app: AppHandle,
    id: String,
    loader: String,
    mc_version: String,
    loader_version: String,
) -> Result<LoaderRecord, String> {
    let dir = instance_dir(&app, &id)?;
    let rec = LoaderRecord {
        loader,
        mc_version,
        loader_version,
        installed_at: crate::util::now_millis(),
    };
    std::fs::write(
        dir.join("loader.json"),
        serde_json::to_string_pretty(&rec).map_err(|e| e.to_string())?,
    )
    .map_err(|e| e.to_string())?;
    Ok(rec)
}
