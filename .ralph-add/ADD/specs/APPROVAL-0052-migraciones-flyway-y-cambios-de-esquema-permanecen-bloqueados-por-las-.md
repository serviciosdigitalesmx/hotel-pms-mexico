# A.SPEC APPROVAL-0052 — Migraciones Flyway y cambios de esquema permanecen bloqueados por las aprobaciones existentes; no bloquean estas verificaciones.

ID: APPROVAL-0052
Mode: WRITE
RISK: HIGH

## WHY
Requerido por el plan ADD actual.

## WHAT
Migraciones Flyway y cambios de esquema permanecen bloqueados por las aprobaciones existentes; no bloquean estas verificaciones.

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
