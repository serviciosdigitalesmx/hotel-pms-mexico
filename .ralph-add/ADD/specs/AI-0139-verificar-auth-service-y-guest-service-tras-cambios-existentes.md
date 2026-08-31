# A.SPEC AI-0139 — Verificar auth-service y guest-service tras cambios existentes

ID: AI-0139
Mode: VERIFY
RISK: LOW

## WHY
Hay cambios locales relevantes en autenticación y huéspedes, independientes de Flyway bloqueado y de frontend.

## WHAT
Ejecutar las pruebas focalizadas de los dos servicios y usar cualquier fallo como evidencia para el siguiente repair mínimo.

## SCOPE
- UserManagementService
- GuestService
- GuestController

## OUT OF SCOPE
- Migraciones
- Secretos
- Deploy
- Cambios de esquema

## CONTRACT
- Mantener los DTOs y endpoints existentes
- No modificar migraciones aplicadas

## INVARIANTS
- No perder aislamiento ni validaciones existentes
- No alterar trabajo local ajeno

## VERIFICATION
- Gradle exit 0
- Pruebas de auth y guest verdes

## ROLLBACK
No aplica: job de verificación sin escritura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- auth-service/src
- guest-service/src
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :auth-service:test :guest-service:test --no-daemon
END_VERIFY_COMMANDS
