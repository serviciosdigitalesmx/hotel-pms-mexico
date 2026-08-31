# A.SPEC AI-0151 — Verificar pruebas existentes de huéspedes y expediente documental

ID: AI-0151
Mode: VERIFY
RISK: MEDIUM

## WHY
Los contratos de huéspedes, documentos y perfil fiscal ya existen y deben cerrarse con evidencia antes de extenderlos.

## WHAT
Ejecutar pruebas focalizadas de huéspedes, documentos de identidad, perfil fiscal y aislamiento por hotel.

## SCOPE
- Huéspedes
- Documentos
- Perfil fiscal
- Aislamiento multi-hotel

## OUT OF SCOPE
- Migraciones
- Storage nuevo
- OCR
- RBAC crítico nuevo

## CONTRACT
- Usar entidades, endpoints y DTOs existentes

## INVARIANTS
- Aislamiento por hotel
- Persistencia documental existente
- Sin cambios de esquema

## VERIFICATION
- Pruebas guest-service verdes o causa concreta para reparación

## ROLLBACK
No requiere rollback; verificación sin mutaciones.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- guest-service/src
- guest-service/build.gradle
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- ./gradlew :guest-service:test --no-daemon
END_VERIFY_COMMANDS
