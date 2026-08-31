# A.SPEC AI-0096 — Desbloquear y evidenciar la verificación local base

ID: AI-0096
Mode: VERIFY
RISK: LOW

## WHY
AI-0095 falló porque el supervisor bloqueó ambos comandos; no existe evidencia técnica de fallos del producto.

## WHAT
Reintentar la verificación determinista del estado del worktree y la suite frontend, registrando la salida completa y conservando todos los cambios existentes.

## SCOPE
- Confirmar si continúa BLOCKED_BY_SUPERVISOR
- Confirmar que el worktree no fue modificado
- Ejecutar la suite frontend existente

## OUT OF SCOPE
- Modificar código
- Instalar dependencias
- Migrar o modificar bases de datos
- Cambiar secretos
- Deploy
- Resetear, limpiar, descartar o stash del worktree

## CONTRACT
- Si los comandos se ejecutan, reportar estado Git y resultado exacto de npm test
- Si continúan bloqueados, devolver BLOCKED_BY_SUPERVISOR sin declarar fallos del producto

## INVARIANTS
- Un solo writer sobre el worktree
- Source mutation none
- No operaciones destructivas o irreversibles
- No cambios de configuración ni secretos

## VERIFICATION
- git status --short antes y después debe ser idéntico
- npm --prefix frontend test debe finalizar con resultado observable o bloqueo explícito
- Conservar evidencia de exit code y salida

## ROLLBACK
No aplica: A.SPEC estrictamente no muta archivos ni estado externo.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- .
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- git status --short
- npm --prefix frontend test
END_VERIFY_COMMANDS
