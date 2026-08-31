# A.SPEC AI-0080 — Reporte de auditoría

## Resultado

**Estado: PASS**

El frontend tiene un contrato local de pruebas definido y ejecutable en principio:

- Unitarias/integración: **Vitest**
- Comando principal: `npm --prefix frontend test`
- Equivalente directo: `npm --prefix frontend run test`
- E2E mockeadas: **Playwright**
- E2E live: **Playwright** con `playwright-live.config.ts`

No se modificaron archivos, dependencias, servicios ni estado de Git.

## Evidencia observada

### Scripts disponibles

Archivo: `frontend/package.json`

```json
"test": "vitest run",
"test:coverage": "vitest run --coverage",
"test:watch": "vitest",
"test:e2e": "playwright test",
"test:e2e:ci": "playwright test",
"test:e2e:live": "playwright test --config=playwright-live.config.ts"
```

`npm --prefix frontend run` confirmó todos los scripts anteriores.

### Runner unitario

Vitest está configurado mediante `frontend/vite.config.ts`:

- `environment: 'jsdom'`
- `globals: true`
- `setupFiles: './src/setupTests.ts'`
- Exclusión explícita de `e2e/**` y `e2e-live/**`
- Cobertura con `@vitest/coverage-v8`

El comando determinista confirmado es:

```bash
npm --prefix frontend test
```

### Dependencias

El `frontend/package-lock.json` contiene:

- `vitest`
- `vite`
- `jsdom`
- `@vitest/coverage-v8`
- `@testing-library/react`
- `@testing-library/jest-dom`
- `vitest-axe`
- `@playwright/test`

También se verificó resolución local de:

```text
frontend/node_modules/vitest/package.json
frontend/node_modules/@playwright/test/package.json
frontend/node_modules/vite/package.json
frontend/node_modules/jsdom/package.json
```

Por tanto, las dependencias necesarias para cargar ambos runners están presentes localmente.

### Suites encontradas

Conteo estructural:

- 71 archivos `*.test.tsx`
- 18 archivos `*.test.ts`
- 18 especificaciones Playwright entre `e2e/` y `e2e-live/`

Las pruebas unitarias se encuentran principalmente bajo `frontend/src`.

Las pruebas E2E mockeadas están bajo `frontend/e2e`.

Las pruebas E2E contra backend real están bajo `frontend/e2e-live`.

### Playwright

`frontend/playwright.config.ts` confirma:

- `testDir: './e2e'`
- Base URL local por defecto: `http://localhost:5173`
- `webServer` local con `npm run dev`
- Reutilización del servidor existente
- Sin reintentos localmente
- Chromium como proyecto

`frontend/playwright-live.config.ts` confirma:

- `testDir: './e2e-live'`
- Base URL por defecto: `http://localhost`
- Dependencia entre setup y pruebas live
- Timeout de 30 segundos
- Un worker
- Uso de `e2e-live/.auth/admin.json`
- Ejecución prevista contra el stack Docker real

## Clasificación

| Área | Estado | Evidencia |
|---|---|---|
| Script unitario | PASS | `test: vitest run` |
| Runner unitario | PASS | Vitest declarado, configurado y resuelto localmente |
| Entorno unitario | PASS | `jsdom` y `setupTests.ts` configurados |
| Cobertura | PASS | Provider V8 y umbrales definidos |
| Suites unitarias | PASS | 89 archivos de pruebas encontrados |
| Runner E2E | PASS | Playwright declarado y resuelto localmente |
| Suites E2E mockeadas | PASS | Especificaciones bajo `frontend/e2e` |
| Suites E2E live | PASS | Configuración y especificaciones bajo `frontend/e2e-live` |
| Dependencias instaladas | PASS | Resolución local confirmada |
| Ejecución real de Vitest | UNKNOWN | No ejecutada; fuera del alcance READ_ONLY |
| Ejecución real de Playwright | UNKNOWN | No ejecutada; requeriría servidor o stack |
| Resultado funcional de las suites | UNKNOWN | La inspección no demuestra tests verdes |
| Estado limpio del worktree | WARNING | Ya existían numerosos cambios y archivos no rastreados antes de esta inspección |

## Huecos y bloqueos

1. No se ejecutó `vitest run`; por tanto, no se confirma que las pruebas actuales pasen.
2. No se ejecutó Playwright; por tanto, no se confirma disponibilidad del navegador ni funcionamiento E2E.
3. Las pruebas live requieren servicios externos al runner local, incluyendo el stack Docker y estado autenticado.
4. El script `test:e2e:live` depende de `e2e-live/.auth/admin.json`; su validez no fue comprobada.
5. `npm run` solo confirma el registro de scripts, no su éxito funcional.
6. El worktree está preexistente y ampliamente modificado; no se atribuyen esos cambios a AI-0080.

## Comando confirmado para el siguiente A.SPEC

Para pruebas unitarias frontend:

```bash
npm --prefix frontend test
```

Para cobertura:

```bash
npm --prefix frontend run test:coverage
```

Para E2E mockeadas:

```bash
npm --prefix frontend run test:e2e
```

Para E2E live, únicamente con stack y autenticación disponibles:

```bash
npm --prefix frontend run test:e2e:live
```

## Acciones no realizadas

- No se modificaron archivos.
- No se instalaron dependencias.
- No se ejecutaron tests.
- No se ejecutó build o lint.
- No se levantaron servicios.
- No se realizaron llamadas externas.
- No se modificó Git, PostgreSQL, Redis o Docker.

DONE: AI-0080
EXIT_SIGNAL: true
