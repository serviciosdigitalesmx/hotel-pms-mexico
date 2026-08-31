# A.SPEC AI-0047 — Ajustar excepciones simuladas en LocalIntentRouterTest para activar el fallback resiliente

ID: AI-0047
Mode: WRITE
RISK: LOW

## WHY
El diagnóstico AI-0046 confirmó que 9 tests fallan porque el mock del servicio AI lanza 'IllegalStateException', la cual no es reconocida por 'ResilientIntentFallbackHandler' como un fallo del proveedor AI. Actualizar el mock para que utilice una excepción soportada (como 'RetryableAiProviderException') activará el fallback determinista esperado.

## WHAT
Modificar la configuración de mock/stubbing en 'LocalIntentRouterTest.java' para lanzar 'RetryableAiProviderException' (o 'PermanentAiProviderException') en lugar de 'IllegalStateException' al simular la indisponibilidad del servicio AI.

## SCOPE
- Actualizar el setup del mock en 'LocalIntentRouterTest.java' para usar una excepción de proveedor reconocida por 'ResilientIntentFallbackHandler'.
- Ejecutar y verificar los tests unitarios de 'LocalIntentRouterTest'.

## OUT OF SCOPE
- Modificaciones al código fuente de producción ('LocalIntentRouter.java', 'ResilientIntentFallbackHandler.java').
- Cambios en la configuración de otros módulos o servicios.

## CONTRACT
- Todos los tests en 'LocalIntentRouterTest' deben pasar correctamente sin excepciones no capturadas.
- El fallback determinista debe ejecutarse limpiamente cuando el proveedor AI falla con la excepción adecuada.

## INVARIANTS
- No alterar las aserciones de negocio de procesamiento de intenciones locales.
- No modificar los tipos de excepciones manejadas por 'ResilientIntentFallbackHandler' en producción.

## VERIFICATION
- ./gradlew test --tests "*LocalIntentRouterTest"

## ROLLBACK
git checkout -- **/LocalIntentRouterTest.java

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- **/LocalIntentRouterTest.java
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew test --tests "*LocalIntentRouterTest"
END_VERIFY_COMMANDS
