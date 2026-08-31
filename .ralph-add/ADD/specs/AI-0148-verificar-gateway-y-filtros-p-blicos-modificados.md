# A.SPEC AI-0148 — Verificar gateway y filtros públicos modificados

ID: AI-0148
Mode: VERIFY
RISK: LOW

## WHY
La verificación del gateway es independiente de las funciones bloqueadas por esquema y evita repetir la verificación frontend bloqueada por supervisor.

## WHAT
Comprobar filtros, rutas públicas y comportamiento del gateway con las pruebas existentes.

## SCOPE
- Filtros HTTP
- Rutas públicas
- Manejo de errores del gateway

## OUT OF SCOPE
- Cambios de secretos
- Despliegue
- Migraciones
- Cambios de contratos no evidenciados

## CONTRACT
- Las rutas públicas actuales siguen accesibles según su contrato
- Los filtros conservan autenticación y manejo de errores

## INVARIANTS
- Solo lectura
- No modificar configuración operativa
- No reiniciar servicios

## VERIFICATION
- api-gateway:test termina con código 0
- Todo fallo reproducible genera reparación mínima

## ROLLBACK
No aplica; operación de verificación.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- api-gateway/src/main
- api-gateway/src/test
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :api-gateway:test --no-daemon
END_VERIFY_COMMANDS
