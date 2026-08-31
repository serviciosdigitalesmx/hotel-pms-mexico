# A.SPEC AI-0083 — Ejecutar y registrar la suite unitaria frontend

ID: AI-0083
Mode: VERIFY
RISK: LOW

## WHY
AI-0082 confirmó el entrypoint real, pero aún no existe evidencia de que las 89 suites unitarias frontend pasen.

## WHAT
Ejecutar Vitest en modo determinista, capturar el resultado completo, identificar fallos, errores de configuración, timeouts y conteos de tests.

## SCOPE
- Suite unitaria frontend
- Configuración Vitest y setupTests.ts
- Conteo de tests aprobados, fallidos y omitidos

## OUT OF SCOPE
- Modificar código
- Instalar o actualizar dependencias
- Ejecutar Playwright
- Build o lint
- Migraciones de base de datos
- Cambios de secretos
- Deploy

## CONTRACT
- El comando debe ejecutarse exactamente desde el contrato npm declarado en frontend/package.json.
- El reporte debe distinguir ejecución exitosa de mera detección estructural.
- Cualquier fallo debe conservar su error reproducible y ubicación.

## INVARIANTS
- No modificar archivos ni dependencias.
- No alterar el worktree preexistente.
- No limpiar, resetear, descartar ni stashar cambios.
- No declarar la suite verde si el proceso termina con error.

## VERIFICATION
- El proceso termina con código 0 o se registra código no cero.
- Se reportan los conteos finales de Vitest.
- Se conserva evidencia de fallos y archivos afectados.
- Se verifica git status --short antes y después para confirmar ausencia de cambios atribuibles.

## ROLLBACK
No aplica: A.SPEC exclusivamente de lectura y verificación; no realiza cambios.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test
END_VERIFY_COMMANDS
