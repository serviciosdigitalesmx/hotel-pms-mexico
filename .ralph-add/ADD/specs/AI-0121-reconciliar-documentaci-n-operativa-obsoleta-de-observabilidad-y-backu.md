# A.SPEC AI-0121 — Reconciliar documentación operativa obsoleta de observabilidad y backup

ID: AI-0121
Mode: WRITE
RISK: LOW

## WHY
La documentación contradice el Compose y los archivos actuales: perfil backup, existencia de alert rules y modelo histórico de retención.

## WHAT
Marcar o corregir únicamente afirmaciones demostrablemente obsoletas, diferenciando implementación estática de evidencia runtime y dejando pendientes explícitos donde no exista prueba.

## SCOPE
- Comando operativo de arranque
- Estado de reglas Prometheus
- Referencia histórica de backup

## OUT OF SCOPE
- Definir retención nueva
- Ejecutar backup o restore
- Cambiar workflows
- Afirmar restore proof sin ejecución real

## CONTRACT
- La documentación debe reflejar solo capacidades verificadas en el checkout.
- Los pendientes HIGH/MEDIUM deben permanecer explícitos.

## INVARIANTS
- No eliminar evidencia histórica; etiquetarla como histórica cuando corresponda.
- No convertir verificación estática en aprobación operativa.

## VERIFICATION
- Búsqueda de las contradicciones conocidas.
- Revisión diff de solo los tres documentos permitidos.

## ROLLBACK
Revertir únicamente las ediciones realizadas en los tres documentos permitidos.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- docs/OPERATIONS_RUNBOOK.md
- docs/FINAL_AUDIT_ULTRA_SEVERE.md
- docs/ROADMAP.md
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n "profile backup|no alert rules|14 days|pg_dumpall|pgbackrest" docs/OPERATIONS_RUNBOOK.md docs/FINAL_AUDIT_ULTRA_SEVERE.md docs/ROADMAP.md
END_VERIFY_COMMANDS
