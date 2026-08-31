# A.SPEC AI-0156 — Verificar contratos actuales de facturación y pagos

ID: AI-0156
Mode: VERIFY
RISK: LOW

## WHY
La superficie de cobros existente debe quedar estable antes de construir cualquier UI derivada y antes de solicitar el contrato persistente de caja.

## WHAT
Ejecutar exclusivamente los tests de billing-service para validar facturas, pagos y métodos de pago existentes.

## SCOPE
- facturas
- pagos
- PaymentMethod
- reporte financiero

## OUT OF SCOPE
- turnos
- cajas
- arqueos
- cierres
- migraciones nuevas
- producción

## CONTRACT
- POST /api/v1/invoices/{invoiceId}/payments permanece compatible
- CASH sigue soportado
- no se inventan contratos

## INVARIANTS
- no editar migraciones
- no modificar backend durante VERIFY

## VERIFICATION
- billing-service:test termina con código 0 o produce causa concreta para reparación

## ROLLBACK
No aplica; VERIFY sin modificaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- billing-service/src/main
- billing-service/src/test
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :billing-service:test --no-daemon
END_VERIFY_COMMANDS
