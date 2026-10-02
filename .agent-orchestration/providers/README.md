# Provider adapters

`deepseek-direct` is the head and coding agent. It uses the official OpenAI-compatible Chat Completions endpoint at `https://api.deepseek.com/chat/completions`, with model configured in `.agent-orchestration/config.json` and key from `DEEPSEEK_API_KEY`. The API was verified with an HTTP 200 response. The key is never stored here.

`codex-cli` is verified locally as version `0.158.0-alpha.2.1`. Its headless interface exists, but a runtime probe failed to initialize its local app-server/state database, so it is secondary and disabled for this run.

The first runtime probe launched the process but failed before inference because the local Codex SQLite/app-server state was not writable/accessible. This is an environment blocker, not a missing CLI interface.

Antigravity CLI was tested and can run headless, but it is disabled because its Gemini quota is exhausted.

Set `AGENT_PROVIDER_COMMAND` only to a reviewed executable that accepts a task file and worktree. Otherwise use `assign`, create worktrees, and have the external master/agent invoke the worker manually.
