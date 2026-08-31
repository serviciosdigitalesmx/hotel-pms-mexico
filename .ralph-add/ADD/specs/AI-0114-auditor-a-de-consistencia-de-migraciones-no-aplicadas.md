# A.SPEC AI-0114 — Auditoría de consistencia de migraciones no aplicadas

ID: AI-0114
Mode: READ_ONLY
RISK: LOW

## WHY
Hay migraciones nuevas no rastreadas y dos aprobaciones pendientes; se requiere conocer dependencias sin ejecutarlas ni editarlas.

## WHAT
Mapear versiones, dependencias, referencias de código y documentación para preparar decisiones de aprobación separadas.

## SCOPE
- Inventario de scripts Flyway
- Referencias de tablas/columnas
- Orden y conflictos de versión
- Relación con APPROVAL-0000 y APPROVAL-0001

## OUT OF SCOPE
- Aplicar migraciones
- Editar migraciones
- Cambiar esquema
- Usar base de datos
- Desplegar

## CONTRACT
- Nunca editar migraciones aplicadas o existentes
- No inferir que una migración está aplicada por su presencia en el checkout

## INVARIANTS
- Las aprobaciones HIGH/CRITICAL siguen intactas
- No se ejecutan comandos destructivos ni de escritura

## VERIFICATION
- Tabla de versiones y dependencias
- Conflictos concretos, si existen
- Recomendación de aprobación o división en A.SPECs

## ROLLBACK
No aplica; operación de solo lectura.

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
