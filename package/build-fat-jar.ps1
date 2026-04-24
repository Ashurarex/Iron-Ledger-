$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$target = Join-Path $root "target"
$classes = Join-Path $target "classes"
$jarPath = Join-Path $target "iron-ledger.jar"
$manifest = Join-Path $root "MANIFEST.MF"
$config = Join-Path $root "config.properties"
$postgresJar = Join-Path $env:USERPROFILE ".m2\repository\org\postgresql\postgresql\42.6.1\postgresql-42.6.1.jar"

if (!(Test-Path $postgresJar)) {
    $postgresJar = Get-ChildItem -Path $root -Recurse -Filter "postgresql-*.jar" | Select-Object -First 1 -ExpandProperty FullName
}

if (!$postgresJar -or !(Test-Path $postgresJar)) {
    throw "PostgreSQL JDBC driver not found. Put postgresql-*.jar in the project or Maven cache."
}

Remove-Item -LiteralPath $classes -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $classes | Out-Null

$sources = Get-ChildItem -Path (Join-Path $root "src") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -d $classes $sources

if (Test-Path $config) {
    Copy-Item -LiteralPath $config -Destination (Join-Path $classes "config.properties") -Force
}

Push-Location $classes
try {
    & jar xf $postgresJar
} finally {
    Pop-Location
}

Remove-Item -Path (Join-Path $classes "META-INF\*.SF") -Force -ErrorAction SilentlyContinue
Remove-Item -Path (Join-Path $classes "META-INF\*.RSA") -Force -ErrorAction SilentlyContinue
Remove-Item -Path (Join-Path $classes "META-INF\*.DSA") -Force -ErrorAction SilentlyContinue

Remove-Item -LiteralPath $jarPath -Force -ErrorAction SilentlyContinue
& jar cfm $jarPath $manifest -C $classes .

Write-Host "Built $jarPath"
