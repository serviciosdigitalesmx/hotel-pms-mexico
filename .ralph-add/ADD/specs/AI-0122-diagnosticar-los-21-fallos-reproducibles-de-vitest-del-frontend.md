# A.SPEC AI-0122 — Diagnosticar los 21 fallos reproducibles de Vitest del frontend

ID: AI-0122
Mode: READ_ONLY
RISK: LOW

## WHY
El build de producción pasa, pero la última ejecución de Vitest terminó con 21 fallos en 4 archivos. Los fallos incluyen expectativas de la exportación JSON de Stays y deben clasificarse antes de cualquier reparación.

## WHAT
Reproducir y clasificar cada fallo, separando regresiones reales, expectativas desactualizadas, aislamiento de mocks/traducciones y problemas del runner. Identificar el cambio mínimo verificable para una futura A.SPEC de reparación.

## SCOPE
- Frontend test runner y configuración vigente
- Stays.test.tsx y componentes relacionados
- Mocks de autenticación, servicios y traducciones
- Registro exacto de archivos, líneas y causas de los 21 fallos

## OUT OF SCOPE
- Modificar código o tests
- Actualizar dependencias
- Cambiar traducciones
- Backend, Flyway o base de datos
- Secretos, RBAC, despliegues o reinicios

## CONTRACT
- No se muta ningún archivo del worktree
- Cada fallo se documenta con causa reproducible o se marca como no reproducible
- No se confunde un bloqueo del supervisor con un fallo real
- La salida produce un alcance cerrado para la siguiente reparación

## INVARIANTS
- Se conservan todos los cambios preexistentes
- No se ejecutan migraciones ni operaciones remotas
- El build exitoso permanece como evidencia independiente
- No se editan migraciones Flyway ni configuraciones de secretos

## VERIFICATION
- Vitest termina con reporte verbose y códigos de salida observables
- Stays.test.tsx se ejecuta de forma aislada
- Se confirma si download_json_export está presente o ausente según el rol
- git diff --check permanece limpio

## ROLLBACK
No aplica; trabajo estrictamente read-only.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/package.json
- frontend/vitest.config.*
- frontend/src/pages/Stays.test.tsx
- frontend/src/pages
- frontend/src/setupTests.ts
- frontend/src/locales
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test -- --run --reporter=verbose
- npm --prefix frontend test -- --run src/pages/Stays.test.tsx --reporter=verbose
- git diff --check
END_VERIFY_COMMANDS
