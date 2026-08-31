# A.SPEC AI-0094 — Determinar el bloqueo reproducible de la suite de tests del frontend

ID: AI-0094
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0093 no verificó funcionalidad porque la ejecución fue bloqueada por el supervisor y no dejó evidencia técnica del motivo. Antes de modificar código hay que identificar el runner real, dependencias y condición exacta del bloqueo.

## WHAT
Inspeccionar únicamente la configuración existente del frontend y ejecutar la suite con el runner declarado, sin instalar, modificar, migrar ni escribir archivos.

## SCOPE
- Identificar el script test y su framework real
- Confirmar el gestor y estado de dependencias mediante comandos no mutantes
- Capturar el primer error reproducible o confirmar que la suite puede ejecutarse
- Registrar si el bloqueo es de política, entorno, dependencia o código

## OUT OF SCOPE
- Modificar package.json o lockfiles
- Instalar dependencias de forma mutante
- Cambiar tests o código de aplicación
- Migraciones de base de datos
- Cambios de secretos, RBAC o despliegue

## CONTRACT
- No se altera ningún archivo del worktree
- La salida debe distinguir BLOCKED_BY_SUPERVISOR de un fallo real de tests
- La evidencia debe incluir comando, exit code y error relevante

## INVARIANTS
- Se conserva todo el worktree preexistente
- No se descartan, limpian, resetean ni sobrescriben cambios
- No se ejecutan operaciones externas ni destructivas
- No se declara V1 completa con tests no verificables

## VERIFICATION
- La suite se ejecuta con el comando soportado por frontend/package.json o queda documentado el bloqueo exacto
- Se reporta git status --short sin modificarlo
- El resultado permite crear el siguiente A.SPEC acotado: desbloqueo técnico, corrección de tests o auditoría adicional

## ROLLBACK
No aplica: la A.SPEC es estrictamente de lectura y verificación; si algún comando intentara escribir, detenerlo y conservar el estado actual.

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
- test -f frontend/package.json && node -e "const p=require('./frontend/package.json'); console.log(JSON.stringify({scripts:p.scripts,devDependencies:p.devDependencies},null,2))"
- if test -f frontend/package-lock.json; then npm --prefix frontend ci --ignore-scripts --dry-run; fi
- npm --prefix frontend test -- --runInBand
END_VERIFY_COMMANDS
