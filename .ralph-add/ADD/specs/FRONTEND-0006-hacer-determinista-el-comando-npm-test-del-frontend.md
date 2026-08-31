# A.SPEC FRONTEND-0006 — Hacer determinista el comando npm test del frontend

ID: FRONTEND-0006
Mode: WRITE
RISK: LOW

## WHY
Los 18 tests pasan, pero npm --prefix frontend test queda en TIMEOUT porque el script no termina de forma determinista.

## WHAT
Ajustar únicamente el script de pruebas para ejecutar Vitest en modo run y conservar la configuración obligatoria de threads con un solo worker.

## SCOPE
- frontend/package.json
- comando npm test

## OUT OF SCOPE
- Cambios de lógica de negocio
- Dependencias nuevas
- Configuración de Vitest fuera del script afectado
- ESLint

## CONTRACT
- npm --prefix frontend test debe finalizar con código de salida verificable
- Las pruebas deben ejecutarse sin forks
- HOTEL-PMS no debe usarse como raíz de Vitest

## INVARIANTS
- No modificar componentes ni pruebas existentes
- No alterar node_modules
- Preservar todos los cambios no confirmados

## VERIFICATION
- La prueba focalizada termina exitosamente
- El build TypeScript/Vite continúa exitoso
- No se repite el timeout de cierre

## ROLLBACK
Revertir únicamente el cambio del script de pruebas en frontend/package.json.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/HotelProfile.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
- npm --prefix frontend run build
END_VERIFY_COMMANDS
