# A.SPEC AI-0118 — Auditoría de observabilidad, healthchecks y alertas operativas

ID: AI-0118
Mode: READ_ONLY
RISK: LOW

## WHY
La operación real requiere señales observables de disponibilidad, errores y dependencias antes de cualquier readiness claim.

## WHAT
Mapear servicios, healthchecks, endpoints observados, reglas de alerta, destinos y huecos estáticos.

## SCOPE
- Prometheus
- Alertmanager y reglas
- Healthchecks de Compose
- Documentación de operación

## OUT OF SCOPE
- Arrancar o reiniciar servicios
- Modificar alertas
- Cambiar secretos
- Deploy
- Pruebas contra infraestructura externa

## CONTRACT
- Reportar solo contratos presentes en archivos
- No considerar configuración estática como prueba runtime
- No imprimir valores sensibles

## INVARIANTS
- Solo lectura
- Se preserva el worktree
- No se cambian redes ni volúmenes

## VERIFICATION
- Tabla servicio-healthcheck-alerta
- Servicios sin señal observable
- Separación entre evidencia estática y smoke runtime pendiente

## ROLLBACK
No aplica; auditoría read-only.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- docker/prometheus
- docker-compose.yml
- docs
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n "health|readiness|liveness|actuator|alert|route|scrape|dependency|error" docker-compose.yml docker/prometheus docs
- sed -n '1,320p' docker/prometheus/alert_rules.yml
- sed -n '1,260p' docker/prometheus/prometheus.yml
END_VERIFY_COMMANDS
