param([switch]$Apply)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$orch = Join-Path $root '.agent-orchestration'
# The scheduler must be stopped; never race its read/modify/write cycle.
try { $lease = [IO.File]::Open((Join-Path $orch 'loop.lease'), 'OpenOrCreate', 'ReadWrite', 'None') }
catch { throw 'RALPH_STILL_RUNNING: wait for the active task to finish before reconciliation' }
try {
    $state = Get-Content (Join-Path $orch 'state.json') -Raw | ConvertFrom-Json
    $changed = $false
    foreach ($entry in $state.tasks.PSObject.Properties) {
        $task = $entry.Value
        if ($task.status -ne 'DONE' -or $task.integration_status -eq 'INTEGRATED') { continue }
        if (!$task.branch) { throw "MISSING_BRANCH:$($entry.Name)" }
        & git -C $root merge-base --is-ancestor $task.branch HEAD
        if ($LASTEXITCODE -ne 0) { throw "DONE_BUT_NOT_MERGED:$($entry.Name)" }
        if (!(Test-Path -LiteralPath $task.last_result)) { throw "MISSING_VALIDATION_RECEIPT:$($entry.Name)" }
        $receipt = Get-Content -LiteralPath $task.last_result -Raw | ConvertFrom-Json
        if ($receipt.status -ne 'PASSED' -or $receipt.task -ne $entry.Name) { throw "INVALID_VALIDATION_RECEIPT:$($entry.Name)" }
        $task.integration_status = 'INTEGRATED'
        $task | Add-Member -NotePropertyName worker_commit -NotePropertyValue ([string](& git -C $root rev-parse $task.branch)) -Force
        $task | Add-Member -NotePropertyName integration_validation -NotePropertyValue 'LEGACY_MERGE_REQUIRES_COMBINED_VALIDATION' -Force
        $state.events += @([pscustomobject]@{ts=[DateTime]::UtcNow.ToString('o');event='INTEGRATION_RECONCILED';task=$entry.Name;worker=$task.worker;detail='Branch ancestry and worker receipt verified; combined/release validation remains outstanding.'})
        $changed = $true
        Write-Output "VERIFIED_MERGED $($entry.Name) $($task.worker_commit)"
    }
    if ($Apply -and $changed) {
        $state.revision = [int]$state.revision + 1
        $state.updated_at = [DateTime]::UtcNow.ToString('o')
        $temporary = Join-Path $orch ('state-reconcile-' + [Guid]::NewGuid().ToString('N') + '.tmp')
        $state | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $temporary -Encoding utf8
        Move-Item -LiteralPath $temporary -Destination (Join-Path $orch 'state.json') -Force
    }
    if (!$Apply) { Write-Output 'READ_ONLY: rerun with -Apply after reviewing the verified merges' }
} finally { $lease.Dispose() }
