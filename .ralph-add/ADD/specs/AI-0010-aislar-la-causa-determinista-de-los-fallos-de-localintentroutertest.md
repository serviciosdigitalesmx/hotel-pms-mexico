# A.SPEC AI-0010 — Aislar la causa determinista de los fallos de LocalIntentRouterTest

ID: AI-0010
Mode: VERIFY
RISK: LOW

## WHY
AI-0009 falló con 10 errores concentrados en LocalIntentRouterTest: ocho NullPointerException y un UnnecessaryStubbingException. Repetir la suite completa no aporta evidencia suficiente para una corrección segura.

## WHAT
Ejecutar únicamente LocalIntentRouterTest con stack traces completos para identificar la dependencia o fixture nulo y separar el problema de configuración Mockito del comportamiento funcional.

## SCOPE
- frontdesk-service/src/test
- frontdesk-service/src/main relacionado con LocalIntentRouter

## OUT OF SCOPE
- Modificar código fuente o tests
- Cambiar contratos de IA
- Cambiar base de datos, secretos o configuración de producción
- Repetir la suite completa

## CONTRACT
- La verificación debe revelar las líneas de producción o fixture responsables de cada familia de fallos
- No se deben alterar archivos fuente

## INVARIANTS
- No crear ni modificar endpoints, DTOs, tablas o datos
- No seleccionar automáticamente huéspedes o habitaciones
- No ejecutar acciones externas ni mutaciones de producción

## VERIFICATION
- El comando termina y registra stack traces para LocalIntentRouterTest
- Se identifica una causa concreta antes de proponer WRITE
- El resultado conserva el estado del workspace

## ROLLBACK
No aplica: paso de verificación sin cambios intencionales en el código.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --tests '*LocalIntentRouterTest' --no-daemon --stacktrace
END_VERIFY_COMMANDS
