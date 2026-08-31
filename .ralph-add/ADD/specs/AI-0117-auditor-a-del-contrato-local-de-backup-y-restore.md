# A.SPEC AI-0117 — Auditoría del contrato local de backup y restore

ID: AI-0117
Mode: READ_ONLY
RISK: LOW

## WHY
Backup y restore proof son requisitos explícitos de V1 y pueden auditarse sin acceder a la base de datos.

## WHAT
Determinar si existen retención, integridad, aislamiento, restore verificable y manejo de errores documentados.

## SCOPE
- Script local de backup
- Workflow de drill
- Documentación operativa

## OUT OF SCOPE
- Ejecutar backup o restore
- Acceder a PostgreSQL
- Cambiar secretos
- Cambiar workflows
- Deploy

## CONTRACT
- Distinguir evidencia estática de prueba real
- No inventar proveedores, destinos ni garantías
- No exponer credenciales

## INVARIANTS
- Solo lectura
- No se alteran artefactos existentes
- No se realizan operaciones destructivas

## VERIFICATION
- Matriz requisito-evidencia
- Bloqueadores para un restore proof aprobado
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
