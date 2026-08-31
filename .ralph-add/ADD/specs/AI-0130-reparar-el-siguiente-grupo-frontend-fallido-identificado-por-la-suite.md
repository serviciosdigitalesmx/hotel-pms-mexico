# A.SPEC AI-0130 — Reparar el siguiente grupo frontend fallido identificado por la suite

ID: AI-0130
Mode: WRITE
RISK: LOW

## WHY
El checkpoint registra 4 archivos y 19 pruebas fallidas, pero solo expone evidencia detallada de Stays. Tras reparar AI-0129, debe atacarse el siguiente fallo concreto sin repetir una auditoría equivalente.

## WHAT
Aplicar la reparación mínima al siguiente grupo de pruebas fallidas, usando exclusivamente el stack trace y comportamiento observado por Vitest.

## SCOPE
- Un solo grupo de pruebas relacionado por causa.
- Código frontend y pruebas directamente afectadas.
- Verificación focalizada del grupo reparado.

## OUT OF SCOPE
- Cambios backend.
- Migraciones Flyway.
- Cambios de secretos o infraestructura.
- Repetir una prueba fallida sin modificar la causa.

## CONTRACT
- Preservar contratos existentes de API, navegación y traducciones.
- No introducir mocks de capacidades inexistentes.

## INVARIANTS
- Cambios quirúrgicos y localizados.
- Mantener el build TypeScript/Vite funcional.
- Preservar trabajo no relacionado.

## VERIFICATION
- El grupo reparado queda verde.
- La suite completa reduce el conteo de fallos sin introducir nuevas regresiones.
- Los cambios quedan limitados a la superficie causal.

## ROLLBACK
Revertir únicamente los archivos modificados por la reparación causal de este A.SPEC.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
- frontend/src/pages
- frontend/src/components
- frontend/src/services
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- npm --prefix frontend exec -- vitest run --root frontend --configLoader runner --reporter=dot --pool=threads --maxWorkers=1
END_VERIFY_COMMANDS
