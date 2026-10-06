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

// ---- minimal .env loader (stdlib only, no new dependency) ---------------
// Reads KEY=VALUE from a .env sitting next to this file and does not overwrite
// anything already set in the real environment, so a service manager still wins.
function loadDotEnv() {
  try {
    const p = path.join(__dirname, '.env');
    if (!fs.existsSync(p)) return;
    for (const line of fs.readFileSync(p, 'utf8').split(/\r?\n/)) {
      const t = line.trim();
      if (!t || t.startsWith('#')) continue;
      const eq = t.indexOf('=');
      if (eq < 1) continue;
      const k = t.slice(0, eq).trim();
      let v = t.slice(eq + 1).trim();
      if ((v.startsWith('"') && v.endsWith('"')) || (v.startsWith("'") && v.endsWith("'"))) v = v.slice(1, -1);
      if (process.env[k] === undefined) process.env[k] = v;
    }
  } catch {}
}
loadDotEnv();

const PORT = process.env.SERVER_PORT || process.env.PORT || 8787;

// ---- admin credential: never stored in this file ------------------------
// Both values come from the environment (.env is git-ignored; see
// .env.example). Generate the key without leaving the password in shell
// history with:   ADMIN_NEW_PASSWORD='…' node server.cjs --make-key
// If either value is missing, every admin login is refused (fail closed).
const ADMIN_USER = process.env.ADMIN_USER || '';
const ADMIN_KEY = process.env.ADMIN_KEY || '';
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
<div class="card"><h3><span id="dot" class="dot bad"></span>Live status <span style="float:right"><button class="ghost" onclick="load()">Refresh</button> <button class="ghost" onclick="revokeAll()">Sign out everywhere</button> <button class="ghost" onclick="lock()">Lock</button></span></h3><div id="status">—</div><div id="msg"></div></div>
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
// The server reads the session from this header so it can authorise an upload
// before it ever buffers the body.
function hdr(k){ return { 'Content-Type': 'application/json', 'x-admin-token': k }; }
const esc = s => String(s == null ? '' : s).replace(/[&<>"']/g,
  c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
let USERS = [], ANNS = [], IPS = [];
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
    const s = await (await fetch(SRV + '/admin/stats', { method: 'POST', headers: hdr(k), body: JSON.stringify({ token: k }) })).json();
    if (s.error) { lock(); return; }
    const u = s.users || [];
    const anns = s.announcements || [];
    USERS = u; ANNS = anns;
    $('dot').className = 'dot ok';
    const on = s.online || { online: 0, total: 0 };
    $('status').innerHTML = '<div class="stat">Version: <b>' + esc(m.version) + '</b> — ' + esc(m.changelog) + '</div>'
      + '<div class="stat">Players: <b>' + esc(on.online) + ' online / ' + esc(on.total) + ' total</b></div>'
      + '<div class="stat">Users: <b>' + esc(u.length) + '</b></div>';
    $('users').innerHTML = u.map((n, i) =>
      '<div class="user"><span>' + esc(n) + '</span><button class="danger" onclick="delUserAt(' + i + ')">Remove</button></div>').join('') || '—';
    $('anns').innerHTML = anns.map(a =>
      '<div class="ann"><b>' + esc(a.title) + '</b><small>' + esc(a.body) + ' · ' + esc(a.date) + '</small><div><button class="danger" onclick="delAnn(' + Number(a.id) + ')">Delete</button></div></div>').join('') || '—';
    loadIpsFromStats();
    say('');
  } catch(e){ $('dot').className = 'dot bad'; say('Cannot reach server: ' + e, true); }
}
async function loadIpsFromStats(){
  try {
    const k = tok();
    const s = await (await fetch(SRV + '/admin/ips', { method: 'POST', headers: hdr(k), body: JSON.stringify({ token: k }) })).json();
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
      method: 'POST', headers: hdr(k),
      body: JSON.stringify({ filename: file.name, version: $('v').value, changelog: $('c').value, data: btoa(bin) })
    });
    say(await res.text(), !res.ok);
    if (res.ok) load();
  } catch(e){ say('Fail: ' + e, true); }
  $('pushBtn').disabled = false;
}
async function postAnn(){
  const k = tok();
  if (!$('at').value) { say('Write a title!', true); return; }
  const res = await fetch(SRV + '/admin/announce', { method: 'POST', headers: hdr(k),
    body: JSON.stringify({ token: k, title: $('at').value, body: $('ab').value }) });
  if (res.ok) { $('at').value = ''; $('ab').value = ''; load(); }
  else say(await res.text(), true);
}
async function delAnn(id){
  const k = tok();
  await fetch(SRV + '/admin/announce/delete', { method: 'POST', headers: hdr(k),
    body: JSON.stringify({ token: k, id }) });
  load();
}
async function addUser(){
  const k = tok();
  const n = $('u').value.trim();
  if (!n) return;
  await fetch(SRV + '/admin/users/add', { method: 'POST', headers: hdr(k),
    body: JSON.stringify({ token: k, username: n }) });
  $('u').value = ''; load();
}
async function delUser(n){
  const k = tok();
  if (!confirm('Remove ' + n + '?')) return;
  await fetch(SRV + '/admin/users/remove', { method: 'POST', headers: hdr(k),
    body: JSON.stringify({ token: k, username: n }) });
  load();
}
if (tok()) { $('login').style.display = 'none'; $('dash').style.display = 'block'; load(); }

async function loadIps(list){
  IPS = list || [];
  $('ips').innerHTML = IPS.map((n, i) =>
    '<div class="user"><span>' + esc(n) + '</span><button class="danger" onclick="delIpAt(' + i + ')">Remove</button></div>').join('') || '<small>Empty = everyone can open /admin (login still required).</small>';
}
// Rows are addressed by index: allowed-IP values are free text, so splicing one
// into an onclick attribute would let an entry like x\');alert(1);// run code.
async function delIpAt(i){ await delIp(IPS[i]); }
async function delUserAt(i){ await delUser(USERS[i]); }
async function revokeAll(){
  const k = tok();
  if (!confirm('Sign out every admin session on this server?')) return;
  try {
    const r = await (await fetch(SRV + '/admin/revoke', { method: 'POST', headers: hdr(k), body: JSON.stringify({ token: k }) })).json();
    sessionStorage.removeItem('txtok');
    location.reload();
  } catch(e){ say('Failed: ' + e, true); }
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
    const s = await (await fetch(SRV + '/admin/ips', { method: 'POST', headers: hdr(k), body: JSON.stringify({ token: k }) })).json();
    return s.ips || [];
  } catch(e){ return []; }
}
async function saveIps(k, list){
  await fetch(SRV + '/admin/ips/set', { method: 'POST', headers: hdr(k),
    body: JSON.stringify({ token: k, ips: list }) });
}

</script>
</body>
</html>
`;
// Embedded staff UI (tyxen-admin/admin-ui/index.html) - served at GET /admin.

// ---- password hashing: scrypt, stdlib only, no secret in source ---------
// The old check was a single SHA-256 round over a fixed salt that was itself
// committed to a public repo, so it was offline-crackable from the source.
// scrypt is memory-hard and the salt now travels inside the stored key.
const SCRYPT = { N: 16384, r: 8, p: 1, keylen: 32 };
function makeAdminKey(password) {
  const salt = crypto.randomBytes(16);
  const hash = crypto.scryptSync(String(password), salt, SCRYPT.keylen, SCRYPT);
  return `scrypt$${SCRYPT.N}$${SCRYPT.r}$${SCRYPT.p}$${salt.toString('base64')}$${hash.toString('base64')}`;
}
function checkAdminKey(key, password) {
  const parts = String(key || '').split('$');
  if (parts.length !== 6 || parts[0] !== 'scrypt') return false;
  const N = Number(parts[1]);
  const r = Number(parts[2]);
  const p = Number(parts[3]);
  if (!Number.isInteger(N) || !Number.isInteger(r) || !Number.isInteger(p)) return false;
  const salt = Buffer.from(parts[4], 'base64');
  const want = Buffer.from(parts[5], 'base64');
  if (!salt.length || !want.length) return false;
  let got;
  try {
    got = crypto.scryptSync(String(password == null ? '' : password), salt, want.length,
      { N, r, p, maxmem: 256 * 1024 * 1024 });
  } catch {
    return false;
  }
  return got.length === want.length && crypto.timingSafeEqual(got, want);
}
function checkPassword(pw) {
  if (!ADMIN_USER || !ADMIN_KEY) return false;
  return checkAdminKey(ADMIN_KEY, pw);
}

// ---- caller identity ----------------------------------------------------
// The old clientIp() took x-forwarded-for[0], which is text the caller writes
// itself. That single header picked who passed the admin IP allowlist AND which
// bucket the brute-force ban was counted against, so both were bypassable.
// By default we use only the TCP peer address, which cannot be forged. Set
// TRUST_PROXY_HOPS=1 behind one rewriting proxy (nginx/Caddy), 2 behind
// CDN + proxy, etc.
const TRUST_PROXY_HOPS = (() => {
  const n = Number.parseInt(process.env.TRUST_PROXY_HOPS || '0', 10);
  return Number.isFinite(n) && n > 0 ? n : 0;
})();
function normalizeIp(value) {
  const s = String(value || '').trim();
  if (s.startsWith('::ffff:')) return s.slice(7);
  if (s === '::1') return '127.0.0.1';
  return s;
}
function clientIp(req) {
  const peer = normalizeIp(req.socket && req.socket.remoteAddress);
  if (TRUST_PROXY_HOPS === 0) return peer || 'unknown';
  const chain = String(req.headers['x-forwarded-for'] || '')
    .split(',').map((x) => normalizeIp(x)).filter(Boolean);
  if (!chain.length) return peer || 'unknown';
  // Count back from the RIGHT: only the entries our own proxies appended are
  // trustworthy, and anything to their left is attacker-controlled.
  const idx = chain.length - TRUST_PROXY_HOPS;
  return (idx >= 0 ? chain[idx] : chain[chain.length - 1]) || peer || 'unknown';
}

// ---- sessions -----------------------------------------------------------
const SESSIONS = new Map();
const SESSION_TTL = Number(process.env.SESSION_TTL_MS || 8 * 3600 * 1000);
function adminToken(req, body) {
  const h = req.headers['x-admin-token'];
  if (typeof h === 'string' && h) return h;
  return (body && body.token) || '';
}
function authed(req, body) {
  const tok = adminToken(req, body);
  if (!tok) return false;
  const s = SESSIONS.get(tok);
  if (!s) return false;
  if (Date.now() - s.last > SESSION_TTL) {
    SESSIONS.delete(tok);
    return false;
  }
  s.last = Date.now();
  return true;
}
// Sliding-window tokens are swept rather than lingering in the map until the
// process restarts.
setInterval(() => {
  const now = Date.now();
  for (const [tok, s] of SESSIONS) {
    if (now - s.last > SESSION_TTL) SESSIONS.delete(tok);
  }
}, 5 * 60 * 1000).unref();

function allowedIp(req) {
  try {
    const list = getDb().allowedIps || [];
    if (!list.length) return true;
    const ip = clientIp(req);
    return list.some((x) => normalizeIp(x) === ip);
  } catch {
    // A failed read used to return true — an unreadable db.json opened the
    // panel instead of closing it.
    return false;
  }
}
function needAuth(req, res, body) {
  if (!allowedIp(req)) {
    sendText(res, 'Denied.', 403);
    return true;
  }
  if (adminDenied(req, res)) return true;
  if (!authed(req, body)) {
    // Only a *presented* token counts toward the ban. Counting anonymous hits
    // would let anyone lock a real admin out by spamming /admin unauthenticated.
    if (adminToken(req, body)) adminFail(req);
    sendText(res, 'Denied.', 403);
    return true;
  }
  return false;
}
// Same gate judged from headers alone, so a request can be refused before its
// body is read into memory. Used by /admin/upload.
function needHeaderAuth(req, res) {
  if (!allowedIp(req)) {
    sendText(res, 'Denied.', 403);
    return true;
  }
  if (adminDenied(req, res)) return true;
  if (!authed(req, null)) {
    if (adminToken(req, null)) adminFail(req);
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
// One process owns db.json, so it is read once and kept hot. Previously every
// request — including 404s and static jar downloads — did readFileSync plus
// JSON.parse on the event loop, and a route that awaited between its read and
// its write could drop another caller's update.
let DB_CACHE = null;
function getDb() {
  if (!DB_CACHE) DB_CACHE = loadDb();
  return DB_CACHE;
}
function saveDb(db) {
  DB_CACHE = db;
  // Atomic write, with a per-writer temp name: a fixed '.tmp' let two writers
  // rename over each other.
  const tmp = DB_FILE + '.' + process.pid + '.' + crypto.randomBytes(6).toString('hex') + '.tmp';
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
// The online map used to only ever gain keys: a name that stopped pinging stayed
// in db.json forever. Entries older than an hour are dropped on the next write.
function pruneOnline(db) {
  const now = Date.now();
  const map = db.online || {};
  for (const u of Object.keys(map)) {
    if (now - Number(map[u] || 0) > ONLINE_TTL_MS * 12) delete map[u];
  }
}
// /api/tyxen/ping is called by every player's mod and cannot require a login,
// so it is throttled per caller+name. A throttled ping still answers ok — the
// mod cannot tell the difference — but it costs no disk write.
const PING_MIN_INTERVAL = Number(process.env.PING_MIN_INTERVAL_MS || 30 * 1000);
const PING_SEEN = new Map();
function pingThrottled(ip, username) {
  const now = Date.now();
  const key = ip + '|' + username;
  const last = PING_SEEN.get(key);
  if (last !== undefined && now - last < PING_MIN_INTERVAL) return true;
  PING_SEEN.set(key, now);
  if (PING_SEEN.size > 20000) {
    for (const [k, t] of PING_SEEN) {
      if (now - t > PING_MIN_INTERVAL) PING_SEEN.delete(k);
    }
    if (PING_SEEN.size > 20000) PING_SEEN.clear();
  }
  return false;
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
// A jar is ~8 MB, so a base64 body needs well under the old 64 MB ceiling.
const MAX_JSON_BODY = Number(process.env.MAX_UPLOAD_BODY_BYTES || 32 * 1024 * 1024);
const FAILS = new Map();
const MAX_FAIL_TRACKS = 5000;
const FAIL_GRACE = Number(process.env.ADMIN_FAIL_GRACE || 5);
// FAILS was keyed on the spoofable header and never pruned, so anyone could
// grow it without bound using fresh X-Forwarded-For values each request.
function sweepFails() {
  const now = Date.now();
  for (const [ip, f] of FAILS) {
    if (f.until < now) FAILS.delete(ip);
  }
  if (FAILS.size > MAX_FAIL_TRACKS) FAILS.clear();
}
function adminDenied(req, res) {
  const ip = clientIp(req);
  const f = FAILS.get(ip);
  if (f && f.until > Date.now()) {
    sendText(res, 'Slow down.', 429);
    return true;
  }
  return false;
}
function adminFail(req) {
  const ip = clientIp(req);
  const f = FAILS.get(ip) || { count: 0, until: 0 };
  f.count += 1;
  // One typo used to cost a 30 second lockout. Free attempts up to a small
  // grace, then escalating lockout — brute force is still stopped, and it was
  // already stopped by the strong password, not by this counter.
  f.until = f.count >= FAIL_GRACE ? Date.now() + Math.min(600000, f.count * 30000) : 0;
  FAILS.set(ip, f);
  if (FAILS.size > MAX_FAIL_TRACKS) sweepFails();
}
setInterval(sweepFails, 10 * 60 * 1000).unref();
function applySecurityHeaders(res) {
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('Referrer-Policy', 'no-referrer');
  res.setHeader('Cache-Control', 'no-store');
}
function sendJson(res, obj, code = 200) {
  const body = JSON.stringify(obj);
  applySecurityHeaders(res);
  res.writeHead(code, { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) });
  res.end(body);
}
function sendText(res, text, code = 200, type = 'text/plain') {
  applySecurityHeaders(res);
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

  // NOTE: the cosmetics store (catalog/coins/equip) was removed — every
  // /api/store/* route now answers 410 Gone so old mod menus fail visibly
  // instead of hanging.
  if (url.pathname.startsWith('/api/store/')) {
    return sendJson(res, { error: 'Store removed.' }, 410);
  }

  // ---- tyxen user ping (nametag badge list + online tracking) ----
  if (url.pathname === '/api/tyxen/ping' && req.method === 'POST') {
    const body = await readBody(req);
    if (!validName(body.username)) {
      return sendJson(res, { error: 'Invalid username.' }, 400);
    }
    const username = String(body.username).toLowerCase();
    // Anyone on the internet could POST this forever; previously each call
    // rewrote two files and added a permanent entry. Throttled pings still get
    // {ok:true} so no client behaviour changes, but touch no disk.
    if (!pingThrottled(clientIp(req), username)) {
      const db = getDb();
      ensureUser(db, username);
      db.online = db.online || {};
      db.online[username] = Date.now();
      pruneOnline(db);
      saveDb(db);
      const list = loadUsers();
      if (!list.includes(username)) {
        list.push(username);
        saveUsers(list);
      }
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
    // The panel uses inline script/styles, so 'unsafe-inline' stays for those;
    // everything else is locked to this origin and framing is refused outright.
    res.setHeader('Content-Security-Policy',
      "default-src 'self'; script-src 'unsafe-inline'; style-src 'unsafe-inline'; "
      + "img-src 'self' data:; connect-src 'self'; font-src 'self'; object-src 'none'; "
      + "base-uri 'none'; form-action 'none'; frame-ancestors 'none'");
    return sendText(res, ADMIN_HTML, 200, 'text/html');
  }
  if (url.pathname === '/admin/login' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (!ADMIN_USER || !ADMIN_KEY) {
      // Nothing configured: refuse, and never fall back to a built-in secret.
      return sendText(res, 'Admin login is not configured on this server.', 503);
    }
    if (!body || body.username !== ADMIN_USER || !checkPassword(body.password)) {
      adminFail(req);
      return sendJson(res, { ok: false });
    }
    const tok = crypto.randomBytes(32).toString('hex');
    SESSIONS.set(tok, { created: Date.now(), last: Date.now() });
    return sendJson(res, { ok: true, token: tok });
  }
  if (url.pathname === '/admin/logout' && req.method === 'POST') {
    const body = await readBody(req);
    if (body && body.token) SESSIONS.delete(body.token);
    return sendJson(res, { ok: true });
  }
  // Drop every live token at once. This is the escape hatch for a stolen or
  // leaked session: before it, a leaked token stayed valid for its whole TTL
  // even after the password was rotated, because sessions lived only in memory.
  if (url.pathname === '/admin/revoke' && req.method === 'POST') {
    const body = await readBody(req);
    if (needAuth(req, res, body)) return;
    const n = SESSIONS.size;
    SESSIONS.clear();
    return sendJson(res, { ok: true, revoked: n });
  }
  if (url.pathname === '/admin/stats' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (needAuth(req, res, body)) return;
    const db5 = getDb();
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
    // Authenticated from headers BEFORE the body is read. This is the route
    // that pushes a jar to every installed client, and it used to accept a 64 MB
    // body from an anonymous caller first and check the token afterwards —
    // enough to exhaust memory with a handful of concurrent requests.
    if (needHeaderAuth(req, res)) return;
    const body = await readBody(req, MAX_JSON_BODY);
    if (body === null) {
      return sendText(res, 'Too big.', 413);
    }
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
    const jar = Buffer.from(String(body.data), 'base64');
    // A .jar is a zip: refuse anything that does not start with PK, so garbage
    // or an arbitrary blob cannot be published as the client update.
    if (jar.length < 4 || jar[0] !== 0x50 || jar[1] !== 0x4b) {
      return sendText(res, 'Data is not a valid .jar (zip) file.', 400);
    }
    const modDir = path.join(FILES, 'mods');
    if (!fs.existsSync(modDir)) fs.mkdirSync(modDir, { recursive: true });
    fs.writeFileSync(path.join(modDir, safe), jar);
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
    const db6 = getDb();
    return sendJson(res, { ips: db6.allowedIps || [], mine: clientIp(req) });
  }
  if (url.pathname === '/admin/ips/set' && req.method === 'POST') {
    const body = await readBody(req);
    if (needAuth(req, res, body)) return;
    const list = Array.isArray(body.ips) ? body.ips.filter((x) => typeof x === 'string' && x.length < 64).slice(0, 50) : [];
    const db7 = getDb();
    db7.allowedIps = list;
    saveDb(db7);
    return sendJson(res, { ok: true, ips: list });
  }
  // ---- announcements (launcher left section) ----
  if (url.pathname === '/api/tyxen/announcements' && req.method === 'GET') {
    const db2 = getDb();
    return sendJson(res, { announcements: db2.announcements || [] });
  }
  if (url.pathname === '/admin/announce' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (needAuth(req, res, body)) return;
    if (!body.title) {
      return sendText(res, 'Title required.', 400);
    }
    const db3 = getDb();
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
    const db4 = getDb();
    const delId = Number(body.id);
    db4.announcements = (db4.announcements || []).filter((a) => a.id !== delId);
    saveDb(db4);
    return sendJson(res, { ok: true });
  }

  // ---- online counter: kitne online / kitne total ----
  if (url.pathname === '/api/tyxen/online' && req.method === 'GET') {
    const db = getDb();
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
    return sendText(res, liveUsers(getDb()).join('\n') + '\n');
  }
  if (url.pathname === '/tyxen/players.md5' && req.method === 'GET') {
    const hash = crypto.createHash('md5').update(JSON.stringify(liveUsers(getDb()))).digest('hex');
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

// ---- key generator: node server.cjs --make-key --------------------------
// Reads the secret from ADMIN_NEW_PASSWORD so it never lands in argv or shell
// history, and prints the ADMIN_KEY line to paste into the environment.
if (process.argv.includes('--make-key')) {
  const pw = process.env.ADMIN_NEW_PASSWORD || '';
  if (pw.length < 12) {
    console.error('Set ADMIN_NEW_PASSWORD first — 12+ characters, not reused anywhere.');
    process.exit(1);
  }
  console.log('Copy these into your environment (see .env.example), then restart:');
  console.log('ADMIN_USER=' + (process.env.ADMIN_USER || '<pick a login name>'));
  console.log('ADMIN_KEY=' + makeAdminKey(pw));
  process.exit(0);
}

server.listen(PORT, () => {
  console.log(`TYXEN backend chal raha hai: http://localhost:${PORT}`);
  console.log('Badges, updates, announcements — sab yahin se.');
  if (!ADMIN_USER || !ADMIN_KEY) {
    console.warn('[warn] ADMIN_USER / ADMIN_KEY are not set — /admin login stays disabled until they are.');
  }
  if (TRUST_PROXY_HOPS === 0) {
    console.warn('[warn] TRUST_PROXY_HOPS=0: callers are identified by TCP address. Set it to your real '
      + 'proxy hop count if nginx/Caddy/Cloudflare fronts this server, or the allowlist will key on the proxy.');
  }
  try {
    if (!(getDb().allowedIps || []).length) {
      console.warn('[warn] Admin IP allowlist is empty — /admin is reachable from anywhere (login still required).');
    }
  } catch {}
});
