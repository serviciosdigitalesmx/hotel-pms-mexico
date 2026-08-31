# A.SPEC AI-0124 — Clasificar los tres archivos restantes con fallos Vitest

ID: AI-0124
Mode: READ_ONLY
RISK: LOW

## WHY
El checkpoint confirma 4 archivos fallidos y 21 assertions, pero el reporte disponible solo expone claramente Stays.test.tsx. Se necesita extraer los otros nombres y trazas antes de programar reparaciones, evitando cambios ciegos.

## WHAT
Leer y clasificar las trazas ya capturadas para identificar los tres archivos restantes, separar fallos de assertions de problemas de cierre de Vite y proponer una A.SPEC mínima por causa.

## SCOPE
- Extraer nombres exactos de archivos y tests fallidos
- Relacionar cada fallo con su línea y expectativa
- Determinar si existe un problema común de setup o aislamiento
- Dejar preparado el siguiente trabajo acotado

## OUT OF SCOPE
- Modificar código o tests
- Repetir la suite completa
- Instalar dependencias
- Cambios de Vite, configuración o infraestructura
- Migraciones, secretos, RBAC, deploy o servicios

## CONTRACT
- Clasificar únicamente con evidencia del reporte existente
- No declarar solucionado un fallo no reproducido
- No atribuir los fallos al cierre tardío de Vite sin evidencia causal

## INVARIANTS
- Source mutation none
- Preservar íntegramente el worktree
- No ejecutar comandos destructivos ni externos

## VERIFICATION
- Obtener los cuatro archivos fallidos o documentar por qué el reporte está incompleto
- Registrar los nombres exactos de las 21 assertions
- Separar product failure, test-contract mismatch y runner shutdown warning

## ROLLBACK
No aplica: A.SPEC de solo lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- .ralph-add/REPORTS/HV-checkpoint-batch-4-20260822-052836.md
- frontend/src
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n "FAIL|Test Files|Tests|AssertionError|TestingLibraryElementError" .ralph-add/REPORTS/HV-checkpoint-batch-4-20260822-052836.md
- rg -n "src/.*\.test\.tsx|src/.*\.test\.ts" .ralph-add/REPORTS/HV-checkpoint-batch-4-20260822-052836.md
END_VERIFY_COMMANDS
