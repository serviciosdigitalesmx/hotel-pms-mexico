param([Parameter(Mandatory=$true)][string]$TaskFile,[Parameter(Mandatory=$true)][string]$Worktree,[string]$LogDir='.agent-orchestration/logs',[int]$TimeoutSeconds=3600)
$ErrorActionPreference='Stop'; $resolved=(Resolve-Path $Worktree).Path; New-Item -ItemType Directory -Force $LogDir | Out-Null
$id=[IO.Path]::GetFileNameWithoutExtension($TaskFile); $stdout=Join-Path $LogDir "$id.stdout.log"; $stderr=Join-Path $LogDir "$id.stderr.log"; $last=Join-Path $LogDir "$id.last-message.txt"
$psi=[Diagnostics.ProcessStartInfo]::new(); $psi.FileName='codex'; $psi.WorkingDirectory=$resolved; $psi.UseShellExecute=$false; $psi.RedirectStandardInput=$true; $psi.RedirectStandardOutput=$true; $psi.RedirectStandardError=$true
$psi.StandardInputEncoding=[Text.UTF8Encoding]::new($false)
$psi.StandardOutputEncoding=[Text.UTF8Encoding]::new($false)
$psi.StandardErrorEncoding=[Text.UTF8Encoding]::new($false)
$gradleHome=Join-Path $resolved '.gradle\user-home'
New-Item -ItemType Directory -Force $gradleHome|Out-Null
$psi.Environment['GRADLE_USER_HOME']=$gradleHome
foreach($a in @('--no-daemon','exec','--ephemeral','--json','-C',$resolved,'-o',$last,'-')){[void]$psi.ArgumentList.Add($a)}
$p=[Diagnostics.Process]::new(); $p.StartInfo=$psi; [void]$p.Start(); $processId=$p.Id
$outTask=$p.StandardOutput.ReadToEndAsync(); $errTask=$p.StandardError.ReadToEndAsync(); $p.StandardInput.Write((Get-Content -Raw -LiteralPath $TaskFile)+"`n`nVALIDATION ENVIRONMENT: GRADLE_USER_HOME=$gradleHome. Use this worktree-local Gradle cache for wrapper, dependencies and tests; if shell environment filtering removes it, set it explicitly for the validation command. Do not use the inaccessible global Gradle cache."); $p.StandardInput.Close()
$sw=[Diagnostics.Stopwatch]::StartNew(); while(-not $p.WaitForExit(500)){if($sw.Elapsed.TotalSeconds -ge $TimeoutSeconds){$p.Kill(); throw "TIMEOUT pid=$processId task=$id"}}
$outTask.Result|Set-Content $stdout; $errTask.Result|Set-Content $stderr
[pscustomobject]@{provider='codex-cli';task=$id;pid=$processId;exit_code=$p.ExitCode;stdout=$stdout;stderr=$stderr;last_message=$last;duration_seconds=[int]$sw.Elapsed.TotalSeconds}|ConvertTo-Json
