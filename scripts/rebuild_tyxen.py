"""Rebuild tyxen-1.0.72.jar CLEANLY in one pass (never in-place update).

Base  : tyxen-server 1.0.71 jar (pristine, never patched in place)
Steps : drop net/minecraft shadows, patch mixins JSON (no ShaderManagerAccessor,
        required=false), bump fabric.mod.json to 1.0.72, overlay freshly
        compiled classes, then FULLY validate (testzip + read every entry).
"""
import io
import json
import os
import zipfile

REPO = r'C:\Users\Administrator\Desktop\TYX-CLIENT'
BASE_JAR = os.path.join(REPO, 'tyxen-server', 'public', 'files', 'mods', 'tyxen-1.0.71.jar')
OUT_JAR = os.path.join(REPO, 'public', 'tyxen', 'tyxen-1.0.73.jar')
CLS_DIR = os.path.join(os.environ['TEMP'], 'tyxen-classes')

# 0. base must itself be healthy
base = zipfile.ZipFile(BASE_JAR)
assert base.testzip() is None, 'base jar corrupt?!'
print('base ok:', len(base.namelist()), 'entries')

# 1. fresh classes
new_files = {}
for root, _, files in os.walk(CLS_DIR):
    for f in files:
        if f.endswith('.class'):
            full = os.path.join(root, f)
            rel = os.path.relpath(full, CLS_DIR).replace(os.sep, '/')
            assert rel.startswith('net/tyxen/'), rel
            new_files[rel] = open(full, 'rb').read()
assert new_files, 'no compiled classes found!'
print('fresh classes:', len(new_files))

# 2. assemble
entries = {}
for n in base.namelist():
    if n.startswith('net/minecraft/'):
        print('dropping shadow:', n)
        continue
    if n in new_files:
        continue
    entries[n] = base.read(n)
base.close()

# Mixins config: tyxen-res is source of truth (it lists ItemEntityRendererMixin
# etc.). Enforce the two boot-safety guards programmatically.
res_mix = os.path.join(REPO, 'tyxen-res', 'tyxen.client.mixins.json')
mix = json.loads(open(res_mix, encoding='utf-8').read())
mix['client'] = [m for m in mix['client'] if m != 'ShaderManagerAccessor']
mix['required'] = False
entries['tyxen.client.mixins.json'] = json.dumps(mix, indent=2).encode()
print('mixins from tyxen-res:', len(mix['client']), 'entries')

fm = json.loads(entries['fabric.mod.json'].decode())
fm['version'] = '1.0.73'
entries['fabric.mod.json'] = json.dumps(fm, indent=2).encode()

entries.update(new_files)

tmp = OUT_JAR + '.new'
with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as z:
    for n in sorted(entries):
        z.writestr(n, entries[n])

# 3. FULL validation: headers + decompress every entry
z = zipfile.ZipFile(tmp)
assert z.testzip() is None, 'testzip failed'
names = z.namelist()
for n in names:
    z.read(n)
z.close()
print('validated: all', len(names), 'entries decompress cleanly')
assert not [n for n in names if n.startswith('net/minecraft/')]
assert not [n for n in names if n.startswith('sses/')]
d = [n for n in names if 'ClickGUIScreen.class' == n.split('/')[-1]]
print('ClickGUI present:', bool(d))
os.replace(tmp, OUT_JAR)
print('wrote', OUT_JAR, os.path.getsize(OUT_JAR))
