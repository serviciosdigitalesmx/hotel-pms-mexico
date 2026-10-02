$ErrorActionPreference = 'Stop'
. (Join-Path (Split-Path $PSScriptRoot -Parent) 'ralph-gates.ps1')
$testDirectory = Join-Path ([IO.Path]::GetTempPath()) ('ralph-gate-test-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $testDirectory | Out-Null
$originalDirectory = (Get-Location).Path
try {
    function git { $global:LASTEXITCODE = 7; 'Simulated failed native check' }
    try {
        Invoke-RalphValidation -Command 'git diff --check' -Checkout $testDirectory -LogPath (Join-Path $testDirectory 'failure.log')
        throw 'Expected the failed native command to reject the gate'
    } catch { if ($_ -notmatch 'GATE_FAILED:git diff --check exit=7') { throw } }
    if ((Get-Location).Path -ne $originalDirectory) { throw 'Gate did not restore working directory' }
    if (!(Get-Content (Join-Path $testDirectory 'failure.log') -Raw).Contains('Simulated failed')) { throw 'Evidence log missing' }
    function git { $global:LASTEXITCODE = 0; 'Simulated passing native check' }
    Invoke-RalphValidation -Command 'git diff --check' -Checkout $testDirectory -LogPath (Join-Path $testDirectory 'success.log')
    try {
        Invoke-RalphValidation -Command 'unknown-validation' -Checkout $testDirectory -LogPath (Join-Path $testDirectory 'unknown.log')
        throw 'Expected unknown validation to be rejected'
    } catch { if ($_ -notmatch 'UNSUPPORTED_VALIDATION') { throw } }
    Write-Output 'PASS: native failure, success, unknown command, evidence log and directory restoration'
} finally {
    Remove-Item Function:git -ErrorAction SilentlyContinue
    # Only remove this test's two known log files and its now-empty unique directory.
    foreach ($name in @('failure.log', 'success.log')) {
        Remove-Item -LiteralPath (Join-Path $testDirectory $name) -ErrorAction SilentlyContinue
    }
    Remove-Item -LiteralPath $testDirectory
}
