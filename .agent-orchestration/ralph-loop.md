# Ralph loop implemented by the orchestrator

Ralph is a loop/protocol in this repository, not a runtime binary. The factory owns the loop and treats `.ralph-add/RALPH/BASE_PROMPT.md`, `.ralph-add/PROJECT/*`, the assigned task spec, and the worker contract as inputs.

State transitions are:

`READY -> ASSIGNED -> RUNNING -> VALIDATING -> PASSED -> INTEGRATED -> DONE`

Failure transitions are `VALIDATING -> FAILED -> RETURN_TO_WORKER`; retries are capped by `max_retries`, and deterministic failures are not retried without a changed result or explicit escalation. Each launch records provider, PID, branch, worktree, attempt, timestamps, stdout, stderr, exit code and validation result.
