# A.SPEC FRONTEND-0004 — Restaurar campos Alloggiati en HotelProfile y cerrar regresión Vitest

ID: FRONTEND-0004
Mode: WRITE
RISK: LOW

## WHY
La última verificación falló 10 pruebas porque HotelProfile ya no renderiza los campos label_alloggiati_username, label_alloggiati_password y label_alloggiati_ws_key que los tests y el contrato existente requieren.

## WHAT
Reintegrar en HotelProfile los campos Alloggiati, sus valores controlados, visibilidad independiente de secretos, carga desde HotelSettingsResponse, envío en updateHotelSettings y limpieza posterior al guardado.

## SCOPE
- Campos de usuario Alloggiati
- Password y WS key
- Toggle de visibilidad
- Estado de credenciales configuradas
- Persistencia y limpieza tras guardado

## OUT OF SCOPE
- Migraciones Flyway
- Secretos
- Cambios backend
- Edición de tests
- Deploy

## CONTRACT
- label_alloggiati_username
- label_alloggiati_password
- label_alloggiati_ws_key
- status_alloggiati_credentials_configured
- updateHotelSettings

## INVARIANTS
- No exponer secretos recibidos por el backend
- Password y WS key deben iniciar vacíos
- Los secretos deben conservar type=password salvo toggle individual
- El valor alloggiatiAutoSend debe preservarse
- No modificar otras secciones del perfil

## VERIFICATION
- HotelProfile.test.tsx debe finalizar sin fallos
- Las 10 pruebas previamente fallidas deben quedar verdes
- No usar forks ni HOTEL-PMS como Vitest root

## ROLLBACK
Revertir únicamente los cambios nuevos de frontend/src/pages/HotelProfile.tsx, preservando el worktree existente.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/HotelProfile.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
END_VERIFY_COMMANDS
