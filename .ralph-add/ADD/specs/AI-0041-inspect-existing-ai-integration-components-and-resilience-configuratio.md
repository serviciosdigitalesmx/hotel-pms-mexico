# A.SPEC AI-0041 — Inspect existing AI integration components and resilience configurations

ID: AI-0041
Mode: READ_ONLY
RISK: LOW

## WHY
To elevate the PMS AI integration to a resilient, production-ready architecture, we must first map out all existing AI services, clients, endpoints, fallback strategies, and Resilience4j configurations across the microservices.

## WHAT
Inspect codebase for existing AI integration points, configurations, prompt management, and fallback mechanisms.

## SCOPE
- Locate all AI-related controllers, services, clients, and configuration files across backend microservices.
- Identify gaps in resilience (e.g. missing timeouts, circuit breakers, fallback providers, rate limiters, or error boundaries).

## OUT OF SCOPE
- Modifying application source code or build configurations.
- Adding external AI provider credentials or mutating infrastructure.

## CONTRACT
- No source files or build artifacts will be modified.
- Diagnostic output will identify existing AI code locations and resilience coverage.

## INVARIANTS
- Workspace remains completely untouched.
- Only safe, non-destructive search commands are executed.

## VERIFICATION
- Search command lists relevant AI source files and configurations without non-zero exit errors.

## ROLLBACK
No rollback actions required for read-only operations.

## MACHINE BOUNDS
BEGIN_ALLOWED_PATHS
END_ALLOWED_PATHS

BEGIN_VERIFY_COMMANDS
- grep -r -i -E 'openai|anthropic|llm|spring-ai|aiservice|prompt' --include='*.java' --include='*.kt' --include='*.yml' --include='*.properties' --include='*.gradle*' . || true
END_VERIFY_COMMANDS
