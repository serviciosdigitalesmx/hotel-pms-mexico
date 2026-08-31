# A.SPEC AI-0087 — Ejecutar y clasificar la suite unitaria del frontend

ID: AI-0087
Mode: VERIFY
RISK: LOW

## WHY
AI-0086 confirmó runner, configuración, dependencias y suites disponibles, pero no verificó ejecución real.

## WHAT
Ejecutar Vitest de forma determinista y registrar resultado, fallos, errores ambientales, duración y archivos afectados.

## SCOPE
- Suite Vitest bajo frontend/src
- Configuración existente de Vite/Vitest
- Estado de dependencias ya instalado

## OUT OF SCOPE
- Modificar código, tests, configuración o lockfiles
- Ejecutar Playwright E2E
- Build, lint, coverage o deploy
- Migraciones, cambios de secretos o cambios destructivos

## CONTRACT
- El comando debe usar el script existente frontend/package.json
- No se deben inventar pruebas, fixtures, endpoints ni datos
- El resultado debe distinguir PASS, FAIL y bloqueo ambiental

## INVARIANTS
- Conservar íntegramente el worktree actual
- No limpiar, resetear, descartar, stashear ni sobrescribir cambios preexistentes
- No modificar dependencias ni lockfiles
- No ejecutar operaciones remotas o irreversibles

## VERIFICATION
- El proceso termina con resultado observable
- Se reporta el número de tests pasados y fallidos
- Cada fallo incluye archivo y causa resumida
- Se confirma que git status permanece sin cambios causados por esta A.SPEC

## ROLLBACK
No aplica: operación de solo verificación; no realizar cambios si alguna herramienta genera artefactos temporales.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
