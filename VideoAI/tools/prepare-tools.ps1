param()

$ErrorActionPreference = "Stop"
$toolsDir = $PSScriptRoot
$ffmpegExe = Join-Path $toolsDir "ffmpeg\\bin\\ffmpeg.exe"
$ffmpegZip = Join-Path $toolsDir "ffmpeg.zip"
$ffmpegExtract = Join-Path $toolsDir "ffmpeg-extract"
$ffmpegDir = Join-Path $toolsDir "ffmpeg"
$redisExe = Join-Path $toolsDir "redis\\redis-server.exe"
$redisZip = Join-Path $toolsDir "redis.zip"
$asrDir = Join-Path $toolsDir "asr"
$asrVenv = Join-Path $asrDir ".venv"
$asrPython = Join-Path $asrVenv "Scripts\\python.exe"
$asrRequirements = Join-Path $asrDir "requirements.txt"
$asrCacheDir = Join-Path $asrDir "cache"
$asrModelDir = Join-Path $asrDir "models\vosk-zh-cn-small"

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

function Ensure-LocalAsr {
  $pythonCommand = Get-Command python -ErrorAction SilentlyContinue
  if (-not $pythonCommand) {
    throw "python command not found. Please install Python 3.10+ first."
  }

  if (!(Test-Path $asrVenv)) {
    Write-Host "Creating ASR virtual environment..."
    & $pythonCommand.Source -m venv $asrVenv
  }

  if (!(Test-Path $asrPython)) {
    throw "ASR python executable not found: $asrPython"
  }

  New-Item -ItemType Directory -Force -Path $asrCacheDir | Out-Null
  $env:HF_HOME = $asrCacheDir
  $env:HUGGINGFACE_HUB_CACHE = $asrCacheDir
  $env:HF_ENDPOINT = "https://hf-mirror.com"

  $dependencyCheck = (& $asrPython -c "import importlib.util; ready = importlib.util.find_spec('vosk') and importlib.util.find_spec('requests'); print('ok' if ready else 'missing')").Trim()
  if ($dependencyCheck -ne "ok") {
    Write-Host "Installing local ASR dependencies..."
    & $asrPython -m pip install --upgrade pip
    & $asrPython -m pip install -r $asrRequirements
  } else {
    Write-Host "Local ASR dependencies already prepared."
  }

  $modelCheck = if (Test-Path (Join-Path $asrModelDir "ivector")) { "ok" } else { "missing" }
  if ($modelCheck -ne "ok") {
    Write-Host "Preparing Vosk model cache..."
    New-Item -ItemType Directory -Force -Path $asrModelDir | Out-Null
    $zipPath = Join-Path $asrDir "models\vosk-model.zip"
    if (!(Test-Path $zipPath)) {
      Write-Host "Downloading Vosk Chinese model..."
      curl.exe -L "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip" -o $zipPath
    }
    Write-Host "Extracting Vosk model..."
    Expand-Archive -Path $zipPath -DestinationPath "$asrModelDir-temp" -Force
    $extractedDir = Get-ChildItem "$asrModelDir-temp" | Select-Object -First 1
    Move-Item -LiteralPath "$asrModelDir-temp\$($extractedDir.Name)\*" -Destination $asrModelDir -Force
    Remove-Item -Recurse -Force "$asrModelDir-temp"
  } else {
    Write-Host "Vosk model cache already prepared."
  }
}

Ensure-Redis
Ensure-Ffmpeg
Ensure-LocalAsr

Write-Host "Local tools are ready under: $toolsDir"
