# A.SPEC AI-0104 — Inventario read-only de migraciones pendientes y conflictos de secuencia

ID: AI-0104
Mode: READ_ONLY
RISK: LOW

## WHY
Existen migraciones nuevas no versionadas en el worktree; antes de cualquier decisión de aplicación o reparación se necesita un inventario seguro de secuencias, alcance y posibles conflictos.

## WHAT
Comparar nombres, versiones y contenido de migraciones existentes y no versionadas, sin ejecutarlas ni modificarlas.

## SCOPE
- V12-V14
- V21-V23
- V10 y migraciones adyacentes de los servicios indicados

## OUT OF SCOPE
- Aplicar migraciones
- Editar o renombrar migraciones
- Cambios de esquema
- Base de datos
- deploy

## CONTRACT
- No alterar archivos
- Reportar únicamente evidencia local

## INVARIANTS
- Las migraciones existentes permanecen intactas
- No se ejecutan comandos Flyway ni Docker

## VERIFICATION
- Lista ordenada de versiones
- Detección de duplicados o saltos
- Resumen de dependencias y riesgos para aprobación posterior

## ROLLBACK
No aplica; operación de solo lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/resources/db/migration
- guest-service/src/main/resources/db/migration
- billing-service/src/main/resources/db/migration
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg --files frontdesk-service/src/main/resources/db/migration guest-service/src/main/resources/db/migration billing-service/src/main/resources/db/migration | sort
- rg -n "^(--|CREATE|ALTER|DROP|INSERT|UPDATE|DELETE)|Flyway|VERSION" frontdesk-service/src/main/resources/db/migration guest-service/src/main/resources/db/migration billing-service/src/main/resources/db/migration
END_VERIFY_COMMANDS
