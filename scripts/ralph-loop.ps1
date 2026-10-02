param([int]$MaxTasks=0,[switch]$Once)
$ErrorActionPreference='Stop'; Set-Location (Split-Path $PSScriptRoot -Parent)
$root=(Get-Location).Path; $orch=Join-Path $root '.agent-orchestration'; $dag=Get-Content -Raw (Join-Path $orch 'dag.json')|ConvertFrom-Json; $config=Get-Content -Raw (Join-Path $orch 'config.json')|ConvertFrom-Json; $codexAdapter=Join-Path $orch 'providers\codex-cli.ps1'; $providerState=Join-Path $orch 'provider-state.json'
$leasePath=Join-Path $orch 'loop.lease'
try {$lease=[IO.File]::Open($leasePath,'OpenOrCreate','ReadWrite','None')} catch {throw 'RALPH_ALREADY_RUNNING'}
try {
$currentBranch=& git branch --show-current;if($currentBranch -ne [string]$config.integration_branch){throw "INTEGRATION_BRANCH_MISMATCH expected=$($config.integration_branch) actual=$currentBranch"}
$stopFile=Join-Path $orch 'STOP'; $doneCount=0
. (Join-Path $PSScriptRoot 'ralph-gates.ps1')
function Save-State($s){$s.revision=[int]$s.revision+1;$s.updated_at=[DateTime]::UtcNow.ToString('o');$tmp=(Join-Path $orch 'state.json.tmp');$s|ConvertTo-Json -Depth 30|Set-Content -LiteralPath $tmp -Encoding utf8;Move-Item -LiteralPath $tmp -Destination (Join-Path $orch 'state.json') -Force}
function Lock-State(){try{New-Item -ItemType File -Path (Join-Path $orch 'state.lock') -ErrorAction Stop|Out-Null;return $true}catch{return $false}}
function Unlock-State(){Remove-Item -LiteralPath (Join-Path $orch 'state.lock') -Force -ErrorAction SilentlyContinue}
function Add-Event($s,$event,$task,$worker,$detail){$s.events+=@([pscustomobject]@{ts=[DateTime]::UtcNow.ToString('o');event=$event;task=$task;worker=$worker;detail=$detail})}
function Get-ReadyTask($s){foreach($n in $dag.nodes){$t=$s.tasks.PSObject.Properties[$n.id].Value;if($t.status -in @('READY','FAILED','BLOCKED') -and [int]$t.attempt -lt ([int]$config.max_retries+1)){$depsOk=$true;foreach($dep in $n.dependencies){$dt=$s.tasks.PSObject.Properties[$dep].Value;if($dt.status -ne 'DONE' -or $dt.integration_status -ne 'INTEGRATED'){$depsOk=$false;break}};if($depsOk){return $n}}};return $null}
function Set-Task($id,$status,$worker,$branch,$worktree,$provider,$pidValue,$result){if(!(Lock-State)){throw 'STATE_LOCKED'};try{$s=Get-Content -Raw (Join-Path $orch 'state.json')|ConvertFrom-Json;$t=$s.tasks.PSObject.Properties[$id].Value;$t.status=$status;if($status -eq 'DONE'){$t.integration_status='INTEGRATED';$t|Add-Member -NotePropertyName integrated_commit -NotePropertyValue ([string](& git rev-parse HEAD)) -Force};if($worker){$t.worker=$worker};if($branch){$t.branch=$branch};if($worktree){$t.worktree=$worktree};if($provider){$t|Add-Member -NotePropertyName provider -NotePropertyValue $provider -Force};if($pidValue -ne $null){$t|Add-Member -NotePropertyName pid -NotePropertyValue $pidValue -Force};if($result -ne $null){$t.last_result=$result};if($status -in @('DONE','FAILED','BLOCKED')){$t|Add-Member -NotePropertyName finished_at -NotePropertyValue ([DateTime]::UtcNow.ToString('o')) -Force};if($worker -and $s.workers.PSObject.Properties[$worker]){$workerState=$s.workers.PSObject.Properties[$worker].Value;$workerState|Add-Member -NotePropertyName status -NotePropertyValue $status -Force;$workerState|Add-Member -NotePropertyName last_result -NotePropertyValue $result -Force};Add-Event $s $status $id $worker $result;Save-State $s}finally{Unlock-State}}
function Test-OwnedChanges($node,$worktree){$worktreeFiles=@(& git -C $worktree diff --name-only);$worktreeFiles+=@(& git -C $worktree diff --cached --name-only);$status=& git -C $worktree status --porcelain;if($LASTEXITCODE -ne 0){throw 'GIT_STATUS_FAILED'};foreach($line in $status){if($line.Length -ge 4){$worktreeFiles+=,$line.Substring(3).Trim('"')}};$branchFiles=@(& git -C $worktree diff --name-only "$($config.integration_branch)...HEAD");$all=@($worktreeFiles+$branchFiles|Select-Object -Unique);$prefixes=@($node.owned_paths|ForEach-Object {($_ -replace '\*\*.*$','').TrimEnd('/','\').Replace('/','\')});$bad=@();foreach($path0 in $all){$path=[string]$path0;if(!$path){continue};$path=$path.Replace('/','\');if(-not($prefixes|Where-Object {$path.StartsWith($_+'\',[StringComparison]::OrdinalIgnoreCase) -or $path -eq $_})){$bad+=,$path}};if($bad.Count){throw ('OWNERSHIP_VIOLATION: '+($bad -join ', '))};if(!$all.Count){throw 'NO_WORKER_CHANGES'};return ($all -join "`n")}
function Get-ActiveProvider(){if(Test-Path $providerState){$p=Get-Content -Raw $providerState|ConvertFrom-Json;if($p.provider -eq 'codex-cli'){return 'codex-cli'}};return 'deepseek-direct'}
function Set-CodexFallback($reason){[pscustomobject]@{provider='codex-cli';reason=$reason;changed_at=[DateTime]::UtcNow.ToString('o')}|ConvertTo-Json|Set-Content -LiteralPath $providerState -Encoding utf8;Write-Output 'PROVIDER_SWITCHED deepseek-direct -> codex-cli (quota exhausted)'}
while($true){
  if(Test-Path $stopFile){Write-Output 'RALPH_STOP_REQUESTED';break}
  if($MaxTasks -gt 0 -and $doneCount -ge $MaxTasks){break}
  $state=Get-Content -Raw (Join-Path $orch 'state.json')|ConvertFrom-Json
  foreach($active in @($state.tasks.PSObject.Properties|Where-Object {$_.Value.status -in @('ASSIGNED','RUNNING','VALIDATING')})){$pidValue=$active.Value.pid;if(!$pidValue -or !(Get-Process -Id ([int]$pidValue) -ErrorAction SilentlyContinue)){$active.Value.status='FAILED';$active.Value.last_result='ORPHAN_RECOVERY: previous Ralph process is no longer running; retry will include this failure context.';Add-Event $state 'ORPHAN_RECOVERED' $active.Name $active.Value.worker $active.Value.last_result}}
  Save-State $state
  $node=Get-ReadyTask $state
  if(!$node){$remaining=@($state.tasks.PSObject.Properties.Value|Where-Object status -ne 'DONE').Count;if($remaining -eq 0){Write-Output 'DAG_COMPLETE';break};Write-Output "NO_READY_TASKS remaining=$remaining";Start-Sleep -Seconds 30;continue}
  $taskFile=Join-Path $orch "tasks\$($node.id).md"
  if(!(Test-Path $taskFile)){
    $spec="Implement this assigned task from the operational DAG. The master contract closes product decisions. Choose technical details consistent with existing Camra code. Respect ownership and forbidden paths. Return the structured report; do not commit or merge.\n"+($node|ConvertTo-Json -Depth 20)
    Set-Content -LiteralPath $taskFile -Value $spec -Encoding utf8
  }
  $provider=Get-ActiveProvider;$worker=if($provider -eq 'codex-cli'){'codex-01'}else{'deepseek-01'};$taskId=$node.id;$branch="agent-$worker-$taskId";$worktreeRoot=[IO.Path]::GetFullPath((Join-Path $root ([string]$config.worktree_root)));$worktree=Join-Path $worktreeRoot "$worker-$taskId"
  if(!(Test-Path $worktree)){
    & (Join-Path $root 'scripts\worker-create.ps1') -Worker $worker -TaskId $taskId
    if($LASTEXITCODE -ne 0){throw 'WORKTREE_CREATE_FAILED'}
  }else{$actual=& git -C $worktree branch --show-current;if($actual -ne $branch){throw "WORKTREE_BRANCH_MISMATCH expected=$branch actual=$actual"}}
  $state=Get-Content -Raw (Join-Path $orch 'state.json')|ConvertFrom-Json
  $taskState=$state.tasks.PSObject.Properties[$taskId].Value;$retryFailure=if($taskState.status -eq 'FAILED'){$taskState.last_result}else{$null};$taskState.status='ASSIGNED';$taskState.worker=$worker;$taskState.branch=$branch;$taskState.worktree=$worktree;$taskState.attempt=[int]$taskState.attempt+1;$taskState|Add-Member -NotePropertyName provider -NotePropertyValue $provider -Force;$taskState|Add-Member -NotePropertyName start_time -NotePropertyValue ([DateTime]::UtcNow.ToString('o')) -Force
  $state.workers|Add-Member -NotePropertyName $worker -NotePropertyValue ([pscustomobject]@{task=$taskId;provider=$provider;pid=$PID;branch=$branch;worktree=$worktree;status='ASSIGNED';start_time=[DateTime]::UtcNow.ToString('o');last_result=$null}) -Force;Add-Event $state 'ASSIGNED' $taskId $worker $branch;Save-State $state
  Set-Task $taskId 'RUNNING' $worker $branch $worktree $provider $PID $null
  $taskFile=Join-Path $orch "tasks\$taskId.md";if(!(Test-Path $taskFile)){throw "TASK_SPEC_MISSING: $taskFile"}
  $runId=[Guid]::NewGuid().ToString('N'); $logDir=Join-Path $orch "logs\$taskId-attempt-$($taskState.attempt)-$runId";New-Item -ItemType Directory -Force $logDir|Out-Null;$resultFile=Join-Path $orch "results\$taskId-attempt-$($taskState.attempt)-$runId.json";New-Item -ItemType Directory -Force (Split-Path $resultFile -Parent)|Out-Null
  $effectiveTaskFile=Join-Path $logDir 'task-prompt.md';$effectiveTask=(Get-Content -Raw (Join-Path $orch 'MASTER_CONTRACT.md'))+"`n`nASSIGNED TASK:`n"+(Get-Content -Raw $taskFile);if($retryFailure){$effectiveTask+="`n`nRETRY CONTEXT FROM LAST FAILURE:`n$retryFailure`nDiagnose and correct the cause; do not repeat an unchanged failed action."};Set-Content -LiteralPath $effectiveTaskFile -Value $effectiveTask -Encoding utf8
  try{
    $agentLog=Join-Path $logDir 'agent.log'
    if($provider -eq 'codex-cli'){
      $contract=Get-Content -Raw (Join-Path $orch 'worker-contract.md');$owned=($node.owned_paths -join ', ')
      $prompt=@"
You are Ralph's coding worker for task $taskId. Camra is the product base. Work only in this worktree. Owned paths: $owned. Read the task and worker contract below. Do not modify outside ownership. Run relevant task validations, do not commit, and return all required structured report fields. Never reveal secrets.

WORKER CONTRACT:
$contract

TASK:
$(Get-Content -Raw $effectiveTaskFile)
"@.Trim()
      Set-Content -LiteralPath $effectiveTaskFile -Value $prompt -Encoding utf8
      $adapterResult=(& $codexAdapter -TaskFile $effectiveTaskFile -Worktree $worktree -LogDir $logDir)|ConvertFrom-Json
      if($adapterResult.exit_code -ne 0){throw "CODEX_EXIT_CODE:$($adapterResult.exit_code); see $($adapterResult.stderr)"}
      $report=Get-Content -Raw $adapterResult.last_message
      if($report -notmatch '(?im)READY_FOR_INTEGRATION:\s*(YES|TRUE)\b'){throw "WORKER_NOT_READY: $report"}
      if($LASTEXITCODE -and $LASTEXITCODE -ne 0){throw "CODEX_EXIT_CODE:$LASTEXITCODE"}
    }else{
      & (Join-Path $root '.agent-orchestration\providers\deepseek-agent.ps1') -TaskFile $effectiveTaskFile -Worktree $worktree -TaskId $taskId -MaxTurns ([int]$config.ralph.max_turns_per_task) *> $agentLog
      if($LASTEXITCODE -and $LASTEXITCODE -ne 0){throw "AGENT_EXIT_CODE:$LASTEXITCODE"}
    }
    if($LASTEXITCODE -and $LASTEXITCODE -ne 0){throw "AGENT_EXIT_CODE:$LASTEXITCODE"}
    Set-Task $taskId 'VALIDATING' $worker $branch $worktree $provider $PID 'Agent returned; running independent gates.'
    $diff=Test-OwnedChanges $node $worktree
    & git -C $worktree diff --check; if($LASTEXITCODE -ne 0){throw 'DIFF_CHECK_FAILED'}
    Assert-RalphMigrations -Checkout $root -Candidate $worktree
    $gateIndex=0
    foreach($validation in $node.validation_commands){
      Invoke-RalphValidation -Command $validation -Checkout $worktree -LogPath (Join-Path $logDir "worker-gate-$gateIndex.log")
      $gateIndex++
    }
    $ownedSpecs=@($node.owned_paths|ForEach-Object {($_ -replace '\*\*.*$','').TrimEnd('/','\')})
    & git -C $worktree add -- $ownedSpecs
    if($LASTEXITCODE -ne 0){throw 'GIT_STAGE_OWNED_PATHS_FAILED'}
    $stagedFiles=@(& git -C $worktree diff --cached --name-only)
    if($LASTEXITCODE -ne 0){throw 'GIT_STAGED_DIFF_FAILED'}
    if($stagedFiles.Count -gt 0){
      & git -C $worktree commit -m "feat($taskId): implement assigned Fixi capability"
      if($LASTEXITCODE -ne 0){throw 'WORKER_COMMIT_FAILED'}
    }
    $workerCommit=[string](& git -C $worktree rev-parse HEAD)
    if($LASTEXITCODE -ne 0 -or !$workerCommit){throw 'WORKER_COMMIT_MISSING'}
    $result=[pscustomobject]@{task=$taskId;worker=$worker;provider=$provider;branch=$branch;worktree=$worktree;status='PASSED';worker_commit=$workerCommit;run_id=$runId;validation_logs=$logDir;files=$diff;completed_at=[DateTime]::UtcNow.ToString('o')};$result|ConvertTo-Json -Depth 10|Set-Content $resultFile
    Set-Task $taskId 'PASSED' $worker $branch $worktree $provider $PID $resultFile
    # Preserve unrelated tracked user changes while integrating the candidate branch.
    if(Test-Path (Join-Path $root '.git/MERGE_HEAD')){throw 'INTEGRATION_ALREADY_IN_PROGRESS'}
    $mergeStarted=$false
    try {
      & git merge --autostash --no-ff --no-commit $branch
      $mergeStarted=Test-Path (Join-Path $root '.git/MERGE_HEAD')
      if($LASTEXITCODE -ne 0){throw 'INTEGRATION_MERGE_CONFLICT'}
      Assert-RalphMigrations -Checkout $root
      Invoke-RalphValidation -Command 'git diff --check' -Checkout $root -LogPath (Join-Path $logDir 'integration-diff.log')
      Invoke-RalphValidation -Command 'scripts/integration-validate.ps1' -Checkout $root -LogPath (Join-Path $logDir 'integration-full.log')
      if($mergeStarted){& git commit --no-edit;if($LASTEXITCODE -ne 0){throw 'INTEGRATION_COMMIT_FAILED'}}
    } catch {
      if($mergeStarted){& git merge --abort;if($LASTEXITCODE -ne 0){throw 'INTEGRATION_ABORT_FAILED: manual recovery required'}}
      throw
    }
    Set-Task $taskId 'DONE' $worker $branch $worktree $provider $PID $resultFile;$doneCount++
    $checkpoint=Join-Path $orch "results\checkpoint-$taskId.json";$nextState=Get-Content -Raw (Join-Path $orch 'state.json')|ConvertFrom-Json;[pscustomobject]@{created_at=[DateTime]::UtcNow.ToString('o');completed_task=$taskId;branch=$branch;quality_gates='passed';next_ready=@($dag.nodes|Where-Object {$_.dependencies -contains $taskId}|Select-Object -ExpandProperty id)}|ConvertTo-Json -Depth 8|Set-Content $checkpoint
  }catch{$failure=$_.Exception.Message;if($provider -eq 'deepseek-direct' -and $failure -match 'DEEPSEEK_QUOTA_EXHAUSTED') {Set-CodexFallback $failure;Set-Task $taskId 'FAILED' $worker $branch $worktree $provider $PID $failure;continue};Set-Task $taskId 'FAILED' $worker $branch $worktree $provider $PID $failure;Write-Warning "TASK_FAILED $taskId $failure";if($taskState.attempt -ge ([int]$config.max_retries+1)){Set-Task $taskId 'BLOCKED' $worker $branch $worktree $provider $PID $failure;break}}
  if($Once){break}
}
} finally {$lease.Dispose()}
