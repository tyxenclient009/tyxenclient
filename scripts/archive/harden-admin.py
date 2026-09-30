import re

p = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-server\server.cjs'
t = open(p, encoding='utf-8').read()

start = t.find("  if (url.pathname === '/admin/verify' && req.method === 'GET') {")
end = t.find("  // ---- announcements (launcher left section) ----")
assert start > 0 and end > start, 'bounds not found'

new_block = '''  if (url.pathname === '/admin/verify' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (!keyOk(body && body.key)) { adminFail(req); return sendJson(res, { ok: false }); }
    return sendJson(res, { ok: true });
  }
  if (url.pathname === '/admin/stats' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (!keyOk(body && body.key)) { adminFail(req); return sendJson(res, { error: 'denied' }); }
    const db5 = loadDb();
    const now = Date.now();
    const onlineMap = db5.online || {};
    const online = Object.values(onlineMap).filter((t) => now - t < 5 * 60 * 1000).length;
    const total = Object.keys(db5.users || {}).length;
    return sendJson(res, {
      users: loadUsers(),
      online: { online, total },
      capes: (db5.capes || [{ id: 'cat' }]).length,
      announcements: db5.announcements || []
    });
  }
  if (url.pathname === '/admin/users' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (!keyOk(body && body.key)) { adminFail(req); return sendText(res, 'Denied.', 403); }
    return sendJson(res, { users: loadUsers() });
  }
  if (url.pathname === '/admin/users/add' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (!keyOk(body && body.key)) { adminFail(req); return sendText(res, 'Denied.', 403); }
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
    if (!keyOk(body && body.key)) { adminFail(req); return sendText(res, 'Denied.', 403); }
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
    const urlKey = url.searchParams.get('key');
    const body = await readBody(req, MAX_JSON_BODY);
    if (body === null) {
      return sendText(res, 'Too big.', 413);
    }
    const provided = (body && body.key) || urlKey;
    if (!keyOk(provided)) { adminFail(req); return sendText(res, 'Denied.', 403); }
    if (!body.filename || !body.version || !body.data) {
      return sendText(res, 'filename + version + data required.', 400);
    }
    if (!/^\\d+\\.\\d+\\.\\d+$/.test(String(body.version))) {
      return sendText(res, 'Version must look like 1.0.72.', 400);
    }
    const safe = path.basename(String(body.filename));
    if (!/^tyxen-\\d+\\.\\d+\\.\\d+\\.jar$/.test(safe)) {
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
'''
t = t[:start] + new_block + t[end:]
open(p, 'w', encoding='utf-8').write(t)
print('admin endpoints hardened')
