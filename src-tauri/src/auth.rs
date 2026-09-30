//! Auth helpers (Rust side).
//!
//! Microsoft/Xbox/XSTS/Minecraft runs HERE (reqwest) — never from the
//! WebView — because WebView fetch hits CORS/network quirks
//! ("Failed to fetch") on some machines. The frontend (`microsoft.ts`)
//! calls these commands on desktop and keeps its own fetch copy only for
//! the browser preview.
//! What else lives here:
//!   - deterministic offline UUIDs (UUID v3-style from the player name)
//!
//! Refresh-token persistence uses the Tauri `store` plugin on the frontend,
//! which encrypts-at-rest via the OS keychain on supported platforms.

/// FNV-1a 64-bit — tiny, stable, std-only hash for offline UUID derivation.
fn fnv1a64(bytes: &[u8], seed: u64) -> u64 {
    let mut h: u64 = seed;
    for b in bytes {
        h ^= *b as u64;
        h = h.wrapping_mul(0x100000001b3);
    }
    h
}

/// Deterministic offline-mode UUID for `username` (format 8-4-4-4-12).
/// Mirrors the idea of Java's `UUID.nameUUIDFromBytes("OfflinePlayer:"+name)`.
#[tauri::command]
pub fn offline_uuid(username: String) -> Result<String, String> {
    let name = username.trim();
    if name.is_empty() || name.len() > 16 {
        return Err("username must be 1–16 characters".into());
    }
    let input = format!("OfflinePlayer:{name}");
    let hi = fnv1a64(input.as_bytes(), 0xcbf29ce484222325);
    let lo = fnv1a64(input.as_bytes(), 0x84222325cbf29ce4);
    // Set version (3) + variant bits so clients accept the shape.
    let hi = (hi & 0xffffffffffff0fff) | 0x0000000000003000;
    let lo = (lo & 0x3fffffffffffffff) | 0x8000000000000000;
    Ok(format!(
        "{:08x}-{:04x}-{:04x}-{:04x}-{:012x}",
        (hi >> 32) as u32,
        ((hi >> 16) & 0xffff) as u16,
        (hi & 0xffff) as u16,
        ((lo >> 48) & 0xffff) as u16,
        lo & 0xffffffffffff
    ))
}

// ── Microsoft device-code flow (desktop; no WebView fetch) ────────────────

const MS_TOKEN_BASE: &str = "https://login.microsoftonline.com/consumers/oauth2/v2.0";
const SCOPE: &str = "XboxLive.signin offline_access";
const UA: &str = "TyxLauncher/0.1.0";

fn ms_client() -> Result<reqwest::Client, String> {
    reqwest::Client::builder()
        .user_agent(UA)
        .timeout(std::time::Duration::from_secs(25))
        .build()
        .map_err(|e| format!("couldn't reach Microsoft — check connection/firewall/VPN ({e})"))
}

fn need_client_id(client_id: &str) -> Result<String, String> {
    let id = client_id.trim();
    if id.is_empty() {
        return Err("Microsoft login is unavailable in this build — use offline mode.".into());
    }
    Ok(id.to_string())
}

/// Microsoft's devicecode response is snake_case — kept as-is (no rename).
/// Tolerant: `interval` / `message` / `verification_uri_complete` may be
/// missing on some tenants — defaults keep the flow alive instead of a
/// cryptic "bad Microsoft response" before the user even sees a code.
#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
pub struct DeviceCode {
    pub device_code: String,
    pub user_code: String,
    pub verification_uri: String,
    #[serde(default)]
    pub verification_uri_complete: Option<String>,
    pub expires_in: u64,
    #[serde(default = "default_interval")]
    pub interval: u64,
    #[serde(default)]
    pub message: String,
}

fn default_interval() -> u64 {
    5
}

/// Step 1: start device flow → code for the user to enter at microsoft.com/link.
// NOTE: `rename_all` keeps IPC arg keys snake_case (Tauri camelCases them
// by default, which silently broke every multi-word auth arg).
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_device_start(client_id: String) -> Result<DeviceCode, String> {
    let id = need_client_id(&client_id)?;
    let client = ms_client()?;
    let res = client
        .post(format!("{MS_TOKEN_BASE}/devicecode"))
        .form(&[("client_id", id.as_str()), ("scope", SCOPE)])
        .send()
        .await
        .map_err(|e| format!("couldn't reach Microsoft — check connection/firewall/VPN ({e})"))?;
    if !res.status().is_success() {
        return Err(format!("Microsoft devicecode failed (HTTP {})", res.status()));
    }
    let status = res.status();
    let text = res
        .text()
        .await
        .map_err(|e| format!("bad Microsoft response: {e}"))?;
    let mut dc: DeviceCode = serde_json::from_str::<DeviceCode>(&text).map_err(|e| {
        let snippet: String = text.chars().take(220).collect();
        // Surface Azure config errors plainly (invalid_client = device flow
        // disabled or wrong app type — the #1 public-release failure).
        if snippet.contains("invalid_client") || snippet.contains("unauthorized_client") {
            return "Microsoft login is temporarily unavailable — update the launcher or use offline mode.".to_string();
        }
        format!("bad Microsoft response (HTTP {status}) [{e}]: {snippet}")
    })?;
    if dc.interval == 0 {
        dc.interval = 5;
    }
    if dc.verification_uri.trim().is_empty() {
        return Err("Microsoft returned no verification URL — retry.".into());
    }
    Ok(dc)
}

/// Poll result — snake_case to match what the frontend reads.
#[derive(Debug, Clone, serde::Serialize)]
pub struct DeviceTokens {
    pub access_token: String,
    pub refresh_token: String,
}

/// Step 2: poll until the user approves. While waiting Microsoft answers
/// `authorization_pending` / `slow_down` — surfaced verbatim so the UI can
/// show "still waiting" instead of an error.
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_device_poll(client_id: String, device_code: String) -> Result<DeviceTokens, String> {
    let id = need_client_id(&client_id)?;
    let client = ms_client()?;
    let res = client
        .post(format!("{MS_TOKEN_BASE}/token"))
        .form(&[
            ("grant_type", "urn:ietf:params:oauth:grant-type:device_code"),
            ("client_id", id.as_str()),
            ("device_code", device_code.as_str()),
        ])
        .send()
        .await
        .map_err(|e| format!("couldn't reach Microsoft — check connection/firewall/VPN ({e})"))?;
    let status = res.status();
    let v: serde_json::Value = res.json().await.map_err(|e| format!("bad Microsoft response: {e}"))?;
    if !status.is_success() {
        // Keep the raw error code first — the UI matches on it.
        let code = v
            .get("error")
            .and_then(|e| e.as_str())
            .unwrap_or("token poll failed");
        let desc = v
            .get("error_description")
            .and_then(|e| e.as_str())
            .unwrap_or("");
        return Err(if desc.is_empty() {
            format!("{code} (HTTP {status})")
        } else {
            format!("{code}: {desc} (HTTP {status})")
        });
    }
    Ok(DeviceTokens {
        access_token: v
            .get("access_token")
            .and_then(|s| s.as_str())
            .unwrap_or("")
            .to_string(),
        refresh_token: v
            .get("refresh_token")
            .and_then(|s| s.as_str())
            .unwrap_or("")
            .to_string(),
    })
}

/// Step 2b: refresh an expired session via `refresh_token`.
/// Returns fresh tokens — caller must re-run `ms_login_with_xbox`.
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_refresh(client_id: String, refresh_token: String) -> Result<DeviceTokens, String> {
    let id = need_client_id(&client_id)?;
    if refresh_token.trim().is_empty() {
        return Err("No refresh token — sign in again.".into());
    }
    let client = ms_client()?;
    let res = client
        .post(format!("{MS_TOKEN_BASE}/token"))
        .form(&[
            ("grant_type", "refresh_token"),
            ("client_id", id.as_str()),
            ("refresh_token", refresh_token.trim()),
            ("scope", SCOPE),
        ])
        .send()
        .await
        .map_err(|e| format!("couldn't reach Microsoft — check connection/firewall/VPN ({e})"))?;
    let status = res.status();
    let v: serde_json::Value = res.json().await.map_err(|e| format!("bad Microsoft response: {e}"))?;
    if !status.is_success() {
        let code = v.get("error").and_then(|e| e.as_str()).unwrap_or("refresh failed");
        if code.contains("invalid_grant") {
            return Err("Session expired — sign in again.".into());
        }
        return Err(format!("{code} (HTTP {status})"));
    }
    Ok(DeviceTokens {
        access_token: v.get("access_token").and_then(|s| s.as_str()).unwrap_or("").to_string(),
        refresh_token: v.get("refresh_token").and_then(|s| s.as_str()).unwrap_or("").to_string(),
    })
}

#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct McProfile {
    pub id: String,
    pub name: String,
    pub access_token: String,
    pub refresh_token: String,
    pub expires_at: u64,
}

/// Shared tail: MS access token → XboxLive → XSTS → Minecraft → profile.
/// Used by BOTH the device-code command and the automatic browser flow.
async fn xbox_to_mc_profile(
    client: &reqwest::Client,
    ms_access: &str,
    refresh_token: String,
) -> Result<McProfile, String> {
    let net = |what: &str| format!("{what} unreachable — check connection/firewall/VPN");

    // 1. Xbox Live
    let xbl: serde_json::Value = client
        .post("https://user.auth.xboxlive.com/user/authenticate")
        .json(&serde_json::json!({
            "Properties": { "AuthMethod": "RPS", "SiteName": "user.auth.xboxlive.com", "RpsTicket": format!("d={ms_access}") },
            "RelyingParty": "http://auth.xboxlive.com",
            "TokenType": "JWT",
        }))
        .send()
        .await
        .map_err(|_| net("Xbox Live"))?
        .json()
        .await
        .map_err(|e| format!("bad Xbox response: {e}"))?;
    let (xbl_token, uhs) = (
        xbl.get("Token").and_then(|t| t.as_str()).unwrap_or(""),
        xbl.get("DisplayClaims")
            .and_then(|d| d.get("xui"))
            .and_then(|x| x.get(0))
            .and_then(|x| x.get("uhs"))
            .and_then(|u| u.as_str())
            .unwrap_or(""),
    );
    if xbl_token.is_empty() || uhs.is_empty() {
        return Err("Xbox Live rejected the Microsoft token (expired? try again)".into());
    }

    // 2. XSTS
    let xsts: serde_json::Value = client
        .post("https://xsts.auth.xboxlive.com/xsts/authorize")
        .json(&serde_json::json!({
            "Properties": { "SandboxId": "RETAIL", "UserTokens": [xbl_token] },
            "RelyingParty": "rp://api.minecraftservices.com/",
            "TokenType": "JWT",
        }))
        .send()
        .await
        .map_err(|_| net("XSTS"))?
        .json()
        .await
        .map_err(|e| format!("bad XSTS response: {e}"))?;
    let xsts_token = xsts.get("Token").and_then(|t| t.as_str()).unwrap_or("");
    if xsts_token.is_empty() {
        // Xbox error payloads carry XErr codes (e.g. 2148916233 = no Xbox account).
        let msg = xsts.get("XErr").map(|x| x.to_string()).unwrap_or_default();
        return Err(format!("XSTS refused sign-in{}.", if msg.is_empty() { String::new() } else { format!(" (XErr {msg})") }));
    }

    // 3. Minecraft (surface Microsoft's REAL reason on failure — the old
    // generic message was a dead end for debugging).
    let now = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .map(|d| d.as_millis() as u64)
        .unwrap_or(0);
    let res = client
        .post("https://api.minecraftservices.com/authentication/login_with_xbox")
        .json(&serde_json::json!({ "identityToken": format!("XBL3.0 x={uhs};{xsts_token}") }))
        .send()
        .await
        .map_err(|_| net("Minecraft services"))?;
    let status = res.status();
    let mc: serde_json::Value = res.json().await.map_err(|e| format!("bad Minecraft response: {e}"))?;
    let mc_token = mc.get("access_token").and_then(|t| t.as_str()).unwrap_or("");
    let expires_in = mc.get("expires_in").and_then(|e| e.as_u64()).unwrap_or(86400);
    if mc_token.is_empty() {
        // Typical shapes: {"errorMessage": "...", "error": "..."} or
        // {"error_description": "..."}. Never show a bare "refused" again.
        let detail = mc
            .get("errorMessage")
            .or(mc.get("error_description"))
            .or(mc.get("error"))
            .and_then(|v| v.as_str())
            .unwrap_or("")
            .trim();
        let etype = mc.get("errorType").and_then(|v| v.as_str()).unwrap_or("");
        let mut msg = if detail.is_empty() {
            format!("Minecraft refused the Xbox token (HTTP {status})")
        } else {
            let d: String = detail.chars().take(220).collect();
            if etype.is_empty() {
                format!("Minecraft refused the Xbox token (HTTP {status}): {d}")
            } else {
                format!("Minecraft refused the Xbox token (HTTP {status} {etype}): {d}")
            }
        };
        if status.as_u16() == 429 {
            msg.push_str(" — too many attempts, wait a few minutes and retry.");
        }
        return Err(msg);
    }

    // 4. Profile (404 = account owns no copy of Minecraft).
    let res = client
        .get("https://api.minecraftservices.com/minecraft/profile")
        .bearer_auth(mc_token)
        .send()
        .await
        .map_err(|_| net("Minecraft profile"))?;
    if res.status() == 404 {
        return Err("No Minecraft copy on this account (404).".into());
    }
    if !res.status().is_success() {
        return Err(format!("Profile fetch failed (HTTP {})", res.status()));
    }
    let prof: serde_json::Value = res.json().await.map_err(|e| format!("bad profile response: {e}"))?;
    let (pid, pname) = (
        prof.get("id").and_then(|v| v.as_str()).unwrap_or(""),
        prof.get("name").and_then(|v| v.as_str()).unwrap_or(""),
    );
    if pid.is_empty() || pname.is_empty() {
        return Err("Profile response was empty.".into());
    }
    Ok(McProfile {
        id: pid.replace('-', ""),
        name: pname.to_string(),
        access_token: mc_token.to_string(),
        refresh_token: refresh_token,
        // 2-minute early-expiry buffer so launches never race the clock.
        expires_at: now + expires_in.saturating_mul(1000).saturating_sub(120_000),
    })
}

// ── Automatic browser login (Modrinth-style, no code copy) ────────────────
// One click → system browser opens Microsoft login → user signs in normally
// → Microsoft redirects to a localhost callback we listen on → we swap the
// code for tokens and run the same Xbox chain. The user never touches a code.

/// Pending automatic login (one at a time; a new begin replaces the old).
pub struct AuthAutoState {
    inner: tokio::sync::Mutex<Option<AutoPending>>,
}

impl Default for AuthAutoState {
    fn default() -> Self {
        Self {
            inner: tokio::sync::Mutex::new(None),
        }
    }
}

struct AutoPending {
    rx: tokio::sync::oneshot::Receiver<Result<String, String>>,
    redirect_uri: String,
    client_id: String,
}

#[derive(Debug, Clone, serde::Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthBegin {
    pub auth_url: String,
    pub redirect_uri: String,
}

fn url_encode(s: &str) -> String {
    let mut out = String::with_capacity(s.len());
    for b in s.bytes() {
        match b {
            b'A'..=b'Z' | b'a'..=b'z' | b'0'..=b'9' | b'-' | b'_' | b'.' | b'~' => out.push(b as char),
            _ => out.push_str(&format!("%{b:02X}")),
        }
    }
    out
}

fn url_decode(s: &str) -> String {
    let mut out = String::with_capacity(s.len());
    let mut it = s.as_bytes().iter().peekable();
    while let Some(&b) = it.next() {
        if b == b'%' {
            let hi = it.next().copied().unwrap_or(b'0');
            let lo = it.next().copied().unwrap_or(b'0');
            let hex = |c: u8| match c {
                b'0'..=b'9' => (c - b'0') as u8,
                b'a'..=b'f' => (c - b'a' + 10) as u8,
                b'A'..=b'F' => (c - b'A' + 10) as u8,
                _ => 0,
            };
            out.push((hex(hi) << 4 | hex(lo)) as char);
        } else if b == b'+' {
            out.push(' ');
        } else {
            out.push(b as char);
        }
    }
    out
}

fn query_param(query: &str, key: &str) -> Option<String> {
    for pair in query.split('&') {
        let mut kv = pair.splitn(2, '=');
        if kv.next() == Some(key) {
            return kv.next().map(url_decode);
        }
    }
    None
}

const SUCCESS_HTML: &str = "<!doctype html><html><body style=\"background:#0e1210;color:#e8eeea;font-family:sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0\"><div style=\"text-align:center\"><div style=\"font-size:48px\">&#9989;</div><h2>Signed in — return to Tyx Launcher</h2><p>You can close this tab.</p><script>try{window.close()}catch(e){}</script></div></body></html>";
const ERROR_HTML: &str = "<!doctype html><html><body style=\"background:#0e1210;color:#e8eeea;font-family:sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0\"><div style=\"text-align:center\"><h2>Sign-in failed</h2><p>Return to Tyx Launcher and try again.</p></div></body></html>";

/// Serve exactly one callback request, then stop.
async fn serve_one_callback(
    listener: tokio::net::TcpListener,
    expected_state: String,
    tx: tokio::sync::oneshot::Sender<Result<String, String>>,
) {
    use tokio::io::{AsyncReadExt, AsyncWriteExt};
    let accept = tokio::time::timeout(std::time::Duration::from_secs(320), listener.accept()).await;
    let (mut stream, _) = match accept {
        Ok(Ok(pair)) => pair,
        _ => {
            let _ = tx.send(Err("Timed out — approve in the browser within 5 minutes, then retry.".into()));
            return;
        }
    };
    let mut buf = vec![0u8; 8192];
    let n = match tokio::time::timeout(std::time::Duration::from_secs(30), stream.read(&mut buf)).await {
        Ok(Ok(n)) => n,
        _ => {
            let _ = tx.send(Err("Browser callback unreadable — try again.".into()));
            return;
        }
    };
    let req = String::from_utf8_lossy(&buf[..n]).into_owned();
    let path = req
        .lines()
        .next()
        .and_then(|l| l.split_whitespace().nth(1))
        .unwrap_or("/")
        .to_string();
    let query = path.splitn(2, '?').nth(1).unwrap_or("").to_string();

    let (result, html) = if let Some(err) = query_param(&query, "error") {
        let desc = query_param(&query, "error_description").unwrap_or_default();
        let msg = if desc.is_empty() { err } else { format!("{err}: {desc}") };
        (Err(format!("Microsoft refused sign-in ({msg})")), ERROR_HTML)
    } else {
        match (query_param(&query, "code"), query_param(&query, "state")) {
            (Some(code), Some(state)) if !code.is_empty() => {
                if state != expected_state {
                    (Err("Sign-in response mismatched — retry (state check failed).".into()), ERROR_HTML)
                } else {
                    (Ok(code), SUCCESS_HTML)
                }
            }
            _ => (Err("Microsoft returned no code — retry.".into()), ERROR_HTML),
        }
    };
    let body = match &result {
        Ok(_) => SUCCESS_HTML,
        Err(_) => html,
    };
    let resp = format!(
        "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: {}\r\nConnection: close\r\n\r\n{}",
        body.len(),
        body
    );
    let _ = stream.write_all(resp.as_bytes()).await;
    let _ = stream.shutdown().await;
    let _ = tx.send(result);
}

/// Step 1 (auto): bind localhost, return the Microsoft login URL.
/// The frontend opens `auth_url` in the system browser, then calls
/// `ms_auth_wait` which resolves when the user finishes in the browser.
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_auth_begin(
    state: tauri::State<'_, AuthAutoState>,
    client_id: String,
) -> Result<AuthBegin, String> {
    let id = need_client_id(&client_id)?;
    // Fresh login replaces any stale pending one.
    *state.inner.lock().await = None;

    let listener = tokio::net::TcpListener::bind("127.0.0.1:0")
        .await
        .map_err(|e| format!("couldn't open a local login port (firewall?): {e}"))?;
    let port = listener
        .local_addr()
        .map_err(|e| e.to_string())?
        .port();
    // `http://localhost` (NOT 127.0.0.1): Microsoft ignores the port for
    // loopback redirects (RFC 8252 §7.3, same as VS Code), so a random free
    // port works with a single portal registration. 127.0.0.1 would need an
    // exact port match — impossible with ephemeral ports.
    // Root path (no /callback): the portal holds bare `http://localhost`,
    // and the path must match too — our listener accepts any path anyway.
    // Portal (one-time, app owner): App registrations → Authentication →
    // Add platform → Mobile and desktop applications → add `http://localhost`.
    let redirect_uri = format!("http://localhost:{port}/");
    let oauth_state = format!("{:x}{:x}", crate::util::now_millis(), std::process::id());

    let auth_url = format!(
        "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?client_id={}&response_type=code&redirect_uri={}&scope={}&state={}&prompt=select_account",
        url_encode(&id),
        url_encode(&redirect_uri),
        url_encode(SCOPE),
        url_encode(&oauth_state),
    );

    let (tx, rx) = tokio::sync::oneshot::channel();
    tokio::spawn(serve_one_callback(listener, oauth_state, tx));
    *state.inner.lock().await = Some(AutoPending {
        rx,
        redirect_uri: redirect_uri.clone(),
        client_id: id,
    });
    Ok(AuthBegin { auth_url, redirect_uri })
}

/// Step 2 (auto): wait for the browser callback (up to ~5 min), swap the
/// code for tokens, run the Xbox chain and return the finished profile.
#[tauri::command]
pub async fn ms_auth_wait(state: tauri::State<'_, AuthAutoState>) -> Result<McProfile, String> {
    let pending = state.inner.lock().await.take();
    let mut pending = pending.ok_or_else(|| "No pending login — press \"Login with Microsoft\" again.".to_string())?;
    let code = match tokio::time::timeout(std::time::Duration::from_secs(320), &mut pending.rx).await {
        Ok(Ok(Ok(code))) => code,
        Ok(Ok(Err(e))) => return Err(e),
        Ok(Err(_)) => return Err("Login was cancelled — press \"Login with Microsoft\" again.".into()),
        // Most common cause: Microsoft showed a `redirect_uri` error page in
        // the browser (no callback ever arrives). Fix: register
        // `http://localhost` under Mobile/desktop redirect URIs in Azure,
        // wait a few minutes, and retry.
        Err(_) => return Err("Timed out — if Microsoft showed a 'redirect_uri' error, register `http://localhost` as a Mobile/desktop redirect URI in Azure and retry.".into()),
    };
    let client = ms_client()?;
    let res = client
        .post(format!("{MS_TOKEN_BASE}/token"))
        .form(&[
            ("client_id", pending.client_id.as_str()),
            ("grant_type", "authorization_code"),
            ("code", code.as_str()),
            ("redirect_uri", pending.redirect_uri.as_str()),
        ])
        .send()
        .await
        .map_err(|e| format!("couldn't reach Microsoft — check connection/firewall/VPN ({e})"))?;
    let status = res.status();
    let v: serde_json::Value = res.json().await.map_err(|e| format!("bad Microsoft response: {e}"))?;
    if !status.is_success() {
        let code_err = v.get("error").and_then(|e| e.as_str()).unwrap_or("token exchange failed");
        if code_err.contains("invalid_client") {
            return Err("Microsoft login is temporarily unavailable — update the launcher or use offline mode.".into());
        }
        if code_err.contains("invalid_grant") {
            return Err("That sign-in expired or was already used — press Login again.".into());
        }
        return Err(format!("{code_err} (HTTP {status})"));
    }
    let access = v.get("access_token").and_then(|s| s.as_str()).unwrap_or("").to_string();
    let refresh = v.get("refresh_token").and_then(|s| s.as_str()).unwrap_or("").to_string();
    if access.is_empty() {
        return Err("Microsoft returned no access token — retry.".into());
    }
    xbox_to_mc_profile(&client, &access, refresh).await
}

/// Cancel a pending automatic login (e.g. user closed the dialog).
#[tauri::command]
pub async fn ms_auth_cancel(state: tauri::State<'_, AuthAutoState>) -> Result<(), String> {
    *state.inner.lock().await = None;
    Ok(())
}

/// Automatic login (Modrinth-style): an embedded sign-in window opens the
/// official Microsoft login — the user just types email + password, no
/// copy-paste. We watch navigation in Rust: when the browser reaches the
/// desktop.srf redirect (URL carries `?code=`), we capture it, close the
/// window, swap the code for tokens and run the Xbox chain.
/// Why embedded: localhost callbacks can't be registered on Mojang's
/// official app registration, and device-code flow is disabled on it.
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_auto_login(app: tauri::AppHandle, client_id: String) -> Result<McProfile, String> {
    use tauri::{Manager, WebviewUrl, WebviewWindowBuilder};
    let id = need_client_id(&client_id)?;
    let redirect_uri = "https://login.live.com/oauth20_desktop.srf";
    let auth_url = format!(
        "{MS_TOKEN_BASE}/authorize?client_id={}&response_type=code&redirect_uri={}&scope={}&prompt=select_account",
        url_encode(&id),
        url_encode(redirect_uri),
        url_encode(SCOPE),
    );
    let parsed: url::Url = auth_url.parse().map_err(|e| format!("bad auth url: {e}"))?;
    // A stale login window from a previous attempt must never linger.
    if let Some(w) = app.get_webview_window("ms-login") {
        let _ = w.close();
    }
    let (tx, mut rx) = tokio::sync::oneshot::channel::<Result<String, String>>();
    let tx = std::sync::Arc::new(std::sync::Mutex::new(Some(tx)));
    {
        let tx = tx.clone();
        let redirect = redirect_uri.to_string();
        let win = WebviewWindowBuilder::new(&app, "ms-login", WebviewUrl::External(parsed))
            .title("Sign in with Microsoft")
            .inner_size(480.0, 720.0)
            .center()
            .on_navigation(move |url| {
                let s = url.as_str();
                if !s.starts_with(redirect.as_str()) {
                    return true;
                }
                let query = s.splitn(2, '?').nth(1).unwrap_or("");
                let result = if let Some(err) = query_param(query, "error") {
                    let desc = query_param(query, "error_description").unwrap_or_default();
                    Err(if desc.is_empty() {
                        format!("Microsoft refused sign-in ({err})")
                    } else {
                        format!("Microsoft refused sign-in ({err}: {desc})")
                    })
                } else {
                    match query_param(query, "code") {
                        Some(code) if !code.is_empty() => Ok(code),
                        _ => Err("Microsoft returned no code — retry.".to_string()),
                    }
                };
                if let Ok(mut guard) = tx.lock() {
                    if let Some(sender) = guard.take() {
                        let _ = sender.send(result);
                    }
                }
                // Don't render the redirect page itself — we close next.
                false
            })
            .build()
            .map_err(|e| format!("couldn't open the sign-in window: {e}"))?;
        let _ = win.set_focus();
    }
    // Wait for the captured code, user closing the window, or ~5 min timeout.
    let outcome: Result<String, String> =
        tokio::time::timeout(std::time::Duration::from_secs(320), async {
            loop {
                tokio::select! {
                    r = &mut rx => {
                        return match r {
                            Ok(v) => v,
                            Err(_) => Err("Login was cancelled — press \"Login with Microsoft\" again.".to_string()),
                        };
                    }
                    _ = tokio::time::sleep(std::time::Duration::from_millis(500)) => {
                        if app.get_webview_window("ms-login").is_none() {
                            return Err("Login was cancelled — press \"Login with Microsoft\" again.".to_string());
                        }
                    }
                }
            }
        })
        .await
        .map_err(|_| "Timed out — sign in within 5 minutes, then retry.".to_string())?;
    if let Some(w) = app.get_webview_window("ms-login") {
        let _ = w.close();
    }
    let code = outcome?;
    let client = ms_client()?;
    let res = client
        .post(format!("{MS_TOKEN_BASE}/token"))
        .form(&[
            ("client_id", id.as_str()),
            ("grant_type", "authorization_code"),
            ("code", code.as_str()),
            ("redirect_uri", redirect_uri),
        ])
        .send()
        .await
        .map_err(|e| format!("couldn't reach Microsoft — check connection/firewall/VPN ({e})"))?;
    let status = res.status();
    let v: serde_json::Value = res.json().await.map_err(|e| format!("bad Microsoft response: {e}"))?;
    if !status.is_success() {
        let code_err = v.get("error").and_then(|e| e.as_str()).unwrap_or("token exchange failed");
        if code_err.contains("invalid_grant") {
            return Err("That sign-in expired or was already used — press Login again.".into());
        }
        if code_err.contains("invalid_client") {
            return Err("Microsoft login is temporarily unavailable — update the launcher or use offline mode.".into());
        }
        return Err(format!("{code_err} (HTTP {status})"));
    }
    let access = v.get("access_token").and_then(|s| s.as_str()).unwrap_or("").to_string();
    let refresh = v.get("refresh_token").and_then(|s| s.as_str()).unwrap_or("").to_string();
    if access.is_empty() {
        return Err("Microsoft returned no access token — retry.".into());
    }
    xbox_to_mc_profile(&client, &access, refresh).await
}

/// Paste-code flow (official launcher app ID): the user signs in via the
/// system browser, lands on the desktop.srf redirect with `?code=`, and
/// pastes the URL/code back. We swap the code for tokens and run the same
/// Xbox chain. Device-code flow is disabled on the official registration,
/// and custom app IDs are rejected by Minecraft Services (HTTP 403
/// "Invalid app registration"), so this is the supported path.
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_auth_exchange(client_id: String, code: String, redirect_uri: String) -> Result<McProfile, String> {
    let id = need_client_id(&client_id)?;
    let code = code.trim().to_string();
    if code.is_empty() {
        return Err("No code pasted — finish signing in to Microsoft in the browser, then paste the page address here.".into());
    }
    let client = ms_client()?;
    let res = client
        .post(format!("{MS_TOKEN_BASE}/token"))
        .form(&[
            ("client_id", id.as_str()),
            ("grant_type", "authorization_code"),
            ("code", code.as_str()),
            ("redirect_uri", redirect_uri.trim()),
        ])
        .send()
        .await
        .map_err(|e| format!("couldn't reach Microsoft — check connection/firewall/VPN ({e})"))?;
    let status = res.status();
    let v: serde_json::Value = res.json().await.map_err(|e| format!("bad Microsoft response: {e}"))?;
    if !status.is_success() {
        let code_err = v.get("error").and_then(|e| e.as_str()).unwrap_or("token exchange failed");
        if code_err.contains("invalid_grant") {
            return Err("That code expired, was already used, or was pasted wrong — sign in again and paste the fresh page address.".into());
        }
        if code_err.contains("invalid_client") {
            return Err("Microsoft login is temporarily unavailable — update the launcher or use offline mode.".into());
        }
        return Err(format!("{code_err} (HTTP {status})"));
    }
    let access = v.get("access_token").and_then(|s| s.as_str()).unwrap_or("").to_string();
    let refresh = v.get("refresh_token").and_then(|s| s.as_str()).unwrap_or("").to_string();
    if access.is_empty() {
        return Err("Microsoft returned no access token — retry.".into());
    }
    xbox_to_mc_profile(&client, &access, refresh).await
}

/// Steps 3–6 (device-code path): MS token → XboxLive → XSTS → Minecraft →
/// profile (proves game ownership; 404 = no copy of Minecraft on the account).
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_login_with_xbox(ms_access: String, refresh_token: String) -> Result<McProfile, String> {
    let client = ms_client()?;
    xbox_to_mc_profile(&client, &ms_access, refresh_token).await
}

// ── Owned skins + capes (Skins page: view everything, equip anything) ────

/// Full Minecraft profile (owned skins[] + capes[] with active flags).
/// 401 = session expired, re-sign in from the sidebar.
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_profile(mc_access: String) -> Result<serde_json::Value, String> {
    let client = ms_client()?;
    let res = client
        .get("https://api.minecraftservices.com/minecraft/profile")
        .bearer_auth(mc_access.trim())
        .send()
        .await
        .map_err(|e| format!("couldn't reach Mojang ({e})"))?;
    let status = res.status();
    if status.as_u16() == 401 {
        return Err("Minecraft session expired — sign in again from the sidebar.".into());
    }
    if !status.is_success() {
        return Err(format!("couldn't load profile (HTTP {status})"));
    }
    res.json().await.map_err(|e| format!("bad profile response: {e}"))
}

/// Equip one of the account's OWNED capes by id. Returns the updated profile.
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_equip_cape(mc_access: String, cape_id: String) -> Result<serde_json::Value, String> {
    let client = ms_client()?;
    let res = client
        .put("https://api.minecraftservices.com/minecraft/profile/capes/active")
        .bearer_auth(mc_access.trim())
        .json(&serde_json::json!({ "capeId": cape_id }))
        .send()
        .await
        .map_err(|e| format!("couldn't reach Mojang ({e})"))?;
    let status = res.status();
    if status.as_u16() == 401 {
        return Err("Minecraft session expired — sign in again from the sidebar.".into());
    }
    if !status.is_success() {
        let body = res.text().await.unwrap_or_default();
        let cut: String = body.chars().take(160).collect();
        return Err(format!("cape change rejected (HTTP {status}): {cut}"));
    }
    res.json().await.map_err(|e| format!("bad profile response: {e}"))
}

/// Equip one of the account's OWNED skins by id + variant. Returns the profile.
#[tauri::command(rename_all = "snake_case")]
pub async fn ms_equip_skin(
    mc_access: String,
    skin_id: String,
    variant: String,
) -> Result<serde_json::Value, String> {
    let v = variant.trim().to_uppercase();
    let v = if v == "SLIM" { "SLIM" } else { "CLASSIC" };
    let client = ms_client()?;
    let res = client
        .put("https://api.minecraftservices.com/minecraft/profile/skins/active")
        .bearer_auth(mc_access.trim())
        .json(&serde_json::json!({ "skinId": skin_id, "variant": v }))
        .send()
        .await
        .map_err(|e| format!("couldn't reach Mojang ({e})"))?;
    let status = res.status();
    if status.as_u16() == 401 {
        return Err("Minecraft session expired — sign in again from the sidebar.".into());
    }
    if !status.is_success() {
        let body = res.text().await.unwrap_or_default();
        let cut: String = body.chars().take(160).collect();
        return Err(format!("skin change rejected (HTTP {status}): {cut}"));
    }
    res.json().await.map_err(|e| format!("bad profile response: {e}"))
}
