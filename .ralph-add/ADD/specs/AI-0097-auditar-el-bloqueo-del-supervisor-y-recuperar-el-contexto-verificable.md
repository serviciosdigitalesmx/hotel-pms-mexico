# A.SPEC AI-0097 — Auditar el bloqueo del supervisor y recuperar el contexto verificable

ID: AI-0097
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0096 no pudo ejecutar ninguna verificación porque el entorno devolvió BLOCKED_BY_SUPERVISOR. Antes de continuar hay que identificar la causa y el mecanismo de reanudación sin modificar el worktree.

## WHAT
Inspeccionar únicamente registros, configuración y artefactos de planificación existentes para determinar el bloqueo, conservar el worktree intacto y producir el siguiente paso verificable.

## SCOPE
- Revisar el estado Git sin mutaciones
- Localizar evidencia del bloqueo del supervisor
- Revisar el contexto de AI-0096 y A.SPECs relacionadas
- Determinar si existe una aprobación o desbloqueo pendiente

## OUT OF SCOPE
- Cambios en código o configuración
- Instalación de dependencias
- Migraciones de base de datos
- Cambios de secretos, RBAC o datos financieros
- Deploy o acciones destructivas

## CONTRACT
- No se modifica ningún archivo
- No se descarta, limpia, resetea ni sobrescribe trabajo existente
- La salida debe identificar el bloqueo o confirmar que no hay evidencia local suficiente

## INVARIANTS
- El worktree permanece exactamente sin mutaciones
- No se ejecutan operaciones irreversibles
- La verificación no depende de credenciales ni servicios externos

## VERIFICATION
- Los comandos terminan sin escribir archivos
- Se registra el estado Git previo y posterior sin diferencias causadas por el A.SPEC
- Se identifica una causa concreta o se documenta el bloqueo como externo al worktree

## ROLLBACK
No aplica: este A.SPEC es estrictamente de lectura y no debe producir cambios.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- .add
- .argos
- docs
- .github
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- pwd
- git status --short
- rg -n "BLOCKED_BY_SUPERVISOR|AI-0096|supervisor|approval" .add .argos docs .github
END_VERIFY_COMMANDS
