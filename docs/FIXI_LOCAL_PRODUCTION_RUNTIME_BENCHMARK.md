# Fixi: runtime local pull-only y benchmark de laptop

Este documento describe el runtime temporal previsto para la laptop Windows. No se debe usar para abrir Fixi a Internet. El Compose productivo publica únicamente el borde Caddy en loopback; la configuración usa TLS de CA interna, no certificados públicos. No hay port forwarding ni firewall changes.

## Artefactos de Actions y selección de versión

GitHub Actions debe producir un solo SHA de fuente para los ocho servicios Native, `fixi-frontend` y `fixi-postgres-pgbackrest`. El workflow publica tags por SHA, registra el digest OCI real de cada imagen y adjunta el artifact `fixi-production-image-refs-<SHA>`. Redis y Caddy también se fijan por digest. El Compose usa `tag@digest`; no usa `native-latest`.

Antes de cualquier arranque local, se requiere que para ese SHA estén verdes:

- CI Quality Gate y tenant/branch isolation;
- publicación de los ocho Native y de las imágenes frontend/PostgreSQL;
- validación del Compose pull-only y escaneo Trivy disponible;
- Native integrated stack con esas diez imágenes.

El artifact de referencias no contiene credenciales. Sus líneas `FIXI_*_DIGEST` se incorporan al `.env` local ignorado por Git, junto con el mismo `FIXI_RELEASE_SHA`. No copiar ni publicar el archivo `.env`.

## Topología y datos

- Caddy es el único servicio con puertos publicados; por defecto `127.0.0.1:8080` y `127.0.0.1:8443`. Caddy usa TLS interno para `fixi.localhost`. La URL local es `https://fixi.localhost:8443`.
- Caddy → frontend Nginx → API gateway. El Nginx conserva el esquema HTTPS del proxy y Caddy sobrescribe `X-Real-IP`; el perfil `prod` del gateway activa la configuración de proxy prevista.
- Gateway, backend y DB no publican puertos al host. La red DB, gateway y observabilidad es `internal`; el Compose productivo quita la red observabilidad compartida de los servicios de aplicación. Las redes de aplicación con salida siguen disponibles para integraciones.
- No arranca Mailpit ni el perfil de observabilidad. El SMTP queda sin relay mientras no se configure uno sintético y aprobado.
- PostgreSQL y pgBackRest usan imágenes GHCR por digest. El volumen productivo separado `pgbackrest_repo_encrypted` deja intacto el repo existente, que es sin cifrado. El volumen de datos del nuevo proyecto también es nuevo y vacío: no migra ni reutiliza datos actuales.
- La passphrase de pgBackRest entra como Docker secret desde `PGBACKREST_CIPHER_PASS_FILE`; el wrapper la lee sin imprimirla y el modo `production` falla cerrado si falta. No se creó ni se regeneró ninguna passphrase en este workpack. El archivo protegido debe existir antes de arrancar.
- Redis usa AOF `everysec` y un volumen dedicado. Debe conservar nonces HMAC anti-replay, revocaciones/versiones de refresh token, lockouts de login, estado del rate limiter y otros datos temporales; una pérdida puede invalidar sesiones, borrar bloqueos o abrir de nuevo una ventana de replay. No es la fuente autoritativa de negocio y no forma parte del backup PostgreSQL.

## Variables necesarias

Las variables están nombradas y documentadas en `.env.example`. Para el perfil temporal hacen falta el SHA y los digests del artifact, `FIXI_PUBLIC_HOST`, `FIXI_PUBLIC_URL` (incluido `:8443` local), las credenciales runtime habituales (`POSTGRES_PASSWORD`, `REDIS_PASSWORD`, `CONFIG_SERVER_PASSWORD`, `INTERNAL_HMAC_SECRET`, `JWT_SECRET`) y `PGBACKREST_CIPHER_PASS_FILE`. El path apunta a un archivo protegido, no a su contenido. El correo SMTP es opcional para el benchmark y debe apuntar sólo a un sink sintético. No configurar las variables S3/B2 en esta fase.

En Linux el archivo de passphrase debe ser legible sólo por el administrador/runtime (por ejemplo modo `0600`). En Windows, restringir ACL al usuario que administra Docker y no guardar el archivo dentro del repositorio. No usar el passphrase del CI ni una cadena de ejemplo.

## Arranque futuro — no ejecutado en esta tarea

Una vez verdes los checks, descargado el artifact y creado/configurado el archivo protegido de passphrase, abrir PowerShell en la raíz del repo y confirmar manualmente que el `.env` contiene los identificadores de imagen del mismo SHA. Después:

```powershell
$ComposeArgs = @(
  '-p', 'fixi-production-benchmark',
  '-f', 'docker-compose.yml',
  '-f', 'docker-compose.native.yml',
  '-f', 'docker-compose.prod.yml'
)
docker compose @ComposeArgs pull
docker compose @ComposeArgs up -d --no-build --wait --wait-timeout 900
docker compose @ComposeArgs ps --all
curl.exe --insecure --fail --show-error https://fixi.localhost:8443/
```

El `--insecure` sólo sirve para la CA interna temporal. El nombre de proyecto único evita colisiones y crea volúmenes separados. Nunca añadir `--build`, `down --volumes` o `volume prune`. No crear clientes reales; usar únicamente tenant/usuarios/datos sintéticos y obtenerlos por el procedimiento de onboarding ya previsto. Si no se puede provisionar un usuario sintético sin introducir datos reales, no iniciar el flujo funcional.

## Benchmark propuesto

Ejecutar cada escenario en una ventana separada y guardar timestamps/resultados fuera del repo. Registrar baseline, no sólo el pico:

1. **Cold start:** desde cero, healthcheck y tiempo hasta todos los servicios healthy.
2. **Idle 15 min:** muestreo cada 10 s; RAM disponible y comprometida Windows, pagefile, proceso `vmmem`, CPU host, memoria/CPU/I/O de cada contenedor, espacio libre de C:/D:, estado de salud, restarts/OOM y contadores PostgreSQL.
3. **Flujo funcional sintético:** login; cliente/equipo; orden; asignación; servicio/producto; movimiento de inventario; pago/anticipo; cierre; historial/reporte. Registrar latencia y errores por paso.
4. **3 sesiones**, luego **10**, luego **20**: sólo escalar si la etapa previa completó sin errores y con margen. Mantener cinco minutos por escalón; anotar concurrencia y tasa de peticiones. No continuar si cualquier umbral de parada aparece.
5. **Backup local:** validar en el repo cifrado nuevo el resultado y el tiempo de un ciclo pgBackRest; registrar tamaño, CPU, disco y WAL. Esto no sustituye un Restore Drill ni demuestra off-site.
6. **Restart controlado:** sólo después de completar y registrar los escenarios anteriores; reiniciar el stack de benchmark, nunca Docker Desktop/WSL completo. Medir recuperación, health, sesión y persistencia de Redis/PostgreSQL.

Para métricas Windows se puede muestrear desde PowerShell:

```powershell
Get-CimInstance Win32_OperatingSystem |
  Select-Object @{n='AvailableGiB';e={[math]::Round($_.FreePhysicalMemory/1MB,2)}},
                @{n='TotalGiB';e={[math]::Round($_.TotalVisibleMemorySize/1MB,2)}}
Get-CimInstance Win32_PerfFormattedData_PerfOS_Memory |
  Select-Object AvailableMBytes,CommittedBytes,CommitLimit,PagesPersec
Get-CimInstance Win32_PerfFormattedData_PerfOS_PagingFile |
  Select-Object Name,PercentUsage
Get-CimInstance Win32_PerfFormattedData_PerfOS_Processor -Filter "Name='_Total'" |
  Select-Object PercentProcessorTime
Get-CimInstance Win32_LogicalDisk -Filter "DeviceID='C:' OR DeviceID='D:'" |
  Select-Object DeviceID,@{n='FreeGiB';e={[math]::Round($_.FreeSpace/1GB,1)}}
Get-Process -Name 'vmmem*' -ErrorAction SilentlyContinue |
  Select-Object ProcessName,@{n='WorkingSetMiB';e={[math]::Round($_.WorkingSet64/1MB,1)}}
docker stats --no-stream --format '{{.Name}}|{{.CPUPerc}}|{{.MemUsage}}|{{.BlockIO}}|{{.NetIO}}'
$ids = @(docker compose -p fixi-production-benchmark -f docker-compose.yml -f docker-compose.native.yml -f docker-compose.prod.yml ps -q)
if ($ids.Count -gt 0) { docker inspect --format '{{.Name}}|{{.State.Health.Status}}|{{.RestartCount}}|{{.State.OOMKilled}}' $ids }
docker compose -p fixi-production-benchmark -f docker-compose.yml -f docker-compose.native.yml -f docker-compose.prod.yml `
  exec -T postgres psql -U postgres -d postgres -Atc "select sum(numbackends),sum(xact_commit),sum(xact_rollback),sum(blks_read),sum(blks_hit),sum(deadlocks) from pg_stat_database"
```

No hay hoy un harness local aprobado que cree usuarios y órdenes sintéticos para 3/10/20 sesiones y calcule P50/P95 de un flujo autenticado. No simular esas cifras con `docker stats` ni con requests anónimos: antes de los escalones concurrentes se debe aprobar/añadir ese driver y un mecanismo de seed exclusivamente sintético. El smoke HTTP y los tests Playwright de GitHub Actions no sustituyen esa medición de laptop.

El comando `docker inspect` puede necesitar iterar cada ID si la versión del CLI no acepta una lista. El consumo de `vmmem` no debe atribuirse enteramente a Fixi: contrastarlo con `docker stats` y las métricas de memoria de WSL.

## Stop conditions

Parar la carga y escalar —no intentar completar 20 sesiones— ante cualquiera de estos casos:

- cualquier OOM o incremento de restart count en Fixi;
- Postgres unhealthy, error de corrupción/FATAL repetido o fallo de archivado WAL;
- cualquier healthcheck de Fixi unhealthy por más de 60 s;
- RAM disponible Windows menor de 2 GiB durante 60 s, o commit/pagefile comprometido mayor de 80% del límite;
- `PagesPersec` sostenido sobre 50 durante 60 s;
- errores funcionales creciendo, tasa de errores HTTP >1% durante un minuto, o P95 >2 s durante un minuto;
- disco de datos/repo con menos de 20% libre.

Los valores son límites conservadores propuestos, no resultados medidos. Capturar estado/logs sin publicar secretos; no ejecutar backup/restore destructivo para investigar una alerta.

## Parar cargas ajenas y restaurar exactamente el estado previo — sólo durante benchmark autorizado

La inspección previa identificó los proyectos Docker `hotel-pms-mexico` (Fixi actual), `pollo-movil` (Supabase) y `pollo-movil-timefold` (Timefold). Vector reinicia por fallo de conexión al Docker API, no por OOM. El siguiente método conserva los IDs de los contenedores que estaban activos y reinicia exactamente esos IDs al finalizar; no borra volúmenes ni corrige Vector:

```powershell
$statePath = Join-Path $env:TEMP 'fixi-benchmark-container-state.json'
$before = [ordered]@{
  fixi = @(docker ps -q --filter 'label=com.docker.compose.project=hotel-pms-mexico')
  supabase = @(docker ps -q --filter 'label=com.docker.compose.project=pollo-movil')
  timefold = @(docker ps -q --filter 'label=com.docker.compose.project=pollo-movil-timefold')
}
$before | ConvertTo-Json | Set-Content -LiteralPath $statePath -Encoding utf8
$stopIds = @($before.fixi + $before.supabase + $before.timefold | Select-Object -Unique)
if ($stopIds.Count -gt 0) { docker stop $stopIds }
```

Al terminar (también si se aborta el benchmark): detener sólo el proyecto nuevo sin borrar volúmenes y restaurar los contenedores previos:

```powershell
docker compose @ComposeArgs down --timeout 30
$before = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
$restoreIds = @($before.fixi + $before.supabase + $before.timefold | Select-Object -Unique)
if ($restoreIds.Count -gt 0) { docker start $restoreIds }
```

No incluir `--volumes`; verificar manualmente que Supabase y Timefold regresaron al estado anterior. El contenedor Vector volverá a su restart loop ya observado; no repararlo durante el benchmark.

## Pendiente antes de hacerlo público

Sustituir TLS interno por TLS público con dominio/DNS controlados; revisar trust proxy y forwarded headers con la topología desplegada; limitar cualquier salida/entrada de red según las integraciones que se habiliten; configurar relay SMTP autorizado; provisionar un passphrase operativo y un procedimiento externo de custodia/recuperación; añadir off-site B2 sólo después del primer despliegue autoritativo y backup cifrado; ejecutar Restore Drill independiente; confirmar backup de Redis sólo si la política de sesión lo requiere. Nada de esto se activa por este Compose local.
