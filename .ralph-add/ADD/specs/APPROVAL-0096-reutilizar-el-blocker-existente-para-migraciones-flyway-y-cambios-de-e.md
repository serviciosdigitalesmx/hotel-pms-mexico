# A.SPEC APPROVAL-0096 — Reutilizar el blocker existente para migraciones Flyway y cambios de esquema; no generar APPROVAL duplicado.

ID: APPROVAL-0096
Mode: WRITE
RISK: HIGH

## WHY
Requerido por el plan ADD actual.

## WHAT
Reutilizar el blocker existente para migraciones Flyway y cambios de esquema; no generar APPROVAL duplicado.

## SCOPE

## OUT OF SCOPE

## CONTRACT

## INVARIANTS

## VERIFICATION

## ROLLBACK
No aplicar cambios; conservar el worktree actual.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
END_VERIFY_COMMANDS
