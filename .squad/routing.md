# Work Routing — JellyBolt Games

## Label-Based Routing

| Label | Assigned To | Description |
|-------|------------|-------------|
| `game-design` | Mario (picard) | Game mechanics, level design, progression |
| `mobile-dev` | Sonic (data) | React Native, Expo, Android code |
| `backend` | Link (belanna) | Supabase, APIs, multiplayer, DB |
| `art` | Yoshi (seven) | UI/UX, game art, store graphics |
| `monetization` | Toad (worf) | IAP, battle pass, ads, pricing |
| `legal` | Mario (picard) | Privacy, ToS, compliance |
| `marketing` | Toad (worf) | TikTok, ASO, growth |
| `docs` | Toadette (scribe) | Documentation, decisions |
| `bug` | Sonic (data) | Bug fixes (mobile-first) |
| `infra` | Link (belanna) | CI/CD, deployments, DevOps |

## Priority Levels

| Priority | Label | Response Time |
|----------|-------|--------------|
| P0 — Critical | `priority:critical` | Same day |
| P1 — High | `priority:high` | Within 2 days |
| P2 — Medium | `priority:medium` | Within sprint |
| P3 — Low | `priority:low` | Backlog |

## Game-Specific Routing

Issues in game repos are handled by that game's dev team.
Cross-game issues go to the studio repo (jellybolt-games).

## Escalation Path

1. **Blocked?** → Tag Mario (picard) on the issue
2. **Security concern?** → Tag Toad (worf) immediately
3. **Architecture question?** → Create issue in studio repo, tag Mario
4. **Revenue/legal question?** → Tag both Mario + Toad

## HQ Upstream Connection

**Parent Squad:** tdsquadai/tamresearch1 (Production HQ)
**Escalation Label:** `jellybolt-games`

### When to Escalate to HQ
- Cross-company decisions (affects other sub-squads)
- Budget/resource requests exceeding squad authority
- Technical infrastructure needs (AKS, ADC, pipelines)
- Strategic direction changes
- Revenue milestones or partnership opportunities

### How to Escalate
1. Create issue in `tdsquadai/tamresearch1`
2. Add label `jellybolt-games`
3. Reference the local issue number
4. HQ Ralph will detect and route to Picard

### Shared Resources
- **Squad Skills Repo:** tdsquadai/squad-skills
- **Squad Brain:** tdsquadai/squad-brain
- **Daily Reports:** Send via Teams webhook (see ~/.squad/teams-webhook.url)
