# A.SPEC AI-0155 — Verificar huéspedes y expediente documental existente

ID: AI-0155
Mode: VERIFY
RISK: LOW

## WHY
El dominio existente de huéspedes, identificaciones y perfil fiscal puede verificarse sin aplicar migraciones.

## WHAT
Ejecutar la batería focalizada de guest-service y conservar cualquier fallo como evidencia para una reparación mínima.

## SCOPE
- persistencia de huéspedes
- documentos de identidad
- perfil fiscal
- aislamiento por hotel
- RBAC existente

## OUT OF SCOPE
- Flyway
- storage externo
- nuevos DTOs
- producción

## CONTRACT
- tests guest-service reflejan contratos actuales
- no se modifican datos runtime

## INVARIANTS
- no editar migraciones
- no alterar código durante VERIFY

## VERIFICATION
- guest-service:test termina con código 0 o produce fallo accionable

## ROLLBACK
No aplica; VERIFY sin modificaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- guest-service/src/main
- guest-service/src/test
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :guest-service:test --no-daemon
END_VERIFY_COMMANDS
