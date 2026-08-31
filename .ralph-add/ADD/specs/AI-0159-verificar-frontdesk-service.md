# A.SPEC AI-0159 — Verificar frontdesk-service

ID: AI-0159
Mode: VERIFY
RISK: LOW

## WHY
Frontdesk es el flujo operativo central de habitaciones, estancias y checkout; su verificación es independiente de frontend y billing.

## WHAT
Ejecutar la suite focalizada del servicio frontdesk y registrar cualquier fallo concreto para reparación inmediata.

## SCOPE
- frontdesk-service

## OUT OF SCOPE
- Migraciones Flyway
- cambios de caja bloqueados
- deploy
- datos de producción

## CONTRACT
- Preservar contratos actuales de estancia y checkout

## INVARIANTS
- No modificar esquemas
- No alterar secretos
- No sobrescribir cambios existentes

## VERIFICATION
- Gradle BUILD SUCCESSFUL
- Tests y cobertura completados

## ROLLBACK
No aplica; verificación sin cambios.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:test --no-daemon
END_VERIFY_COMMANDS
