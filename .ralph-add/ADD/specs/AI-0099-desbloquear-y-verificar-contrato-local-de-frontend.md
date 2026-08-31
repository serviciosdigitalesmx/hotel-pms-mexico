# A.SPEC AI-0099 — Desbloquear y verificar contrato local de frontend

ID: AI-0099
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0098 no produjo evidencia porque el supervisor bloqueó todos los comandos; antes de planificar correcciones se necesita confirmar el contrato real de pruebas y el estado del worktree.

## WHAT
Leer el estado del worktree y el manifiesto de frontend, identificar los scripts de test disponibles y registrar cualquier bloqueo determinista sin modificar archivos.

## SCOPE
- Estado Git sin mutación
- Scripts y dependencias declaradas en frontend/package.json
- Disponibilidad de comandos locales de frontend

## OUT OF SCOPE
- Cambios de código
- Instalación de dependencias
- Migraciones
- Cambios de secretos
- Deploy
- Ejecución de operaciones financieras

## CONTRACT
- No se modifica ningún archivo
- Se conserva íntegramente el worktree existente
- La salida debe distinguir bloqueo del supervisor de fallo real

## INVARIANTS
- No ejecutar comandos destructivos
- No limpiar, resetear ni sobrescribir cambios
- No afirmar que los tests pasan sin ejecutarlos exitosamente

## VERIFICATION
- git status --short devuelve evidencia o BLOCKED_BY_SUPERVISOR
- frontend/package.json existe y sus scripts quedan identificados
- npm --prefix frontend run devuelve el listado de scripts o un bloqueo explícito

## ROLLBACK
No aplica: A.SPEC estrictamente de lectura y sin mutaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/vite.config.*
- frontend/src
- frontend/tests
- frontend/test*
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- git status --short
- test -f frontend/package.json
- node -e "const p=require('./frontend/package.json'); console.log(JSON.stringify({scripts:p.scripts,dependencies:p.dependencies,devDependencies:p.devDependencies},null,2))"
- npm --prefix frontend run
END_VERIFY_COMMANDS
