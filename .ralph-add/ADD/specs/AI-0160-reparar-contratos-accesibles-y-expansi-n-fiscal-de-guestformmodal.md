# A.SPEC AI-0160 — Reparar contratos accesibles y expansión fiscal de GuestFormModal

ID: AI-0160
Mode: WRITE
RISK: LOW

## WHY
La causa ya está identificada: el test busca label_fiscal_code, pero el componente renderiza RFC directamente; la sección fiscal no queda accesible con el contrato esperado.

## WHAT
Restaurar las claves de traducción accesibles existentes para RFC y revisar los labels relacionados sin alterar el contrato de datos ni la lógica de guardado.

## SCOPE
- Corregir el label accesible del RFC
- Conservar expansión automática cuando existe información fiscal
- Mantener intactos los campos y payload fiscal existentes

## OUT OF SCOPE
- Migraciones
- Cambios backend
- Cambios de contratos fiscales

## CONTRACT
- screen.getByLabelText(/label_fiscal_code/i) debe resolver el RFC
- GuestFormModal conserva createGuest/updateGuest y el payload actual

## INVARIANTS
- La sección fiscal permanece colapsada en alta sin datos
- La sección fiscal permanece expandida al editar datos fiscales
- Los tests de teléfono existentes no deben degradarse

## VERIFICATION
- GuestFormModal.test.tsx pasa con el comando Vitest obligatorio
- El build frontend se reserva para el checkpoint posterior

## ROLLBACK
Revertir únicamente el cambio de labels en GuestFormModal.tsx.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/GuestFormModal.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/GuestFormModal.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
END_VERIFY_COMMANDS
