# A.SPEC AI-0138 — Verificar pagos en efectivo y contratos actuales de billing

ID: AI-0138
Mode: VERIFY
RISK: LOW

## WHY
Es una verificación independiente de frontend y no depende de Flyway ni de AI-0137; confirma el flujo existente de cobro en efectivo antes de avanzar hacia caja y turnos.

## WHAT
Ejecutar pruebas focalizadas backend y frontend para pagos en efectivo, referencias opcionales y visualización del método cash.

## SCOPE
- PaymentRequest y PaymentController
- PaymentModal
- InvoiceDetailModal
- Pruebas existentes de billing

## OUT OF SCOPE
- Implementar caja o turnos
- Aplicar migraciones
- Cambiar contratos financieros
- Producción

## CONTRACT
- El pago en efectivo no requiere transaction_reference
- El método cash se persiste y se muestra correctamente
- Los endpoints actuales de billing son la fuente de verdad

## INVARIANTS
- Solo lectura
- No modificar datos runtime
- No editar migraciones aplicadas

## VERIFICATION
- billing-service:test termina exitosamente
- Pruebas Vitest focalizadas terminan exitosamente
- Cualquier fallo se convierte en el siguiente WRITE mínimo

## ROLLBACK
No aplica; operación de verificación.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- billing-service/src/main/java
- billing-service/src/test/java
- frontend/src/pages/Billing
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :billing-service:test --no-daemon
- npm --prefix frontend exec vitest run src/pages/Billing.test.tsx src/pages/Billing/PaymentModal.test.tsx src/pages/Billing/InvoiceDetailModal.test.tsx --reporter=dot
END_VERIFY_COMMANDS
