# A.SPEC AI-0154 — Reparar timeout del runner frontend y cerrar Vitest/build

ID: AI-0154
Mode: WRITE
RISK: LOW

## WHY
El bloqueo frontend ya está diagnosticado; repetir VERIFY sin reparar violaría la política anti-loop.

## WHAT
Aplicar la reparación mínima al runner, configuración o tests reproduciblemente colgados.

## SCOPE
- Vitest no interactivo
- Tests frontend reproducibles
- TypeScript/Vite build

## OUT OF SCOPE
- Migraciones Flyway
- Secretos
- Backend
- Producción
- Nuevas funcionalidades de caja, expediente u OCR

## CONTRACT
- npm --prefix frontend test debe finalizar correctamente
- npm --prefix frontend run build debe finalizar con código 0
- No modificar APIs ni modelos backend

## INVARIANTS
- Preservar cambios preexistentes
- No crear artefactos fuera de los paths permitidos
- No introducir mocks de producción
- No modificar migraciones

## VERIFICATION
- Suite Vitest completa termina en menos de 180 segundos
- Build TypeScript/Vite termina en menos de 180 segundos
- No quedan fallos reproducibles del frontend

## ROLLBACK
Revertir únicamente el diff producido dentro de los paths permitidos por AI-0149 si la verificación muestra regresión.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/vite.config.*
- frontend/vitest.config.*
- frontend/src/**/*.test.*
- frontend/src/**/*.spec.*
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- timeout 180 npm --prefix frontend test -- --run --reporter=dot
- timeout 180 npm --prefix frontend run build
END_VERIFY_COMMANDS
