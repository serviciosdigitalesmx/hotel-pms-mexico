# A.SPEC AI-0109 — Auditar contrato local de backup y restore

ID: AI-0109
Mode: READ_ONLY
RISK: LOW

## WHY
Backup y restore proof son requisitos de V1 y pueden auditarse sin ejecutar operaciones sobre bases de datos o infraestructura.

## WHAT
Mapear evidencia local de backup, retención, integridad, restore verificable, aislamiento y manejo de errores.

## SCOPE
- Script local de backup
- Workflow de restore drill
- Documentación operativa relacionada

## OUT OF SCOPE
- Ejecutar backup o restore
- Acceder a PostgreSQL
- Cambiar secretos
- Modificar workflows
- Deploy

## CONTRACT
- No inventar proveedores, destinos ni garantías
- Distinguir evidencia estática de prueba real
- No exponer credenciales

## INVARIANTS
- Solo lectura
- Worktree sin modificaciones
- Sin operaciones destructivas

## VERIFICATION
- Matriz requisito-evidencia
- Bloqueadores para restore proof aprobado
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
