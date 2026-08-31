# A.SPEC AI-0147 — Verificar frontdesk-service tras cambios del asistente y estancia

ID: AI-0147
Mode: VERIFY
RISK: LOW

## WHY
El servicio tiene cambios recientes del asistente y estancia; su verificación es independiente de frontend y Flyway.

## WHAT
Ejecutar pruebas focalizadas del servicio frontdesk y conservar los contratos actuales.

## SCOPE
- Intent routing
- Fallback
- ConversationSessionStore
- Operaciones de estancia

## OUT OF SCOPE
- Cambios de autenticación
- Migraciones
- Despliegue
- Producción

## CONTRACT
- El router conserva fallback determinista
- Las sesiones mantienen el contrato actual
- Las operaciones de estancia no pierden aislamiento

## INVARIANTS
- Solo lectura
- No cambiar contratos API
- No modificar datos runtime

## VERIFICATION
- frontdesk-service:test termina con código 0
- Cualquier regresión produce un WRITE concreto

## ROLLBACK
No aplica; operación de verificación.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main
- frontdesk-service/src/test
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --no-daemon
END_VERIFY_COMMANDS
