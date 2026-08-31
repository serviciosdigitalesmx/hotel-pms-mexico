# A.SPEC AI-0098 — Ejecutar y verificar la suite real de frontend

ID: AI-0098
Mode: VERIFY
RISK: LOW

## WHY
AI-0097 confirmó que AI-0096 fue bloqueada por el supervisor y no existe evidencia técnica del estado de Vitest.

## WHAT
Ejecutar la suite frontend autorizada, registrar exit code, resumen de Vitest, primer fallo reproducible y comparar el estado Git antes y después.

## SCOPE
- Verificación read-only de la suite frontend
- Confirmación de que el worktree permanece sin cambios causados por la prueba

## OUT OF SCOPE
- Modificar código
- Instalar dependencias
- Iniciar servicios
- Migraciones de base de datos
- Cambios de secretos
- Deploys
- Operaciones destructivas

## CONTRACT
- El comando npm --prefix frontend test debe ejecutarse literalmente y registrar su resultado real.
- El resultado debe distinguir PASS, FAIL técnico o bloqueo del supervisor.

## INVARIANTS
- Preservar todo el worktree existente.
- No modificar archivos del repositorio.
- No alterar Git, base de datos, Docker ni secretos.

## VERIFICATION
- Exit code real del comando de tests.
- Resumen de tests aprobados, fallidos y omitidos.
- Primer error reproducible, si existe.
- Comparación exacta de git status --short antes y después.

## ROLLBACK
No aplica: la A.SPEC no debe producir cambios persistentes.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- .
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- git status --short
- npm --prefix frontend test
- git status --short
END_VERIFY_COMMANDS
