# A.SPEC AI-0145 — Verificar pagos en efectivo y contratos actuales de billing

ID: AI-0145
Mode: VERIFY
RISK: LOW

## WHY
Es independiente de Flyway y confirma el contrato financiero existente antes de implementar caja.

## WHAT
Verificar pagos cash, referencias opcionales y visualización del método.

## SCOPE
- PaymentRequest
- PaymentController
- PaymentModal
- InvoiceDetailModal

## OUT OF SCOPE
- Caja y turnos
- Migraciones
- Producción

## CONTRACT
- cash no requiere transaction_reference
- cash se persiste y se muestra correctamente

## INVARIANTS
- Solo lectura
- No modificar datos runtime
- No editar migraciones

## VERIFICATION
- billing-service:test termina con código 0
- Pruebas Vitest focalizadas terminan con código 0
- Todo fallo concreto genera el WRITE mínimo siguiente

## ROLLBACK
No aplica; operación de verificación.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- billing-service/src/main
- billing-service/src/test
- frontend/src/pages/Billing
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :billing-service:test --no-daemon
- npm --prefix frontend exec vitest run src/pages/Billing.test.tsx src/pages/Billing/PaymentModal.test.tsx src/pages/Billing/InvoiceDetailModal.test.tsx --reporter=dot
END_VERIFY_COMMANDS
