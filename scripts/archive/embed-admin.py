import re

srv = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-server\server.cjs'
ui = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-admin\admin-ui\index.html'
t = open(srv, encoding='utf-8').read()
html = open(ui, encoding='utf-8').read()

start = t.find("if (url.pathname === '/admin' && req.method === 'GET') {")
assert start >= 0, 'admin block not found'
brace = t.find('{', start)
depth = 0
i = brace
while True:
    if t[i] == '{':
        depth += 1
    elif t[i] == '}':
        depth -= 1
        if depth == 0:
            break
    i += 1
new_block = ("if (url.pathname === '/admin' && req.method === 'GET') {\n"
             "    return sendText(res, ADMIN_HTML, 200, 'text/html');\n"
             "  }")
t = t[:start] + new_block + t[i + 1:]

anchor = "const ADMIN_USER = 'SatyamSen';"
assert anchor in t
escaped = html.replace('\\', '\\\\').replace('`', '\\`').replace('$', '\\$')
t = t.replace(anchor, anchor + "\nconst ADMIN_HTML = `" + escaped + "`;", 1)
open(srv, 'w', encoding='utf-8').write(t)
print('admin page embedded, server.cjs bytes:', len(t))
