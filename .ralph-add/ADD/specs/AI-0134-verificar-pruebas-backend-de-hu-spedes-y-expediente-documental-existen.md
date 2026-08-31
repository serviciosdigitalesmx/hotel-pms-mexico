# A.SPEC AI-0134 — Verificar pruebas backend de huéspedes y expediente documental existente

ID: AI-0134
Mode: VERIFY
RISK: LOW

## WHY
El servicio ya contiene contratos de huéspedes e identity documents; debe verificarse antes de implementar funciones duplicadas.

## WHAT
Ejecutar pruebas focalizadas de guest-service, incluyendo documentos de identidad, perfil fiscal y aislamiento por hotel.

## SCOPE
- guest-service/src/main/java
- guest-service/src/test/java

## OUT OF SCOPE
- Aplicar Flyway
- cambiar esquema
- storage externo
- producción

## CONTRACT
- Persistencia y DTOs actuales
- RBAC y aislamiento existentes

## INVARIANTS
- No editar migraciones
- No modificar datos ni servicios runtime

## VERIFICATION
- Pruebas Gradle verdes
- reporte de fallos concretos si los hay

## ROLLBACK
No aplica; operación de solo lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- guest-service/src/main
- guest-service/src/test
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :guest-service:test --no-daemon
END_VERIFY_COMMANDS
