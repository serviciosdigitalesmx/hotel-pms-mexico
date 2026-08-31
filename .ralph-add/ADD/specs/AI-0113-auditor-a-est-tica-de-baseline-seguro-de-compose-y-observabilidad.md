# A.SPEC AI-0113 — Auditoría estática de baseline seguro de Compose y observabilidad

ID: AI-0113
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0110 y AI-0111 identificaron receptores deshabilitados, cobertura incompleta y una credencial Grafana hardcodeada.

## WHAT
Confirmar el inventario vigente, separar remediaciones LOW/MEDIUM de cambios HIGH que requieren aprobación y detectar contradicciones documentales.

## SCOPE
- Secretos y defaults operativos
- Reglas y receptores de alertas
- Retención documentada
- Diferencias entre Compose y documentación

## OUT OF SCOPE
- Editar secretos
- Cambiar Grafana
- Activar notificaciones externas
- Arrancar servicios
- Cambios de infraestructura

## CONTRACT
- No tratar configuración estática como prueba runtime
- No exponer valores secretos en la salida
- Preservar cambios existentes

## INVARIANTS
- Los cambios HIGH/CRITICAL permanecen bloqueados
- No se alteran reglas ni receptores durante la auditoría

## VERIFICATION
- Inventario actualizado de hallazgos
- Clasificación de cada remediación por riesgo
- Lista de A.SPECs independientes posteriores

## ROLLBACK
No aplica; operación de solo lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- docker-compose.yml
- docker/prometheus/alert_rules.yml
- docker/prometheus/prometheus.yml
- docs/DEPLOYMENT_GUIDE.md
- docs/OPERATIONS_RUNBOOK.md
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n "GF_SECURITY_ADMIN_PASSWORD|ci_placeholder|PGBACKREST_CIPHER_PASS|SMTP_|ALLOGGIATI_|receiver:|retention|repo[0-9]+-retention" docker-compose.yml docker/prometheus docs
- git diff -- docker-compose.yml docker/prometheus/alert_rules.yml docs/DEPLOYMENT_GUIDE.md docs/OPERATIONS_RUNBOOK.md
END_VERIFY_COMMANDS
