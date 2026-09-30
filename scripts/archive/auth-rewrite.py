import re

p = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-server\server.cjs'
t = open(p, encoding='utf-8').read()

# 1) replace key const + helpers with session auth
old_auth = """const ADMIN_KEY = 'new-year-new-client-2027';"""
new_auth = """const ADMIN_USER = 'SatyamSen';
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
}"""
assert old_auth in t
t = t.replace(old_auth, new_auth, 1)

# 2) verify -> login
old_verify = """  if (url.pathname === '/admin/verify' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (!keyOk(body && body.key)) { adminFail(req); return sendJson(res, { ok: false }); }
    return sendJson(res, { ok: true });
  }"""
new_verify = """  if (url.pathname === '/admin/login' && req.method === 'POST') {
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
  }"""
assert old_verify in t
t = t.replace(old_verify, new_verify, 1)

# 3) swapRemaining key checks to needAuth
patterns = [
    ("if (!keyOk(body && body.key)) { adminFail(req); return sendJson(res, { error: 'denied' }); }",
     "if (needAuth(req, res, body)) return;"),
    ("if (!keyOk(body && body.key)) { adminFail(req); return sendText(res, 'Denied.', 403); }",
     "if (needAuth(req, res, body)) return;"),
    ("if (!keyOk(provided)) { adminFail(req); return sendText(res, 'Denied.', 403); }",
     "if (needAuth(req, res, body)) return;"),
]
for a, b in patterns:
    assert a in t, a[:60]
    t = t.replace(a, b)

# 4) IP allowlist management endpoints (insert before announcements section)
anchor = "  // ---- announcements (launcher left section) ----"
addition = """  if (url.pathname === '/admin/ips' && req.method === 'POST') {
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
"""
assert anchor in t
t = t.replace(anchor, addition + anchor, 1)

# 5) gate the /admin HTML page itself by IP (no info leak to strangers)
old_page = """  if (url.pathname === '/admin' && req.method === 'GET') {
    return sendText(res, ADMIN_HTML, 200, 'text/html');
  }"""
new_page = """  if (url.pathname === '/admin' && req.method === 'GET') {
    if (!allowedIp(req)) {
      return sendText(res, 'Not found.', 404);
    }
    return sendText(res, ADMIN_HTML, 200, 'text/html');
  }"""
assert old_page in t
t = t.replace(old_page, new_page, 1)

open(p, 'w', encoding='utf-8').write(t)
print('auth system replaced')
