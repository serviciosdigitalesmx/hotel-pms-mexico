# A.SPEC AI-0007 — Inventario real de integración IA

**Estado:** PASS con WARNINGS  
**Modo:** READ_ONLY  
**Cambios realizados:** Ninguno  
**Proveedores externos invocados:** Ninguno

## 1. Integración IA existente

**Clasificación: PASS**

### Evidencia observada

- Existe un endpoint real:

  `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantController.java:18-34`

  - `POST /api/v1/stays/assistant/chat`
  - Requiere `ADMIN`, `OWNER` o `RECEPTIONIST`.
  - Obtiene `hotelId` y roles desde el contexto autenticado.

- Existe implementación real del cliente IA:

  `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java:35-97`

- El servicio realiza llamadas HTTP mediante `java.net.http.HttpClient`.

- La respuesta esperada es compatible con Chat Completions e incluye `choices[0].message`, contenido y `tool_calls`:

  `AssistantService.java:199-216`

### Inferencia

La integración IA no es solamente documental ni un stub: existe una ruta HTTP, un servicio ejecutable, DTOs y construcción de solicitudes al proveedor.

## 2. Proveedores y rutas

**Clasificación: PASS**

### Evidencia observada

- DeepSeek:

  `AssistantService.java:41`

  `https://api.deepseek.com/chat/completions`

- Ollama local o compatible con OpenAI:

  `AssistantService.java:258-275`

  - Lee `OLLAMA_BASE_URL` mediante `System.getenv`.
  - Si no existe, utiliza:

    `http://host.docker.internal:11434/v1/chat/completions`

- La selección depende del modelo:

  `AssistantService.java:279-288`

  - Modelos cuyo identificador inicia con `deepseek-` usan DeepSeek.
  - Los demás usan Ollama.

- El modelo se configura por hotel mediante `HotelSettings.aiModel`:

  `frontdesk-service/src/main/java/com/hotelpms/frontdesk/stays/domain/HotelSettings.java:123-138`

- Las migraciones muestran cambios históricos entre Groq, DeepSeek y Ollama:

  - `V17__add_tenant_ai_assistant.sql`
  - `V18__use_low_cost_ai_model.sql`
  - `V19__replace_retired_groq_model.sql`
  - `V20__force_groq_ai_provider.sql`
  - `V21__migrate_ai_to_deepseek.sql`
  - `V22__use_ollama_by_default_keep_deepseek_option.sql`
  - `V23__use_qwen3_4b_instruct_q4.sql`

### Inferencia

La implementación activa actual soporta dos destinos: Ollama y DeepSeek. Las referencias a Groq son históricas y no prueban que Groq siga operativo.

## 3. Secretos y credenciales

**Clasificación: PASS con WARNING**

### Evidencia observada

- La clave IA por hotel se almacena en el campo cifrado:

  `HotelSettings.java:136-138`

  `ai_api_key_encrypted`

- La clave se descifra únicamente cuando el modelo seleccionado es DeepSeek:

  `AssistantService.java:86-90`

- La configuración exige clave para modelos DeepSeek:

  `AssistantService.java:99-107`

- La clave no se incluye en la respuesta de configuración según el contrato documentado en:

  `HotelSettings.java:136`

- No se le solicita al modelo procesar contraseñas, tokens o credenciales:

  `AssistantService.java:191-192`

### WARNING — observabilidad de errores

`AssistantService.java:203-207` registra el cuerpo de error del proveedor mediante funciones de sanitización y truncamiento. La revisión confirma sanitización parcial, pero no demuestra una política completa de eliminación de datos personales para todos los cuerpos devueltos por proveedores.

No se leyeron valores de `.env` ni secretos.

## 4. Contrato de entrada y salida

**Clasificación: PASS**

### Evidencia observada

- Entrada:

  `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/dto/AssistantChatRequest.java:14-22`

  - La conversación es obligatoria.
  - Máximo de 40 mensajes.

- Mensajes:

  `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/dto/AssistantMessage.java`

  - Roles limitados a `user`, `assistant` y `tool`.
  - Incluye contenido, identificadores de tool y llamadas de herramientas.

- Salida:

  `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/dto/AssistantChatResponse.java`

  - `answer`
  - `toolCalls`

- Solicitud enviada al proveedor:

  `AssistantService.java:110-143`

  Incluye:

  - modelo
  - mensajes
  - temperatura `0.1`
  - máximo `1040` tokens
  - herramientas
  - `tool_choice: auto`

## 5. Herramientas y límites operativos

**Clasificación: PASS con WARNING**

### Evidencia observada

- Las herramientas están allow-listed:

  `frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantToolCatalog.java:62-75`

- Se exponen dos operaciones abstractas:

  - `consultar_pms`
  - `proponer_accion_pms`

- Las acciones requieren confirmación humana:

  `AssistantToolCatalog.java:78-85`

- El prompt operativo ordena que las acciones nunca se ejecuten directamente:

  `AssistantService.java:183-196`

- El endpoint aplica autorización por rol:

  `AssistantController.java:25-26`

### WARNING

En los archivos inspeccionados se observa la generación y devolución de propuestas de acción, pero no una ruta activa en `AssistantController` que ejecute esas acciones. Por tanto, no debe afirmarse que exista ejecución transaccional de herramientas desde esta integración IA.

## 6. Timeouts, retries, circuit breakers y fallback

### Timeout

**Clasificación: PASS**

### Evidencia observada

- Timeout de conexión HTTP: 10 segundos.

  `AssistantService.java:53-55`

- Timeout total por solicitud: 30 segundos.

  `AssistantService.java:81-84`

### Retries

**Clasificación: WARNING**

No se observaron retries en `AssistantService.java`.

### Circuit breaker

**Clasificación: WARNING**

No se observó `@CircuitBreaker` ni una configuración Resilience4j asociada al cliente IA. Los circuit breakers encontrados en configuración corresponden a otros clientes Feign, no a esta integración:

`config-service/src/main/resources/config/frontdesk-service.yml:98-111`

### Fallback

**Clasificación: WARNING**

Existe un router determinista:

`frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java`

También existe lógica documentada para intentar rutas locales cuando Ollama falla. Sin embargo:

- El controlador activo inyecta directamente `AssistantService`:

  `AssistantController.java:23-34`
- La ruta efectiva del endpoint activo no demuestra que `LocalIntentRouter` esté conectado al controlador.
- Los controladores `.backup-*` no son evidencia de la implementación activa.

### Inferencia

El fallback local existe en código, pero su uso por la ruta HTTP activa no queda demostrado con la estructura actualmente inspeccionada. Debe clasificarse como capacidad potencial, no como fallback operativo confirmado.

## 7. Datos personales y datos del hotel enviados

**Clasificación: WARNING**

### Evidencia observada

El prompt enviado al proveedor contiene:

`AssistantService.java:183-196`

- Nombre del hotel.
- Zona horaria.
- Instrucciones específicas del hotel.
- Conversación del usuario.
- Mensajes previos.
- Resultados y llamadas de herramientas cuando forman parte de la conversación.

El DTO permite contenido arbitrario limitado por validaciones de tamaño en los mensajes y herramientas.

### Inferencia

La integración puede exponer al proveedor externo datos personales o información operativa del hotel si dichos datos están presentes en la conversación o en resultados de herramientas. No se observó en esta inspección:

- clasificación automática de PII;
- redacción de nombres, correos, teléfonos o documentos;
- política de retención del proveedor;
- consentimiento o control por tipo de dato;
- garantía de que todos los resultados PMS permanezcan locales.

El prompt prohíbe inventar o solicitar determinadas credenciales, pero eso no equivale a una capa técnica de prevención de PII.

## 8. Estado de compilación y estado del workspace

**Clasificación: UNKNOWN**

### Evidencia observada

No se ejecutó compilación ni pruebas, porque el A.SPEC exige inventario de lectura y no requiere comandos de build.

`git status` mostró cambios preexistentes en varios archivos, incluidos archivos del asistente y configuración. No se modificaron, descartaron, limpiaron ni sobrescribieron.

### Inferencia

No es posible certificar desde esta ejecución que la compilación previamente verificada permanezca verde. Sí es posible certificar que esta ejecución no realizó cambios.

## 9. Inventario reproducible

Comando de verificación ejecutado:

```text
rg -n -i "openai|anthropic|llm|embedding|prompt|chatcompletion|ai|provider" README.md docs config-service/src/main/resources api-gateway/src/main frontdesk-service/src/main
```

La búsqueda encontró evidencia real de:

- endpoint IA;
- servicio HTTP;
- DeepSeek;
- Ollama;
- configuración por hotel;
- claves cifradas;
- herramientas PMS;
- prompt;
- timeouts;
- fallback determinista;
- migraciones históricas de proveedor.

También encontró coincidencias no relacionadas con integración IA, por ejemplo `available`, `billing`, `ai` dentro de palabras italianas o documentación sobre uso de herramientas de IA durante desarrollo. Esas coincidencias no se contaron como capacidades operativas.

## 10. Gaps antes de una futura modificación WRITE

1. Confirmar mediante wiring real si `LocalIntentRouter` participa en la ruta activa `/api/v1/stays/assistant/chat`.
2. Definir si el fallback local es garantía operativa o código no conectado.
3. Añadir, si se autoriza posteriormente, límites explícitos de retries y circuit breaker para el proveedor IA.
4. Documentar proveedor, residencia, retención y tratamiento de PII.
5. Definir una política técnica de redacción o exclusión de datos personales antes de enviarlos a DeepSeek.
6. Verificar la configuración real de `OLLAMA_BASE_URL` sin imprimir valores.
7. Ejecutar build y pruebas únicamente en una A.SPEC posterior que lo autorice.

## Veredicto

**PASS:** Existe una integración IA real, autenticada, multi-tenant, con endpoint, proveedor, contratos, modelo configurable, timeout y herramientas allow-listed.

**WARNING:** No se observaron retries ni circuit breaker específicos para IA; el fallback determinista no queda probado como conectado a la ruta activa; y el tratamiento técnico de PII antes de enviar conversaciones o datos PMS a proveedores externos no está demostrado.

**BLOCKER:** Ninguno para este A.SPEC READ_ONLY.

**UNKNOWN:** Estado actual de compilación y disponibilidad real de los proveedores, porque no se ejecutaron builds, requests externas ni lecturas de secretos.
