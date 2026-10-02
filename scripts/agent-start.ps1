param([int]$MaxTasks=0,[switch]$Once)
$ErrorActionPreference='Stop'; Set-Location (Split-Path $PSScriptRoot -Parent); if(-not(Test-Path .agent-orchestration/state.json)){& scripts/agent-bootstrap.ps1}
if(-not $env:DEEPSEEK_API_KEY){throw 'Set DEEPSEEK_API_KEY in this PowerShell process, then run agent-start.ps1.'}
Remove-Item .agent-orchestration\STOP -Force -ErrorAction SilentlyContinue
& scripts/ralph-loop.ps1 -MaxTasks $MaxTasks -Once:$Once
