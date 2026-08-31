# Reporte A.SPEC AI-0002

## Resultado

**Estado: PASS con WARNING**

La integración IA existente fue localizada en `frontdesk-service` y `frontend`. El inventario muestra integración con Ollama local y DeepSeek remoto, herramientas PMS con control RBAC y confirmación humana para acciones mutantes.

No se modificaron archivos, Git, bases de datos, Redis, Docker, migraciones ni secretos. No se ejecutaron Gradle, Docker, migraciones, servicios ni proveedores IA.

## Evidencia Git

**OBSERVED — PASS**

- Checkout: `/Users/usuario/Desktop/HOTEL-PMS`
- `git status --short` fue consultado al inicio y al final.
- El estado final coincide con el inicial: existen cambios modificados y archivos no rastreados preexistentes.
- No se hicieron operaciones de escritura Git.

**WARNING**

El checkout contiene cambios IA no confirmados, incluyendo:

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/*`
- `frontdesk-service/src/main/resources/db/migration/V21__migrate_ai_to_deepseek.sql`
- `frontdesk-service/src/main/resources/db/migration/V22__use_ollama_by_default_keep_deepseek_option.sql`
- `frontdesk-service/src/main/resources/db/migration/V23__use_qwen3_4b_instruct_q4.sql`
- `docker-compose.ollama.yml`
- `scripts/argos/*`

Las migraciones V21–V23 y varios archivos IA aparecen como no rastreados; por tanto, no forman parte del código versionado confirmado.

## Componentes IA localizados

### Backend

**OBSERVED — PASS**

Servicio principal:

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantToolCatalog.java`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/dto/*`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/*`

Endpoint:

```text
POST /api/v1/stays/assistant/chat
```

El controlador exige:

```text
ADMIN, OWNER o RECEPTIONIST
```

El `hotelId` y los roles se obtienen de la autenticación actual.

### Frontend

**OBSERVED — PASS**

- `frontend/src/pages/Assistant.tsx`
- `frontend/src/services/assistantService.ts`
- `frontend/src/services/assistantToolService.ts`
- `frontend/src/pages/Assistant.test.tsx`
- `frontend/src/services/assistantToolService.test.ts`

La interfaz limita la conversación a seis rondas de herramientas y muestra confirmación humana antes de ejecutar acciones mutantes.

## Contrato de entrada y salida

**OBSERVED — PASS**

Entrada backend:

- `AssistantChatRequest.messages`
- Máximo: 40 mensajes.
- Cada mensaje permite los roles `user`, `assistant` y `tool`.
- Contenido máximo por mensaje: 12.000 caracteres.
- Máximo de cuatro llamadas de herramienta por mensaje.

Salida:

- `AssistantChatResponse.answer`
- `AssistantChatResponse.toolCalls`

Cada llamada contiene:

- `id`
- nombre de operación
- argumentos JSON
- indicador `requiresConfirmation`

## Proveedores y rutas

**OBSERVED — WARNING**

`AssistantService` implementa dos rutas:

1. **DeepSeek remoto**
   - URI fija: `https://api.deepseek.com/chat/completions`
   - Se activa cuando el modelo comienza con `deepseek-`.
   - Usa una clave API cifrada almacenada en `HotelSettings`.
   - La clave no fue leída ni expuesta.

2. **Ollama local**
   - URI derivada de `OLLAMA_BASE_URL`.
   - Valor por defecto de código: `http://host.docker.internal:11434`
   - Endpoint compatible: `/v1/chat/completions`
   - Modelo por defecto de código: `qwen3:4b-instruct-2507-q4_K_M`

**INFERENCE**

La arquitectura pretende permitir modelos locales Ollama y DeepSeek remoto por tenant, pero la disponibilidad real de cualquiera de los proveedores no está verificada porque la A.SPEC prohíbe ejecutar servicios o llamar proveedores.

## Parámetros, límites y fallos

**OBSERVED — PASS**

Configuración encontrada en `AssistantService`:

- Timeout total de solicitud: 30 segundos.
- Timeout de conexión HTTP: 10 segundos.
- `temperature`: `0.1`.
- `max_tokens`: `1040`.
- Máximo de rondas frontend: 6.
- Respuestas vacías generan error.
- Errores HTTP del proveedor generan `AI_PROVIDER_REJECTED_REQUEST`.
- Interrupciones, errores IO y argumentos de herramientas inválidos tienen errores explícitos.
- Se sanitizan parcialmente cuerpos de error antes del log y se limita su longitud.

**WARNING**

No se observan reintentos ni backoff específicos para llamadas al proveedor IA.

No se observan colas IA dedicadas. Redis aparece como infraestructura general del PMS para rate limiting y anti-replay, no como cola IA.

## Persistencia y configuración

**OBSERVED — PASS**

La configuración IA se almacena en `HotelSettings` y se consulta mediante `HotelSettingsRepository`.

La migración rastreada `V17__add_tenant_ai_assistant.sql` define:

- `ai_enabled`
- `ai_model`
- `ai_instructions`
- `ai_api_key_encrypted`

Migraciones rastreadas V18–V20 modifican modelos por defecto históricos.

**WARNING**

Las migraciones V21–V23, que cambian DeepSeek/Ollama/Qwen, están presentes como archivos no rastreados. No puede afirmarse que estén versionadas ni aplicadas.

**UNKNOWN**

No se inspeccionó el estado real de Flyway ni de PostgreSQL, conforme al alcance READ_ONLY y la prohibición de ejecutar migraciones o consultar bases de datos.

## Herramientas PMS y autorización

**OBSERVED — PASS**

`AssistantToolCatalog` expone operaciones allow-listed diferenciadas entre:

- Lecturas para personal operativo.
- Lecturas privilegiadas para `ADMIN` y `OWNER`.
- Acciones operativas.
- Acciones privilegiadas.

Las acciones mutantes requieren confirmación en la interfaz antes de llamar a los servicios PMS.

El frontend valida nombres y parámetros mediante Zod y enruta las operaciones a servicios existentes de huéspedes, reservas, estancias, inventario, facturación, F&B, cotizaciones, tarifas y usuarios.

**INFERENCE**

La autorización efectiva depende también de que los servicios PMS subyacentes mantengan sus controles RBAC y aislamiento por tenant. El inventario estático no demuestra autorización end-to-end.

## Pruebas existentes

**OBSERVED — PASS**

Pruebas rastreadas localizadas:

- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/AssistantServiceTest.java`
- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/AssistantToolCatalogTest.java`
- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/DeterministicParserTest.java`
- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouterTest.java`
- `frontend/src/pages/Assistant.test.tsx`
- `frontend/src/services/assistantToolService.test.ts`

También existen pruebas IA no rastreadas, entre ellas `BatchCheckInParserTest.java`.

**UNKNOWN**

No se ejecutaron pruebas, builds ni comandos Gradle porque la A.SPEC es estrictamente READ_ONLY y prohíbe ejecutar Gradle.

Comandos de validación documentados por el repositorio incluyen:

```text
./gradlew test jacocoTestReport
npm run test:coverage
```

Su resultado actual queda pendiente.

## Persistencia de secretos

**OBSERVED — PASS**

- No se leyeron valores de `.env`.
- No se leyeron claves API.
- No se imprimieron secretos.
- El código usa `ai_api_key_encrypted` y un componente de descifrado existente.
- La clave DeepSeek sólo se incorpora al header de la solicitud cuando el modelo seleccionado es DeepSeek.

**WARNING**

La sanitización de logs cubre patrones concretos como `gsk_` y `AIza`, pero no constituye evidencia de redacción universal para cualquier secreto o token posible.

## Findings

| Clasificación | Hallazgo |
|---|---|
| PASS | Endpoint IA localizado con autenticación y roles explícitos. |
| PASS | Contratos de entrada/salida concretos y limitados. |
| PASS | Herramientas PMS allow-listed. |
| PASS | Confirmación humana para acciones mutantes. |
| PASS | Timeout, límites y errores explícitos. |
| PASS | Pruebas backend y frontend existentes. |
| WARNING | No se observan reintentos ni backoff del proveedor IA. |
| WARNING | V21–V23 y parte de la integración Ollama no están rastreadas por Git. |
| WARNING | Hay cambios IA sin confirmar en el checkout. |
| UNKNOWN | Estado real de Flyway, PostgreSQL, Redis y proveedores. |
| UNKNOWN | Resultado actual de las pruebas y del build. |
| UNKNOWN | Verificación end-to-end de autorización, proveedor y persistencia. |

## Pendientes explícitos

- Confirmar qué cambios IA deben conservarse y versionarse.
- Verificar el estado de las migraciones sólo con autorización posterior.
- Ejecutar pruebas y build en una A.SPEC autorizada.
- Verificar proveedor local o remoto en una A.SPEC que permita runtime.
- Añadir o confirmar política de reintentos/backoff si se requiere resiliencia.
- Realizar validación end-to-end de tenant isolation y RBAC.
