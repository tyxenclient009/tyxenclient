p = r'C:\Users\Administrator\Desktop\TYX-CLIENT\tyxen-admin\admin-ui\index.html'
t = open(p, encoding='utf-8').read()
reps = [
    ('Staff tool \u2014 updates + users. Sirf team ke paas rakho.',
     'Staff tool \u2014 updates, users and announcements. Keep it with the team only.'),
    ('(launcher mein dikhenge)', '(shown in the launcher)'),
    ('key galat?', 'wrong key?'),
    ('Server se baat nahi hui', 'Cannot reach server'),
    ('URL/server check karo.', 'Check URL/server.'),
    ('Jar + version chahiye!', 'Jar file + version required!'),
    ('Title likho!', 'Write a title!'),
    ("confirm(n + ' hatao?')", "confirm('Remove ' + n + '?')"),
]
for a, b in reps:
    t = t.replace(a, b)
open(p, 'w', encoding='utf-8').write(t)
print('admin strings fixed')
