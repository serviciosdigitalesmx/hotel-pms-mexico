# A.SPEC AI-0004 — Auditoría del contrato runtime de la integración IA local

## Resultado

**WARNING — Contrato parcialmente confirmado mediante inspección estática.**

El flujo de entrada, selección de proveedor/modelo, persistencia de sesión Redis y controles básicos de autenticación/autorización están presentes. No existe evidencia runtime ni de producción, conforme al alcance READ_ONLY. Se identifican huecos en reintentos, configuración efectiva y verificación de migraciones aplicadas.

## Evidencia y hallazgos

### 1. Flujo de entrada y salida

**PASS — OBSERVED**

- `AssistantController.chat()` expone `POST /api/v1/stays/assistant/chat` en [AssistantController.java:18-34](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java:18).
- El endpoint exige `@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RECEPTIONIST')")` en [AssistantController.java:25-26](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java:25).
- El `hotelId` se obtiene del contexto autenticado y los roles de las authorities en [AssistantController.java:37-55](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java:37).
- El controlador delega a `AssistantService.chat(hotelId, roles, request)` en [AssistantController.java:27-34](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java:27).
- `AssistantService` carga `HotelSettings` por `hotelId`, valida configuración y construye la petición al proveedor en [AssistantService.java:70-97](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:70).
- La respuesta se transforma en `AssistantChatResponse`, incluyendo texto y llamadas de herramientas en [AssistantService.java:209-216](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:209).

**INFERENCE**

El contrato de entrada está diseñado para operar por tenant autenticado, pero la inspección no demuestra que el filtro de seguridad siempre establezca correctamente `authentication.details` ni que el endpoint esté alcanzable a través del gateway.

### 2. Selección de proveedor y modelo

**PASS — OBSERVED**

- Los modelos cuyo nombre comienza con `deepseek-` se enrutan al endpoint fijo `https://api.deepseek.com/chat/completions` en [AssistantService.java:41](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:41) y [AssistantService.java:278-289](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:278).
- Cualquier otro modelo configurado se enruta a Ollama mediante `OLLAMA_BASE_URL`, con fallback estático a `http://host.docker.internal:11434`, en [AssistantService.java:252-275](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:252).
- El modelo se toma de `HotelSettings.aiModel`, con fallback `qwen3:4b-instruct-2507-q4_K_M`, en [AssistantService.java:77-82](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:77).
- La configuración persistente existe en `HotelSettings.aiEnabled`, `aiModel`, `aiInstructions` y `aiApiKeyEncrypted` en [HotelSettings.java:123-138](frontdesk-service/src/main/java/com/hotelpms/frontdesk/stays/domain/HotelSettings.java:123).
- `V22` establece `qwen3:4b` como modelo local por defecto y `V23` lo sustituye por `qwen3:4b-instruct-2507-q4_K_M` en [V22__use_ollama_by_default_keep_deepseek_option.sql:11-20](frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql:11) y [V23__use_qwen3_4b_instruct_q4.sql:11-21](frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql:11).
- `docker-compose.ollama.yml` define `OLLAMA_BASE_URL`, `OLLAMA_MODEL` y `ASSISTANT_AI_FIRST` en [docker-compose.ollama.yml:1-6](docker-compose.ollama.yml:1).

**WARNING — OBSERVED**

- `OLLAMA_MODEL` se declara en `docker-compose.ollama.yml`, pero `AssistantService` selecciona el modelo desde `HotelSettings.aiModel`; no se observó uso directo de `OLLAMA_MODEL` en el servicio Java.
- La selección de proveedor depende del prefijo del nombre del modelo. Todo modelo que no comience por `deepseek-` se considera Ollama, sin allow-list explícita de modelos.
- `V21` permite modelos `deepseek-v4-flash` y `deepseek-v4-pro`, mientras que el endpoint estático usa la API oficial de DeepSeek. La compatibilidad real de esos identificadores no fue validada runtime.

**INFERENCE**

La configuración local pretende ser Ollama-first y conservar DeepSeek como opción por tenant, pero no se puede confirmar que las migraciones V21–V23 estén aplicadas en la base de datos.

### 3. Persistencia y configuración relacionada con IA

**PASS — OBSERVED**

- La configuración IA se persiste en `hotel_settings`, según los campos JPA de [HotelSettings.java:123-138](frontdesk-service/src/main/java/com/hotelpms/frontdesk/stays/domain/HotelSettings.java:123).
- `V21` preserva las credenciales cifradas existentes y deshabilita IA para configuraciones legacy en [V21__migrate_ai_to_deepseek.sql:6-25](frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql:6).
- `V22` documenta que Ollama no consume la credencial cifrada en [V22__use_ollama_by_default_keep_deepseek_option.sql:5-9](frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql:5).
- Las conversaciones se almacenan en Redis con aislamiento por `hotelId` y operador, TTL de 10 minutos y lock de 45 segundos en [ConversationSessionStore.java:21-35](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ConversationSessionStore.java:21).
- La clave Redis incorpora el tenant y un hash SHA-256 del usuario en [ConversationSessionStore.java:106-116](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ConversationSessionStore.java:106).

**WARNING — OBSERVED**

- La persistencia de conversación depende de Redis; no se observó una estrategia de recuperación si Redis no está disponible.
- El estado conversacional tiene TTL fijo de 10 minutos; no se observó configuración por entorno o tenant.

### 4. Timeouts, errores, retry y fallback

**PASS — OBSERVED**

- El cliente HTTP tiene timeout de conexión de 10 segundos en [AssistantService.java:53-55](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:53).
- Cada petición al proveedor tiene timeout total de 30 segundos en [AssistantService.java:81-84](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:81).
- Se manejan interrupciones, errores de IO, respuestas HTTP no exitosas, respuestas vacías y argumentos de herramientas inválidos en [AssistantService.java:199-243](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:199).
- `LocalIntentRouter` intenta primero el proveedor IA cuando `aiFirstEnabled()` está activo y, ante una excepción runtime, continúa con el flujo determinista local en [LocalIntentRouter.java:99-115](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java:99).
- El parser determinista clasifica intenciones locales como disponibilidad, creación de huésped, check-in y check-in masivo en [DeterministicParser.java:129-189](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/DeterministicParser.java:129).

**WARNING — OBSERVED**

- No se observó ningún retry automático en `AssistantService`; una falla de proveedor produce error o deriva al router local.
- El fallback captura `RuntimeException` de forma amplia en [LocalIntentRouter.java:102-114](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java:102). Esto puede ocultar diferencias entre indisponibilidad del proveedor, errores de configuración y errores de programación.
- No se observó fallback de Ollama hacia DeepSeek ni de DeepSeek hacia Ollama.
- El script auxiliar `scripts/argos/ollama_adapter.py` usa un timeout de 300 segundos en [ollama_adapter.py:67-81](scripts/argos/ollama_adapter.py:67), distinto del contrato Java de 30 segundos.
- No se observó circuit breaker, backoff, límite de concurrencia del proveedor ni métrica específica de fallos.

**INFERENCE**

El fallback confirmado es principalmente de proveedor IA hacia el motor determinista local, no un fallback entre proveedores remotos/locales.

### 5. Autorización de herramientas y acciones

**PASS — OBSERVED**

- `AssistantToolCatalog` expone herramientas allow-listed y calcula las disponibles según roles en [AssistantToolCatalog.java:57-66](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantToolCatalog.java:57).
- Las herramientas se incluyen en el request enviado al proveedor en [AssistantService.java:131-137](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:131).
- Las llamadas se marcan con `requiresConfirmation(name)` en [AssistantService.java:225-241](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:225).
- El prompt del sistema indica que las acciones requieren revisión y confirmación humana explícita en [AssistantService.java:183-196](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:183).
- El router local mantiene sesiones por tenant y usuario, aplica lock Redis y vuelve a validar disponibilidad antes de operaciones de check-in masivo en [LocalIntentRouter.java:100-115](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java:100) y [LocalIntentRouter.java:1039-1095](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java:1039).

**WARNING — OBSERVED**

- La autorización de entrada está explícitamente protegida por Spring Security, pero la autorización de cada operación debe depender adicionalmente de `AssistantToolCatalog` y de las validaciones internas del router; no se observó una prueba estática exhaustiva de cada herramienta.
- El proveedor recibe herramientas permitidas por rol, pero el reporte no puede confirmar que un proveedor externo no intente solicitar nombres de herramientas fuera del catálogo; la validación posterior debe permanecer en la capa de aplicación.

### 6. Secretos y credenciales

**PASS — OBSERVED**

- La API key de DeepSeek sólo se descifra cuando el modelo seleccionado es DeepSeek, en [AssistantService.java:86-90](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:86).
- Ollama usa una cabecera local fija `Bearer ollama` en [AssistantService.java:86-90](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:86) y [ollama_adapter.py:57-64](scripts/argos/ollama_adapter.py:57).
- Los logs sanitizan patrones de credenciales conocidos antes de imprimir errores del proveedor en [AssistantService.java:303-345](frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:303).

**UNKNOWN**

No se inspeccionaron archivos `.env`, valores de secretos, almacenes de credenciales ni configuración runtime efectiva, por estar expresamente fuera de alcance.

## Verificación de estado y cambios

**PASS — OBSERVED**

- La inspección fue estática y no ejecutó migraciones, reinicios, cambios de Docker, llamadas a proveedores IA ni modificaciones de archivos.
- El estado Git observado al inicio y al final conserva los mismos cambios preexistentes, incluyendo modificaciones en el área del asistente y archivos no rastreados relacionados.
- No se realizó ninguna operación destructiva.

**WARNING — OBSERVED**

- `git diff --check` no produjo hallazgos de whitespace, pero Git emitió errores del entorno macOS al intentar crear archivos temporales de Xcode en `/tmp`. Por ello, la ejecución del comando no puede considerarse completamente limpia desde el punto de vista del entorno.

## Contratos confirmados

1. Entrada: `POST /api/v1/stays/assistant/chat`.
2. Autorización de endpoint: roles `ADMIN`, `OWNER`, `RECEPTIONIST`.
3. Tenant: `hotelId` derivado del contexto autenticado.
4. Proveedor: DeepSeek para modelos `deepseek-*`; Ollama para los demás.
5. Modelo local por defecto en código: `qwen3:4b-instruct-2507-q4_K_M`.
6. Timeout Java: 10 segundos de conexión y 30 segundos por petición.
7. Persistencia conversacional: Redis, aislada por tenant/usuario, TTL 10 minutos.
8. Fallback: motor determinista local cuando falla el flujo IA-first.
9. Acciones: catálogo allow-listed y confirmación explícita para operaciones sensibles.
10. Credenciales DeepSeek: cifradas en `HotelSettings` y descifradas sólo para ese proveedor.

## Riesgos

- **WARNING:** ausencia de retry, backoff y circuit breaker.
- **WARNING:** discrepancia entre `OLLAMA_MODEL` del compose y `HotelSettings.aiModel`.
- **WARNING:** fallback amplio basado en cualquier `RuntimeException`.
- **WARNING:** dependencia de Redis sin contrato visible de degradación.
- **UNKNOWN:** migraciones V21–V23 no verificadas como aplicadas.
- **UNKNOWN:** conectividad y compatibilidad efectiva de Ollama/DeepSeek no verificadas.
- **UNKNOWN:** enforcement runtime del gateway, autenticación y permisos por herramienta no ejecutado.

## Preguntas bloqueantes

1. ¿Las migraciones V21, V22 y V23 están aplicadas en la base de datos objetivo?
2. ¿Cuál es la fuente de verdad para el modelo local: `HotelSettings.aiModel` o `OLLAMA_MODEL`?
3. ¿Debe existir retry controlado para errores transitorios del proveedor?
4. ¿Cuál es el comportamiento requerido si Redis no está disponible?
5. ¿Debe el fallback determinista activarse ante errores de configuración o sólo ante indisponibilidad del proveedor?
6. ¿La autorización de cada herramienta se valida nuevamente al ejecutar la operación, además del filtrado del catálogo?

## Clasificación final

**WARNING — La arquitectura estáticamente definida es coherente en sus líneas principales, pero el contrato runtime no queda completamente demostrado sin validación de migraciones, configuración efectiva, Redis, autenticación y proveedores.**
