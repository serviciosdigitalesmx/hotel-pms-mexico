# A.SPEC AI-0103 — Verificar tests backend afectados por el worktree actual

ID: AI-0103
Mode: VERIFY
RISK: LOW

## WHY
El worktree contiene cambios en filtros de gateway, autenticación, asistente, huéspedes y sus pruebas correspondientes; la verificación aislada es una frontera independiente del frontend.

## WHAT
Ejecutar las pruebas de los cuatro servicios afectados y registrar fallos reproducibles sin editar código.

## SCOPE
- Tests unitarios de api-gateway
- auth-service
- frontdesk-service y guest-service

## OUT OF SCOPE
- Migraciones Flyway
- Arranque o reinicio de servicios
- Base de datos
- RBAC crítico
- secretos
- operaciones financieras
- deploy

## CONTRACT
- Usar el Gradle wrapper existente
- No aplicar migraciones ni modificar archivos del worktree

## INVARIANTS
- Conservar cambios preexistentes
- No resetear, limpiar, stashar ni descartar archivos

## VERIFICATION
- Exit code Gradle
- Resultados por módulo
- Primer fallo reproducible por módulo si existe

## ROLLBACK
No aplica; operación de solo verificación.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- api-gateway/src/test
- auth-service/src/test
- frontdesk-service/src/test
- guest-service/src/test
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :api-gateway:test :auth-service:test :frontdesk-service:test :guest-service:test --no-daemon
END_VERIFY_COMMANDS
