$ErrorActionPreference='Stop'
$orch=Join-Path (Split-Path $PSScriptRoot -Parent) '.agent-orchestration'
$lock=Join-Path $orch 'state.lock'
New-Item -ItemType File -Path $lock -ErrorAction Stop|Out-Null
try {
  $path=Join-Path $orch 'state.json'
  $s=Get-Content -Raw $path|ConvertFrom-Json
  $t=$s.tasks.'workflow-engine'
  if($t.status -eq 'BLOCKED' -and $t.last_result -match 'Gradle starts a local daemon'){
    $t.status='FAILED'; $t.attempt=0; $t.pid=$null
    $t.last_result='RECOVERED: Orchestrator fixed missing jakarta.persistence.LockModeType import; independent frontdesk-service:test completed BUILD SUCCESSFUL in 4m56s, including coverage verification. No Gradle hang remains demonstrated. Before integration finish task notes: remove role-only permission fallback, verify branch/tenant enforcement and concurrency, and resolve unintegrated workflow V24 collision with main customer-device V24. Preserve changes and rerun gates after fixes. Do not declare DONE based on the prior test alone.'
    $s.events+=@([pscustomobject]@{ts=[DateTime]::UtcNow.ToString('o');event='VALIDATION_RECOVERED';task='workflow-engine';worker='codex-01';detail=$t.last_result})
    $s.revision++;$s.updated_at=[DateTime]::UtcNow.ToString('o')
    $tmp="$path.recovery.tmp"; $s|ConvertTo-Json -Depth 30|Set-Content -LiteralPath $tmp -Encoding utf8
    Move-Item -LiteralPath $tmp -Destination $path -Force
  }
} finally {Remove-Item -LiteralPath $lock}
