# A.SPEC AI-0001 — Delimitar cambios preexistentes de la integración AI

ID: AI-0001
Mode: READ_ONLY
RISK: LOW

## WHY
El worktree contiene cambios y archivos AI no comprometidos; cualquier implementación resiliente podría sobrescribir trabajo existente o asumir migraciones no aplicadas.

## WHAT
Obtener una delimitación reproducible de archivos rastreados modificados, no rastreados y contratos AI actualmente presentes, sin editar ni limpiar el worktree.

## SCOPE
- Comparar estado Git y diff únicamente en las rutas AI y de configuración relacionadas.
- Identificar migraciones, backups y archivos auxiliares AI preexistentes.
- Confirmar los contratos actuales de proveedor, configuración y selección de modelo.
- Registrar evidencia suficiente para fijar la frontera de AI-0002.

## OUT OF SCOPE
- Modificar código, configuración, migraciones o documentación.
- Aplicar migraciones o consultar bases de datos remotas.
- Realizar peticiones a proveedores AI.
- Cambiar secretos, modelos, endpoints, rate limits o políticas de failover.
- Hacer stash, reset, checkout, clean o cualquier acción destructiva de Git.

## CONTRACT
- La salida debe distinguir explícitamente archivos modificados rastreados y archivos no rastreados.
- Cada archivo AI identificado debe clasificarse como preexistente, pendiente de revisión o fuera del alcance.
- No se deben imprimir valores de secretos ni cuerpos de prompts/respuestas.

## INVARIANTS
- No se modifica ningún archivo del PMS.
- No se alteran índices, ramas ni historial Git.
- Se preservan todos los cambios preexistentes.
- La autorización humana y el aislamiento por hotelId permanecen sin cambios.

## VERIFICATION
- Los comandos terminan sin errores y producen un inventario reproducible.
- El inventario incluye AssistantService, AssistantController, LocalIntentRouter, configuración Ollama y migraciones AI relacionadas.
- No aparecen valores de claves, tokens ni secretos en la evidencia.

## ROLLBACK
No aplica: la transición es estrictamente de lectura y no cambia el estado del workspace.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant
- frontdesk-service/src/main/resources
- frontdesk-service/src/test
- docker-compose.ollama.yml
- flyway
- db/migration
- .ralph-add/REPORTS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- git status --short -- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant frontdesk-service/src/main/resources frontdesk-service/src/test docker-compose.ollama.yml flyway db/migration .ralph-add/REPORTS
- git diff --name-status -- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant frontdesk-service/src/main/resources frontdesk-service/src/test docker-compose.ollama.yml flyway db/migration
- rg -n "AssistantService|AssistantController|LocalIntentRouter|ai_api_key_encrypted|OLLAMA_BASE_URL|ASSISTANT_AI_FIRST|deepseek-|qwen3" frontdesk-service docker-compose.ollama.yml flyway db/migration
END_VERIFY_COMMANDS
