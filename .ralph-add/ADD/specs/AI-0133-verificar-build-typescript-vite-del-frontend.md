# A.SPEC AI-0133 — Verificar build TypeScript/Vite del frontend

ID: AI-0133
Mode: VERIFY
RISK: LOW

## WHY
La suite focalizada pasa, pero la V1 requiere build frontend verde.

## WHAT
Construir el frontend con el contrato actual y capturar errores TypeScript/Vite reproducibles.

## SCOPE
- frontend/src
- frontend/vite.config.ts
- frontend/tsconfig.json

## OUT OF SCOPE
- Cambios de API
- migraciones
- secretos
- deploy

## CONTRACT
- Build de producción usa la configuración existente

## INVARIANTS
- No cambiar configuración durante la verificación

## VERIFICATION
- Exit code cero
- artefacto dist generado

## ROLLBACK
No aplica; operación de solo lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- cd frontend && npm run build
END_VERIFY_COMMANDS
