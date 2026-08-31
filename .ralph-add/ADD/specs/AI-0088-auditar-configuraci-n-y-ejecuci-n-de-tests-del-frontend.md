# A.SPEC AI-0088 — Auditar configuración y ejecución de tests del frontend

ID: AI-0088
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0087 no verificó el código porque la ejecución de npm test fue bloqueada por el supervisor. Antes de proponer correcciones o una nueva verificación, hay que determinar el runner real, los tests existentes y si el bloqueo es de configuración, permisos o política.

## WHAT
Inspeccionar únicamente la configuración del frontend, los scripts declarados y la ubicación de tests; identificar el comando local determinista correcto y cualquier bloqueo reproducible, sin modificar archivos ni ejecutar migraciones, servicios o despliegues.

## SCOPE
- frontend/package.json
- archivos de configuración de runners existentes
- tests existentes del frontend
- scripts npm relacionados con test

## OUT OF SCOPE
- cambios de código
- instalación o actualización de dependencias
- migraciones Flyway
- cambios de secretos o RBAC
- arranque de Docker o servicios
- deployments
- eliminación, reset, stash o sobrescritura del worktree

## CONTRACT
- La auditoría debe reportar el runner y comando realmente definidos por el repositorio.
- No se deben inventar tests, scripts, dependencias ni capacidades inexistentes.
- La fuente de verdad será la configuración actualmente presente en el worktree.

## INVARIANTS
- No mutar ningún archivo.
- Conservar intacto todo el worktree existente.
- No ejecutar operaciones irreversibles ni de alto riesgo.
- Distinguir bloqueo del supervisor de fallo real de tests.

## VERIFICATION
- Confirmar que frontend/package.json existe y contiene scripts reproducibles.
- Enumerar archivos de tests/configuración encontrados.
- Registrar el comando recomendado para la siguiente A.SPEC y la causa exacta del bloqueo si puede determinarse.

## ROLLBACK
No aplica: esta A.SPEC es estrictamente de lectura y no produce cambios.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/package-lock.json
- frontend/jest.config.*
- frontend/vitest.config.*
- frontend/src
- frontend/tests
- frontend/__tests__
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- test -f frontend/package.json && node -e "const p=require('./frontend/package.json'); console.log(JSON.stringify({scripts:p.scripts,devDependencies:p.devDependencies},null,2))"
- rg --files frontend | rg '(^|/)(.*test.*|.*spec.*|jest|vitest|playwright|cypress)' | sort
- rg -n 'BLOCKED_BY_SUPERVISOR|testEnvironment|setupFiles|coverage|vitest|jest|playwright|cypress' frontend/package.json frontend 2>/dev/null | head -200
END_VERIFY_COMMANDS
