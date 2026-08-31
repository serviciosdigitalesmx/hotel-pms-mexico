# System Invariants — Hotel PMS

1. hotelId tenant isolation must never weaken.
2. An occupied room cannot be treated as available for another active stay.
3. Check-in must preserve room/stay/guest/billing coherence.
4. Historical data must not be silently deleted.
5. Applied Flyway migrations are immutable.
6. PostgreSQL must never be reset/recreated to bypass a bug.
7. Docker volumes must not be destroyed as generic troubleshooting.
8. Secrets must never appear in source, reports, logs, prompts, or commits.
9. Auth/RBAC remains enforced in real application layers.
10. AI/agent paths must not bypass authorization with direct DB writes.
11. Existing uncommitted work must not be reset, stashed, cleaned, or discarded.
12. One A.SPEC = one bounded observable transition.
13. Financial, auth/security, DB/schema, secret, mass, deploy, rollback and destructive actions are HIGH risk or above.
