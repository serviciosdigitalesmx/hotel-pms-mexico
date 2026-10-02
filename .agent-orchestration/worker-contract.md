# Worker contract

The user-provided `MASTER CONTRACT — CAMRA -> FIXI` is authoritative for this run. It closes the product decisions for tenant/branch isolation, granular permissions, Customer/Device, workflows, requests, quotes, authorizations, inventory optionality, finance, Evolution, AI, auditability, transactions, idempotency, migrations, frontend, testing, ownership, Ralph execution, and definition of done. Workers must not re-escalate those decisions as missing. If a task needs a concrete implementation detail not fixed by that contract, choose the safest maintainable option consistent with Camra and record it as an implementation decision, not a blocker.

Camra is the product base. Read the assigned task and the authoritative Fixi decisions before editing. Inspect first; modify only `owned_paths`; escalate shared-contract changes to the orchestrator. Do not create a parallel Express/Supabase architecture or fake hotel-to-Fixi mappings.

Use additive Flyway migrations; never edit an applied historical migration. Preserve tenant and branch isolation, server-side granular authorization, transactions, idempotency, auditability, and existing observability/resilience. Add tests and run every validation command. Do not declare DONE with failing tests, hide errors, delete functionality without dependency evidence, or commit outside the assigned branch/worktree.

Return exactly these fields: `STATUS`, `TASK_ID`, `FILES_CHANGED`, `MIGRATIONS`, `ENDPOINTS`, `EVENTS`, `TESTS_ADDED`, `TESTS_RUN`, `TEST_RESULTS`, `KNOWN_ISSUES`, `CONTRACT_CHANGES_REQUESTED`, `READY_FOR_INTEGRATION`.
