# A.SPEC FRONTEND-0000 — Reparar guardado de perfil del hotel y cerrar su contrato de toast

ID: FRONTEND-0000
Mode: WRITE
RISK: LOW

## WHY
La evidencia reciente identifica un fallo reproducible en HotelProfile.test.tsx: el guardado no produce el toast esperado. El build ya está verde.

## WHAT
Aplicar la reparación mínima al flujo de guardado usando el servicio y contrato existentes.

## SCOPE
- Corregir callback y manejo async del guardado
- Preservar validaciones, API y etiquetas existentes

## OUT OF SCOPE
- Migraciones
- Cambios de API
- Secretos
- Rediseño

## CONTRACT
- btn_save_profile dispara el servicio existente
- El éxito produce el toast esperado
- El error conserva el manejo actual

## INVARIANTS
- No cambiar endpoints ni DTOs
- No tocar archivos fuera de la superficie autorizada

## VERIFICATION
- La prueba focalizada termina sin fallos con pool=threads y maxWorkers=1

## ROLLBACK
Revertir únicamente el diff de los archivos autorizados.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/HotelProfile.tsx
- frontend/src/pages/HotelProfile.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
END_VERIFY_COMMANDS
