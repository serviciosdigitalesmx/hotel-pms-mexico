# Native consolidation: PR disposition

Snapshot: 2026-09-06. This document records integration status, not a claim that
the current consolidated stack has passed runtime validation.

- PR #25 is merged. Native PRs #17, #18, #20, #21, #22, #23 and #24 are merged.
- PR #26 repairs the combined Gradle build. Its current checks must finish
  before merge. JVM integration-image packaging excludes processAot for each
  packaged service; dedicated Native builds still execute their own AOT.
- Config Server uses application port 8888 and management port 8090.
- PR #19 is an alternative Config implementation, superseded by #20. Its
  runtime security matchers are equivalent to the integrated variant. It moves
  refresh disabling into application-native.yml and replaces the build/runtime
  pipeline; these alternatives are not additional validated functionality.
  Retain the implementation validated by #20 and the integrated stack. The
  original branch and PR preserve the alternative code and its pilot note.
- Dependabot PRs #1–16 remain outside this consolidation, as explicitly agreed
  in the implementation plan.

Outstanding: validate PR #26, reconcile #19, verify the integrated main stack,
verify the recoverable Desktop backup, synchronize the Desktop checkout and
launchers, and record final hashes and artifacts. Earlier successful image
evidence alone does not prove the consolidated source commit is validated.
