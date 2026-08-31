# A.SPEC AI-0105 — Verificar build TypeScript y Vite del frontend

ID: AI-0105
Mode: VERIFY
RISK: LOW

## WHY
La suite Vitest continúa bloqueada por el supervisor; el build es una frontera independiente y observable para detectar errores de compilación en los cambios actuales.

## WHAT
Ejecutar el build real del frontend y clasificar PASS, FAIL o BLOCKED con código de salida y primer error reproducible.

## SCOPE
- frontend/package.json
- frontend/src
- frontend/vite.config.*
- frontend/tsconfig*.json

## OUT OF SCOPE
- Cambios de código
- instalación de dependencias
- Playwright
- backend
- deploy
- migraciones
- secretos

## CONTRACT
- Usar el script build existente
- No afirmar PASS sin código de salida cero
- Preservar el worktree completo

## INVARIANTS
- No resetear, limpiar, stashar ni descartar cambios
- No modificar contratos ni dependencias
- No imprimir secretos

## VERIFICATION
- Registrar exit code
- Registrar resultado de tsc y Vite
- Distinguir fallo técnico de bloqueo del entorno

## ROLLBACK
No aplica; verificación sin cambios intencionales.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend run build
END_VERIFY_COMMANDS
