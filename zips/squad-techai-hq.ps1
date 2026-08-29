Write-Host '🚀 TechAI Explained HQ Squad' -ForegroundColor Blue
Set-Location 'C:\Users\tdsquadai\source\repos\techai-explained'
gh issue list --repo techai-explained/techai-explained --state open --limit 5 2>$null
Write-Host ''
Write-Host 'Launching interactive Copilot session...' -ForegroundColor Yellow
Write-Host 'Paste this as your first message:' -ForegroundColor Cyan
Write-Host 'Read STATUS.md and README.md. You are TechAI Explained HQ Squad. Check issues: gh issue list --repo techai-explained/techai-explained --state open. Sisters: JellyBolt Games (needs testers!) and Content Empire. Work on: 1) Audit the 11ty site 2) Cross-promote JellyBolt beta testers 3) Convert articles to Dev.to format 4) Check video pipeline. NO personal names. NO YouTube uploads without approval.' -ForegroundColor White
Write-Host ''
copilot
