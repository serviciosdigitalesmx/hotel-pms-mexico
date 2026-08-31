# A.SPEC AI-0046 — Diagnóstico de `LocalIntentRouterTest`

## Resultado

**BLOCKER — Causa raíz identificada.**

Los fallos no se originan en la aserción de la línea 236. Esa línea únicamente invoca:

```java
router.processRequest(...)
```

La excepción `IllegalStateException` es lanzada por el mock de `assistantService` durante el flujo AI-first.

## Evidencia observada

### Test

En `LocalIntentRouterTest.java`:

- `@BeforeEach` configura `assistantService.chat(...)` para lanzar:

```java
new IllegalStateException("AI provider unavailable")
```

- La línea 236 ejecuta `router.processRequest(...)`.
- El constructor de prueba crea un `LocalIntentRouter` con el `ResilientIntentFallbackHandler` por defecto.
- El entorno de prueba no sobrescribe `ASSISTANT_AI_FIRST`.

### Implementación

En `LocalIntentRouter.java`:

- `aiFirstEnabled()` devuelve `true` cuando `ASSISTANT_AI_FIRST` está ausente o vacío.
- Por ello, `processRequest()` ejecuta primero:

```java
fallbackHandler.resolve(
    () -> assistantService.chat(...),
    () -> processDeterministically(...));
```

En `ResilientIntentFallbackHandler.java`:

- Solo reconoce como errores recuperables:

  - `RetryableAiProviderException`
  - `PermanentAiProviderException`
  - `CallNotPermittedException`
  - `RequestNotPermitted`

- Una `IllegalStateException` no es considerada fallo del proveedor.
- El handler la registra y la vuelve a lanzar:

```java
throw providerFailure;
```

Por tanto, el flujo determinista nunca se alcanza.

## Inferencia

Los nueve tests que esperan que el router local procese intenciones deterministas fallan porque el mock usa `IllegalStateException`, mientras que la nueva frontera de fallback exige excepciones específicas del proveedor AI.

La línea 236 es únicamente el punto común donde todos los tests reciben la excepción; no es la causa lógica del fallo.

## Clasificación

| Hallazgo | Clasificación |
|---|---|
| La línea 236 es el punto común de invocación | PASS |
| `ASSISTANT_AI_FIRST` está habilitado por defecto | OBSERVED / WARNING |
| El mock lanza `IllegalStateException` para simular indisponibilidad AI | OBSERVED |
| `IllegalStateException` no activa el fallback actual | OBSERVED |
| La excepción se propaga antes del router determinista | OBSERVED |
| Causa raíz de los nueve fallos | BLOCKER para la suite actual |
| Ejecución de tests durante este diagnóstico | UNKNOWN; no se ejecutó por modo READ_ONLY |

## Conclusión

La incompatibilidad está entre el tipo de excepción configurado por `LocalIntentRouterTest` y los tipos aceptados por `ResilientIntentFallbackHandler`.

No se realizaron modificaciones, ejecuciones de build/test, cambios de Git, cambios de configuración, acceso a secretos ni operaciones externas.
