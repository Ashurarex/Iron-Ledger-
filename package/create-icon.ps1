$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$iconPath = Join-Path $root "icon.ico"

Add-Type -AssemblyName System.Drawing

$bitmap = New-Object System.Drawing.Bitmap 64, 64
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.Clear([System.Drawing.Color]::Transparent)

$bg = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    [System.Drawing.Rectangle]::new(0, 0, 64, 64),
    [System.Drawing.Color]::FromArgb(59, 130, 246),
    [System.Drawing.Color]::FromArgb(17, 24, 39),
    45
)
$graphics.FillEllipse($bg, 4, 4, 56, 56)

$font = New-Object System.Drawing.Font "Segoe UI", 28, ([System.Drawing.FontStyle]::Bold), ([System.Drawing.GraphicsUnit]::Pixel)
$brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
$format = New-Object System.Drawing.StringFormat
$format.Alignment = [System.Drawing.StringAlignment]::Center
$format.LineAlignment = [System.Drawing.StringAlignment]::Center
$graphics.DrawString("IL", $font, $brush, [System.Drawing.RectangleF]::new(0, 0, 64, 62), $format)

$handle = $bitmap.GetHicon()
$icon = [System.Drawing.Icon]::FromHandle($handle)
$stream = [System.IO.File]::Open($iconPath, [System.IO.FileMode]::Create)
try {
    $icon.Save($stream)
} finally {
    $stream.Dispose()
    $icon.Dispose()
    $graphics.Dispose()
    $bitmap.Dispose()
}

Write-Host "Created $iconPath"
