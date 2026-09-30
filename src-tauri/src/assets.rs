//! Mojang game-file pipeline: version manifest → version JSON → client jar,
//! libraries (+OS rules), natives extraction, logging config, asset objects.
//!
//! Everything lands inside the instance folder, verified by size on every
//! launch (sha1 checked on download for small files). Progress streams as
//! `tyx://assets` events: { stage, doneFiles, totalFiles, doneBytes,
//! totalBytes, current }.

use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use tauri::{AppHandle, Emitter};

const MANIFEST_URL: &str = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
const RESOURCES_URL: &str = "https://resources.download.minecraft.net";

#[cfg(windows)]
const OS_NAME: &str = "windows";
#[cfg(target_os = "linux")]
const OS_NAME: &str = "linux";
#[cfg(target_os = "macos")]
const OS_NAME: &str = "osx";
#[cfg(not(any(windows, target_os = "linux", target_os = "macos")))]
const OS_NAME: &str = "unknown";

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AssetsProgress {
    pub stage: String,
    pub done_files: u64,
    pub total_files: u64,
    pub done_bytes: u64,
    pub total_bytes: u64,
    pub current: String,
    /// Rolling download speed (bytes/sec). 0 = unknown / idle.
    #[serde(default)]
    pub speed_bps: u64,
    /// Estimated seconds left. u64::MAX = unknown.
    #[serde(default)]
    pub eta_secs: u64,
}

// ── Mojang metadata shapes ──────────────────────────────────────────────────

#[derive(Deserialize, Serialize)]
struct Manifest {
    versions: Vec<ManifestEntry>,
}
#[derive(Deserialize, Serialize)]
struct ManifestEntry {
    id: String,
    url: String,
}

#[derive(Deserialize, Serialize, Clone, Default)]
struct FileRef {
    #[serde(default)]
    path: String,
    #[serde(default)]
    sha1: String,
    #[serde(default)]
    size: u64,
    #[serde(default)]
    url: String,
}

#[derive(Deserialize, Serialize, Clone, Default)]
struct RuleOs {
    #[serde(default)]
    name: Option<String>,
    #[serde(default)]
    arch: Option<String>,
}
#[derive(Deserialize, Serialize, Clone)]
struct Rule {
    action: String,
    #[serde(default)]
    os: Option<RuleOs>,
    #[serde(default)]
    features: Option<serde_json::Value>,
}

#[derive(Deserialize, Serialize, Clone)]
#[serde(untagged)]
pub(crate) enum ArgValue {
    Str(String),
    Ruled { rules: Vec<Rule>, value: ValueOrVec },
}
#[derive(Deserialize, Serialize, Clone)]
#[serde(untagged)]
enum ValueOrVec {
    One(String),
    Many(Vec<String>),
}

#[derive(Deserialize, Serialize, Clone, Default)]
struct LibraryDownloads {
    #[serde(default)]
    artifact: Option<FileRef>,
    #[serde(default)]
    classifiers: Option<HashMap<String, FileRef>>,
}
#[derive(Deserialize, Serialize, Clone)]
pub(crate) struct Library {
    pub(crate) name: String,
    /// Maven base URL (loader profiles ship `name` + `url` instead of
    /// Mojang artifact URLs — e.g. https://maven.fabricmc.net/).
    #[serde(default)]
    pub(crate) url: String,
    #[serde(default)]
    downloads: LibraryDownloads,
    #[serde(default)]
    rules: Option<Vec<Rule>>,
    #[serde(default)]
    natives: Option<HashMap<String, String>>,
}

#[derive(Deserialize, Serialize, Clone, Default)]
pub struct AssetIndexRef {
    #[serde(default)]
    pub id: String,
    #[serde(default)]
    pub sha1: String,
    #[serde(default)]
    pub size: u64,
    #[serde(default)]
    pub url: String,
    #[serde(default, rename = "totalSize")]
    pub total_size: u64,
}
#[derive(Deserialize, Serialize)]
struct AssetObject {
    hash: String,
    size: u64,
}
#[derive(Deserialize, Serialize)]
struct AssetIndex {
    objects: HashMap<String, AssetObject>,
}

#[derive(Deserialize, Serialize, Clone, Default)]
struct LoggingFile {
    #[serde(default)]
    id: String,
    #[serde(default)]
    sha1: String,
    #[serde(default)]
    size: u64,
    #[serde(default)]
    url: String,
}
#[derive(Deserialize, Serialize, Clone, Default)]
struct LoggingClient {
    #[serde(default)]
    argument: String,
    #[serde(default)]
    file: LoggingFile,
}
#[derive(Deserialize, Serialize, Clone, Default)]
struct Logging {
    #[serde(default)]
    client: LoggingClient,
}

#[derive(Deserialize, Serialize, Clone, Default)]
struct VersionDownloads {
    #[serde(default)]
    client: FileRef,
}
#[derive(Deserialize, Serialize, Clone, Default)]
pub(crate) struct VersionArgs {
    #[serde(default)]
    pub(crate) game: Vec<ArgValue>,
    #[serde(default)]
    pub(crate) jvm: Vec<ArgValue>,
}

#[derive(Deserialize, Serialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct VersionJson {
    #[serde(default)]
    pub id: String,
    pub main_class: String,
    #[serde(default)]
    pub minecraft_arguments: Option<String>,
    #[serde(default)]
    pub arguments: Option<VersionArgs>,
    pub asset_index: AssetIndexRef,
    #[serde(default)]
    pub logging: Option<Logging>,
    #[serde(default)]
    pub downloads: VersionDownloads,
    #[serde(default)]
    pub libraries: Vec<Library>,
    /// Everything else Mojang ships (javaVersion, time, releaseTime, type,
    /// minimumLauncherVersion, complianceLevel, assets, …) — unmodeled on
    /// purpose, but preserved verbatim so the disk cache stays a faithful
    /// copy. Dropping javaVersion here once hid the Java 25 requirement
    /// and crashed every launch (26.2).
    #[serde(default, flatten)]
    pub extra: HashMap<String, serde_json::Value>,
}

// ── rules / substitution ────────────────────────────────────────────────────

fn os_matches(os: &Option<RuleOs>) -> bool {
    let Some(o) = os else { return true };
    if let Some(name) = &o.name {
        if name != OS_NAME {
            return false;
        }
    }
    if let Some(arch) = &o.arch {
        // Mojang tokens: "x86" (32-bit), "x86_64", "arm64".
        let a = arch.to_lowercase();
        let ok = if a.contains("x86_64") || a == "x64" || a == "amd64" {
            cfg!(target_arch = "x86_64")
        } else if a.contains("arm64") || a == "aarch64" {
            cfg!(target_arch = "aarch64")
        } else if a.contains("x86") || a == "i386" || a == "i686" {
            cfg!(target_arch = "x86")
        } else if a.contains("64") {
            cfg!(target_arch = "x86_64")
        } else {
            true // unknown token — don't gate on it
        };
        if !ok {
            return false;
        }
    }
    // `version` is a regex (old-macOS rules) — no engine; treat as match.
    true
}

/// Vanilla semantics (verified against real version.json: 66 allow-rules,
/// 0 disallow): no rules → ALLOW; rules present → each MATCHING rule
/// overrides (last match wins); NO match → DISALLOW. The old
/// default-allow leaked macOS-only flags (e.g. -XstartOnFirstThread) onto
/// Windows and killed the JVM at boot.
/// Feature-gated rules never match (we supply no features).
fn rules_allow(rules: &Option<Vec<Rule>>) -> bool {
    let Some(rs) = rules else { return true };
    if rs.is_empty() {
        return true;
    }
    let mut allow = false;
    for r in rs {
        if r.features.is_some() {
            continue;
        }
        if os_matches(&r.os) {
            allow = r.action == "allow";
        }
    }
    allow
}

fn eval_args(src: &[ArgValue]) -> Vec<String> {
    let mut out = Vec::new();
    for a in src {
        match a {
            ArgValue::Str(s) => out.push(s.clone()),
            ArgValue::Ruled { rules, value } => {
                if !rules_allow(&Some(rules.clone())) {
                    continue;
                }
                match value {
                    ValueOrVec::One(s) => out.push(s.clone()),
                    ValueOrVec::Many(v) => out.extend(v.clone()),
                }
            }
        }
    }
    out
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

fn subst(template: &str, vars: &HashMap<&str, String>) -> String {
    let mut out = template.to_string();
    for (k, v) in vars {
        out = out.replace(&format!("${{{k}}}"), v);
    }
    // Drop any leftover unknown placeholders rather than passing ${...} to java.
    while let Some(s) = out.find("${") {
        if let Some(e) = out[s..].find('}') {
            out.replace_range(s..s + e + 1, "");
        } else {
            break;
        }
    }
    out
}

// ── download helpers ────────────────────────────────────────────────────────
// DlCtx + fetch_file are pub(crate): the modpack installer reuses the same
// progress events + skip-when-size-matches + sha1-verify behavior.

pub(crate) struct DlCtx<'a> {
    pub(crate) app: &'a AppHandle,
    pub(crate) stage: String,
    pub(crate) done_files: u64,
    pub(crate) total_files: u64,
    pub(crate) done_bytes: u64,
    pub(crate) total_bytes: u64,
}

impl DlCtx<'_> {
    pub(crate) fn emit(&self, current: &str) {
        let _ = self.app.emit(
            "tyx://assets",
            AssetsProgress {
                stage: self.stage.clone(),
                done_files: self.done_files,
                total_files: self.total_files,
                done_bytes: self.done_bytes,
                total_bytes: self.total_bytes,
                current: current.to_string(),
                speed_bps: 0,
                eta_secs: u64::MAX,
            },
        );
    }
}

fn sha1_file(path: &std::path::Path) -> Result<String, String> {
    use sha1::{Digest, Sha1};
    let mut f = std::fs::File::open(path)
        .map_err(|e| format!("can't open {}: {e}", path.display()))?;
    let mut h = Sha1::new();
    std::io::copy(&mut f, &mut h).map_err(|e| format!("can't hash {}: {e}", path.display()))?;
    Ok(format!("{:x}", h.finalize()))
}

pub(crate) fn http_client() -> Result<reqwest::Client, String> {
    reqwest::Client::builder()
        .user_agent("TyxenLauncher/0.1.0")
        // Parallel downloader: big idle pool + keep-alive so 16 concurrent
        // Mojang/resource fetches reuse connections instead of re-handshaking.
        .pool_max_idle_per_host(32)
        .pool_idle_timeout(std::time::Duration::from_secs(60))
        .tcp_nodelay(true)
        .connect_timeout(std::time::Duration::from_secs(20))
        .timeout(std::time::Duration::from_secs(180))
        .build()
        .map_err(|e| e.to_string())
}

pub(crate) fn fmt_mb(bytes: u64) -> String {
    format!("{:.1}", bytes as f64 / 1_048_576.0)
}

pub(crate) fn fmt_speed(bps: u64) -> String {
    let mb = bps as f64 / 1_048_576.0;
    if mb >= 10.0 {
        format!("{mb:.0} MB/s")
    } else {
        format!("{mb:.1} MB/s")
    }
}

pub(crate) fn fmt_eta(secs: u64) -> String {
    if secs == u64::MAX {
        return "…".into();
    }
    let s = secs.min(99 * 3600);
    format!("{:02}:{:02}", s / 60, s % 60)
}

fn glog_line(app: &AppHandle, stream: &str, line: &str) {
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

/// Shared parallel-download progress hub.
///
/// `DlCtx` is single-threaded (sequential manifest/version phases). Bulk
/// phases (libraries / objects) run N downloads concurrently — atomics let
/// every task report bytes without a lock, while a small mutex throttles
/// event spam (assets event ≤ ~7/s, game-log line ≤ 1/3s or per 5%).
pub(crate) struct Hub {
    app: AppHandle,
    stage: ParkingStage,
    done_files: std::sync::atomic::AtomicU64,
    total_files: u64,
    done_bytes: std::sync::atomic::AtomicU64,
    total_bytes: std::sync::atomic::AtomicU64,
    started: std::time::Instant,
    last_emit: std::sync::Mutex<std::time::Instant>,
    last_log: std::sync::Mutex<(std::time::Instant, u64)>,
}

use std::sync::atomic::Ordering as AtomicOrd;

struct ParkingStage(std::sync::Mutex<String>);
impl Hub {
    fn new(app: &AppHandle, stage: &str, done_files: u64, total_files: u64, done_bytes: u64, total_bytes: u64) -> Self {
        let now = std::time::Instant::now();
        Self {
            app: app.clone(),
            stage: ParkingStage(std::sync::Mutex::new(stage.to_string())),
            done_files: std::sync::atomic::AtomicU64::new(done_files),
            total_files,
            done_bytes: std::sync::atomic::AtomicU64::new(done_bytes),
            total_bytes: std::sync::atomic::AtomicU64::new(total_bytes),
            started: now,
            last_emit: std::sync::Mutex::new(now),
            last_log: std::sync::Mutex::new((now, 101)),
        }
    }
    fn set_stage(&self, s: &str) {
        if let Ok(mut g) = self.stage.0.lock() {
            *g = s.to_string();
        }
    }
    fn stage_now(&self) -> String {
        self.stage.0.lock().map(|g| g.clone()).unwrap_or_default()
    }
    fn speed_bps(&self) -> u64 {
        let el = self.started.elapsed().as_secs_f64();
        if el < 0.5 {
            return 0;
        }
        (self.done_bytes.load(AtomicOrd::Relaxed) as f64 / el) as u64
    }
    fn eta_secs(&self) -> u64 {
        let sp = self.speed_bps();
        if sp == 0 {
            return u64::MAX;
        }
        let done = self.done_bytes.load(AtomicOrd::Relaxed);
        let total = self.total_bytes.load(AtomicOrd::Relaxed);
        if total <= done {
            return 0;
        }
        (total - done) / sp
    }
    /// Hot path: called per chunk. Emits at most ~7 events/sec.
    fn on_bytes(&self, n: u64, current: &str) {
        self.done_bytes.fetch_add(n, AtomicOrd::Relaxed);
        let now = std::time::Instant::now();
        let due = self.last_emit.lock().map(|mut g| {
            if now.duration_since(*g).as_millis() >= 150 {
                *g = now;
                true
            } else {
                false
            }
        }).unwrap_or(false);
        if due {
            self.emit(current);
            self.maybe_log(current, false);
        }
    }
    /// File completed (or skipped): always emit so the bar never stalls.
    fn on_file(&self, current: &str) {
        self.done_files.fetch_add(1, AtomicOrd::Relaxed);
        self.emit(current);
        self.maybe_log(current, false);
    }
    fn emit(&self, current: &str) {
        let _ = self.app.emit(
            "tyx://assets",
            AssetsProgress {
                stage: self.stage_now(),
                done_files: self.done_files.load(AtomicOrd::Relaxed),
                total_files: self.total_files,
                done_bytes: self.done_bytes.load(AtomicOrd::Relaxed),
                total_bytes: self.total_bytes.load(AtomicOrd::Relaxed),
                current: current.to_string(),
                speed_bps: self.speed_bps(),
                eta_secs: self.eta_secs(),
            },
        );
    }
    /// Game-log mirror so the log window NEVER looks stuck: a `[dl]` line
    /// at most every 3 s, or immediately on every 5 % step.
    fn maybe_log(&self, current: &str, force: bool) {
        let done = self.done_bytes.load(AtomicOrd::Relaxed);
        let total = self.total_bytes.load(AtomicOrd::Relaxed).max(1);
        let pct = (done * 100 / total).min(100);
        let now = std::time::Instant::now();
        let due = self.last_log.lock().map(|mut g| {
            let time_due = now.duration_since(g.0).as_secs() >= 3;
            let pct_due = pct >= g.1.saturating_add(5) || (pct == 100 && g.1 != 100);
            if force || time_due || pct_due {
                *g = (now, pct);
                true
            } else {
                false
            }
        }).unwrap_or(false);
        if due {
            let df = self.done_files.load(AtomicOrd::Relaxed);
            glog_line(
                &self.app,
                "sys",
                &format!(
                    "[dl] {} {df}/{} files · {}/{} MB ({}%) · {} · ETA {} · {current}",
                    self.stage_now(),
                    self.total_files,
                    fmt_mb(done),
                    fmt_mb(total),
                    pct,
                    fmt_speed(self.speed_bps()),
                    fmt_eta(self.eta_secs()),
                ),
            );
        }
    }
    fn log_force(&self, line: &str) {
        glog_line(&self.app, "sys", line);
    }
}

/// Download one file: skip when size matches, verify sha1 when known.
/// Transient failures (AV scanners locking or yanking fresh files on
/// first-seen machines, flaky wifi) retry 3× — one bare IO error must
/// never kill Play.
pub(crate) async fn fetch_file(
    client: &reqwest::Client,
    ctx: &mut DlCtx<'_>,
    path: &std::path::Path,
    url: &str,
    size: u64,
    sha1: Option<&str>,
    label: &str,
) -> Result<(), String> {
    if size > 0 {
        if let Ok(md) = std::fs::metadata(path) {
            if md.len() == size {
                ctx.done_files += 1;
                ctx.done_bytes += size;
                ctx.emit(label);
                return Ok(());
            }
        }
    } else if path.exists() {
        ctx.done_files += 1;
        ctx.emit(label);
        return Ok(());
    }
    if url.is_empty() {
        return Err(format!("no download URL for {label}"));
    }
    let mut last_err = String::new();
    for attempt in 1..=3u8 {
        match fetch_once(client, ctx, path, url, sha1, label).await {
            Ok(()) => {
                ctx.done_files += 1;
                ctx.emit(label);
                return Ok(());
            }
            Err(e) => {
                last_err = e;
                let _ = std::fs::remove_file(path.with_extension("downloading"));
                if attempt < 3 {
                    ctx.emit(&format!("{label} (retry {attempt}/3)"));
                    tokio::time::sleep(std::time::Duration::from_millis(750 * attempt as u64)).await;
                }
            }
        }
    }
    Err(format!("{label} failed after 3 tries: {last_err}"))
}

async fn fetch_once(
    client: &reqwest::Client,
    ctx: &mut DlCtx<'_>,
    path: &std::path::Path,
    url: &str,
    sha1: Option<&str>,
    label: &str,
) -> Result<(), String> {
    ctx.emit(label);
    if let Some(par) = path.parent() {
        std::fs::create_dir_all(par)
            .map_err(|e| format!("can't create folder {}: {e}", par.display()))?;
    }
    let mut res = client.get(url).send().await.map_err(|e| format!("{label}: {e}"))?;
    if !res.status().is_success() {
        return Err(format!("{label}: HTTP {}", res.status()));
    }
    let tmp = path.with_extension("downloading");
    {
        use tokio::io::AsyncWriteExt;
        let mut file = tokio::fs::File::create(&tmp)
            .await
            .map_err(|e| format!("can't write {}: {e}", tmp.display()))?;
        loop {
            match res.chunk().await {
                Ok(Some(chunk)) => {
                    file.write_all(&chunk)
                        .await
                        .map_err(|e| format!("can't write {}: {e}", tmp.display()))?;
                    ctx.done_bytes += chunk.len() as u64;
                    ctx.emit(label);
                }
                Ok(None) => break,
                Err(e) => return Err(format!("{label}: interrupted: {e}")),
            }
        }
        file.flush()
            .await
            .map_err(|e| format!("can't flush {}: {e}", tmp.display()))?;
    }
    if let Some(h) = sha1 {
        if !h.is_empty() {
            let got = sha1_file(&tmp)?;
            if got != h.to_lowercase() {
                let _ = std::fs::remove_file(&tmp);
                return Err(format!("{label}: checksum mismatch, retry launch"));
            }
        }
    }
    std::fs::rename(&tmp, path)
        .map_err(|e| format!("can't finalize {}: {e}", path.display()))?;
    Ok(())
}

/// Single download used by the parallel hub: skip-when-size-matches, then
/// stream with per-chunk progress into the hub. Retried 3× by the caller.
async fn fetch_single_hub(
    client: &reqwest::Client,
    hub: &std::sync::Arc<Hub>,
    path: &std::path::Path,
    url: &str,
    size: u64,
    sha1: Option<&str>,
    label: &str,
) -> Result<bool, String> {
    // Fast path: already on disk.
    if size > 0 {
        if let Ok(md) = std::fs::metadata(path) {
            if md.len() == size {
                hub.done_bytes.fetch_add(size, AtomicOrd::Relaxed);
                hub.on_file(label);
                return Ok(true);
            }
        }
    } else if path.exists() {
        hub.on_file(label);
        return Ok(true);
    }
    if url.is_empty() {
        return Err(format!("no download URL for {label}"));
    }
    let mut last_err = String::new();
    for attempt in 1..=3u8 {
        match fetch_stream_hub(client, hub, path, url, sha1, label).await {
            Ok(()) => {
                hub.on_file(label);
                return Ok(false);
            }
            Err(e) => {
                last_err = e;
                let _ = std::fs::remove_file(path.with_extension("downloading"));
                if attempt < 3 {
                    tokio::time::sleep(std::time::Duration::from_millis(500 * attempt as u64)).await;
                }
            }
        }
    }
    Err(format!("{label} failed after 3 tries: {last_err}"))
}

async fn fetch_stream_hub(
    client: &reqwest::Client,
    hub: &std::sync::Arc<Hub>,
    path: &std::path::Path,
    url: &str,
    sha1: Option<&str>,
    label: &str,
) -> Result<(), String> {
    if let Some(par) = path.parent() {
        std::fs::create_dir_all(par)
            .map_err(|e| format!("can't create folder {}: {e}", par.display()))?;
    }
    let mut res = client.get(url).send().await.map_err(|e| format!("{label}: {e}"))?;
    if !res.status().is_success() {
        return Err(format!("{label}: HTTP {}", res.status()));
    }
    let tmp = path.with_extension("downloading");
    {
        use tokio::io::AsyncWriteExt;
        let mut file = tokio::fs::File::create(&tmp)
            .await
            .map_err(|e| format!("can't write {}: {e}", tmp.display()))?;
        loop {
            match res.chunk().await {
                Ok(Some(chunk)) => {
                    file.write_all(&chunk)
                        .await
                        .map_err(|e| format!("can't write {}: {e}", tmp.display()))?;
                    hub.on_bytes(chunk.len() as u64, label);
                }
                Ok(None) => break,
                Err(e) => return Err(format!("{label}: interrupted: {e}")),
            }
        }
        file.flush()
            .await
            .map_err(|e| format!("can't flush {}: {e}", tmp.display()))?;
    }
    if let Some(h) = sha1 {
        if !h.is_empty() {
            let got = sha1_file(&tmp)?;
            if got != h.to_lowercase() {
                let _ = std::fs::remove_file(&tmp);
                return Err(format!("{label}: checksum mismatch, retry launch"));
            }
        }
    }
    std::fs::rename(&tmp, path)
        .map_err(|e| format!("can't finalize {}: {e}", path.display()))?;
    Ok(())
}

struct BulkItem {
    path: std::path::PathBuf,
    url: String,
    size: u64,
    sha1: Option<String>,
    label: String,
}

/// Parallel bulk downloader: `concurrency` simultaneous streams (16 for
/// small asset objects, 8 for big jars). Order-independent — every task
/// reports into the shared hub, failures abort with the first error.
async fn fetch_bulk(
    client: &reqwest::Client,
    hub: &std::sync::Arc<Hub>,
    items: Vec<BulkItem>,
    concurrency: usize,
) -> Result<(), String> {
    use tokio::sync::Semaphore;
    let sem = std::sync::Arc::new(Semaphore::new(concurrency.max(1)));
    let mut set = tokio::task::JoinSet::new();
    for it in items {
        let c = client.clone();
        let h = hub.clone();
        let permit = sem.clone();
        set.spawn(async move {
            let _p = permit.acquire_owned().await.map_err(|e| e.to_string())?;
            fetch_single_hub(&c, &h, &it.path, &it.url, it.size, it.sha1.as_deref(), &it.label).await?;
            Ok::<(), String>(())
        });
    }
    let mut first_err: Option<String> = None;
    while let Some(r) = set.join_next().await {
        match r {
            Ok(Ok(())) => {}
            Ok(Err(e)) => {
                if first_err.is_none() {
                    first_err = Some(e);
                }
            }
            Err(e) => {
                if first_err.is_none() {
                    first_err = Some(format!("download task crashed: {e}"));
                }
            }
        }
    }
    if let Some(e) = first_err {
        return Err(e);
    }
    Ok(())
}

/// `group:artifact:version` → relative repo path (for loader libs w/o URLs).
fn lib_path_from_name(name: &str) -> Option<String> {
    let mut it = name.split(':');
    let (g, a, v) = (it.next()?, it.next()?, it.next()?);
    Some(format!(
        "{}/{}/{}/{}-{}.jar",
        g.replace('.', "/"),
        a,
        v,
        a,
        v
    ))
}

// ── public pipeline ─────────────────────────────────────────────────────────

/// Mutant-cache detector: modern Mojang version jsons always declare
/// `javaVersion.majorVersion`. A cache missing it is stale (previously a
/// guaranteed JVM crash); an unparseable cache is corrupt. Both refetch.
fn is_suspect_version_cache(path: &std::path::Path, mc_version: &str) -> bool {
    // Old versions legitimately lack the declaration — never touch those.
    if crate::java::java_major_for_mc(mc_version) < 17 {
        return false;
    }
    let raw = match std::fs::read_to_string(path) {
        Ok(r) => r,
        Err(_) => return false,
    };
    match serde_json::from_str::<serde_json::Value>(&raw) {
        Ok(v) => v
            .get("javaVersion")
            .and_then(|j| j.get("majorVersion"))
            .and_then(|n| n.as_u64())
            .is_none(),
        Err(_) => true,
    }
}

pub struct LaunchVars {
    pub username: String,
    pub uuid: String,
    pub token: String,
    pub res_w: u32,
    pub res_h: u32,
}

/// Full ensure: manifest → version json → client → libraries → natives →
/// logging config → asset objects. Idempotent; skips verified files.
pub async fn ensure_assets(
    app: &AppHandle,
    instance_id: &str,
    mc_version: &str,
) -> Result<GameFiles, String> {
    let inst = crate::util::instance_dir(app, instance_id)?;
    let versions_dir = inst.join("versions");
    let libs_dir = inst.join("libraries");
    let assets_dir = inst.join("assets");
    let natives_dir = inst.join("natives");
    for d in [&versions_dir, &libs_dir, &assets_dir, &natives_dir] {
        std::fs::create_dir_all(d).map_err(|e| e.to_string())?;
    }

    let client = http_client()?;
    let mut ctx = DlCtx {
        app,
        stage: "manifest".into(),
        done_files: 0,
        total_files: 1,
        done_bytes: 0,
        total_bytes: 1,
    };

    // Version JSON (cached on disk after first fetch).
    let version_dir = versions_dir.join(mc_version);
    std::fs::create_dir_all(&version_dir).map_err(|e| e.to_string())?;
    let version_json_path = version_dir.join(format!("{mc_version}.json"));
    // Self-heal: a cached version json that parses but lacks the
    // javaVersion declaration on a MODERN version is stale (real Mojang
    // jsons declare it) — e.g. the mutant 26.2 cache that hid the Java 25
    // requirement and crashed the JVM. Drop it so the manifest fetch below
    // pulls the genuine file. Corrupt caches heal the same way.
    if version_json_path.exists() && is_suspect_version_cache(&version_json_path, mc_version) {
        let _ = std::fs::remove_file(&version_json_path);
    }
    let version: VersionJson = if version_json_path.exists() {
        let raw = std::fs::read_to_string(&version_json_path).map_err(|e| e.to_string())?;
        serde_json::from_str(&raw).map_err(|e| format!("bad cached version json: {e}"))?
    } else {
        ctx.emit("version list");
        let manifest: Manifest = client
            .get(MANIFEST_URL)
            .send()
            .await
            .map_err(|e| format!("cannot reach Mojang ({e}) — check your connection"))?
            .json()
            .await
            .map_err(|e| format!("bad Mojang response: {e}"))?;
        let entry = manifest
            .versions
            .iter()
            .find(|v| v.id == mc_version)
            .ok_or_else(|| format!("Minecraft {mc_version} not found on Mojang"))?;
        let v: VersionJson = client
            .get(&entry.url)
            .send()
            .await
            .map_err(|e| e.to_string())?
            .json()
            .await
            .map_err(|e| e.to_string())?;
        std::fs::write(
            &version_json_path,
            serde_json::to_string_pretty(&v).map_err(|e| e.to_string())?,
        )
        .map_err(|e| e.to_string())?;
        v
    };
    ctx.done_files = 1;
    ctx.emit("version list");

    // Mod-loader merge (Lunar parity): Fabric/Quilt profiles fold their
    // libraries + KnotClient mainClass into this JSON, so every step below
    // downloads and boots the loader with zero special-casing.
    let mut version = version;
    crate::loader::ensure_loader_merged(
        &app,
        &inst,
        instance_id,
        mc_version,
        &version_json_path,
        &mut version,
    )
    .await?;

    // Asset index (needed to count total work before downloading).
    let index_path = assets_dir
        .join("indexes")
        .join(format!("{}.json", version.asset_index.id));
    fetch_file(
        &client,
        &mut ctx,
        &index_path,
        &version.asset_index.url,
        version.asset_index.size,
        Some(&version.asset_index.sha1),
        "asset index",
    )
    .await?;
    let index_raw = std::fs::read_to_string(&index_path).map_err(|e| e.to_string())?;
    let index: AssetIndex = serde_json::from_str(&index_raw).map_err(|e| e.to_string())?;

    // Plan the file list (client + libraries + log config + objects).
    struct Job {
        path: std::path::PathBuf,
        url: String,
        size: u64,
        sha1: Option<String>,
        label: String,
        stage: &'static str,
    }
    let mut jobs: Vec<Job> = Vec::new();

    let client_jar = version_dir.join(format!("{mc_version}.jar"));
    jobs.push(Job {
        path: client_jar.clone(),
        url: version.downloads.client.url.clone(),
        size: version.downloads.client.size,
        sha1: Some(version.downloads.client.sha1.clone()),
        label: format!("client {mc_version}"),
        stage: "client",
    });

    struct NativeJob {
        path: std::path::PathBuf,
        url: String,
        size: u64,
        sha1: Option<String>,
    }
    let mut native_jobs: Vec<NativeJob> = Vec::new();
    let mut lib_jars: Vec<String> = vec![client_jar.to_string_lossy().to_string()];

    for lib in &version.libraries {
        if !rules_allow(&lib.rules) {
            continue;
        }
        // Natives classifier for this OS (e.g. natives-windows-${arch}).
        if let Some(natives) = &lib.natives {
            if let Some(raw) = natives.get(OS_NAME) {
                let classifier = raw.replace("${arch}", if std::mem::size_of::<usize>() == 8 { "64" } else { "32" });
                if let Some(f) = lib.downloads.classifiers.as_ref().and_then(|c| c.get(&classifier)) {
                    let p = libs_dir.join(&f.path);
                    native_jobs.push(NativeJob {
                        path: p,
                        url: f.url.clone(),
                        size: f.size,
                        sha1: Some(f.sha1.clone()),
                    });
                    continue; // natives jars never join the classpath
                }
            } else {
                continue; // natives lib for another OS
            }
        }
        if let Some(a) = &lib.downloads.artifact {
            let p = libs_dir.join(&a.path);
            lib_jars.push(p.to_string_lossy().to_string());
            jobs.push(Job {
                path: p,
                url: a.url.clone(),
                size: a.size,
                sha1: Some(a.sha1.clone()),
                label: lib.name.clone(),
                stage: "libraries",
            });
        } else if let Some(rel) = lib_path_from_name(&lib.name) {
            let p = libs_dir.join(&rel);
            lib_jars.push(p.to_string_lossy().to_string());
            // Loader libraries (Fabric/Quilt profiles) carry a maven base
            // URL instead of Mojang artifact URLs. They MUST be downloaded —
            // classpath-only entries boot to KnotClient ClassNotFound.
            if !lib.url.is_empty() {
                let already = std::fs::metadata(&p).map(|m| m.len() >= 1024).unwrap_or(false);
                if !already {
                    let base = lib.url.trim_end_matches('/');
                    jobs.push(Job {
                        path: p,
                        url: format!("{base}/{rel}"),
                        size: 0,
                        sha1: None,
                        label: lib.name.clone(),
                        stage: "libraries",
                    });
                }
            }
        }
    }

    let mut log_config_path: Option<String> = None;
    if let Some(logging) = &version.logging {
        if !logging.client.file.url.is_empty() {
            let p = assets_dir.join("log_configs").join(&logging.client.file.id);
            log_config_path = Some(p.to_string_lossy().to_string());
            jobs.push(Job {
                path: p,
                url: logging.client.file.url.clone(),
                size: logging.client.file.size,
                sha1: Some(logging.client.file.sha1.clone()),
                label: "logging config".into(),
                stage: "config",
            });
        }
    }

    let mut object_jobs: Vec<(String, u64)> = Vec::new(); // (hash, size)
    for obj in index.objects.values() {
        object_jobs.push((obj.hash.clone(), obj.size));
    }

    let total_files = (jobs.len() + native_jobs.len() + object_jobs.len()) as u64;
    let total_bytes = jobs.iter().map(|j| j.size).sum::<u64>()
        + native_jobs.iter().map(|n| n.size).sum::<u64>()
        + version.asset_index.total_size;
    // Overall hub: bar + speed + ETA + [dl] log mirror from here on, so the
    // console NEVER sits silent through the big multi-hundred-MB fetch.
    let hub = std::sync::Arc::new(Hub::new(app, "client", 1, total_files.max(1), 0, total_bytes.max(1)));
    hub.log_force(&format!(
        "[tyx] downloading game files: {} files · ~{} MB total · parallel (fast) — progress below, do not close…",
        total_files,
        fmt_mb(total_bytes),
    ));

    // Phase 1 — client jar + libraries + log config (8× parallel; big jars).
    hub.set_stage("client");
    hub.emit("starting…");
    hub.log_force(&format!(
        "[dl] phase 1/3 — client + libraries ({} files)…",
        jobs.len()
    ));
    let bulk: Vec<BulkItem> = jobs
        .iter()
        .map(|j| BulkItem {
            path: j.path.clone(),
            url: j.url.clone(),
            size: j.size,
            sha1: j.sha1.clone(),
            label: j.label.clone(),
        })
        .collect();
    // Stage label follows the majority phase for the bar title.
    if bulk.iter().any(|b| b.label.starts_with("client")) {
        hub.set_stage("client");
    } else {
        hub.set_stage("libraries");
    }
    fetch_bulk(&client, &hub, bulk, 8).await?;
    hub.maybe_log("libraries done", true);

    // Phase 2 — natives jars (small count, still parallel) + extraction.
    hub.set_stage("natives");
    hub.log_force(&format!(
        "[dl] phase 2/3 — natives ({} files)…",
        native_jobs.len()
    ));
    let nbulk: Vec<BulkItem> = native_jobs
        .iter()
        .map(|n| {
            let name = n.path.file_name().map(|x| x.to_string_lossy().to_string()).unwrap_or_default();
            BulkItem {
                path: n.path.clone(),
                url: n.url.clone(),
                size: n.size,
                sha1: n.sha1.clone(),
                label: format!("natives {name}"),
            }
        })
        .collect();
    fetch_bulk(&client, &hub, nbulk, 8).await?;
    for n in &native_jobs {
        // Extract (skip directories + signatures).
        let f = std::fs::File::open(&n.path)
            .map_err(|e| format!("can't open {}: {e}", n.path.display()))?;
        let mut zip = zip::ZipArchive::new(f).map_err(|e| e.to_string())?;
        for i in 0..zip.len() {
            let mut entry = zip.by_index(i).map_err(|e| e.to_string())?;
            let Some(rel) = entry.enclosed_name() else { continue };
            if entry.is_dir() {
                continue;
            }
            let rel_s = rel.to_string_lossy();
            if rel_s.starts_with("META-INF") {
                continue;
            }
            if let Some(name) = rel.file_name() {
                let dest = natives_dir.join(name);
                let mut out = std::fs::File::create(&dest)
                    .map_err(|e| format!("can't write {}: {e}", dest.display()))?;
                std::io::copy(&mut entry, &mut out)
                    .map_err(|e| format!("can't write {}: {e}", dest.display()))?;
            }
        }
    }
    hub.maybe_log("natives done", true);

    // Phase 3 — asset objects (thousands of tiny files, 16× parallel).
    // This is the long pole on first launch — the [dl] mirror + ETA is what
    // stops it looking "stuck".
    hub.set_stage("assets");
    hub.log_force(&format!(
        "[dl] phase 3/3 — assets ({} files, ~{} MB) — the big one, stay put…",
        object_jobs.len(),
        fmt_mb(version.asset_index.total_size),
    ));
    let obulk: Vec<BulkItem> = object_jobs
        .iter()
        .map(|(hash, size)| {
            let rel = format!("{}/{}", &hash[..2], hash);
            BulkItem {
                path: assets_dir.join("objects").join(&rel),
                url: format!("{RESOURCES_URL}/{rel}"),
                size: *size,
                sha1: None,
                label: format!("asset {}", &hash[..8]),
            }
        })
        .collect();
    fetch_bulk(&client, &hub, obulk, 16).await?;

    hub.set_stage("done");
    hub.emit("complete");
    hub.maybe_log("complete", true);
    {
        let el = hub.started.elapsed().as_secs();
        hub.log_force(&format!(
            "[tyx] game files ready — {} files in {}:{:02} — verifying & booting…",
            hub.done_files.load(AtomicOrd::Relaxed),
            el / 60,
            el % 60,
        ));
    }

    Ok(GameFiles {
        client_jar: client_jar.to_string_lossy().to_string(),
        lib_jars,
        natives_dir: natives_dir.to_string_lossy().to_string(),
        assets_dir: assets_dir.to_string_lossy().to_string(),
        asset_index_id: version.asset_index.id.clone(),
        version,
    })
}

pub struct GameFiles {
    pub client_jar: String,
    pub lib_jars: Vec<String>,
    pub natives_dir: String,
    pub assets_dir: String,
    pub asset_index_id: String,
    pub version: VersionJson,
}

/// Build the real JVM + game argument lists from version.json + instance vars.
pub fn build_launch_args(
    files: &GameFiles,
    instance_dir: &str,
    vars_in: &LaunchVars,
) -> Result<(Vec<String>, String, Vec<String>), String> {
    #[cfg(windows)]
    const SEP: &str = ";";
    #[cfg(not(windows))]
    const SEP: &str = ":";

    let classpath = files.lib_jars.join(SEP);
    let online = vars_in.token != "0" && !vars_in.token.is_empty();
    let mut vars: HashMap<&str, String> = HashMap::new();
    vars.insert("auth_player_name", vars_in.username.clone());
    vars.insert("version_name", files.version.id.clone());
    vars.insert("game_directory", instance_dir.to_string());
    vars.insert("assets_root", files.assets_dir.clone());
    vars.insert("assets_index_name", files.asset_index_id.clone());
    vars.insert("auth_uuid", vars_in.uuid.clone());
    vars.insert(
        "auth_access_token",
        if online { vars_in.token.clone() } else { "0".into() },
    );
    vars.insert("clientid", "0".into());
    vars.insert("auth_xuid", "0".into());
    vars.insert("user_type", if online { "msa".into() } else { "legacy".into() });
    vars.insert("version_type", "release".into());
    vars.insert("resolution_width", vars_in.res_w.max(640).to_string());
    vars.insert("resolution_height", vars_in.res_h.max(360).to_string());
    vars.insert("natives_directory", files.natives_dir.clone());
    vars.insert("launcher_name", "Tyxen Launcher".into());
    vars.insert("launcher_version", "0.1.0".into());
    vars.insert(
        "library_directory",
        std::path::Path::new(instance_dir)
            .join("libraries")
            .to_string_lossy()
            .to_string(),
    );
    vars.insert("classpath", classpath);
    vars.insert("classpath_separator", SEP.into());

    // JVM args.
    let mut jvm: Vec<String> = Vec::new();
    if let Some(args) = files.version.arguments.as_ref() {
        for a in eval_args(&args.jvm) {
            jvm.push(subst(&a, &vars));
        }
    } else {
        // Pre-1.13 layout.
        jvm.push(subst("-Djava.library.path=${natives_directory}", &vars));
        jvm.push(subst("-Dminecraft.client.jar=${classpath}", &vars));
    }
    if let Some(logging) = files.version.logging.as_ref() {
        if !logging.client.argument.is_empty() {
            let cfg = std::path::Path::new(&files.assets_dir)
                .join("log_configs")
                .join(&logging.client.file.id)
                .to_string_lossy()
                .to_string();
            vars.insert("path", cfg);
            jvm.push(subst(&logging.client.argument, &vars));
        }
    }
    if !jvm.iter().any(|a| a == "-cp" || a == "-classpath") {
        jvm.push("-cp".into());
        jvm.push(vars["classpath"].clone());
    }

    // Game args.
    let mut game: Vec<String> = if let Some(args) = files.version.arguments.as_ref() {
        eval_args(&args.game).iter().map(|a| subst(a, &vars)).collect()
    } else if let Some(legacy) = files.version.minecraft_arguments.as_ref() {
        split_args(legacy).iter().map(|a| subst(a, &vars)).collect()
    } else {
        return Err("version json has no launch arguments".into());
    };
    // The json gates --width/--height behind the has_custom_resolution
    // feature (always filtered out here) — but we always have a resolution
    // setting, so append them when no entry survived filtering.
    if !game.iter().any(|a| a == "--width") {
        game.push("--width".into());
        game.push(vars_in.res_w.max(640).to_string());
    }
    if !game.iter().any(|a| a == "--height") {
        game.push("--height".into());
        game.push(vars_in.res_h.max(360).to_string());
    }

    Ok((jvm, files.version.main_class.clone(), game))
}
