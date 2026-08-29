Write-Host '⚡ JellyBolt Games Squad' -ForegroundColor Green
Set-Location 'C:\Users\tdsquadai\source\repos\jellybolt-games'
gh issue list --repo tdsquadai/jellybolt-games --state open --limit 10 2>$null
Write-Host ''
Write-Host 'Launching interactive Copilot session...' -ForegroundColor Yellow
Write-Host 'Paste this as your first message:' -ForegroundColor Cyan
Write-Host 'Read STATUS.md. You are the JellyBolt Games Squad. Check open issues: gh issue list --repo tdsquadai/jellybolt-games --state open. Sister companies: Content Empire (content-empire-pub/content-empire) and TechAI Explained (techai-explained/techai-explained). Work on: 1) Build a new dungeon game (issue #49) 2) Recruit beta testers (issue #53) 3) Cross-promote with sisters. NO personal names. Contact: tdsquadai@gmail.com' -ForegroundColor White
Write-Host ''
copilot
