# A.SPEC AI-0078 — Determinar el comando real de pruebas frontend

## Resultado

**PASS — comando real identificado.**

El frontend usa **Vitest** para pruebas unitarias:

```bash
npm --prefix frontend test
```

Equivalente:

```bash
cd frontend && npm test
```

El script está definido como:

```json
"test": "vitest run"
```

## Evidencia

### Configuración

- `frontend/package.json:13` — `test: vitest run`
- `frontend/package.json:14` — cobertura: `vitest run --coverage`
- `frontend/package.json:15` — modo watch: `vitest`
- `frontend/vite.config.ts:1-2` — configuración basada en Vitest
- `frontend/vite.config.ts:10-14` — entorno `jsdom`, setup y exclusiones
- `frontend/vite.config.ts:15-41` — cobertura V8 y umbrales configurados

### Dependencias

El runner está declarado y disponible:

- `vitest` declarado en `devDependencies`
- `@vitest/coverage-v8` declarado
- `@testing-library/react` y `vitest-axe` declarados
- `frontend/package-lock.json` presente, `lockfileVersion: 3`
- Ejecutables locales encontrados:
  - `frontend/node_modules/.bin/vitest`
  - `frontend/node_modules/.bin/playwright`

### Pruebas encontradas

Existen pruebas unitarias `.test.ts` y `.test.tsx` dentro de `frontend/src`, incluyendo componentes, páginas, servicios, stores y utilidades.

También existen pruebas E2E con Playwright:

```bash
npm --prefix frontend run test:e2e
npm --prefix frontend run test:e2e:ci
npm --prefix frontend run test:e2e:live
```

Scripts correspondientes:

- `test:e2e`: `playwright test`
- `test:e2e:ci`: `playwright test`
- `test:e2e:live`: `playwright test --config=playwright-live.config.ts`

## Distinción de estados

| Estado | Conclusión |
|---|---|
| Script inexistente | No aplica |
| Runner declarado | PASS — Vitest está configurado |
| Runner instalado localmente | PASS — binario local presente |
| Comando verificable | PASS — `npm --prefix frontend test` |
| Ejecución en esta A.SPEC | No realizada; la A.SPEC solicitó inspección estructural |
| Evidencia histórica de ejecución | AI-0077 registró `npm --prefix frontend test` como `BLOCKED_BY_SUPERVISOR`; reportes previos también registran fallos de pruebas, por lo que no se declara suite verde |

## Comandos de verificación recomendados

Prueba unitaria frontend:

```bash
npm --prefix frontend test
```

Cobertura:

```bash
npm --prefix frontend run test:coverage
```

Pruebas E2E:

```bash
npm --prefix frontend run test:e2e
```

## Integridad del worktree

No se modificaron archivos, dependencias ni estado del repositorio durante esta inspección.

El worktree ya estaba previamente modificado y contiene cambios no relacionados, incluyendo archivos bajo `frontend/src`. Esos cambios fueron preservados.

También se observaron advertencias de macOS relacionadas con la creación de cachés temporales de `git/xcrun`; no alteraron archivos del proyecto.

## Conclusión

El contrato real de pruebas frontend está definido y es ejecutable mediante:

```bash
npm --prefix frontend test
```

La existencia del runner queda demostrada, pero la suite no puede declararse aprobada sin una ejecución posterior permitida y verificable.
