# A.SPEC AI-0143 — Verificar HotelProfile con el runner correcto de Vitest

ID: AI-0143
Mode: VERIFY
RISK: LOW

## WHY
La configuración ya declara jsdom; el fallo document se produjo usando npm --prefix desde la raíz y no demuestra un defecto de la aplicación.

## WHAT
Ejecutar la prueba focalizada desde frontend para cargar vite.config.ts y confirmar el ajuste de HotelProfile.

## SCOPE
- Entorno jsdom
- HotelProfile.test.tsx
- manejo de detail en toast

## OUT OF SCOPE
- Migraciones
- secretos
- backend
- deploy

## CONTRACT
- Vitest debe ejecutar con environment jsdom
- Los 18 casos deben poder renderizar componentes React

## INVARIANTS
- No modificar trabajo existente
- No tocar migraciones ni configuración sensible

## VERIFICATION
- Resultado focalizado sin document is not defined
- git diff --check

## ROLLBACK
No requiere cambios; si aparece un defecto real, aplicar únicamente la reparación mínima en la superficie frontend afectada.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/vite.config.ts
- frontend/src/setupTests.ts
- frontend/src/pages/HotelProfile.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- cd frontend && npm test -- --run src/pages/HotelProfile.test.tsx --reporter=dot
END_VERIFY_COMMANDS
