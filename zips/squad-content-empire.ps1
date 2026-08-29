Write-Host '📝 Content Empire Squad' -ForegroundColor Magenta
Set-Location 'C:\Users\tdsquadai\source\repos\content-empire'
gh issue list --repo tdsquadai/content-empire --state open --limit 5 2>$null
Write-Host ''
Write-Host 'Launching interactive Copilot session...' -ForegroundColor Yellow
Write-Host 'Paste this as your first message:' -ForegroundColor Cyan
Write-Host 'Read STATUS.md. You are the Content Empire Squad. Check issues: gh issue list --repo tdsquadai/content-empire --state open. Sisters: JellyBolt Games (needs testers - promote jellybolt-games.github.io/jellybolt-games/beta-testers.html) and TechAI Explained. Work on: 1) Publish Dev.to articles from devto-ready/ 2) Add Amazon affiliate links 3) Cross-promote JellyBolt beta testers. NO personal names.' -ForegroundColor White
Write-Host ''
copilot
