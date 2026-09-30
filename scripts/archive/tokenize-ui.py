p = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-admin\admin-ui\index.html'
t = open(p, encoding='utf-8').read()
reps = [
    ("  const k = key();\n  const file = $('f').files[0];",
     "  const k = tok();\n  const file = $('f').files[0];"),
    ("const res = await fetch(SRV + '/admin/upload?key=' + encodeURIComponent(k), {\n      method: 'POST', headers: {'Content-Type': 'application/json'},\n      body: JSON.stringify({ filename: file.name, version: $('v').value, changelog: $('c').value, data: btoa(bin) })",
     "const res = await fetch(SRV + '/admin/upload', {\n      method: 'POST', headers: {'Content-Type': 'application/json'},\n      body: JSON.stringify({ token: k, filename: file.name, version: $('v').value, changelog: $('c').value, data: btoa(bin) })"),
    ("  const k = key();\n  if (!$('at').value)",
     "  const k = tok();\n  if (!$('at').value)"),
    ("body: JSON.stringify({ key: k, title: $('at').value, body: $('ab').value })",
     "body: JSON.stringify({ token: k, title: $('at').value, body: $('ab').value })"),
    ("  const k = key();\n  await fetch(SRV + '/admin/announce/delete'",
     "  const k = tok();\n  await fetch(SRV + '/admin/announce/delete'"),
    ("body: JSON.stringify({ key: k, id })",
     "body: JSON.stringify({ token: k, id })"),
    ("  const k = key();\n  const n = $('u').value.trim();",
     "  const k = tok();\n  const n = $('u').value.trim();"),
    ("body: JSON.stringify({ key, username: n }) });\n  $('u').value = ''; load();",
     "body: JSON.stringify({ token: k, username: n }) });\n  $('u').value = ''; load();"),
    ("  const k = key();\n  if (!confirm('Remove ' + n + '?')) return;",
     "  const k = tok();\n  if (!confirm('Remove ' + n + '?')) return;"),
    ("body: JSON.stringify({ key, username: n }) });\n  load();",
     "body: JSON.stringify({ token: k, username: n }) });\n  load();"),
]
for a, b in reps:
    assert a in t, a[:60]
    t = t.replace(a, b)

ip_js = """
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
"""
t = t.replace('if (key()) {', 'if (tok()) {')
assert 'function tok()' in t
# hook IP list into load(): after users render
old_users = "$('users').innerHTML = u.map(n =>"
assert old_users in t
t = t.replace("    say('');\n  } catch(e){ $('dot').className = 'dot bad';",
              "    loadIpsFromStats();\n    say('');\n  } catch(e){ $('dot').className = 'dot bad';")
t = t.replace('async function push(){', 'async function loadIpsFromStats(){\n  try {\n    const k = tok();\n    const s = await (await fetch(SRV + \'/admin/ips\', { method: \'POST\', headers: {\'Content-Type\': \'application/json\'}, body: JSON.stringify({ token: k }) })).json();\n    loadIps(s.ips || []);\n  } catch(e){}\n}\nasync function push(){')
t = t.replace('if (key()) {', 'if (tok()) {')
t = t.replace('</script>\n</body>\n</html>', ip_js + '\n</script>\n</body>\n</html>', 1)
open(p, 'w', encoding='utf-8').write(t)
print('admin ui tokenized')
