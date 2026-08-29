#Requires -Version 7.0
# ⚡ JellyBolt Games — Ralph Watch Script (Goal-Driven)
# Monitors all game repos, tracks studio health, and auto-creates
# issues for any strategic objective that isn't tracked yet.
# Run: pwsh ralph-watch.ps1

param(
    [int]$IntervalMinutes = 30,
    [switch]$Once
)

$StudioName = "JellyBolt Games"
$Owner = "tdsquadai"
$GameRepos = @(
    "brainrot-quiz-battle",
    "bounce-blitz",
    "idle-critter-farm",
    "code-conquest"
)
$StudioRepo = "jellybolt-games"
$AllRepos = @($StudioRepo) + $GameRepos

function gh-p { gh auth switch --user tdsquadai 2>$null | Out-Null; gh @args }

# ── Company Objectives ─────────────────────────────────────────────────────────
# Strategic goals for JellyBolt. Ralph auto-creates issues for uncovered items.
$StudioObjectives = @(
    @{ Key="upload-pending-games";  Title="Upload 9 pending games to itch.io (Color Match, Cube Runner, Maze Runner, Planet Defense, Riddle Master, Space Orbit, Tower Stack, Typing Blitz, Dungeon Crawler Classic)"; Body="9 games are built and sitting in the games/ directory but NOT yet published on itch.io. Upload all of them at jellyboltgames.itch.io. See STATUS.md 'Built, pending itch.io upload' section." }
    @{ Key="admob-integration";     Title="Integrate Google AdMob ads into all 16 published games"; Body="Wire up ads.js (games/shared/ads.js) with a real AdMob publisher ID for all published itch.io games. Set up rewarded video + interstitial per REVENUE_STRATEGY.md. Expected eCPM: \$5-15." }
    @{ Key="google-play-publish";   Title="Publish top 5 games to Google Play (TWA packages)"; Body="Create TWA (Trusted Web Activity) APKs for the top 5 games and submit to Google Play. See issue #38 for details. Requires Google Play Console account (#2, already closed)." }
    @{ Key="dungeon-bolt-series";   Title="Build Dungeon Bolt series — 5 games (flagship expansion)"; Body="Expand the Dungeon Bolt franchise with a 5-game series. See issues #47 and #49. Dungeon Bolt at \$2.99 is the only premium title — more premium sequels = more revenue." }
    @{ Key="battle-pass";           Title="Design and launch Battle Pass system (seasonal, \$5.99/season)"; Body="Implement the Battle Pass per REVENUE_STRATEGY.md: free tier + premium tier at \$5.99/season. 4-6 week seasons. Target attach rate 8-12% of DAU. Start with Dungeon Bolt." }
    @{ Key="adsense-all-games";     Title="Enable AdSense/AdMob across all game pages"; Body="Wire up adsense-setup.md instructions. Add AdSense to the JellyBolt landing page and each game page for passive display ad revenue." }
    @{ Key="steam-listing";         Title="Publish Dungeon Bolt to Steam"; Body="Submit Dungeon Bolt to Steam. See issue #39. Steam page + Steamworks integration. Premium game at \$2.99 → Steam cut is 30%, net ~\$2.09 per sale." }
    @{ Key="youtube-revenue";       Title="Enable YouTube monetization (channel has 12 videos)"; Body="Apply for YouTube Partner Program on the JellyBolt channel. Requires 1,000 subscribers + 4,000 watch hours. If not yet eligible, create a content plan to grow toward threshold." }
    @{ Key="gumroad-bundle";        Title="Promote Gumroad game bundle (\$4.99) — add marketing links"; Body="The \$4.99 game bundle exists at squadai.gumroad.com but needs promotion. Add 'Get the Bundle' CTAs in all game pages and the landing page." }
    @{ Key="social-marketing";      Title="Launch Reddit marketing campaign (r/gamedev, r/IndieGaming)"; Body="Execute Reddit marketing plan from issue #42. Share game trailers, dev stories, and progress posts in r/gamedev and r/IndieGaming to drive itch.io traffic." }
    @{ Key="cosmetic-shop";         Title="Build in-game cosmetic shop for Dungeon Bolt (skins \$1.49-\$6.99)"; Body="Implement the cosmetic monetization layer per REVENUE_STRATEGY.md. Character skins, themes, emotes. 3-5% conversion target. Wire to payment via Stripe or itch.io." }
    @{ Key="crazygames-submission";  Title="Submit all games to CrazyGames developer portal"; Body="Submit JellyBolt HTML5 games to CrazyGames for additional distribution and ad revenue share. See issue #37." }
)

function Write-Header {
    param([string]$Text)
    $line = "=" * 60
    Write-Host "`n$line" -ForegroundColor Cyan
    Write-Host "  $Text" -ForegroundColor White
    Write-Host "$line" -ForegroundColor Cyan
}

function Get-RepoIssues {
    param([string]$Repo)
    try {
        $issues = gh-p issue list --repo "$Owner/$Repo" --state open --json number,title,labels,assignees,createdAt,updatedAt --limit 50 2>$null | ConvertFrom-Json
        return $issues
    } catch {
        return @()
    }
}

# ── Goal Coverage ──────────────────────────────────────────────────────────────
function Invoke-GoalCheck {
    Write-Host "`n  🎯 OBJECTIVE COVERAGE CHECK:" -ForegroundColor Cyan
    $allIssues = gh-p issue list --repo "$Owner/$StudioRepo" --state all --json number,title --limit 500 2>$null | ConvertFrom-Json
    $gaps = @()
    foreach ($obj in $StudioObjectives) {
        $parts = $obj.Key -split '-' | Where-Object { $_.Length -gt 3 }
        $covered = $allIssues | Where-Object {
            $t = $_.title.ToLower()
            foreach ($p in $parts) { if ($t -like "*$p*") { return $true } }
            return $false
        }
        if (-not $covered) { $gaps += $obj }
    }

    if ($gaps.Count -eq 0) {
        Write-Host "     ✅ All $($StudioObjectives.Count) studio objectives tracked — full coverage!" -ForegroundColor Green
    } else {
        Write-Host "     ⚠️  $($gaps.Count) objective(s) missing — creating issues:" -ForegroundColor Yellow
        foreach ($gap in $gaps) {
            Write-Host "     🆕 $($gap.Title)" -ForegroundColor White
            $result = gh-p issue create --repo "$Owner/$StudioRepo" `
                --title $gap.Title `
                --body $gap.Body `
                --label "squad" 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-Host "        ✅ $result" -ForegroundColor DarkGreen
            } else {
                Write-Host "        ❌ Failed: $result" -ForegroundColor DarkRed
            }
            Start-Sleep -Milliseconds 400
        }
    }
}

function Get-StaleIssues {
    param($Issues, [int]$DaysStale = 7)
    $cutoff = (Get-Date).AddDays(-$DaysStale)
    return $Issues | Where-Object {
        if (-not $_.updatedAt) { return $false }
        $updated = [DateTime]::Parse($_.updatedAt)
        $updated -lt $cutoff
    }
}

function Get-UnassignedIssues {
    param($Issues)
    return $Issues | Where-Object {
        $_.assignees.Count -eq 0
    }
}

function Show-DashboardRound {
    Write-Header "$StudioName — Ralph Dashboard $(Get-Date -Format 'yyyy-MM-dd HH:mm')"

    # Goal-driven check — runs FIRST every round
    Invoke-GoalCheck

    $totalOpen = 0
    $totalStale = 0
    $totalUnassigned = 0

    foreach ($repo in $AllRepos) {
        $issues = Get-RepoIssues -Repo $repo
        $stale = Get-StaleIssues -Issues $issues
        $unassigned = Get-UnassignedIssues -Issues $issues

        $totalOpen += $issues.Count
        $totalStale += $stale.Count
        $totalUnassigned += $unassigned.Count

        $statusIcon = if ($stale.Count -gt 0) { "🟡" } elseif ($issues.Count -gt 0) { "🟢" } else { "⚪" }

        Write-Host "`n  $statusIcon $repo" -ForegroundColor Yellow
        Write-Host "     Open: $($issues.Count) | Stale (7d+): $($stale.Count) | Unassigned: $($unassigned.Count)" -ForegroundColor Gray

        if ($stale.Count -gt 0) {
            Write-Host "     ⚠️  Stale issues:" -ForegroundColor Red
            foreach ($s in $stale | Select-Object -First 3) {
                Write-Host "        #$($s.number) $($s.title)" -ForegroundColor DarkYellow
            }
        }

        if ($unassigned.Count -gt 0) {
            Write-Host "     📋 Unassigned:" -ForegroundColor Magenta
            foreach ($u in $unassigned | Select-Object -First 3) {
                Write-Host "        #$($u.number) $($u.title)" -ForegroundColor DarkMagenta
            }
        }
    }

    # Summary
    Write-Host "`n  📊 Studio Summary" -ForegroundColor Cyan
    Write-Host "     Total Open Issues: $totalOpen" -ForegroundColor White
    Write-Host "     Stale (7d+):       $totalStale" -ForegroundColor $(if ($totalStale -gt 0) { "Yellow" } else { "Green" })
    Write-Host "     Unassigned:        $totalUnassigned" -ForegroundColor $(if ($totalUnassigned -gt 0) { "Yellow" } else { "Green" })

    # Health score
    $health = 100
    if ($totalStale -gt 0) { $health -= ($totalStale * 5) }
    if ($totalUnassigned -gt 3) { $health -= (($totalUnassigned - 3) * 3) }
    $health = [Math]::Max(0, $health)
    $healthColor = if ($health -ge 80) { "Green" } elseif ($health -ge 50) { "Yellow" } else { "Red" }
    Write-Host "     Studio Health:     $health%" -ForegroundColor $healthColor

    # Future placeholders
    Write-Host "`n  🔮 Future Monitors (not yet active):" -ForegroundColor DarkGray
    Write-Host "     📱 Play Store Reviews — coming after first launch" -ForegroundColor DarkGray
    Write-Host "     💰 Revenue Metrics — coming after RevenueCat integration" -ForegroundColor DarkGray
    Write-Host "     📈 DAU/MAU Tracking — coming after Firebase setup" -ForegroundColor DarkGray

    Write-Host ""
}

# Main loop
if ($Once) {
    Show-DashboardRound
} else {
    Write-Host "🔄 Ralph watching $StudioName — every $IntervalMinutes minutes" -ForegroundColor Cyan
    Write-Host "   Press Ctrl+C to stop`n" -ForegroundColor Gray

    while ($true) {
        Show-DashboardRound
        Write-Host "  ⏳ Next check in $IntervalMinutes minutes..." -ForegroundColor DarkGray
        Start-Sleep -Seconds ($IntervalMinutes * 60)
    }
}
