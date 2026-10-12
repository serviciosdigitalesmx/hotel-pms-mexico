$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent; $orch=Join-Path $root '.agent-orchestration'; $path=Join-Path $orch 'state.json'; $lock=Join-Path $orch 'state.lock'
while(Test-Path $lock){Start-Sleep -Milliseconds 300}
New-Item $lock -ItemType File|Out-Null
try{$s=Get-Content -Raw $path|ConvertFrom-Json;$t=$s.tasks.'workflow-engine';$t.status='READY';$t.attempt=0;$t.pid=$null;$t.last_result='REOPENED_INTEGRATION_AUTOSTASH: branch clean; unrelated main .gitignore no longer blocks merge';$s.revision++;$s.updated_at=[DateTime]::UtcNow.ToString('o');$tmp="$path.retry.tmp";$s|ConvertTo-Json -Depth 30|Set-Content $tmp -Encoding utf8;Move-Item $tmp $path -Force}finally{Remove-Item $lock -Force -ErrorAction SilentlyContinue}
