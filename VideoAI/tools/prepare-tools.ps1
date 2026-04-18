param()

$ErrorActionPreference = "Stop"
$toolsDir = $PSScriptRoot
$ffmpegExe = Join-Path $toolsDir "ffmpeg\\bin\\ffmpeg.exe"
$ffmpegZip = Join-Path $toolsDir "ffmpeg.zip"
$ffmpegExtract = Join-Path $toolsDir "ffmpeg-extract"
$ffmpegDir = Join-Path $toolsDir "ffmpeg"
$redisExe = Join-Path $toolsDir "redis\\redis-server.exe"
$redisZip = Join-Path $toolsDir "redis.zip"

function Ensure-Redis {
  if (Test-Path $redisExe) {
    Write-Host "Redis already prepared."
    return
  }
  if (!(Test-Path $redisZip)) {
    throw "Redis package not found: $redisZip"
  }

  $extractDir = Join-Path $toolsDir "redis-extract"
  if (Test-Path $extractDir) {
    Remove-Item -Recurse -Force $extractDir
  }

  Write-Host "Extracting Redis package..."
  Expand-Archive -Path $redisZip -DestinationPath $extractDir -Force
  $root = Get-ChildItem $extractDir | Select-Object -First 1
  Move-Item -LiteralPath $root.FullName -Destination (Join-Path $toolsDir "redis")
  Remove-Item -Recurse -Force $extractDir
}

function Ensure-Ffmpeg {
  if (Test-Path $ffmpegExe) {
    Write-Host "FFmpeg already prepared."
    return
  }

  Write-Host "Downloading FFmpeg package to $ffmpegZip ..."
  if (Test-Path $ffmpegZip) {
    Remove-Item -Force $ffmpegZip
  }
  curl.exe -L "https://github.com/BtbN/FFmpeg-Builds/releases/latest/download/ffmpeg-master-latest-win64-gpl.zip" -o $ffmpegZip

  if (Test-Path $ffmpegExtract) {
    Remove-Item -Recurse -Force $ffmpegExtract
  }
  if (Test-Path $ffmpegDir) {
    Remove-Item -Recurse -Force $ffmpegDir
  }

  Write-Host "Extracting FFmpeg package..."
  Expand-Archive -Path $ffmpegZip -DestinationPath $ffmpegExtract -Force
  $root = Get-ChildItem $ffmpegExtract | Select-Object -First 1
  Move-Item -LiteralPath $root.FullName -Destination $ffmpegDir
  Remove-Item -Recurse -Force $ffmpegExtract
}

Ensure-Redis
Ensure-Ffmpeg

Write-Host "Local tools are ready under: $toolsDir"
