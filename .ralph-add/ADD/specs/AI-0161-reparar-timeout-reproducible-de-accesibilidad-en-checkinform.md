# A.SPEC AI-0161 — Reparar timeout reproducible de accesibilidad en CheckInForm

ID: AI-0161
Mode: WRITE
RISK: LOW

## WHY
La verificación reciente falla por timeout de 5 segundos en la prueba axe, después de que el componente espera el render y ejecuta lookups iniciales.

## WHAT
Aislar la causa mínima del bloqueo de accesibilidad y ajustar el flujo de render/espera para que los lookups no mantengan la prueba en estado indeterminado; modificar el timeout del test solo si la ejecución focalizada demuestra que axe excede el límite por costo legítimo.

## SCOPE
- CheckInForm y su prueba de accesibilidad
- Lookups iniciales ya mockeados
- Render estable antes de ejecutar axe

## OUT OF SCOPE
- Cambios Alloggiati
- Migraciones
- Cambios de API

## CONTRACT
- CheckInForm sigue renderizando checkin_title
- Los lookups fallidos continúan siendo no bloqueantes
- La prueba axe debe terminar determinísticamente

## INVARIANTS
- No cambiar la validación de huéspedes
- No cambiar la creación de estancias
- No introducir mocks de producción

## VERIFICATION
- CheckInForm.test.tsx pasa con threads, maxWorkers=1 y reporter dot
- No repetir la verificación sin un cambio aplicado

## ROLLBACK
Revertir únicamente los cambios de render/espera y timeout asociados a CheckInForm.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Stays/CheckInForm.tsx
- frontend/src/pages/Stays/CheckInForm.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend src/pages/Stays/CheckInForm.test.tsx --pool=threads --maxWorkers=1 --reporter=dot
END_VERIFY_COMMANDS
