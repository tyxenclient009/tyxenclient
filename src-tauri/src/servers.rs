//! servers.dat sync — saved launcher servers appear in the in-game
//! Multiplayer list. Minimal NBT reader/writer (no extra crates) for exactly
//! the shape Minecraft uses:
//!
//!   TAG_Compound("") { TAG_List("servers") : [ TAG_Compound {
//!       TAG_String("name"), TAG_String("ip") }*, ... ] }
//!
//! Merge policy: entries are keyed by host+port. Ours update-or-append;
//! anything the player added by hand in-game is preserved untouched.

/// One multiplayer entry (host without port noise, display name).
#[derive(Debug, Clone, serde::Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ServerEntry {
    #[serde(default)]
    pub name: String,
    #[serde(default)]
    pub host: String,
    #[serde(default)]
    pub port: u16,
}

fn display_ip(e: &ServerEntry) -> String {
    if e.port == 25565 {
        e.host.clone()
    } else {
        format!("{}:{}", e.host, e.port)
    }
}

/// Split a stored `ip` back into host+port for key comparison.
fn split_ip(ip: &str) -> (String, u16) {
    let t = ip.trim().to_lowercase();
    if let Some(idx) = t.rfind(':') {
        if let Ok(p) = t[idx + 1..].parse::<u16>() {
            if p >= 1 {
                return (t[..idx].to_string(), p);
            }
        }
    }
    (t, 25565)
}

// ── NBT decode (defensive: any shape error → empty vec → fresh write) ────

struct Cursor<'a> {
    b: &'a [u8],
    pos: usize,
}

impl<'a> Cursor<'a> {
    fn byte(&mut self) -> Option<u8> {
        let v = *self.b.get(self.pos)?;
        self.pos += 1;
        Some(v)
    }
    fn u16(&mut self) -> Option<u16> {
        let hi = self.byte()? as u16;
        let lo = self.byte()? as u16;
        Some((hi << 8) | lo)
    }
    fn i32(&mut self) -> Option<i32> {
        let mut n: i32 = 0;
        for _ in 0..4 {
            n = (n << 8) | (self.byte()? as i32);
        }
        Some(n)
    }
    fn bytes(&mut self, n: usize) -> Option<&'a [u8]> {
        let s = self.b.get(self.pos..self.pos + n)?;
        self.pos += n;
        Some(s)
    }
    fn name(&mut self) -> Option<String> {
        let n = self.u16()? as usize;
        let b = self.bytes(n)?;
        Some(String::from_utf8_lossy(b).into_owned())
    }
    fn string_payload(&mut self) -> Option<String> {
        let n = self.u16()? as usize;
        let b = self.bytes(n)?;
        Some(String::from_utf8_lossy(b).into_owned())
    }
    /// Skip one unnamed list element payload of `tag`.
    fn skip_payload(&mut self, tag: u8) -> Option<()> {
        match tag {
            0 => Some(()),
            1 => {
                self.bytes(1)?;
                Some(())
            }
            2 => {
                self.bytes(2)?;
                Some(())
            }
            3 | 4 => {
                self.bytes(4)?;
                Some(())
            }
            5 | 6 => {
                self.bytes(8)?;
                Some(())
            }
            7 => {
                let n = self.i32()? as usize;
                self.bytes(n)?;
                Some(())
            }
            8 => {
                self.string_payload()?;
                Some(())
            }
            9 => {
                let t = self.byte()?;
                let n = self.i32()? as usize;
                for _ in 0..n {
                    self.skip_payload(t)?;
                }
                Some(())
            }
            10 => {
                loop {
                    let t = self.byte()?;
                    if t == 0 {
                        break;
                    }
                    self.name()?;
                    self.skip_named_payload(t)?;
                }
                Some(())
            }
            11 => {
                let n = self.i32()? as usize;
                self.bytes(n * 4)?;
                Some(())
            }
            12 => {
                let n = self.i32()? as usize;
                self.bytes(n * 8)?;
                Some(())
            }
            _ => None,
        }
    }
    fn skip_named_payload(&mut self, tag: u8) -> Option<()> {
        self.name()?;
        self.skip_payload(tag)
    }
}

fn read_existing(data: &[u8]) -> Vec<(String, String)> {
    fn inner(data: &[u8]) -> Option<Vec<(String, String)>> {
        let mut out = Vec::new();
        let mut c = Cursor { b: data, pos: 0 };
        // Root TAG_Compound + name.
        if c.byte()? != 10 {
            return None;
        }
        c.name()?;
        // Walk root tags until TAG_End; we only care about List("servers").
        loop {
            let tag = match c.byte()? {
                0 => break,
                t => t,
            };
            let name = c.name()?;
            if tag == 9 && name == "servers" {
                let elem = c.byte()?;
                let len = c.i32()?;
                if len < 0 {
                    return None;
                }
                if elem != 10 {
                    // Unexpected element type — skip whole list.
                    for _ in 0..len as usize {
                        c.skip_payload(elem)?;
                    }
                    continue;
                }
                for _ in 0..len as usize {
                    let mut sname: Option<String> = None;
                    let mut sip: Option<String> = None;
                    loop {
                        let t = c.byte()?;
                        if t == 0 {
                            break;
                        }
                        let n = c.name()?;
                        if t == 8 && n == "name" {
                            sname = c.string_payload();
                        } else if t == 8 && n == "ip" {
                            sip = c.string_payload();
                        } else {
                            c.skip_payload(t)?;
                        }
                    }
                    if let (Some(n), Some(ip)) = (sname, sip) {
                        if !ip.trim().is_empty() {
                            out.push((n, ip));
                        }
                    }
                }
            } else {
                c.skip_named_payload(tag)?;
            }
        }
        Some(out)
    }
    inner(data).unwrap_or_default()
}

// ── NBT encode ────────────────────────────────────────────────────────────

fn w16(out: &mut Vec<u8>, v: u16) {
    out.push((v >> 8) as u8);
    out.push((v & 0xff) as u8);
}
fn w32(out: &mut Vec<u8>, v: i32) {
    out.extend_from_slice(&v.to_be_bytes());
}
fn wname(out: &mut Vec<u8>, name: &str) {
    w16(out, name.len() as u16);
    out.extend_from_slice(name.as_bytes());
}
fn wstring(out: &mut Vec<u8>, name: &str, value: &str) {
    out.push(8);
    wname(out, name);
    w16(out, value.len() as u16);
    out.extend_from_slice(value.as_bytes());
}

fn encode(entries: &[(String, String)]) -> Vec<u8> {
    let mut out = Vec::with_capacity(256 + entries.len() * 64);
    out.push(10); // TAG_Compound, root
    w16(&mut out, 0); // empty root name
    out.push(9); // TAG_List
    wname(&mut out, "servers");
    out.push(10); // element type: compound
    w32(&mut out, entries.len() as i32);
    for (name, ip) in entries {
        wstring(&mut out, "name", name);
        wstring(&mut out, "ip", ip);
        out.push(0); // TAG_End
    }
    out.push(0); // root TAG_End
    out
}

// ── Public sync ───────────────────────────────────────────────────────────

/// Merge `ours` into `<instance>/servers.dat`, preserving hand-added rows.
/// Never fatal — returns the final entry count or an error string.
pub fn sync_servers_dat(instance_dir: &std::path::Path, ours: &[ServerEntry]) -> Result<usize, String> {
    let path = instance_dir.join("servers.dat");
    let mut merged: Vec<(String, String)> = std::fs::read(&path)
        .ok()
        .map(|b| read_existing(&b))
        .unwrap_or_default();

    for e in ours {
        let host = e.host.trim().to_lowercase();
        if host.is_empty() {
            continue;
        }
        let port = if e.port == 0 { 25565 } else { e.port };
        let name = if e.name.trim().is_empty() { host.clone() } else { e.name.trim().to_string() };
        let ip = display_ip(&ServerEntry { name: String::new(), host: host.clone(), port });
        let mut placed = false;
        for (n, existing_ip) in merged.iter_mut() {
            let (h, p) = split_ip(existing_ip);
            if h == host && p == port {
                *n = name.clone();
                *existing_ip = ip.clone();
                placed = true;
                break;
            }
        }
        if !placed {
            if merged.len() >= 64 {
                break;
            }
            merged.push((name, ip));
        }
    }

    std::fs::write(&path, encode(&merged)).map_err(|e| e.to_string())?;
    Ok(merged.len())
}

// ── Live server ping (Feather-style list: status + MOTD + ping + place) ──
// Speaks the vanilla ServerListPing handshake over TCP (no MC copy needed),
// then geolocates the resolved IP. Offline/unreachable hosts return
// `online: false` — never an error — so rows render grey instead of dying.

/// What the server list shows per row.
#[derive(Debug, Clone, serde::Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ServerStatus {
    pub online: bool,
    pub motd: String,
    pub version: String,
    pub players_online: i64,
    pub players_max: i64,
    pub ping_ms: u64,
    pub ip: String,
    pub country: String,
    pub country_code: String,
    pub city: String,
    pub favicon: String,
}

fn offline(ip: &str) -> ServerStatus {
    ServerStatus {
        online: false,
        motd: String::new(),
        version: String::new(),
        players_online: 0,
        players_max: 0,
        ping_ms: 0,
        ip: ip.into(),
        country: String::new(),
        country_code: String::new(),
        city: String::new(),
        favicon: String::new(),
    }
}

fn write_varint(buf: &mut Vec<u8>, mut v: i32) {
    loop {
        let mut b = (v & 0x7f) as u8;
        v >>= 7;
        if v != 0 {
            b |= 0x80;
        }
        buf.push(b);
        if v == 0 {
            break;
        }
    }
}

fn write_string(buf: &mut Vec<u8>, s: &str) {
    write_varint(buf, s.len() as i32);
    buf.extend_from_slice(s.as_bytes());
}

fn frame(packet_id: i32, body: &[u8]) -> Vec<u8> {
    let mut inner = Vec::new();
    write_varint(&mut inner, packet_id);
    inner.extend_from_slice(body);
    let mut out = Vec::new();
    write_varint(&mut out, inner.len() as i32);
    out.extend_from_slice(&inner);
    out
}

struct Reader<'a> {
    data: &'a [u8],
    pos: usize,
}

impl<'a> Reader<'a> {
    fn varint(&mut self) -> Result<i32, String> {
        let mut v = 0i32;
        for i in 0..5 {
            let b = *self.data.get(self.pos).ok_or("short ping reply")?;
            self.pos += 1;
            v |= ((b & 0x7f) as i32) << (7 * i);
            if b & 0x80 == 0 {
                return Ok(v);
            }
        }
        Err("bad ping varint".into())
    }

    fn bytes(&mut self, n: usize) -> Result<&'a [u8], String> {
        if self.pos + n > self.data.len() {
            return Err("short ping reply".into());
        }
        let s = &self.data[self.pos..self.pos + n];
        self.pos += n;
        Ok(s)
    }

    fn string(&mut self) -> Result<String, String> {
        let n = self.varint()? as usize;
        if n > 65536 {
            return Err("ping string too long".into());
        }
        let b = self.bytes(n)?;
        String::from_utf8(b.to_vec()).map_err(|_| "bad ping text".to_string())
    }

    fn i64be(&mut self) -> Result<i64, String> {
        let b = self.bytes(8)?;
        let mut a = [0u8; 8];
        a.copy_from_slice(b);
        Ok(i64::from_be_bytes(a))
    }
}

/// Flatten Mojang's chat soup (string | {text,extra[]}) into one line.
fn plain_text(v: &serde_json::Value) -> String {
    match v {
        serde_json::Value::String(s) => s.clone(),
        serde_json::Value::Array(a) => a.iter().map(plain_text).collect(),
        serde_json::Value::Object(o) => {
            let mut s = o.get("text").and_then(|t| t.as_str()).unwrap_or("").to_string();
            if let Some(extra) = o.get("extra") {
                s.push_str(&plain_text(extra));
            }
            s
        }
        _ => String::new(),
    }
}

fn is_private_ip(ip: &std::net::IpAddr) -> bool {
    match ip {
        std::net::IpAddr::V4(v) => {
            let o = v.octets();
            o[0] == 127 || o[0] == 10 || (o[0] == 192 && o[1] == 168) || (o[0] == 172 && (16..32).contains(&o[1]))
        }
        std::net::IpAddr::V6(v) => v.is_loopback(),
    }
}

async fn geo_lookup(ip: &str) -> (String, String, String) {
    if ip.is_empty() {
        return (String::new(), String::new(), String::new());
    }
    let url = format!("http://ip-api.com/json/{ip}?fields=status,country,countryCode,city");
    let client = match reqwest::Client::builder()
        .user_agent("TyxLauncher/0.1.0")
        .timeout(std::time::Duration::from_secs(5))
        .build()
    {
        Ok(c) => c,
        Err(_) => return (String::new(), String::new(), String::new()),
    };
    let v: serde_json::Value = match client.get(&url).send().await {
        Ok(r) => match r.json().await {
            Ok(j) => j,
            Err(_) => return (String::new(), String::new(), String::new()),
        },
        Err(_) => return (String::new(), String::new(), String::new()),
    };
    if v.get("status").and_then(|s| s.as_str()) != Some("success") {
        return (String::new(), String::new(), String::new());
    }
    (
        v.get("country").and_then(|s| s.as_str()).unwrap_or("").into(),
        v.get("countryCode").and_then(|s| s.as_str()).unwrap_or("").into(),
        v.get("city").and_then(|s| s.as_str()).unwrap_or("").into(),
    )
}

async fn ping_once(host: &str, port: u16) -> Result<ServerStatus, String> {
    use tokio::io::{AsyncReadExt, AsyncWriteExt};
    let mut addrs = tokio::net::lookup_host((host, port))
        .await
        .map_err(|e| format!("can't resolve {host}: {e}"))?;
    let addr = addrs.next().ok_or_else(|| format!("can't resolve {host}"))?;
    let ip_str = addr.ip().to_string();

    let local = is_private_ip(&addr.ip());
    let t0 = std::time::Instant::now();
    let mut stream = tokio::time::timeout(
        std::time::Duration::from_secs(5),
        tokio::net::TcpStream::connect(addr),
    )
    .await
    .map_err(|_| "connection timed out".to_string())?
    .map_err(|e| format!("can't reach {host}:{port} ({e})"))?;

    // Handshake (protocol 767 = 1.21.1; servers accept any recent number).
    let mut hs = Vec::new();
    write_varint(&mut hs, 767);
    write_string(&mut hs, host);
    hs.extend_from_slice(&port.to_be_bytes());
    write_varint(&mut hs, 1);
    let mut req = frame(0x00, &hs);
    req.extend_from_slice(&frame(0x00, &[]));
    stream.write_all(&req).await.map_err(|e| e.to_string())?;

    // Status response: length + id + json.
    let mut len_buf = [0u8; 5];
    let mut len = 0i32;
    for i in 0..5 {
        stream.read_exact(&mut len_buf[i..i + 1]).await.map_err(|e| e.to_string())?;
        len |= ((len_buf[i] & 0x7f) as i32) << (7 * i);
        if len_buf[i] & 0x80 == 0 {
            break;
        }
    }
    if len <= 0 || len > 1_048_576 {
        return Err("bad status reply".into());
    }
    let mut payload = vec![0u8; len as usize];
    tokio::time::timeout(
        std::time::Duration::from_secs(5),
        stream.read_exact(&mut payload),
    )
    .await
    .map_err(|_| "status reply timed out".to_string())?
    .map_err(|e| e.to_string())?;
    let mut r = Reader { data: &payload, pos: 0 };
    let _pid = r.varint()?;
    let json = r.string()?;
    let v: serde_json::Value =
        serde_json::from_str(&json).map_err(|_| "bad status json".to_string())?;

    // Ping pong → real round-trip latency.
    let sent = 123456789i64;
    let pong = frame(0x01, &sent.to_be_bytes());
    stream.write_all(&pong).await.map_err(|e| e.to_string())?;
    let mut echo = vec![0u8; 0];
    let mut tmp = [0u8; 64];
    let n = tokio::time::timeout(std::time::Duration::from_secs(5), stream.read(&mut tmp))
        .await
        .map_err(|_| "ping timed out".to_string())?
        .map_err(|e| e.to_string())?;
    echo.extend_from_slice(&tmp[..n]);
    let mut r2 = Reader { data: &echo, pos: 0 };
    let _l2 = r2.varint()?;
    let _p2 = r2.varint()?;
    let _back = r2.i64be()?;
    let ping_ms = t0.elapsed().as_millis().min(9999) as u64;

    let version = v
        .get("version")
        .and_then(|x| x.get("name"))
        .and_then(|s| s.as_str())
        .unwrap_or("")
        .to_string();
    let players_online = v
        .get("players")
        .and_then(|x| x.get("online"))
        .and_then(|n| n.as_i64())
        .unwrap_or(0);
    let players_max = v
        .get("players")
        .and_then(|x| x.get("max"))
        .and_then(|n| n.as_i64())
        .unwrap_or(0);
    let motd = v
        .get("description")
        .map(plain_text)
        .unwrap_or_default()
        .split_whitespace()
        .collect::<Vec<_>>()
        .join(" ");
    let favicon = v
        .get("favicon")
        .and_then(|s| s.as_str())
        .unwrap_or("")
        .to_string();

    let (country, country_code, city) = if local {
        ("Local network".into(), String::new(), String::new())
    } else {
        geo_lookup(&ip_str).await
    };
    Ok(ServerStatus {
        online: true,
        motd,
        version,
        players_online,
        players_max,
        ping_ms,
        ip: ip_str,
        country,
        country_code,
        city,
        favicon,
    })
}

/// Ping a server for the list UI. Never throws for dead hosts — those come
/// back `online: false` so rows render grey. Whole op capped at ~15s.
#[tauri::command]
pub async fn server_ping(host: String, port: u16) -> Result<ServerStatus, String> {
    let host = host.trim().to_lowercase();
    if host.is_empty() {
        return Err("empty server address".into());
    }
    // Resolve first for the offline shape (even DNS failure reports the IP).
    let ip_guess = tokio::net::lookup_host((host.as_str(), port))
        .await
        .ok()
        .and_then(|mut a| a.next())
        .map(|a| a.ip().to_string())
        .unwrap_or_default();
    match tokio::time::timeout(std::time::Duration::from_secs(15), ping_once(&host, port)).await {
        Ok(Ok(s)) => Ok(s),
        Ok(Err(_)) | Err(_) => Ok(offline(&ip_guess)),
    }
}
