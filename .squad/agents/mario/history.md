# Mario — Project Knowledge

## Project Context
JellyBolt Games — Free mobile games studio for kids & teens.
Stack: React Native, Expo, TypeScript, Supabase
Owner: tdsquadAI Team

## Studio Portfolio
1. **BrainRot Quiz Battle** — Trivia game (in development)
2. **Bounce Blitz** — Arcade game (launch first)
3. **Idle Critter Farm** — Idle game
4. **Code Conquest** — Strategy board game

## Key Decisions
- Android only (no iOS)
- React Native + Expo stack
- Supabase backend
- RevenueCat for billing
- No gambling/loot boxes (non-negotiable)
- Launch order: Bounce Blitz → BrainRot → Idle → Code

## Learnings

### Project State (seeded 2026-03-22)
- **16 games published on itch.io:** Full portfolio from BrainRot Quiz Battle to Dungeon Bolt (premium at $2.99)
- **9 additional games built:** Color Match, Cube Runner, Maze Runner, Planet Defense, Riddle Master, Space Orbit, Tower Stack, Typing Blitz, Dungeon Crawler Classic — pending itch.io upload
- **Total portfolio: 25 games** across 8 genres (arcade, puzzle, strategy, action, rhythm, RPG, educational, simulation)
- **Architecture:** Vanilla HTML5 Canvas + JavaScript per game (7-16 KB files, instant load, no frameworks)
- **Platform strategy:** itch.io (primary), Gumroad ($4.99 bundle), planned Android via React Native (separate repo structure for mobile)
- **Revenue model:** Free games (audience building) + PWYW ($1-2 suggested tips) + premium flagship (Dungeon Bolt at $2.99) + cosmetics/battle pass when mobile launches
- **Brand:** "Fun hits different ⚡" — independent studio, neon/electric blue + lime green identity
- **Key constraints:** NO copyright content, NO gambling mechanics, NO pay-to-win, Android-only for mobile launches
- **Shared infrastructure:** monetization.js, games-menu.html, ads.js modules for cross-game consistency
- **Distribution:** GitHub Pages (org + personal), YouTube (12 videos), Gumroad, Amazon Associates (tag: jellybolt-20)
- **Next phase:** Upload 9 pending games, launch Android versions via React Native, establish revenue from cosmetics/battle passes

