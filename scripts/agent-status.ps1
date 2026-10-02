$ErrorActionPreference='Stop'; Set-Location (Split-Path $PSScriptRoot -Parent); $s=Get-Content -Raw .agent-orchestration/state.json|ConvertFrom-Json
$s.tasks.psobject.Properties.Value|Group-Object status|Sort-Object Name|Format-Table Name,Count -AutoSize
$s.tasks.psobject.Properties|ForEach-Object { $v=$_.Value; if($v.worker){"{0}: {1} worker={2} branch={3}" -f $_.Name,$v.status,$v.worker,$v.branch} }
