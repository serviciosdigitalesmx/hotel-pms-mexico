# A.SPEC AI-0090 — Auditoría de configuración y cobertura ejecutable de pruebas frontend

**Modo:** READ_ONLY  
**Riesgo:** LOW  
**Resultado:** PASS — auditoría estructural completada  
**Cambios realizados:** Ninguno

## Alcance

Se inspeccionaron:

- `frontend/package.json`
- `frontend/package-lock.json`
- Configuración de Vitest/Vite
- Configuración de Playwright
- Setup de pruebas
- Suites unitarias, integración y E2E
- Estado Git del frontend

No se modificaron archivos, no se instalaron dependencias, no se ejecutaron migraciones, Docker, despliegues ni pruebas funcionales.

## Runner real

La fuente de verdad es [`frontend/package.json`](/Users/usuario/Desktop/HOTEL-PMS/frontend/package.json:6).

| Script | Comando | Runner |
|---|---|---|
| `test` | `vitest run` | Vitest |
| `test:coverage` | `vitest run --coverage` | Vitest + V8 |
| `test:watch` | `vitest` | Vitest watch |
| `test:e2e` | `playwright test` | Playwright |
| `test:e2e:ci` | `playwright test` | Playwright |
| `test:e2e:live` | `playwright test --config=playwright-live.config.ts` | Playwright live |

Comando unitario determinista recomendado:

```bash
npm --prefix frontend test
```

Comando con cobertura:

```bash
npm --prefix frontend run test:coverage
```

## Configuración Vitest

La configuración está integrada en [`frontend/vite.config.ts`](/Users/usuario/Desktop/HOTEL-PMS/frontend/vite.config.ts:1); no existe un archivo independiente `vitest.config.*`.

Configuración observada:

- `globals: true`
- Entorno `jsdom`
- Setup: `src/setupTests.ts`
- Exclusión de `e2e/**` y `e2e-live/**`
- Proveedor de cobertura: `v8`
- Reportes: `text`, `html`, `lcov`
- Umbrales:

| Métrica | Umbral |
|---|---:|
| Statements | 90% |
| Branches | 80% |
| Functions | 84% |
| Lines | 92% |

La configuración también declara el alias `@` hacia `frontend/src`.

## Setup de pruebas

[`frontend/src/setupTests.ts`](/Users/usuario/Desktop/HOTEL-PMS/frontend/src/setupTests.ts:1) configura:

- `@testing-library/jest-dom/vitest`
- `vitest-axe`
- Matchers de accesibilidad
- `localStorage` compatible con `jsdom`
- `window.matchMedia`
- Extensión de `expect`

El tipado específico de Vitest está declarado en [`frontend/tsconfig.vitest.json`](/Users/usuario/Desktop/HOTEL-PMS/frontend/tsconfig.vitest.json:1).

## Inventario encontrado

Conteo estático actual:

| Categoría | Archivos |
|---|---:|
| Tests unitarios/integración bajo `frontend/src` | 89 |
| Tests E2E en `frontend/e2e` y `frontend/e2e-live` | 18 |
| Configuraciones Vitest independientes | 0 |
| Configuraciones Jest | 0 |
| Configuraciones Playwright | 2 |

Las pruebas unitarias cubren componentes, layouts, páginas, servicios, stores y utilidades.

Las suites E2E se encuentran en:

- `frontend/e2e`
- `frontend/e2e-live`

## Playwright

La configuración normal está en [`frontend/playwright.config.ts`](/Users/usuario/Desktop/HOTEL-PMS/frontend/playwright.config.ts:1):

- `testDir: './e2e'`
- Chromium
- `PLAYWRIGHT_BASE_URL` configurable
- Base URL local predeterminada: `http://localhost:5173`
- En ejecución local puede iniciar `npm run dev`
- En CI usa workers y reintentos diferenciados

La configuración live está en [`frontend/playwright-live.config.ts`](/Users/usuario/Desktop/HOTEL-PMS/frontend/playwright-live.config.ts:1):

- `testDir: './e2e-live'`
- Base URL predeterminada: `http://localhost`
- Un worker
- Estado de autenticación en `e2e-live/.auth/admin.json`
- Dependencia explícita de un proyecto de setup
- Diseñada para ejecutarse contra el stack Docker real
- No está conectada al pipeline CI según los comentarios del archivo

## Dependencias y lockfile

Existe [`frontend/package-lock.json`](/Users/usuario/Desktop/HOTEL-PMS/frontend/package-lock.json:1).

El manifest declara las dependencias necesarias para las suites:

- `vitest`
- `@vitest/coverage-v8`
- `jsdom`
- `@testing-library/react`
- `@testing-library/jest-dom`
- `vitest-axe`
- `@playwright/test`

No se ejecutó `npm install` ni se alteró el lockfile.

## Ejecución funcional

La A.SPEC es estrictamente READ_ONLY y únicamente autoriza inspección estructural. Por ello, no se ejecutó `npm test`.

La evidencia previa de AI-0089 registra:

```text
$ npm --prefix frontend test
BLOCKED_BY_SUPERVISOR
```

Conclusiones:

- Runner real: **confirmado**
- Scripts npm: **confirmados**
- Configuración Vitest: **confirmada**
- Configuración Playwright: **confirmada**
- Suites existentes: **confirmadas**
- Suite actualmente verde: **UNKNOWN**
- Cobertura efectiva actual: **UNKNOWN**
- Bloqueo histórico de ejecución: **BLOCKED_BY_SUPERVISOR**

No es válido afirmar que las pruebas pasan sin ejecutar Vitest.

## Estado del worktree

El frontend ya contenía cambios antes de esta auditoría, incluyendo archivos modificados de producción y pruebas. También se observaron advertencias ambientales de macOS relacionadas con la creación de archivos temporales de `xcrun`.

No se realizó ninguna operación de:

- edición;
- instalación;
- limpieza;
- reset;
- stash;
- checkout;
- eliminación;
- modificación de Git.

## Verificación contractual

| Criterio | Estado | Evidencia |
|---|---|---|
| `frontend/package.json` presente | PASS | Manifest existente |
| Runner unitario identificado | PASS | `vitest run` |
| Comando determinista identificado | PASS | `npm --prefix frontend test` |
| Cobertura configurada | PASS | V8 y umbrales en `vite.config.ts` |
| Setup de pruebas presente | PASS | `src/setupTests.ts` |
| Lockfile presente | PASS | `package-lock.json` |
| Tests unitarios encontrados | PASS estructural | 89 archivos |
| Tests E2E encontrados | PASS estructural | 18 archivos |
| Playwright configurado | PASS | 2 configuraciones |
| Jest configurado | No aplica | No existe configuración Jest |
| Ejecución funcional | UNKNOWN | No autorizada en READ_ONLY |
| Bloqueador de ejecución | WARNING/BLOCKER | `BLOCKED_BY_SUPERVISOR` histórico |
| Worktree intacto | PASS | No se realizaron escrituras |

## Próximo paso recomendado

Una A.SPEC posterior de tipo `VERIFY` debe autorizar:

```bash
npm --prefix frontend test
```

Opcionalmente, en una verificación separada:

```bash
npm --prefix frontend run test:coverage
```

El estado funcional de la suite y el cumplimiento de los umbrales de cobertura permanecen pendientes de esa ejecución.
