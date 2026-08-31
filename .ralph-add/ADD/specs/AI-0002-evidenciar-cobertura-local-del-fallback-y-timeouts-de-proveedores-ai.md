# A.SPEC AI-0002 — Evidenciar cobertura local del fallback y timeouts de proveedores AI

ID: AI-0002
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0001 confirmó la existencia del fallback y de dos proveedores, pero no demostró que sus fallos, timeouts y selección de proveedor estén cubiertos por pruebas ejecutables.

## WHAT
Inspeccionar y ejecutar únicamente la compilación y las pruebas locales existentes para obtener evidencia reproducible sobre timeout, error de proveedor, fallback determinista y selección Ollama/DeepSeek.

## SCOPE
- Documentar pruebas existentes y sus resultados.
- Identificar huecos concretos de resiliencia sin modificar código.
- Separar evidencia observada de inferencias.

## OUT OF SCOPE
- Modificar los siete archivos Java preexistentes.
- Crear migraciones o consultar bases de datos.
- Cambiar secretos, proveedores, Docker, Redis o configuración de producción.
- Añadir mocks, endpoints o contratos nuevos.

## CONTRACT
- La ruta autenticada /api/v1/stays/assistant permanece sin cambios.
- HotelSettings.aiModel continúa siendo la fuente de selección por tenant.
- Ollama y DeepSeek siguen siendo los únicos proveedores activos demostrados.
- Ante fallo de AI, LocalIntentRouter conserva el fallback local existente.

## INVARIANTS
- No se imprimen secretos ni credenciales.
- No se realizan peticiones a proveedores externos.
- No se modifican archivos, Git, base de datos ni infraestructura.
- Los cambios rastreados y archivos no rastreados de AI-0001 se preservan intactos.

## VERIFICATION
- Compilación local completada o fallo reproducible registrado.
- Pruebas relevantes ejecutadas o ausencia de pruebas documentada.
- Cada resultado se clasifica como PASS, WARNING o BLOCKER.
- Se genera un reporte de evidencia bajo .ralph-add/REPORTS sin alterar código de aplicación.

## ROLLBACK
No aplica: la transición es READ_ONLY; eliminar el reporte generado sería una acción separada y explícita.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/AssistantService.java
- frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant/engine/LocalIntentRouter.java
- frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant
- frontdesk-service/pom.xml
- .ralph-add/REPORTS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n "timeout|fallback|AssistantService|LocalIntentRouter|deepseek|ollama|chat/completions" frontdesk-service/src/main/java/com/hotelpms/frontdesk/assistant frontdesk-service/src/test/java/com/hotelpms/frontdesk/assistant
- mvn -pl frontdesk-service -DskipTests compile
- mvn -pl frontdesk-service -Dtest='*Assistant*Test,*Intent*Test' test
END_VERIFY_COMMANDS
