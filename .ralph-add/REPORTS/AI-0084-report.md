# A.SPEC AI-0084 — Diagnóstico del bloqueo determinista de tests del frontend

## Resultado

**PASS — diagnóstico completado en modo READ_ONLY.**

El bloqueo `BLOCKED_BY_SUPERVISOR` no proviene del frontend, de npm, de Vitest ni de una configuración de tests defectuosa. Proviene de la política del supervisor antiguo que clasificó `npm --prefix frontend test` como comando no permitido.

El comando real y reproducible para la suite unitaria es:

```bash
npm --prefix frontend test
```

Equivalente:

```bash
npm --prefix frontend run test
```

## Evidencia observada

### Script npm

Archivo: `frontend/package.json`

```json
"test": "vitest run"
```

Scripts relacionados:

```json
"test:coverage": "vitest run --coverage",
"test:watch": "vitest",
"test:e2e": "playwright test",
"test:e2e:ci": "playwright test"
```

Por tanto:

| Comando | Runner | Alcance |
|---|---|---|
| `npm --prefix frontend test` | Vitest | Tests unitarios |
| `npm --prefix frontend run test:coverage` | Vitest + V8 | Cobertura |
| `npm --prefix frontend run test:watch` | Vitest | Modo watch |
| `npm --prefix frontend run test:e2e` | Playwright | E2E |

### Dependencias existentes

`frontend/package.json` declara:

- `vitest`
- `vitest-axe`
- `@vitest/coverage-v8`
- `@testing-library/react`
- `@testing-library/jest-dom`
- `jsdom`
- `@playwright/test`

Existe `frontend/package-lock.json`.

No se instalaron dependencias.

### Configuración Vitest

La configuración está en:

`frontend/vite.config.ts`

Configuración observada:

- `environment: 'jsdom'`
- `globals: true`
- `setupFiles: './src/setupTests.ts'`
- Exclusión de `e2e/**` y `e2e-live/**`
- Cobertura con proveedor `v8`
- Umbrales configurados:
  - statements: 90
  - branches: 80
  - functions: 84
  - lines: 92

El archivo `frontend/src/setupTests.ts` configura:

- `@testing-library/jest-dom/vitest`
- `vitest-axe`
- `localStorage` compatible con tests
- `window.matchMedia`
- matchers de accesibilidad

### Inventario de tests

Se observaron **89 archivos de tests** bajo `frontend/src`, principalmente `*.test.ts` y `*.test.tsx`.

No existen directorios `frontend/test` ni `frontend/tests` con archivos adicionales detectables.

### Playwright

La configuración E2E está en:

`frontend/playwright.config.ts`

El script E2E usa `playwright test` y apunta al directorio `frontend/e2e`.

Esto es independiente de la suite unitaria Vitest.

## Origen del bloqueo

El reporte previo AI-0083 registra:

```text
$ npm --prefix frontend test
BLOCKED_BY_SUPERVISOR
```

El código del supervisor contiene una ruta que registra exactamente ese marcador cuando un comando no pasa la validación de seguridad:

```python
log.append(f"$ {cmd}\nBLOCKED_BY_SUPERVISOR\n")
```

También existe una implementación posterior del supervisor que reconoce explícitamente comandos npm frontend como:

```python
npm --prefix frontend test
npm --prefix frontend run test
```

Y documenta que el script frontend usa Vitest, no Jest.

Conclusión: el marcador es evidencia de una decisión del supervisor, no de un fallo del producto.

## Estado Git

`git status --short` mostró numerosos cambios preexistentes, incluyendo modificaciones dentro de `frontend/src` y varios tests.

Ejemplos:

```text
M frontend/src/setupTests.ts
M frontend/src/pages/HotelProfile.test.tsx
M frontend/src/pages/Quotations.test.tsx
M frontend/src/pages/Rates/RateCalendar.test.tsx
M frontend/src/pages/Settings/SettingsSystem.test.tsx
```

También existen cambios y archivos no versionados fuera del alcance de esta A.SPEC.

Estos cambios fueron preservados intactos.

## Verificación de mutaciones

Se ejecutó:

```bash
git diff --exit-code -- frontend/package.json frontend/src frontend/test frontend/tests
```

Resultado: código de salida `1`, debido a cambios preexistentes en `frontend/src`.

Esto no demuestra una mutación causada por esta inspección. No se modificó ningún archivo.

No se ejecutó la suite Vitest porque AI-0084 autoriza diagnóstico read-only y no autoriza reintentar el test bloqueado.

## Clasificación

| Área | Estado | Conclusión |
|---|---|---|
| Script npm | PASS | `test` ejecuta `vitest run` |
| Runner | PASS | Vitest está declarado y configurado |
| Dependencias | PASS | Dependencias necesarias están declaradas y existe lockfile |
| Configuración | PASS | `vite.config.ts` contiene configuración Vitest válida |
| Tests unitarios | PASS estructural | Existen 89 archivos de tests |
| E2E | PASS estructural | Playwright está configurado separadamente |
| Bloqueo reportado | BLOCKER externo | La política del supervisor rechazó el comando |
| Suite verde | UNKNOWN | No se ejecutó Vitest en esta A.SPEC |
| Worktree | WARNING | Ya contiene cambios preexistentes |

## Comando correcto de siguiente verificación

En una A.SPEC que autorice ejecución de tests:

```bash
npm --prefix frontend test
```

Para cobertura:

```bash
npm --prefix frontend run test:coverage
```

La siguiente ejecución debe registrar el resultado real de Vitest; no debe tratar `BLOCKED_BY_SUPERVISOR` como fallo del frontend.

## Cambios realizados

Ninguno.

- No se modificaron archivos.
- No se instalaron dependencias.
- No se ejecutaron migraciones.
- No se alteró Git.
- No se accedió a secretos.
- No se reiniciaron servicios.

DONE: AI-0084  
EXIT_SIGNAL: true
