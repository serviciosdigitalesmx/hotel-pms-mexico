# A.SPEC AI-0091 — Ejecutar y registrar la suite unitaria frontend

ID: AI-0091
Mode: VERIFY
RISK: LOW

## WHY
La auditoría AI-0090 confirmó la configuración y detectó que el estado funcional de la suite permanece desconocido.

## WHAT
Ejecutar Vitest con el runner determinista existente y registrar resultado, errores, duración y conteo de pruebas.

## SCOPE
- Suite unitaria e integración bajo frontend/src
- Detección de fallos de configuración, compilación o runtime
- Confirmación del código de salida del runner

## OUT OF SCOPE
- Cambios de código
- Instalación o actualización de dependencias
- Cobertura
- Playwright E2E
- Migraciones
- Docker
- Despliegues
- Modificación del worktree

## CONTRACT
- El comando debe ser exactamente npm --prefix frontend test
- Debe conservarse intacto todo el worktree existente
- El resultado debe distinguir PASS, FAIL o BLOCKED

## INVARIANTS
- No editar, eliminar, resetear, limpiar ni sobrescribir archivos
- No modificar package-lock.json ni configuración
- No ejecutar operaciones externas o destructivas

## VERIFICATION
- Código de salida 0 implica suite ejecutada correctamente
- Código distinto de 0 implica registrar los fallos exactos
- Si la ejecución no puede iniciar, declarar BLOCKED con causa verificable

## ROLLBACK
No aplica: VERIFY sin escrituras.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
