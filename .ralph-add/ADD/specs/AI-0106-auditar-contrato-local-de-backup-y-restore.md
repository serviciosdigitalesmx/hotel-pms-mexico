# A.SPEC AI-0106 — Auditar contrato local de backup y restore

ID: AI-0106
Mode: READ_ONLY
RISK: LOW

## WHY
Backup y restore proof forman parte explícita de la Definition of Done y ya existen artefactos locales que pueden auditarse sin tocar bases de datos ni infraestructura.

## WHAT
Determinar si el flujo documenta backup, retención, integridad, restore verificable, aislamiento y manejo de errores; clasificar huecos sin ejecutar operaciones.

## SCOPE
- Script local de backup
- Workflow de drill
- Documentación operativa relacionada

## OUT OF SCOPE
- Ejecutar backup o restore
- Acceder a PostgreSQL
- Cambiar secretos
- Cambiar workflows
- Deploy

## CONTRACT
- No inventar proveedores, destinos ni garantías
- Distinguir evidencia estática de prueba real
- No exponer credenciales

## INVARIANTS
- Solo lectura
- No alterar artefactos existentes
- No realizar operaciones destructivas

## VERIFICATION
- Matriz requisito-evidencia
- Lista de bloqueadores para un restore proof aprobado
- Clasificación LOW/MEDIUM/HIGH/CRITICAL

## ROLLBACK
No aplica; auditoría read-only.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- docker/postgres/backup-scheduler.sh
- .github/workflows/backup-restore-drill.yml
- docs
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- sed -n '1,260p' docker/postgres/backup-scheduler.sh
- sed -n '1,300p' .github/workflows/backup-restore-drill.yml
- rg -n "backup|restore|pgbackrest|retention|checksum|verify|drill" docker/postgres/backup-scheduler.sh .github/workflows/backup-restore-drill.yml docs
END_VERIFY_COMMANDS
