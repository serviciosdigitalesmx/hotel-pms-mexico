# A.SPEC AI-0137 — Reparar y verificar los fallos Vitest restantes sin generar artefactos fuera de alcance

ID: AI-0137
Mode: WRITE
RISK: LOW

## WHY
AI-0136 no se aplicó por artefactos generados fuera de allowed_paths y EPERM del runner; la causa funcional ya está diagnosticada y requiere reparación, no otra auditoría.

## WHAT
Aplicar únicamente los ajustes mínimos de expectativas i18n y estado Alloggiati conforme al contrato actual. Ejecutar el runner evitando escritura de .eslintcache, tsbuildinfo y temporales fuera de alcance.

## SCOPE
- Alinear HotelProfile con los códigos y mensajes traducidos actuales
- Alinear Stays con alloggiati_failed
- Mantener intactos los contratos API y la implementación funcional

## OUT OF SCOPE
- Migraciones Flyway
- Secretos
- Backend
- Deploy
- Cambios destructivos

## CONTRACT
- Los errores UPPER_SNAKE_CASE se traducen antes de llegar a la UI
- La UI muestra el mensaje traducido vigente
- El estado alloggiati_failed permanece visible para roles autorizados

## INVARIANTS
- No modificar migraciones
- No sobrescribir trabajo preexistente
- No crear ni integrar artefactos fuera de allowed_paths
- Build TypeScript/Vite permanece verde

## VERIFICATION
- HotelProfile.test.tsx pasa
- Stays.test.tsx pasa
- Build frontend pasa
- No quedan cambios no autorizados por el A.SPEC

## ROLLBACK
Revertir solamente los cambios realizados en los tres archivos permitidos si la verificación falla.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/HotelProfile.test.tsx
- frontend/src/pages/Stays.tsx
- frontend/src/pages/Stays.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec vitest run src/pages/HotelProfile.test.tsx src/pages/Stays.test.tsx --reporter=dot
- npm --prefix frontend run build
END_VERIFY_COMMANDS
