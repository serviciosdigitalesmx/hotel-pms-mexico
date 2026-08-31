# AI-0100 Verification Result

- Result: FAIL
- Source mutation: none detected
- Commands:
  - `npm --prefix frontend test -- --reporter=verbose`

## Local build manifest
Build system: Gradle root
Wrapper: ./gradlew
Docker Compose: present
Top-level directories: .add, .argos, .backups, .github, api-gateway, auth-service, billing-service, common-web-lib, config, config-service, docker, docs, fb-service, frontdesk-service, frontend, gradle, guest-service, internal-auth-lib, notification-service, pdf-template-engine, scripts

## Verification log tail
```text
$ npm --prefix frontend test -- --reporter=verbose
BLOCKED_BY_SUPERVISOR

```
