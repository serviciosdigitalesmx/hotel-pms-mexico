# A.SPEC APPROVAL-0087 — Flyway y cambios de esquema permanecen bloqueados por aprobación explícita; no bloquean esta reparación frontend.

ID: APPROVAL-0087
Mode: WRITE
RISK: HIGH

## WHY
Requerido por el plan ADD actual.

## WHAT
Flyway y cambios de esquema permanecen bloqueados por aprobación explícita; no bloquean esta reparación frontend.

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
