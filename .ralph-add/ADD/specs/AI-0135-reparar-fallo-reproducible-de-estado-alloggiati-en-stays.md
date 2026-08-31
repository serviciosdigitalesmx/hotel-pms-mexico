# A.SPEC AI-0135 — Reparar fallo reproducible de estado alloggiati en Stays

ID: AI-0135
Mode: WRITE
RISK: LOW

## WHY
La evidencia reciente identifica un fallo concreto: Stays no renderiza alloggiati_failed tras una respuesta fallida. La verificación completa del frontend quedó bloqueada por el supervisor y no debe repetirse sin reparación.

## WHAT
Aplicar la reparación mínima para traducir y mostrar el estado de error esperado, preservando carga, filtros y acciones existentes.

## SCOPE
- Corregir el manejo visible de errores de getAllStays
- Mantener compatibilidad con la clave i18next alloggiati_failed
- Verificar exclusivamente Stays.test.tsx

## OUT OF SCOPE
- Migraciones Flyway
- Backend
- Secretos
- Caja y turnos
- Expediente digital
- OCR
- Facturación
- Deploy

## CONTRACT
- Las respuestas rechazadas de getAllStays producen un estado de error visible
- La clave alloggiati_failed permanece compatible con i18next
- No se modifican contratos API

## INVARIANTS
- Preservar cambios no relacionados del worktree
- No editar migraciones aplicadas
- No eliminar funcionalidad existente

## VERIFICATION
- Stays.test.tsx termina verde
- La salida alloggiati_failed es visible y estable
- No aparecen errores TypeScript en el área modificada

## ROLLBACK
Revertir únicamente los cambios realizados dentro de los allowed_paths de AI-0128.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Stays.tsx
- frontend/src/pages/Stays.test.tsx
- frontend/src/pages/Stays/
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec vitest run src/pages/Stays.test.tsx --reporter=dot
END_VERIFY_COMMANDS
