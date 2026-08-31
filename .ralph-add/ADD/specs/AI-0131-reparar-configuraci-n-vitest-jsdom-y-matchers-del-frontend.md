# A.SPEC AI-0131 — Reparar configuración Vitest/jsdom y matchers del frontend

ID: AI-0131
Mode: WRITE
RISK: LOW

## WHY
La causa ya está identificada: document no está definido y los matchers de Testing Library no se cargan. No corresponde repetir diagnóstico.

## WHAT
Configurar el entorno jsdom y el setup global de matchers sin modificar el comportamiento de Stays ni sus contratos.

## SCOPE
- Hacer ejecutable Stays.test.tsx en jsdom
- Cargar correctamente los matchers de Testing Library
- Conservar la modificación existente de StayRow.tsx

## OUT OF SCOPE
- Cambios de producto en Stays
- Migraciones o cambios de backend
- Modificar pruebas para ocultar fallos reales

## CONTRACT
- Las pruebas frontend usan DOM realista mediante jsdom
- Los matchers toBeInTheDocument y equivalentes quedan disponibles globalmente

## INVARIANTS
- No sobrescribir cambios preexistentes
- No cambiar endpoints, DTOs ni datos
- git diff --check debe permanecer limpio

## VERIFICATION
- Stays.test.tsx pasa completamente
- La configuración no introduce errores de TypeScript/Vite

## ROLLBACK
Revertir únicamente los archivos de configuración/setup modificados por AI-0130.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/vite.config.*
- frontend/vitest.config.*
- frontend/src/test/**
- frontend/src/setupTests.*
- frontend/package.json
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec vitest run src/pages/Stays.test.tsx --reporter=dot
END_VERIFY_COMMANDS
