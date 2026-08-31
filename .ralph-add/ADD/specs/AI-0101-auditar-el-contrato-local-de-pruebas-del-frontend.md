# A.SPEC AI-0101 — Auditar el contrato local de pruebas del frontend

ID: AI-0101
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0100 quedó bloqueado por BLOCKED_BY_SUPERVISOR y no existe evidencia de que el runner haya iniciado. Hay que identificar el entrypoint y contrato real de pruebas antes de reintentar o modificar nada.

## WHAT
Leer los scripts npm, configuración del runner, dependencias y estructura de tests del frontend; determinar el comando mínimo reproducible y clasificar cualquier bloqueo como configuración, dependencia o supervisor.

## SCOPE
- Inspeccionar package.json y configuraciones existentes
- Localizar tests unitarios, integración y smoke ya presentes
- Registrar el comando de verificación correcto y sus prerrequisitos
- No modificar archivos ni instalar dependencias

## OUT OF SCOPE
- Cambios de código
- Cambios de dependencias
- Migraciones de base de datos
- Cambios de secretos o RBAC
- Deploy
- Ejecución de operaciones financieras

## CONTRACT
- Debe conservarse íntegramente el worktree actual
- La salida debe distinguir bloqueo de supervisor de fallo técnico real
- No se inventan scripts, tests, endpoints ni módulos

## INVARIANTS
- Source mutation: none
- No se limpian, resetean, descartan ni stashéan cambios
- Solo lectura dentro de los paths permitidos
- El backend y los servicios no se modifican

## VERIFICATION
- Confirmar que el comando npm de pruebas está definido en frontend/package.json
- Confirmar qué runner y archivos de configuración existen
- Confirmar al menos una ruta real de tests o documentar que no existe
- Producir un comando determinista para el siguiente VERIFY

## ROLLBACK
No aplica: A.SPEC estrictamente de solo lectura y sin mutaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/vitest.config.*
- frontend/src
- frontend/tests
- frontend/test
- frontend/tsconfig*.json
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n '"test"|vitest|jest|playwright|cypress' frontend/package.json frontend/vitest.config.* frontend/tsconfig*.json frontend/src frontend/tests frontend/test 2>/dev/null
- npm --prefix frontend test -- --help
END_VERIFY_COMMANDS
