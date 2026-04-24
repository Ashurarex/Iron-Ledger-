$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$target = Join-Path $root "target"
$installer = Join-Path $root "installer"
$jarPath = Join-Path $target "iron-ledger.jar"
$iconPath = Join-Path $root "icon.ico"

if (!(Test-Path $jarPath)) {
    & (Join-Path $PSScriptRoot "build-fat-jar.ps1")
}

if (!(Test-Path $iconPath)) {
    & (Join-Path $PSScriptRoot "create-icon.ps1")
}

Remove-Item -LiteralPath $installer -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $installer | Out-Null

$hasWix = (Get-Command candle.exe -ErrorAction SilentlyContinue) -and (Get-Command light.exe -ErrorAction SilentlyContinue)

if ($hasWix) {
    & jpackage `
        --name "Iron Ledger" `
        --input $target `
        --main-jar "iron-ledger.jar" `
        --type exe `
        --icon $iconPath `
        --win-console `
        --win-shortcut `
        --win-menu `
        --dest $installer

    if ($LASTEXITCODE -ne 0) {
        throw "jpackage installer build failed."
    }
} else {
    Write-Host "WiX installer tooling unavailable. Building app-image executable fallback."

    & jpackage `
        --name "Iron Ledger" `
        --input $target `
        --main-jar "iron-ledger.jar" `
        --type app-image `
        --icon $iconPath `
        --win-console `
        --dest $installer

    if ($LASTEXITCODE -ne 0) {
        throw "jpackage failed."
    }
}

Write-Host "Package output: $installer"
