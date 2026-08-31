# A.SPEC AI-0120 — Cerrar healthchecks faltantes de la plataforma de observabilidad

ID: AI-0120
Mode: WRITE
RISK: LOW

## WHY
Grafana, Prometheus y Alertmanager no tienen healthcheck Compose, aunque son componentes críticos para operación y alertamiento.

## WHAT
Añadir healthchecks no destructivos y coherentes con los endpoints o comandos nativos disponibles para Grafana, Prometheus y Alertmanager.

## SCOPE
- Definiciones Compose de grafana
- Definición Compose de prometheus
- Definición Compose de alertmanager

## OUT OF SCOPE
- Reiniciar servicios
- Cambiar puertos
- Cambiar secretos
- Activar receptores externos
- Cambiar reglas Prometheus
- Deploy remoto

## CONTRACT
- Los healthchecks deben devolver fallo cuando el servicio no esté listo.
- No deben depender de herramientas ausentes en la imagen.

## INVARIANTS
- docker compose config debe validar.
- No alterar redes, volúmenes ni credenciales existentes.

## VERIFICATION
- Validación sintáctica de Compose.
- Inspección de que cada healthcheck use una ruta o comando soportado por su imagen.

## ROLLBACK
Eliminar únicamente los bloques de healthcheck añadidos en docker-compose.yml.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- docker-compose.yml
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- docker compose config --quiet
END_VERIFY_COMMANDS
