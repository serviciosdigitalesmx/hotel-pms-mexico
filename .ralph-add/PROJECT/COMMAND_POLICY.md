# Command Policy

Tier 0 safe inspection:
pwd, ls, find, rg/grep, sed/awk/head/tail, git status/log/rev-parse,
docker compose ps/config, safe health GETs, static config inspection.

Tier 1 workspace mutation:
build/test commands, generated build outputs, source edits explicitly in A.SPEC.

Tier 2 explicit approval:
DB/schema migrations, secrets, auth/RBAC, financial mutations, service restarts,
remote infrastructure, production deploy/rollback, Git history changes.

Tier 3 never implicit:
docker compose down -v, docker volume rm, destructive SQL, flyway clean,
flyway repair without forensic A.SPEC, git reset --hard, git clean -fdx,
deleting/overwriting .env, production destructive actions.

When uncertain, do not execute; classify BLOCKED.
