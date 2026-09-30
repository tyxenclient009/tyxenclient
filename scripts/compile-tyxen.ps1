# Recompiles changed tyxen-src files with JDK 21 and patches them into the
# shipped Tyxen jar (repack via scripts/rebuild_tyxen.py afterwards for a
# fully validated jar — never ship the in-place patched file directly).
# Usage: .\compile-tyxen.ps1 [-InstanceId <id>] [-Jar <path-to-tyxen.jar>]
param(
  [string]$InstanceId = "",
  [string]$Jar = "",
  [switch]$CompileOnly
)
$ErrorActionPreference = "Stop"
$Repo = "C:\Users\Administrator\Desktop\TYX-CLIENT"
$Javac = "C:\tools\jdk-21\bin\javac.exe"

if ($InstanceId -eq "") {
  $InstanceId = Get-ChildItem "$env:APPDATA\com.tyx.launcher\tyx\instances" -Directory |
    Where-Object { Test-Path "$($_.FullName)\.fabric\remappedJars" } |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1 -ExpandProperty Name
}
$Inst = "$env:APPDATA\com.tyx.launcher\tyx\instances\$InstanceId"
$Client = Get-ChildItem "$Inst\.fabric\remappedJars" -Recurse -Filter "client-intermediary.jar" |
  Select-Object -First 1 -ExpandProperty FullName
if (-not $Client) { throw "no remapped client jar in $InstanceId" }
if ($Jar -eq "" -or -not (Test-Path $Jar)) {
  $Jar = Get-ChildItem "$Repo\public\tyxen\tyxen-*.jar" |
    Sort-Object Name -Descending | Select-Object -First 1 -ExpandProperty FullName
}
if (-not $Jar -or -not (Test-Path $Jar)) { throw "no tyxen jar found for classpath" }
Write-Host "mod jar for classpath: $Jar"

$cp = @($Client, $Jar)
$cp += Get-ChildItem "$Inst\libraries" -Recurse -Filter "*.jar" | ForEach-Object { $_.FullName }
$cp += Get-ChildItem "$Repo\tyxen-libs" -Filter "*.jar" | ForEach-Object { $_.FullName }
# fabric-loader (annotations) may live outside instance libs; grab from gradle cache as fallback
$cp += Get-ChildItem "$env:USERPROFILE\.gradle\caches\modules-2" -Recurse -Filter "fabric-loader-*.jar" -ErrorAction SilentlyContinue |
  Select-Object -First 2 -ExpandProperty FullName
$cp = $cp | Sort-Object -Unique
Write-Host "classpath jars: $($cp.Count)"

$Out = (New-Item -ItemType Directory -Force (Join-Path ([System.IO.Path]::GetTempPath()) "tyxen-classes")).FullName
Remove-Item -Recurse -Force (Join-Path $Out "*") -ErrorAction SilentlyContinue

$Src = "$Repo\tyxen-src\net\tyxen\hud"
$files = @(
  "$Src\launcher\LauncherRenderer.java",
  "$Src\core\ModuleManager.java",
  "$Src\render\Theme.java",
  "$Src\render\AnimationUtils.java",
  "$Src\gui\TyxenUI.java",
  "$Src\gui\components\ColorPicker.java",
  "$Src\gui\components\Dropdown.java",
  "$Src\gui\components\KeybindButton.java",
  "$Src\gui\components\Slider.java",
  "$Src\gui\components\TextInput.java",
  "$Src\gui\components\ToggleButton.java",
  "$Src\gui\widgets\CategoryButton.java",
  "$Src\gui\widgets\ModuleCard.java",
  "$Src\mixin\client\PauseScreenMixin.java",
  "$Src\mixin\client\TitleScreenMixin.java",
  "$Src\mixin\client\TyxenSelfNametagMixin.java",
  "$Src\mixin\client\EntityRendererMixin.java",
  "$Src\mixin\client\PlayerTabOverlayMixin.java",
  "$Src\mixin\client\ItemEntityRendererMixin.java",
  "$Src\render\MotionBlurShaderManager.java",
  "$Src\modules\impl\render\ItemPhysicsModule.java",
  "$Src\gui\screens\HudOverlayScreen.java",
  "$Src\gui\screens\HudEditorScreen.java",
  "$Src\gui\screens\ModuleConfigScreen.java",
  "$Src\gui\screens\StoreScreen.java",
  "$Src\gui\screens\WaypointsScreen.java",
  "$Src\gui\screens\ClickGUIScreen.java",
  "$Src\gui\screens\ScreenshotGalleryScreen.java",
  "$Src\modules\impl\hud\FPSModule.java",
  "$Src\modules\impl\hud\Keystrokes.java",
  "$Src\modules\impl\hud\ArmorHUD.java",
  "$Src\modules\impl\hud\ComboCounter.java",
  "$Src\modules\impl\hud\PingDisplay.java",
  "$Src\modules\impl\hud\ServerInfoHUD.java",
  "$Src\modules\impl\hud\PotionHUD.java",
  "$Src\modules\impl\hud\PackDisplay.java",
  "$Src\modules\impl\hud\CoordinatesModule.java",
  "$Src\modules\impl\render\ScoreboardMod.java",
  "$Src\modules\impl\render\DamageIndicator.java",
  "$Src\modules\impl\utility\ToolWarning.java",
  "$Src\modules\impl\utility\Notifications.java",
  "$Src\modules\impl\utility\ZoomModule.java",
  "$Src\modules\impl\player\ToggleSneak.java",
  "$Src\modules\impl\movement\SprintModule.java"
)
& $Javac -encoding UTF-8 -nowarn -proc:none -cp ($cp -join ";") -d $Out $files
if ($LASTEXITCODE -ne 0) { throw "javac failed" }
$new = Get-ChildItem $Out -Recurse -Filter "*.class"
Write-Host "compiled classes: $($new.Count)"
# Canonical base (resolves 8.3 vs long-name mismatch) for exact entry names.
$base = (Get-Item $Out).FullName
$rel = @{}
foreach ($c in $new) {
  $r = $c.FullName.Substring($base.Length + 1).Replace("\", "/")
  if (-not $r.StartsWith("net/tyxen/")) { throw "unexpected output path: $r" }
  $rel[$c.FullName] = $r
  Write-Host "  $r"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
if ($CompileOnly) {
  Write-Host "compile-only: classes in $Out, jar untouched"
  return
}
$zip = [System.IO.Compression.ZipFile]::Open($Jar, "Update")
try {
  foreach ($c in $new) {
    $relName = $rel[$c.FullName]
    $old = $zip.Entries | Where-Object { $_.FullName -eq $relName }
    if ($old) { $old.Delete() }
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $c.FullName, $relName, "Optimal") | Out-Null
    Write-Host "patched: $relName"
  }
} finally { $zip.Dispose() }

# Safety: mod jars must never shadow minecraft classes.
Add-Type -AssemblyName System.IO.Compression
$check = [System.IO.Compression.ZipFile]::OpenRead($Jar)
$bad = @($check.Entries | Where-Object { $_.FullName.StartsWith("net/minecraft/") } | ForEach-Object { $_.FullName })
$check.Dispose()
if ($bad.Count -gt 0) { throw "SHADOW CLASSES PRESENT: $($bad -join ', ')" }
Write-Host "no net/minecraft shadows. jar bytes: $((Get-Item $Jar).Length)"
