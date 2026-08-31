# A.SPEC AI-0005 — Verificar compilación y pruebas locales del contrato IA

ID: AI-0005
Mode: VERIFY
RISK: LOW

## WHY
La auditoría estática confirmó el diseño, pero aún falta evidencia determinista de que el servicio y sus pruebas compilan y pasan localmente.

## WHAT
Ejecutar compilación y suite de pruebas del servicio que contiene AssistantService, LocalIntentRouter y la configuración IA.

## SCOPE
- frontdesk-service
- AssistantService
- LocalIntentRouter
- ConversationSessionStore
- migraciones IA relacionadas

## OUT OF SCOPE
- Modificar código
- Aplicar migraciones
- Iniciar o reiniciar servicios
- Conectar con Ollama o DeepSeek
- Cambiar secretos
- Desplegar a producción

## CONTRACT
- El módulo frontdesk-service compila correctamente.
- Las pruebas locales relacionadas con el flujo IA terminan exitosamente o reportan fallos reproducibles.

## INVARIANTS
- No se modifican archivos fuente ni configuración.
- No se ejecutan operaciones destructivas.
- No se realizan llamadas a proveedores externos.
- No se alteran datos de la base de datos.

## VERIFICATION
- Registrar código de salida de compilación.
- Registrar resultado y fallos de la suite de pruebas.
- Distinguir fallos de código de fallos del entorno o dependencias.

## ROLLBACK
No aplica: VERIFY no cambia código ni datos; eliminar únicamente artefactos temporales generados por la herramienta si fuera necesario.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./mvnw -pl frontdesk-service -DskipTests compile
- ./mvnw -pl frontdesk-service test
END_VERIFY_COMMANDS
