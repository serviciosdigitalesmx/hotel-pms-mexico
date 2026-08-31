# A.SPEC AI-0082 — Identificar el entrypoint real de pruebas frontend

## Resultado

**Estado: PASS**

Se identificó el entrypoint real de pruebas frontend y las suites existentes sin modificar archivos, dependencias ni estado del repositorio.

## Alcance

- `frontend/package.json`
- Archivos de pruebas bajo `frontend`
- Configuración frontend relacionada únicamente como evidencia estructural

## Entrypoint confirmado

En `frontend/package.json`:

```json
"test": "vitest run"
```

Comando determinista para la siguiente A.SPEC:

```bash
npm --prefix frontend test
```

Equivalente:

```bash
npm --prefix frontend run test
```

## Scripts de pruebas disponibles

| Script | Comando | Propósito |
|---|---|---|
| `test` | `vitest run` | Suite unitaria frontend |
| `test:coverage` | `vitest run --coverage` | Unitarias con cobertura |
| `test:watch` | `vitest` | Ejecución interactiva |
| `test:e2e` | `playwright test` | E2E mockeadas |
| `test:e2e:ci` | `playwright test` | E2E mockeadas para CI |
| `test:e2e:live` | `playwright test --config=playwright-live.config.ts` | E2E contra entorno live |

## Configuración del runner unitario

Evidencia en `frontend/vite.config.ts`:

- Runner: Vitest.
- Entorno: configurado mediante Vite/Vitest.
- Setup: `frontend/src/setupTests.ts`.
- Exclusiones explícitas:
  - `e2e/**`
  - `e2e-live/**`
  - `node_modules`
  - `dist`
  - `.git`
  - `.cache`

El setup importa los matchers de Testing Library y `vitest-axe`.

## Suites localizadas

### Pruebas unitarias

Ubicación principal:

```text
frontend/src/**/*.test.ts
frontend/src/**/*.test.tsx
```

Conteo:

- 71 archivos `*.test.tsx`
- 18 archivos `*.test.ts`
- **89 archivos unitarios en total**

Áreas observadas:

```text
frontend/src/components/
frontend/src/layouts/
frontend/src/pages/
frontend/src/services/
frontend/src/store/
frontend/src/utils/
frontend/src/App.test.tsx
```

### E2E mockeadas

Ubicación:

```text
frontend/e2e/
```

Conteo:

- **14 especificaciones**

Ejemplos:

```text
frontend/e2e/auth.spec.ts
frontend/e2e/billing.spec.ts
frontend/e2e/checkout.spec.ts
frontend/e2e/dashboard.spec.ts
frontend/e2e/guests.spec.ts
frontend/e2e/reservations.spec.ts
frontend/e2e/security-auth.spec.ts
frontend/e2e/security-idor.spec.ts
frontend/e2e/security-rbac.spec.ts
```

### E2E live

Ubicación:

```text
frontend/e2e-live/
```

Conteo:

- **4 especificaciones**

Archivos:

```text
frontend/e2e-live/checkout-live.spec.ts
frontend/e2e-live/idor-cross-tenant-live.spec.ts
frontend/e2e-live/planning-board-live.spec.ts
frontend/e2e-live/walk-in-live.spec.ts
```

### Total de suites encontradas

- 89 archivos unitarios
- 14 E2E mockeadas
- 4 E2E live
- **107 archivos de pruebas**

El comando de descubrimiento reportó 108 coincidencias porque también incluye:

```text
frontend/src/test-utils/mockAxiosError.ts
```

Ese archivo es un helper, no una suite de pruebas.

## Dependencias relevantes

`frontend/package.json` declara:

```text
vitest
@vitest/coverage-v8
jsdom
@testing-library/react
@testing-library/jest-dom
vitest-axe
@playwright/test
```

No se instalaron ni modificaron dependencias.

## Verificación ejecutada

### Comando 1

```bash
node -e "const p=require('./frontend/package.json'); console.log(JSON.stringify({scripts:p.scripts,devDependencies:p.devDependencies,dependencies:p.dependencies},null,2))"
```

Resultado: **PASS**

Confirmó los scripts, dependencias de Vitest y dependencias de Playwright.

### Comando 2

```bash
rg --files frontend | rg '(^|/)(test|tests|__tests__|.*\.(spec|test)\.)' | sort
```

Resultado: **PASS**

Enumeró las rutas de pruebas frontend existentes.

## Limitaciones

No se ejecutaron:

- `npm --prefix frontend test`
- Playwright
- Build
- Lint
- Instalación de dependencias
- Servicios Docker
- Migraciones
- Deploy

Por tanto, esta A.SPEC confirma el contrato estructural y el entrypoint, pero no confirma que las pruebas pasen.

## Estado del worktree

El worktree ya estaba modificado antes de esta inspección. Entre los cambios frontend preexistentes se observan archivos modificados en:

```text
frontend/src/pages/
frontend/src/locales/
frontend/src/setupTests.ts
frontend/src/types/
```

No se atribuyen esos cambios a AI-0082.

`git status --short` emitió advertencias del entorno macOS relacionadas con la creación de archivos temporales de `xcrun`, pero devolvió el estado del worktree. No se realizaron cambios de Git.

## Conclusión

La suite frontend sí existe y tiene un script válido.

Comando exacto recomendado para la próxima A.SPEC de verificación:

```bash
npm --prefix frontend test
```

No existe bloqueador estructural para ejecutar la suite unitaria. El resultado funcional de las pruebas permanece pendiente de una A.SPEC que autorice ejecución.

```text
DONE: AI-0082
EXIT_SIGNAL: true
```
