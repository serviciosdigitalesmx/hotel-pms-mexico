$ErrorActionPreference='Stop'
$orch=Join-Path (Split-Path $PSScriptRoot -Parent) '.agent-orchestration'
$lock=Join-Path $orch 'state.lock'
New-Item -ItemType File -Path $lock -ErrorAction Stop|Out-Null
try {
  $path=Join-Path $orch 'state.json'
  $s=Get-Content -Raw $path|ConvertFrom-Json
  $t=$s.tasks.'workflow-engine'
  if($t.status -eq 'BLOCKED' -and $t.last_result -like 'CODEX_EXIT_CODE:1*'){
    $errorLog=Join-Path $orch "logs\workflow-engine-attempt-$($t.attempt)\task-prompt.stderr.log"
    if((Get-Content -Raw $errorLog) -notmatch 'input is not valid UTF-8'){throw 'Unexpected failure; preserve retry state'}
    $t.status='READY'; $t.attempt=0; $t.pid=$null
    $t.last_result='REPAIRED_UTF8_STDIN: adapter now sends UTF-8; retry after verified cause correction'
    $s.events+=@([pscustomobject]@{ts=[DateTime]::UtcNow.ToString('o');event='REPAIRED_UTF8';task='workflow-engine';worker='codex-01';detail=$t.last_result})
    $s.revision++;$s.updated_at=[DateTime]::UtcNow.ToString('o')
    $tmp="$path.repair.tmp"; $s|ConvertTo-Json -Depth 30|Set-Content -LiteralPath $tmp -Encoding utf8
    Move-Item -LiteralPath $tmp -Destination $path -Force
  }
} finally {Remove-Item -LiteralPath $lock}
