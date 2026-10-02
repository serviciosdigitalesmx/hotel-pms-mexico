# Shared by the scheduler and manual integration. Unsupported commands fail closed.
function Invoke-RalphValidation {
    param([string]$Command, [string]$Checkout, [string]$LogPath)
    $ErrorActionPreference = 'Stop'
    Push-Location $Checkout
    try {
        if ($Command -match '^\./gradlew\s+(.+)$') {
            & (Join-Path $Checkout 'gradlew.bat') --no-daemon --max-workers=2 @($Matches[1].Split(' ', [StringSplitOptions]::RemoveEmptyEntries)) 2>&1 | Tee-Object -FilePath $LogPath
        } elseif ($Command -match '^npm\s+--prefix\s+frontend\s+(.+)$') {
            & npm --prefix (Join-Path $Checkout 'frontend') @($Matches[1].Split(' ', [StringSplitOptions]::RemoveEmptyEntries)) 2>&1 | Tee-Object -FilePath $LogPath
        } elseif ($Command -match '^scripts/[a-zA-Z0-9_-]+\.ps1$') {
            & (Join-Path $PSHOME 'pwsh.exe') -NoProfile -File (Join-Path $Checkout $Command) 2>&1 | Tee-Object -FilePath $LogPath
        } elseif ($Command -eq 'git diff --check') {
            & git diff --check 2>&1 | Tee-Object -FilePath $LogPath
        } else {
            throw "UNSUPPORTED_VALIDATION: $Command"
        }
        if ($LASTEXITCODE -ne 0) { throw "GATE_FAILED:$Command exit=$LASTEXITCODE log=$LogPath" }
    } finally { Pop-Location }
}

function Assert-RalphMigrations {
    param([string]$Checkout, [string]$Candidate)
    $arguments = @((Join-Path $PSScriptRoot 'ralph_safety.py'), '--root', $Checkout)
    if ($Candidate) { $arguments += @('--overlay', $Candidate) }
    & python @arguments
    if ($LASTEXITCODE -ne 0) { throw 'MIGRATION_GATE_FAILED' }
}
