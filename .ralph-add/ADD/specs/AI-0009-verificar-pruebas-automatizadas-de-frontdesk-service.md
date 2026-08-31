# A.SPEC AI-0009 — Verificar pruebas automatizadas de frontdesk-service

ID: AI-0009
Mode: VERIFY
RISK: LOW

## WHY
La compilación de frontdesk-service pasó, pero aún no existe evidencia de que sus pruebas automatizadas validen la integración de IA.

## WHAT
Ejecutar la suite determinista de pruebas del módulo frontdesk-service y registrar el resultado.

## SCOPE
- frontdesk-service
- pruebas automatizadas existentes

## OUT OF SCOPE
- Cambios de código
- Cambios de esquema o base de datos
- Configuración de secretos
- Despliegue
- Pruebas remotas

## CONTRACT
- El comando debe finalizar con exit=0.
- Las pruebas existentes deben completarse sin fallos.

## INVARIANTS
- No modificar archivos fuente, configuración ni contratos.
- No iniciar servicios externos ni alterar datos persistentes.

## VERIFICATION
- Confirmar exit=0.
- Confirmar que Gradle reporte BUILD SUCCESSFUL.
- Registrar cualquier prueba omitida o fallida como evidencia de bloqueo.

## ROLLBACK
No aplica: VERIFY sin mutaciones de fuente ni configuración.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --no-daemon
END_VERIFY_COMMANDS
