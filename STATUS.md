# JellyBolt Games — Project Status
Last updated: 2026-09-07

## Personal handwriting / כתב יד אישי

- `android-app-handwriting/` — separate offline Android prototype for per-child
  recognition of digits, English letters, and Hebrew including final forms.
  Teaches from locally labeled finger drawings; confirms one character at a
  time into a number or word. English/Hebrew UI; no ads or network permission.
  See its README for build/install instructions and recognition limitations.
- Version 0.2.0 targets Android 16/API 36. Non-UI emulator checks exercise
  4,450 labeled examples. Google Play internal testing is active as of
  September 7, 2026 (version code 2); the existing JellyBolt Beta Testers list
  is enabled (2 members). Join: https://play.google.com/apps/internaltest/4700966795480494872
  Additional testers must be enrolled first. This is not a public production release.
- `android-app-handwriting/` — אפליקציית Android ראשונית ונפרדת לזיהוי אישי של
  ספרות ואותיות באנגלית ובעברית, כולל אותיות סופיות. לומדת מדוגמאות מתויגות
  במכשיר ופועלת ללא אינטרנט. מאשרים תו אחד בכל פעם ליצירת מספר או מילה.
  הממשק בעברית ובאנגלית, ללא פרסומות או הרשאת רשת. הוראות ומגבלות ב־README.
- גרסה 0.2.0 מכוונת ל־Android 16/API 36. בדיקות באמולטור ללא הפעלת הממשק
  בוחנות 4,450 דוגמאות מתויגות. הבדיקה הפנימית ב־Google Play פעילה מ־7
  בספטמבר 2026, עם קוד גרסה 2 ורשימת JellyBolt Beta Testers הקיימת (2 חברים).
  קישור ההצטרפות מופיע למעלה; יש להוסיף בודקים נוספים מראש. זו אינה הפצה ציבורית.

## Brand Identity
- **Name:** JellyBolt Games (JellyBolt⚡)
- **NEVER mention "real owner identity"** — independent brand
- **Landing page:** `index.html` — 24-game responsive site with affiliate gear section
- **GitHub Pages:** https://jellybolt-games.github.io/jellybolt-games/ — live, but the org mirror is stale since 2026-03-21
- **itch.io:** jellyboltgames.itch.io
- **YouTube:** https://www.youtube.com/channel/UC0roFVTTy1nSW9Zc7DgcsmQ (12 videos)
- **Contact:** jellybolt@sharebot.net
- **Gumroad:** squadai.gumroad.com (Game Bundle $4.99)

## Games (45 total)
All games in `games/` — single HTML5 Canvas files (7-16 KB, instant load):

### Published on itch.io (16)
1. **Asteroid Dash** 🚀 — Space Shooter ($1 suggested)
2. **Bolt Breaker** ⚡ — Breakout-style (free)
3. **Bounce Blitz** 🏐 — Hyper-casual arcade (free)
4. **BrainRot Quiz Battle** 🧠 — Trivia/quiz (free)
5. **Card Clash** 🃏 — Card game (free)
6. **Code Conquest** 💻 — Programming-themed (free)
7. **Dungeon Bolt** ⚔️ — Roguelike RPG ($2.99 premium flagship)
8. **Gravity Dash** 🌀 — Physics arcade (free)
9. **Hex Match** 🔷 — Puzzle ($1 suggested)
10. **Light Trail** 💡 — Arcade (free)
11. **Memory Matrix** 🧩 — Memory puzzle (free)
12. **Neon Snake** 🐍 — Classic reimagined ($1 suggested)
13. **Pixel Tower** 🏗️ — Stacking (free)
14. **Rhythm Tap** 🎵 — Rhythm action ($2 suggested)
15. **Space Trader** 🛸 — Trading sim (free)
16. **Word Rush** ⌨️ — Typing game (free)

### Built, pending itch.io upload (29 new)
17. **Color Match** 🎨 — Puzzle (free)
18. **Cube Runner** 🧊 — 3D Runner (free)
19. **Maze Runner** 🏃 — Maze navigation (free)
20. **Planet Defense** 🌍 — Tower Defense (free)
21. **Riddle Master** 🧙 — Brain Teaser (free)
22. **Space Orbit** 🪐 — Space Arcade (free)
23. **Tower Stack** 🏰 — Stacking (free)
24. **Typing Blitz** 💨 — Typing speed (free)
25. **Dungeon Crawler Classic** - Tile-based dungeon crawler with fog of war (free)
26. **Archery Quest** 🏹 — Precision archery arcade (free)
27. **Battle Arena** ⚔️ — Arena combat action (free)
28. **Battle Royale** 🎖️ — Last-player-standing battle (free)
29. **Block Storm** 🌊 — Block-clearing storm (free)
30. **Bolt Blocks** ⚡ — Lightning-fast block puzzle (free)
31. **Bolt Solitaire** 🃏 — Solitaire with a bolt twist (free)
32. **Bolt Tiles** 🔲 — Tile-matching puzzle (free)
33. **Bubble Pop** 🫧 — Classic bubble shooting (free)
34. **Crystal Caves** 💎 — Cave exploration adventure (free)
35. **Escape Room** 🔐 — Puzzle adventure with 5 rooms (free)
36. **Merge Master** 🔀 — Merge number puzzle (free)
37. **Neon Dash** 💨 — Neon-lit endless runner (free)
38. **Quest RPG** ⚔️ — Lightweight RPG quest (free)
39. **Racing 3D** 🏎️ — 3D racing action (free)
40. **Sniper Elite** 🎯 — Precision sniper challenge (free)
41. **Space Jump** 🚀 — Gravity-defying space platformer (free)
42. **Space Shooter 3D** 🚀 — First-person 3D space combat (free)
43. **Territory Bolt** ⚡ — Territory control strategy (free)
44. **Tower Defense** 🏰 — Classic tower defense (free)
45. **Zombie Shooter** 🧟 — Wave-based zombie survival (free)

### Shared Assets
- `games/shared/monetization.js` (16.8 KB) — Global monetization module
- `games/shared/games-menu.html` — Cross-game navigation
- `games/shared/ads.js` — Ad integration

## Websites & Platforms
| Platform | URL | Status |
|----------|-----|--------|
| JellyBolt Games (org) | jellybolt-games.github.io/jellybolt-games | ✅ Live |
| JellyBolt Games (org mirror) | jellybolt-games.github.io/jellybolt-games | ⚠️ Live but stale (2026-03-21); push blocked |
| TechAI Explained | techai-explained.github.io/techai-explained | ✅ Live (11ty) |
| Content Empire | content-empire.github.io | ✅ Live |
| dev.to | dev.to/techaiexplained | ✅ Active |
| itch.io | jellyboltgames.itch.io | ✅ 16 games |
| YouTube | UC0roFVTTy1nSW9Zc7DgcsmQ | ✅ 12 videos |
| Google Play | JellyBolt Games Collection | ✅ Internal testing |
| Gumroad | squadai.gumroad.com | ⚠️ Root loads, bundle `/l/qtmyl` 404s |

### Custom Domains (NOT WORKING — need registrar action)
- techai-explained.dev — No DNS records
- content-empire.dev — No DNS records
- jellybolt.dev — No DNS records

## Monetization

### Active
- **itch.io:** jellyboltgames.itch.io — 16 games (8 more pending upload)
- **Gumroad:** squadai.gumroad.com — Game Bundle $4.99 + courses (⚠️ product `qtmyl` returns 404 — verify product ID)
- **Amazon Associates:** Tag `jellybolt-20` configured, gear section on landing page (pending account activation)

### Pending
- **Google AdSense** — Placeholder in all games + landing page
- **BuyMeACoffee** — buymeacoffee.com/jellyboltgames ⚠️ **account does not exist yet (404)**; public support buttons now use the live itch.io store as a safe fallback.
- **Unity Affiliate** — pending signup
- **JetBrains Affiliate** — pending signup

## Configuration
- `config/affiliates.json` — All affiliate/monetization platform configs (Amazon, Gumroad, itch.io, YouTube)
- `.env.example` — Environment variable template

## GitHub Actions
- `ci.yml` — Validates JSON files + checks required directories
- `pages-deploy.yml` — Deploy to GitHub Pages (org repo)

## Pending Actions
- [ ] Sign up for Amazon Associates (affiliate-program.amazon.com) — tag jellybolt-20 ready
- [ ] Upload 8 new games to itch.io (#48)
- [ ] Register real AdSense publisher ID
- [ ] Set up BuyMeACoffee page
- [ ] Register .dev domains (techai-explained.dev, content-empire.dev, jellybolt.dev)
- [ ] Build dungeon game series (#49)
- [ ] Render remaining video scripts (#51)
- [ ] Publish articles to Dev.to/Hashnode (#52)
- [ ] Set up Gmail API for brand inbox monitoring (#50)
- [ ] Record Squad for Kids demo videos (NO YouTube without approval)
