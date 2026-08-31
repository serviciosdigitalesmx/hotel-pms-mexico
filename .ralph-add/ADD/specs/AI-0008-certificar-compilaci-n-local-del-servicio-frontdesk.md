# A.SPEC AI-0008 — Certificar compilación local del servicio frontdesk

ID: AI-0008
Mode: VERIFY
RISK: LOW

## WHY
La integración IA real ya está inventariada; falta verificar que el código actual compile sin modificar fuentes.

## WHAT
Ejecutar la compilación determinista de frontdesk-service y registrar el resultado real.

## SCOPE
- Compilación Java de frontdesk-service
- Resolución local de dependencias requerida por Gradle

## OUT OF SCOPE
- Cambios en código
- Pruebas de proveedores externos
- Lectura de secretos
- Retries o circuit breaker
- Cambios de base de datos
- Deploy

## CONTRACT
- La compilación debe finalizar con código de salida 0
- No se deben modificar archivos fuente ni configuración

## INVARIANTS
- Se conserva el workspace y todos los cambios preexistentes
- No se invocan proveedores IA externos
- No se ejecutan acciones PMS ni migraciones

## VERIFICATION
- Resultado del comando y código de salida
- Errores de compilación, si existen
- Confirmación de que no hubo cambios intencionales en fuentes

## ROLLBACK
No aplica: VERIFY no modifica intencionalmente el código; cualquier caché generado por Gradle queda fuera del árbol fuente.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:compileJava --no-daemon
END_VERIFY_COMMANDS
