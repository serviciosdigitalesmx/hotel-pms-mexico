[CmdletBinding()]
param([switch]$PreflightOnly)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
. (Join-Path $PSScriptRoot 'ralph-gates.ps1')
$runId = [Guid]::NewGuid().ToString('N')
$evidence = Join-Path $root "build/integration/$runId"
New-Item -ItemType Directory -Path $evidence -Force | Out-Null
$report = [ordered]@{
    run_id=$runId; started_at=[DateTime]::UtcNow.ToString('o'); status='RUNNING';
    head=([string](& git rev-parse HEAD)); mode='FULL_JVM';
    production_ready=$false; gates=@(); finished_at=$null
}
if ($PreflightOnly) { $report.mode='PREFLIGHT_ONLY' }

function Gate([string]$Name, [scriptblock]$Action) {
    try {
        & $Action
        $report.gates += @([pscustomobject]@{name=$Name;status='PASSED'})
    } catch {
        $report.gates += @([pscustomobject]@{name=$Name;status='FAILED';error=$_.Exception.Message})
        throw
    }
}

try {
    Gate 'toolchain' {
        foreach ($command in @('git','python','docker')) {
            if (!(Get-Command $command -ErrorAction SilentlyContinue)) { throw "MISSING_COMMAND:$command" }
        }
        if (!$env:JAVA_HOME -or !(Test-Path "$env:JAVA_HOME/bin/java.exe")) { throw 'JAVA_HOME_MISSING' }
        & docker info --format '{{.ServerVersion}}'
        if ($LASTEXITCODE -ne 0) { throw 'DOCKER_ENGINE_UNAVAILABLE' }
    }
    Gate 'migration-versions' { Assert-RalphMigrations -Checkout $root }
    Gate 'diff' { Invoke-RalphValidation -Command 'git diff --check' -Checkout $root -LogPath (Join-Path $evidence 'diff.log') }
    if (!$PreflightOnly) {
        # Matches the existing JVM CI. Native/AOT remains a separate deployment gate.
        Gate 'backend-build-quality-tests' {
            Invoke-RalphValidation -Command './gradlew build -x processAot -x processAotTest' -Checkout $root -LogPath (Join-Path $evidence 'backend.log')
        }
        Gate 'frontend-install-lint-tests-build' {
            # CI uses Node 24. The container also works when the host has Node but no npm.
            & docker run --rm --mount "type=bind,source=$root/frontend,target=/app" -w /app node:24-bookworm-slim sh -c 'npm ci && npm run lint && npm run test && npm run build' 2>&1 | Tee-Object -FilePath (Join-Path $evidence 'frontend.log')
            if ($LASTEXITCODE -ne 0) { throw "FRONTEND_GATE_FAILED:exit=$LASTEXITCODE" }
        }
        Gate 'compose-configuration' {
            & docker compose config --quiet 2>&1 | Tee-Object -FilePath (Join-Path $evidence 'compose.log')
            if ($LASTEXITCODE -ne 0) { throw "COMPOSE_GATE_FAILED:exit=$LASTEXITCODE" }
        }
        $report.status = 'PASSED'
    } else { $report.status = 'PREFLIGHT_ONLY' }
} catch {
    $report.status = 'FAILED'
    Write-Warning $_.Exception.Message
} finally {
    $report.finished_at = [DateTime]::UtcNow.ToString('o')
    $report | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $evidence 'report.json') -Encoding utf8
    Write-Output "VALIDATION_REPORT:$evidence/report.json"
}
if ($report.status -eq 'FAILED') { exit 1 }
