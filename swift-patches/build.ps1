# SwiftClient TYX patch builder — recompile edited sources, repack the jar, deploy.
# Usage: powershell -ExecutionPolicy Bypass -File build.ps1
#   Optional: -Only "SwiftModsMenu,Ui" (comma list of class simple-names, default = all)
param([string]$Only = "")

$ErrorActionPreference = "Stop"
$ROOT = Split-Path $MyInvocation.MyCommand.Path -Parent
$SRC = Join-Path $ROOT "src"
$OUT = Join-Path $ROOT "out"
$JAR = "C:\Users\Administrator\Desktop\TYX-CLIENT\public\swiftclient\swiftclient-1.0.0-tyx.jar"
$ORIG = "c:\Users\Administrator\Downloads\swiftclient-1.0.0.jar"
$JDK = "C:\tyx-dev\jdk-21.0.12.1+1"
$IJ1 = (Get-ChildItem "C:\Users\Administrator\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-clientonly-intermediary\1.21.11*" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notlike "*.backup" -and $_.Name -notlike "*.pom" } | Select-Object -First 1).FullName
$IJ2 = (Get-ChildItem "C:\Users\Administrator\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-common-intermediary\1.21.11*" -Recurse -Filter "*.jar" | Where-Object { $_.Name -notlike "*.backup" -and $_.Name -notlike "*.pom" } | Select-Object -First 1).FullName
$JOML = (Get-ChildItem "C:\Users\Administrator\.gradle\caches\modules-2\files-2.1\org.joml\joml\1.10.8" -Recurse -Filter "joml-1.10.8.jar" | Select-Object -First 1).FullName
$BRIG = "C:\Users\Administrator\.gradle\caches\modules-2\files-2.1\com.mojang\brigadier\1.3.10\d15b53a14cf20fdcaa98f731af5dda654452c010\brigadier-1.3.10.jar"
$FL = "C:\Users\Administrator\.gradle\caches\modules-2\files-2.1\net.fabricmc\fabric-loader\0.16.14\5778d47f536bf2c63ed2abc1a56f5a1c129e34a\fabric-loader-0.16.14.jar"
$SPONGE = "C:\tyx-dev\sponge\sponge-mixin.jar"

New-Item -ItemType Directory -Path $OUT -Force | Out-Null
$files = Get-ChildItem $SRC -Filter "*.java" | ForEach-Object { $_.FullName }
if ($Only -ne "") {
    $want = $Only.Split(",") | ForEach-Object { $_.Trim() }
    $files = $files | Where-Object {
        $bn = [System.IO.Path]::GetFileNameWithoutExtension($_)
        $want -contains $bn
    }
}
Write-Output "Compiling $($files.Count) files…"
& "$JDK\bin\javac.exe" -source 21 -target 21 -nowarn -proc:none `
    -cp "$OUT;$ORIG;$IJ1;$IJ2;$JOML;$BRIG;$FL;$SPONGE" `
    -d $OUT $files
if ($LASTEXITCODE -ne 0) { throw "compile failed" }

Write-Output "Repacking…"
C:\Users\Administrator\AppData\Local\Programs\Python\Python313\python.exe -c "
import zipfile, os
jar = r'$JAR'
out = r'$OUT'
with zipfile.ZipFile(jar, 'r') as zin:
    data = {info.filename: zin.read(info.filename) for info in zin.infolist()}
count = 0
for root, dirs, fs in os.walk(out):
    for fn in fs:
        if not fn.endswith('.class'): continue
        full = os.path.join(root, fn)
        rel = os.path.relpath(full, out).replace(os.sep, '/')
        with open(full, 'rb') as fh:
            data[rel] = fh.read()
        count += 1
print('staged', count, 'classes')
tmp = jar + '.new'
with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as zout:
    for name, blob in data.items():
        zout.writestr(name, blob)
os.replace(tmp, jar)
print('rebuilt OK, entries:', len(data))
"
$mods = "$env:APPDATA\com.tyx.launcher\tyx\instances\1-521716\mods\swiftclient-1.0.0-tyx.jar"
try {
    Copy-Item $JAR $mods -Force -ErrorAction Stop
    Write-Output "Deployed to instance mods."
} catch {
    Write-Output "Game is running (jar locked) — will apply on next launch."
}
Write-Output "DONE — restart the game to load it (mixins/classes load once at startup)."
