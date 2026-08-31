# A.SPEC AI-0122 — Reporte de diagnóstico Vitest

## Resultado

La ejecución no alcanzó la fase de descubrimiento ni ejecución de tests. Ambos comandos fueron bloqueados por el supervisor del entorno con `EPERM` al intentar crear el artefacto temporal de Vite:

```text
frontend/node_modules/.vite-temp/vite.config.ts.timestamp-*.mjs
```

Por lo tanto, no es posible confirmar ni clasificar los 21 fallos de assertions en esta ejecución. Los códigos `1` observados corresponden al bloqueo del runner, no a fallos reproducibles de tests.

## Comandos ejecutados

| Comando | Resultado |
|---|---|
| `npm --prefix frontend test -- --run --reporter=verbose` | Exit `1`; bloqueo `EPERM` cargando `vite.config.ts` |
| `npm --prefix frontend test -- --run src/pages/Stays.test.tsx --reporter=verbose` | Exit `1`; mismo bloqueo `EPERM` |
| `git diff --check` | Exit `0`; limpio |

No se modificaron archivos, Git, secretos, bases de datos, servicios ni infraestructura.

## Evidencia del runner

La configuración vigente usa:

- `vitest run`
- entorno `jsdom`
- `frontend/src/setupTests.ts`
- plugin React
- plugin ESLint de Vite

El fallo ocurre antes de cargar los tests:

```text
failed to load config from .../frontend/vite.config.ts
Error: EPERM: operation not permitted, open
.../frontend/node_modules/.vite-temp/vite.config.ts.timestamp-*.mjs
```

Esto se clasifica como:

> Bloqueo del supervisor/permisos del entorno — no es un fallo funcional de Vitest ni una regresión comprobada.

## Revisión estática de `Stays`

`Stays.test.tsx` contiene 17 casos, incluyendo:

- carga inicial
- render exitoso
- estado vacío
- errores de carga
- badges Alloggiati, factura y checkout
- filtros
- ordenamiento
- accesibilidad
- navegación al huésped
- permisos de exportación JSON para `RECEPTIONIST`, `ADMIN` y `OWNER`

La implementación actual de [`Stays.tsx`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0122-9p2d39ke/frontend/src/pages/Stays.tsx:25) no renderiza `AlloggiatiReportSection`.

La sección que contiene el botón `download_json_export` está en [`AlloggiatiReportSection.tsx`](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0122-9p2d39ke/frontend/src/pages/Stays/AlloggiatiReportSection.tsx:19), pero no aparece importada ni montada dentro de `Stays.tsx`.

### Clasificación estática de las expectativas JSON

| Caso | Evidencia | Clasificación |
|---|---|---|
| `RECEPTIONIST` no ve `download_json_export` | El componente no está montado | No verificable por runner; expectativa probablemente desactualizada respecto al montaje actual |
| `ADMIN` ve `download_json_export` | El componente no está montado | No verificable por runner; expectativa incompatible con la implementación actual |
| `OWNER` ve `download_json_export` | El componente no está montado | No verificable por runner; expectativa incompatible con la implementación actual |

La autorización interna de `AlloggiatiReportSection` sí es coherente: muestra el botón únicamente cuando `isAdminOrOwner` es verdadero, y el componente tiene cobertura propia en `AlloggiatiReportSection.test.tsx`.

## Mocks y traducciones

`Stays.test.tsx`:

- mockea `react-i18next` devolviendo cada clave sin traducir;
- mockea `stayService`;
- mockea `useAuthStore`;
- mockea navegación y toast;
- resetea mocks en `beforeEach`.

No se pudo observar ejecución real de estos mocks debido al bloqueo de configuración de Vite.

## Clasificación de los 21 fallos solicitados

| Categoría | Resultado |
|---|---:|
| Regresiones reales confirmadas | 0 |
| Expectativas desactualizadas identificadas estáticamente | 3 expectativas JSON en `Stays.test.tsx` |
| Fallos de aislamiento de mocks/traducciones confirmados | 0 |
| Problemas del runner | 2 ejecuciones bloqueadas por `EPERM` |
| Fallos de assertion reproducidos | 0 |
| Fallos no reproducibles en este entorno | 21 — no alcanzables por el bloqueo previo |

No sería correcto asignar los otros 18 fallos a una causa concreta sin el reporte verbose real.

## Cambio mínimo verificable para la futura reparación

La siguiente A.SPEC debe comenzar resolviendo únicamente el bloqueo operativo del runner, sin modificar código de aplicación:

1. Permitir que Vite cree su caché temporal `.vite-temp`, o ejecutar Vitest con una caché temporal escribible.
2. Repetir los dos comandos definidos por la A.SPEC.
3. Capturar el reporte verbose completo y los nombres exactos de cada assertion fallida.
4. Ejecutar después `Stays.test.tsx` de forma aislada.
5. Confirmar si el contrato esperado es:
   - montar `AlloggiatiReportSection` dentro de `Stays`, o
   - eliminar/mover las tres expectativas JSON de `Stays.test.tsx`.

No debe hacerse una reparación funcional hasta obtener primero el reporte de assertions real.

## Estado de Git

El worktree ya contenía numerosos cambios preexistentes y archivos no rastreados fuera del alcance de esta A.SPEC. No se modificaron.

`git diff --check` terminó correctamente con exit `0`.

## Cierre

La A.SPEC queda parcialmente bloqueada por permisos del supervisor. El alcance funcional no puede cerrarse todavía porque Vitest no llegó a ejecutar ningún test. La única causa concreta identificada además del bloqueo es la probable desalineación entre las expectativas JSON de `Stays.test.tsx` y el hecho de que `AlloggiatiReportSection` no está montado en `Stays.tsx`.
