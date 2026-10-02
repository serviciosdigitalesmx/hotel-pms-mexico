param([Parameter(Mandatory=$true)][string]$TaskFile,[Parameter(Mandatory=$true)][string]$Worktree,[string]$LogDir='.agent-orchestration/logs',[string]$Timeout='1h')
$ErrorActionPreference='Stop'; $agy=Join-Path $env:LOCALAPPDATA 'agy\bin\agy.exe'; if(-not(Test-Path $agy)){throw 'ANTIGRAVITY_BLOCKER: agy.exe not found'}
$resolved=(Resolve-Path $Worktree).Path; New-Item -ItemType Directory -Force $LogDir|Out-Null; $id=[IO.Path]::GetFileNameWithoutExtension($TaskFile); $out=Join-Path $LogDir "$id.agy.jsonl"; $err=Join-Path $LogDir "$id.agy.stderr.log"; $prompt=Get-Content -Raw $TaskFile
$psi=[Diagnostics.ProcessStartInfo]::new(); $psi.FileName=$agy; $psi.WorkingDirectory=$resolved; $psi.UseShellExecute=$false; $psi.RedirectStandardOutput=$true; $psi.RedirectStandardError=$true
# Do not inherit a stale loopback proxy from the orchestrator/parent process.
foreach($proxyVar in @('HTTP_PROXY','HTTPS_PROXY','ALL_PROXY','http_proxy','https_proxy','all_proxy')){[void]$psi.Environment.Remove($proxyVar)}
$psi.Environment['NO_PROXY']='daily-cloudcode-pa.googleapis.com,play.googleapis.com,googleapis.com,.googleapis.com'
foreach($a in @('--output-format','json','--print-timeout',$Timeout,'--dangerously-skip-permissions',"--print=$prompt")){[void]$psi.ArgumentList.Add($a)}
$p=[Diagnostics.Process]::new(); $p.StartInfo=$psi; [void]$p.Start(); $pidValue=$p.Id; $o=$p.StandardOutput.ReadToEndAsync(); $e=$p.StandardError.ReadToEndAsync(); $sw=[Diagnostics.Stopwatch]::StartNew(); while(-not $p.WaitForExit(500)){if($sw.Elapsed.TotalSeconds -gt 3600){$p.Kill();throw "TIMEOUT pid=$pidValue task=$id"}}
$o.Result|Set-Content $out; $e.Result|Set-Content $err; [pscustomobject]@{provider='antigravity-cli';task=$id;pid=$pidValue;exit_code=$p.ExitCode;stdout=$out;stderr=$err;duration_seconds=[int]$sw.Elapsed.TotalSeconds}|ConvertTo-Json
