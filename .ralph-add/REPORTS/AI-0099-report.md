# A.SPEC AI-0099 — Desbloquear y verificar contrato local de frontend

## Resultado

**PASS — inspección READ_ONLY completada.**

No se modificaron archivos, configuraciones, dependencias, Git, servicios ni estado externo.

## Alcance ejecutado

- Estado Git.
- Existencia de `frontend/package.json`.
- Scripts, dependencias y devDependencies declaradas.
- Listado de scripts mediante npm.
- Configuración Vite/Vitest.
- Inventario estructural de pruebas.
- Disponibilidad de binarios locales.

## Evidencia Git

`git status --short` devolvió correctamente el estado del worktree.

El repositorio contiene numerosos cambios previos, incluyendo modificaciones en backend, frontend, configuración, migraciones y archivos no versionados.

Clasificación: **WARNING**

Estos cambios fueron preservados íntegramente. No se ejecutaron `reset`, `clean`, `stash`, checkout ni operaciones destructivas.

Git mostró advertencias del entorno macOS relacionadas con la creación de cachés temporales de `xcrun`, pero el estado del worktree sí fue reportado.

## Contrato de frontend

Archivo confirmado:

- `frontend/package.json`

Scripts disponibles:

| Script | Comando |
|---|---|
| `dev` | `vite` |
| `build` | `tsc -b && vite build` |
| `lint` | `eslint .` |
| `lint:a11y` | `eslint src/ --ext .jsx,.tsx` |
| `knip` | `knip` |
| `preview` | `vite preview` |
| `test` | `vitest run` |
| `test:coverage` | `vitest run --coverage` |
| `test:watch` | `vitest` |
| `test:e2e` | `playwright test` |
| `test:e2e:ci` | `playwright test` |
| `test:e2e:live` | `playwright test --config=playwright-live.config.ts` |

`npm --prefix frontend run` terminó correctamente y mostró el listado completo de scripts.

Clasificación: **PASS**

## Dependencias relevantes

Dependencias de ejecución confirmadas:

- React 19.
- React Router 7.
- Axios.
- Zustand.
- i18next.
- Zod.
- React Big Calendar.

Dependencias de pruebas y tooling confirmadas:

- Vitest.
- Testing Library.
- jsdom.
- Playwright.
- axe-core para Playwright.
- Vite.
- TypeScript.
- ESLint.
- Coverage V8.

No se instalaron ni actualizaron dependencias.

## Configuración de pruebas

Archivo confirmado:

- `frontend/vite.config.ts`

Configuración observada:

- Runner: Vitest.
- Entorno: `jsdom`.
- Globals habilitados.
- Setup: `./src/setupTests.ts`.
- Exclusión de `node_modules`, `dist`, `.git`, `.cache`, `e2e` y `e2e-live`.
- Coverage con provider `v8`.
- Umbrales declarados:
  - Statements: 90%.
  - Branches: 80%.
  - Functions: 84%.
  - Lines: 92%.

La configuración también confirma alias `@` hacia `frontend/src` y proxies locales hacia los servicios PMS.

Clasificación: **PASS** para el contrato estructural.

## Inventario de pruebas

Se identificaron **89 archivos de prueba** bajo `frontend/src`.

También existen suites E2E live bajo:

- `frontend/e2e-live/checkout-live.spec.ts`
- `frontend/e2e-live/idor-cross-tenant-live.spec.ts`
- `frontend/e2e-live/planning-board-live.spec.ts`
- `frontend/e2e-live/walk-in-live.spec.ts`

Clasificación: **PASS** para disponibilidad estructural de pruebas.

## Disponibilidad local

Binarios locales confirmados:

- Vitest: disponible en `frontend/node_modules/.bin/vitest`.
- Playwright: disponible en `frontend/node_modules/.bin/playwright`.

Versiones observadas:

- Node.js `v26.7.0`
- npm `11.19.0`

Clasificación: **PASS**

## Pruebas no ejecutadas

No se ejecutaron:

- `npm --prefix frontend test`
- `npm --prefix frontend run build`
- `npm --prefix frontend run lint`
- Playwright o E2E live

La A.SPEC autoriza inspección del contrato y listado de comandos, pero no solicita ejecutar la suite funcional. Por tanto, el estado real PASS/FAIL de los tests permanece **UNKNOWN**.

No debe afirmarse que las pruebas pasan.

Clasificación: **UNKNOWN**

## Bloqueo del supervisor

El reporte de AI-0098 registró `BLOCKED_BY_SUPERVISOR` para comandos anteriores.

En AI-0099, los comandos autorizados sí pudieron ejecutarse:

- `git status --short`: evidencia devuelta.
- `test -f frontend/package.json`: PASS.
- Inspección Node del manifiesto: PASS.
- `npm --prefix frontend run`: PASS.

Conclusión: el bloqueo anterior no se reproduce para el alcance actual y no constituye un fallo real de npm ni del contrato local de frontend.

## Invariantes

| Invariante | Resultado |
|---|---|
| No modificar archivos | PASS |
| Preservar worktree existente | PASS |
| No instalar dependencias | PASS |
| No ejecutar operaciones destructivas | PASS |
| No leer ni imprimir secretos | PASS |
| No afirmar tests exitosos sin ejecución | PASS |
| Distinguir bloqueo del supervisor de fallo real | PASS |

## Conclusión

AI-0099 queda **PASS** para su objetivo de identificar el contrato local del frontend y desbloquear la inspección estructural.

El frontend tiene:

- Manifiesto válido.
- Scripts de test definidos.
- Vitest y Playwright disponibles localmente.
- Configuración Vite/Vitest presente.
- Amplio inventario de pruebas.

La ejecución funcional de Vitest, build, lint y E2E queda pendiente de una A.SPEC que la autorice explícitamente.

```text
DONE: AI-0099
EXIT_SIGNAL: true
```
