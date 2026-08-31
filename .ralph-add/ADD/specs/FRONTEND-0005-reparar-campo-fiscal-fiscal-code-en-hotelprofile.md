# A.SPEC FRONTEND-0005 — Reparar campo fiscal fiscal_code en HotelProfile

ID: FRONTEND-0005
Mode: WRITE
RISK: LOW

## WHY
La prueba falla de forma reproducible porque no existe un control asociado a label_fiscal_code.

## WHAT
Agregar o corregir el campo fiscal_code usando el contrato y patrón de campos fiscales ya existente, manteniendo la asociación label-control y el comportamiento actual.

## SCOPE
- HotelProfile
- campo fiscal_code
- accesibilidad del label

## OUT OF SCOPE
- migraciones
- backend
- secretos
- facturación PAC
- cambios de diseño no relacionados

## CONTRACT
- Debe existir un elemento consultable mediante getByLabelText(/label_fiscal_code/i).

## INVARIANTS
- No eliminar ni renombrar campos fiscales existentes.
- Conservar cambios de trabajo preexistentes.
- No introducir datos ni endpoints nuevos.

## VERIFICATION
- La prueba HotelProfile queda verde con el comando obligatorio.
- No repetir la verificación sin aplicar primero esta reparación.

## ROLLBACK
Revertir únicamente el cambio acotado en los archivos permitidos si la prueba revela una regresión.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/HotelProfile.tsx
- frontend/src/pages/HotelProfile.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
END_VERIFY_COMMANDS
