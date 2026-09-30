import re

p = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-server\server.cjs'
t = open(p, encoding='utf-8').read()
BT = chr(96)
pat = 'const ADMIN_HTML = ' + BT + '.*?' + BT + ';\n'
t2, n = re.subn(pat, '', t, flags=re.S)
open(p, 'w', encoding='utf-8').write(t2)
print('removed:', n)
