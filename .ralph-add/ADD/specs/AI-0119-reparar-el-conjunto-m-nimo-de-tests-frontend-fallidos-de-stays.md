# A.SPEC AI-0119 — Reparar el conjunto mínimo de tests frontend fallidos de Stays

ID: AI-0119
Mode: WRITE
RISK: LOW

## WHY
El build frontend pasa, pero existen fallos deterministas en tests por textos traducidos o estado de renderizado. Es el bloqueo de calidad más pequeño y observable.

## WHAT
Aislar y corregir únicamente las expectativas o configuración de test que no coincidan con el contrato actual de Stays, sin debilitar cobertura ni cambiar comportamiento productivo.

## SCOPE
- Stays.test.tsx
- AlloggiatiReportSection.test.tsx
- setupTests.ts si es estrictamente necesario

## OUT OF SCOPE
- Cambios de API
- Cambios de negocio
- Migraciones
- Cambios de autenticación
- Cambios en componentes productivos salvo evidencia directa de regresión

## CONTRACT
- Las pruebas deben verificar el texto visible o la ausencia real definida por el componente.
- No aceptar selectores que oculten una regresión funcional.

## INVARIANTS
- npm --prefix frontend run build debe continuar pasando.
- No modificar trabajo preexistente no relacionado.

## VERIFICATION
- Los archivos de test afectados pasan.
- El build frontend continúa pasando.
- Registrar cualquier fallo restante sin repetir ciegamente la suite completa.

## ROLLBACK
Revertir únicamente los cambios realizados dentro de los tres paths permitidos, preservando el resto del worktree.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Stays.test.tsx
- frontend/src/pages/Stays/AlloggiatiReportSection.test.tsx
- frontend/src/setupTests.ts
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend run test -- --run src/pages/Stays.test.tsx src/pages/Stays/AlloggiatiReportSection.test.tsx
END_VERIFY_COMMANDS
