# A.SPEC AI-0015 — Diagnóstico de `LocalIntentRouterTest`

## Estado

**WARNING** — La inspección fue realizada en modo estrictamente READ_ONLY. No se modificaron archivos, Git, bases de datos, Redis, Docker, migraciones ni secretos. No se ejecutó la prueba porque el comando de verificación puede generar artefactos de build.

## Evidencia observada

### 1. El router intenta primero `assistantService.chat`

En `LocalIntentRouter.java`:

- Líneas 102–105: si `aiFirstEnabled()` devuelve `true`, se invoca directamente:

```java
return assistantService.chat(hotelId, roles, request);
```

- Líneas 105–111: cualquier `RuntimeException` del servicio AI se captura y después se continúa con el flujo determinista local.
- Líneas 1574–1581: `aiFirstEnabled()` devuelve `true` cuando `ASSISTANT_AI_FIRST` no está definido, está vacío o tiene cualquier valor distinto de `"false"`.

**Clasificación: PASS** — Existe un fallback determinista explícito cuando el proveedor AI falla.

### 2. El test configura el mock AI para lanzar `IllegalStateException`

En `LocalIntentRouterTest.java`:

- Líneas 82–83:

```java
lenient().doThrow(new IllegalStateException("AI provider unavailable"))
        .when(assistantService).chat(any(), any(), any());
```

Esto provoca que cada llamada al router, bajo AI-first habilitado, registre una invocación a `assistantService.chat(...)` antes de continuar al flujo local.

**Clasificación: PASS** — La configuración reproduce un proveedor AI no disponible.

### 3. Primera expectativa incompatible con AI-first

En `LocalIntentRouterTest.java`:

- Líneas 100–106: `preservesKnownSlotsAndAsksOnlyForDates()` espera que no se invoque AI:

```java
verify(assistantService, never()).chat(any(), any(), any());
```

Sin embargo, con `aiFirstEnabled() == true`, `LocalIntentRouter.processRequest()` invoca `assistantService.chat(...)` en las líneas 102–105 antes del fallback.

**Clasificación: BLOCKER para esta aserción** — La implementación actual contradice directamente la verificación `never()`.

### 4. Segunda expectativa incompatible con AI-first

En `LocalIntentRouterTest.java`:

- Líneas 224–230: `repeatedConfirmationDoesNotDuplicateCheckIn()` realiza dos llamadas al router y finalmente espera:

```java
verify(assistantService, never()).chat(any(), any(), any());
```

Con AI-first habilitado, ambas llamadas pasan primero por `assistantService.chat(...)`. Aunque el mock lanza `IllegalStateException` y el router realiza fallback, Mockito conserva el registro de las invocaciones.

**Clasificación: BLOCKER para esta aserción** — El fallback evita que la excepción salga del método, pero no evita que Mockito registre la llamada.

### 5. La prueba de fallback AI sí coincide con el comportamiento actual

En `LocalIntentRouterTest.java`:

- Líneas 157–163: `unknownMessageUsesGroqFallbackUnchanged()` configura `assistantService.chat(...)` para devolver una respuesta y espera recibir exactamente esa misma instancia.
- En `LocalIntentRouter.processRequest()`, AI-first devuelve directamente la respuesta en las líneas 102–105 cuando el mock no lanza excepción.

**Clasificación: PASS** — Esta prueba es coherente con el comportamiento AI-first observado.

### 6. El `IllegalStateException` configurado es capturado en el nivel correcto

En `LocalIntentRouter.java`:

- Líneas 105–111: el `IllegalStateException` producido por `assistantService.chat(...)` en modo AI-first se captura como `RuntimeException`.
- Líneas 169–173: existe otro manejo de `IllegalStateException`, pero pertenece al flujo determinista dentro de `processLocked()` y devuelve una respuesta de error conservando la sesión.

**Clasificación: PASS** — El `IllegalStateException` del mock AI no debería propagarse fuera de `processRequest()` cuando ocurre durante el bloque AI-first.

**INFERENCE:** Si AI-0014 reportó un `IllegalStateException` propagado, probablemente ocurrió fuera del bloque AI-first, durante el flujo determinista, o el fallo correspondió a una versión distinta del archivo actualmente inspeccionado. La evidencia estática actual no permite identificar una línea exacta de propagación sin ejecutar la prueba.

## Diagnóstico de los tres fallos reportados

### Fallo 1

- Test: `preservesKnownSlotsAndAsksOnlyForDates`
- Línea: 106
- Expectativa: `verify(assistantService, never()).chat(...)`
- Causa observada: AI-first invoca `assistantService.chat(...)` en `LocalIntentRouter.java:102–105`.

**Clasificación: BLOCKER**

### Fallo 2

- Test: `repeatedConfirmationDoesNotDuplicateCheckIn`
- Línea: 230
- Expectativa: `verify(assistantService, never()).chat(...)`
- Causa observada: cada llamada pasa primero por AI-first y el mock lanza después la excepción configurada.

**Clasificación: BLOCKER**

### Fallo 3

- El tercer fallo no puede asignarse con certeza a una línea concreta únicamente con la información de la A.SPEC y la inspección estática.
- La única fuente explícita disponible es la configuración de `IllegalStateException` en `LocalIntentRouterTest.java:82–83` y los manejadores correspondientes en `LocalIntentRouter.java:105–111` y `169–173`.

**Clasificación: UNKNOWN**

**INFERENCE:** El tercer fallo podría ser otra expectativa afectada por el cambio AI-first, o una excepción ocurrida dentro del flujo determinista. Se requiere la salida real de Gradle/JUnit para confirmarlo.

## Causa raíz probable

**INFERENCE:** El test fue escrito bajo el supuesto de que el router determinista se ejecutaba directamente y que `assistantService.chat(...)` solo se invocaba para mensajes desconocidos. La implementación actual cambió el contrato a AI-first: intenta AI para toda solicitud cuando `ASSISTANT_AI_FIRST` no es `"false"`.

La incompatibilidad principal es:

```text
Test: assistantService.chat nunca debe invocarse
Implementación: assistantService.chat se intenta primero para toda solicitud
```

## Fix dirigido sugerido para una A.SPEC posterior

No se aplicó ningún cambio por el modo READ_ONLY.

El cambio debe decidir explícitamente entre una de estas dos estrategias:

1. Adaptar el test al contrato AI-first, verificando que el router haga fallback tras el `IllegalStateException`, sin exigir `never()`.
2. Hacer que el test fuerce AI-first deshabilitado durante la prueba, si el objetivo específico es probar exclusivamente el router determinista.

No hay evidencia suficiente para recomendar cambiar la implementación productiva del router.
