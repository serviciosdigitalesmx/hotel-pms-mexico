$ErrorActionPreference='Stop'; Set-Location (Split-Path $PSScriptRoot -Parent)
$secure=Read-Host 'DeepSeek API key (session only; do not paste into the command line)' -AsSecureString
$ptr=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try{$env:DEEPSEEK_API_KEY=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr); & scripts/agent-start.ps1}
finally{Remove-Item Env:DEEPSEEK_API_KEY -ErrorAction SilentlyContinue;[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)}
