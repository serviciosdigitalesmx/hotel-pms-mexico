# A.SPEC AI-0153 — Verificar gateway y filtros públicos modificados

ID: AI-0153
Mode: VERIFY
RISK: LOW

## WHY
Es independiente de billing, guest-service y frontend; confirma que los cambios públicos no rompieron el enrutamiento.

## WHAT
Ejecutar pruebas focalizadas de filtros, autenticación anónima y rutas públicas existentes.

## SCOPE
- Gateway
- Filtros
- Rutas públicas
- Sesión anónima

## OUT OF SCOPE
- Cambios de secretos
- Deploy
- Migraciones
- Nuevos endpoints

## CONTRACT
- Mantener rutas y filtros existentes

## INVARIANTS
- Rutas públicas no requieren bypass de autenticación
- 401 anónimo sigue siendo válido
- Sin cambios destructivos

## VERIFICATION
- Pruebas gateway verdes o fallo accionable

## ROLLBACK
No requiere rollback; verificación sin mutaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- api-gateway/src
- api-gateway/build.gradle
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :api-gateway:test --no-daemon
END_VERIFY_COMMANDS
