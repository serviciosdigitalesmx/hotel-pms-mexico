# A.SPEC AI-0003 — Delimitar los cambios IA no versionados

ID: AI-0003
Mode: READ_ONLY
RISK: LOW

## WHY
La resiliencia no puede avanzar de forma segura mientras no esté claro qué implementación IA pertenece al producto y qué archivos son cambios locales no versionados.

## WHAT
Inspeccionar y clasificar exclusivamente el estado Git de los archivos IA identificados, separando rastreados, modificados y no rastreados, sin modificar el checkout.

## SCOPE
- Obtener el estado Git de los archivos IA listados en el reporte.
- Comparar únicamente los cambios locales relevantes mediante estadísticas de diff.
- Enumerar los archivos IA no rastreados dentro de los prefijos autorizados.

## OUT OF SCOPE
- Añadir archivos a Git o hacer commits.
- Modificar código, migraciones, configuración o secretos.
- Ejecutar Gradle, Docker, migraciones, servicios o proveedores IA.
- Validar PostgreSQL, Flyway, Redis, RBAC o tenant isolation en runtime.

## CONTRACT
- La salida debe identificar cada archivo IA como rastreado/modificado, no rastreado o ausente.
- La inspección no debe leer ni imprimir secretos.
- El estado Git final debe ser idéntico al inicial.

## INVARIANTS
- No se modifica ningún archivo.
- No se ejecutan operaciones Git de escritura.
- No se consulta ni altera infraestructura externa.
- Los límites de alcance del reporte AI-0002 permanecen intactos.

## VERIFICATION
- Los tres comandos terminan sin cambios en el checkout.
- El inventario resultante permite decidir explícitamente qué cambios IA deben versionarse en una A.SPEC WRITE posterior.
- git status --short final coincide con el estado inicial.

## ROLLBACK
No aplica: la A.SPEC es estrictamente READ_ONLY y no realiza cambios.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant
- frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql
- frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql
- frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql
- docker-compose.ollama.yml
- scripts/argos
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- git status --short -- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql docker-compose.ollama.yml scripts/argos
- git diff --stat -- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql docker-compose.ollama.yml scripts/argos
- git ls-files --others --exclude-standard -- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql docker-compose.ollama.yml scripts/argos
END_VERIFY_COMMANDS
