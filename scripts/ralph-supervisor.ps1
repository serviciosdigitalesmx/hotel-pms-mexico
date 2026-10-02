param([int]$PollSeconds=30)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
Set-Location $root
$orch=Join-Path $root '.agent-orchestration'
try {$lease=[IO.File]::Open((Join-Path $orch 'supervisor.lease'),'OpenOrCreate','ReadWrite','None')} catch {throw 'SUPERVISOR_ALREADY_RUNNING'}
try {
  $env:JAVA_HOME='C:\Users\Jesus Villa\Documents\Codex\2026-09-28\s-carnal-por-favor-g-ey\work\jdk-install\ms\jdk-21.0.12.1+1'
  if(!(Test-Path "$env:JAVA_HOME\bin\java.exe")){throw 'JDK_MISSING'}
  $env:Path="$env:JAVA_HOME\bin;$env:Path"
  $logDir=Join-Path $orch 'logs\supervisor'
  New-Item -ItemType Directory -Force $logDir|Out-Null
  while(!(Test-Path (Join-Path $orch 'STOP'))){
    $stamp=Get-Date -Format 'yyyyMMdd-HHmmssfff'
    $p=Start-Process -FilePath (Join-Path $PSHOME 'pwsh.exe') -ArgumentList @('-NoProfile','-File',('"'+(Join-Path $root 'scripts\ralph-loop.ps1')+'"')) -WorkingDirectory $root -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logDir "$stamp.out.log") -RedirectStandardError (Join-Path $logDir "$stamp.err.log")
    $p.WaitForExit()
    $state=Get-Content -Raw (Join-Path $orch 'state.json')|ConvertFrom-Json
    if(@($state.tasks.PSObject.Properties.Value|Where-Object status -ne 'DONE').Count -eq 0){break}
    Start-Sleep -Seconds $PollSeconds
  }
} finally {$lease.Dispose()}
