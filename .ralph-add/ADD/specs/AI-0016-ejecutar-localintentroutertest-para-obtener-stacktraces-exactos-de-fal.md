# A.SPEC AI-0016 — Ejecutar LocalIntentRouterTest para obtener stacktraces exactos de fallos JUnit

ID: AI-0016
Mode: VERIFY
RISK: LOW

## WHY
AI-0015 identificó estáticamente los motivos de fallo en LocalIntentRouterTest, pero es necesario obtener la salida exacta de la suite de pruebas JUnit y los stacktraces completos para verificar los tres fallos reportados antes de aplicar un fix en código.

## WHAT
Ejecutar la suite de pruebas unitarias para LocalIntentRouterTest mediante el ejecutor de Gradle del repositorio y capturar los informes de fallo.

## SCOPE
- Ejecución determinista de ./gradlew test para el filtro LocalIntentRouterTest.

## OUT OF SCOPE
- Modificación de archivos de código Java o tests.
- Alteración de esquemas de BD, Redis, secretos o Docker.

## CONTRACT
- Se ejecuta la prueba unitaria sin editar ningún archivo del repositorio.
- Cualquier fallo en la prueba sirve como evidencia estructurada para la siguiente A.SPEC.

## INVARIANTS
- El estado del código fuente permanezca inalterado.

## VERIFICATION
- El comando Gradle finaliza informando detalladamente la cantidad de tests ejecutados, fallados y los stacktraces asociados a LocalIntentRouterTest.

## ROLLBACK
No aplica para modo VERIFY al no existir mutaciones de estado.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew test --tests "*LocalIntentRouterTest*"
END_VERIFY_COMMANDS
