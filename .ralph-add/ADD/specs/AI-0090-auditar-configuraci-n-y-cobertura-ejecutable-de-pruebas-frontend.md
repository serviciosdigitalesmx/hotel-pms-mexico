# A.SPEC AI-0090 — Auditar configuración y cobertura ejecutable de pruebas frontend

ID: AI-0090
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0089 no pudo verificar porque npm --prefix frontend test fue bloqueado por el supervisor. Antes de reintentar ejecución, hay que determinar el runner real, sus scripts y si existen pruebas ejecutables.

## WHAT
Inspeccionar únicamente la configuración y los archivos de prueba existentes en frontend, identificar el comando determinista correcto y documentar bloqueadores de ejecución sin modificar archivos.

## SCOPE
- Leer frontend/package.json y configuraciones de test existentes
- Localizar suites unitarias, integración y smoke del frontend
- Determinar dependencias o scripts faltantes que impidan ejecutar pruebas

## OUT OF SCOPE
- Modificar código o configuración
- Instalar dependencias
- Ejecutar migraciones o Docker
- Cambiar secretos, RBAC o datos
- Deploy

## CONTRACT
- No se altera ningún archivo del worktree
- Se reutilizan exclusivamente scripts, runners y suites existentes
- Toda conclusión se basa en evidencia local

## INVARIANTS
- El worktree permanece intacto
- No se ejecutan operaciones destructivas ni de escritura
- No se inventan suites, endpoints ni contratos

## VERIFICATION
- El comando de inspección termina con evidencia del runner y scripts disponibles
- Se reportan rutas reales de pruebas encontradas
- Si falta configuración o ejecución está bloqueada, se registra como bloqueador explícito

## ROLLBACK
No aplica: A.SPEC completamente de solo lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/src
- frontend/test
- frontend/tests
- frontend/vitest.config.*
- frontend/jest.config.*
- frontend/playwright.config.*
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- test -f frontend/package.json && node -e "const p=require('./frontend/package.json'); console.log(JSON.stringify({scripts:p.scripts,devDependencies:p.devDependencies,dependencies:p.dependencies},null,2))"
- rg -n "(vitest|jest|playwright|cypress|test\(|describe\(|it\()" frontend --glob '!frontend/node_modules/**' --glob '!frontend/dist/**'
END_VERIFY_COMMANDS
