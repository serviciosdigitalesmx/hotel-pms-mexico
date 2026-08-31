# A.SPEC AI-0039 — Actualizar stubs y verificaciones en UserManagementServiceImplTest

ID: AI-0039
Mode: WRITE
RISK: MEDIUM

## WHY
El diagnóstico AI-0038 identificó que los 5 fallos en UserManagementServiceImplTest son causados por stubs y verificaciones desactualizados que invocan métodos antiguos (findAllByHotelId, existsByUsername, existsByEmail) en lugar de las variantes *IncludingInactive utilizadas por la implementación.

## WHAT
Ajustar UserManagementServiceImplTest.java para que configure y verifique los métodos incluyendo inactivos (findAllByHotelIdIncludingInactive, existsByUsernameIncludingInactive, existsByEmailIncludingInactive), resolviendo UnnecessaryStubbingException y alineando el comportamiento del test con la implementación.

## SCOPE
- auth-service/src/test/java/com/hotelpms/auth/service/UserManagementServiceImplTest.java

## OUT OF SCOPE
- auth-service/src/main/java/com/hotelpms/auth/service/UserManagementServiceImpl.java
- auth-service/src/main/java/com/hotelpms/auth/repository/UserAccountRepository.java
- Cualquier modificación a DTOs, mappers o controladores de auth-service
- Cualquier otro módulo fuera de auth-service

## CONTRACT
- Reemplazar stubs y verifies de findAllByHotelId por findAllByHotelIdIncludingInactive
- Reemplazar stubs de existsByUsername y existsByEmail por existsByUsernameIncludingInactive y existsByEmailIncludingInactive
- Asegurar que la suite de pruebas unitarias UserManagementServiceImplTest ejecute y pase al 100%

## INVARIANTS
- No alterar la implementación de UserManagementServiceImpl ni repositorios
- Preservar la configuración estricta de Mockito (@ExtendWith(MockitoExtension.class)) sin ignorar stubs
- No modificar esquema de base de datos ni migraciones de Flyway

## VERIFICATION
- Ejecutar ./gradlew :auth-service:test --tests *UserManagementServiceImplTest y confirmar que todas las pruebas pasan sin UnnecessaryStubbingException ni NullPointerException

## ROLLBACK
git checkout -- auth-service/src/test/java/

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- auth-service/src/test/java/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :auth-service:test --tests *UserManagementServiceImplTest
END_VERIFY_COMMANDS
