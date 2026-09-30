import os

d = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-src'
n = 0
for root, _, files in os.walk(d):
    for f in files:
        if not f.endswith('.java'):
            continue
        p = os.path.join(root, f)
        t = open(p, encoding='utf-8').read()
        t2 = t.replace('getLogger((String)"TyxenHUD")', 'getLogger((String)"Tyxen")')
        t2 = t2.replace('"[TyxenHUD] ', '"[Tyxen] ')
        t2 = t2.replace('"TyxenHUD initializing', '"Tyxen initializing')
        t2 = t2.replace('"TyxenHUD initialized', '"Tyxen initialized')
        if t2 != t:
            open(p, 'w', encoding='utf-8').write(t2)
            n += 1
            print(' ', f)
print('files touched:', n)
