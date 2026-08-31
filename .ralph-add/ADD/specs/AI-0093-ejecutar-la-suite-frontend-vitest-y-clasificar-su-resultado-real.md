# A.SPEC AI-0093 — Ejecutar la suite frontend Vitest y clasificar su resultado real

ID: AI-0093
Mode: VERIFY
RISK: LOW

## WHY
AI-0092 confirmó la configuración y dejó pendiente la única verificación funcional: ejecutar Vitest.

## WHAT
Ejecutar la suite frontend completa con el runner configurado y registrar salida, conteo de pruebas, duración y errores reproducibles.

## SCOPE
- Todas las suites Vitest bajo frontend/src
- Configuración Vite/Vitest existente
- Clasificación de PASS, FAIL o bloqueo ambiental

## OUT OF SCOPE
- Modificar código o configuración
- Instalar o actualizar dependencias
- Corregir pruebas
- Ejecutar E2E con Playwright
- Builds, migraciones, Docker o deploy

## CONTRACT
- Usar exactamente el script npm existente: vitest run
- No alterar archivos del worktree
- Si falla, conservar el primer error reproducible y sus ubicaciones

## INVARIANTS
- No resetear, limpiar, descartar, stashar ni sobrescribir cambios preexistentes
- No ejecutar migraciones ni operaciones destructivas
- No declarar la suite verde si el proceso no termina exitosamente

## VERIFICATION
- El comando termina con código 0 o se documenta el bloqueo/fallo real
- Registrar número de tests, suites aprobadas/fallidas y errores principales
- Comprobar git status --short antes y después para confirmar ausencia de cambios atribuibles

## ROLLBACK
No aplica: operación de verificación sin edición intencional; si Vitest genera artefactos temporales, no eliminarlos automáticamente y reportarlos.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
