# A.SPEC AI-0125 — Restaurar sección de exportación JSON Alloggiati en Estancias para ADMIN y OWNER

ID: AI-0125
Mode: WRITE
RISK: LOW

## WHY
El checkpoint muestra 21 fallos concentrados en Stays.test.tsx porque ADMIN y OWNER no encuentran download_json_export. El componente funcional AlloggiatiReportSection ya existe y contiene la lógica real de exportación.

## WHAT
Reintegrar AlloggiatiReportSection en Stays.tsx y conectarlo al rol autenticado usando el contrato existente de useAuthStore, mostrando la exportación JSON únicamente a ADMIN y OWNER.

## SCOPE
- Importar y renderizar AlloggiatiReportSection
- Derivar isAdminOrOwner desde el usuario autenticado existente
- Conservar la restricción de no mostrar exportación a RECEPTIONIST
- Mantener el servicio stayService y endpoints existentes

## OUT OF SCOPE
- Cambios de backend
- Migraciones Flyway
- Cambios de RBAC
- Cambios de secretos
- Modificar tests para ocultar el fallo
- Resolver warnings globales de cierre de Vitest

## CONTRACT
- ADMIN y OWNER pueden ver download_json_export
- RECEPTIONIST no puede ver download_json_export
- La descarga usa stayService.downloadAlloggiatiJson existente
- No se inventan endpoints, DTOs ni permisos

## INVARIANTS
- No modificar worktree preexistente fuera de frontend/src/pages/Stays.tsx
- No alterar la lógica actual de estancias, filtros, checkout o paginación
- No ejecutar operaciones financieras, migraciones ni despliegues

## VERIFICATION
- Stays.test.tsx pasa para ADMIN, OWNER y RECEPTIONIST
- La suite frontend conserva el build exitoso
- git diff --check no reporta errores

## ROLLBACK
Revertir únicamente el diff de frontend/src/pages/Stays.tsx si la verificación falla; no tocar otros cambios del worktree.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages/Stays.tsx
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend test -- --run src/pages/Stays.test.tsx
END_VERIFY_COMMANDS
