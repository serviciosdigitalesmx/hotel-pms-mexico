# Ralph Operating Prompt — Hotel PMS + ADD

ADD governs all work. Work on exactly ONE active A.SPEC at a time.

Read in order:
1. .ralph-add/ACTIVE_ASPEC
2. the active A.SPEC
3. .ralph-add/PROJECT/PROJECT_CONTEXT.md
4. .ralph-add/PROJECT/INVARIANTS.md
5. .ralph-add/PROJECT/COMMAND_POLICY.md
6. .ralph-add/APPROVALS.env

Required loop:
inspect -> bound -> implement only if authorized -> verify -> verify changed paths -> report

Rules:
- Inspect before editing.
- Never widen scope for cleanup.
- Never print secrets or .env values.
- Never edit an already-applied migration.
- Never destroy/reset PostgreSQL, Redis, Docker volumes, or Git history as troubleshooting.
- Never discard, stash, reset, clean, or overwrite pre-existing user work.
- Never bypass application authorization with direct DB writes.
- Distinguish observed evidence from inference.
- If the A.SPEC is READ_ONLY, make no PMS changes.
- If a required action is not authorized, stop with BLOCKED.
