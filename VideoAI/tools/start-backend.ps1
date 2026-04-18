param(
  [string]$DbPassword = "gao200039"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $projectRoot "videoai-backend"
$runLogDir = Join-Path $projectRoot ".runlogs"
New-Item -ItemType Directory -Force -Path $runLogDir | Out-Null

if (Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue) {
  Write-Host "Backend already listening on port 8080"
  exit 0
}

function Resolve-CommandPath {
  param([string[]]$Names, [string[]]$Fallbacks)
  foreach ($name in $Names) {
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
  }
  foreach ($fallback in $Fallbacks) {
    if (Test-Path $fallback) { return $fallback }
  }
  return $null
}

function Resolve-Ffmpeg {
  $cmd = Get-Command ffmpeg.exe -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }
  $candidates = @(
    (Join-Path $projectRoot "tools\\ffmpeg\\bin\\ffmpeg.exe"),
    "E:\\ffmpeg-2023-05-04-git-4006c71d19-full_build\\bin\\ffmpeg.exe"
  )
  foreach ($candidate in $candidates) {
    if (Test-Path $candidate) { return $candidate }
  }
  return "ffmpeg"
}

$mvnCmd = Resolve-CommandPath -Names @("mvn.cmd", "mvn") -Fallbacks @("E:\\Maven\\apache-maven-3.9.4-bin\\apache-maven-3.9.4\\bin\\mvn.cmd")
if (-not $mvnCmd) {
  throw "mvn command not found."
}

$ffmpegCmd = Resolve-Ffmpeg
$backendOut = Join-Path $runLogDir "backend.out.log"
$backendErr = Join-Path $runLogDir "backend.err.log"
$command = @"
Set-Location '$backendDir'
`$env:VIDEOAI_DB_PASSWORD = '$DbPassword'
`$env:VIDEOAI_FFMPEG = '$ffmpegCmd'
& '$mvnCmd' spring-boot:run
"@

Start-Process -FilePath "powershell.exe" `
  -ArgumentList "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", $command `
  -RedirectStandardOutput $backendOut `
  -RedirectStandardError $backendErr

Start-Sleep -Seconds 12
if (Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue) {
  Write-Host "Backend started on port 8080"
} else {
  Write-Warning "Backend is not listening on 8080 yet. Check $backendOut and $backendErr"
}
