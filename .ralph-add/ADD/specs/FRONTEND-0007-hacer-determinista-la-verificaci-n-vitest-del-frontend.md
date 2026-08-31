# A.SPEC FRONTEND-0007 — Hacer determinista la verificación Vitest del frontend

ID: FRONTEND-0007
Mode: WRITE
RISK: LOW

## WHY
El build ya está verde, pero el comando de pruebas no conserva explícitamente la raíz y el pool obligatorio y presenta cierre no determinista.

## WHAT
Ajustar únicamente el script npm test para usar Vitest run con --root frontend, threads y un worker.

## SCOPE
- frontend/package.json
- script test

## OUT OF SCOPE
- componentes
- tests
- dependencias
- backend
- migraciones

## CONTRACT
- Vitest usa frontend como root
- usa threads
- usa maxWorkers=1
- npm test termina determinísticamente

## INVARIANTS
- preservar todos los cambios existentes
- no modificar node_modules
- no usar forks

## VERIFICATION
- test focalizado termina con código 0
- TypeScript/Vite build termina con código 0

## ROLLBACK
Revertir únicamente el cambio del script test en frontend/package.json.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
- npm --prefix frontend run build
END_VERIFY_COMMANDS
