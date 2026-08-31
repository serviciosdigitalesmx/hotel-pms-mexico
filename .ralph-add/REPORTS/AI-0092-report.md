# A.SPEC AI-0092 — Inspeccionar bloqueo determinista de pruebas frontend

## Resultado

**PASS — inspección READ_ONLY completada**

La configuración frontend identifica correctamente a **Vitest** como runner. El bloqueo previo de `npm --prefix frontend test` proviene de la política del supervisor, no de una causa observable en `package.json`, configuración o dependencias.

No se modificaron archivos ni estado del repositorio.

## Alcance revisado

- `frontend/package.json`
- `frontend/package-lock.json`
- `frontend/vite.config.ts`
- `frontend/tsconfig.vitest.json`
- `frontend/src/setupTests.ts`
- Suites bajo `frontend/src`
- Presencia local de Vitest
- Estado Git del worktree

## Runner y scripts

Fuente: [frontend/package.json](/Users/usuario/Desktop/HOTEL-PMS/frontend/package.json:6)

| Script | Comando | Estado |
|---|---|---|
| `test` | `vitest run` | Confirmado |
| `test:coverage` | `vitest run --coverage` | Confirmado |
| `test:watch` | `vitest` | Confirmado |
| `test:e2e` | `playwright test` | Confirmado |
| `test:e2e:ci` | `playwright test` | Confirmado |
| `test:e2e:live` | `playwright test --config=playwright-live.config.ts` | Confirmado |

Comando determinista correcto:

```bash
npm --prefix frontend test
```

## Configuración Vitest

Fuente: [frontend/vite.config.ts](/Users/usuario/Desktop/HOTEL-PMS/frontend/vite.config.ts:1)

- Configuración integrada en Vite.
- No existe `vitest.config.*` independiente.
- Entorno: `jsdom`.
- Globals habilitados.
- Setup: `src/setupTests.ts`.
- Exclusión de `e2e/**` y `e2e-live/**`.
- Cobertura mediante `v8`.
- Reportes: `text`, `html`, `lcov`.
- Umbrales configurados:
  - Statements: 90%
  - Branches: 80%
  - Functions: 84%
  - Lines: 92%

## Setup de pruebas

Fuente: [frontend/src/setupTests.ts](/Users/usuario/Desktop/HOTEL-PMS/frontend/src/setupTests.ts:1)

Incluye:

- `@testing-library/jest-dom/vitest`
- `vitest-axe`
- Matchers de accesibilidad
- Implementación local de `localStorage`
- Implementación de `window.matchMedia`
- Extensión de `expect`

El tipado de Vitest está declarado en [frontend/tsconfig.vitest.json](/Users/usuario/Desktop/HOTEL-PMS/frontend/tsconfig.vitest.json:1).

## Dependencias y suites

Confirmado:

- `frontend/package-lock.json` existe.
- `vitest` está declarado en `devDependencies`.
- `jsdom` está declarado.
- `@testing-library/react` está declarado.
- `@testing-library/jest-dom` está declarado.
- `vitest-axe` está declarado.
- `frontend/node_modules/vitest` está presente.
- `frontend/node_modules/.bin/vitest` está presente y es ejecutable.
- Se encontraron **89 archivos** de pruebas bajo `frontend/src`.

## Evidencia del bloqueo

Los reportes previos registran:

```text
$ npm --prefix frontend test
BLOCKED_BY_SUPERVISOR
```

El supervisor clasifica explícitamente ese marcador como bloqueo de política y no como fallo del producto.

Por tanto:

| Pregunta | Resultado |
|---|---|
| Runner identificado | PASS |
| Script npm identificado | PASS |
| Configuración presente | PASS |
| Dependencias declaradas | PASS |
| Dependencia ejecutable localmente presente | PASS |
| Suites existentes | PASS estructural |
| Suite funcionalmente verde | UNKNOWN |
| Causa del bloqueo observable en el proyecto | No evidenciada |
| Bloqueo por política del supervisor | CONFIRMADO históricamente |
| Ejecución de Vitest en AI-0092 | No realizada |
| Cambios de archivos | Ninguno |

## Estado del worktree

El estado inicial y final conserva cambios preexistentes en múltiples servicios y archivos frontend. También existen archivos no rastreados.

La verificación final de Git mostró la misma rama:

```text
## recovery/20260817-073005
```

No se ejecutaron:

- instalación o actualización de dependencias;
- pruebas;
- builds;
- migraciones;
- Docker;
- deploy;
- limpieza;
- reset;
- checkout;
- stash;
- edición de archivos.

Git emitió advertencias ambientales de macOS relacionadas con `xcrun` y archivos temporales; no alteraron el worktree.

## Conclusión

AI-0092 queda satisfecha como auditoría READ_ONLY.

El comando reproducible para la siguiente A.SPEC de verificación es:

```bash
npm --prefix frontend test
```

El resultado real de la suite —PASS o FAIL, conteo y errores— sigue pendiente de una A.SPEC que autorice ejecutar Vitest.
