# A.SPEC AI-0141 — Verificar gateway y filtros públicos modificados

ID: AI-0141
Mode: VERIFY
RISK: LOW

## WHY
CsrfFilter, PublicBookingFilter y su prueba nueva están modificados; la validación es independiente de los bloqueos de esquema.

## WHAT
Ejecutar la suite del gateway y verificar CSRF y booking público con los contratos actuales.

## SCOPE
- CsrfFilter
- PublicBookingFilter
- PublicBookingFilterTest

## OUT OF SCOPE
- Cambios de autenticación global
- Secretos
- Deploy

## CONTRACT
- Preservar rutas públicas existentes
- Mantener protección CSRF para rutas protegidas

## INVARIANTS
- No abrir endpoints protegidos
- No romper reservas públicas

## VERIFICATION
- Gradle exit 0
- Pruebas de filtros verdes

## ROLLBACK
No aplica: job de verificación sin escritura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- api-gateway/src/main/java/com/hotelpms/gateway/filter
- api-gateway/src/test/java/com/hotelpms/gateway/filter
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :api-gateway:test --no-daemon
END_VERIFY_COMMANDS
