# A.SPEC AI-0007 — Establecer el inventario real de integración IA y sus límites operativos

ID: AI-0007
Mode: READ_ONLY
RISK: LOW

## WHY
La compilación de frontdesk-service pasó, pero no demuestra que exista una integración IA operativa ni define sus contratos, proveedores, timeouts, fallback, observabilidad o tratamiento de datos.

## WHAT
Obtener evidencia del código y configuración reales para identificar cualquier integración IA existente, sus puntos de entrada, dependencias, secretos, límites y comportamiento ante fallos.

## SCOPE
- Código y configuración existentes relacionados con IA
- Contratos HTTP o internos reales
- Configuración de timeouts, circuit breakers, retries y fallback
- Exposición potencial de datos personales o de hotel

## OUT OF SCOPE
- Crear endpoints, módulos o proveedores IA
- Modificar código o configuración
- Añadir secretos o datos de prueba
- Cambiar esquemas, autenticación o infraestructura

## CONTRACT
- El resultado debe distinguir explícitamente entre integración existente, referencias documentales y ausencia de implementación.
- Toda capacidad declarada debe estar respaldada por una ruta, clase, configuración o contrato existente.

## INVARIANTS
- No se modifica ningún archivo fuente ni configuración.
- No se inventan proveedores, endpoints, DTOs, tablas ni capacidades.
- La compilación previamente verificada permanece sin cambios.

## VERIFICATION
- La búsqueda termina con un inventario reproducible de coincidencias o evidencia de ausencia.
- Cada hallazgo incluye archivo y ubicación.
- Se identifican los gaps necesarios antes de proponer una modificación WRITE.

## ROLLBACK
No aplica: el paso es exclusivamente de lectura y no muta el workspace.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- README.md
- docs
- config-service/src/main/resources
- api-gateway/src/main
- frontdesk-service/src/main
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n -i "openai|anthropic|llm|embedding|prompt|chatcompletion|ai|provider" README.md docs config-service/src/main/resources api-gateway/src/main frontdesk-service/src/main
END_VERIFY_COMMANDS
