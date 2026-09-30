"""FastClient HUD -> Tyxen rebrand. Decompiled sources + resources to tyxen-src/tyxen-res."""
import os, shutil, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
SRC_JAR = r'c:\Users\Administrator\Downloads\fastclient-hud-1.21.11-latest (1).jar'
DECOMP = os.path.join(HERE, 'fastclient-decomp', 'net')
OUT_SRC = os.path.join(HERE, 'tyxen-src')
OUT_RES = os.path.join(HERE, 'tyxen-res')
STUBS = os.path.join(HERE, 'tyxen-stubs')

# longest-first ordered replacements
REPS = [
    ('net/fastclient/hud', 'net/tyxen/hud'),
    ('net.fastclient.hud', 'net.tyxen.hud'),
    ('FastClientHUDClient', 'TyxenHUDClient'),
    ('FastClientHUD', 'TyxenHUD'),
    ('FastClientUserCache', 'TyxenUserCache'),
    ('fastclient-hud', 'tyxen'),
    ('FASTCLIENT', 'TYXEN'),
    ('FastClient', 'Tyxen'),
    ('fastclient', 'tyxen'),
]

def rebrand_text(t):
    for a, b in REPS:
        t = t.replace(a, b)
    return t

def rebrand_path(p):
    for a, b in REPS:
        p = p.replace(a, b)
    return p

# ---- java sources ----
if os.path.exists(OUT_SRC):
    shutil.rmtree(OUT_SRC)
count = 0
for root, _d, files in os.walk(DECOMP):
    for f in files:
        if not f.endswith('.java'):
            continue
        src = os.path.join(root, f)
        rel = os.path.relpath(src, os.path.join(HERE, 'fastclient-decomp'))
        dst = os.path.join(OUT_SRC, rebrand_path(rel))
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        with open(src, encoding='utf-8') as fh:
            t = fh.read()
        with open(dst, 'w', encoding='utf-8') as fh:
            fh.write(rebrand_text(t))
        count += 1
print('java files rebranded:', count)

# ---- mixinextras compile stubs (NOT packed into jar) ----
if os.path.exists(STUBS):
    shutil.rmtree(STUBS)
os.makedirs(os.path.join(STUBS, 'com', 'llamalad7', 'mixinextras', 'injector', 'wrapoperation'))
with open(os.path.join(STUBS, 'com', 'llamalad7', 'mixinextras', 'injector', 'ModifyReturnValue.java'), 'w') as fh:
    fh.write('package com.llamalad7.mixinextras.injector;\n'
             'import java.lang.annotation.*;\n'
             'import org.spongepowered.asm.mixin.injection.At;\n'
             '@Retention(RetentionPolicy.RUNTIME)\n@Target(ElementType.METHOD)\n'
             'public @interface ModifyReturnValue {\n'
             '    String[] method() default {};\n'
             '    At[] at() default {};\n'
             '    boolean require() default true;\n'
             '    int expect() default -1;\n'
             '    int allow() default -1;\n'
             '}\n')
with open(os.path.join(STUBS, 'com', 'llamalad7', 'mixinextras', 'injector', 'wrapoperation', 'Operation.java'), 'w') as fh:
    fh.write('package com.llamalad7.mixinextras.injector.wrapoperation;\n'
             'public interface Operation<T> {\n'
             '    T call(Object... args);\n'
             '}\n')
with open(os.path.join(STUBS, 'com', 'llamalad7', 'mixinextras', 'injector', 'wrapoperation', 'WrapOperation.java'), 'w') as fh:
    fh.write('package com.llamalad7.mixinextras.injector.wrapoperation;\n'
             'import java.lang.annotation.*;\n'
             'import org.spongepowered.asm.mixin.injection.At;\n'
             '@Retention(RetentionPolicy.RUNTIME)\n@Target(ElementType.METHOD)\n'
             'public @interface WrapOperation {\n'
             '    String[] method() default {};\n'
             '    At[] at() default {};\n'
             '    boolean require() default true;\n'
             '    int expect() default -1;\n'
             '    int allow() default -1;\n'
             '}\n')
os.makedirs(os.path.join(STUBS, 'net', 'minecraft'), exist_ok=True)
with open(os.path.join(STUBS, 'net', 'minecraft', 'class_10151.java'), 'w') as fh:
    fh.write('package net.minecraft;\n'
             'import java.util.Set;\n'
             'public class class_10151 {\n'
             '    public static class class_10170 implements AutoCloseable {\n'
             '        public class_279 method_63523(class_2960 id, Set<class_2960> deps) throws Exception {\n'
             '            return null;\n'
             '        }\n'
             '        @Override\n'
             '        public void close() {\n'
             '        }\n'
             '    }\n'
             '}\n')
print('stubs written')

# ---- resources ----
if os.path.exists(OUT_RES):
    shutil.rmtree(OUT_RES)
z = zipfile.ZipFile(SRC_JAR)
for name in z.namelist():
    if name.endswith('.class') or name.endswith('/') or name.startswith('META-INF'):
        continue
    data = z.read(name)
    if name == 'fabric.mod.json':
        continue  # rewritten below
    if name in ('fastclient-hud.mixins.json', 'fastclient-hud.client.mixins.json'):
        t = data.decode('utf-8').replace('net.fastclient.hud', 'net.tyxen.hud')
        data = t.encode('utf-8')
    elif name.endswith(('.json', '.fsh', '.vsh', '.mcmeta', '.txt')):
        try:
            data = rebrand_text(data.decode('utf-8')).encode('utf-8')
        except UnicodeDecodeError:
            pass
    out = os.path.join(OUT_RES, rebrand_path(name))
    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, 'wb') as fh:
        fh.write(data)

import json as _json
_VER = _json.loads(zipfile.ZipFile(SRC_JAR).read('fabric.mod.json'))['version']
FMJ = '''{
  "schemaVersion": 1,
  "id": "tyxen",
  "version": "''' + _VER + '''",
  "name": "Tyxen",
  "description": "Tyxen client mod with customizable HUD modules and visual enhancements.",
  "authors": ["Tyxen"],
  "license": "All-Rights-Reserved",
  "icon": "assets/tyxen/icon.png",
  "environment": "*",
  "entrypoints": {
    "main": ["net.tyxen.hud.TyxenHUD"],
    "client": ["net.tyxen.hud.TyxenHUDClient"]
  },
  "accessWidener": "tyxen.accesswidener",
  "mixins": [
    "tyxen.mixins.json",
    {
      "config": "tyxen.client.mixins.json",
      "environment": "client"
    }
  ],
  "depends": {
    "fabricloader": ">=0.18.4",
    "minecraft": "~1.21.11",
    "java": ">=21",
    "fabric-api": "*"
  }
}
'''
with open(os.path.join(OUT_RES, 'fabric.mod.json'), 'w', encoding='utf-8') as fh:
    fh.write(FMJ)
print('resources done')
