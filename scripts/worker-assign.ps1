param([Parameter(Mandatory=$true)][string]$Worker,[Parameter(Mandatory=$true)][string]$TaskId)
$ErrorActionPreference='Stop'; Set-Location (Split-Path $PSScriptRoot -Parent)
$lock='.agent-orchestration/state.lock'; $created=$false
try {
  try { New-Item -ItemType File -Path $lock -ErrorAction Stop | Out-Null; $created=$true } catch { throw 'LOCKED: another orchestrator is assigning a task' }
  $s=Get-Content -Raw .agent-orchestration/state.json|ConvertFrom-Json
  $prop=$s.tasks.PSObject.Properties[$TaskId]; if(!$prop){throw 'UNKNOWN_TASK'}; $t=$prop.Value
  if($t.status -notin @('READY','FAILED')){throw "NOT_CLAIMABLE:$($t.status)"}
  $t.status='RUNNING'; $t.worker=$Worker; $t.attempt=[int]$t.attempt+1; $t.last_result=$null
  $wp=$s.workers.PSObject.Properties[$Worker]; if(!$wp){$s.workers|Add-Member -NotePropertyName $Worker -NotePropertyValue ([pscustomobject]@{task=$TaskId})}else{$wp.Value.task=$TaskId}
  $s.events += [pscustomobject]@{event='CLAIMED';task=$TaskId;worker=$Worker;ts=[DateTimeOffset]::UtcNow.ToUnixTimeSeconds()}
  $s.revision=[int]$s.revision+1; $s.updated_at=[DateTime]::UtcNow.ToString('o'); $s|ConvertTo-Json -Depth 20|Set-Content .agent-orchestration/state.json
  Write-Output "CLAIMED $TaskId $Worker"
} finally { if($created){Remove-Item -LiteralPath $lock -Force -ErrorAction SilentlyContinue} }
