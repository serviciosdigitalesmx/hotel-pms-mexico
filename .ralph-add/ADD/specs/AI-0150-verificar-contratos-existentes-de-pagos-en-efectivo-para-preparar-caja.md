# A.SPEC AI-0150 — Verificar contratos existentes de pagos en efectivo para preparar caja

ID: AI-0150
Mode: VERIFY
RISK: MEDIUM

## WHY
Existe contrato financiero implementado y la verificación es independiente de Flyway y frontend.

## WHAT
Ejecutar pruebas focalizadas de pagos en efectivo, montos y estados actuales.

## SCOPE
- Pagos en efectivo
- Estados de cobro
- Contratos billing

## OUT OF SCOPE
- Crear caja
- Cambios de esquema
- Aplicar migraciones
- Producción

## CONTRACT
- Reutilizar endpoints y modelos existentes de billing

## INVARIANTS
- No modificar pagos existentes
- No alterar contratos públicos
- No aplicar migraciones

## VERIFICATION
- Suite focalizada de billing verde o fallo accionable

## ROLLBACK
No requiere rollback; verificación sin mutaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- billing-service/src
- billing-service/build.gradle
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :billing-service:test --no-daemon
END_VERIFY_COMMANDS
