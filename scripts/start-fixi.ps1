$ErrorActionPreference = 'Stop'

$projectRoot = 'D:\Projects\hotel-pms-mexico'
Set-Location -LiteralPath $projectRoot

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker CLI no está instalado o no está disponible en PATH.'
}

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Desktop no está iniciado. Inícialo y vuelve a ejecutar el acceso directo.'
}

# Development-only values used by the current local Compose stack.
$env:CONFIG_SERVER_PASSWORD = 'codex-runtime-config'
$env:POSTGRES_PASSWORD = 'codex-runtime-db'
$env:REDIS_PASSWORD = 'codex-redis-20261003'
$env:INTERNAL_HMAC_SECRET = 'codex-runtime-hmac-20261005-strong-secret'
$env:JWT_SECRET = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes(('A' * 32)))
$env:SMTP_HOST = 'mailpit'
$env:SMTP_PORT = '1025'

Write-Host 'Levantando Fixi desde:' $projectRoot -ForegroundColor Cyan
docker compose --profile observability up -d
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Compose no pudo levantar Fixi.'
}

if (docker ps -a --format '{{.Names}}' | Select-String -Quiet '^mailpit$') {
    docker start mailpit *> $null
}

Write-Host ''
Write-Host 'Fixi está arrancando. Frontend: http://localhost' -ForegroundColor Green
Write-Host 'API Gateway: http://localhost:8080' -ForegroundColor Green
Write-Host 'Mailpit: http://localhost:18025' -ForegroundColor Green
Write-Host ''
Start-Process 'http://localhost'
docker compose ps
Write-Host ''
Write-Host 'La ventana queda abierta para consultar el estado. Ctrl+C detiene este comando, no elimina los contenedores.' -ForegroundColor Yellow
docker compose logs -f --tail=80
