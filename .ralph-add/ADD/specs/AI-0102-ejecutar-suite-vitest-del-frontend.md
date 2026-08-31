# A.SPEC AI-0102 — Ejecutar suite Vitest del frontend

ID: AI-0102
Mode: VERIFY
RISK: LOW

## WHY
AI-0101 confirmó que el runner, dependencias y contrato npm están disponibles; falta ejecutar la suite real.

## WHAT
Ejecutar los 89 tests Vitest existentes y clasificar resultados por fallo funcional, configuración o infraestructura.

## SCOPE
- frontend/src/**/*.test.ts
- frontend/src/**/*.test.tsx
- frontend/package.json

## OUT OF SCOPE
- Cambios de código
- Playwright
- backend
- deploy
- migraciones
- secretos

## CONTRACT
- El comando debe usar el script test existente
- No modificar archivos fuente ni configuración

## INVARIANTS
- Preservar todos los cambios y archivos no versionados del worktree
- No ejecutar operaciones destructivas

## VERIFICATION
- Exit code del comando
- Resumen de tests passed/failed/skipped
- Clasificación de cualquier bloqueo

## ROLLBACK
No aplica; operación de solo verificación.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test -- --run
END_VERIFY_COMMANDS
