# A.SPEC AI-0158 — Verificar auth-service

ID: AI-0158
Mode: VERIFY
RISK: LOW

## WHY
Los servicios guest y billing ya tienen evidencia verde; auth es una verificación independiente necesaria para cerrar la base operativa.

## WHAT
Ejecutar tests y cobertura del servicio de autenticación sin tocar configuración sensible.

## SCOPE
- auth-service

## OUT OF SCOPE
- Rotación de secretos
- cambios RBAC críticos
- migraciones
- deploy

## CONTRACT
- Mantener contratos actuales de autenticación
- No cambiar credenciales ni configuración sensible

## INVARIANTS
- No aplicar migraciones
- No reiniciar infraestructura remota
- No descartar trabajo ajeno

## VERIFICATION
- Gradle BUILD SUCCESSFUL
- Tests y cobertura completados

## ROLLBACK
No aplica; verificación sin cambios.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- auth-service
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :auth-service:test --no-daemon
END_VERIFY_COMMANDS
