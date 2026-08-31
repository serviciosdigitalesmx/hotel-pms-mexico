# A.SPEC AI-0115 — Diagnóstico focalizado de fallos de tests de Stays

**Modo:** READ_ONLY  
**Resultado:** Diagnóstico completado parcialmente; el test no pudo iniciar por restricción de escritura del entorno.

## 1. Cumplimiento de invariantes

- No se modificaron archivos.
- No se modificó Git, servicios, bases de datos, secretos ni infraestructura.
- Se preservaron los cambios preexistentes.
- `git status --short` confirmó un worktree previamente modificado, ajeno a esta ejecución.

## 2. Evidencia reproducible

Comando ejecutado:

```bash
npm --prefix frontend run test -- --run src/pages/Stays.test.tsx
```

Resultado:

```text
Startup Error
Error: EPERM: operation not permitted, open
frontend/node_modules/.vite-temp/vite.config.ts.timestamp-...
```

El test no llegó a ejecutar ningún caso. Por tanto, no es posible reportar conteo real de tests fallidos desde este entorno READ_ONLY.

## 3. Hallazgos

### Traducciones

Las claves principales sí existen en los locales:

- `no_active_stays`
  - [en/common.json:83](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0115-l534w79z/frontend/src/locales/en/common.json:83)
  - también presentes en `es` e `it`.

- `download_json_export`
  - [en/common.json:274](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0115-l534w79z/frontend/src/locales/en/common.json:274)
  - también presentes en `es` e `it`.

- `alloggiati_json_downloaded`
  - [en/common.json:275](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0115-l534w79z/frontend/src/locales/en/common.json:275)
  - también presentes en `es` e `it`.

No hay evidencia de que el fallo principal sea una clave de traducción obsoleta.

### Causa localizada

`Stays.tsx` no importa ni renderiza `AlloggiatiReportSection`.

La evidencia está en:

- Imports de [Stays.tsx:1-12](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0115-l534w79z/frontend/src/pages/Stays.tsx:1)
- Render principal de [Stays.tsx:253-270](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0115-l534w79z/frontend/src/pages/Stays.tsx:253)
- No existe ninguna referencia a `AlloggiatiReportSection` dentro de `Stays.tsx`.

Sin embargo, `Stays.test.tsx` espera que `Stays` muestre el botón JSON para ADMIN y OWNER:

- [Stays.test.tsx:229-240](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0115-l534w79z/frontend/src/pages/Stays.test.tsx:229)
- [Stays.test.tsx:257-268](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0115-l534w79z/frontend/src/pages/Stays.test.tsx:257)

El botón sí existe dentro de `AlloggiatiReportSection`, condicionado por `isAdminOrOwner`:

- [AlloggiatiReportSection.tsx:101-111](/Users/usuario/.ralph-hotel/hypervelocity-worktrees/AI-0115-l534w79z/frontend/src/pages/Stays/AlloggiatiReportSection.tsx:101)

## 4. Clasificación

### Fallos de selector/traducción

No identificados como causa primaria.

Las pruebas usan claves literales porque mockean `react-i18next` para devolver la clave, y las claves existen en los archivos reales.

### Regresión funcional

No demostrada funcionalmente por falta de ejecución del test.

### Desalineación test-componente

Sí identificada:

- El test de `Stays` exige una sección/botón que el componente `Stays` actualmente no monta.
- `AlloggiatiReportSection` tiene pruebas unitarias propias y su contrato de exportación JSON está explícitamente condicionado a `isAdminOrOwner`.
- La exportación JSON no debe considerarse expuesta desde `Stays` mientras `Stays` no renderice dicha sección.

## 5. Reparación mínima propuesta

Procede un único A.SPEC posterior:

> Integrar o retirar de `Stays.test.tsx` las expectativas de exportación JSON según el contrato vigente de composición de `Stays`, verificando explícitamente si `AlloggiatiReportSection` debe formar parte de esta página. No añadir claves, endpoints ni permisos nuevos.

La reparación no debe consistir únicamente en cambiar selectores o traducciones.

## 6. Limitación de verificación

La ejecución focalizada quedó bloqueada por `EPERM` porque Vitest/Vite necesita crear un archivo temporal en:

```text
frontend/node_modules/.vite-temp/
```

Esto impide distinguir mediante ejecución cuáles casos fallarían exactamente. La evidencia estática sí localiza la incompatibilidad entre `Stays.test.tsx` y `Stays.tsx`.

## Conclusión

La causa localizada es una desalineación entre las pruebas de `Stays` y la composición actual de `Stays.tsx`, no una traducción faltante. La exportación JSON solo está implementada en `AlloggiatiReportSection`; actualmente esa sección no se expone desde `Stays`.
