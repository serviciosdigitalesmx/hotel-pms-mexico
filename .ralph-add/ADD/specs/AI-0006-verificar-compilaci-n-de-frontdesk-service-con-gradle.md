# A.SPEC AI-0006 — Verificar compilación de frontdesk-service con Gradle

ID: AI-0006
Mode: VERIFY
RISK: LOW

## WHY
La verificación anterior falló porque ./mvnw no existe; el manifiesto local confirma que el repositorio usa ./gradlew.

## WHAT
Ejecutar la compilación determinista del módulo frontdesk-service usando el wrapper Gradle observado.

## SCOPE
- Compilación de frontdesk-service
- Resolución local de dependencias y configuración Gradle

## OUT OF SCOPE
- Cambios en código fuente
- Cambios de configuración
- Pruebas de integración
- Despliegue o reinicio de servicios

## CONTRACT
- El comando debe ejecutarse desde la raíz del repositorio
- El resultado debe registrar exit code y salida completa de Gradle

## INVARIANTS
- No editar archivos fuente ni configuración
- No usar ./mvnw
- No ejecutar comandos destructivos

## VERIFICATION
- Exit code 0 confirma compilación exitosa
- Cualquier fallo debe conservar el error Gradle concreto para definir el siguiente paso

## ROLLBACK
No aplica; la compilación puede generar únicamente artefactos o cachés locales.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:compileJava --no-daemon
END_VERIFY_COMMANDS
