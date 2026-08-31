# A.SPEC AI-0000 — Auditoría de integración AI actual

**Modo:** READ-ONLY AUDIT  
**Estado:** WARNING  
**Mutaciones realizadas:** Ninguna  
**Peticiones a proveedores AI:** Ninguna  
**Secretos leídos o impresos:** Ninguno  
**Git:** Se preservaron cambios preexistentes observados en el worktree.

## Resumen ejecutivo

La integración AI actual está concentrada en `frontdesk-service`.

Se encontraron:

- Ollama mediante API compatible con OpenAI.
- DeepSeek mediante API compatible con OpenAI.
- Referencias históricas a Groq y Gemini en migraciones, pero no clientes activos identificados.
- No se encontraron clientes activos para OpenAI, OpenRouter o Anthropic.
- Existe un asistente local determinista como fallback.
- El endpoint está protegido por autenticación y roles.
- No existe retry específico para el proveedor AI.
- No existe circuit breaker específico para el proveedor AI.
- No existe rate limiter específico para AI; el endpoint queda cubierto indirectamente por el rate limiter general del gateway.
- No se identificaron métricas AI específicas.

## Hallazgos

| # | Punto | Clasificación | Evidencia observada | Inferencia |
|---:|---|---|---|---|
| 1 | Proveedores AI encontrados | WARNING | `AssistantService.java` contiene endpoint DeepSeek `https://api.deepseek.com/chat/completions` y construcción de endpoint Ollama desde `OLLAMA_BASE_URL`. | La integración activa actual soporta DeepSeek y Ollama. |
| 2 | Microservicio consumidor | PASS | `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java`; `AssistantController.java`; `LocalIntentRouter.java`. | `frontdesk-service` es el único consumidor AI activo localizado. |
| 3 | Archivos responsables | PASS | `AssistantController`, `AssistantService`, `AssistantToolCatalog`, DTOs del asistente, `LocalIntentRouter`, `DeterministicParser`, `ConversationSessionStore`, `HotelSettings`. | El cambio futuro deberá concentrarse principalmente en este módulo y sus contratos de configuración/persistencia. |
| 4 | SDK o protocolo | PASS | Se utiliza `java.net.http.HttpClient`, `HttpRequest` y `HttpResponse`; el payload usa `POST /v1/chat/completions` con `messages`, `tools` y `tool_choice`. | No se encontró SDK oficial de DeepSeek, Ollama, OpenAI, Groq, Gemini, OpenRouter o Anthropic. |
| 5 | Modelos configurados | WARNING | Default Java: `qwen3:4b-instruct-2507-q4_K_M`. `docker-compose.ollama.yml`: `OLLAMA_MODEL` default `qwen3:1.7b`. Migraciones históricas contienen `llama-3.3-70b-versatile`, `llama-3.1-8b-instant`, `openai/gpt-oss-20b`, `deepseek-v4-flash`, `deepseek-v4-pro` y `qwen3:4b`. | Existe posible divergencia entre defaults de Docker, Java y migraciones. El modelo efectivo depende del valor almacenado por hotel y del estado real de Flyway, que no se verificó en base de datos. |
| 6 | Variables requeridas | WARNING | Nombres observados: `OLLAMA_BASE_URL`, `OLLAMA_MODEL`, `ASSISTANT_AI_FIRST`, `ALLOGGIATI_CREDENTIALS_ENCRYPTION_KEY`, además de la clave AI almacenada cifrada en `hotel_settings.ai_api_key_encrypted`. | `OLLAMA_MODEL` aparece configurada en Compose, pero el código mostrado selecciona el modelo desde `HotelSettings`; no se comprobó que `OLLAMA_MODEL` sea consumida por Java. La credencial DeepSeek es por hotel y se descifra en runtime. |
| 7 | Flujo actual de petición AI | PASS | `POST /api/v1/stays/assistant/chat` → `AssistantController` → resolución de `hotelId` y roles → `AssistantService.chat` → configuración de `HotelSettings` → selección DeepSeek/Ollama → HTTP chat completion → parsing de respuesta y tool calls. | Las herramientas no ejecutan directamente operaciones en `AssistantService`; devuelven propuestas estructuradas para el flujo posterior. |
| 8 | Timeouts | PASS | `HttpClient.connectTimeout(Duration.ofSeconds(10))`; cada request usa `.timeout(Duration.ofSeconds(30))`. | El timeout total de proveedor es 30 segundos, con conexión limitada a 10 segundos. |
| 9 | Retries | WARNING | No se encontraron reintentos en `AssistantService`; `send` ejecuta una sola llamada HTTP. | Los reintentos manuales pertenecen a algunos flujos locales del PMS, no al proveedor AI. |
| 10 | Manejo HTTP 429 | WARNING | Cualquier estado fuera de 2xx se registra y termina como `AI_PROVIDER_REJECTED_REQUEST`; no hay rama específica para 429 ni lectura de `Retry-After`. | Un límite del proveedor no recibe tratamiento diferenciado, backoff ni respuesta operativa específica. |
| 11 | Manejo HTTP 5xx | WARNING | Cualquier estado fuera de 2xx produce `ExternalServiceException("AI_PROVIDER_REJECTED_REQUEST")`. | Los 5xx se agrupan con 4xx; no hay clasificación, backoff ni retry específico. |
| 12 | Fallback existente | PASS | `LocalIntentRouter` intenta AI primero cuando `ASSISTANT_AI_FIRST` está habilitado; ante `RuntimeException` llama al flujo determinista local. El flujo local cubre huéspedes, disponibilidad y check-in. | Existe fallback funcional a reglas y servicios PMS locales, aunque ciertos mensajes desconocidos vuelven a intentar `assistantService.chat`. |
| 13 | Circuit breaker | WARNING | Hay Resilience4j para clientes Feign de otros servicios, por ejemplo billing, notification y guest. No se encontró anotación ni instancia Resilience4j específica para `AssistantService`. | La llamada externa AI no está protegida por circuit breaker. |
| 14 | Rate limiter | WARNING | `api-gateway.yml` aplica `RequestRateLimiter` al route general de `frontdesk-service` con Redis y `userKeyResolver`. | El endpoint AI queda cubierto indirectamente por el límite general del servicio, pero no existe una cuota específica por proveedor, hotel, usuario, tokens o costo. |
| 15 | Métricas y logging | WARNING | Hay Actuator/Prometheus y tracing configurados para `frontdesk-service`. `AssistantService` registra status y cuerpo sanitizado del proveedor; `LocalIntentRouter` registra fallos con `hotelId`, tipo y status. | No se observaron métricas AI específicas para latencia, tokens, proveedor, modelo, 429, 5xx, fallback o costo. El logging de errores de proveedor está limitado, pero se debe revisar continuamente para evitar exposición de datos sensibles enviados en cuerpos de error. |
| 16 | Riesgos detectados | BLOCKER | El endpoint AI remoto recibe conversaciones, instrucciones del hotel y contexto operativo; la llamada DeepSeek usa una clave descifrada en memoria. No hay circuit breaker ni retry/backoff específico. Hay migraciones y backups de archivos AI dentro del worktree modificado. | Antes de un gateway común se necesita definir clasificación de datos, política de retención, redacción, límites y proveedor autorizado. También debe verificarse el estado real de migraciones antes de cambiar defaults. |
| 17 | Change Surface probable de AI-0001 | WARNING | Áreas identificadas: `AssistantController`, `AssistantService`, `AssistantToolCatalog`, `LocalIntentRouter`, configuración de `frontdesk-service`, rutas del gateway, `HotelSettings`/DTOs, migraciones AI y observabilidad. | AI-0001 debería introducir un gateway detrás de un contrato estable, sin modificar directamente los flujos PMS ni permitir que el modelo ejecute mutaciones sin autorización humana. |
| 18 | Propuesta arquitectónica inicial | WARNING | La A.SPEC define: PMS/Assistant → AI Gateway → Provider Registry → Rate Limiter → Retry/Backoff/Jitter → Circuit Breaker → Failover autorizado → Usage/Metrics → Secret Provider. | La arquitectura objetivo es compatible con el sistema actual, pero requiere separar proveedor, resiliencia, seguridad, observabilidad y ejecución de herramientas. El failover debe ser explícito y no utilizar rotación de cuentas para evadir cuotas. |

## Proveedores

### Ollama

**Clasificación:** PASS

**Evidencia observada:**

- Endpoint construido como `${OLLAMA_BASE_URL}/v1/chat/completions`.
- Default de código: `http://host.docker.internal:11434`.
- Compose adicional: `docker-compose.ollama.yml`.
- Modelo configurado en Docker: `qwen3:1.7b`.
- Modelo default de `HotelSettings`: `qwen3:4b-instruct-2507-q4_K_M`.

**Inferencia:**

Ollama es el proveedor local predeterminado según la lógica actual cuando el modelo no comienza con `deepseek-`.

### DeepSeek

**Clasificación:** PASS

**Evidencia observada:**

- Endpoint fijo: `https://api.deepseek.com/chat/completions`.
- Se envía `Authorization: Bearer ...`.
- Se activa cuando el modelo comienza con `deepseek-`.
- La clave se obtiene descifrando `ai_api_key_encrypted`.

**Inferencia:**

DeepSeek es el único proveedor remoto activo identificado en el código actual.

### Groq

**Clasificación:** UNKNOWN

**Evidencia observada:**

- Existen referencias históricas a modelos Groq en las migraciones V17–V20.
- No se encontró cliente HTTP ni endpoint Groq activo en `AssistantService`.

**Inferencia:**

Groq parece haber sido proveedor anterior o configuración histórica, pero su uso actual no está demostrado.

### Gemini

**Clasificación:** UNKNOWN

**Evidencia observada:**

- Una migración histórica contempla modelos con prefijo `gemini-`.
- No se encontró cliente Gemini ni llamada `generateContent`.

**Inferencia:**

No puede clasificarse como integración activa.

### OpenAI, OpenRouter y Anthropic

**Clasificación:** UNKNOWN

**Evidencia observada:**

- No se localizaron SDKs ni endpoints activos de estos proveedores.
- El formato utilizado es compatible con OpenAI, pero la compatibilidad de protocolo no prueba consumo de OpenAI.

**Inferencia:**

No hay evidencia suficiente de integración activa.

## Autorización y herramientas

**Clasificación:** PASS

**Evidencia observada:**

- `AssistantController` usa `@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RECEPTIONIST')")`.
- Las herramientas se filtran por roles en `AssistantToolCatalog`.
- Las acciones se exponen como `proponer_accion_pms`.
- Las acciones requieren confirmación explícita según `requiresConfirmation`.
- El contexto de sesión Redis se segmenta por `hotelId` y usuario.

**Riesgo:**

El reporte es de código estático. No se verificó ejecución autenticada, aislamiento real entre tenants ni comportamiento efectivo del entorno desplegado.

## Integridad del worktree

**Clasificación:** BLOCKER para cualquier cambio posterior sin revisión de límites

**Evidencia observada:**

El worktree contiene múltiples cambios no comprometidos y archivos no rastreados relacionados con AI, incluyendo:

- Cambios en `AssistantController.java`, `AssistantService.java`, `AssistantToolCatalog.java` y el router.
- `docker-compose.ollama.yml`.
- Migraciones AI V21, V22 y V23.
- Backups de `AssistantController.java`.
- Archivos auxiliares no rastreados.

**Inferencia:**

AI-0001 debe comenzar con una delimitación exacta de cambios preexistentes. No se debe sobrescribir, limpiar, resetear, hacer stash ni asumir que las migraciones están aplicadas.

## Propuesta inicial para AI-0001

1. Definir un contrato interno único `AssistantGateway`.
2. Mover la selección de proveedor a un `ProviderRegistry`.
3. Mantener DeepSeek y Ollama como adaptadores separados.
4. Agregar timeout, retry limitado con backoff y jitter únicamente para errores elegibles.
5. Agregar circuit breaker específico por proveedor.
6. Clasificar 429 y 5xx de forma distinta.
7. Agregar rate limiting específico por hotel, usuario y proveedor.
8. Instrumentar latencia, éxito, errores, fallback y consumo sin registrar prompts, respuestas completas ni secretos.
9. Centralizar secretos en un proveedor autorizado.
10. Preservar autorización, aislamiento por `hotelId` y confirmación humana para mutaciones.
11. Definir failover autorizado sin rotación de cuentas para evadir cuotas.
12. Verificar migraciones y configuración efectiva antes de cambiar modelos o defaults.

## Definition of Done

- [x] Integraciones AI inventariadas a nivel de código.
- [x] Proveedores y modelos identificados.
- [x] Variables identificadas sin valores.
- [x] Flujo actual documentado.
- [x] Timeouts documentados.
- [x] Retries documentados.
- [x] Manejo de 429 documentado.
- [x] Manejo de 5xx documentado.
- [x] Fallback documentado.
- [x] Circuit breaker documentado.
- [x] Rate limiter documentado.
- [x] Métricas y logging documentados.
- [x] Riesgos clasificados.
- [x] Change Surface propuesto.
- [x] Ningún secreto revelado.
- [x] Ninguna petición externa realizada.
- [x] Ningún archivo del PMS modificado.
- [ ] `.ralph-add/REPORTS/AI-0000-report.md` no fue creado porque la ejecución fue estrictamente de solo lectura y la autorización de mutación es `NONE`.

<promise>COMPLETE</promise>
