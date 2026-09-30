p = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-server\server.cjs'
t = open(p, encoding='utf-8').read()

# remove dead keyOk helper
start = t.find('function keyOk(provided) {')
assert start > 0
end = t.find('\n}\n', start) + len('\n}\n')
t = t[:start] + t[end:]

# remove dead ADMIN_KEY const
t = t.replace("const ADMIN_KEY = 'new-year-new-client-2027';\n", '', 1)

# remove dead provided line in upload
t = t.replace('    const provided = (body && body.key) || urlKey;\n', '', 1)

# fix stale comment
t = t.replace('// ---- ADMIN panel (team only, ?key=ADMIN_KEY) ----',
              '// ---- ADMIN panel (login + IP allowlist) ----')

open(p, 'w', encoding='utf-8').write(t)
print('cleaned')
