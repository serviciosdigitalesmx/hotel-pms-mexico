# Report A.SPEC AI-0003

## Resultado

**WARNING** — Se detectaron cambios IA rastreados/modificados y archivos IA no rastreados dentro de los prefijos autorizados. No se realizaron modificaciones.

## Evidencia OBSERVED

### Archivos rastreados y modificados

`git diff --stat` reportó:

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java` — modificado, `40` líneas afectadas.
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java` — modificado, `96` líneas afectadas.
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantToolCatalog.java` — modificado, `2` líneas afectadas.
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ConversationStep.java` — modificado, `9` líneas afectadas.
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/DeterministicParser.java` — modificado, `50` líneas afectadas.
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntent.java` — modificado, `1` línea afectada.
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java` — modificado, `927` líneas afectadas.

Total observado: `1,093` inserciones y `32` eliminaciones en 7 archivos.

### Archivos no rastreados

`git ls-files --others --exclude-standard` reportó:

- `docker-compose.ollama.yml`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java.backup-20260818-005334`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java.backup-ai-only-20260818-005244`
- `frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql`
- `frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql`
- `frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql`
- `scripts/argos/__pycache__/argos.cpython-314.pyc`
- `scripts/argos/argos.py`
- `scripts/argos/ollama_adapter.py`

### Estado Git antes y después

El resultado de `git status --short` fue idéntico antes y después:

- Los 7 archivos Java IA aparecen como `M`.
- `docker-compose.ollama.yml`, los dos backups, las migraciones V21–V23 y `scripts/argos/` aparecen como no rastreados.

**PASS** — No se observó cambio en el estado Git durante la inspección.

### Errores o advertencias del entorno

Git mostró advertencias de macOS relacionadas con `DARWIN_USER_TEMP_DIR` y archivos temporales de `xcrun` no creables en `/tmp`. Las consultas Git sí produjeron la evidencia solicitada y no modificaron el checkout.

## Clasificación por requisito

| Requisito | Clasificación | Evidencia |
|---|---|---|
| Identificar archivos IA rastreados/modificados | PASS | 7 archivos modificados en `git status` y `git diff --stat`. |
| Enumerar archivos IA no rastreados | PASS | 9 rutas enumeradas por `git ls-files --others`. |
| Comparar cambios locales mediante estadísticas | PASS | Estadística de diff disponible para los archivos rastreados. |
| No modificar archivos, Git o infraestructura | PASS | Estado inicial y final idénticos; no se ejecutaron operaciones de escritura. |
| No leer ni imprimir secretos | PASS | No se leyeron archivos `.env` ni valores secretos. |
| Mantener límites de AI-0002 | UNKNOWN | La presente inspección confirma únicamente los prefijos autorizados de AI-0003; no se verificó el contenido completo de AI-0002. |

## INFERENCE

- Los cambios Java representan modificaciones locales sobre implementación IA ya rastreada.
- Las migraciones V21–V23, `docker-compose.ollama.yml` y `scripts/argos` son candidatos a revisión explícita en una futura A.SPEC WRITE.
- Los archivos `.backup-*` y `__pycache__/argos.cpython-314.pyc` parecen artefactos locales, pero su intención y política de versionado no se determinan sin una especificación adicional.
- Este reporte no demuestra que ninguna integración IA funcione en runtime ni valida migraciones, Docker, Redis, PostgreSQL, RBAC o proveedores IA.

## Conclusión

**WARNING** — El inventario permite decidir qué cambios IA deberán versionarse posteriormente, pero no autoriza añadir, eliminar, limpiar, modificar ni hacer commit de ninguno de ellos.
