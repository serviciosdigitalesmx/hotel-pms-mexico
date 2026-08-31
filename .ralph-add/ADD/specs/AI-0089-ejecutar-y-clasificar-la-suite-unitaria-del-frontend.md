# A.SPEC AI-0089 — Ejecutar y clasificar la suite unitaria del frontend

ID: AI-0089
Mode: VERIFY
RISK: LOW

## WHY
AI-0088 confirmó la configuración y existencia de tests, pero el resultado real de la suite sigue siendo UNKNOWN.

## WHAT
Ejecutar Vitest con el runner oficial y registrar resultado, errores, duración y cualquier bloqueo ambiental o de dependencias.

## SCOPE
- Suite unitaria e integración bajo frontend/src
- Configuración efectiva de Vitest
- Clasificación de tests PASS, FAIL o BLOCKED

## OUT OF SCOPE
- Modificar código o tests
- Instalar o actualizar dependencias
- Ejecutar Playwright E2E
- Modificar Docker, base de datos, secretos o Git
- Desplegar

## CONTRACT
- La fuente de verdad es frontend/package.json
- El comando autorizado es npm --prefix frontend test
- El resultado debe distinguir suite verde de ejecución bloqueada

## INVARIANTS
- Conservar íntegramente el worktree actual
- No escribir archivos ni alterar package-lock.json
- No ejecutar migraciones ni operaciones destructivas

## VERIFICATION
- El comando termina con código 0, o se documenta el fallo exacto
- Se reporta el número de tests ejecutados y fallidos
- Se conserva evidencia textual del resultado
- Se inspecciona git status --short después de la ejecución

## ROLLBACK
No aplica: A.SPEC de verificación sin cambios persistentes.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
