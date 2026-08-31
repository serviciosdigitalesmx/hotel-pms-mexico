# A.SPEC FRONTEND-0002 — Restaurar campos Alloggiati visibles y accesibles en HotelProfile

ID: FRONTEND-0002
Mode: WRITE
RISK: LOW

## WHY
La causa reproducible ya está identificada: HotelProfile.test.tsx busca label_alloggiati_username, label_alloggiati_password y label_alloggiati_ws_key, pero esos controles no están presentes en el DOM. No corresponde repetir diagnóstico.

## WHAT
Reparar HotelProfile para conservar y renderizar los campos Alloggiati existentes con sus labels, ids, valores y binding actuales, respetando las condiciones de configuración existentes y sin inventar contratos.

## SCOPE
- Restaurar la sección de credenciales Alloggiati en el formulario existente
- Mantener labels accesibles asociados mediante for/id
- Preservar carga, edición y guardado con el modelo ya utilizado
- Confirmar que los 18 tests de HotelProfile pasan

## OUT OF SCOPE
- Cambios de migraciones o esquema
- Cambios de secretos
- Modificar pruebas para ocultar el fallo
- Cambios backend o de contrato API

## CONTRACT
- Los labels existentes label_alloggiati_username, label_alloggiati_password y label_alloggiati_ws_key deben ser consultables por getByLabelText
- Los campos deben conservar el contrato de persistencia ya implementado por HotelProfile
- No se deben exponer credenciales fuera del formulario autorizado

## INVARIANTS
- Los campos fiscales y de perfil actuales continúan funcionando
- Los 8 tests actualmente verdes permanecen verdes
- No se modifican archivos fuera de la superficie frontend indicada

## VERIFICATION
- HotelProfile.test.tsx pasa con el comando Vitest obligatorio usando threads y un worker
- El build de frontend termina correctamente
- No queda un servidor Vitest colgado que impida el cierre limpio

## ROLLBACK
Revertir únicamente el diff de frontend/src/pages/HotelProfile.tsx conservando todos los demás cambios del worktree.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/HotelProfile.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
- npm --prefix frontend run build
END_VERIFY_COMMANDS
