import hashlib

files = [
    r'C:\Users\Administrator\Desktop\TYX-CLIENT\public\tyxen\tyxen-1.0.68.jar',
    r'C:\Users\Administrator\Desktop\TYX-CLIENT\dist\tyxen\tyxen-1.0.68.jar',
    r'C:\Users\Administrator\AppData\Roaming\com.tyx.launcher\tyx\instances\1-521716\mods\tyxen-1.0.68.jar',
]
for f in files:
    d = open(f, 'rb').read()
    print(hashlib.sha1(d).hexdigest()[:12], len(d), f.split('\\')[-2] + '/' + f.split('\\')[-1])
