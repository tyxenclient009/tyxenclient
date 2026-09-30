p = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-server\server.cjs'
t = open(p, encoding='utf-8').read()

a = """  if (url.pathname === '/admin/announce' && req.method === 'POST') {
    const body = await readBody(req);
    if (body.key !== ADMIN_KEY || !body.title) {
      return sendText(res, 'key + title chahiye.', 400);
    }"""
b = """  if (url.pathname === '/admin/announce' && req.method === 'POST') {
    const body = await readBody(req);
    if (adminDenied(req, res)) return;
    if (!keyOk(body and body.key if False else None) if False else not keyOk((body or {}).get('key') if isinstance(body, dict) else None)) if False else None:
      pass"""
# NOTE: placeholder replaced below with exact JS below
old_announce = t[t.find("  if (url.pathname === '/admin/announce'"):t.find("  // ---- online counter")]
print('announce block chars:', len(old_announce))
