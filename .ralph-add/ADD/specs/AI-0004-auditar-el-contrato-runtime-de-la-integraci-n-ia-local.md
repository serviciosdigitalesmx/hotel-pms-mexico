# A.SPEC AI-0004 — Auditar el contrato runtime de la integración IA local

ID: AI-0004
Mode: READ_ONLY
RISK: LOW

## WHY
El inventario anterior detectó cambios IA, pero no demuestra qué proveedor, modelo, persistencia, timeouts, reintentos, fallback ni controles de autorización requiere el runtime.

## WHAT
Documentar mediante evidencia estática el flujo AssistantController -> AssistantService -> motor/router -> proveedor IA y su relación con las migraciones y configuración local, identificando contratos existentes y huecos de resiliencia.

## SCOPE
- Flujo de entrada y salida del asistente
- Selección de proveedor y modelo
- Manejo de errores, timeout, retry y fallback
- Persistencia/configuración relacionada con IA
- Controles de autenticación y autorización ya presentes

## OUT OF SCOPE
- Modificar código, migraciones, Docker o scripts
- Ejecutar migraciones o reiniciar servicios
- Leer secretos o archivos .env
- Validar comportamiento runtime o producción
- Limpiar backups o __pycache__

## CONTRACT
- El reporte debe separar OBSERVED de INFERENCE
- Cada hallazgo debe incluir archivo y símbolo o línea
- No se deben inventar proveedores, endpoints, tablas ni capacidades

## INVARIANTS
- El checkout y el estado Git permanecen idénticos
- No se imprimen valores secretos
- No se realizan operaciones destructivas ni externas

## VERIFICATION
- Las búsquedas y git diff --check terminan sin modificar archivos
- El estado Git inicial y final es idéntico
- El resultado enumera explícitamente contratos confirmados, riesgos y preguntas bloqueantes

## ROLLBACK
No aplica: la especificación es READ_ONLY y no debe producir cambios.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/
- frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql
- frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql
- frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql
- docker-compose.ollama.yml
- scripts/argos/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- git diff --check -- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/
- rg -n "ollama|deepseek|qwen|provider|model|timeout|retry|fallback|redis|role|permission|secret|credential" frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/ frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql docker-compose.ollama.yml scripts/argos/
- git status --short -- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/ frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql docker-compose.ollama.yml scripts/argos/
END_VERIFY_COMMANDS
