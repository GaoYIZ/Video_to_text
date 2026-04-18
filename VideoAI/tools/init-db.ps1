param(
  [string]$MySqlExe = "",
  [string]$DbHost = "127.0.0.1",
  [int]$DbPort = 3306,
  [string]$DbUser = "root",
  [string]$DbPassword = "gao200039",
  [string]$DbName = "videoai"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$sqlFile = Join-Path $projectRoot "sql\\videoai.sql"

function Resolve-MySqlExe {
  param([string]$Preferred)
  if ($Preferred -and (Test-Path $Preferred)) { return $Preferred }
  $cmd = Get-Command mysql.exe -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }
  $fallbacks = @(
    "D:\\Mysql\\bin\\mysql.exe",
    "E:\\MySQL\\bin\\mysql.exe"
  )
  foreach ($candidate in $fallbacks) {
    if (Test-Path $candidate) { return $candidate }
  }
  throw "mysql.exe not found. Please pass -MySqlExe explicitly."
}

$mysql = Resolve-MySqlExe -Preferred $MySqlExe
Write-Host "Using mysql: $mysql"

function Invoke-MySqlQuery {
  param(
    [string]$Query,
    [string]$Database = ""
  )
  $args = @(
    "--default-character-set=utf8mb4",
    "-h$DbHost",
    "-P$DbPort",
    "-u$DbUser",
    "-p$DbPassword"
  )
  if ($Database) {
    $args += $Database
  }
  $args += "-e"
  $args += $Query
  & $mysql @args
}

function Test-ColumnExists {
  param(
    [string]$TableName,
    [string]$ColumnName
  )
  $args = @(
    "--default-character-set=utf8mb4",
    "-N",
    "-s",
    "-h$DbHost",
    "-P$DbPort",
    "-u$DbUser",
    "-p$DbPassword",
    $DbName,
    "-e",
    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '$DbName' AND TABLE_NAME = '$TableName' AND COLUMN_NAME = '$ColumnName';"
  )
  $result = & $mysql @args
  return ([int]($result | Select-Object -First 1)) -gt 0
}

Invoke-MySqlQuery -Query "CREATE DATABASE IF NOT EXISTS $DbName DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
Get-Content -Encoding UTF8 $sqlFile | & $mysql "--default-character-set=utf8mb4" "-h$DbHost" "-P$DbPort" "-u$DbUser" "-p$DbPassword" $DbName

if (-not (Test-ColumnExists -TableName "user" -ColumnName "daily_quota_limit")) {
  Invoke-MySqlQuery -Database $DbName -Query "ALTER TABLE user ADD COLUMN daily_quota_limit INT NOT NULL DEFAULT 100;"
}
if (-not (Test-ColumnExists -TableName "user" -ColumnName "monthly_quota_limit")) {
  Invoke-MySqlQuery -Database $DbName -Query "ALTER TABLE user ADD COLUMN monthly_quota_limit INT NOT NULL DEFAULT 3000;"
}
Invoke-MySqlQuery -Database $DbName -Query "UPDATE user SET nickname = 'Demo User', daily_quota_limit = 100, monthly_quota_limit = 3000 WHERE username = 'demo_user';"

Write-Host "Database initialized: $DbName"
