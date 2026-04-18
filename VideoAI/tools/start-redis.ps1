param(
  [int]$Port = 6379
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$runLogDir = Join-Path $projectRoot ".runlogs"
$redisExe = Join-Path $PSScriptRoot "redis\\redis-server.exe"
New-Item -ItemType Directory -Force -Path $runLogDir | Out-Null

if (Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue) {
  Write-Host "Redis already listening on port $Port"
  exit 0
}

if (!(Test-Path $redisExe)) {
  throw "Redis executable not found: $redisExe"
}

Start-Process -FilePath $redisExe `
  -ArgumentList "--port $Port" `
  -WorkingDirectory (Split-Path $redisExe -Parent) `
  -RedirectStandardOutput (Join-Path $runLogDir "redis.out.log") `
  -RedirectStandardError (Join-Path $runLogDir "redis.err.log")

Start-Sleep -Seconds 2
if (Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue) {
  Write-Host "Redis started on port $Port"
} else {
  throw "Failed to start Redis on port $Port"
}
