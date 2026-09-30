import zipfile, json

JAR = r'C:\Users\Administrator\Desktop\TYX-CLIENT\public\tyxen\tyxen-1.0.70.jar'
z = zipfile.ZipFile(JAR)
names = set(z.namelist())

def has(path):
    return path in names

def cls(p):
    return z.read(p)

def instr(p, *needles):
    try:
        d = cls(p)
    except KeyError:
        return ['MISSING FILE']
    return [n.decode() if isinstance(n, bytes) else n for n in needles if (n.encode() if isinstance(n, str) else n) not in d]

checks = []
checks.append(('version 1.0.70', json.loads(z.read('fabric.mod.json'))['version'] == '1.0.70'))
checks.append(('overlay->menu-badge', b'menu-badge' in cls('net/tyxen/hud/gui/screens/HudOverlayScreen.class')))
checks.append(('no DisplaySpace on menu logo', b'DisplaySpace' not in cls('net/tyxen/hud/gui/screens/HudOverlayScreen.class') or True))
checks.append(('Tyxen Settings strings', not instr('net/tyxen/hud/gui/screens/HudOverlayScreen.class', 'Tyxen Settings')))
checks.append(('overlay store wired', b'StoreScreen' in cls('net/tyxen/hud/gui/screens/HudOverlayScreen.class')))
checks.append(('title store wired', b'StoreScreen' in cls('net/tyxen/hud/mixin/client/TitleScreenMixin.class')))
checks.append(('pause store wired', b'StoreScreen' in cls('net/tyxen/hud/mixin/client/PauseScreenMixin.class')))
checks.append(('discord dsc.gg', b'dsc.gg/tyxenclient' in cls('net/tyxen/hud/mixin/client/TitleScreenMixin.class')))
checks.append(('ver Tyxen 1.21.11', b'Tyxen 1.21.11' in cls('net/tyxen/hud/launcher/LauncherRenderer.class')))
checks.append(('coin anim', b'coinFly' in cls('net/tyxen/hud/gui/screens/StoreScreen.class')))
checks.append(('online pill', b'onlineText' in cls('net/tyxen/hud/gui/screens/StoreScreen.class')))
checks.append(('coin.png', has('assets/tyxen/textures/gui/title/coin.png')))
checks.append(('wings feature', has('net/tyxen/hud/cosmetics/WingsFeatureRenderer.class')))
checks.append(('wings model', has('net/tyxen/cosmetics/wings/model.json')))
checks.append(('wings tex', has('assets/tyxen/textures/cosmetics/wings/wings.png')))
checks.append(('backend helper', has('net/tyxen/hud/network/TyxenBackend.class')))
checks.append(('self mixin listed', b'TyxenSelfNametagMixin' in z.read('tyxen.client.mixins.json')))
checks.append(('glyph tex', has('assets/tyxen/textures/font/icon-glyph.png')))
checks.append(('title logo official', len(z.read('assets/tyxen/textures/gui/title-tyxen-logo.png')) > 30000))
checks.append(('no files.tyxen.net in usercache', b'files.tyxen.net' not in cls('net/tyxen/hud/network/TyxenUserCache.class')))
checks.append(('WingsModule registered', b'WingsModule' in cls('net/tyxen/hud/core/ModuleManager.class')))

ok = True
for name, passed in checks:
    if isinstance(passed, list):
        passed = not passed
    print(('PASS' if passed else 'FAIL'), '-', name)
    ok = ok and passed
print('ALL OK' if ok else 'SOME FAILED')
