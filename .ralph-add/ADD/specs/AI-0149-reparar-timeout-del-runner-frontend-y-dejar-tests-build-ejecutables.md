# A.SPEC AI-0149 — Reparar timeout del runner frontend y dejar tests/build ejecutables

ID: AI-0149
Mode: WRITE
RISK: LOW

## WHY
El checkpoint confirma que backend está verde, pero Vitest y el build frontend expiran. Repetir la verificación sin reparar no aporta evidencia nueva.

## WHAT
Identificar y corregir la causa mínima del bloqueo del runner frontend, incluyendo scripts/configuración o tests reproduciblemente colgados, sin cambiar contratos backend.

## SCOPE
- Frontend test runner
- Configuración Vitest/Vite
- Fallos reproducibles de tests frontend
- Build TypeScript/Vite

## OUT OF SCOPE
- Migraciones Flyway
- Secretos
- Infraestructura remota
- Cambios backend no requeridos por la evidencia
- Nuevas funcionalidades de caja, expediente u OCR

## CONTRACT
- npm --prefix frontend test debe ejecutar Vitest en modo no interactivo
- npm --prefix frontend run build debe completar con código 0
- No modificar APIs ni modelos existentes

## INVARIANTS
- Preservar todos los cambios preexistentes
- No borrar ni sobrescribir trabajo ajeno
- Mantener comportamiento funcional existente
- No introducir mocks de producción

## VERIFICATION
- Vitest frontend termina dentro de 180 segundos
- Build frontend termina dentro de 180 segundos
- Los tests afectados por Billing se ejecutan explícitamente
- git diff limitado a allowed_paths

## ROLLBACK
Revertir únicamente el diff de AI-0149 si la verificación focalizada muestra regresión; no tocar cambios preexistentes.

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
