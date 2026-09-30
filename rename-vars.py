import os

jobs = [
    (r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-src\net\tyxen\hud\launcher\LauncherSkinPreference.java',
     [('fastClientSkinEnabled', 'tyxenSkinEnabled')]),
    (r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-src\net\tyxen\hud\launcher\LauncherRenderer.java',
     [('fastClientEnabled', 'tyxenSkinEnabled')]),
]
for path, reps in jobs:
    t = open(path, encoding='utf-8').read()
    for a, b in reps:
        t = t.replace(a, b)
    open(path, 'w', encoding='utf-8').write(t)
    print('done', os.path.basename(path))
