# A.SPEC AI-0080 — Auditar y verificar el contrato local de pruebas del frontend

ID: AI-0080
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0079 no pudo verificar porque npm test fue bloqueado por el supervisor; antes de reintentar hay que identificar el runner y el comando real configurado, sin modificar el worktree.

## WHAT
Inspeccionar package.json, lockfiles, configuración de tests y estructura de suites para determinar el comando determinista correcto y documentar cualquier hueco o bloqueo observable.

## SCOPE
- Leer scripts npm del frontend
- Identificar Jest, Vitest, Playwright u otro runner existente
- Localizar suites unitarias, integración y smoke existentes
- Comprobar si las dependencias instaladas permiten ejecutar el runner

## OUT OF SCOPE
- Modificar archivos
- Instalar dependencias
- Ejecutar migraciones
- Levantar servicios
- Cambiar secretos o configuración
- Cambiar RBAC
- Deploy

## CONTRACT
- No se modifica ningún archivo del worktree
- La salida identifica exactamente el runner y el comando de test disponible, o reporta ausencia/bloqueo

## INVARIANTS
- Se conserva todo el trabajo preexistente
- No se limpian, resetean, descartan ni sobrescriben archivos
- No se realizan operaciones externas ni destructivas

## VERIFICATION
- Los comandos son de solo lectura y terminan con resultado determinista
- Se registra el script de pruebas encontrado y la causa concreta si no puede ejecutarse
- El siguiente A.SPEC utilizará únicamente el comando confirmado

## ROLLBACK
No aplica: este paso no muta el worktree.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/package-lock.json
- frontend/pnpm-lock.yaml
- frontend/yarn.lock
- frontend/src
- frontend/test
- frontend/tests
- frontend/vitest.config.*
- frontend/jest.config.*
- frontend/playwright.config.*
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- test -f frontend/package.json
- node -e "const p=require('./frontend/package.json'); console.log(JSON.stringify({scripts:p.scripts,devDependencies:p.devDependencies,dependencies:p.dependencies},null,2))"
- npm --prefix frontend run
END_VERIFY_COMMANDS
