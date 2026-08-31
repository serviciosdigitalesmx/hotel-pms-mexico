# A.SPEC AI-0084 — Diagnosticar el bloqueo determinista de tests del frontend

ID: AI-0084
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0083 no pudo verificar porque el comando npm --prefix frontend test devolvió BLOCKED_BY_SUPERVISOR. Antes de cambiar código o reintentar con supuestos, hay que identificar si el bloqueo proviene del script npm, la configuración del runner o una política local.

## WHAT
Inspeccionar de forma read-only los scripts npm, lockfiles, configuración de tests y referencias al bloqueo; conservar intacto todo el worktree y producir la causa observable y el comando correcto de verificación.

## SCOPE
- Determinar qué script ejecuta frontend test
- Identificar runner, configuración y dependencias existentes
- Localizar el origen textual o indirecto de BLOCKED_BY_SUPERVISOR
- Registrar cambios preexistentes mediante git status

## OUT OF SCOPE
- Modificar archivos
- Instalar dependencias
- Ejecutar migraciones
- Cambiar secretos o RBAC
- Deploy
- Descartar, limpiar, resetear o stashar cambios

## CONTRACT
- No inventar scripts, tests, endpoints ni dependencias
- Usar únicamente contratos y archivos existentes
- Si el bloqueo es externo al repositorio, reportarlo como bloqueador sin mutar el worktree

## INVARIANTS
- No debe haber mutaciones de fuente
- No se ejecutan operaciones destructivas
- Se preserva cualquier cambio preexistente
- La siguiente verificación debe ser reproducible localmente

## VERIFICATION
- git diff --exit-code -- frontend/package.json frontend/src frontend/test frontend/tests
- El diagnóstico identifica el script y runner reales o confirma que el bloqueo es externo
- Se documenta un comando de test existente y seguro, o el bloqueador exacto

## ROLLBACK
No aplica: esta A.SPEC es exclusivamente de lectura y no modifica archivos.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/package-lock.json
- frontend/pnpm-lock.yaml
- frontend/yarn.lock
- frontend/vitest.config.*
- frontend/jest.config.*
- frontend/src
- frontend/test
- frontend/tests
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- git status --short
- npm --prefix frontend run
- sed -n '1,240p' frontend/package.json
- rg -n "BLOCKED_BY_SUPERVISOR|test|vitest|jest|playwright" frontend/package.json frontend/.npmrc frontend/vitest.config.* frontend/jest.config.* frontend/src frontend/test frontend/tests
END_VERIFY_COMMANDS
