# A.SPEC AI-0142 — Reparar fallo reproducible de guardado en HotelProfile

ID: AI-0142
Mode: WRITE
RISK: LOW

## WHY
El checkpoint más reciente identifica un fallo concreto: el guardado con error INVALID_VAT_NUMBER no produce el toast esperado. El build ya está verde; corresponde reparar la causa mínima antes de repetir la suite.

## WHAT
Alinear el manejo del error de guardado de perfil con el contrato existente de detalles de error y conservar el toast esperado por la prueba.

## SCOPE
- HotelProfile
- manejo de errores de guardado
- prueba focalizada

## OUT OF SCOPE
- migraciones Flyway
- secretos
- backend
- cambios de contrato API
- producción

## CONTRACT
- Un error INVALID_VAT_NUMBER debe llegar al toast como código de error.
- El guardado exitoso conserva su comportamiento actual.
- No modificar datos ni contratos persistentes.

## INVARIANTS
- No descartar cambios preexistentes.
- No editar migraciones.
- El build TypeScript/Vite debe seguir compilando.

## VERIFICATION
- La prueba focalizada de HotelProfile pasa.
- No aparecen cambios fuera de las rutas permitidas.

## ROLLBACK
Revertir únicamente los cambios realizados en las dos rutas permitidas, preservando el worktree existente.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/HotelProfile.tsx
- frontend/src/pages/HotelProfile.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec vitest run src/pages/HotelProfile.test.tsx --reporter=dot
END_VERIFY_COMMANDS
