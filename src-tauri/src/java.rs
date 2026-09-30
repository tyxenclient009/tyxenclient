//! Java runtime detection + automatic provisioning (std + reqwest + zip).
//!
//! - `java_detect` — probes JAVA_HOME, PATH, well-known locations, Mojang
//!   runtimes AND the launcher-managed `runtimes/` dir.
//! - `java_ensure` — returns a runtime for the requested major version,
//!   downloading a Temurin JRE automatically when nothing matches.
//!   Progress streams as `tyx://java-dl` events: { major, downloaded, total }.
//! - `java_major_for_mc` — maps a Minecraft version to its required Java.

use serde::Serialize;
use tauri::{AppHandle, Emitter, Manager};

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct JavaInstall {
    pub path: String,
    pub version: String,
    pub source: String, // "JAVA_HOME" | "PATH" | "bundled" | "program-files" | "auto"
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct JavaDownloadProgress {
    pub major: u32,
    pub downloaded: u64,
    pub total: u64,
}

/// Parse `openjdk version "17.0.11" ...` / `java version "1.8.0_411"`.
pub fn java_major_of_version_line(line: &str) -> Option<u32> {
    let q1 = line.find('"')?;
    let rest = &line[q1 + 1..];
    let q2 = rest.find('"')?;
    let v = &rest[..q2];
    if let Some(legacy) = v.strip_prefix("1.") {
        return legacy.split('.').next()?.parse().ok();
    }
    v.split('.').next()?.parse().ok()
}

/// Minecraft → required Java major.
/// 1.20.5+/1.21 → 21, 1.17–1.20.4 → 17, 1.x older → 8.
/// New Mojang year-based scheme (26.x, …) → 21 (modern clients).
/// Unknown strings → 21: a wrong 8 breaks every modern client at boot
/// (`Unrecognized option`), while 21 only affects pre-1.17 instances.
pub fn java_major_for_mc(version: &str) -> u32 {
    let parts: Vec<u32> = version
        .split('.')
        .filter_map(|p| p.parse().ok())
        .collect();
    match parts.as_slice() {
        [major, ..] if *major >= 21 => 21,
        [1, minor, ..] if *minor > 20 => 21,
        [1, 20, ..] => {
            let p = match parts.as_slice() {
                [_, _, p, ..] => *p,
                _ => 0,
            };
            if p >= 5 { 21 } else { 17 }
        }
        [1, minor, ..] if *minor >= 17 => 17,
        [1, ..] => 8,
        _ => 21,
    }
}

/// Major version of a java binary (`-version` probe) or None when the
/// binary is missing/broken.
pub fn java_major_of_path(java: &str) -> Option<u32> {
    let v = probe(std::path::Path::new(java))?;
    java_major_of_version_line(&v)
}

/// Child processes must never flash a console window (CREATE_NO_WINDOW).
fn silent_cmd<S: AsRef<std::ffi::OsStr>>(program: S) -> std::process::Command {
    let mut cmd = std::process::Command::new(program);
    #[cfg(windows)]
    {
        use std::os::windows::process::CommandExt;
        cmd.creation_flags(0x08000000);
    }
    cmd
}

fn probe(path: &std::path::Path) -> Option<String> {
    if !path.exists() {
        return None;
    }
    let out = silent_cmd(path).arg("-version").output().ok()?;
    let text = format!(
        "{}{}",
        String::from_utf8_lossy(&out.stdout),
        String::from_utf8_lossy(&out.stderr)
    );
    let first = text.lines().next().unwrap_or("").trim().to_string();
    if first.is_empty() {
        Some("unknown version".into())
    } else {
        Some(first.chars().take(120).collect())
    }
}

/// Launcher-managed runtimes: `<data>/tyx/runtimes/*/bin/java[.exe]`.
/// Recursive per entry — Temurin zips wrap everything in one top-level
/// folder (`jdk-21…-jre/`), so a one-level check misses installs that are
/// already on disk and every launch would re-download ~200 MB.
fn scan_managed_runtimes(app: &AppHandle, found: &mut Vec<JavaInstall>, seen: &mut Vec<String>) {
    let root = match crate::util::data_root(app) {
        Ok(r) => r.join("runtimes"),
        Err(_) => return,
    };
    let entries = match std::fs::read_dir(&root) {
        Ok(e) => e,
        Err(_) => return,
    };
    for e in entries.flatten() {
        let dir = e.path();
        if !dir.is_dir() {
            continue; // skip leftover zips / stray files
        }
        if let Some(bin) = find_java_bin(&dir) {
            if let Some(v) = probe(&bin) {
                let p = bin.to_string_lossy().to_string();
                if !seen.contains(&p) {
                    seen.push(p.clone());
                    found.push(JavaInstall {
                        path: p,
                        version: v,
                        source: "auto".into(),
                    });
                }
            }
        }
    }
}

#[tauri::command]
pub async fn java_detect(app: AppHandle) -> Vec<JavaInstall> {
    let mut found: Vec<JavaInstall> = Vec::new();
    let mut seen: Vec<String> = Vec::new();
    let mut push = |path: String, version: String, source: &str| {
        if !seen.contains(&path) {
            seen.push(path.clone());
            found.push(JavaInstall {
                path,
                version,
                source: source.into(),
            });
        }
    };

    if let Ok(home) = std::env::var("JAVA_HOME") {
        #[cfg(windows)]
        let bin = std::path::Path::new(&home).join("bin").join("java.exe");
        #[cfg(not(windows))]
        let bin = std::path::Path::new(&home).join("bin").join("java");
        if let Some(v) = probe(&bin) {
            push(bin.to_string_lossy().to_string(), v, "JAVA_HOME");
        }
    }

    #[cfg(windows)]
    let path_probe: Option<String> = silent_cmd("where")
        .arg("java")
        .output()
        .ok()
        .and_then(|o| {
            String::from_utf8_lossy(&o.stdout)
                .lines()
                .next()
                .map(|s| s.trim().to_string())
        })
        .filter(|s| !s.is_empty());
    #[cfg(not(windows))]
    let path_probe: Option<String> = silent_cmd("which")
        .arg("java")
        .output()
        .ok()
        .and_then(|o| {
            String::from_utf8_lossy(&o.stdout)
                .lines()
                .next()
                .map(|s| s.trim().to_string())
        })
        .filter(|s| !s.is_empty());

    if let Some(p) = path_probe {
        let pb = std::path::Path::new(&p).to_path_buf();
        if let Some(v) = probe(&pb) {
            push(p, v, "PATH");
        }
    }

    let mut candidates: Vec<std::path::PathBuf> = Vec::new();
    #[cfg(windows)]
    {
        for base in [
            "C:\\Program Files\\Java",
            "C:\\Program Files (x86)\\Java",
            "C:\\Program Files\\Eclipse Adoptium",
            "C:\\Program Files\\Microsoft\\jdk-17",
            "C:\\Program Files\\Microsoft\\jdk-21",
        ] {
            let root = std::path::Path::new(base);
            if let Ok(rd) = std::fs::read_dir(root) {
                for e in rd.flatten() {
                    candidates.push(e.path().join("bin").join("java.exe"));
                }
            }
        }
        if let Ok(appdata) = std::env::var("APPDATA") {
            let rt = std::path::Path::new(&appdata)
                .join(".minecraft")
                .join("runtime");
            if let Ok(gamma) = std::fs::read_dir(&rt) {
                for g in gamma.flatten() {
                    if let Ok(os) = std::fs::read_dir(g.path()) {
                        for o in os.flatten() {
                            candidates.push(o.path().join("bin").join("java.exe"));
                        }
                    }
                }
            }
        }
    }
    #[cfg(not(windows))]
    {
        for p in ["/usr/bin/java", "/usr/local/bin/java", "/opt/java/bin/java"] {
            candidates.push(std::path::PathBuf::from(p));
        }
    }

    for c in candidates {
        if let Some(v) = probe(&c) {
            let src = if c.to_string_lossy().contains(".minecraft") {
                "bundled"
            } else {
                "program-files"
            };
            push(c.to_string_lossy().to_string(), v, src);
        }
    }

    scan_managed_runtimes(&app, &mut found, &mut seen);
    found
}

fn find_java_bin(dir: &std::path::Path) -> Option<std::path::PathBuf> {
    let mut stack = vec![dir.to_path_buf()];
    while let Some(d) = stack.pop() {
        let entries = std::fs::read_dir(&d).ok()?;
        for e in entries.flatten() {
            let p = e.path();
            if p.is_dir() {
                stack.push(p);
            } else {
                #[cfg(windows)]
                let is_java = p.file_name().map(|n| n == "java.exe").unwrap_or(false)
                    && p.parent().map(|par| par.file_name().map(|n| n == "bin").unwrap_or(false)).unwrap_or(false);
                #[cfg(not(windows))]
                let is_java = p.file_name().map(|n| n == "java").unwrap_or(false)
                    && p.parent().map(|par| par.file_name().map(|n| n == "bin").unwrap_or(false)).unwrap_or(false);
                if is_java {
                    return Some(p);
                }
            }
        }
    }
    None
}

/// Stream a URL to the tmp zip with progress events. Truncates any partial
/// file first so retries never append to garbage.
async fn download_stream(
    app: &AppHandle,
    client: &reqwest::Client,
    url: &str,
    tmp_zip: &std::path::Path,
    major: u32,
    source: &str,
) -> Result<(), String> {
    use tokio::io::AsyncWriteExt;
    let mut res = client
        .get(url)
        .send()
        .await
        .map_err(|e| format!("{source}: {e}"))?;
    if !res.status().is_success() {
        return Err(format!("{source}: HTTP {}", res.status()));
    }
    let total = res.content_length().unwrap_or(0);
    if let Some(par) = tmp_zip.parent() {
        std::fs::create_dir_all(par).map_err(|e| e.to_string())?;
    }
    let mut file = tokio::fs::File::create(tmp_zip)
        .await
        .map_err(|e| e.to_string())?;
    let mut downloaded: u64 = 0;
    let mut last_pct: u64 = 101;
    let started = std::time::Instant::now();
    let mut last_log = std::time::Instant::now();
    let glog = |line: &str| {
        #[derive(serde::Serialize, Clone)]
        struct GameLog {
            stream: String,
            line: String,
        }
        let _ = app.emit(
            "tyx://game-log",
            GameLog {
                stream: "sys".into(),
                line: line.into(),
            },
        );
    };
    glog(&format!(
        "[tyx] downloading Java {major} runtime (~200 MB, one-time) — progress below, do not close…"
    ));
    loop {
        match res.chunk().await {
            Ok(Some(chunk)) => {
                file.write_all(&chunk).await.map_err(|e| e.to_string())?;
                downloaded += chunk.len() as u64;
                if total > 0 {
                    let pct = downloaded * 100 / total;
                    if pct != last_pct && (pct == 100 || pct >= last_pct.saturating_add(5) || last_pct == 101) {
                        last_pct = pct;
                        let _ = app.emit(
                            "tyx://java-dl",
                            JavaDownloadProgress { major, downloaded, total },
                        );
                    }
                    // Game-log mirror every ~3 s so the console never looks stuck.
                    if last_log.elapsed().as_secs() >= 3 || pct == 100 {
                        last_log = std::time::Instant::now();
                        let el = started.elapsed().as_secs_f64().max(0.5);
                        let bps = (downloaded as f64 / el) as u64;
                        let mb_s = bps as f64 / 1_048_576.0;
                        let eta = if bps > 0 && total > downloaded {
                            let s = (total - downloaded) / bps;
                            format!("{:02}:{:02}", s / 60, s % 60)
                        } else {
                            "…".into()
                        };
                        glog(&format!(
                            "[dl] java {major} {}/{} MB ({}%) · {:.1} MB/s · ETA {}",
                            format!("{:.1}", downloaded as f64 / 1_048_576.0),
                            format!("{:.1}", total as f64 / 1_048_576.0),
                            pct,
                            mb_s,
                            eta,
                        ));
                    }
                } else {
                    // Unknown total: still heartbeat so logs move.
                    if last_log.elapsed().as_secs() >= 3 {
                        last_log = std::time::Instant::now();
                        glog(&format!(
                            "[dl] java {major} {:.1} MB downloaded…",
                            downloaded as f64 / 1_048_576.0
                        ));
                    }
                }
            }
            Ok(None) => break,
            Err(e) => return Err(format!("{source}: interrupted: {e}")),
        }
    }
    file.flush().await.map_err(|e| e.to_string())?;
    drop(file);
    let _ = app.emit("tyx://java-dl", JavaDownloadProgress { major, downloaded, total });
    Ok(())
}

/// One download attempt, guarded by a 10-minute stall timeout.
async fn download_attempt(
    app: &AppHandle,
    client: &reqwest::Client,
    url: &str,
    tmp_zip: &std::path::Path,
    major: u32,
    source: &str,
) -> Result<(), String> {
    tokio::time::timeout(
        std::time::Duration::from_secs(600),
        download_stream(app, client, url, tmp_zip, major, source),
    )
    .await
    .map_err(|_| format!("{source}: timed out after 10 min"))?
}

/// Adoptium's GitHub release for this major → direct JRE zip URL (JDK as
/// backup). No API key; plain release metadata.
async fn github_jre_url(client: &reqwest::Client, major: u32) -> Result<Option<String>, String> {
    #[cfg(windows)]
    let kick = "x64_windows_hotspot";
    #[cfg(not(windows))]
    let kick = "x64_linux_hotspot";
    let api = format!("https://api.github.com/repos/adoptium/temurin{major}-binaries/releases/latest");
    let res = tokio::time::timeout(
        std::time::Duration::from_secs(30),
        client
            .get(&api)
            .header("Accept", "application/vnd.github+json")
            .send(),
    )
    .await
    .map_err(|_| "timed out".to_string())?
    .map_err(|e| e.to_string())?;
    if !res.status().is_success() {
        return Err(format!("HTTP {}", res.status()));
    }
    let v: serde_json::Value = res.json().await.map_err(|e| e.to_string())?;
    let assets = v
        .get("assets")
        .and_then(|a| a.as_array())
        .cloned()
        .unwrap_or_default();
    let pick = |kind: &str| {
        assets
            .iter()
            .filter_map(|a| {
                let name = a.get("name")?.as_str()?;
                let url = a.get("browser_download_url")?.as_str()?;
                if name.ends_with(".zip") && name.contains(kick) && name.contains(kind) {
                    Some(url.to_string())
                } else {
                    None
                }
            })
            .next()
    };
    Ok(pick("-jre_").or_else(|| pick("-jdk_")))
}

/// Amazon Corretto "latest" JDK zips (stable URLs, no API). Windows-only
/// here — other platforms ship tarballs our extractor doesn't handle.
#[cfg(windows)]
fn corretto_url(major: u32) -> Option<String> {
    if ![8, 11, 17, 21, 25].contains(&major) {
        return None;
    }
    Some(format!(
        "https://corretto.aws/downloads/latest/amazon-corretto-{major}-x64-windows-jdk.zip"
    ))
}

async fn download_temurin(app: &AppHandle, major: u32) -> Result<std::path::PathBuf, String> {
    let target_dir = crate::util::data_root(app)
        .map_err(|e| e.to_string())?
        .join("runtimes")
        .join(format!("temurin-{major}"));
    #[cfg(windows)]
    let java_bin = target_dir.join("bin").join("java.exe");
    #[cfg(not(windows))]
    let java_bin = target_dir.join("bin").join("java");

    // Already provisioned? Direct path first, then the recursive search
    // (older installs extracted with the wrapper folder still nested).
    if let Some(v) = probe(&java_bin) {
        if java_major_of_version_line(&v) == Some(major) {
            return Ok(java_bin);
        }
    }
    if let Some(nested) = find_java_bin(&target_dir) {
        if let Some(v) = probe(&nested) {
            if java_major_of_version_line(&v) == Some(major) {
                return Ok(nested);
            }
        }
    }

    let adoptium_url = format!(
        "https://api.adoptium.net/v3/binary/latest/{major}/ga/windows/x64/jre/hotspot/normal/eclipse"
    );
    #[cfg(not(windows))]
    let adoptium_url = format!(
        "https://api.adoptium.net/v3/binary/latest/{major}/ga/linux/x64/jre/hotspot/normal/eclipse"
    );

    let client = reqwest::Client::builder()
        .user_agent("TyxLauncher/0.1.0")
        .build()
        .map_err(|e| e.to_string())?;
    let tmp_zip = target_dir.with_extension("downloading.zip");

    // Self-healing mirrors: Adoptium API first (retried with backoff), then
    // the Adoptium GitHub release, then Amazon Corretto. A single 504 (like
    // the one that once stranded a launch) must never be a dead end.
    let mut last_err = String::new();
    let mut done = false;
    for attempt in 1..=3u64 {
        match download_attempt(app, &client, &adoptium_url, &tmp_zip, major, "Adoptium").await {
            Ok(()) => {
                done = true;
                break;
            }
            Err(e) => {
                last_err = format!("Adoptium attempt {attempt}: {e}");
                if attempt < 3 {
                    tokio::time::sleep(std::time::Duration::from_secs(2 * attempt)).await;
                }
            }
        }
    }
    if !done {
        match github_jre_url(&client, major).await {
            Ok(Some(url)) => {
                for attempt in 1..=2u64 {
                    match download_attempt(app, &client, &url, &tmp_zip, major, "GitHub").await {
                        Ok(()) => {
                            done = true;
                            break;
                        }
                        Err(e) => {
                            last_err = format!("GitHub attempt {attempt}: {e}");
                            if attempt < 2 {
                                tokio::time::sleep(std::time::Duration::from_secs(3)).await;
                            }
                        }
                    }
                }
            }
            Ok(None) => last_err = "GitHub: no matching JRE/JDK asset in the latest release".into(),
            Err(e) => last_err = format!("GitHub: {e}"),
        }
    }
    #[cfg(windows)]
    if !done {
        if let Some(url) = corretto_url(major) {
            for attempt in 1..=2u64 {
                match download_attempt(app, &client, &url, &tmp_zip, major, "Corretto").await {
                    Ok(()) => {
                        done = true;
                        break;
                    }
                    Err(e) => {
                        last_err = format!("Corretto attempt {attempt}: {e}");
                        if attempt < 2 {
                            tokio::time::sleep(std::time::Duration::from_secs(3)).await;
                        }
                    }
                }
            }
        }
    }
    if !done {
        return Err(format!(
            "Java download failed ({last_err}). All mirrors unreachable — install Temurin manually and point to it in Settings → Runtime."
        ));
    }

    // Extract (blocking — Temurin zips unpack in seconds).
    let zip_path = tmp_zip.clone();
    let dest = target_dir.clone();
    tokio::task::spawn_blocking(move || -> Result<(), String> {
        let f = std::fs::File::open(&zip_path).map_err(|e| e.to_string())?;
        let mut zip = zip::ZipArchive::new(f).map_err(|e| format!("bad runtime archive: {e}"))?;
        for i in 0..zip.len() {
            let mut entry = zip.by_index(i).map_err(|e| e.to_string())?;
            let path = match entry.enclosed_name() {
                Some(p) => dest.join(p),
                None => continue, // skip unsafe paths
            };
            if entry.is_dir() {
                std::fs::create_dir_all(&path).map_err(|e| e.to_string())?;
            } else {
                if let Some(par) = path.parent() {
                    std::fs::create_dir_all(par).map_err(|e| e.to_string())?;
                }
                let mut out = std::fs::File::create(&path).map_err(|e| e.to_string())?;
                std::io::copy(&mut entry, &mut out).map_err(|e| e.to_string())?;
            }
        }
        Ok(())
    })
    .await
    .map_err(|e| e.to_string())??;
    let _ = std::fs::remove_file(&tmp_zip);

    // Temurin zips wrap everything in ONE top-level folder
    // (`jdk-21…-jre/`). Flatten it so `runtimes/temurin-N/bin/java.exe`
    // exists directly — without this the "already provisioned" check above
    // never hits and every launch re-downloads the whole ~200 MB runtime.
    if !java_bin.exists() {
        if let Ok(rd) = std::fs::read_dir(&target_dir) {
            let kids: Vec<std::path::PathBuf> = rd
                .flatten()
                .map(|e| e.path())
                .filter(|p| p.is_dir())
                .collect();
            if kids.len() == 1 {
                let inner = &kids[0];
                #[cfg(windows)]
                let inner_bin = inner.join("bin").join("java.exe");
                #[cfg(not(windows))]
                let inner_bin = inner.join("bin").join("java");
                if inner_bin.exists() {
                    if let Ok(moved) = std::fs::read_dir(inner) {
                        for e in moved.flatten() {
                            let to = target_dir.join(e.file_name());
                            let _ = std::fs::rename(e.path(), &to);
                        }
                    }
                    let _ = std::fs::remove_dir(inner);
                }
            }
        }
    }

    find_java_bin(&target_dir)
        .filter(|p| probe(p).is_some())
        .ok_or_else(|| "downloaded runtime is unusable — delete the runtimes folder and retry".to_string())
}

/// Find-or-download a Java runtime for `major` (8/17/21, newer on demand).
#[tauri::command]
pub async fn java_ensure(app: AppHandle, major: u32) -> Result<JavaInstall, String> {
    if !(8..=30).contains(&major) {
        return Err(format!("unsupported Java major ({major})"));
    }
    for j in java_detect(app.clone()).await {
        if java_major_of_version_line(&j.version) == Some(major) {
            return Ok(j);
        }
    }
    let path = download_temurin(&app, major).await?;
    let version = probe(&path).unwrap_or_else(|| format!("Temurin {major} (auto-installed)"));
    Ok(JavaInstall {
        path: path.to_string_lossy().to_string(),
        version,
        source: "auto".into(),
    })
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn mc_version_to_java_major() {
        // New Mojang year-based scheme (the 26.2 crash: fell through to 8).
        assert_eq!(java_major_for_mc("26.2"), 21);
        assert_eq!(java_major_for_mc("26.1"), 21);
        // Classic 1.x line.
        assert_eq!(java_major_for_mc("1.21.1"), 21);
        assert_eq!(java_major_for_mc("1.20.5"), 21);
        assert_eq!(java_major_for_mc("1.20.4"), 17);
        assert_eq!(java_major_for_mc("1.17.1"), 17);
        assert_eq!(java_major_for_mc("1.12.2"), 8);
        // Garbage must never resolve to 8 (instant JVM death on modern flags).
        assert_eq!(java_major_for_mc("whatever"), 21);
    }

    #[test]
    fn version_line_parsing() {
        assert_eq!(java_major_of_version_line("openjdk version \"21.0.3\""), Some(21));
        assert_eq!(java_major_of_version_line("java version \"1.8.0_411\""), Some(8));
        assert_eq!(java_major_of_version_line("openjdk version \"17.0.11\""), Some(17));
    }
}
