$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$orch=Join-Path $root '.agent-orchestration'
Remove-Item -LiteralPath (Join-Path $orch 'STOP') -Force -ErrorAction SilentlyContinue
$lock=Join-Path $orch 'state.lock'
New-Item -ItemType File -Path $lock -ErrorAction Stop|Out-Null
try {
  $path=Join-Path $orch 'state.json'; $s=Get-Content -Raw $path|ConvertFrom-Json
  foreach($id in @('workflow-engine','platform-gates')){
    $t=$s.tasks.PSObject.Properties[$id].Value
    if($t.status -in @('FAILED','BLOCKED')){$t.status='READY';$t.attempt=0;$t.pid=$null;$t.last_result="REOPENED_AFTER_VALIDATION_REPAIR: $($t.last_result)"}
  }
  $s.revision++;$s.updated_at=[DateTime]::UtcNow.ToString('o')
  $tmp="$path.resume.tmp";$s|ConvertTo-Json -Depth 30|Set-Content -LiteralPath $tmp -Encoding utf8;Move-Item -LiteralPath $tmp -Destination $path -Force
} finally {Remove-Item -LiteralPath $lock -Force -ErrorAction SilentlyContinue}
