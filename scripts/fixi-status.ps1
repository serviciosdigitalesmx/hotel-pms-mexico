$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Join-Path $PSScriptRoot '..')
$compose = @('-f', 'docker-compose.yml', '-f', 'docker-compose.native.yml')
$services = @('config-server','auth-service','api-gateway','guest-service','frontdesk-service','billing-service','fb-service','notification-service','frontend')
Write-Host 'Fixi runtime image status' -ForegroundColor Cyan
foreach ($service in $services) {
  $id = docker compose @compose ps -q $service
  if (-not $id) { Write-Host "$service|NOT_RUNNING"; continue }
  $container = docker inspect $id | ConvertFrom-Json
  $imageId = $container[0].Image
  $digests = docker image inspect $imageId --format '{{json .RepoDigests}}'
  $health = if ($container[0].State.Health) { $container[0].State.Health.Status } else { 'no-healthcheck' }
  Write-Host "$service|$($container[0].Config.Image)|image-id=$imageId|repo-digests=$digests|$($container[0].State.Status)|$health"
}
Write-Host "`nFrontend: http://localhost"
