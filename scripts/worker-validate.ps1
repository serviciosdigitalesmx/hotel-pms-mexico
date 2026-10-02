param([string]$TaskId='')
$ErrorActionPreference='Stop'; Set-Location (Split-Path $PSScriptRoot -Parent); ./gradlew test; if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}; if(Test-Path frontend/package.json){npm --prefix frontend run build; if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}}; Write-Output "VALIDATED $TaskId"
