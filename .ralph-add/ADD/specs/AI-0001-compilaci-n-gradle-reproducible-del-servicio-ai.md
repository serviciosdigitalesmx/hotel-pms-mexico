# A.SPEC AI-0001 — Compilación Gradle reproducible del servicio AI

ID: AI-0001
Mode: READ_ONLY
RISK: LOW

## WHY
La compilación requerida no pudo ejecutarse porque el A.SPEC usa Maven, pero el repositorio está basado en Gradle. Primero debe existir evidencia ejecutable de compilación con el build system real.

## WHAT
Ejecutar la compilación del módulo frontdesk-service mediante el Gradle Wrapper existente y registrar el resultado reproducible.

## SCOPE
- Verificar que el Gradle Wrapper sea ejecutable.
- Compilar frontdesk-service sin ejecutar pruebas ni contactar proveedores AI.
- Registrar errores de compilación, si existen.

## OUT OF SCOPE
- Modificar código, pruebas o configuración.
- Ejecutar proveedores Ollama o DeepSeek.
- Validar timeout, fallback o selección de proveedor.
- Modificar Docker, base de datos, Redis, Git o archivos .env.

## CONTRACT
- El comando debe ejecutarse desde la raíz del repositorio.
- La verificación debe usar exclusivamente el Gradle Wrapper del repositorio.
- El resultado debe clasificarse como PASS o BLOCKER con la salida observada.

## INVARIANTS
- No se modifican archivos del repositorio.
- No se imprimen secretos ni valores de .env.
- No se realizan peticiones a proveedores AI.
- No se reinician ni recrean servicios.

## VERIFICATION
- PASS si ./gradlew :frontdesk-service:compileJava termina con código 0.
- BLOCKER si el wrapper no puede ejecutarse o la compilación falla.

## ROLLBACK
No aplica: la operación es READ_ONLY y no modifica el checkout.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/
- gradlew
- gradle/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :frontdesk-service:compileJava
END_VERIFY_COMMANDS
