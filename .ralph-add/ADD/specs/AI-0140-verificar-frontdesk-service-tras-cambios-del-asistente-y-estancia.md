# A.SPEC AI-0140 — Verificar frontdesk-service tras cambios del asistente y estancia

ID: AI-0140
Mode: VERIFY
RISK: LOW

## WHY
El servicio tiene cambios locales extensos y pruebas nuevas de parser, fallback, estancia y configuración.

## WHAT
Ejecutar exclusivamente la suite del frontdesk-service para cerrar o localizar la siguiente reparación concreta.

## SCOPE
- AssistantService
- LocalIntentRouter
- DeterministicParser
- ResilientIntentFallbackHandler
- Stays

## OUT OF SCOPE
- Proveedor externo de IA
- Flyway
- Alertmanager
- Producción

## CONTRACT
- Conservar el enrutamiento determinista existente
- No inventar endpoints ni contratos

## INVARIANTS
- No degradar operaciones de estancia
- No modificar configuración sensible

## VERIFICATION
- Gradle exit 0
- Pruebas del servicio verdes

## ROLLBACK
No aplica: job de verificación sin escritura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/stays
- frontdesk-service/src/test
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --no-daemon
END_VERIFY_COMMANDS
