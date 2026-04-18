$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $projectRoot "videoai-frontend"
$runLogDir = Join-Path $projectRoot ".runlogs"
New-Item -ItemType Directory -Force -Path $runLogDir | Out-Null

if (Get-NetTCPConnection -State Listen -LocalPort 5173 -ErrorAction SilentlyContinue) {
  Write-Host "Frontend already listening on port 5173"
  exit 0
}

function Resolve-Npm {
  $cmd = Get-Command npm.cmd -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }
  if (Test-Path "E:\\Node\\npm.cmd") { return "E:\\Node\\npm.cmd" }
  throw "npm.cmd not found."
}

$npmCmd = Resolve-Npm
Start-Process -FilePath $npmCmd `
  -ArgumentList "run", "dev" `
  -WorkingDirectory $frontendDir `
  -RedirectStandardOutput (Join-Path $runLogDir "frontend.out.log") `
  -RedirectStandardError (Join-Path $runLogDir "frontend.err.log")

Start-Sleep -Seconds 8
if (Get-NetTCPConnection -State Listen -LocalPort 5173 -ErrorAction SilentlyContinue) {
  Write-Host "Frontend started on port 5173"
} else {
  Write-Warning "Frontend is not listening on 5173 yet. Check logs in .runlogs"
}
