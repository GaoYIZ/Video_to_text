param(
  [string]$DbPassword = "gao200039"
)

$ErrorActionPreference = "Stop"
$toolsDir = $PSScriptRoot

& (Join-Path $toolsDir "prepare-tools.ps1")
& (Join-Path $toolsDir "init-db.ps1") -DbPassword $DbPassword
& (Join-Path $toolsDir "start-redis.ps1")
& (Join-Path $toolsDir "start-backend.ps1") -DbPassword $DbPassword
& (Join-Path $toolsDir "start-frontend.ps1")

Write-Host ""
Write-Host "VideoAI should now be available at:"
Write-Host "Frontend: http://localhost:5173"
Write-Host "Backend : http://localhost:8080"
Write-Host "Swagger : http://localhost:8080/doc.html"
