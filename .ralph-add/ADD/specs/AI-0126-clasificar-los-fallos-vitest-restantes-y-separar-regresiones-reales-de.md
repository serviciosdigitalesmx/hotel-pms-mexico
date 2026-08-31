# A.SPEC AI-0126 — Clasificar los fallos Vitest restantes y separar regresiones reales de problemas del runner

ID: AI-0126
Mode: READ_ONLY
RISK: LOW

## WHY
El checkpoint mantiene 4 archivos fallidos y 21 assertions, pero sólo expone trazas completas de Stays.test.tsx. AI-0125 no dejó la suite verde, por lo que se requiere evidencia precisa antes de otra reparación.

## WHAT
Obtener los cuatro archivos fallidos, registrar las 21 assertions, clasificar cada causa y cerrar el alcance de la siguiente reparación mínima.

## SCOPE
- Reporte checkpoint actual
- Tests y componentes frontend relacionados
- Setup y aislamiento de Vitest
- Advertencia de cierre tardío de Vite

## OUT OF SCOPE
- Modificar código o tests
- Actualizar dependencias
- Cambiar Vite o infraestructura
- Migraciones Flyway
- Secretos, RBAC, deploy o reinicios

## CONTRACT
- No se muta ningún archivo del worktree
- No se declara solucionado ningún fallo no reproducido
- Cada fallo queda asociado a archivo, test, línea y causa
- Los warnings del runner se separan de fallos funcionales

## INVARIANTS
- Se conserva íntegramente el worktree actual
- El build frontend exitoso permanece como evidencia independiente
- No se editan migraciones ni configuraciones sensibles
- No se ejecutan operaciones remotas ni destructivas

## VERIFICATION
- Los cuatro archivos fallidos quedan identificados o se documenta la ausencia de evidencia
- Las 21 assertions quedan clasificadas
- Se determina si Stays sigue fallando después de AI-0125
- Se define una A.SPEC de reparación mínima por causa

## ROLLBACK
No aplica; operación estrictamente read-only.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- .ralph-add/REPORTS/HV-checkpoint-batch-4-20260822-052836.md
- frontend/src
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n "FAIL|Test Files|Tests|AssertionError|TestingLibraryElementError|src/.*\.test\.(tsx|ts)" .ralph-add/REPORTS/HV-checkpoint-batch-4-20260822-052836.md
- npm --prefix frontend test -- --run --reporter=verbose
- npm --prefix frontend test -- --run src/pages/Stays.test.tsx --reporter=verbose
- git diff --check
END_VERIFY_COMMANDS
