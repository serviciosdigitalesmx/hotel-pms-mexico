param([switch]$Force)
$ErrorActionPreference='Stop'; Set-Location (Split-Path $PSScriptRoot -Parent)
$dag=Get-Content -Raw .agent-orchestration/dag.json | ConvertFrom-Json
$state=Get-Content -Raw .agent-orchestration/state.json | ConvertFrom-Json
$state.tasks=@{}; foreach($n in $dag.nodes){$initial=if($n.dependencies.Count -eq 0){'READY'}else{'BLOCKED'};$state.tasks[$n.id]=[pscustomobject]@{status=$initial;worker=$null;branch=$null;worktree=$null;attempt=0;last_result=$null;integration_status='PENDING';dependencies=$n.dependencies}}
$state.updated_at=(Get-Date).ToUniversalTime().ToString('o'); $state | ConvertTo-Json -Depth 20 | Set-Content .agent-orchestration/state.json
Write-Output 'BOOTSTRAPPED: persistent DAG state initialized.'
