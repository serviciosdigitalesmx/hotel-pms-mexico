# A.SPEC AI-0034 — Informe de inspección

## Alcance

Inspección exclusivamente de:

- `frontdesk-service/src`
- `guest-service/src`
- `common-web-lib/src`
- `config-service/src`

No se ejecutaron builds, pruebas, llamadas a proveedores AI, cambios de código, cambios de configuración ni operaciones sobre DB, Redis, Docker o Git.

## Estado de workspace

**PASS**

**OBSERVED**

`git status --short` mostró cambios y archivos no rastreados preexistentes antes y después de la inspección. No se modificó ese estado.

**INFERENCE**

El workspace permaneció sin cambios atribuibles a esta ejecución.

## Integración AI existente

**WARNING**

**OBSERVED**

`frontdesk-service` contiene una integración AI en:

- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java`
- `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java`

La integración:

- Usa `HttpClient` directamente.
- Llama a un endpoint compatible con DeepSeek o Ollama.
- Usa `OLLAMA_BASE_URL` para el endpoint local.
- Usa `https://api.deepseek.com/chat/completions` para modelos cuyo nombre comienza con `deepseek-`.
- Tiene timeout de conexión de 10 segundos.
- Tiene timeout de request de 30 segundos.
- Limita la salida a `1040` tokens.
- Mantiene herramientas permitidas mediante `AssistantToolCatalog`.
- Requiere confirmación para acciones operativas.

**INFERENCE**

No existe una abstracción común de proveedor AI ni una estrategia explícita de failover entre DeepSeek y Ollama. Si el proveedor configurado falla, el flujo depende del fallback determinista local.

## Autorización y aislamiento

**PASS**

**OBSERVED**

`AssistantController` protege `/api/v1/stays/assistant/chat` con:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RECEPTIONIST')")
```

También obtiene el `hotelId` y roles desde el contexto autenticado.

El prompt operativo indica que las acciones sólo se proponen y requieren confirmación explícita del usuario.

`LocalIntentRouter` conserva el contexto de hotel, usuario y sesión.

**INFERENCE**

La ruta observada no muestra bypass directo de autorización ni escrituras directas a base de datos por parte del proveedor AI.

## Circuit breaker y retry

**PASS**

**OBSERVED**

`AssistantService.chat(...)` está anotado con:

```java
@CircuitBreaker(name = "aiProvider")
@Retry(name = "aiProvider")
```

La configuración en `config-service/src/main/resources/config/frontdesk-service.yml` define:

- Circuit breaker `aiProvider`.
- Ventana basada en 5 llamadas.
- Mínimo de 3 llamadas.
- Umbral de fallos del 50%.
- Estado abierto durante 10 segundos.
- Una llamada permitida en half-open.
- Exclusión de `PermanentAiProviderException`.
- Máximo de 3 intentos.
- Espera inicial de 250 ms.
- Backoff exponencial con multiplicador 2.
- Reintento únicamente para `RetryableAiProviderException`.

Los códigos HTTP `429` y `5xx` se clasifican como reintentables. Los códigos `4xx` restantes se clasifican como errores permanentes.

## Fallback funcional

**PASS**

**OBSERVED**

Existe `ResilientIntentFallbackHandler`, que ejecuta un fallback determinista cuando falla el proveedor principal.

`LocalIntentRouter` integra este handler y conserva el flujo local de recepción, incluyendo:

- Búsqueda de huéspedes.
- Selección de habitación.
- Validación de fechas.
- Confirmación de creación de huésped.
- Confirmación de check-in.
- Manejo de indisponibilidad de `guest-service`.

Existen pruebas unitarias para:

- Respuesta primaria exitosa.
- Fallback ante indisponibilidad del proveedor.
- Propagación de errores del fallback.
- Reglas de retry por código HTTP.
- Continuidad de flujos locales.

## Cobertura de pruebas

**WARNING**

**OBSERVED**

Existen pruebas relacionadas con AI y resiliencia:

- `AssistantServiceTest`
- `ResilientIntentFallbackHandlerTest`
- `LocalIntentRouterTest`

Las pruebas cubren clasificación de errores, fallback determinista y continuidad de flujos locales.

No se encontraron pruebas específicas para:

- Aplicación real de `@CircuitBreaker` y `@Retry` dentro del contexto Spring.
- Apertura, half-open y cierre del circuito.
- Conteo de reintentos.
- Validación de timeout real del `HttpClient`.
- Redacción completa de secretos en todas las variantes de logs.
- Resiliencia específica del endpoint Ollama.
- Resiliencia específica del endpoint DeepSeek.
- Métricas AI o consumo de tokens.

**INFERENCE**

La cobertura está orientada a lógica unitaria; no demuestra por sí misma el comportamiento runtime de Resilience4j ni la integración real con proveedores.

No se ejecutaron pruebas porque el A.SPEC lo excluye explícitamente.

## Token tracking

**WARNING**

**OBSERVED**

El código fija `max_tokens` en `1040`, pero no extrae ni persiste los campos de uso del proveedor, como:

- `prompt_tokens`
- `completion_tokens`
- `total_tokens`

No se encontraron contadores, métricas, persistencia ni eventos de consumo AI específicos.

El uso de la palabra `token` encontrado en otros archivos corresponde principalmente a locks Redis, sesiones Alloggiati o credenciales, no a seguimiento de consumo AI.

**INFERENCE**

Actualmente no hay evidencia de observabilidad de consumo, coste, cuota o volumen de tokens por hotel, usuario, modelo o solicitud.

## Observabilidad

**WARNING**

**OBSERVED**

`frontdesk-service` y `guest-service` incluyen:

- Spring Boot Actuator.
- Endpoint Prometheus.
- Health y readiness/liveness probes.
- Micrometer tracing con Brave.
- Exportación Zipkin.
- Logs con `correlationId`.

`frontdesk-service` registra errores del proveedor con estado HTTP y cuerpo sanitizado/truncado.

El circuit breaker `aiProvider` tiene `registerHealthIndicator: true`.

`common-web-lib` contiene manejo compartido de `ProblemDetail`, pero no contiene métricas ni instrumentación específica de AI.

**INFERENCE**

Existe observabilidad técnica general, pero no se observaron métricas AI dedicadas para:

- Latencia del proveedor.
- Número de solicitudes.
- Éxitos y fallos por proveedor.
- Reintentos.
- Circuit-open events.
- Fallback activations.
- Tokens consumidos.
- Errores por modelo o tenant.

La exposición del health indicator no demuestra que exista un dashboard, alerta o consulta operativa configurada para `aiProvider`.

## guest-service

**PASS**

**OBSERVED**

No se encontró integración directa con proveedores AI dentro de `guest-service/src`.

El servicio sí usa Resilience4j para clientes Feign operativos, por ejemplo `StayServiceClient`, pero no tiene un flujo AI propio ni configuración `aiProvider`.

Cuenta con Actuator, Prometheus, tracing y health endpoints mediante configuración compartida.

**INFERENCE**

No hay una superficie AI en `guest-service` que requiera circuit breaker, retry o token tracking específicos.

## common-web-lib

**PASS**

**OBSERVED**

`common-web-lib` contiene utilidades web y manejo común de errores.

No se encontraron clientes AI, decoradores AI, circuit breakers AI, retry policies AI ni token tracking.

**INFERENCE**

La ausencia de lógica AI en esta librería reduce el riesgo de duplicación, pero también significa que no existe una instrumentación AI compartida entre servicios.

## config-service

**PASS**

**OBSERVED**

`config-service` sirve configuración nativa desde `classpath:/config/`.

La configuración AI/resiliencia observada está en `config-service/src/main/resources/config/frontdesk-service.yml`.

Los valores sensibles se referencian mediante variables de entorno; no se imprimieron valores `.env`.

**INFERENCE**

El comportamiento de resiliencia AI depende de que la configuración servida por Config Server sea la versión efectivamente cargada por `frontdesk-service`. Esto no fue verificado en runtime porque el A.SPEC exige inspección estática y prohíbe ejecutar tareas de build o pruebas.

## Riesgos y gaps restantes

### WARNING — Falta token tracking

No hay evidencia de extracción, persistencia o métricas de tokens consumidos por solicitudes AI.

### WARNING — Falta observabilidad AI específica

La observabilidad existente es genérica. No hay métricas o alertas explícitas para latencia, retries, fallbacks, circuit-open, proveedor, modelo o tenant.

### WARNING — Falta prueba runtime de Resilience4j

Las pruebas observadas no prueban la aplicación efectiva de los decoradores Spring ni las transiciones del circuito.

### WARNING — Cliente AI acoplado a `AssistantService`

El uso directo de `HttpClient` y selección interna DeepSeek/Ollama limita la sustitución de proveedor, failover explícito y pruebas contractuales del cliente.

### UNKNOWN — Configuración efectiva en ejecución

No se verificó qué configuración está cargada actualmente por el servicio ni si los endpoints Actuator, Prometheus y Zipkin están operativos.

### UNKNOWN — Disponibilidad real de proveedores

No se realizaron llamadas a DeepSeek, Ollama ni a ningún proveedor AI externo, conforme a las restricciones del A.SPEC.

## Clasificación final

| Área | Clasificación |
|---|---|
| Workspace sin mutaciones | PASS |
| Autorización y tenant context | PASS |
| Circuit breaker AI | PASS |
| Retry policy AI | PASS |
| Fallback determinista | PASS |
| Pruebas unitarias de lógica | PASS |
| Pruebas runtime de resiliencia | WARNING |
| Timeout de cliente | PASS |
| Token tracking | WARNING |
| Observabilidad general | PASS |
| Observabilidad AI específica | WARNING |
| Integración AI en guest-service | PASS |
| Integración AI en common-web-lib | PASS |
| Configuración runtime efectiva | UNKNOWN |
| Disponibilidad real de proveedores | UNKNOWN |

## Conclusión

La integración AI de `frontdesk-service` ya cuenta con timeout, retry, circuit breaker, clasificación de errores, fallback determinista, autorización y pruebas unitarias relevantes.

La preparación operativa no está completa: faltan token tracking, métricas AI específicas, alertas operativas y pruebas runtime que demuestren las transiciones reales de Resilience4j. No se identificó un BLOCKER estático dentro del alcance de esta inspección.
