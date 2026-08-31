# A.SPEC AI-0144 — Confirmar build TypeScript/Vite del frontend

ID: AI-0144
Mode: VERIFY
RISK: LOW

## WHY
El build es un gate independiente y requerido para V1; no depende de Flyway ni de aprobaciones pendientes.

## WHAT
Validar TypeScript y empaquetado Vite después de los cambios frontend acumulados.

## SCOPE
- TypeScript
- Vite build
- assets frontend

## OUT OF SCOPE
- Producción
- migraciones
- secretos
- E2E live

## CONTRACT
- npm run build debe finalizar con código 0

## INVARIANTS
- Preservar todos los cambios no relacionados

## VERIFICATION
- Código de salida 0
- sin errores TypeScript/Vite

## ROLLBACK
No requiere cambios; si falla, generar la reparación mínima sobre el archivo señalado.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- cd frontend && npm run build
END_VERIFY_COMMANDS
