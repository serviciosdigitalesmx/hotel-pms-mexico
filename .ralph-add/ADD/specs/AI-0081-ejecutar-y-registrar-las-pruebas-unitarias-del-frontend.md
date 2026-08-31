# A.SPEC AI-0081 — Ejecutar y registrar las pruebas unitarias del frontend

ID: AI-0081
Mode: VERIFY
RISK: LOW

## WHY
AI-0080 confirmó la infraestructura de Vitest, pero no ejecutó las pruebas; el estado funcional de las suites sigue UNKNOWN.

## WHAT
Ejecutar el runner determinista de Vitest y registrar el resultado real, incluyendo fallos, errores de configuración, duración y conteo de pruebas.

## SCOPE
- Pruebas unitarias e integración bajo frontend/src
- Configuración y setup de Vitest
- Reporte del resultado observable

## OUT OF SCOPE
- Modificar código o pruebas
- Instalar dependencias
- Ejecutar Playwright
- Levantar servicios
- Modificar base de datos, secretos o Git

## CONTRACT
- El comando debe ejecutarse desde el contrato existente frontend/package.json
- Un resultado exitoso requiere exit code 0
- Los fallos deben conservar sus rutas y mensajes exactos para el siguiente A.SPEC

## INVARIANTS
- No modificar archivos del worktree
- No limpiar, resetear, descartar ni sobrescribir cambios preexistentes
- No iniciar operaciones remotas o destructivas

## VERIFICATION
- Confirmar exit code del comando
- Contabilizar pruebas passed, failed, skipped y errores de colección
- Clasificar PASS si todas pasan; BLOCKED si falta una dependencia o servicio local; FAIL si existen pruebas fallidas

## ROLLBACK
No aplica: operación de solo lectura y verificación; no debe producir cambios persistentes.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
