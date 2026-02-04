param(
  [switch]$ReplaceDb
)
$root = Split-Path $PSScriptRoot -Parent
$appYml = Join-Path $root 'litemall\deploy\litemall\application.yml'
if (-not (Test-Path $appYml)) { $appYml = Join-Path $root 'deploy\litemall\application.yml' }
$yml = Get-Content $appYml -Raw
$username = ($yml | Select-String -Pattern 'username:\s*(\S+)' -AllMatches).Matches.Groups[1].Value
$password = ($yml | Select-String -Pattern 'password:\s*(\S+)' -AllMatches).Matches.Groups[1].Value
$url = ($yml | Select-String -Pattern 'url:\s*(\S+)' -AllMatches).Matches.Groups[1].Value
$m = [Regex]::Match($url, 'jdbc:mysql://([^:/]+)(?::(\d+))?/([^?]+)')
$host = $m.Groups[1].Value
$port = if ($m.Groups[2].Success) { [int]$m.Groups[2].Value } else { 3306 }
$db = $m.Groups[3].Value
$binCandidates = @(
  'C:\Program Files\MariaDB 12.1\bin',
  'C:\Program Files\MariaDB 10.6\bin',
  'C:\Program Files\\MySQL\\MySQL Server 8.0\\bin'
)
$mysqldump = $null
$mysql = $null
foreach ($p in $binCandidates) {
  $d = Join-Path $p 'mysqldump.exe'
  if (-not $mysqldump -and (Test-Path $d)) { $mysqldump = $d }
  $c = Join-Path $p 'mysql.exe'
  if (-not $mysql -and (Test-Path $c)) { $mysql = $c }
}
if (-not $mysqldump) { $cmd = Get-Command mysqldump.exe -ErrorAction SilentlyContinue; if ($cmd) { $mysqldump = $cmd.Source } }
if (-not $mysql) { $cmd = Get-Command mysql.exe -ErrorAction SilentlyContinue; if ($cmd) { $mysql = $cmd.Source } }
if (-not $mysqldump) { throw 'mysqldump.exe not found' }
if (-not $mysql) { throw 'mysql.exe not found' }
$env:MYSQL_PWD = $password
$sqlDir = Join-Path $root 'litemall-db\sql'
if (-not (Test-Path $sqlDir)) { New-Item -ItemType Directory -Path $sqlDir | Out-Null }
$schemaFile = Join-Path $sqlDir 'litemall_table.sql'
$dataFile = Join-Path $sqlDir 'litemall_data.sql'
$fullFile = Join-Path $sqlDir 'litemall_full.sql'
& $mysqldump -h $host -P $port -u $username --default-character-set=utf8mb4 --single-transaction --routines --events --triggers --no-data $db | Set-Content -Encoding UTF8 $schemaFile
& $mysqldump -h $host -P $port -u $username --default-character-set=utf8mb4 --single-transaction --no-create-info $db | Set-Content -Encoding UTF8 $dataFile
& $mysqldump -h $host -P $port -u $username --default-character-set=utf8mb4 --single-transaction $db | Set-Content -Encoding UTF8 $fullFile
if ($ReplaceDb) {
  & $mysql -h $host -P $port -u $username -e \"DROP DATABASE IF EXISTS `$db; CREATE DATABASE `$db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;\"
  Get-Content $fullFile -Raw | & $mysql -h $host -P $port -u $username $db
}
