# A.SPEC AI-0025 — Diagnosticar fallo de LocalIntentRouterTest en frontdesk-service

ID: AI-0025
Mode: VERIFY
RISK: LOW

## WHY
AI-0024 resultó en VERIFY_FAIL al modificar LocalIntentRouterTest. Se requiere ejecutar la prueba de forma determinista para capturar las trazas exactas de fallo antes de aplicar la reparación.

## WHAT
Ejecutar la suite de pruebas unitarias de LocalIntentRouterTest en el módulo frontdesk-service para evaluar los fallos de aserción.

## SCOPE
- Ejecución del comando Gradle test para la clase LocalIntentRouterTest

## OUT OF SCOPE
- Modificación de código fuente de aplicación o tests
- Ejecución de otros módulos o servicios de infraestructura

## CONTRACT
- Obtener evidencia precisa del error de prueba sin alterar ningún archivo del repositorio

## INVARIANTS
- No se modifican ni editan archivos en el repositorio

## VERIFICATION
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest

## ROLLBACK
No requiere rollback al tratarse de una verificación que no modifica el estado del código.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest
END_VERIFY_COMMANDS
