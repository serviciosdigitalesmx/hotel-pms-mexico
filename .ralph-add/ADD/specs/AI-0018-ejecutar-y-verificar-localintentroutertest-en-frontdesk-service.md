# A.SPEC AI-0018 — Ejecutar y verificar LocalIntentRouterTest en frontdesk-service

ID: AI-0018
Mode: VERIFY
RISK: LOW

## WHY
AI-0017 identificó con precisión que LocalIntentRouter y LocalIntentRouterTest se encuentran en frontdesk-service. Se debe validar de forma determinista el estado de ejecución y resultado de esta suite de pruebas.

## WHAT
Ejecutar las pruebas unitarias de LocalIntentRouterTest en el módulo frontdesk-service utilizando Gradle para evaluar el comportamiento actual del router de intenciones de IA.

## SCOPE
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java
- Ejecución de test mediante Gradle en frontdesk-service

## OUT OF SCOPE
- Modificación de archivos fuente de Java o configuración de Gradle
- Ejecución de tests en módulos ajenos a frontdesk-service
- Cambios en bases de datos, Redis, Docker o secretos

## CONTRACT
- No alterar el estado de Git ni código en el repositorio
- Obtener la evidencia limpia de compilación y resultados de JUnit para LocalIntentRouterTest

## INVARIANTS
- El repositorio permanece inalterado
- Sin efectos secundarios en entorno ni dependencias externas

## VERIFICATION
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest

## ROLLBACK
No se requiere rollback al ser un paso de modo VERIFY sin modificaciones en el código fuente.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests com.hotelpms.frontdesk.assistant.engine.LocalIntentRouterTest
END_VERIFY_COMMANDS
