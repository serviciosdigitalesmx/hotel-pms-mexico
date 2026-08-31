# A.SPEC AI-0092 — Inspeccionar bloqueo determinista de pruebas frontend

ID: AI-0092
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0091 no pudo verificar porque el supervisor bloqueó npm --prefix frontend test; antes de cambiar nada hay que identificar el runner, scripts y causa observable del bloqueo.

## WHAT
Auditar únicamente la configuración existente de pruebas frontend y registrar el comando correcto y cualquier prerrequisito local verificable.

## SCOPE
- Leer scripts de package.json
- Localizar configuración y suites de tests existentes
- Confirmar estado del worktree sin modificarlo
- Determinar si el bloqueo proviene del script, dependencias o política del supervisor

## OUT OF SCOPE
- Modificar código o configuración
- Instalar dependencias
- Ejecutar migraciones
- Cambiar secretos o RBAC
- Desplegar
- Descartar, limpiar, resetear o sobrescribir trabajo existente

## CONTRACT
- No se modifica ningún archivo
- Se conserva íntegramente el worktree actual
- La salida debe identificar un siguiente comando de verificación reproducible o un bloqueo explícito con evidencia

## INVARIANTS
- No hay mutaciones de fuente
- No hay cambios en base de datos
- No hay operaciones financieras
- No se ejecutan acciones irreversibles

## VERIFICATION
- El contenido de package.json y archivos de configuración queda revisado
- Se documenta el runner y script de test existente
- git status --short confirma que el diagnóstico no alteró el worktree

## ROLLBACK
No aplica: A.SPEC de solo lectura; si se generara algún archivo temporal fuera del repositorio, eliminarlo sin tocar archivos existentes.

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
- frontend/test
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- test -f frontend/package.json && sed -n '1,220p' frontend/package.json
- find frontend -maxdepth 3 -type f \( -name '*vitest*' -o -name '*jest*' -o -name '*test*' \) -print | sort
- git status --short
END_VERIFY_COMMANDS
