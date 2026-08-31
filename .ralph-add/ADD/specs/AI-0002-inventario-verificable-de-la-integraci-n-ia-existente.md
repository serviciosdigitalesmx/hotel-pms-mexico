# A.SPEC AI-0002 — Inventario verificable de la integración IA existente

ID: AI-0002
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0001 no identifica la causa efectiva del bloqueo de Gradle y no aporta evidencia sobre la arquitectura IA del PMS. Antes de diseñar cambios resilientes se necesita localizar los componentes, contratos y puntos de fallo reales.

## WHAT
Inspeccionar únicamente el código versionado del checkout para identificar servicios, adaptadores, configuración, endpoints, persistencia, colas y pruebas existentes relacionados con IA, además de registrar el estado Git sin modificarlo.

## SCOPE
- Localizar referencias y módulos de integración IA existentes
- Identificar contratos de entrada y salida actualmente implementados
- Detectar dependencias de proveedor, secretos, timeouts, reintentos y límites configurados
- Registrar pruebas y comandos de validación ya existentes

## OUT OF SCOPE
- Modificar código, configuración, dependencias o documentación
- Ejecutar Gradle, Docker, migraciones, servicios o despliegues
- Inspeccionar o cambiar secretos, bases de datos, Redis o infraestructura remota
- Diagnosticar definitivamente el bloqueo de /Users/usuario/.gradle/wrapper/dists

## CONTRACT
- La inspección debe producir un inventario basado exclusivamente en archivos existentes del checkout
- Toda capacidad no encontrada debe quedar como pendiente, no asumirse ni inventarse

## INVARIANTS
- No se modifican archivos ni permisos
- No se ejecutan comandos destructivos ni operaciones de escritura
- Se preserva el estado Git existente
- No se exponen valores de secretos

## VERIFICATION
- El inventario identifica rutas concretas o declara explícitamente que no se encontraron componentes IA
- git status permanece igual antes y después de la inspección
- La salida no contiene valores de secretos

## ROLLBACK
No aplica: esta A.SPEC es de solo lectura y no realiza cambios.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- /Users/usuario/Desktop/HOTEL-PMS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- git -C /Users/usuario/Desktop/HOTEL-PMS status --short
- rg -n -i "openai|anthropic|llm|ai|artificial intelligence|prompt|embedding|rag|chat" /Users/usuario/Desktop/HOTEL-PMS --glob '!**/build/**' --glob '!**/node_modules/**'
END_VERIFY_COMMANDS
