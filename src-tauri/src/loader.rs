//! Real mod-loader install (Lunar parity: Play just works).
//!
//! Vanilla `ensure_assets` downloads Mojang's version JSON whose `mainClass`
//! boots `net.minecraft.client.main.Main`. For Fabric/Quilt instances we
//! fetch the loader profile from meta.fabricmc.net / meta.quiltmc.org and
//! merge it into that JSON (loader libraries + KnotClient mainClass +
//! extra args). Everything downstream — library download, natives,
//! arg substitution, JVM spawn — then flows untouched, because it all
//! reads the same merged file.
//!
//! Idempotent: `loader.json` records what was merged; matching requests
//! skip the network entirely. Changing loaders re-fetches vanilla first
//! so families never pollute each other.

use crate::assets::{AssetsProgress, VersionArgs, VersionJson};
use serde::Deserialize;
use tauri::{AppHandle, Emitter};

const FABRIC_META: &str = "https://meta.fabricmc.net/v2/versions/loader";
const QUILT_META: &str = "https://meta.quiltmc.org/v3/versions/loader";
const MANIFEST_URL: &str = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
enum Family {
    Fabric,
    Quilt,
}

impl Family {
    fn main_marker(&self) -> &'static str {
        match self {
            Family::Fabric => "fabricmc.loader",
            Family::Quilt => "quiltmc.loader",
        }
    }
}

#[derive(Deserialize)]
struct FabricEntry {
    loader: FabricLoaderInfo,
}

#[derive(Deserialize)]
struct FabricLoaderInfo {
    version: String,
    #[serde(default)]
    stable: bool,
}

#[derive(Deserialize)]
struct QuiltEntry {
    loader: QuiltLoaderInfo,
}

#[derive(Deserialize)]
struct QuiltLoaderInfo {
    version: String,
}

/// Loader profile JSON — same vanilla schema Mojang uses, so we reuse
/// `VersionArgs` / `Library` straight from `assets`.
#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct LoaderProfile {
    #[serde(default)]
    main_class: String,
    #[serde(default)]
    minecraft_arguments: Option<String>,
    #[serde(default)]
    arguments: Option<VersionArgs>,
    #[serde(default)]
    libraries: Vec<crate::assets::Library>,
}

#[derive(Deserialize, Clone)]
struct LoaderRecord {
    #[serde(default)]
    loader: String,
    #[serde(default)]
    mc_version: String,
    #[serde(default)]
    loader_version: String,
}

/// The loader's own jar must be present WITH a maven base — caches merged
/// before URL capture carry classpath-only entries that boot to
/// KnotClient ClassNotFound. Missing base forces one clean re-merge.
fn loader_lib_ready(version: &VersionJson, family: Family) -> bool {
    version.libraries.iter().any(|l| {
        let n = &l.name;
        let is_loader = match family {
            Family::Fabric => n.starts_with("net.fabricmc:fabric-loader:"),
            Family::Quilt => n.starts_with("org.quiltmc:quilt-loader:"),
        };
        is_loader && !l.url.is_empty()
    })
}

fn emit(app: &AppHandle, current: &str) {
    let _ = app.emit(
        "tyx://assets",
        AssetsProgress {
            stage: "loader".into(),
            done_files: 0,
            total_files: 1,
            done_bytes: 0,
            total_bytes: 1,
            current: current.into(),
            speed_bps: 0,
            eta_secs: u64::MAX,
        },
    );
}

/// Console line (same stream the launch thread uses) so the user can SEE
/// which profile booted — silent loader paths are undiagnosable otherwise.
fn glog(app: &AppHandle, stream: &str, line: &str) {
    #[derive(serde::Serialize, Clone)]
    struct GameLog {
        stream: String,
        line: String,
    }
    let _ = app.emit(
        "tyx://game-log",
        GameLog {
            stream: stream.into(),
            line: line.into(),
        },
    );
}

fn want_loader(inst_dir: &std::path::Path) -> Option<(Family, String)> {
    let raw = std::fs::read_to_string(inst_dir.join("instance.json")).ok()?;
    let v: serde_json::Value = serde_json::from_str(&raw).ok()?;
    let loader = v.get("loader")?.as_str()?.trim().to_lowercase();
    let family = if loader.contains("fabric") {
        Family::Fabric
    } else if loader.contains("quilt") {
        Family::Quilt
    } else {
        return None; // Vanilla / Forge / NeoForge — not handled here yet.
    };
    let lv = v
        .get("loaderVersion")
        .and_then(|s| s.as_str())
        .unwrap_or("")
        .trim()
        .to_string();
    Some((family, lv))
}

fn merged_record(inst_dir: &std::path::Path) -> Option<LoaderRecord> {
    let raw = std::fs::read_to_string(inst_dir.join("loader.json")).ok()?;
    serde_json::from_str(&raw).ok()
}

async fn resolve_loader_version(
    client: &reqwest::Client,
    family: Family,
    mc: &str,
) -> Result<String, String> {
    let url = format!(
        "{}/{mc}",
        match family {
            Family::Fabric => FABRIC_META,
            Family::Quilt => QUILT_META,
        }
    );
    match family {
        Family::Fabric => {
            let list: Vec<FabricEntry> = client
                .get(&url)
                .send()
                .await
                .map_err(|e| format!("loader index unreachable: {e}"))?
                .json()
                .await
                .map_err(|e| format!("bad loader index: {e}"))?;
            list.iter()
                .find(|e| e.loader.stable)
                .or(list.first())
                .map(|e| e.loader.version.clone())
                .ok_or_else(|| format!("no {mc} loader published"))
        }
        Family::Quilt => {
            let list: Vec<QuiltEntry> = client
                .get(&url)
                .send()
                .await
                .map_err(|e| format!("loader index unreachable: {e}"))?
                .json()
                .await
                .map_err(|e| format!("bad loader index: {e}"))?;
            list.first()
                .map(|e| e.loader.version.clone())
                .ok_or_else(|| format!("no {mc} loader published"))
        }
    }
}

async fn fetch_profile(
    client: &reqwest::Client,
    family: Family,
    mc: &str,
    lv: &str,
) -> Result<LoaderProfile, String> {
    let base = match family {
        Family::Fabric => FABRIC_META,
        Family::Quilt => QUILT_META,
    };
    let url = format!("{base}/{mc}/{lv}/profile/json");
    let p: LoaderProfile = client
        .get(&url)
        .send()
        .await
        .map_err(|e| format!("loader profile unreachable: {e}"))?
        .json()
        .await
        .map_err(|e| format!("bad loader profile: {e}"))?;
    if p.main_class.is_empty() || p.libraries.is_empty() {
        return Err("loader profile is empty".into());
    }
    Ok(p)
}

/// Fresh vanilla Mojang JSON (used when the cache holds another family).
async fn fetch_vanilla(
    client: &reqwest::Client,
    mc: &str,
) -> Result<VersionJson, String> {
    #[derive(Deserialize)]
    struct Manifest {
        versions: Vec<ManifestEntry>,
    }
    #[derive(Deserialize)]
    struct ManifestEntry {
        id: String,
        url: String,
    }
    let m: Manifest = client
        .get(MANIFEST_URL)
        .send()
        .await
        .map_err(|e| format!("cannot reach Mojang ({e})"))?
        .json()
        .await
        .map_err(|e| format!("bad Mojang response: {e}"))?;
    let entry = m
        .versions
        .iter()
        .find(|v| v.id == mc)
        .ok_or_else(|| format!("Minecraft {mc} not found on Mojang"))?;
    client
        .get(&entry.url)
        .send()
        .await
        .map_err(|e| e.to_string())?
        .json()
        .await
        .map_err(|e| e.to_string())
}

fn arg_seen(list: &[crate::assets::ArgValue], v: &crate::assets::ArgValue) -> bool {
    let key = serde_json::to_value(v).unwrap_or(serde_json::Value::Null);
    list.iter().any(|e| {
        serde_json::to_value(e).unwrap_or(serde_json::Value::Null) == key
    })
}

/// Merge a loader profile into the Mojang version JSON (idempotent).
fn merge(version: &mut VersionJson, profile: LoaderProfile) {
    for lib in profile.libraries {
        if !version.libraries.iter().any(|e| e.name == lib.name) {
            version.libraries.push(lib);
        }
    }
    if !profile.main_class.is_empty() {
        version.main_class = profile.main_class;
    }
    if let Some(pa) = profile.arguments {
        let mut va = version.arguments.take().unwrap_or_default();
        for a in pa.jvm {
            if !arg_seen(&va.jvm, &a) {
                va.jvm.push(a);
            }
        }
        for a in pa.game {
            if !arg_seen(&va.game, &a) {
                va.game.push(a);
            }
        }
        version.arguments = Some(va);
    }
    if let Some(legacy) = profile.minecraft_arguments {
        let cur = version.minecraft_arguments.take().unwrap_or_default();
        version.minecraft_arguments = Some(if cur.is_empty() {
            legacy
        } else {
            format!("{cur} {legacy}")
        });
    }
}

/// Called from `ensure_assets` right after the Mojang JSON is loaded.
/// Vanilla instances return immediately (no network, no changes).
pub async fn ensure_loader_merged(
    app: &AppHandle,
    inst_dir: &std::path::Path,
    instance_id: &str,
    mc_version: &str,
    version_json_path: &std::path::Path,
    version: &mut VersionJson,
) -> Result<(), String> {
    let Some((family, lv_raw)) = want_loader(inst_dir) else {
        glog(app, "sys", "[tyx] vanilla profile — mods/ stays inactive (create a Fabric instance for mods)");
        return Ok(());
    };
    let client = crate::assets::http_client()?;

    // Resolve "recommended"/empty → latest stable (needs one index fetch).
    // When offline, a previously merged cache still boots with zero network.
    let resolved_lv = if lv_raw.is_empty() || lv_raw == "recommended" {
        match tokio::time::timeout(
            std::time::Duration::from_secs(30),
            resolve_loader_version(&client, family, mc_version),
        )
        .await
        {
            Ok(Ok(lv)) => lv,
            Ok(Err(e)) => {
                if version.main_class.contains(family.main_marker()) {
                    return Ok(()); // offline, merged cache boots fine
                }
                return Err(e);
            }
            Err(_) => {
                if version.main_class.contains(family.main_marker()) {
                    return Ok(());
                }
                return Err("loader index timed out — check your connection".into());
            }
        }
    } else {
        lv_raw
    };

    // Idempotent skip: same family + version already merged on disk, with
    // maven bases present (else one clean re-merge repairs old caches).
    if version.main_class.contains(family.main_marker()) {
        if let Some(rec) = merged_record(inst_dir) {
            if rec.loader_version == resolved_lv && rec.mc_version == mc_version
                && loader_lib_ready(version, family)
            {
                glog(app, "sys", &format!("[tyx] {} loader {} ready", rec.loader, rec.loader_version));
                return Ok(());
            }
        }
    }

    emit(app, "resolving mod loader");
    // Guarantee a vanilla base: another family's merge would pollute this one.
    if version.main_class.contains("fabricmc.loader") || version.main_class.contains("quiltmc.loader") {
        emit(app, "refreshing version profile");
        *version = fetch_vanilla(&client, mc_version).await?;
    }
    emit(app, &format!("installing loader {resolved_lv}"));
    let profile = tokio::time::timeout(
        std::time::Duration::from_secs(60),
        fetch_profile(&client, family, mc_version, &resolved_lv),
    )
    .await
    .map_err(|_| "loader profile timed out — check your connection".to_string())??;

    merge(version, profile);
    std::fs::write(
        version_json_path,
        serde_json::to_string_pretty(&version).map_err(|e| e.to_string())?,
    )
    .map_err(|e| format!("can't write {}: {e}", version_json_path.display()))?;
    let loader_name = match family {
        Family::Fabric => "Fabric",
        Family::Quilt => "Quilt",
    };
    std::fs::write(
        inst_dir.join("loader.json"),
        serde_json::to_string_pretty(&serde_json::json!({
            "loader": loader_name,
            "mcVersion": mc_version,
            "loaderVersion": resolved_lv,
            "installedAt": crate::util::now_millis(),
        }))
        .map_err(|e| e.to_string())?,
    )
    .map_err(|e| e.to_string())?;
    let _ = instance_id;
    glog(app, "sys", &format!("[tyx] {loader_name} loader {resolved_lv} installed — mods/ active"));
    Ok(())
}
