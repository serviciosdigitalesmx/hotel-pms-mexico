# A.SPEC AI-0085 — Ejecutar y clasificar la suite unitaria real del frontend

ID: AI-0085
Mode: VERIFY
RISK: LOW

## WHY
AI-0084 confirmó que el bloqueo previo fue del supervisor y que el comando correcto usa Vitest. Falta evidencia real del estado de la suite.

## WHAT
Ejecutar Vitest sin modificar archivos, registrar resultado completo, tests fallidos, errores de configuración, duración y código de salida.

## SCOPE
- Suite unitaria frontend
- Configuración Vitest
- Compatibilidad del entorno local

## OUT OF SCOPE
- Cambios de código
- Instalación de dependencias
- Cobertura
- Playwright E2E
- Migraciones
- Deploy
- Cambios de secretos

## CONTRACT
- No modificar el worktree
- No instalar ni actualizar paquetes
- Reportar PASS, FAIL o BLOCKED con evidencia reproducible

## INVARIANTS
- Preservar todos los cambios preexistentes
- No ejecutar operaciones destructivas
- No alterar base de datos ni servicios remotos

## VERIFICATION
- Confirmar que el comando se ejecuta realmente
- Capturar código de salida
- Clasificar cada fallo por causa
- Comparar git status --short antes y después

## ROLLBACK
No aplica: A.SPEC exclusivamente de lectura y verificación.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
