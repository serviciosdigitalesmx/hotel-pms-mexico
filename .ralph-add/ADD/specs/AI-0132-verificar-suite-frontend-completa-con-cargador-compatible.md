# A.SPEC AI-0132 — Verificar suite frontend completa con cargador compatible

ID: AI-0132
Mode: VERIFY
RISK: LOW

## WHY
El fallo de Stays ya está reparado y verificado; falta confirmar regresiones en el resto del frontend.

## WHAT
Ejecutar la suite Vitest completa usando el cargador runner que evita el EPERM del worktree enlazado.

## SCOPE
- frontend/src
- frontend/vite.config.ts
- frontend/src/setupTests.ts

## OUT OF SCOPE
- Cambios de código
- dependencias
- migraciones
- deploy

## CONTRACT
- Tests React ejecutan en jsdom
- Los cambios existentes se conservan

## INVARIANTS
- No modificar node_modules
- No descartar trabajo preexistente

## VERIFICATION
- Resultado de Vitest
- git diff --check

## ROLLBACK
No aplica; operación de solo lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- cd frontend && npm run test -- --configLoader runner --reporter=dot
END_VERIFY_COMMANDS
