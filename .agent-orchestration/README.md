# Camra -> Fixi agent factory

This is orchestration infrastructure only. Camra remains the product base; the authoritative Fixi documents are listed in `dag.json`.

`state.json` is the persistent source of truth. `orchestrator.py claim` takes an exclusive lock before changing a task from `READY`/`FAILED` to `RUNNING`, preventing double assignment. Worktrees are created by `scripts/worker-create.ps1`.

Ralph evidence found: `.ralph-add/RALPH/BASE_PROMPT.md`, `.ralph-add/PROJECT/*`, specs and reports. No executable, machine task format, hook, completion API, or worker API was found. The provider adapter therefore exposes an explicit `AUTOMATION_BLOCKER`; it does not simulate GUI/provider automation. `codex` is installed, but no non-interactive worker contract is assumed.

PowerShell quick start:

```powershell
.\scripts\agent-bootstrap.ps1
.\scripts\agent-status.ps1
.\scripts\agent-start.ps1
```

DeepSeek start without putting the key in PowerShell history:

```powershell
.\scripts\agent-start-deepseek.ps1
```

Then assign an eligible task:

```powershell
.\scripts\worker-create.ps1 -Worker worker-01 -TaskId foundation-contracts
.\scripts\worker-assign.ps1 -Worker worker-01 -TaskId foundation-contracts
.\scripts\worker-validate.ps1 -TaskId foundation-contracts
```

Integration is explicit: `.\scripts\integration-validate.ps1`. State survives terminal/IDE restarts; `agent-resume.ps1` prints it.
