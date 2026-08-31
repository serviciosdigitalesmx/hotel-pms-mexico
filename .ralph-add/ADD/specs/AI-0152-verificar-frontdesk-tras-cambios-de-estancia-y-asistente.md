# A.SPEC AI-0152 — Verificar frontdesk tras cambios de estancia y asistente

ID: AI-0152
Mode: VERIFY
RISK: LOW

## WHY
El servicio ya tiene cambios recientes y healthcheck previo; falta cerrar la evidencia de pruebas.

## WHAT
Ejecutar pruebas focalizadas de estancias, checkout y routing determinista del asistente.

## SCOPE
- Estancias
- Checkout
- LocalIntentRouter
- Persistencia de sesión

## OUT OF SCOPE
- Migraciones
- Proveedor externo de IA
- Producción

## CONTRACT
- Conservar ConversationSessionStore y rutas actuales

## INVARIANTS
- Checkout conserva estados válidos
- Routing determinista
- No credenciales inventadas

## VERIFICATION
- Pruebas frontdesk verdes o fallo reproducible mínimo

## ROLLBACK
No requiere rollback; verificación sin mutaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src
- frontdesk-service/build.gradle
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --no-daemon
END_VERIFY_COMMANDS
