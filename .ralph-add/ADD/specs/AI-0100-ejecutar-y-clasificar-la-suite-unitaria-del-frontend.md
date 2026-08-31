# A.SPEC AI-0100 — Ejecutar y clasificar la suite unitaria del frontend

ID: AI-0100
Mode: VERIFY
RISK: LOW

## WHY
AI-0099 confirmó el contrato y las herramientas locales, pero el estado funcional de las pruebas sigue UNKNOWN.

## WHAT
Ejecutar Vitest completo, capturar código de salida, errores, pruebas fallidas, timeouts y cobertura reportada si aplica.

## SCOPE
- Pruebas unitarias y de componentes bajo frontend/src
- Configuración local de Vitest
- Clasificación PASS, FAIL o BLOCKED basada únicamente en evidencia

## OUT OF SCOPE
- Modificar código, configuración o dependencias
- Ejecutar build, lint o Playwright
- Instalar paquetes
- Modificar base de datos, secretos, Git o servicios externos

## CONTRACT
- No afirmar PASS si el proceso termina con errores o pruebas fallidas
- Distinguir fallo real de test, timeout, falta de dependencia y bloqueo del entorno
- Preservar íntegramente el worktree existente

## INVARIANTS
- No modificar archivos
- No ejecutar operaciones destructivas
- No leer ni imprimir secretos
- No instalar ni actualizar dependencias
- No cambiar estado externo

## VERIFICATION
- Registrar código de salida del comando
- Registrar resumen de tests passed, failed, skipped y errores
- Registrar si hubo bloqueos del supervisor o del entorno
- Comparar el resultado contra los umbrales declarados solo si se generó cobertura

## ROLLBACK
No aplica: operación exclusivamente de verificación y sin escrituras intencionales.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test -- --reporter=verbose
END_VERIFY_COMMANDS
