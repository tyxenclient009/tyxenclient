# Tyx Client multi-version builder — one jar per MC version (Lunar-style).
# Usage: run from tyx-client-mod/ :  powershell -ExecutionPolicy Bypass -File build-all.ps1
#   Optional: -Only "1.21.1,1.21.11"  (comma list, builds just those)
param([string]$Only = "")

$ErrorActionPreference = "Stop"
$MOD = "1.6.2"
$JDK21 = "C:\tyx-dev\jdk-21.0.12.1+1"
$JDK25 = "C:\tyx-dev\jdk-25.0.4.1+1"
$G8 = "C:\tyx-dev\gradle-8.14\bin\gradle.bat"
$G9 = "C:\tyx-dev\gradle-9.7.0\bin\gradle.bat"

# mc, yarn, fapi, loom, variant(old1 = 1.21.1 / old = 1.21.2-5 layer-fn GUI / old2 = 1.21.6-8 pipelines / mid hybrid / new manager)
$VERSIONS = @(
    @("1.21.1", "1.21.1+build.3", "0.102.0+1.21.1", "1.11.8", "old1"),
    @("1.21.2", "1.21.2+build.1", "0.106.1+1.21.2", "1.11.8", "old"),
    @("1.21.3", "1.21.3+build.2", "0.114.1+1.21.3", "1.11.8", "old"),
    @("1.21.4", "1.21.4+build.8", "0.119.4+1.21.4", "1.11.8", "old"),
    @("1.21.5", "1.21.5+build.1", "0.128.2+1.21.5", "1.11.8", "old"),
    @("1.21.6", "1.21.6+build.1", "0.128.2+1.21.6", "1.11.8", "old2"),
    @("1.21.7", "1.21.7+build.8", "0.129.0+1.21.7", "1.11.8", "old2"),
    @("1.21.8", "1.21.8+build.1", "0.136.1+1.21.8", "1.11.8", "old2"),
    @("1.21.9", "1.21.9+build.1", "0.134.1+1.21.9", "1.18.2", "mid"),
    @("1.21.10", "1.21.10+build.3", "0.138.4+1.21.10", "1.18.2", "mid"),
    @("1.21.11", "1.21.11+build.6", "0.141.6+1.21.11", "1.18.2", "new")
)

if ($Only -ne "") {
    $want = $Only.Split(",") | ForEach-Object { $_.Trim() }
    $filtered = @()
    foreach ($ver in $VERSIONS) {
        if ($want -contains $ver[0]) { $filtered += ,$ver }
    }
    $VERSIONS = $filtered
}

$TITLE = "src\main\java\gg\tyx\client\mixin\TitleScreenMixin.java"
$MIXIN = "src\main\java\gg\tyx\client\mixin\ManagerCosmeticsMixin.java"
$SELF1 = "src\main\java\gg\tyx\client\mixin\SelfNametagMixin.java"
$SELF2 = "src\main\java\gg\tyx\client\mixin\SelfNametagBase.java"
$OLDMIXIN = "src\main\java\gg\tyx\client\mixin\NametagBadgeMixin.java"
$KEYS = "src\main\java\gg\tyx\client\Keybinds.java"
$MENU = "src\main\java\gg\tyx\client\gui\TyxMenuScreen.java"
$JSON = "src\main\resources\tyxclient.mixins.json"

function Restore-Canonical {
    Copy-Item "variants\canonical\TitleScreenMixin.java" $TITLE -Force
    Copy-Item "variants\canonical\ManagerCosmeticsMixin.java" $MIXIN -Force
    Copy-Item "variants\canonical\SelfNametagMixin.java" $SELF1 -Force
    Copy-Item "variants\canonical\SelfNametagBase.java" $SELF2 -Force
    Copy-Item "variants\canonical\Keybinds.java" $KEYS -Force
    Copy-Item "variants\canonical\TyxMenuScreen.java" $MENU -Force
    Copy-Item "variants\canonical\tyxclient.mixins.json" $JSON -Force
    if (Test-Path $OLDMIXIN) { Remove-Item $OLDMIXIN -Force }
}

function Apply-Variant($kind) {
    Restore-Canonical
    if ($kind -eq "old" -or $kind -eq "old1" -or $kind -eq "old2") {
        # Title video bg: 1.21.1 plain call / 1.21.2–5 layer-function /
        # 1.21.6–8 render pipeline (same file as canonical). Everything else
        # is the shared old dispatcher pipeline (1218 files + json).
        $titledir = if ($kind -eq "old1") { "variants\1211" } elseif ($kind -eq "old2") { "variants\1216" } else { "variants\1218" }
        Copy-Item "$titledir\TitleScreenMixin.java" $TITLE -Force
        Copy-Item "variants\1218\NametagBadgeMixin.java" $OLDMIXIN -Force
        Copy-Item "variants\1218\SelfNametagMixin.java" $SELF1 -Force
        Copy-Item "variants\1218\SelfNametagBase.java" $SELF2 -Force
        Copy-Item "variants\1218\Keybinds.java" $KEYS -Force
        Copy-Item "variants\1218\TyxMenuScreen.java" $MENU -Force
        Copy-Item "variants\1218\tyxclient.mixins.json" $JSON -Force
        Remove-Item $MIXIN -Force
    } elseif ($kind -eq "mid") {
        # 1.21.9/10 hybrid pipeline: old RenderLayer factory names.
        (Get-Content $MIXIN) -replace "RenderLayers\.entityCutout\(", "RenderLayer.getEntityCutout(" `
            -replace "RenderLayers\.entityCutoutNoCull\(", "RenderLayer.getArmorCutoutNoCull(" `
            -replace "import net\.minecraft\.client\.render\.RenderLayers;", "import net.minecraft.client.render.RenderLayer;" `
        | Set-Content $MIXIN
    }
}

function Set-Loom($v) {
    (Get-Content build.gradle) -replace "id 'fabric-loom' version '[^']*'", "id 'fabric-loom' version '$v'" | Set-Content build.gradle
}

$results = @()
foreach ($ver in $VERSIONS) {
    $mc, $yarn, $fapi, $loom, $kind = $ver
    Write-Output "=== $mc ($kind, loom $loom) ==="
    Set-Content gradle.properties "org.gradle.jvmargs=-Xmx3G`r`norg.gradle.daemon=false`r`nminecraft_version=$mc`r`nyarn_mappings=$yarn`r`nloader_version=0.16.14`r`nfabric_api_version=$fapi`r`nmod_version=$MOD"
    Set-Loom $loom
    Apply-Variant $kind
    if ($kind -eq "new" -or $kind -eq "mid") { $env:JAVA_HOME = $JDK25; $gradle = $G9 }
    else { $env:JAVA_HOME = $JDK21; $gradle = $G8 }
    # Gradle/Mixin warnings on stderr must not kill the loop under
    # $ErrorActionPreference="Stop" (require=0 skips warn once per build).
    $eap = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & $gradle build > build-last.log 2>&1
    $code = $LASTEXITCODE
    $ErrorActionPreference = $eap
    Get-Content build-last.log | Select-String -Pattern "FAILED|BUILD SUCCESSFUL|BUILD FAILED" | Select-Object -Last 3 | ForEach-Object { Write-Output $_.Line }
    if ($code -eq 0) {
        Copy-Item "build\libs\tyx-client-$mc-$MOD.jar" "..\public\tyx-mods\tyx-client-$mc-$MOD.jar" -Force
        $results += "$mc OK"
    } else {
        $results += "$mc FAILED"
    }
}

Restore-Canonical
Set-Loom "1.18.2"
Set-Content gradle.properties "org.gradle.jvmargs=-Xmx3G`r`norg.gradle.daemon=false`r`nminecraft_version=1.21.11`r`nyarn_mappings=1.21.11+build.6`r`nloader_version=0.16.14`r`nfabric_api_version=0.141.6+1.21.11`r`nmod_version=$MOD"
Write-Output "==== RESULTS ===="
$results | ForEach-Object { Write-Output $_ }
