# A.SPEC AI-0095 — Ejecutar suite Vitest real y capturar resultado reproducible

ID: AI-0095
Mode: VERIFY
RISK: LOW

## WHY
AI-0094 identificó el runner y las dependencias, pero no existe evidencia técnica de ejecución ni resultado de tests.

## WHAT
Ejecutar únicamente la suite Vitest declarada por el frontend y registrar exit code, resumen, primer error reproducible y cualquier archivo generado.

## SCOPE
- Ejecutar npm --prefix frontend test
- Capturar stdout y stderr completos
- Registrar estado del worktree antes y después
- Clasificar PASS, FAIL o BLOCKED según evidencia real

## OUT OF SCOPE
- Modificar código o configuración
- Instalar o actualizar dependencias
- Ejecutar cobertura
- Ejecutar Playwright
- Modificar Git, Docker, base de datos o servicios externos

## CONTRACT
- El comando ejecutado debe ser exactamente npm --prefix frontend test
- El resultado debe incluir exit code real
- Un fallo solo se declara si Vitest produce error reproducible
- Si el supervisor vuelve a bloquear la ejecución, conservar BLOCKED sin inferir fallo de código

## INVARIANTS
- No modificar archivos ni dependencias
- No alterar el worktree existente
- No cambiar configuración, secretos, datos ni servicios
- No declarar la suite verde sin exit code 0 y resumen de Vitest

## VERIFICATION
- Exit code documentado
- Cantidad de tests, archivos y duración documentadas cuando Vitest ejecute
- Primer error y stack trace documentados si falla
- git status --short comparado antes y después
- Resultado final clasificado como PASS, FAIL o BLOCKED

## ROLLBACK
No aplica: operación de verificación sin escrituras intencionales; si aparecen artefactos generados, documentarlos y no eliminarlos automáticamente.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- git status --short
- npm --prefix frontend test
- git status --short
END_VERIFY_COMMANDS
