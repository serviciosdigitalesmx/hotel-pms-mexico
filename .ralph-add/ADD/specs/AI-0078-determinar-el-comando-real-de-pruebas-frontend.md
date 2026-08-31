# A.SPEC AI-0078 — Determinar el comando real de pruebas frontend

ID: AI-0078
Mode: READ_ONLY
RISK: LOW

## WHY
AI-0077 falló porque npm --prefix frontend test quedó BLOCKED_BY_SUPERVISOR; antes de declarar un hueco o repetir el bloqueo hay que identificar el contrato real de pruebas frontend.

## WHAT
Inspeccionar únicamente la configuración y estructura existente del frontend para determinar si existe un runner y cuál es el comando verificable, sin modificar archivos ni instalar dependencias.

## SCOPE
- Leer frontend/package.json y locks existentes
- Localizar tests, configuración de Vitest/Jest/Playwright/Cypress y scripts disponibles
- Documentar el comando frontend exacto o confirmar que no existe

## OUT OF SCOPE
- Modificar package.json o locks
- Instalar dependencias
- Ejecutar migraciones
- Cambiar secretos, RBAC o datos
- Deploy o cambios remotos

## CONTRACT
- El resultado debe distinguir entre script inexistente, runner presente pero bloqueado y pruebas ejecutables
- No se inventarán tests, scripts, endpoints ni dependencias
- No se alterará el worktree

## INVARIANTS
- No borrar, limpiar, resetear, sobrescribir ni stashar cambios preexistentes
- Solo lectura
- Backend Gradle permanece sin cambios
- No se declara V1 completa mientras frontend y smoke operativo sigan sin prueba verificable

## VERIFICATION
- node debe poder leer frontend/package.json sin modificarlo
- rg debe devolver evidencia reproducible de la configuración o ausencia de tests
- git status --short debe mostrar que no hubo mutaciones

## ROLLBACK
No aplica: A.SPEC estrictamente de solo lectura; si alguna herramienta intentara escribir, detenerla y conservar el worktree intacto.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/package-lock.json
- frontend/pnpm-lock.yaml
- frontend/yarn.lock
- frontend/src
- frontend/test
- frontend/tests
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- node -e "const p=require('./frontend/package.json'); console.log(JSON.stringify({scripts:p.scripts,packageManager:p.packageManager},null,2))"
- rg --files frontend | rg '(^|/)(test|tests|__tests__|vitest|jest|playwright|cypress)'
END_VERIFY_COMMANDS
