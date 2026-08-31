# A.SPEC FRONTEND-0003 — Verificar grupos frontend modificados con runner Vitest obligatorio

ID: FRONTEND-0003
Mode: VERIFY
RISK: LOW

## WHY
El build frontend ya está verde y estos tests son la superficie modificada con evidencia previa de fallos/hanging-process. La verificación focalizada determina la siguiente reparación sin repetir una auditoría.

## WHAT
Ejecutar únicamente el grupo frontend modificado y registrar fallos reproducibles, tiempos de finalización y procesos colgados.

## SCOPE
- Tests unitarios de páginas modificadas
- Detección de fallos reproducibles
- Confirmación de ejecución con threads y un worker

## OUT OF SCOPE
- Cambios de código
- Migraciones Flyway
- Secretos
- Despliegue
- Suite frontend completa

## CONTRACT
- El comando debe usar --root frontend
- El pool debe ser threads
- Debe ejecutarse con --maxWorkers=1
- No se deben usar forks

## INVARIANTS
- Preservar todos los cambios existentes
- No editar migraciones aplicadas
- No modificar configuración operativa

## VERIFICATION
- Todos los tests del grupo pasan
- El proceso Vitest termina limpiamente
- Si falla, generar el WRITE mínimo sobre la causa concreta

## ROLLBACK
No aplica; verificación sin mutaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/HotelProfile.test.tsx
- frontend/src/pages/Quotations.test.tsx
- frontend/src/pages/Quotations/QuotationForm.test.tsx
- frontend/src/pages/Rates/RateCalendar.test.tsx
- frontend/src/pages/Reservations/RoomSelection.test.tsx
- frontend/src/pages/Rooms/RateSeasonManagerModal.test.tsx
- frontend/src/pages/Rooms/RoomTypeList.test.tsx
- frontend/src/pages/Settings/SettingsAppearance.test.tsx
- frontend/src/pages/Settings/SettingsSystem.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx src/pages/Quotations.test.tsx src/pages/Quotations/QuotationForm.test.tsx src/pages/Rates/RateCalendar.test.tsx src/pages/Reservations/RoomSelection.test.tsx src/pages/Rooms/RateSeasonManagerModal.test.tsx src/pages/Rooms/RoomTypeList.test.tsx src/pages/Settings/SettingsAppearance.test.tsx src/pages/Settings/SettingsSystem.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
END_VERIFY_COMMANDS
