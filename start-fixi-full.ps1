$ErrorActionPreference = 'Stop'
$compose = @('-f', 'docker-compose.yml', '-f', 'docker-compose.native.yml', '--profile', 'observability')
Write-Host 'Pulling prebuilt Fixi Native images from GHCR...'
docker compose @compose pull
if ($LASTEXITCODE -ne 0) { throw 'Native image pull failed. Check GHCR login and FIXI_NATIVE_TAG.' }
docker compose @compose up -d
if ($LASTEXITCODE -ne 0) { throw 'Fixi Full startup failed.' }
