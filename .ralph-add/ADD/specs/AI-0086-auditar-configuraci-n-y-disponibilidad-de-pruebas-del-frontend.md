# A.SPEC AI-0086 — Auditar configuración y disponibilidad de pruebas del frontend

ID: AI-0086
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0085 no verificó nada porque la ejecución fue bloqueada por el supervisor. Antes de solicitar otra ejecución, hay que determinar el runner real, los tests existentes y si las dependencias están instaladas.

## WHAT
Inspeccionar únicamente la configuración declarada del frontend, localizar suites de pruebas y comprobar el estado local de dependencias, sin modificar archivos ni instalar paquetes.

## SCOPE
- Identificar el comando de test correcto
- Inventariar archivos de pruebas existentes
- Detectar dependencias ausentes o inconsistencias del lockfile
- Registrar bloqueadores reproducibles para la siguiente verificación

## OUT OF SCOPE
- Modificar código o configuración
- Instalar o actualizar dependencias
- Ejecutar migraciones
- Cambiar secretos, RBAC o datos
- Deploy o cambios remotos

## CONTRACT
- No debe producir mutaciones en el worktree
- Debe conservar el runner y lockfile existentes
- La salida debe permitir definir un comando de verificación determinista posterior

## INVARIANTS
- No limpiar, resetear, descartar ni sobrescribir trabajo preexistente
- No ejecutar operaciones destructivas
- No afirmar que los tests pasan sin ejecución efectiva

## VERIFICATION
- Comparar git status --short antes y después
- Confirmar que el comando correcto y las suites existentes quedaron identificados
- Clasificar explícitamente cualquier dependencia o permiso faltante como bloqueador

## ROLLBACK
No aplica: la A.SPEC es estrictamente de lectura y no modifica el worktree.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/package-lock.json
- frontend/pnpm-lock.yaml
- frontend/yarn.lock
- frontend/vitest.config.*
- frontend/jest.config.*
- frontend/src
- frontend/tests
- frontend/__tests__
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- test -f frontend/package.json && node -e "const p=require('./frontend/package.json'); console.log(JSON.stringify({scripts:p.scripts,devDependencies:p.devDependencies,dependencies:p.dependencies},null,2))"
- rg --files frontend | rg '(^|/)(test|tests|__tests__|.*\.(test|spec)\.).*'
- npm --prefix frontend ls --depth=0
END_VERIFY_COMMANDS
