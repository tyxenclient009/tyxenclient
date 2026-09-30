import zipfile, glob

js = glob.glob(r'C:\Users\Administrator\.gradle\caches\modules-2\files-2.1\net.fabricmc\yarn\1.21.11+build.6\*\*.jar')
js = [j for j in js if not j.endswith('sources.jar')]
print('yarn jar:', js[0])
z = zipfile.ZipFile(js[0])
t = z.read('mappings/mappings.tiny').decode('utf-8')
want = {'method_1498', 'method_31044', 'method_31034', 'method_5756', 'method_5782', 'method_1551', 'method_4055', 'method_5438', 'method_5805'}
for line in t.splitlines():
    parts = line.split('\t')
    if parts[0] == 'METHOD' and len(parts) >= 6 and parts[4] in want:
        print(parts[1], '|', parts[4], '->', parts[5])
