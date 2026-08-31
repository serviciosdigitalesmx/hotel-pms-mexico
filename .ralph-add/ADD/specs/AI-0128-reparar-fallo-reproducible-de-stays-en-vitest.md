# A.SPEC AI-0128 — Reparar fallo reproducible de Stays en Vitest

ID: AI-0128
Mode: WRITE
RISK: LOW

## WHY
El checkpoint confirma fallos frontend reproducibles y aporta evidencia concreta en Stays.test.tsx: la vista no muestra alloggiati_failed tras una respuesta fallida.

## WHAT
Aplicar la reparación mínima para que Stays traduzca y renderice correctamente el estado de error esperado por sus pruebas, preservando el flujo existente de carga, filtros y acciones.

## SCOPE
- Stays.tsx
- Stays.test.tsx
- componentes auxiliares de Stays únicamente si son necesarios para la reparación

## OUT OF SCOPE
- Migraciones Flyway
- cambios de backend
- secretos
- caja y turnos
- expediente digital
- OCR
- facturación
- deploy

## CONTRACT
- Las respuestas rechazadas de getAllStays deben producir un estado de error visible.
- La clave alloggiati_failed debe permanecer compatible con i18next.

## INVARIANTS
- No modificar contratos API.
- No eliminar funcionalidad existente.
- No editar migraciones aplicadas.
- Preservar cambios no relacionados del worktree.

## VERIFICATION
- El archivo Stays.test.tsx termina verde.
- No aparecen errores TypeScript en el área modificada.
- La salida de error es visible y estable para el usuario.

## ROLLBACK
Revertir únicamente los cambios realizados dentro de los allowed_paths de este A.SPEC, sin tocar el resto del worktree.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Stays.tsx
- frontend/src/pages/Stays.test.tsx
- frontend/src/pages/Stays/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec vitest run src/pages/Stays.test.tsx --reporter=dot
END_VERIFY_COMMANDS
