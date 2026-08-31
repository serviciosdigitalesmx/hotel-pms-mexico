# A.SPEC AI-0136 — Reparar fallos reproducibles restantes de frontend y cerrar Vitest

ID: AI-0136
Mode: WRITE
RISK: LOW

## WHY
El build ya está verde, pero Vitest mantiene 18 fallos en 3 archivos. La evidencia concreta muestra expectativas obsoletas frente a errores traducidos y el cambio reciente de Alloggiati.

## WHAT
Aplicar las reparaciones mínimas en implementación o expectativas de prueba según el contrato actual de i18n; no cambiar APIs ni contratos funcionales.

## SCOPE
- HotelProfile: manejo y expectativa del error INVALID_VAT_NUMBER
- Stays: mensaje traducido alloggiati_failed
- Pruebas focalizadas de los archivos afectados

## OUT OF SCOPE
- Migraciones Flyway
- Secretos
- Cambios de backend
- Deploy
- Cambios destructivos

## CONTRACT
- Los códigos de error UPPER_SNAKE_CASE son traducidos por el interceptor Axios antes de llegar a la UI
- La UI debe mostrar el mensaje traducido
- El build TypeScript/Vite debe permanecer verde

## INVARIANTS
- No sobrescribir trabajo preexistente
- No editar migraciones aplicadas
- No modificar contratos API
- No ocultar errores reales

## VERIFICATION
- Vitest focalizado sin fallos
- Build frontend exitoso

## ROLLBACK
Revertir únicamente los cambios de los archivos incluidos en este A.SPEC si la verificación falla.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/HotelProfile.tsx
- frontend/src/pages/HotelProfile.test.tsx
- frontend/src/pages/Stays.tsx
- frontend/src/pages/Stays.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec vitest run src/pages/HotelProfile.test.tsx src/pages/Stays.test.tsx --reporter=dot
- npm --prefix frontend run build
END_VERIFY_COMMANDS
