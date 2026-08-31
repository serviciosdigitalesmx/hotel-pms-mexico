# A.SPEC AI-0112 — Diagnóstico focalizado de fallos de tests de Stays

ID: AI-0112
Mode: READ_ONLY
RISK: LOW

## WHY
El checkpoint reporta fallos concentrados en Stays mientras el build frontend es verde.

## WHAT
Determinar si los fallos son regresiones funcionales o selectores/traducciones obsoletos y definir la reparación mínima.

## SCOPE
- Stays.test.tsx
- Stays.tsx
- AlloggiatiReportSection
- claves de traducción relacionadas

## OUT OF SCOPE
- Cambios de código
- Backend
- Migraciones
- Secretos
- Deploy

## CONTRACT
- No modificar el worktree
- No sobrescribir cambios preexistentes
- Separar evidencia de test de evidencia funcional

## INVARIANTS
- La exportación JSON solo debe existir cuando el contrato actual la expone
- No inventar claves ni endpoints

## VERIFICATION
- Resultado focalizado del test
- Rutas y líneas de la causa
- Propuesta de un único A.SPEC de reparación si procede

## ROLLBACK
No aplica; operación de solo lectura.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Stays.test.tsx
- frontend/src/pages/Stays.tsx
- frontend/src/pages/Stays
- frontend/src/locales
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- rg -n "download_json_export|no_active_stays|Alloggiati" frontend/src/pages/Stays.test.tsx frontend/src/pages/Stays.tsx frontend/src/pages/Stays frontend/src/locales
- npm --prefix frontend run test -- --run src/pages/Stays.test.tsx
END_VERIFY_COMMANDS
