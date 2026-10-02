param(
  [Parameter(Mandatory=$true)][string]$TaskFile,
  [Parameter(Mandatory=$true)][string]$Worktree,
  [Parameter(Mandatory=$true)][string]$TaskId,
  [int]$MaxTurns=80
)
$ErrorActionPreference='Stop'
$root=(Resolve-Path $Worktree).Path
$repoRoot=(Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$config=Get-Content -Raw (Join-Path $repoRoot '.agent-orchestration\config.json')|ConvertFrom-Json
$apiKey=[Environment]::GetEnvironmentVariable([string]$config.api_key_env,'Process')
if([string]::IsNullOrWhiteSpace($apiKey)){throw "Missing environment variable $($config.api_key_env). Set it for this process and resume."}
$dag=Get-Content -Raw (Join-Path $repoRoot '.agent-orchestration\dag.json')|ConvertFrom-Json
$task=$dag.nodes|Where-Object id -eq $TaskId|Select-Object -First 1
if(!$task){throw "Unknown task id: $TaskId"}
$owned=@($task.owned_paths|ForEach-Object {($_ -replace '\*\*.*$','').TrimEnd('/','\').Replace('/','\')})
$workerContract=Get-Content -Raw (Join-Path $repoRoot '.agent-orchestration\worker-contract.md')
$taskText=Get-Content -Raw $TaskFile
$system=@"
You are the autonomous coding worker controlled by the Ralph execution loop. Camra is the product base. Read the task, worker contract, and authoritative product decisions. Work only in this worktree. You can inspect repository files and use the provided functions. Changes must stay within these owned path prefixes: $($owned -join ', '). Never modify outside ownership; if a shared contract needs change, report CONTRACT_CHANGES_REQUESTED. Preserve security, tenant and branch isolation, auditability, transactions, and additive Flyway migration rules. Do not claim completion unless implementation and task validations pass. Finish with the exact structured worker report fields from the contract. The orchestrator handles branch commits after independent validation; do not commit yourself. Never reveal secrets or environment variables.

WORKER CONTRACT:
$workerContract

ASSIGNED TASK:
$taskText
"@
$messages=[System.Collections.Generic.List[object]]::new()
$messages.Add(@{role='system';content=$system})
$messages.Add(@{role='user';content="Inspect relevant implementation and decisions, implement task $TaskId, run appropriate allowed validations, fix failures, then report the result."})
$tools=@(
  @{type='function';function=@{name='list_files';description='List repository files under a relative directory. Read only.';parameters=@{type='object';properties=@{path=@{type='string'}};required=@('path')}}},
  @{type='function';function=@{name='search_text';description='Search repository text with ripgrep. Read only.';parameters=@{type='object';properties=@{query=@{type='string'};path=@{type='string'}};required=@('query','path')}}},
  @{type='function';function=@{name='read_file';description='Read a UTF-8 text file inside this repository.';parameters=@{type='object';properties=@{path=@{type='string'}};required=@('path')}}},
  @{type='function';function=@{name='write_file';description='Create or replace a file under task ownership only.';parameters=@{type='object';properties=@{path=@{type='string'};content=@{type='string'}};required=@('path','content')}}},
  @{type='function';function=@{name='replace_in_file';description='Replace one exact text occurrence in an owned file.';parameters=@{type='object';properties=@{path=@{type='string'};old_text=@{type='string'};new_text=@{type='string'}};required=@('path','old_text','new_text')}}},
  @{type='function';function=@{name='run_validation';description='Run a validation command listed in this task DAG or git diff --check, from this worktree.';parameters=@{type='object';properties=@{command=@{type='string'}};required=@('command')}}}
)
function Resolve-RepoPath([string]$relative,[switch]$ReadOnly){
  if([IO.Path]::IsPathRooted($relative)){throw 'Absolute paths are not accepted.'}
  $base=$root
  if($ReadOnly -and !(Test-Path (Join-Path $root $relative))){$base=$repoRoot}
  $candidate=[IO.Path]::GetFullPath((Join-Path $base $relative))
  if(!$candidate.StartsWith($root+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase) -and $candidate -ne $root -and (!$ReadOnly -or (!$candidate.StartsWith($repoRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase) -and $candidate -ne $repoRoot))){throw 'Path escapes permitted repository roots.'}
  return $candidate
}
function Assert-Owned([string]$relative){
  $normalized=$relative.Replace('/','\')
  if(-not ($owned|Where-Object {$normalized.StartsWith($_+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase) -or $normalized -eq $_})){throw "OWNERSHIP_VIOLATION: $relative"}
}
function Invoke-TaskTool([string]$name,$arg){
  switch($name){
    'list_files' { $path=Resolve-RepoPath $arg.path -ReadOnly; if(!(Test-Path $path)){throw 'Path not found'}; $text=& rg --files $path; return ($text|Select-Object -First 300) -join "`n" }
    'search_text' { $path=Resolve-RepoPath $arg.path -ReadOnly; if(!(Test-Path $path)){throw 'Path not found'}; $output=& rg -n --max-count 80 -- $arg.query $path 2>&1; return (($output|Select-Object -First 150)|Out-String) }
    'read_file' { $path=Resolve-RepoPath $arg.path -ReadOnly; if(!(Test-Path -LiteralPath $path -PathType Leaf)){throw 'File not found'}; return (Get-Content -Raw -LiteralPath $path) }
    'write_file' { Assert-Owned $arg.path; $path=Resolve-RepoPath $arg.path; $parent=Split-Path -Parent $path; if(!(Test-Path $parent)){New-Item -ItemType Directory -Force $parent|Out-Null}; [IO.File]::WriteAllText($path,[string]$arg.content,[Text.UTF8Encoding]::new($false)); return 'FILE_WRITTEN' }
    'replace_in_file' { Assert-Owned $arg.path; $path=Resolve-RepoPath $arg.path; $content=Get-Content -Raw -LiteralPath $path; $count=([regex]::Matches($content,[regex]::Escape([string]$arg.old_text))).Count; if($count -ne 1){throw "Expected one exact match, found $count"}; $updated=$content.Replace([string]$arg.old_text,[string]$arg.new_text); [IO.File]::WriteAllText($path,$updated,[Text.UTF8Encoding]::new($false)); return 'FILE_UPDATED' }
    'run_validation' { $cmd=[string]$arg.command; $allowed=@($task.validation_commands)+@('git diff --check'); if($cmd -notin $allowed){throw 'VALIDATION_NOT_ALLOWED: command is not listed in the task DAG'}; if($cmd -match '^\./gradlew\s+(.+)$'){$cmd='gradlew.bat '+$Matches[1]}elseif($cmd -match '^scripts/.+\.ps1$'){$cmd='powershell -NoProfile -ExecutionPolicy Bypass -File "'+(Join-Path $root $cmd)+'"'}; $psi=[Diagnostics.ProcessStartInfo]::new(); $psi.FileName='cmd.exe'; $psi.WorkingDirectory=$root; $psi.UseShellExecute=$false; $psi.RedirectStandardOutput=$true; $psi.RedirectStandardError=$true; [void]$psi.ArgumentList.Add('/d'); [void]$psi.ArgumentList.Add('/s'); [void]$psi.ArgumentList.Add('/c'); [void]$psi.ArgumentList.Add($cmd); $p=[Diagnostics.Process]::new(); $p.StartInfo=$psi; [void]$p.Start(); $stdout=$p.StandardOutput.ReadToEndAsync(); $stderr=$p.StandardError.ReadToEndAsync(); if(!$p.WaitForExit(1800000)){ $p.Kill(); throw 'VALIDATION_TIMEOUT' }; return (("EXIT_CODE=$($p.ExitCode)`nSTDOUT:`n$($stdout.Result)`nSTDERR:`n$($stderr.Result)")|Out-String) }
    default { throw "Unknown tool $name" }
  }
}
for($turn=1;$turn -le $MaxTurns;$turn++){
  $body=@{model=[string]$config.model;messages=@($messages.ToArray());tools=$tools;tool_choice='auto';stream=$false;max_tokens=8192}
  $json=$body|ConvertTo-Json -Depth 80 -Compress
  try{$response=Invoke-RestMethod -Method Post -Uri (([string]$config.api_base).TrimEnd('/')+'/chat/completions') -Headers @{Authorization="Bearer $apiKey"} -ContentType 'application/json' -Body $json -TimeoutSec 180}
  catch{
    $status=$null;$detail=$_.ErrorDetails.Message
    if($_.Exception.Response){try{$status=[int]$_.Exception.Response.StatusCode}catch{}}
    if($status -in @(402,429) -or ($detail -match '(?i)(insufficient.balance|quota|rate.limit|billing|余额不足|额度|配额)')){throw "DEEPSEEK_QUOTA_EXHAUSTED: HTTP $status $detail"}
    throw
  }
  $choice=$response.choices[0]; $message=$choice.message
  if(!$message.tool_calls -or $message.tool_calls.Count -eq 0){
    $final=[string]$message.content; Write-Output $final; if($choice.finish_reason -eq 'length'){throw 'MODEL_OUTPUT_TRUNCATED'}; return
  }
  $assistantMessage=@{role='assistant';content=$message.content;tool_calls=$message.tool_calls}; if($message.reasoning_content){$assistantMessage.reasoning_content=$message.reasoning_content}; $messages.Add($assistantMessage)
  foreach($call in $message.tool_calls){
    try{$arguments=$call.function.arguments|ConvertFrom-Json; $result=Invoke-TaskTool $call.function.name $arguments; $toolContent=[string]$result}
    catch{$toolContent="TOOL_ERROR: $($_.Exception.Message)"}
    $messages.Add(@{role='tool';tool_call_id=$call.id;content=$toolContent})
  }
}
throw "RALPH_MAX_TURNS_EXCEEDED: $MaxTurns"
