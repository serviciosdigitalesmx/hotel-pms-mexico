# A.SPEC AI-0127 — Reparar fallo reproducible de Stays.test.tsx

ID: AI-0127
Mode: WRITE
RISK: LOW

## WHY
El checkpoint demuestra un fallo reproducible en Stays.test.tsx: la expectativa alloggiati_failed no aparece aunque el backend mock entrega alloggiatiSendFailed=true. El build ya está verde; corresponde reparar la causa mínima sin repetir auditoría.

## WHAT
Alinear la representación del estado fallido de Alloggiati con el contrato actual de StayResponse y sus pruebas, conservando el comportamiento funcional y sin introducir endpoints ni datos nuevos.

## SCOPE
- Badge o mensaje de fallo Alloggiati en Stays
- Contrato de campos frontend existente
- Pruebas focalizadas de Stays

## OUT OF SCOPE
- Migraciones Flyway
- Backend
- Secretos
- RBAC
- Deploy
- Cambios destructivos

## CONTRACT
- Una estancia con alloggiatiSendFailed=true debe mostrar alloggiati_failed
- El retry existente debe conservar su comportamiento
- El build TypeScript/Vite debe continuar verde

## INVARIANTS
- Preservar cambios no relacionados del worktree
- No editar migraciones aplicadas
- No cambiar contratos backend
- No fabricar datos ni endpoints

## VERIFICATION
- Stays.test.tsx pasa completamente
- Build frontend pasa
- git diff --check pasa

## ROLLBACK
Revertir únicamente el diff de los archivos permitidos si la verificación focalizada falla.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Stays.tsx
- frontend/src/pages/Stays.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test -- --run src/pages/Stays.test.tsx --reporter=verbose
- npm --prefix frontend run build
- git diff --check
END_VERIFY_COMMANDS
