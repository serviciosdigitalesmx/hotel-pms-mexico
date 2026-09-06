# Native consolidation: PR disposition

Snapshot: 2026-09-06. This document records the integration status and known
CI limitations.

- PR #25 is merged. Native PRs #17, #18, #20, #21, #22, #23 and #24 are merged.
- PR #26 is merged as `4b4dab3`. JVM integration-image packaging excludes
  processAot for each packaged service; dedicated Native builds still execute
  their own AOT.
- Config Server uses application port 8888 and management port 8090.
- PR #19 was closed because it is an alternative Config implementation,
  superseded by #20. Its
  runtime security matchers are equivalent to the integrated variant. It moves
  refresh disabling into application-native.yml and replaces the build/runtime
  pipeline; these alternatives are not additional validated functionality.
  Retain the implementation validated by #20 and the integrated stack. The
  original branch and PR preserve the alternative code and its pilot note.
- Dependabot PRs #1–16 remain outside this consolidation, as explicitly agreed
  in the implementation plan.

Known limitation: the final PR check run had a general CI failure from existing
Frontdesk PMD/Checkstyle violations, while Frontend and API Gateway passed and
the prior integrated Native stack evidence passed. Native jobs for the final
PR run were cancelled after becoming stale on GitHub runners. Dependabot PRs
remain separate maintenance work.
