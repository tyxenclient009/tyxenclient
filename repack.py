"""Clean repack: fresh ZipInfo for every entry (no metadata reuse = no corruption)."""
import zipfile
import os
import sys

SRC_JAR = sys.argv[1]
CLASS_DIRS = sys.argv[2].split(';')  # out dirs whose .class files override/add
EXTRA = {}  # filled below via EXTRA_FILES env-ish args is overkill; edit here if needed

zin = zipfile.ZipFile(SRC_JAR, 'r')
entries = {}
for item in zin.infolist():
    if item.filename in entries:
        continue
    # NEVER ship Minecraft classes inside the mod jar — Knot loads mod
    # classes first, so a stale class_10151 (or any net/minecraft class)
    # shadows the real game class and crashes every 1.21.11 boot
    # (NoSuchMethodError + bogus mixin failures). Seen 2026-09-22.
    if item.filename.startswith('net/minecraft/'):
        print('STRIPPED shadow class:', item.filename)
        continue
    entries[item.filename] = zin.read(item.filename)
zin.close()

for d in CLASS_DIRS:
    for root, _, files in os.walk(d):
        for f in files:
            if not f.endswith('.class'):
                continue
            full = os.path.join(root, f)
            rel = os.path.relpath(full, d).replace(os.sep, '/')
            entries[rel] = open(full, 'rb').read()

tmp = SRC_JAR + '.new'
zout = zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED)
for name in sorted(entries.keys()):
    zi = zipfile.ZipInfo(filename=name)
    zi.compress_type = zipfile.ZIP_DEFLATED
    zout.writestr(zi, entries[name])
zout.close()
os.replace(tmp, SRC_JAR)

z2 = zipfile.ZipFile(SRC_JAR)
bad = z2.testzip()
print('entries:', len(z2.namelist()), '| bad:', bad, '| size:', os.path.getsize(SRC_JAR))
