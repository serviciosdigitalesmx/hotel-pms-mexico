# A.SPEC AI-0012 — Obtener el fallo reproducible exacto de LocalIntentRouterTest

ID: AI-0012
Mode: VERIFY
RISK: LOW

## WHY
AI-0011 falló porque la verificación quedó bloqueada y no expuso ninguna aserción concreta; repetir el mismo escaneo no aporta diagnóstico.

## WHAT
Ejecutar únicamente la suite determinista de LocalIntentRouterTest y capturar la primera causa reproducible, incluyendo expected/actual y stacktrace.

## SCOPE
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/DeterministicParser.java

## OUT OF SCOPE
- Modificar código fuente
- Modificar pruebas
- Modificar migraciones o configuración
- Usar proveedores externos de IA
- Desplegar o reiniciar servicios

## CONTRACT
- La ejecución debe usar el wrapper Gradle existente del repositorio.
- El resultado debe identificar PASS o un fallo concreto de prueba, no BLOCKED_BY_SUPERVISOR.

## INVARIANTS
- No se editan archivos fuente ni pruebas.
- No se ejecutan migraciones ni operaciones destructivas.
- La verificación permanece local y determinista.

## VERIFICATION
- Confirmar que Gradle finaliza con código de salida explícito.
- Confirmar que el reporte contiene el nombre de la prueba fallida o un resultado PASS.
- Registrar expected, actual y stacktrace si existe.

## ROLLBACK
No hay cambios de aplicación que revertir; solo pueden generarse artefactos locales de build/test.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest --stacktrace --info
END_VERIFY_COMMANDS
