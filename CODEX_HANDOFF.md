# Estado de transformación CAMRA → FIXI

## Último workpack completado

Z02, Z03, Z05 y Z06 quedaron parcialmente implementados: se agregaron las rutas
`/branch-dashboard`, selector de sucursal y métricas reales derivadas de órdenes,
existencias y usuarios, además de reportes de órdenes, inventario y productividad.
La validación sigue diferida y el contrato backend de
listado aún es tenant-wide; no se marcó como validado ni completamente cerrado.
AE02 se mantiene bloqueado: `/reservations`, `/stays`, `/rooms`, `/housekeeping`,
`/restaurant`, `/calendar`, `/rates`, hotel profile y flujos de quotations siguen
teniendo consumidores activos. AF04, AF11 y AF12 conservan sus contratos reales
con las brechas externas ya registradas; AG05 no puede cerrarse mientras queden
workpacks parciales.

U08, W03–W04, X02–X07 y Y02–Y06 — IMPLEMENTACIÓN APLICADA / BLOQUEO PARCIAL /
VALIDACIÓN PENDIENTE. U08 no puede retirar contratos porque aún tiene
consumidores activos; notificaciones, herramientas AI y reglas de automatización
ya tienen sus contratos persistidos. O02, R04, N01, L01–L06, AA01–AA05,
M01/M03 y cambios previos se conservaron.

## Próximo workpack

A07 — SECURITY ISOLATION HARDENING remains partial with gateway-signed tenant propagation and a server-side branch ownership guard added to `CapabilityService`; continue AA02/AA04 producer coverage where safe. AE02 remains blocked by active legacy consumers.
dependencias, sin iniciar validación global mientras exista implementación viable.

## Estado actual del repositorio

El estado detallado de los 202 workpacks está en
`CAMRA_TO_FIXI_PROGRESS.md`. A01–D05 y los workpacks funcionales ya marcados
como `[x]` allí deben tratarse como trabajo previo y no reimplementarse. AE01
está validado como auditoría. AE02 está `[~]`: el inventario es completo, pero
la retirada frontend no puede considerarse terminada mientras existan los
consumidores descritos abajo. AE03–AG05 no deben considerarse ejecutados por
este checkpoint.

## Cambios recientes relevantes

- AE01 produjo `docs/AE01_HOTEL_DOMAIN_AUDIT.md`, con inventario de frontend,
  backend, FIXI, migraciones y terminología.
- AE02 produjo `docs/AE02_FRONTEND_ROUTE_DEPENDENCY_MAP.md`, que clasifica las
  rutas canónicas FIXI, las rutas hoteleras legacy y las dependencias que
  bloquean su eliminación.
- Se agregaron también auditorías de apoyo para AE03–AE07; no representan
  implementación ni autorización para saltar el checkpoint: son evidencia
  persistida de dependencias y riesgos.
- Se añadieron `CatalogImportJob`, `CatalogImportService`,
  `CatalogImportController` y `V44__create_catalog_import_jobs.sql`.
- Se añadieron `AuditEvent`, `AuditEventService`, `AuditEventController` y
  `V45__create_audit_events.sql`.

## Decisiones arquitectónicas vigentes

- No eliminar rutas frontend hoteleras mientras sigan siendo referenciadas por
  `Dashboard`, redirecciones de roles, cotizaciones, navegación, servicios o
  pruebas.
- El shell operativo FIXI debe reemplazar primero los enlaces y estadísticas
  que dependen de habitaciones/estancias; después se puede retirar por grupos.
- No cambiar contratos de dominio, aislamiento tenant/branch, facturación,
  cotizaciones inmutables ni idempotencia para acelerar la retirada.
- `platform/hotels` es administración de tenants y no debe retirarse como si
  fuera una pantalla hotelera tenant-operativa.

## Validaciones ya realizadas

- Frontend `npm run build`: PASS.
- Frontend `npm run lint`: PASS.
- Suite frontend Vitest: PASS, 89 archivos / 826 tests.
- Prueba dirigida de `RateCalendar`: PASS después de ajustar timeout de axe.
- `auth-service`: Checkstyle 0, PMD 0, compilación y pruebas PASS.
- `frontdesk-service`: Checkstyle 0, PMD 5 residuales de heurística, compilación
  y pruebas PASS.
- `billing-service`: Checkstyle 0, PMD 1 residual de heurística (`CashDtos`),
  compilación y pruebas PASS.
- `ServiceOrderAuthorizationTest` y suite frontdesk: PASS.
- No existe validación E2E live en este checkpoint: Docker Desktop no estaba
  disponible (`docker compose ps` no pudo conectar al daemon).

## Validaciones pendientes

- Completar el reemplazo del shell frontend requerido para terminar AE02.
- Revalidar rutas, navegación, roles y pruebas frontend después de retirar
  consumidores hoteleros.
- AE03–AE07 y AF01–AF20.
- Validación global AG01–AG05: seguridad, tenant/branch isolation, permisos,
  workflows, cotizaciones, finanzas, inventario, POS, portal, WhatsApp, IA,
  migraciones, runtime, observabilidad y regresión crítica.

## Bloqueos/dependencias de entorno

- La validación E2E real depende de Docker Desktop y del stack configurado.
- No se debe interpretar la ausencia del daemon como validación exitosa.
- El repositorio contiene cambios acumulados de workpacks anteriores; no deben
  limpiarse ni revertirse para aislar AE02.

## Deuda técnica conocida

- PMD conserva 5 heurísticas en frontdesk y 1 heurística en billing; están
  documentadas como falsos positivos/contratos que no deben alterarse solo para
  satisfacer la herramienta.
- Checkstyle está limpio en los módulos auditados.
- La retirada hotel legacy sigue pendiente y no debe hacerse con reemplazos
  globales de nombres ni borrado masivo.

## Estado del retiro hotel legacy

Puede conservarse como superficie de compatibilidad hasta completar la
migración. `/service-orders`, inventario, workflow, guests, billing y assistant
son superficies FIXI/canónicas o compartidas. Todavía tienen consumidores
activos y NO deben eliminarse: `/reservations`, `/stays`, `/rooms`,
`/housekeeping`, `/restaurant`, `/calendar`, `/rates`, `/profile/hotel`, y los
formularios/redirecciones asociados. Quotations también depende de rooms y
reservations. Ninguna ruta fue borrada en AE02.

## Archivos/documentos que la nueva sesión debe leer primero

1. `CODEX_HANDOFF.md`
2. `CAMRA_TO_FIXI_PROGRESS.md`
3. `docs/AE02_FRONTEND_ROUTE_DEPENDENCY_MAP.md`
4. `docs/AE01_HOTEL_DOMAIN_AUDIT.md`
5. `D:\Desktop\CATALOGO_EXACTO_WORKPACKS_A01-AG05.txt`

## Estado de cambios y git

El repositorio quedó con cambios sin commit; no se hizo commit. Hay numerosos
cambios acumulados de workpacks previos en auth, billing, frontdesk, frontend,
guest, fb e internal-auth-lib, además de nuevos módulos FIXI, migraciones,
pruebas y scripts. Los archivos nuevos relevantes de este checkpoint son
`CODEX_HANDOFF.md` y `docs/AE01_HOTEL_DOMAIN_AUDIT.md` hasta
`docs/AE07_DEAD_CODE_CLEANUP_AUDIT.md`; `git status --short` fue verificado.
Una nueva sesión debe preservar esos cambios y no usar una limpieza destructiva
para “preparar” AE03.

## Instrucción de continuación

Continuar desde N01 sin reauditar ni reimplementar workpacks anteriores salvo
que una falla nueva demuestre que es necesario. Durante la implementación
masiva no ejecutar tests, builds, auditorías, E2E, QA, Checkstyle ni PMD.
