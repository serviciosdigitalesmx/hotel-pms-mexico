# A.SPEC AI-0013 — Aislar las pruebas del modo AI-first ambiental

ID: AI-0013
Mode: WRITE
RISK: LOW

## WHY
La prueba falla porque ASSISTANT_AI_FIRST queda activo por defecto y el mock de AssistantService devuelve null; los flujos locales no llegan al router determinista.

## WHAT
Ajustar únicamente el fixture de LocalIntentRouterTest para simular explícitamente la indisponibilidad del proveedor IA durante los flujos locales y conservar una respuesta controlada para el caso de fallback Groq.

## SCOPE
- Modificar el setup de LocalIntentRouterTest
- Mantener la cobertura de fallback y de los flujos deterministas

## OUT OF SCOPE
- Cambios en LocalIntentRouter
- Cambios de configuración de producción
- Cambios de contratos, endpoints o persistencia

## CONTRACT
- Los flujos locales deben ejecutarse cuando el proveedor IA falla
- El mensaje desconocido debe devolver sin cambios la respuesta del AssistantService

## INVARIANTS
- No se modifican archivos de producción
- No se realizan llamadas reales a servicios externos
- No se relaja Mockito globalmente ni se ocultan interacciones inválidas

## VERIFICATION
- La clase LocalIntentRouterTest termina con exit 0
- Los diez casos reportan cero fallos
- La prueba unknownMessageUsesGroqFallbackUnchanged conserva identidad de respuesta

## ROLLBACK
Revertir únicamente el cambio en LocalIntentRouterTest.java.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest
END_VERIFY_COMMANDS
