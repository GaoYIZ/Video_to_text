$ErrorActionPreference = "SilentlyContinue"
$ports = 8080, 5173, 6379
$processIds = Get-NetTCPConnection -State Listen | Where-Object { $ports -contains $_.LocalPort } | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($processId in $processIds) {
  try { Stop-Process -Id $processId -Force } catch {}
}
Write-Host "Stopped listeners on ports: $($ports -join ', ')"
