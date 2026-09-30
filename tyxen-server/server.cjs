/**
 * TYXEN backend — apna khud ka server.
 * Badges (live online list), mod updates, announcements, users — sab yahin se.
 *
 * Chalane ke liye:  node server.js   (ya start.bat double-click)
 * Port: 8787 (mod isi se baat karta hai)
 *
 * Khatm karne ke liye: Ctrl+C
 */
const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const PORT = process.env.SERVER_PORT || process.env.PORT || 8787;
const ADMIN_USER = 'SatyamSen';
// Embedded staff UI (tyxen-admin/admin-ui/index.html) - served at GET /admin.
const ADMIN_HTML = `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Tyxen Admin</title>
<style>
*{box-sizing:border-box}
body{background:radial-gradient(1200px 600px at 20% -10%,#16233d 0%,#0b1120 55%,#070b16 100%);color:#e8edf5;font-family:Segoe UI,Arial,sans-serif;max-width:700px;margin:0 auto;padding:24px 16px;min-height:100vh}
.top{display:flex;align-items:center;gap:12px;margin-bottom:4px}
.logo{width:40px;height:40px;border-radius:10px;background:linear-gradient(135deg,#22c55e,#0ea5e9);display:flex;align-items:center;justify-content:center;font-weight:900;font-size:22px;color:#04121f}
h1{margin:0;font-size:22px;letter-spacing:.5px}
.sub{color:#8a93a6;font-size:12px;margin:2px 0 14px 52px}
.card{background:rgba(20,29,46,.72);border:1px solid rgba(255,255,255,.08);border-radius:14px;padding:14px 16px;margin:12px 0;backdrop-filter:blur(12px);box-shadow:0 8px 30px rgba(0,0,0,.35)}
.card h3{margin:0 0 8px;font-size:14px;color:#cfe3ff}
.row{display:flex;gap:8px;margin:6px 0}
input{padding:9px 10px;background:rgba(0,0,0,.35);border:1px solid rgba(255,255,255,.1);border-radius:8px;color:#e8edf5;flex:1;font-size:13px}
input:focus{outline:none;border-color:#1E9BF0}
button{padding:9px 16px;background:linear-gradient(135deg,#1E9BF0,#0ea5e9);color:#fff;border:0;border-radius:8px;cursor:pointer;font-weight:bold;white-space:nowrap;font-size:13px}
button:disabled{opacity:.5;cursor:wait}
button.danger{background:linear-gradient(135deg,#b03232,#7f1d1d)}
button.ghost{background:rgba(255,255,255,.06)}
#msg{color:#37E393;min-height:20px;font-size:13px}
.user,.ann{display:flex;justify-content:space-between;align-items:center;gap:8px;background:rgba(0,0,0,.3);border:1px solid rgba(255,255,255,.07);border-radius:8px;padding:8px 12px;margin:6px 0;font-size:13px}
.ann{flex-direction:column;align-items:flex-start}
.ann b{color:#fff}.ann small{color:#8a93a6}
.badge{font-size:11px;color:#8a93a6;font-weight:normal}
.dot{display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:6px}
.ok{background:#37E393}.bad{background:#f87171}
.stat{font-size:14px;margin:3px 0}
.stat b{color:#fff}
#login{max-width:380px;margin:60px auto}
#dash{display:none}
.lock{font-size:44px;text-align:center}
</style>
</head>
<body>
<div id="login">
<div class="top"><div class="logo">T</div><h1>Tyxen Admin</h1></div>
<div class="sub">Staff only. Login required.</div>
<div class="card"><div class="lock">🔒</div>
<div class="row"><input id="user" placeholder="username" onkeydown="if(event.key==='Enter')unlock()"></div>
<div class="row"><input id="key" type="password" placeholder="password" onkeydown="if(event.key==='Enter')unlock()"></div>
<div class="row"><button onclick="unlock()">Login</button></div>
<div id="loginMsg"></div></div>
</div>
<div id="dash">
<div class="top"><div class="logo">T</div><h1>Tyxen Admin</h1></div>
<div class="sub">Staff dashboard — updates, users, announcements.</div>
<div class="card"><h3><span id="dot" class="dot bad"></span>Live status <span style="float:right"><button class="ghost" onclick="load()">Refresh</button> <button class="ghost" onclick="lock()">Lock</button></span></h3><div id="status">—</div><div id="msg"></div></div>
<div class="card"><h3>Push update</h3>
<div class="row"><input id="f" type="file" accept=".jar"></div>
<div class="row"><input id="v" placeholder="version, e.g. 1.0.72"></div>
<div class="row"><input id="c" placeholder="changelog, one line"></div>
<div class="row"><button id="pushBtn" onclick="push()">Upload + Go Live</button></div>
</div>
<div class="card"><h3>Announcements <span class="badge">(shown in the launcher)</span></h3>
<div class="row"><input id="at" placeholder="Title"><input id="ab" placeholder="Detail (optional)"><button onclick="postAnn()">Post</button></div>
<div id="anns">—</div>
</div>
<div class="card"><h3>Users <span class="badge">(nametag badge)</span></h3>
<div class="row"><input id="u" placeholder="username"><button onclick="addUser()">Add</button></div>
<div id="users">—</div>
</div>
<div class="card"><h3>Allowed IPs <span class="badge">(empty = everyone; /admin hidden otherwise)</span></h3>
<div class="row"><input id="ip" placeholder="e.g. 1.2.3.4"><button onclick="addIp()">Allow</button><button class="ghost" onclick="addMyIp()">Allow my IP</button></div>
<div id="ips">—</div>
</div>
</div>
<script>
const $ = id => document.getElementById(id);
const SRV = location.origin;
function tok(){ return sessionStorage.getItem('txtok') || ''; }
function say(t, bad){ $('msg').textContent = t; $('msg').style.color = bad ? '#f87171' : '#37E393'; }
async function unlock(){
  const u = $('user').value.trim();
  const k = $('key').value;
  if (!u || !k) return;
  try {
    const r = await (await fetch(SRV + '/admin/login', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ username: u, password: k }) })).json();
    if (r.ok && r.token) {
      sessionStorage.setItem('txtok', r.token);
      $('key').value = '';
      $('login').style.display = 'none';
      $('dash').style.display = 'block';
      load();
    } else {
      $('loginMsg').textContent = 'Wrong login. Access denied.';
      $('loginMsg').style.color = '#f87171';
    }
  } catch(e){ $('loginMsg').textContent = 'Server unreachable.'; $('loginMsg').style.color = '#f87171'; }
}
function lock(){
  const t = tok();
  if (t) fetch(SRV + '/admin/logout', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ token: t }) });
  sessionStorage.removeItem('txtok');
  location.reload();
}
async function load(){
  const k = tok();
  if (!k) { lock(); return; }
  try {
    const m = await (await fetch(SRV + '/api/tyxen/latest')).json();
    const s = await (await fetch(SRV + '/admin/stats', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ token: k }) })).json();
    if (s.error) { lock(); return; }
    const u = s.users || [];
    const anns = s.announcements || [];
    $('dot').className = 'dot ok';
    const on = s.online || { online: 0, total: 0 };
    $('status').innerHTML = '<div class="stat">Version: <b>' + m.version + '</b> — ' + (m.changelog || '') + '</div>'
      + '<div class="stat">Players: <b>' + on.online + ' online / ' + on.total + ' total</b></div>'
      + '<div class="stat">Users: <b>' + u.length + '</b></div>';
    $('users').innerHTML = u.map(n =>
      '<div class="user"><span>' + n + '</span><button class="danger" onclick="delUser(\\'' + n + '\\')">Remove</button></div>').join('') || '—';
    $('anns').innerHTML = anns.map(a =>
      '<div class="ann"><b>' + a.title + '</b><small>' + (a.body || '') + ' · ' + (a.date || '') + '</small><div><button class="danger" onclick="delAnn(' + a.id + ')">Delete</button></div></div>').join('') || '—';
    loadIpsFromStats();
    say('');
  } catch(e){ $('dot').className = 'dot bad'; say('Cannot reach server: ' + e, true); }
}
async function loadIpsFromStats(){
  try {
    const k = tok();
    const s = await (await fetch(SRV + '/admin/ips', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ token: k }) })).json();
    loadIps(s.ips || []);
  } catch(e){}
}
async function push(){
  const k = tok();
  const file = $('f').files[0];
  if (!file || !$('v').value) { say('Jar file + version required!', true); return; }
  $('pushBtn').disabled = true; say('Uploading ' + (file.size/1048576).toFixed(1) + ' MB...');
  try {
    const buf = await file.arrayBuffer();
    const bytes = new Uint8Array(buf);
    let bin = '';
    for (let i = 0; i < bytes.length; i += 8192) bin += String.fromCharCode.apply(null, bytes.subarray(i, i+8192));
    const res = await fetch(SRV + '/admin/upload', {
      method: 'POST', headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({ token: k, filename: file.name, version: $('v').value, changelog: $('c').value, data: btoa(bin) })
    });
    say(await res.text(), !res.ok);
    if (res.ok) load();
  } catch(e){ say('Fail: ' + e, true); }
  $('pushBtn').disabled = false;
}
async function postAnn(){
  const k = tok();
  if (!$('at').value) { say('Write a title!', true); return; }
  const res = await fetch(SRV + '/admin/announce', { method: 'POST', headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ token: k, title: $('at').value, body: $('ab').value }) });
  if (res.ok) { $('at').value = ''; $('ab').value = ''; load(); }
  else say(await res.text(), true);
}
async function delAnn(id){
  const k = tok();
  await fetch(SRV + '/admin/announce/delete', { method: 'POST', headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ token: k, id }) });
  load();
}
async function addUser(){
  const k = tok();
  const n = $('u').value.trim();
  if (!n) return;
  await fetch(SRV + '/admin/users/add', { method: 'POST', headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ token: k, username: n }) });
  $('u').value = ''; load();
}
async function delUser(n){
  const k = tok();
  if (!confirm('Remove ' + n + '?')) return;
  await fetch(SRV + '/admin/users/remove', { method: 'POST', headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ token: k, username: n }) });
  load();
}
if (tok()) { $('login').style.display = 'none'; $('dash').style.display = 'block'; load(); }

async function loadIps(list){
  $('ips').innerHTML = (list || []).map(n =>
    '<div class="user"><span>' + n + '</span><button class="danger" onclick="delIp(\\'' + n + '\\')">Remove</button></div>').join('') || '<small>Empty = everyone can open /admin (login still required).</small>';
}
async function addIp(){
  const k = tok();
  const n = $('ip').value.trim();
  if (!n) return;
  const cur = await curIps(k);
  cur.push(n);
  await saveIps(k, cur);
  $('ip').value = ''; load();
}
async function addMyIp(){
  const k = tok();
  const cur = await curIps(k);
  const mine = prompt('Your IP (check ipv4.icanhazip.com in browser):', '');
  if (!mine) return;
  cur.push(mine.trim());
  await saveIps(k, cur);
  load();
}
async function delIp(n){
  const k = tok();
  const cur = (await curIps(k)).filter((x) => x !== n);
  await saveIps(k, cur);
  load();
}
async function curIps(k){
  try {
    const s = await (await fetch(SRV + '/admin/ips', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({ token: k }) })).json();
    return s.ips || [];
  } catch(e){ return []; }
}
async function saveIps(k, list){
  await fetch(SRV + '/admin/ips/set', { method: 'POST', headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ token: k, ips: list }) });
}

</script>
</body>
</html>
`;
// Embedded staff UI (tyxen-admin/admin-ui/index.html) - served at GET /admin.
const ADMIN_SALT = '7895485c52d196a8d20a3c74ef596f22';
const ADMIN_HASH = '1be680a443ae9849fbf8362490a592fb70177ccb956b8bcea0e2d898da5a7968';
const SESSIONS = new Map();
const SESSION_TTL = 12 * 3600 * 1000;
function checkPassword(pw) {
  try {
    const h = crypto.createHash('sha256').update(ADMIN_SALT + String(pw || '')).digest();
    const e = Buffer.from(ADMIN_HASH, 'hex');
    return h.length === e.length && crypto.timingSafeEqual(h, e);
  } catch {
    return false;
  }
}
function allowedIp(req) {
  try {
    const db = loadDb();
    const list = db.allowedIps || [];
    if (!list.length) return true;
    return list.includes(clientIp(req));
  } catch {
    return true;
  }
}
function authed(req, body) {
  const tok = (body && body.token) || req.headers['x-admin-token'] || '';
  if (!tok) return false;
  const s = SESSIONS.get(tok);
  if (!s) return false;
  if (Date.now() - s > SESSION_TTL) {
    SESSIONS.delete(tok);
    return false;
  }
  return true;
}
function needAuth(req, res, body) {
  if (!allowedIp(req)) {
    sendText(res, 'Denied.', 403);
    return true;
  }
  if (adminDenied(req, res)) return true;
  if (!authed(req, body)) {
    adminFail(req);
    sendText(res, 'Denied.', 403);
    return true;
  }
  return false;
}
const ROOT = __dirname;
const DATA = path.join(ROOT, 'data');
const FILES = path.join(ROOT, 'public', 'files');

if (!fs.existsSync(DATA)) fs.mkdirSync(DATA, { recursive: true });
if (!fs.existsSync(path.join(FILES, 'textures', 'skins'))) fs.mkdirSync(path.join(FILES, 'textures', 'skins'), { recursive: true });
if (!fs.existsSync(path.join(FILES, 'tyxen'))) fs.mkdirSync(path.join(FILES, 'tyxen'), { recursive: true });

const DB_FILE = path.join(DATA, 'db.json');
const USERS_FILE = path.join(DATA, 'users.json');

function defaultDb() {
  return {
    online: {},
    users: {}
  };
}

function loadDb() {
  if (!fs.existsSync(DB_FILE)) {
    const db = defaultDb();
    fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2));
    return db;
  }
  try {
    return JSON.parse(fs.readFileSync(DB_FILE, 'utf8'));
  } catch {
    // Torn write / corrupt JSON must never take every route down.
    // Fall back to defaults (in-memory) and try to preserve the broken file.
    try { fs.renameSync(DB_FILE, DB_FILE + '.corrupt-' + Date.now()); } catch {}
    const db = defaultDb();
    try { fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2)); } catch {}
    return db;
  }
}
function saveDb(db) {
  // Atomic write: tmp + rename so a crash never leaves a torn db.json.
  const tmp = DB_FILE + '.tmp';
  fs.writeFileSync(tmp, JSON.stringify(db, null, 2));
  fs.renameSync(tmp, DB_FILE);
}
function loadUsers() {
  if (!fs.existsSync(USERS_FILE)) {
    fs.writeFileSync(USERS_FILE, JSON.stringify(['senzqr'], null, 2));
    return ['senzqr'];
  }
  return JSON.parse(fs.readFileSync(USERS_FILE, 'utf8'));
}
function saveUsers(list) {
  fs.writeFileSync(USERS_FILE, JSON.stringify(list, null, 2));
}
// Badge list TTL: a username counts as "on Tyxen" only while its mod
// keeps pinging (join + client refresh re-pings). Permanent ever-joined
// list would badge ex-Tyxen players on other launchers forever.
const ONLINE_TTL_MS = 5 * 60 * 1000;
function liveUsers(db) {
  const now = Date.now();
  const map = db.online || {};
  return Object.keys(map).filter((u) => now - map[u] < ONLINE_TTL_MS).sort();
}
function ensureUser(db, username) {
  const key = username.toLowerCase();
  if (!db.users[key]) {
    db.users[key] = {};
  }
  return db.users[key];
}
// Minecraft usernames only — stops junk users ('steve', '{}', etc.)
// from malformed bodies, and wrong names from ever touching the db.
const NAME_RE = /^[A-Za-z0-9_]{3,16}$/;
function validName(n) {
  return typeof n === 'string' && NAME_RE.test(n);
}
const MAX_JSON_BODY = 64 * 1024 * 1024;
const FAILS = new Map();
function clientIp(req) {
  return ((req.headers['x-forwarded-for'] || '').split(',')[0] || '').trim() || (req.socket && req.socket.remoteAddress) || 'x';
}
function adminDenied(req, res) {
  const ip = clientIp(req);
  const now = Date.now();
  const f = FAILS.get(ip);
  if (f && f.until > now) {
    sendText(res, 'Slow down.', 429);
    return true;
  }
  return false;
}
function adminFail(req) {
  const ip = clientIp(req);
  const f = FAILS.get(ip) || { count: 0, until: 0 };
  f.count += 1;
  f.until = Date.now() + Math.min(600000, f.count * 30000);
  FAILS.set(ip, f);
}
function sendJson(res, obj, code = 200) {
  const body = JSON.stringify(obj);
  res.writeHead(code, { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) });
  res.end(body);
}
function sendText(res, text, code = 200, type = 'text/plain') {
  res.writeHead(code, { 'Content-Type': type, 'Content-Length': Buffer.byteLength(text) });
  res.end(text);
}
function readBody(req, maxBytes) {
  const limit = maxBytes || 256 * 1024;
  return new Promise((resolve) => {
    let size = 0;
    let done = false;
    let data = '';
    req.on('data', (c) => {
      if (done) return;
      size += c.length;
      if (size > limit) {
        done = true;
        resolve(null);
        try { req.destroy(); } catch {}
        return;
      }
      data += c;
    });
    req.on('end', () => {
      if (done) return;
      try { resolve(JSON.parse(data || '{}')); } catch { resolve({}); }
    });
  });
}
const MIME = { '.png': 'image/png', '.txt': 'text/plain', '.json': 'application/json', '.md5': 'text/plain', '.jar': 'application/java-archive' };

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://localhost');
  console.log(`[${new Date().toLocaleTimeString()}] ${req.method} ${url.pathname}${url.search}`);
  const db = loadDb();

  // NOTE: the cosmetics store (catalog/coins/equip) was removed — every
  // /api/store/* route now answers 410 Gone so old mod menus fail visibly
  // instead of hanging.
  if (url.pathname.startsWith('/api/store/')) {
    return sendJson(res, { error: 'Store removed.' }, 410);
  }

  // ---- tyxen user ping (nametag badge list + online tracking) ----
  if (url.pathname === '/api/tyxen/ping' && req.method === 'POST') {
    const body = await readBody(req);
    const username = (body.username || '').toLowerCase();
    if (!validName(body.username)) {
      return sendJson(res, { error: 'Invalid username.' }, 400);
    }
    ensureUser(db, username);
    db.online = db.online || {};
    db.online[username] = Date.now();
    saveDb(db);
    const list = loadUsers();
    if (!list.includes(username)) {
      list.push(username);
      saveUsers(list);
    }
    return sendJson(res, { ok: true });
  }

  // ---- mod update manifest (launcher check karta hai) ----
  function liveManifest() {
    try {
      const m = JSON.parse(fs.readFileSync(path.join(DATA, 'manifest.json'), 'utf8'));
      if (m && m.version) return m;
    } catch {}
    return { version: '1.0.73', url: '/files/mods/tyxen-1.0.73.jar', changelog: 'No-downgrade launcher guard; latest fixes rolled in.' };
  }
  if (url.pathname === '/api/tyxen/latest' && req.method === 'GET') {
    return sendJson(res, liveManifest());
  }

  // ---- ADMIN panel (login + IP allowlist) ----
  if (url.pathname === '/admin' && req.method === 'GET') {
    if (!allowedIp(req)) {
      return sendText(res, 'Not found.', 404);
    }
    return sendText(res, ADMIN_HTML, 200, 'text/html');
  }
  if (url.pathname === '/admin/login' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (!body || body.username !== ADMIN_USER || !checkPassword(body.password)) {
      adminFail(req);
      return sendJson(res, { ok: false });
    }
    const tok = crypto.randomBytes(32).toString('hex');
    SESSIONS.set(tok, Date.now());
    return sendJson(res, { ok: true, token: tok });
  }
  if (url.pathname === '/admin/logout' && req.method === 'POST') {
    const body = await readBody(req);
    if (body && body.token) SESSIONS.delete(body.token);
    return sendJson(res, { ok: true });
  }
  if (url.pathname === '/admin/stats' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (needAuth(req, res, body)) return;
    const db5 = loadDb();
    const now = Date.now();
    const onlineMap = db5.online || {};
    const online = Object.values(onlineMap).filter((t) => now - t < ONLINE_TTL_MS).length;
    const total = Object.keys(db5.users || {}).length;
    return sendJson(res, {
      users: loadUsers(),
      online: { online, total },
      announcements: db5.announcements || []
    });
  }
  if (url.pathname === '/admin/users' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (needAuth(req, res, body)) return;
    return sendJson(res, { users: loadUsers() });
  }
  if (url.pathname === '/admin/users/add' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (needAuth(req, res, body)) return;
    const raw = String((body && body.username) || '');
    if (!/^[A-Za-z0-9_]{3,16}$/.test(raw)) {
      return sendText(res, 'Invalid username.', 400);
    }
    const list = loadUsers();
    const name = raw.toLowerCase();
    if (!list.includes(name)) {
      list.push(name);
      saveUsers(list);
    }
    return sendJson(res, { ok: true, users: list });
  }
  if (url.pathname === '/admin/users/remove' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (needAuth(req, res, body)) return;
    const raw = String((body && body.username) || '');
    if (!/^[A-Za-z0-9_]{3,16}$/.test(raw)) {
      return sendText(res, 'Invalid username.', 400);
    }
    const name = raw.toLowerCase();
    saveUsers(loadUsers().filter((u) => u !== name));
    return sendJson(res, { ok: true, users: loadUsers() });
  }
  if (url.pathname === '/admin/upload' && req.method === 'POST') {
    if (adminDenied(req, res)) return;
    const body = await readBody(req, MAX_JSON_BODY);
    if (body === null) {
      return sendText(res, 'Too big.', 413);
    }
    if (needAuth(req, res, body)) return;
    if (!body.filename || !body.version || !body.data) {
      return sendText(res, 'filename + version + data required.', 400);
    }
    if (!/^\d+\.\d+\.\d+$/.test(String(body.version))) {
      return sendText(res, 'Version must look like 1.0.72.', 400);
    }
    const safe = path.basename(String(body.filename));
    if (!/^tyxen-\d+\.\d+\.\d+\.jar$/.test(safe)) {
      return sendText(res, 'Filename must be tyxen-X.Y.Z.jar.', 400);
    }
    const modDir = path.join(FILES, 'mods');
    if (!fs.existsSync(modDir)) fs.mkdirSync(modDir, { recursive: true });
    fs.writeFileSync(path.join(modDir, safe), Buffer.from(body.data, 'base64'));
    fs.writeFileSync(path.join(DATA, 'manifest.json'), JSON.stringify({
      version: body.version,
      url: '/files/mods/' + safe,
      changelog: String(body.changelog || '').slice(0, 200)
    }, null, 2));
    console.log(`[admin] Update live: ${body.version} (${safe})`);
    return sendText(res, `LIVE! ${body.version} — players will get the update.`);
  }
  if (url.pathname === '/admin/ips' && req.method === 'POST') {
    const body = await readBody(req);
    if (needAuth(req, res, body)) return;
    const db6 = loadDb();
    return sendJson(res, { ips: db6.allowedIps || [], mine: clientIp(req) });
  }
  if (url.pathname === '/admin/ips/set' && req.method === 'POST') {
    const body = await readBody(req);
    if (needAuth(req, res, body)) return;
    const list = Array.isArray(body.ips) ? body.ips.filter((x) => typeof x === 'string' && x.length < 64).slice(0, 50) : [];
    const db7 = loadDb();
    db7.allowedIps = list;
    saveDb(db7);
    return sendJson(res, { ok: true, ips: list });
  }
  // ---- announcements (launcher left section) ----
  if (url.pathname === '/api/tyxen/announcements' && req.method === 'GET') {
    const db2 = loadDb();
    return sendJson(res, { announcements: db2.announcements || [] });
  }
  if (url.pathname === '/admin/announce' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (needAuth(req, res, body)) return;
    if (!body.title) {
      return sendText(res, 'Title required.', 400);
    }
    const db3 = loadDb();
    db3.announcements = db3.announcements || [];
    db3.announcements.unshift({ id: Date.now(), title: body.title, body: body.body || '', date: new Date().toISOString().slice(0, 10) });
    db3.announcements = db3.announcements.slice(0, 20);
    saveDb(db3);
    return sendJson(res, { ok: true });
  }
  if (url.pathname === '/admin/announce/delete' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (needAuth(req, res, body)) return;
    const db4 = loadDb();
    const delId = Number(body.id);
    db4.announcements = (db4.announcements || []).filter((a) => a.id !== delId);
    saveDb(db4);
    return sendJson(res, { ok: true });
  }

  // ---- online counter: kitne online / kitne total ----
  if (url.pathname === '/api/tyxen/online' && req.method === 'GET') {
    const now = Date.now();
    const onlineMap = db.online || {};
    const online = Object.values(onlineMap).filter((t) => now - t < ONLINE_TTL_MS).length;
    const total = Object.keys(db.users || {}).length;
    return sendJson(res, { online, total });
  }

  // NOTE: the cape wardrobe was removed along with the store — every
  // /api/tyxen/cape* route answers 410 Gone so old mod menus fail visibly
  // instead of hanging.
  if (url.pathname === '/api/tyxen/capes' || url.pathname.startsWith('/api/tyxen/cape/')) {
    return sendJson(res, { ok: false, message: 'Capes removed.' }, 410);
  }

  // ---- user manifest (badge list for all clients) ----
  // LIVE list only: whoever's Tyxen mod pinged in the last 5 minutes.
  // Someone on another launcher never pings → never badged. Someone who
  // quits lingers at most ONLINE_TTL_MS, then drops off by itself.
  if (url.pathname === '/tyxen/active-users.txt' && req.method === 'GET') {
    return sendText(res, liveUsers(db).join('\n') + '\n');
  }
  if (url.pathname === '/tyxen/players.md5' && req.method === 'GET') {
    const hash = crypto.createHash('md5').update(JSON.stringify(liveUsers(db))).digest('hex');
    return sendText(res, hash + '\n');
  }

  // ---- static files (mod jars, misc) ----
  if ((url.pathname.startsWith('/textures/') || url.pathname.startsWith('/files/')) && req.method === 'GET') {
    let rel = decodeURIComponent(url.pathname);
    if (rel.startsWith('/files/')) rel = rel.slice('/files/'.length);
    else if (rel.startsWith('/')) rel = rel.slice(1);
    const file = path.normalize(path.join(FILES, rel));
    if (file !== FILES && !file.startsWith(FILES + path.sep)) {
      res.writeHead(403);
      return res.end('forbidden');
    }
    let st = null;
    try { st = fs.statSync(file); } catch {}
    if (!st || !st.isFile()) {
      res.writeHead(404);
      return res.end('not found');
    }
    const ext = path.extname(file).toLowerCase();
    res.writeHead(200, { 'Content-Type': MIME[ext] || 'application/octet-stream' });
    return fs.createReadStream(file).pipe(res);
  }

  res.writeHead(404);
  res.end('tyxen backend: unknown route');
});

server.listen(PORT, () => {
  console.log(`TYXEN backend chal raha hai: http://localhost:${PORT}`);
  console.log('Badges, updates, announcements — sab yahin se.');
});
