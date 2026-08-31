# Informe A.SPEC AI-0101

## Resultado

Auditoría completada en modo `READ_ONLY`.

- No se modificaron archivos, Git, secretos, bases de datos, servicios ni infraestructura.
- El worktree ya contenía cambios y archivos no versionados; fueron preservados íntegramente.
- El runner local sí está disponible y pudo iniciar su CLI.
- No se observó un fallo técnico del runner en esta auditoría.

## Contrato npm del frontend

Archivo: `frontend/package.json`

Scripts relevantes:

```json
"test": "vitest run",
"test:coverage": "vitest run --coverage",
"test:watch": "vitest",
"test:e2e": "playwright test",
"test:e2e:ci": "playwright test",
"test:e2e:live": "playwright test --config=playwright-live.config.ts"
```

Runner unitario/integración:

- Vitest `^4.1.0`
- Versión detectada al ejecutar el comando: `vitest/4.1.5`
- Entorno de pruebas configurado mediante `tsconfig.vitest.json`
- Integración con Testing Library, `jest-dom` y `vitest-axe`

Runner E2E:

- Playwright `^1.58.2`
- `@playwright/test` está declarado como dependencia de desarrollo
- Existen configuraciones `playwright.config.ts` y `playwright-live.config.ts`; no fueron inspeccionadas porque están fuera de los paths permitidos por esta A.SPEC.

## Estructura real de pruebas

Pruebas Vitest encontradas:

- 89 archivos `*.test.ts` / `*.test.tsx` bajo `frontend/src`
- Ejemplos reales:
  - `frontend/src/App.test.tsx`
  - `frontend/src/pages/Login.test.tsx`
  - `frontend/src/pages/Reservations.test.tsx`
  - `frontend/src/services/api.test.ts`
  - `frontend/src/store/authStore.test.ts`

Pruebas Playwright encontradas:

- 18 archivos bajo:
  - `frontend/e2e`
  - `frontend/e2e-live`

Ejemplos reales:

- `frontend/e2e/auth.spec.ts`
- `frontend/e2e/checkout.spec.ts`
- `frontend/e2e/security-rbac.spec.ts`
- `frontend/e2e-live/checkout-live.spec.ts`

Directorios convencionales adicionales:

- `frontend/tests`: no existe o no contiene archivos.
- `frontend/test`: no existe o no contiene archivos.

## Configuración descubierta

`frontend/tsconfig.vitest.json`:

- Extiende `tsconfig.app.json`.
- Incluye `src` y `vite.config.ts`.
- Declara tipos globales de Vitest, Testing Library y `vitest-axe`.

`frontend/src/setupTests.ts`:

- Carga `@testing-library/jest-dom/vitest`.
- Carga `vitest-axe`.
- Configura `localStorage` de prueba.
- Define `window.matchMedia`.
- Extiende `expect` con `toHaveNoViolations`.

No se encontró ningún archivo `frontend/vitest.config.*` dentro de los paths permitidos. La configuración efectiva puede estar en `frontend/vite.config.ts`, pero ese archivo está fuera del conjunto de lectura autorizado por la A.SPEC.

## Verificación ejecutada

Comando autorizado:

```bash
npm --prefix frontend test -- --help
```

Resultado:

- Exitosa.
- npm resolvió el script real:

```text
frontend@0.0.0 test
vitest run --help
```

- Vitest mostró correctamente su ayuda y opciones.
- Esto confirma que el entrypoint npm y la dependencia instalada son utilizables localmente.

## Comando mínimo reproducible

Para el siguiente `VERIFY`:

```bash
npm --prefix frontend test
```

Forma equivalente y explícita:

```bash
npm --prefix frontend run test -- --run
```

Prerrequisitos:

- Ejecutar desde la raíz del worktree.
- Tener instaladas las dependencias de `frontend`.
- No requiere modificar servicios backend para las pruebas Vitest.
- Las pruebas Playwright requieren su configuración y, según el spec, posiblemente servicios frontend/backend disponibles.

## Clasificación de bloqueos

| Área | Estado | Evidencia |
|---|---|---|
| Configuración | Parcialmente no verificable | No existe `frontend/vitest.config.*` visible en los paths permitidos; la configuración puede residir en `vite.config.ts`, fuera de alcance |
| Dependencias | No bloqueada | `npm --prefix frontend test -- --help` inició Vitest `4.1.5` |
| Runner | Disponible | El script `test` está definido y responde correctamente |
| Supervisor | No demostrado en esta ejecución | No se reprodujo `BLOCKED_BY_SUPERVISOR`; la auditoría no ejecutó la suite |
| Tests | Presentes | 89 archivos Vitest-style y 18 specs Playwright reales |

## Conclusión

El contrato local de pruebas está definido y es reproducible. El siguiente paso determinista es:

```bash
npm --prefix frontend test
```

El bloqueo registrado en `AI-0100` no puede atribuirse a una ausencia del script npm, del runner Vitest o de las dependencias instaladas. Si vuelve a aparecer `BLOCKED_BY_SUPERVISOR`, debe clasificarse como bloqueo externo de supervisión, no como fallo técnico del frontend.
