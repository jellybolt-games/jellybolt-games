Write-Host '🏢 tamresearch1 HQ Squad' -ForegroundColor Yellow
Set-Location 'C:\Users\tdsquadai\.squad'
Get-ChildItem ralph-heartbeat-tamresearch1*.json -ErrorAction SilentlyContinue | ForEach-Object { Write-Host ('Heartbeat: ' + $_.Name + ' - ' + $_.LastWriteTime) -ForegroundColor Cyan }
Write-Host ''
copilot
