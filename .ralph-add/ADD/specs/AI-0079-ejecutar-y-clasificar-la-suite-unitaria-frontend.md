# A.SPEC AI-0079 — Ejecutar y clasificar la suite unitaria frontend

ID: AI-0079
Mode: VERIFY
RISK: LOW

## WHY
AI-0078 confirmó el comando real, pero la suite aún no tiene ejecución verificable en el estado actual.

## WHAT
Ejecutar Vitest una vez, capturar código de salida, pruebas fallidas, errores de configuración y duración; clasificar el resultado como PASS, FAIL o BLOCKED.

## SCOPE
- Pruebas unitarias y de componentes frontend
- Errores de dependencias, configuración, entorno jsdom y cobertura no involucrada
- Registro reproducible del resultado

## OUT OF SCOPE
- Modificar código o dependencias
- Ejecutar E2E con Playwright
- Cambiar configuración de Vitest
- Modificar base de datos, secretos o worktree

## CONTRACT
- El comando debe ejecutarse desde el checkout actual mediante npm --prefix frontend test.
- Debe conservarse íntegramente el worktree preexistente.
- Un fallo de pruebas debe reportarse como FAIL, no corregirse dentro de esta A.SPEC.

## INVARIANTS
- No se modifican archivos del repositorio.
- No se limpian, resetean, descartan ni stashean cambios.
- No se declara V1 estable basándose únicamente en la existencia del runner.

## VERIFICATION
- Código de salida del comando.
- Resumen de tests passed, failed, skipped y errores.
- Confirmación de que git status --short no presenta cambios causados por la ejecución.
- Clasificación determinista PASS, FAIL o BLOCKED con evidencia.

## ROLLBACK
No aplica: operación de verificación sin escritura intencional; conservar cualquier artefacto temporal fuera del worktree.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
