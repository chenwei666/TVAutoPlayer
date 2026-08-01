[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$requestedBuildBase = if ($env:TV_AUTOPLAYER_BUILD_ROOT) {
    $env:TV_AUTOPLAYER_BUILD_ROOT
} else {
    'C:\tmp'
}
$buildBase = [System.IO.Path]::GetFullPath($requestedBuildBase)
$buildRoot = [System.IO.Path]::GetFullPath((Join-Path $buildBase 'TVAutoPlayer-build'))
$gradleHome = [System.IO.Path]::GetFullPath((Join-Path $buildBase 'TVAutoPlayer-gradle'))

if ($buildBase -match '[^\x00-\x7F]') {
    throw 'Build root must contain ASCII characters only. Set TV_AUTOPLAYER_BUILD_ROOT to an English path.'
}
if (-not $buildRoot.StartsWith($buildBase, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Unsafe build mirror path: $buildRoot"
}
if (-not $env:JAVA_HOME) {
    throw 'JAVA_HOME is not set. Install JDK 17 and set JAVA_HOME first.'
}
if (-not $env:ANDROID_HOME -and -not $env:ANDROID_SDK_ROOT) {
    throw 'ANDROID_HOME or ANDROID_SDK_ROOT is not set. Install Android SDK Platform 36 first.'
}

New-Item -ItemType Directory -Force -Path $buildBase | Out-Null
New-Item -ItemType Directory -Force -Path $buildRoot | Out-Null
New-Item -ItemType Directory -Force -Path $gradleHome | Out-Null

& robocopy.exe $projectRoot $buildRoot /MIR /R:2 /W:1 `
    /XD .git .gradle build app\build Version dist `
    /XF local.properties
if ($LASTEXITCODE -ge 8) {
    throw "Source mirror failed with Robocopy exit code $LASTEXITCODE"
}

$env:GRADLE_USER_HOME = $gradleHome
Push-Location $buildRoot
try {
    & .\gradlew.bat clean testDebugUnitTest lintDebug assembleDebug --console=plain --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }
} finally {
    Pop-Location
}

$distDirectory = Join-Path $projectRoot 'dist'
New-Item -ItemType Directory -Force -Path $distDirectory | Out-Null
$sourceApk = Join-Path $buildRoot 'app\build\outputs\apk\debug\app-debug.apk'
$targetApk = Join-Path $distDirectory 'TVAutoPlayer-v1.1.1-debug.apk'
Copy-Item -LiteralPath $sourceApk -Destination $targetApk -Force

$sha256 = [System.Security.Cryptography.SHA256]::Create()
$stream = [System.IO.File]::OpenRead($targetApk)
try {
    $hash = -join ($sha256.ComputeHash($stream) | ForEach-Object { $_.ToString('x2') })
} finally {
    $stream.Dispose()
    $sha256.Dispose()
}
Set-Content -LiteralPath (Join-Path $distDirectory 'TVAutoPlayer-v1.1.1-debug.apk.sha256') `
    -Value "$hash  TVAutoPlayer-v1.1.1-debug.apk" `
    -Encoding ascii

Write-Host "Build succeeded: $targetApk"
Write-Host "SHA-256: $hash"
