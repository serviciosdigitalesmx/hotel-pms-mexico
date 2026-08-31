# AI-0095 Verification Result

- Result: FAIL
- Source mutation: none detected
- Commands:
  - `git status --short`
  - `npm --prefix frontend test`
  - `git status --short`

## Local build manifest
Build system: Gradle root
Wrapper: ./gradlew
Docker Compose: present
Top-level directories: .add, .argos, .backups, .github, api-gateway, auth-service, billing-service, common-web-lib, config, config-service, docker, docs, fb-service, frontdesk-service, frontend, gradle, guest-service, internal-auth-lib, notification-service, pdf-template-engine, scripts

## Verification log tail
```text
$ git status --short
BLOCKED_BY_SUPERVISOR

$ npm --prefix frontend test
BLOCKED_BY_SUPERVISOR

$ git status --short
BLOCKED_BY_SUPERVISOR

```
