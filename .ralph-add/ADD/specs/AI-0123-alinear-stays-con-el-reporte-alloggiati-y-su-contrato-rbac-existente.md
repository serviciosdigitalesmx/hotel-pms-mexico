# A.SPEC AI-0123 — Alinear Stays con el reporte Alloggiati y su contrato RBAC existente

ID: AI-0123
Mode: WRITE
RISK: LOW

## WHY
La suite ya ejecutó realmente y confirmó 21 fallos; el fallo reproducible visible es que Stays no monta AlloggiatiReportSection, aunque sus tests esperan el export JSON para ADMIN y OWNER. La sección existente ya encapsula descarga, envío, permisos y manejo de errores.

## WHAT
Integrar AlloggiatiReportSection en Stays usando el selector de autenticación y el contrato RBAC existente, sin crear endpoints, DTOs, permisos ni datos nuevos. Ajustar únicamente expectativas si el contrato real del componente demuestra una diferencia puntual.

## SCOPE
- Montar AlloggiatiReportSection en la página Stays
- Derivar isAdminOrOwner desde el usuario autenticado existente
- Conservar la autorización interna que oculta JSON y submit a RECEPTIONIST
- Añadir o ajustar cobertura mínima de montaje y permisos

## OUT OF SCOPE
- Cambios de backend o API
- Migraciones Flyway
- Cambios de secretos o RBAC crítico
- Modificar AlloggiatiReportSection o sus servicios salvo incompatibilidad de compilación
- Resolver fallos de otros archivos sin evidencia específica

## CONTRACT
- ADMIN y OWNER pueden ver download_json_export
- RECEPTIONIST no puede ver download_json_export
- Las llamadas existentes downloadAlloggiatiReport, downloadAlloggiatiJson y submitAlloggiatiReport se conservan
- No se inventan capacidades ni contratos nuevos

## INVARIANTS
- Preservar todos los cambios preexistentes del worktree
- Cambios limitados a los paths permitidos
- No editar migraciones ni ejecutar operaciones externas
- La build de frontend debe continuar compilando

## VERIFICATION
- Stays.test.tsx termina sin fallos reproducibles
- La prueba de permisos mantiene ADMIN/OWNER permitidos y RECEPTIONIST denegado
- La build de producción termina con exit 0
- git diff --check termina con exit 0

## ROLLBACK
Revertir únicamente el diff nuevo de AI-0123 en los dos paths permitidos; no tocar cambios preexistentes ni usar reset, clean o checkout.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Stays.tsx
- frontend/src/pages/Stays.test.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test -- --run src/pages/Stays.test.tsx --reporter=verbose
- npm --prefix frontend run build
- git diff --check
END_VERIFY_COMMANDS
