//! .mrpack installer (Modrinth App parity).
//!
//! Flow: download pack → parse `modrinth.index.json` → create a FRESH
//! isolated instance (name / MC version / loader all come from the pack) →
//! download every client-side file into place (sha1-verified, resumable via
//! size-match skip) → apply `overrides/` + `client-overrides/`.
//! Progress reuses the `tyx://assets` events, so the console + Settings
//! download bars work with zero frontend changes.

use crate::assets::{fetch_file, http_client, DlCtx};
use serde::Deserialize;
use std::collections::HashMap;
use std::io::{Cursor, Read, Write};
use tauri::{AppHandle, Emitter};

#[derive(Deserialize, Default)]
struct PackEnv {
    #[serde(default)]
    client: Option<String>, // required | optional | unsupported
}

#[derive(Deserialize)]
struct PackFile {
    path: String,
    #[serde(default)]
    hashes: HashMap<String, String>,
    #[serde(default)]
    env: Option<PackEnv>,
    #[serde(default)]
    downloads: Vec<String>,
    #[serde(default, rename = "fileSize")]
    file_size: u64,
}

#[derive(Deserialize)]
struct PackIndex {
    #[serde(default)]
    name: String,
    #[serde(default)]
    game: String,
    #[serde(default, rename = "versionId")]
    version_id: String,
    #[serde(default)]
    files: Vec<PackFile>,
    #[serde(default)]
    dependencies: HashMap<String, String>,
}

/// Map mrpack dependency keys → (display loader, loader version).
fn loader_from_deps(deps: &HashMap<String, String>) -> (String, String) {
    for (key, display) in [
        ("fabric-loader", "Fabric"),
        ("quilt-loader", "Quilt"),
        ("neoforge", "NeoForge"),
        ("forge", "Forge"),
        ("minecraftforge", "Forge"),
    ] {
        if let Some(v) = deps.get(key) {
            return (display.to_string(), v.clone());
        }
    }
    ("Vanilla".to_string(), String::new())
}

/// Instance-relative, traversal-free path or None.
fn clean_rel(raw: &str) -> Option<String> {
    let t = raw.replace('\\', "/");
    let t = t.trim().trim_start_matches('/').to_string();
    if t.is_empty() {
        return None;
    }
    let mut parts: Vec<&str> = Vec::new();
    for p in t.split('/') {
        if p.is_empty() || p == "." {
            continue;
        }
        if p == ".." {
            return None;
        }
        parts.push(p);
    }
    if parts.is_empty() {
        return None;
    }
    Some(parts.join("/"))
}

#[tauri::command]
pub async fn modpack_install(
    app: AppHandle,
    url: String,
    filename: String,
    path: Option<String>,
) -> Result<crate::instances::InstanceMeta, String> {
    // Local file import (drag-drop / file picker): read bytes off disk.
    if let Some(p) = path.as_deref().map(str::trim).filter(|s| !s.is_empty()) {
        if !p.to_lowercase().ends_with(".mrpack") {
            return Err("not a .mrpack file".into());
        }
        let bytes = std::fs::read(p).map_err(|e| format!("can't read pack: {e}"))?;
        if bytes.is_empty() {
            return Err("pack file is empty".into());
        }
        let label = std::path::Path::new(p)
            .file_name()
            .map(|n| n.to_string_lossy().to_string())
            .unwrap_or_else(|| "pack.mrpack".to_string());
        let _ = app.emit(
            "tyx://game-log",
            serde_json::json!({"stream": "sys", "line": format!("[tyx] importing {label}…")}),
        );
        return install_from_bytes(&app, &bytes, &label).await;
    }
    // Classic URL install (Browse / detail).
    if !url.starts_with("https://") {
        return Err("refusing non-https modpack URL".into());
    }
    let _ = filename; // display only; the pack's own index names the instance

    let client = http_client()?;
    let _ = app.emit(
        "tyx://game-log",
        serde_json::json!({"stream": "sys", "line": "[tyx] downloading modpack…"}),
    );
    let res = client.get(&url).send().await.map_err(|e| format!("pack download failed: {e}"))?;
    if !res.status().is_success() {
        return Err(format!("pack download: HTTP {}", res.status()));
    }
    let bytes = res.bytes().await.map_err(|e| format!("pack download interrupted: {e}"))?;
    if bytes.len() > 2 * 1024 * 1024 * 1024 {
        return Err("modpack too large (>2 GB)".into());
    }
    install_from_bytes(&app, &bytes, &filename).await
}

/// Shared core: validated pack bytes → fresh instance. Used by both the
/// URL downloader above and local-file imports.
async fn install_from_bytes(
    app: &AppHandle,
    bytes: &[u8],
    display: &str,
) -> Result<crate::instances::InstanceMeta, String> {
    let _ = display;
    // ── index ──────────────────────────────────────────────────────────
    let mut zip = zip::ZipArchive::new(Cursor::new(bytes)).map_err(|e| format!("bad .mrpack: {e}"))?;
    let index: PackIndex = {
        let mut entry = zip
            .by_name("modrinth.index.json")
            .map_err(|_| "modrinth.index.json missing — not a modpack".to_string())?;
        let mut s = String::new();
        entry.read_to_string(&mut s).map_err(|e| e.to_string())?;
        serde_json::from_str(&s).map_err(|e| format!("bad pack index: {e}"))?
    };
    if index.game != "minecraft" {
        return Err(format!("unsupported pack game: {}", index.game));
    }
    let mc = index
        .dependencies
        .get("minecraft")
        .cloned()
        .filter(|v| !v.trim().is_empty())
        .ok_or_else(|| "pack has no minecraft dependency".to_string())?;
    let (loader_display, loader_version) = loader_from_deps(&index.dependencies);
    let name = if index.name.trim().is_empty() {
        format!("Modpack {}", index.version_id)
    } else {
        index.name.trim().to_string()
    };
    if index.files.len() > 5000 {
        return Err("pack lists too many files".into());
    }

    // ── fresh isolated instance ────────────────────────────────────────
    let meta = crate::instances::instance_create(        app.clone(),
        name.clone(),
        mc.clone(),
        loader_display.clone(),
        "modpack".to_string(),
    )?;
    let dir = crate::util::instance_dir(&app, &meta.id)?;
    if !loader_version.is_empty() {
        let _ = crate::minecraft::loader_record(
            app.clone(),
            meta.id.clone(),
            loader_display.clone(),
            mc.clone(),
            loader_version.clone(),
        );
    }
    let _ = app.emit(
        "tyx://game-log",
        serde_json::json!({"stream": "sys", "line": format!("[tyx] created instance “{name}” ({mc} · {loader_display}) — fetching {} files…", index.files.len())}),
    );

    // ── files (skip server-only) ───────────────────────────────────────
    let client = http_client()?;
    let wanted: Vec<&PackFile> = index
        .files
        .iter()
        .filter(|f| f.env.as_ref().and_then(|e| e.client.as_deref()) != Some("unsupported"))
        .collect();
    let total: u64 = wanted.iter().map(|f| f.file_size).sum();
    let mut ctx = DlCtx {
        app: &app,
        stage: "modpack".to_string(),
        done_files: 0,
        total_files: wanted.len() as u64,
        done_bytes: 0,
        total_bytes: total,
    };
    for f in &wanted {
        let rel = match clean_rel(&f.path) {
            Some(r) => r,
            None => continue, // unsafe path — skip, never abort the pack
        };
        let dest = dir.join(&rel);
        let url = f.downloads.first().map(|s| s.as_str()).unwrap_or("");
        let sha1 = f.hashes.get("sha1").map(|s| s.as_str());
        fetch_file(&client, &mut ctx, &dest, url, f.file_size, sha1, &format!("pack {rel}")).await?;
    }

    // ── overrides (client-side only) ───────────────────────────────────
    let mut zip2 = zip::ZipArchive::new(Cursor::new(bytes)).map_err(|e| format!("bad .mrpack: {e}"))?;
    let mut applied = 0u32;
    for i in 0..zip2.len() {
        let mut entry = zip2.by_index(i).map_err(|e| e.to_string())?;
        let full = entry.name().replace('\\', "/");
        let rel = if let Some(r) = full.strip_prefix("overrides/") {
            r
        } else if let Some(r) = full.strip_prefix("client-overrides/") {
            r
        } else {
            continue; // server-overrides/, index, extras — not for the client
        };
        let rel = match clean_rel(rel) {
            Some(r) => r,
            None => continue,
        };
        if entry.is_dir() {
            std::fs::create_dir_all(dir.join(&rel)).map_err(|e| e.to_string())?;
            continue;
        }
        let dest = dir.join(&rel);
        if let Some(par) = dest.parent() {
            std::fs::create_dir_all(par).map_err(|e| e.to_string())?;
        }
        let mut buf = Vec::new();
        entry.read_to_end(&mut buf).map_err(|e| e.to_string())?;
        std::fs::write(&dest, buf).map_err(|e| e.to_string())?;
        applied += 1;
    }

    let _ = app.emit(
        "tyx://game-log",
        serde_json::json!({"stream": "sys", "line": format!("[tyx] modpack ready: “{name}” ({} files, {} overrides)", wanted.len(), applied)}),
    );
    // Final "done" tick so progress cards clear.
    let _ = app.emit(
        "tyx://assets",
        crate::assets::AssetsProgress {
            stage: "done".to_string(),
            done_files: wanted.len() as u64,
            total_files: wanted.len() as u64,
            done_bytes: total,
            total_bytes: total,
            current: String::new(),
            speed_bps: 0,
            eta_secs: 0,
        },
    );
    Ok(meta)
}

// ── export ────────────────────────────────────────────────────────────────

/// Collect (zip_name, disk_path) pairs under `base`, skipping nothing.
/// Zip names always use forward slashes.
fn collect_tree(
    base: &std::path::Path,
    prefix: &str,
    out: &mut Vec<(String, std::path::PathBuf)>,
) -> Result<(), String> {
    let mut stack = vec![base.to_path_buf()];
    while let Some(d) = stack.pop() {
        let rd = std::fs::read_dir(&d).map_err(|e| e.to_string())?;
        for e in rd.flatten() {
            let p = e.path();
            if p.is_dir() {
                stack.push(p);
                continue;
            }
            let rel = p.strip_prefix(base).map_err(|e| e.to_string())?;
            let name = format!("{prefix}/{}", rel.to_string_lossy().replace('\\', "/"));
            out.push((name, p));
            if out.len() > 20000 {
                return Err("too many files to export (cap 20k)".into());
            }
        }
    }
    Ok(())
}

/// Export an instance as a distributable `.mrpack` (Modrinth format).
///
/// Everything rides in `overrides/` as exact copies (no URLs needed, so it
/// always re-imports cleanly): mods, resourcepacks, shaderpacks, configs,
/// options.txt. Worlds, caches, logs, icons and servers.dat stay behind.
/// Index carries the instance name + MC/loader dependencies.
#[tauri::command]
pub async fn modpack_export(app: AppHandle, id: String, dest: String) -> Result<String, String> {
    if !dest.to_lowercase().ends_with(".mrpack") {
        return Err("destination must end with .mrpack".into());
    }
    let dir = crate::util::instance_dir(&app, &id)?;
    let raw = std::fs::read_to_string(dir.join("instance.json")).map_err(|e| e.to_string())?;
    let meta: crate::instances::InstanceMeta =
        serde_json::from_str(&raw).map_err(|e| e.to_string())?;

    // Loader version: instance record first, loader.json as fallback.
    let mut loader_version = meta.loader_version.clone();
    if loader_version.is_empty() {
        if let Ok(lr) = std::fs::read_to_string(dir.join("loader.json")) {
            if let Ok(rec) = serde_json::from_str::<crate::minecraft::LoaderRecord>(&lr) {
                loader_version = rec.loader_version;
            }
        }
    }
    let loader_key = match meta.loader.to_lowercase().as_str() {
        s if s.contains("fabric") => Some("fabric-loader"),
        s if s.contains("quilt") => Some("quilt-loader"),
        s if s.contains("neoforge") => Some("neoforge"),
        s if s.contains("forge") => Some("forge"),
        _ => None,
    };
    let mut deps = serde_json::Map::new();
    deps.insert("minecraft".to_string(), serde_json::Value::String(meta.version.clone()));
    if let (Some(k), v) = (loader_key, loader_version.trim()) {
        if !v.is_empty() {
            deps.insert(k.to_string(), serde_json::Value::String(v.to_string()));
        }
    }

    // Gather payload.
    let mut entries: Vec<(String, std::path::PathBuf)> = Vec::new();
    for sub in ["mods", "resourcepacks", "shaderpacks", "config"] {
        let base = dir.join(sub);
        if base.is_dir() {
            collect_tree(&base, &format!("overrides/{sub}"), &mut entries)?;
        }
    }
    let options = dir.join("options.txt");
    if options.is_file() {
        entries.push(("overrides/options.txt".to_string(), options));
    }
    if entries.is_empty() {
        return Err("nothing to export — install some mods or configs first".into());
    }

    let index = serde_json::json!({
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": format!("export-{}", crate::util::now_millis()),
        "name": meta.name,
        "summary": "Exported from Tyx Launcher",
        "files": [],
        "dependencies": deps,
    });

    let dest_path = std::path::Path::new(&dest);
    if let Some(par) = dest_path.parent() {
        if !par.as_os_str().is_empty() {
            std::fs::create_dir_all(par).map_err(|e| e.to_string())?;
        }
    }
    let f = std::fs::File::create(dest_path).map_err(|e| e.to_string())?;
    let mut zw = zip::ZipWriter::new(f);
    let stored = [".jar", ".zip"];
    let opt_for = |name: &str| {
        let m = if stored.iter().any(|e| name.to_lowercase().ends_with(e)) {
            zip::CompressionMethod::Stored
        } else {
            zip::CompressionMethod::Deflated
        };
        zip::write::SimpleFileOptions::default().compression_method(m)
    };
    zw.start_file(
        "modrinth.index.json",
        zip::write::SimpleFileOptions::default().compression_method(zip::CompressionMethod::Deflated),
    )
    .map_err(|e| e.to_string())?;
    zw.write_all(serde_json::to_string_pretty(&index).map_err(|e| e.to_string())?.as_bytes())
        .map_err(|e| e.to_string())?;
    for (name, path) in &entries {
        zw.start_file(name, opt_for(name)).map_err(|e| e.to_string())?;
        let mut fh = std::fs::File::open(path).map_err(|e| e.to_string())?;
        std::io::copy(&mut fh, &mut zw).map_err(|e| e.to_string())?;
    }
    zw.finish().map_err(|e| e.to_string())?;
    Ok(dest)
}
