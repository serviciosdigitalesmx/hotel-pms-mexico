# A.SPEC AI-0157 — Verificar Vitest frontend con runner determinista

ID: AI-0157
Mode: VERIFY
RISK: LOW

## WHY
El build TypeScript/Vite ya está verde, pero la suite frontend aún no tiene evidencia de cierre.

## WHAT
Ejecutar la suite Vitest completa bajo la configuración obligatoria y conservar el primer fallo reproducible como evidencia para la reparación mínima siguiente.

## SCOPE
- frontend/src
- frontend/vitest.config.*

## OUT OF SCOPE
- Cambios de producto
- migraciones
- secretos
- deploy

## CONTRACT
- Usar frontend como raíz de Vitest
- Usar pool=threads y maxWorkers=1

## INVARIANTS
- No modificar código durante la verificación
- No usar forks
- Preservar cambios existentes

## VERIFICATION
- Resultado PASS/FAIL reproducible
- Listado de archivos y tests fallidos si aplica

## ROLLBACK
No aplica; verificación sin mutaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src --pool=threads --maxWorkers=1 --reporter=dot
END_VERIFY_COMMANDS
