# A.SPEC AI-0129 — Reparar regresión de estados Alloggiati en Stays

ID: AI-0129
Mode: WRITE
RISK: LOW

## WHY
El checkpoint muestra fallo reproducible en Stays: la prueba espera alloggiati_failed pero el texto no aparece. El build ya está verde; corresponde reparar la causa concreta antes de repetir VERIFY.

## WHAT
Alinear el estado recibido por Stays con la representación de StayRow, preservando el contrato existente y sin inventar campos ni endpoints.

## SCOPE
- Corregir la propagación o lectura de alloggiatiSendFailed y alloggiatiFailureReason.
- Actualizar únicamente la prueba afectada si el contrato real ya cambió.
- Mantener intactos los estados de facturación existentes.

## OUT OF SCOPE
- Cambios de backend o migraciones.
- Cambios de contratos API.
- Rediseño de la página Stays.

## CONTRACT
- alloggiati_failed debe renderizarse cuando el estado real indique fallo de envío.
- La razón del fallo debe conservarse como metadata visible.

## INVARIANTS
- No eliminar indicadores existentes de invoiceCreationFailed.
- No modificar datos persistidos.
- No sobrescribir cambios preexistentes fuera del alcance.

## VERIFICATION
- La suite Stays debe finalizar sin fallos.
- No deben aparecer errores TypeScript en los archivos modificados.
- El build frontend previamente verde debe permanecer compatible.

## ROLLBACK
Revertir exclusivamente el diff de los archivos permitidos de este A.SPEC, preservando cualquier cambio ajeno.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Stays.tsx
- frontend/src/pages/Stays/StayRow.tsx
- frontend/src/pages/Stays.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec vitest run src/pages/Stays.test.tsx --configLoader runner --reporter=dot
END_VERIFY_COMMANDS
