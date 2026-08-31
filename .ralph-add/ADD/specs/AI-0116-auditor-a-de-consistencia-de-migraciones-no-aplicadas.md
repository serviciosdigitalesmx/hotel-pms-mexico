# A.SPEC AI-0116 — Auditoría de consistencia de migraciones no aplicadas

ID: AI-0116
Mode: READ_ONLY
RISK: LOW

## WHY
Hay migraciones nuevas no rastreadas y aprobaciones HIGH/CRITICAL pendientes; se necesita evidencia antes de solicitar decisiones.

## WHAT
Mapear versiones, dependencias, referencias de código y posibles conflictos sin ejecutar ni editar migraciones.

## SCOPE
- Inventario de scripts Flyway
- Orden de versiones
- Referencias de tablas y columnas
- Relación con APPROVAL-0000 y APPROVAL-0001

## OUT OF SCOPE
- Aplicar migraciones
- Editar migraciones
- Cambiar esquema
- Usar base de datos
- Deploy

## CONTRACT
- No inferir que una migración está aplicada por existir en el checkout
- No modificar migraciones
- Mantener intactas las aprobaciones pendientes

## INVARIANTS
- No se realizan operaciones de escritura
- No se ejecutan comandos destructivos

## VERIFICATION
- Tabla de versiones y dependencias
- Conflictos concretos, si existen
- Recomendación de aprobación o división en A.SPECs

## ROLLBACK
No aplica; auditoría read-only.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/resources/db/migration
- guest-service/src/main/resources/db/migration
- docs
- .ralph-add/ADD/specs
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- find frontdesk-service/src/main/resources/db/migration guest-service/src/main/resources/db/migration -maxdepth 1 -type f -print | sort
- rg -n "V12__|V13__|V14__|V21__|V22__|V23__|V10__|flyway_schema_history|APPROVAL-0000|APPROVAL-0001" frontdesk-service guest-service docs .ralph-add/ADD/specs
END_VERIFY_COMMANDS
