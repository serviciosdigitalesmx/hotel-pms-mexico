# A.SPEC AI-0088 — Auditoría de configuración y ejecución de tests del frontend

**Modo:** READ_ONLY  
**Riesgo:** LOW  
**Resultado:** PASS — auditoría estructural completada  
**Cambios realizados:** Ninguno

## Alcance

Se inspeccionaron exclusivamente:

- `frontend/package.json`
- `frontend/package-lock.json`
- Configuración de Vitest/Vite
- Configuración de Playwright
- Setup de tests
- Tests existentes del frontend
- Scripts npm relacionados con pruebas
- Estado Git del worktree

No se ejecutaron migraciones, servicios, Docker, despliegues, instalaciones ni la suite de tests.

## Runner real

La fuente de verdad es [`frontend/package.json`](/Users/usuario/Desktop/HOTEL-PMS/frontend/package.json:6).

| Script | Comando real | Runner |
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

Equivalente:

```bash
npm --prefix frontend run test
```

No corresponde agregar flags exclusivos de Jest como `--watchAll=false`.

## Configuración Vitest

No existe un archivo independiente `vitest.config.*`.

La configuración efectiva está integrada en [`frontend/vite.config.ts`](/Users/usuario/Desktop/HOTEL-PMS/frontend/vite.config.ts:1):

- Importa `defineConfig` desde `vitest/config`.
- Usa `globals: true`.
- Usa `environment: 'jsdom'`.
- Carga [`frontend/src/setupTests.ts`](/Users/usuario/Desktop/HOTEL-PMS/frontend/src/setupTests.ts:1).
- Excluye `node_modules`, `dist`, `.git`, `e2e/**` y `e2e-live/**`.
- Configura cobertura con proveedor `v8`.
- Reporta cobertura en formatos `text`, `html` y `lcov`.
- Define umbrales:

  - Statements: 90%
  - Branches: 80%
  - Functions: 84%
  - Lines: 92%

La configuración también define alias `@` hacia `frontend/src`.

## Setup de tests

[`frontend/src/setupTests.ts`](/Users/usuario/Desktop/HOTEL-PMS/frontend/src/setupTests.ts:1) configura:

- `@testing-library/jest-dom/vitest`
- `vitest-axe`
- Matchers de accesibilidad
- Un `localStorage` compatible con `jsdom`
- `window.matchMedia`
- Extensión de `expect` con matchers de accesibilidad

El tipado específico está declarado en [`frontend/tsconfig.vitest.json`](/Users/usuario/Desktop/HOTEL-PMS/frontend/tsconfig.vitest.json:1), incluyendo:

- `vitest/globals`
- `@testing-library/jest-dom`
- `vitest-axe`

## Inventario de tests

Conteo obtenido mediante inspección estática:

| Categoría | Cantidad |
|---|---:|
| Tests unitarios/integración bajo `frontend/src` | 89 |
| Archivos E2E bajo `frontend/e2e` y `frontend/e2e-live` | 21 |
| Configuraciones Playwright | 2 |
| Configuraciones Vitest independientes | 0 |
| Configuraciones Jest | 0 |

Los tests unitarios se encuentran principalmente en:

- `frontend/src/components`
- `frontend/src/layouts`
- `frontend/src/pages`
- `frontend/src/services`
- `frontend/src/store`
- `frontend/src/utils`

Los tests E2E se encuentran en:

- `frontend/e2e`
- `frontend/e2e-live`

## Playwright

La configuración principal está en [`frontend/playwright.config.ts`](/Users/usuario/Desktop/HOTEL-PMS/frontend/playwright.config.ts:1).

Evidencia observada:

- `testDir: './e2e'`
- Navegador Chromium
- `baseURL` configurable mediante `PLAYWRIGHT_BASE_URL`
- Valor local por defecto: `http://localhost:5173`
- En ejecución local puede iniciar `npm run dev`
- En CI utiliza configuración distinta de reintentos, workers y servidor
- La configuración live está separada en `frontend/playwright-live.config.ts`

Vitest excluye explícitamente `e2e/**` y `e2e-live/**`, por lo que las suites unitarias y E2E están separadas.

## Dependencias y lockfile

Existe:

- [`frontend/package.json`](/Users/usuario/Desktop/HOTEL-PMS/frontend/package.json:42)
- [`frontend/package-lock.json`](/Users/usuario/Desktop/HOTEL-PMS/frontend/package-lock.json:1)

El manifest declara las dependencias necesarias para el runner:

- `vitest`
- `@vitest/coverage-v8`
- `vite`
- `jsdom`
- `@testing-library/react`
- `@testing-library/jest-dom`
- `vitest-axe`
- `@playwright/test`

La A.SPEC no autoriza instalar, actualizar ni resolver dependencias, por lo que no se ejecutó `npm install` ni se modificó el lockfile.

## Bloqueo de AI-0087

La evidencia histórica disponible en los reportes previos registra:

```text
$ npm --prefix frontend test
BLOCKED_BY_SUPERVISOR
```

El marcador aparece asociado a la política del supervisor, no al script de `package.json`, a Vitest ni a un error de configuración del frontend.

En esta A.SPEC no se reintentó `npm --prefix frontend test`, porque la especificación autoriza únicamente inspección estructural READ_ONLY. Por tanto:

- El runner real está confirmado.
- La configuración está confirmada.
- La existencia de tests está confirmada.
- El resultado actual de la suite permanece **UNKNOWN**.
- No se puede afirmar que todos los tests pasen.

## Estado del worktree

El worktree ya contenía numerosos cambios antes de esta auditoría, incluyendo modificaciones en frontend, backend y archivos no versionados.

Entre los cambios frontend preexistentes:

```text
M frontend/src/setupTests.ts
M frontend/src/pages/HotelProfile.test.tsx
M frontend/src/pages/Quotations.test.tsx
M frontend/src/pages/Rates/RateCalendar.test.tsx
M frontend/src/pages/Settings/SettingsSystem.test.tsx
```

También existe:

```text
?? node_modules/
?? .ralph-add/
?? .add/
```

Estos cambios fueron preservados. Ninguna operación ejecutada por AI-0088 escribió archivos, instaló dependencias o alteró Git.

Los comandos `git status --short` emitieron advertencias ambientales de macOS relacionadas con `xcrun_db-*` y el directorio temporal. Esto no impidió la inspección de archivos.

## Clasificación

| Área | Estado | Evidencia |
|---|---|---|
| `frontend/package.json` existe | PASS | Manifest presente |
| Script unitario real | PASS | `test: vitest run` |
| Runner | PASS | Vitest declarado |
| Configuración Vitest | PASS | Integrada en `vite.config.ts` |
| Setup de tests | PASS | `src/setupTests.ts` |
| Lockfile | PASS | `package-lock.json` presente |
| Tests unitarios | PASS estructural | 89 archivos detectados |
| Tests E2E | PASS estructural | 21 archivos detectados |
| Configuración Jest | NOT APPLICABLE | No existe |
| Bloqueo histórico | WARNING/BLOCKER externo | `BLOCKED_BY_SUPERVISOR` |
| Suite actualmente verde | UNKNOWN | No se ejecutó Vitest |
| Worktree | WARNING | Cambios preexistentes |

## Próxima verificación recomendada

En una A.SPEC posterior que autorice ejecución:

```bash
npm --prefix frontend test
```

Para cobertura:

```bash
npm --prefix frontend run test:coverage
```

Para E2E:

```bash
npm --prefix frontend run test:e2e
```

## Conclusión

AI-0088 queda completada correctamente en modo READ_ONLY.

La configuración del frontend define Vitest como runner unitario real y Playwright como runner E2E. No se detectó un problema estructural de configuración. El bloqueo histórico `BLOCKED_BY_SUPERVISOR` corresponde a política de ejecución del supervisor, no a un fallo confirmado del frontend.

El estado funcional de la suite sigue pendiente de una A.SPEC que autorice ejecutar:

```bash
npm --prefix frontend test
```

**EXIT_SIGNAL:** `true`
