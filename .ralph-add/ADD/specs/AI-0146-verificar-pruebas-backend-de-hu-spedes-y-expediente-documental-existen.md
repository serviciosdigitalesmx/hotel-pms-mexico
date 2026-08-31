# A.SPEC AI-0146 — Verificar pruebas backend de huéspedes y expediente documental existente

ID: AI-0146
Mode: VERIFY
RISK: LOW

## WHY
Los contratos de huéspedes, documentos de identidad y perfil fiscal ya existen y deben verificarse sin aplicar esquema.

## WHAT
Ejecutar la batería focalizada de guest-service.

## SCOPE
- Persistencia de huéspedes
- Documentos de identidad
- Perfil fiscal
- Aislamiento por hotel
- RBAC existente

## OUT OF SCOPE
- Migraciones Flyway
- Storage externo
- Nuevos DTOs
- Producción

## CONTRACT
- Reutilizar persistencia y DTOs actuales
- Conservar aislamiento y RBAC existentes

## INVARIANTS
- No editar migraciones
- No modificar datos runtime

## VERIFICATION
- guest-service:test termina con código 0
- Los fallos se clasifican como evidencia para reparación mínima

## ROLLBACK
No aplica; operación de verificación.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- guest-service/src/main
- guest-service/src/test
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :guest-service:test --no-daemon
END_VERIFY_COMMANDS
