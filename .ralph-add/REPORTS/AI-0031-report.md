# A.SPEC AI-0031 — Auditoría de integración AI resiliente

**Modo:** READ_ONLY  
**Resultado:** WARNING  
**Cambios realizados:** Ninguno  
**Proveedores AI externos invocados:** No

## Resumen ejecutivo

La integración AI existente tiene controles básicos de resiliencia:

- Timeout de conexión de 10 segundos.
- Timeout total de solicitud de 30 segundos.
- Fallback determinista local para el enrutamiento de intenciones.
- Control de autorización mediante `@PreAuthorize`.
- Separación entre modelos locales Ollama y DeepSeek.

Sin embargo, no existe un circuito Resilience4j aplicado al cliente AI, ni políticas explícitas de retry, rate limiting, bulkhead, métricas de proveedor o clasificación detallada de errores AI. La configuración Resilience4j encontrada corresponde principalmente a clientes Feign de servicios internos y no al proveedor AI.

## Evidencia observada

### 1. Cliente AI y timeout

**PASS — Timeout de conexión y solicitud presentes**

`AssistantService` usa un `HttpClient` estático con:

- `connectTimeout(Duration.ofSeconds(10))`
- Timeout por solicitud de `Duration.ofSeconds(30)`

Evidencia observada:

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:41-55`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:81-84`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:199-222`

La respuesta HTTP no exitosa se transforma en `ExternalServiceException("AI_PROVIDER_REJECTED_REQUEST")`. Los errores de IO se transforman en `AI_PROVIDER_UNAVAILABLE`, y la interrupción conserva el estado de interrupción del hilo.

### 2. Selección de proveedor

**PASS — La selección de proveedor está explícitamente delimitada**

El modelo cuyo identificador empieza con `deepseek-` utiliza:

- `https://api.deepseek.com/chat/completions`
- API key desencriptada desde la configuración del hotel.

Los demás modelos utilizan un endpoint compatible con Ollama, derivado de `OLLAMA_BASE_URL`, con fallback estático a:

- `http://host.docker.internal:11434/v1/chat/completions`

Evidencia observada:

- `AssistantService.java:41`
- `AssistantService.java:77-95`
- `AssistantService.java:252-289`

**INFERENCE:** El fallback de Ollama depende de conectividad desde el contenedor hacia `host.docker.internal`; esto no fue validado en runtime porque la A.SPEC prohíbe ejecutar builds, despliegues o llamadas AI.

### 3. Circuit breaker aplicado al cliente AI

**BLOCKER para producción resiliente — No se observó circuit breaker AI**

No se encontró `@CircuitBreaker`, `CircuitBreakerRegistry` ni configuración Resilience4j asociada a `AssistantService` o al endpoint AI.

El único control de circuito AI identificado es un componente propio:

- `ResilientIntentFallbackHandler`

Evidencia observada:

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/ResilientIntentFallbackHandler.java:20-31`

Este componente ejecuta el proveedor primario y, ante cualquier `RuntimeException`, ejecuta el fallback determinista. No mantiene estado abierto/cerrado, ventana deslizante, half-open, umbral de fallos ni aislamiento de concurrencia.

**INFERENCE:** Una indisponibilidad persistente del proveedor puede provocar intentos hacia el proveedor en cada solicitud nueva; no existe evidencia de supresión temprana mediante circuito abierto.

### 4. Retry policy

**WARNING — No existe retry explícito para solicitudes AI**

No se observó:

- `@Retry`
- configuración `resilience4j.retry`
- reintentos manuales alrededor de `HTTP_CLIENT.send`
- backoff
- límite de intentos
- diferenciación entre errores transitorios y permanentes.

Evidencia observada:

- `AssistantService.java:199-222` contiene una sola invocación a `HTTP_CLIENT.send`.
- La búsqueda de configuración Resilience4j sólo encontró circuit breakers de clientes internos en `config-service`.

**INFERENCE:** Los errores temporales de conexión, timeout o respuestas 5xx no se reintentan antes de activar el fallback determinista.

### 5. Fallback AI

**PASS — Existe fallback determinista y conserva el contexto operativo**

`ResilientIntentFallbackHandler` recibe el fallback desde `LocalIntentRouter`, permitiendo conservar el bloqueo de sesión y el contexto del hotel en vez de crear un flujo alternativo aislado.

Evidencia observada:

- `ResilientIntentFallbackHandler.java:15-30`
- `LocalIntentRouter.java:78`
- `LocalIntentRouter.java:136`

El fallback propaga sus propios errores, en vez de ocultarlos:

- `ResilientIntentFallbackHandlerTest.java:38-45`

**WARNING — El fallback captura cualquier `RuntimeException`**

El handler no distingue entre:

- timeout o indisponibilidad del proveedor;
- error de serialización;
- respuesta inválida;
- error de configuración;
- error lógico o de programación.

Evidencia observada:

- `ResilientIntentFallbackHandler.java:25-30`

**INFERENCE:** Un error no relacionado con disponibilidad AI podría clasificarse como degradación del proveedor y ocultarse detrás del flujo determinista.

### 6. Error contract y respuesta del endpoint

**PASS — El controlador exige autenticación y roles**

El endpoint está protegido por:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RECEPTIONIST')")
```

El `hotelId` y los roles se obtienen del contexto de seguridad, no del cuerpo de la petición.

Evidencia observada:

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java:18-55`

**WARNING — No se observó un contrato HTTP específico para degradación AI**

El servicio produce excepciones de dominio como:

- `AI_PROVIDER_REJECTED_REQUEST`
- `AI_PROVIDER_EMPTY_RESPONSE`
- `AI_PROVIDER_UNAVAILABLE`
- `AI_PROVIDER_INTERRUPTED`
- `AI_PROVIDER_INVALID_TOOL_ARGUMENTS`

Evidencia observada:

- `AssistantService.java:203-221`
- `AssistantService.java:213-215`
- `AssistantService.java:235-242`

No se observó en esta auditoría una clasificación específica que distinga claramente entre:

- proveedor no disponible;
- configuración inválida;
- rate limit del proveedor;
- error permanente de solicitud;
- fallback local exitoso.

**INFERENCE:** El contrato final HTTP depende del manejador global de excepciones y puede presentar varios fallos AI como errores genéricos si no existe un mapeo específico.

### 7. Rate limiting

**WARNING — No existe rate limiting específico para AI**

Se observaron filtros `RequestRateLimiter` en `api-gateway`, pero no se verificó una ruta específica para `/api/v1/stays/assistant/chat`.

Evidencia observada:

- `config-service/src/main/resources/config/api-gateway.yml:35-126` contiene rate limits para rutas del gateway.
- No se encontró configuración `resilience4j.ratelimiter` asociada a AI.
- No se encontró rate limiter dentro de `AssistantService`.

Los límites observados en el gateway incluyen valores como:

- `replenishRate: 10`, `burstCapacity: 20`
- `replenishRate: 5`, `burstCapacity: 10`
- `replenishRate: 2`, `burstCapacity: 10`

**INFERENCE:** No puede confirmarse que el endpoint AI tenga un límite dedicado por hotel, usuario, rol o proveedor. La búsqueda estática no prueba el comportamiento efectivo del gateway.

### 8. Configuración Resilience4j existente

**PASS — Resilience4j está presente para dependencias internas**

`frontdesk-service` declara la dependencia:

- `spring-cloud-starter-circuitbreaker-resilience4j`

Evidencia observada:

- `frontdesk-service/build.gradle.kts:59`

La configuración encontrada en `frontdesk-service.yml` incluye circuit breakers para:

- `notificationService`
- `billingService`

Evidencia observada:

- `config-service/src/main/resources/config/frontdesk-service.yml:91-114`

Los clientes Feign internos tienen anotaciones y fallbacks:

- `BillingClient`
- `GuestClient`
- `NotificationClient`

Evidencia observada:

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/client/BillingClient.java:56-115`
- `GuestClient.java:78-107`
- `NotificationClient.java:38-70`

**WARNING — La configuración Resilience4j no cubre la ruta AI**

No existe una instancia AI equivalente a `aiProvider`, `deepSeek` u `ollama` en la configuración observada.

### 9. Guest-service y common-web-lib

**UNKNOWN — Sin integración AI identificada en guest-service**

La búsqueda estática no encontró un cliente AI, endpoint AI o configuración de proveedor AI en `guest-service`.

Sí se encontraron circuit breakers para dependencias funcionales no AI:

- `ReservationClient`
- `AlloggiatiComuniClient`
- `StayServiceClient`
- `BillingServiceClient`

**UNKNOWN — Sin mecanismo AI identificado en common-web-lib**

La búsqueda no encontró clientes AI, circuit breakers AI, retry policies AI ni fallback AI en `common-web-lib`.

La librería contiene principalmente manejo común de errores HTTP, no una abstracción resiliente del proveedor AI.

### 10. Cobertura de pruebas

**PASS — Hay pruebas unitarias del fallback determinista**

Evidencia observada:

- `ResilientIntentFallbackHandlerTest.java:16-45`
- Verifica respuesta primaria exitosa.
- Verifica fallback ante excepción.
- Verifica propagación de error del fallback.

**PASS — Hay pruebas del enrutamiento AI/local**

Evidencia observada:

- `LocalIntentRouterTest.java:78-107`
- `LocalIntentRouterTest.java:158-162`
- `LocalIntentRouterTest.java:187-231`

Se cubren escenarios de proveedor AI fallido y uso del flujo determinista para intenciones operativas.

**WARNING — Cobertura insuficiente de fallos de transporte y proveedor real**

No se observaron pruebas específicas para:

- timeout de conexión;
- timeout total de 30 segundos;
- interrupción del hilo;
- HTTP 429;
- HTTP 401/403;
- HTTP 5xx;
- cuerpo de respuesta inválido;
- respuesta AI vacía;
- JSON de `tool_calls` inválido;
- circuit breaker abierto;
- retry/backoff;
- rate limiting;
- recuperación después de indisponibilidad.

`AssistantServiceTest` sólo prueba `requiresInitialTool`, no el transporte HTTP.

Evidencia observada:

- `frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant/AssistantServiceTest.java:10-27`

**INFERENCE:** Las pruebas existentes validan principalmente la selección de flujo y el fallback en memoria; no demuestran resiliencia del cliente HTTP ni comportamiento contra un proveedor degradado.

## Matriz de hallazgos

| Área | Clasificación | Hallazgo |
|---|---|---|
| Timeout de conexión | PASS | 10 segundos configurados en `HttpClient`. |
| Timeout de solicitud | PASS | 30 segundos por solicitud AI. |
| Fallback determinista | PASS | Existe y conserva contexto del router. |
| Protección del endpoint | PASS | Roles y hotel derivado del contexto autenticado. |
| Circuit breaker AI | BLOCKER | No existe circuito Resilience4j para el proveedor AI. |
| Retry AI | WARNING | No existe retry ni backoff explícito. |
| Rate limiting AI | WARNING | No se observó límite específico para la ruta AI. |
| Clasificación de errores | WARNING | El fallback captura cualquier `RuntimeException`. |
| Contrato HTTP de degradación | WARNING | No se verificó mapeo específico de errores AI. |
| Pruebas de timeout/transporte | WARNING | No hay cobertura observada del cliente HTTP real. |
| Pruebas de 429/5xx | WARNING | No hay cobertura observada. |
| guest-service AI | UNKNOWN | No se identificó integración AI. |
| common-web-lib AI | UNKNOWN | No se identificó abstracción AI. |

## Siguiente objetivo recomendado

Definir una siguiente A.SPEC acotada para implementar y verificar, sin mezclar dominios:

1. Circuit breaker dedicado al proveedor AI.
2. Retry limitado únicamente para errores transitorios.
3. Clasificación explícita de 429, 5xx, timeout y errores permanentes.
4. Rate limiting por hotel/usuario para el endpoint AI.
5. Pruebas unitarias del cliente HTTP mediante un transporte controlado, sin llamar a proveedores externos.
6. Pruebas de apertura, half-open, fallback y recuperación del circuito.

La implementación debe preservar el fallback determinista, el `hotelId` autenticado y las confirmaciones existentes para operaciones PMS.
