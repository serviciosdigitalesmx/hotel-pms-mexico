# Integration contract

Integration is a separate role. A worker result is not completion. The state flow is `IMPLEMENTED -> VALIDATING -> PASSED -> INTEGRATED -> DONE`; failures are `VALIDATING -> FAILED -> RETURN_TO_WORKER`.

Run backend/frontend builds, unit/integration/architecture tests, Flyway checks, tenant/branch isolation, permission enforcement, API contracts, Docker/config checks, regressions, formatting/linting, and security-sensitive checks. Integrators may make only small conflict/build corrections; feature work returns to the owner.
