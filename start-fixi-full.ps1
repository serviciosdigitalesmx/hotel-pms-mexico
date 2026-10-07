$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path -Parent $MyInvocation.MyCommand.Path)
$compose = @('-f', 'docker-compose.yml', '-f', 'docker-compose.native.yml', '--profile', 'observability')
Write-Host 'Pulling prebuilt Fixi Native images from GHCR...'
$nativeServices = @('config-server', 'auth-service', 'api-gateway', 'guest-service', 'frontdesk-service', 'billing-service', 'fb-service', 'notification-service')
docker compose @compose pull --quiet @nativeServices
if ($LASTEXITCODE -ne 0) { throw 'Native image pull failed. Check GHCR login and FIXI_NATIVE_TAG.' }
Write-Host 'Building only the local frontend image...'
docker compose @compose build frontend
if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed.' }
Write-Host 'Recreating services so changed image digests are actually used...'
docker compose @compose up -d --force-recreate --no-build
if ($LASTEXITCODE -ne 0) { throw 'Fixi Full startup failed.' }
$requiredServices = $nativeServices + @('frontend')
$deadline = (Get-Date).AddMinutes(3)
do {
  $notReady = @()
  foreach ($service in $requiredServices) {
    $id = docker compose @compose ps -q $service | Select-Object -First 1
    if (-not $id) { $notReady += "$service=missing"; continue }
    $state = docker inspect $id --format '{{.State.Status}}'
    $health = docker inspect $id --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}no-healthcheck{{end}}'
    if ($state -ne 'running' -or ($health -ne 'healthy' -and $service -ne 'frontend')) { $notReady += "$service=$state/$health" }
  }
  if ($notReady.Count -eq 0) { break }
  if ((Get-Date) -ge $deadline) { throw "Fixi Full healthcheck timeout: $($notReady -join ', ')" }
  Start-Sleep -Seconds 5
} while ($true)
& (Join-Path $PSScriptRoot 'scripts\fixi-status.ps1')
