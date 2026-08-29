# ⚡ JellyBolt Games — Studio Expansion Roadmap

<div align="center">

```
     ⚡ J E L L Y B O L T   G A M E S ⚡
         Fun hits different.™
```

**Publisher:** JellyBolt Games · **Contact:** tdsquadai@gmail.com · **Web:** [jellyboltgames.itch.io](https://jellyboltgames.itch.io)

</div>

> **Version:** 2.0 · **Date:** July 2026
> **Status:** 🟢 ACTIVE — Phase 1 in progress

---

## 🎯 Studio Vision

JellyBolt Games is an indie **app publisher** — games AND utilities. We build **instant-play mobile games** and **lightweight utility tools**, all under the same neon-lit brand. Every app loads in under a second, looks like it was dipped in liquid lightning, and earns ad revenue while it sleeps.

This roadmap takes us from **3 Play Store apps** to a **full publisher portfolio** of 20+ titles — games for engagement, utilities for steady passive ad income.

### Brand DNA

| Element | Value |
|---------|-------|
| **Publisher** | JellyBolt Games |
| **Visual identity** | Dark backgrounds (`#050510`), neon green accent (`#00ff88`), electric cyan (`#58a6ff`), gold highlights (`#ffcc00`) |
| **Icon / motif** | ⚡ Lightning bolt |
| **Tagline** | "Fun hits different." |
| **Voice** | Energetic, Gen-Z friendly, no-BS. Think "your friend who found the best game and is screaming about it" |
| **App naming** | Game name as title (e.g., "Dungeon Bolt", "BrainRot Quiz Battle"). Publisher attribution via Play Store developer profile |
| **Ethical stance** | No loot boxes, no pay-to-win, no energy gates. Players pay for expression, never for advantage. |

---

## 📊 Current Portfolio

| App | Package | Play Store Status | Category |
|-----|---------|-------------------|----------|
| ⚡ JellyBolt Games - Arcade Collection | `com.jellybolt.games` | Closed testing (active) | `GAME_CASUAL` |
| 🧠 BrainRot Quiz Battle | `com.jellybolt.brainrotquiz` | Internal testing | `GAME_TRIVIA` |
| ⚔️ Code Conquest | `com.jellybolt.codeconquest` | Internal testing | `GAME_BOARD` |

**Full catalog:** 40 games across arcade, puzzle, strategy, RPG, shooter, rhythm, and simulation genres. All vanilla HTML5 Canvas + JS, 7–65 KB per game, playable in-browser or natively via Android WebView.

---

## 1. ⚡ Standalone App Expansion — Top 8 + New Flagship

We're pulling the best games out of the collection and shipping them as standalone Play Store apps. Each one gets its own store listing, screenshots, icon, and Play Store category — built to rank independently.

### 💎 NEW FLAGSHIP — Gem Storm (Match-3 Puzzle)

> **This is the game that will define JellyBolt's DAU numbers.**

| Field | Value |
|-------|-------|
| **Title** | **Gem Storm** |
| **Package** | `com.jellybolt.gemstorm` |
| **Category** | `GAME_PUZZLE` |
| **Engine** | Phaser 3 (CDN) — smooth gem animations, particle effects, tweens |
| **Priority** | 🔴 **HIGHEST** — Phase 2A, parallel with JellyBolt Core SDK |
| **Estimated size** | ~120 KB game + Phaser CDN |

Match-3 is the #1 casual mobile genre by revenue. Candy Crush alone makes $4M/day. We're not building Candy Crush — we're building a **JellyBolt-flavored match-3** that's lightning-fast, neon-gorgeous, and designed to be played for months.

#### Core Mechanics

**Board:** 8×8 grid of colored gems (6 gem types + special gems)

**Basic play:**
- Swap two adjacent gems to create a row/column of 3+ matching gems
- Matched gems explode with neon particle effects, new gems cascade from above
- Chain reactions (cascades) = massive score multipliers

**Power-up combos (the addiction layer):**

| Combo | Creates | Effect |
|-------|---------|--------|
| 4 in a row | ⚡ **Lightning Gem** | Clears entire row OR column (player chooses direction) |
| 5 in a row | 💎 **Bolt Gem** | Clears ALL gems of one color on the board |
| L-shape or T-shape | 💥 **Storm Gem** | 3×3 explosion around the gem |
| 2 special gems swapped | 🌪️ **Mega Storm** | Combines both effects — devastating board clear |

#### Progression System

**100+ Levels** across 10 worlds, each world with a visual theme:

| World | Theme | Visual Style | Levels |
|-------|-------|-------------|--------|
| 1 | Neon Caverns | Dark caves, glowing crystals, green (#00ff88) | 1–10 |
| 2 | Cyber Grid | Tron-style grid, cyan (#58a6ff) | 11–20 |
| 3 | Magma Core | Lava flows, red/orange | 21–30 |
| 4 | Frozen Peaks | Ice crystals, white/blue | 31–40 |
| 5 | Void Storm | Deep space, purple/black | 41–50 |
| 6 | Golden Temple | Ancient ruins, gold (#ffcc00) | 51–60 |
| 7 | Toxic Swamp | Radioactive green, bubbling | 61–70 |
| 8 | Cloud Kingdom | Floating islands, soft pastels | 71–80 |
| 9 | Shadow Realm | Inverted colors, dark motif | 81–90 |
| 10 | Bolt Nexus | JellyBolt HQ — all effects combined | 91–100+ |

**Level objectives** (variety keeps it fresh):

| Type | Example |
|------|---------|
| Score target | "Score 5,000 points in 30 moves" |
| Clear gems | "Clear 20 blue gems" |
| Drop items | "Drop 3 keys to the bottom row" |
| Break obstacles | "Destroy all ice blocks" |
| Chain target | "Create 5 cascades in one level" |
| Boss levels | "Defeat the Void Guardian" (every 10th level) |

**Star ratings:** 1–3 ⚡bolts per level based on score thresholds. Completionists will replay to get 3 bolts on every level.

**Level map:** Scrollable world map (like Candy Crush / Monument Valley) showing progress, locked levels, and boss gates.

#### Lives System

- **5 lives maximum**
- Lose a life when you fail a level (run out of moves / time)
- **1 life regenerates every 20 minutes** (not 30 — we're kinder than King)
- **Watch a rewarded ad** → get 1 bonus life immediately
- **Spend 50 ⚡coins** → refill all 5 lives
- **Daily login** → free life refill
- **No paywall** — you can always play again in 20 minutes, or earn coins through other JellyBolt games

#### Daily & Social Features

- **Daily challenge level:** Unique level every 24h, bonus ⚡coins + XP for completion
- **Streak bonus:** Play daily → streak multiplier on ⚡coin rewards
- **Weekly tournament:** Top scorer on a special weekly level gets exclusive cosmetic gem skin
- **Cross-game integration:** JellyBolt Core SDK — XP, ⚡coins, achievements all shared with other JellyBolt games

#### Visual Design (Phaser 3)

```
┌──────────────────────────────────┐
│ ⚡ GEM STORM    ★★★  Lv.23     │  ← Dark header bar
│ ══════════════════════════════  │
│ Score: 3,450    Moves: 12      │
│ ┌──┬──┬──┬──┬──┬──┬──┬──┐     │
│ │🔴│🔵│🟢│🔴│💎│🟡│🔵│🟢│     │  ← 8×8 gem grid
│ ├──┼──┼──┼──┼──┼──┼──┼──┤     │     on dark bg
│ │🟡│🔴│🔵│🟢│🔴│🔵│🟡│🔴│     │
│ ├──┼──┼──┼──┼──┼──┼──┼──┤     │     Gems glow with
│ │🔵│🟢│🔴│⚡│🟡│🔴│🔵│🟢│     │     neon outlines
│ ├──┼──┼──┼──┼──┼──┼──┼──┤     │
│ │🟢│🔵│🟡│🔴│🔵│🟢│💥│🔴│     │     Special gems
│ ├──┼──┼──┼──┼──┼──┼──┼──┤     │     pulse with
│ │🔴│🟡│🔵│🟢│🔴│🟡│🔵│🟢│     │     lightning FX
│ ├──┼──┼──┼──┼──┼──┼──┼──┤     │
│ │🔵│🔴│🟢│🔵│🟡│🔴│🟢│🔵│     │
│ ├──┼──┼──┼──┼──┼──┼──┼──┤     │
│ │🟡│🟢│🔴│🟡│🔵│🟢│🔴│🟡│     │
│ ├──┼──┼──┼──┼──┼──┼──┼──┤     │
│ │🔴│🔵│🟡│🔴│🟢│🔵│🟡│🔴│     │
│ └──┴──┴──┴──┴──┴──┴──┴──┘     │
│                                │
│  ❤️❤️❤️❤️🤍  Lives: 4/5      │  ← Lives bar
│ ┌────────────────────────────┐ │
│ │        [ Ad Banner ]       │ │  ← AdMob banner
│ └────────────────────────────┘ │
└──────────────────────────────────┘
```

- Gems: Rounded squares with inner glow, subtle pulsing animation
- Matches: Neon lightning effect connecting matched gems, then shatter + particles
- Cascades: Screen-shake, combo counter flies up ("×3!", "×4!", "MEGA!")
- Special gems: Visible lightning/storm/bolt effects inside the gem
- Level complete: ⚡ bolt strikes from top, star rating flies in, confetti particles
- Background: Per-world themed backdrop (caves, grid, lava, etc.) with subtle parallax

#### Monetization (Ethical, Per Revenue Strategy)

| Method | Details |
|--------|---------|
| **Rewarded video ads** | Watch ad → +5 moves on a failed level, OR +1 life |
| **Interstitial ads** | Between levels (max 1 per 3 levels, skippable after 5s) |
| **Banner ad** | Bottom of screen during gameplay |
| **Future IAP** (Phase 5) | ⚡coin packs for lives/boosters. Cosmetic gem skins. NO pay-to-win level skips. |

#### Why This Is the #1 Priority

1. **Match-3 = highest DAU genre on mobile.** Users play 5-10 sessions/day.
2. **Session length sweet spot:** 2-5 minutes per level = perfect for ads between levels.
3. **100+ levels = months of content** before users run out. Expandable with new worlds.
4. **Lives system = return visits.** Users come back every 20 min when lives refill.
5. **Cross-game synergy:** ⚡coins earned in Gem Storm power up ALL JellyBolt games.
6. **ASO keywords:** "match 3", "puzzle game", "gem puzzle" = massive search volume.
7. **Phaser 3 engine:** Buttery-smooth animations that make the game FEEL premium.

---

### 🥇 Tier 1 — Flagships (Ship First)

| # | Title | Package | Category | Why It's a Standalone |
|---|-------|---------|----------|----------------------|
| 1 | **Dungeon Bolt** | `com.jellybolt.dungeonbolt` | `GAME_ADVENTURE` | ⚡ Our flagship. 5-floor roguelite with 11 enemy types, loot drops, shops, bosses, and permadeath. This IS the game that justifies the studio name. Premium-tier depth in a 30 KB package. |
| 2 | **Quest RPG** | `com.jellybolt.questrpg` | `GAME_ROLE_PLAYING` | Full RPG with character progression, quests, and combat. Largest codebase (44 KB) = deepest content. RPGs drive the highest session times on mobile. |
| 3 | **Escape Room** | `com.jellybolt.escaperoom` | `GAME_PUZZLE` | 5 interconnected rooms, 30+ puzzles, inventory system, 4 endings. Puzzle-adventure is a proven Play Store category (The Room series, Escape games). |

### 🥈 Tier 2 — High Engagement (Ship Second)

| # | Title | Package | Category | Why It's a Standalone |
|---|-------|---------|----------|----------------------|
| 4 | **Block Storm** | `com.jellybolt.blockstorm` | `GAME_PUZZLE` | Tetris-like mechanics = instant recognition. Everyone knows how to play. Infinite replay. 40 KB of pure dopamine. |
| 5 | **Merge Master** | `com.jellybolt.mergemaster` | `GAME_CASUAL` | Our biggest game (67 KB). Merge mechanics are clinically proven addiction loops. Perfect for future coin-sink monetization. |
| 6 | **Battle Royale** | `com.jellybolt.battleroyale` | `GAME_ACTION` | Genre recognition alone drives downloads. Complex multi-system gameplay. Great ASO keywords. |

### 🥉 Tier 3 — Niche Gems (Ship Third)

| # | Title | Package | Category | Why It's a Standalone |
|---|-------|---------|----------|----------------------|
| 7 | **Tower Defense** | `com.jellybolt.towerdefense` | `GAME_STRATEGY` | TD has a dedicated fanbase that searches specifically for it. Strategic depth, easy to extend with new maps/towers. |
| 8 | **Bubble Pop** | `com.jellybolt.bubblepop` | `GAME_CASUAL` | Second-largest game (45 KB). Bubble shooter is the #1 casual genre on mobile. Pure mass-market appeal. |

### Full Studio Lineup (After Expansion)

```
⚡ JellyBolt Games — Play Store Portfolio

 LIVE NOW                           NEW — PHASE 1               NEW — PHASE 2A
 ─────────                          ──────────────               ──────────────
 🎮 JellyBolt Collection            ⚔️ Dungeon Bolt              💎 GEM STORM ← FLAGSHIP
 🧠 BrainRot Quiz Battle            🗡️ Quest RPG                    (match-3 puzzle)
 ⚔️ Code Conquest                   🔐 Escape Room

 NEW — PHASE 3                      FUTURE — PHASE 4 (New IP)
 ──────────────                     ─────────────────────────
 🧱 Block Storm                     🏎️ Neon Drift (3D racing)
 🔄 Merge Master                    ⚡ Phantom Arena (action RPG)
 💥 Battle Royale                   🌌 Star Colonies (3D strategy)
 🏰 Tower Defense
 🫧 Bubble Pop

 Total: 15 game apps + 10 utility apps = 25 apps from one publisher
```

---

## 2. 🔧 Game Engine Strategy

### Why We Stay JavaScript-Native

| Engine | WebView OK? | Build Size | Our Verdict |
|--------|------------|-----------|-------------|
| **Vanilla Canvas + JS** | ✅ Perfect | 7–65 KB | **Our core stack.** Instant load, offline, tiny APKs. Keep using for 2D games. |
| **Phaser 3** | ✅ Excellent | 200 KB (CDN) | **Best upgrade for 2D.** Particle effects, physics, sprite sheets. Use for next-gen 2D titles. |
| **Three.js** | ✅ Excellent | 150 KB (CDN) | **Already in use** (Space Shooter 3D). Best 3D-to-size ratio. |
| **BabylonJS** | ✅ Good | 500 KB–3 MB (CDN) | **Best for complex 3D.** Orbit cameras, PBR materials, full scene graphs. |
| **Unity WebGL** | ❌ No | 15–50 MB | **Rejected.** WASM overhead, SharedArrayBuffer issues, 3-10s startup. Kills the "instant play" brand promise. |
| **Godot HTML5** | ⚠️ Marginal | 5–15 MB | **Not worth it.** Still too heavy for WebView. Marginal quality gain over JS. |

**Decision:** Phaser 3 (2D) + Three.js/BabylonJS (3D). JavaScript is our competitive advantage — games that load before the splash screen fades.

### 3 New Game Concepts (Phase 4)

These use advanced engines while staying true to the JellyBolt aesthetic: neon colors, dark backgrounds, fast gameplay.

#### 🏎️ Neon Drift
> *Low-poly neon racing. Drift through procedural tracks. One thumb, infinite speed.*

- **Engine:** Three.js + cannon-es physics
- **Package:** `com.jellybolt.neondrift`
- **Visual style:** Synthwave tracks glowing against void-black space. Neon trail particles.
- **Mechanic hook:** Drift-to-boost. The more you drift, the faster you go. But lose control and you're toast.
- **Estimated size:** ~50 KB game + CDN libs

#### ⚡ Phantom Arena
> *Top-down hack-and-slash. Procedural dungeons. Pixel-perfect carnage.*

- **Engine:** Phaser 3
- **Package:** `com.jellybolt.phantomarena`
- **Visual style:** JellyBolt neon-dark with sprite-based characters and particle explosions
- **Mechanic hook:** Combo chains. Kill enemies fast enough and your damage multiplier climbs. Stop killing and it resets. Never. Stop. Swinging.
- **Estimated size:** ~80 KB game + CDN lib

#### 🌌 Star Colonies
> *Build your empire among the stars. Trade, expand, defend. One more turn...*

- **Engine:** BabylonJS
- **Package:** `com.jellybolt.starcolonies`
- **Visual style:** Low-poly space stations orbiting glowing planets. UI in JellyBolt neon green on dark panels.
- **Mechanic hook:** "One more turn" syndrome. Each turn takes 30 seconds but the consequences unfold across 5 turns. You can't stop mid-chain.
- **Estimated size:** ~100 KB game + CDN lib

---

## 2B. 📈 Chart-Inspired Games — Top-Charting Variations

Every game below is **inspired by** a top-charting Play Store title but built as a legally distinct JellyBolt original — different name, different art, different twist. All built in HTML5 Canvas or Phaser 3, running in our proven WebView wrapper.

### Feasibility & Priority Matrix

| # | JellyBolt Title | Inspired By | Engine | Build Effort | Chart Potential | Priority |
|---|----------------|-------------|--------|-------------|----------------|----------|
| 1 | **⚡ Bolt Blocks** | Block Blast! | Canvas | 🟢 Low (1–2 days) | ⭐⭐⭐⭐⭐ | 🔴 **P0 — Build immediately** |
| 2 | **⚡ Bolt Solitaire** | Solitaire | Canvas | 🟢 Low (2–3 days) | ⭐⭐⭐⭐⭐ | 🔴 **P0 — Build immediately** |
| 3 | **⚡ Bolt Tiles** | Mahjong | Canvas | 🟢 Low (2–3 days) | ⭐⭐⭐⭐ | 🟡 P1 — Ship soon |
| 4 | **⚡ Bolt Color** | Zen Color | Canvas | 🟢 Low (2–3 days) | ⭐⭐⭐⭐ | 🟡 P1 — Ship soon |
| 5 | **⚡ Neon Dash** | Subway Surfers | Phaser 3 | 🟡 Medium (5–7 days) | ⭐⭐⭐⭐⭐ | 🟡 P1 — High impact, more effort |
| 6 | **⚡ Rhythm Bolt** | Geometry Dash | Phaser 3 | 🟡 Medium (5–7 days) | ⭐⭐⭐⭐⭐ | 🟡 P1 — Needs level editor/music |
| 7 | **⚡ Territory Bolt** | Paper.io 2 | Canvas | 🟡 Medium (3–5 days) | ⭐⭐⭐⭐ | 🟡 P1 — Bot AI needed |
| 8 | **⚡ Bolt Spinner** | Coin Master | Canvas | 🟡 Medium (4–6 days) | ⭐⭐⭐⭐ | 🟢 P2 — Complex meta-game |
| 9 | **⚡ Saboteur** | Among Us | Canvas/Phaser | 🔴 High (7–10 days) | ⭐⭐⭐ | 🟢 P2 — AI deduction is hard |
| 10 | **⚡ Bolt World** | Roblox | Three.js | 🔴 Very High (weeks) | ⭐⭐⭐ | ⚪ P3 — Long-term moonshot |

### 🔴 P0 — Build Immediately (1–3 Day Builds)

#### 1. ⚡ Bolt Blocks
> *Block puzzle. Fit shapes. Clear rows. One-hand play. Infinite rounds.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.boltblocks` |
| **Inspired by** | Block Blast! (#1 Puzzle, 100M+ downloads) |
| **Engine** | Vanilla Canvas |
| **Build time** | 1–2 days |

**Gameplay:** 10×10 grid. Each turn you get 3 random block shapes (tetrominos, L-shapes, lines). Drag and place them on the grid. When a full row or column is filled, it clears and you score. Game ends when no shapes fit. No time pressure — pure puzzle.

**Why it charts:** Block Blast has 100M+ downloads because the mechanic is dead simple and infinitely replayable. Zero learning curve. Perfect for ad breaks between rounds.

**JellyBolt twist:** Neon ghost-preview of where blocks will land. Combo multiplier for clearing multiple rows at once. ⚡ Lightning effect on clears. Daily challenge: "Clear 20 rows in one game."

---

#### 2. ⚡ Bolt Solitaire
> *Classic Klondike solitaire. Neon cards. Daily challenges. Streak rewards.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.boltsolitaire` |
| **Inspired by** | Solitaire (perennial top-10, 500M+ category downloads) |
| **Engine** | Vanilla Canvas |
| **Build time** | 2–3 days |

**Gameplay:** Standard Klondike solitaire — 7 tableau columns, draw-1 or draw-3 from stock, build foundation piles A→K by suit. Auto-complete when possible. Undo button. Win tracking + stats.

**Why it charts:** Solitaire is literally the most-played card game in history. It's been in the top-10 since app stores existed. Players play 5-10 games per day. It's a daily habit game.

**JellyBolt twist:** Neon card faces on dark background. ⚡ bolt suit symbols. "Bolt Mode" — timed challenge (solve in 3 minutes for bonus coins). Daily deal: same deck for everyone, compete on speed. Streak rewards tied to JellyBolt Core.

---

### 🟡 P1 — Ship Soon (2–7 Day Builds)

#### 3. ⚡ Bolt Tiles
> *Mahjong tile matching. Hundreds of layouts. Zen neon. Relaxing but addictive.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.bolttiles` |
| **Inspired by** | Mahjong (evergreen puzzle, 200M+ category downloads) |
| **Engine** | Vanilla Canvas |
| **Build time** | 2–3 days |

**Gameplay:** Classic Mahjong solitaire — match pairs of identical tiles to remove them from the board. Tiles must be "free" (not blocked by adjacent or stacked tiles). 50+ board layouts. Hint system. Shuffle if stuck.

**JellyBolt twist:** Neon-glow tile edges. Tiles have JellyBolt-themed symbols (bolts, gems, circuits, stars). Zen ambient mode with subtle particle backgrounds. Daily layout challenge. "Bolt Match" — match highlighted tiles within 5 seconds for bonus points.

---

#### 4. ⚡ Bolt Color
> *Color by number. Neon & space themes. Relaxing. Ad-supported zen game.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.boltcolor` |
| **Inspired by** | Zen Color / Happy Color (50M+ downloads) |
| **Engine** | Vanilla Canvas |
| **Build time** | 2–3 days |

**Gameplay:** Tap numbered cells to fill them with the corresponding color. Pinch-to-zoom on large images. Categories: Space, Neon, Animals, Mandala, Pixel Art. New images weekly.

**JellyBolt twist:** Space & neon themed images (rockets, galaxies, circuit boards, gem patterns). Completed images glow with neon outlines. Dark background with neon color palette. "Bolt Rush" mode — race to complete an image against the clock. Share completed art.

**Content pipeline:** Images can be generated programmatically (pixel art grids, mandala patterns) or hand-designed. Start with 30 images, add 5/week.

---

#### 5. ⚡ Neon Dash
> *Endless runner. Swipe to dodge. Neon city. Power-ups. Daily missions.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.neondash` |
| **Inspired by** | Subway Surfers (#1 Arcade, 3B+ downloads) |
| **Engine** | Phaser 3 |
| **Build time** | 5–7 days |

**Gameplay:** Side-scrolling (or pseudo-3D lane-based) endless runner. Character runs automatically. Swipe left/right to change lanes, up to jump, down to slide. Collect ⚡coins. Dodge obstacles. Power-ups: magnet, shield, score-doubler, mega-jump. Distance = score.

**Why it charts:** Endless runners are the single most-downloaded game category in history. The mechanic is universal — everyone understands "run and dodge."

**JellyBolt twist:** Neon cyberpunk city backdrop. Character leaves a neon trail. Obstacles are glitched data blocks and rogue code. "Bolt Rush" power-up turns everything gold and triples coins for 10 seconds. Daily mission system: "Collect 500 coins", "Run 2000m without dying."

**Phaser 3 usage:** Arcade physics for jumping/sliding, tween-based obstacle spawning, particle trails on the character, parallax scrolling for 3-layer city background.

---

#### 6. ⚡ Rhythm Bolt
> *Rhythm platformer. Tap to the beat. Neon obstacles synced to music.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.rhythmbolt` |
| **Inspired by** | Geometry Dash (100M+ downloads) |
| **Engine** | Phaser 3 |
| **Build time** | 5–7 days |

**Gameplay:** Auto-scrolling platformer. Character moves forward automatically. Tap to jump, hold to fly (rocket mode), release to drop. Obstacles are synced to the beat of the background music. Die = restart from beginning (or checkpoint in practice mode). 20+ built-in levels with increasing difficulty.

**Why it charts:** Geometry Dash has 100M+ downloads and a fanatical community. The "die and retry" loop is incredibly addictive when paired with music.

**JellyBolt twist:** Neon obstacle course with pulsing colors that match the BPM. ⚡ bolt character with particle trail. "Bolt Mode" sections where speed doubles and the screen flashes. User-created levels via a simple JSON format (future feature).

**Music:** Use royalty-free electronic/synthwave tracks. Start with 10 songs. Web Audio API for beat detection and sync.

---

#### 7. ⚡ Territory Bolt
> *Claim territory. Draw lines. Don't get cut. Eat the competition.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.territorybolt` |
| **Inspired by** | Paper.io 2 (100M+ downloads) |
| **Engine** | Vanilla Canvas |
| **Build time** | 3–5 days |

**Gameplay:** Top-down arena. You control a small square that moves continuously. You own a colored territory. Move outside your territory to draw a line, then return to claim all enclosed area. But if an opponent (bot) crosses your line before you return, you die. Eat other players' territory. Last one standing — or whoever owns the most territory when time runs out — wins.

**JellyBolt twist:** Neon grid arena on dark background. Your territory glows with your color. Bolt power-up: temporarily become invincible + speed boost. AI bots with personalities (Aggressive, Defensive, Sneaky). Mini-map in corner. "Ranked" mode with bot difficulty scaling based on your win rate.

---

### 🟢 P2 — Queue for Later (4–10 Day Builds)

#### 8. ⚡ Bolt Spinner
> *Spin the slot machine. Build your base. Raid rivals. Coin economy.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.boltspinner` |
| **Inspired by** | Coin Master (100M+ downloads, $280M/year revenue) |
| **Engine** | Canvas + Phaser 3 |
| **Build time** | 4–6 days |

**Gameplay:** Core loop: spin a 3-reel slot machine. Results determine your action: Coins (build), Shields (defend), Hammers (raid), or Bolts (attack). Use coins to build/upgrade your base (5 structures per level, 50+ levels). Raid bots' bases to steal coins. Shield to protect your own. Progress through themed villages.

**JellyBolt twist:** Slot machine has ⚡bolt symbols instead of coins. Bases are neon-themed space stations. Raid animations show lightning strikes on enemy bases. "Bolt Jackpot" — all 3 reels show ⚡ → massive coin reward + rare item. Daily free spins tied to streak system.

**Note:** Single-player with bots only (no real multiplayer needed). Coin Master's genius is the meta-game loop, not the social features.

---

#### 9. ⚡ Saboteur
> *Social deduction. Find the traitor. Complete tasks. Vote to eject. Single-player vs AI.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.saboteur` |
| **Inspired by** | Among Us (500M+ downloads) |
| **Engine** | Phaser 3 |
| **Build time** | 7–10 days |

**Gameplay:** You're a crew member on a neon space station. 1 of 6 crew is a saboteur (AI-controlled). Complete mini-tasks (wire puzzles, pattern matching, code entry) while watching for suspicious AI behavior. When a "body" is found, vote on who to eject. Saboteur wins if they eliminate enough crew; crew wins by completing all tasks or ejecting the saboteur.

**JellyBolt twist:** Neon space station with JellyBolt aesthetic. Mini-tasks are JellyBolt-themed puzzles. AI saboteur has behavioral tells that get subtler as difficulty increases. "Bolt Scanner" item reveals one crew member's role. Single-player campaign with 20 scenarios of increasing complexity.

**Feasibility note:** The hard part is convincing AI behavior. Start with scripted scenarios (like a visual novel with choices) rather than truly emergent AI. Still compelling.

---

### ⚪ P3 — Long-Term Moonshot

#### 10. ⚡ Bolt World
> *Sandbox builder. Voxel construction. Create, explore, share.*

| Field | Value |
|-------|-------|
| **Package** | `com.jellybolt.boltworld` |
| **Inspired by** | Roblox / Minecraft (largest game category by MAU) |
| **Engine** | Three.js / BabylonJS |
| **Build time** | Weeks–months (ongoing project) |

**Gameplay:** Simplified voxel sandbox. Place and destroy blocks in a 3D world. Pre-made templates (house, castle, spaceship). Free-build mode. Share creations via screenshot.

**Reality check:** This is a moonshot. A true Roblox competitor is impossible for a small team. But a "Minecraft Creative Mode Lite" in Three.js is feasible. Scope it as a relaxing builder, not a game platform. Start with a 32×32×32 world, basic block types, simple camera controls.

**Phase:** Not on the immediate roadmap. Revisit after the studio has 20+ apps generating revenue.

---

### Chart-Inspired Games — Implementation Order

```
IMMEDIATE (Phase 3C)         SOON (Phase 4C)              LATER (Phase 5+)
────────────────────         ────────────────              ────────────────
⚡ Bolt Blocks (1-2d)        ⚡ Neon Dash (5-7d)           ⚡ Bolt Spinner (4-6d)
⚡ Bolt Solitaire (2-3d)     ⚡ Rhythm Bolt (5-7d)         ⚡ Saboteur (7-10d)
⚡ Bolt Tiles (2-3d)         ⚡ Territory Bolt (3-5d)      ⚡ Bolt World (weeks)
⚡ Bolt Color (2-3d)

Build days: ~10              Build days: ~15               Build days: ~20+
```

---

## 3. ⚡ JellyBolt Core — The Addiction Framework

A shared JavaScript module (`games/shared/jellybolt-core.js`) loaded by every game, providing a unified player identity and progression system across the entire JellyBolt ecosystem.

**Design principle:** No dark patterns. Engagement through genuine reward loops, not manipulation. Aligned with our revenue strategy: "Players pay for expression, never for advantage."

### 3.1 🔥 Daily Streaks & Challenges

```
 ┌────────────────────────────────────────────┐
 │  ⚡ JellyBolt Daily                        │
 │                                            │
 │  🔥 Streak: 7 days                         │
 │  ┌─────┬─────┬─────┬─────┬─────┬─────┐   │
 │  │ ✅  │ ✅  │ ✅  │ ✅  │ ✅  │ ✅  │   │
 │  │ Mon │ Tue │ Wed │ Thu │ Fri │ Sat │   │
 │  └─────┴─────┴─────┴─────┴─────┴─────┘   │
 │                                            │
 │  Today's Challenge:                        │
 │  "Clear Floor 3 in Dungeon Bolt"           │
 │  Reward: 50 XP + 15 ⚡coins               │
 │                                            │
 │  🎁 Day 7 Reward: 100 ⚡coins + Neon Skin │
 └────────────────────────────────────────────┘
```

| Streak Day | Reward |
|-----------|--------|
| Day 1 | 10 ⚡coins |
| Day 3 | 25 ⚡coins + Mystery Box |
| Day 7 | 100 ⚡coins + Rare Skin |
| Day 14 | 250 ⚡coins + Epic Skin |
| Day 30 | 1000 ⚡coins + Legendary title |

- **Daily challenge** rotates per game — fresh every 24h
- **Cross-game daily**: "Play 3 JellyBolt games today" → bonus XP
- **Streak protection**: Miss a day? Spend 50 ⚡coins to keep your streak alive
- Storage: `localStorage` with `jb_` prefix — works offline, no backend needed

### 3.2 📈 XP & Leveling

| Action | XP Earned |
|--------|-----------|
| Complete any round | +10 XP |
| Win / beat high score | +25 XP |
| Complete daily challenge | +50 XP |
| First game of the day | +20 XP |
| Try a new JellyBolt game | +30 XP (first time only) |

**Level titles** — shown on leaderboards and game-over screens:

| Level Range | Title | Color |
|------------|-------|-------|
| 1–5 | ⚡ Rookie | White |
| 6–15 | ⚡ Pro | Green (`#00ff88`) |
| 16–30 | ⚡ Elite | Cyan (`#58a6ff`) |
| 31–50 | ⚡ Legend | Gold (`#ffcc00`) |
| 51+ | ⚡ Mythic | Magenta (`#ff006e`) |

**Streak multiplier:** ×1.5 XP at 7-day streak, ×2 XP at 30-day streak.

### 3.3 🎨 Unlockables

| Category | Examples | How to Unlock |
|----------|----------|---------------|
| **Themes** | Void Black, Retro Pixel, Ocean Blue, Neon Pink, JellyBolt Gold | Level milestones |
| **Skins** | Character/board skins per game (neon outlines, particle trails) | ⚡coins or achievements |
| **Power-ups** | Slow-mo, Shield, Score ×2, Extra Life | ⚡coins (50–200 each) |
| **Titles** | "Speed Demon", "Puzzle King", "Streak Master", "Bolt Legend" | Specific achievements |
| **Borders** | Neon glow borders on profile card | Rare/epic achievements |

All cosmetic. No gameplay advantages. No pay-to-win. Ever.

### 3.4 🏆 Leaderboards

- **Per-game top 100** (localStorage, instant)
- **Personal best** with date + level at time of score
- **⚡ JellyBolt Score**: Weighted composite across all games — the "studio-wide" rank
- **Future-ready**: Data schema designed to sync to Firebase/Supabase when we add cloud leaderboards

### 3.5 🎖️ Achievements

| Achievement | Condition | Reward |
|-------------|-----------|--------|
| ⚡ First Bolt | Play any JellyBolt game once | 10 ⚡coins |
| 🏆 Century Club | Score 100+ in any game | 25 ⚡coins |
| 🔥 Week Warrior | 7-day streak | 100 ⚡coins + "Dedicated" title |
| 🌟 Game Explorer | Play 10 different JellyBolt games | 50 ⚡coins + "Explorer" title |
| 💎 Bolt Completionist | Play all 40 games | 500 ⚡coins + "Legend" title |
| ⚡ Lightning Fingers | Complete Word Rush in under 30s | 75 ⚡coins |
| 🧠 Galaxy Brain | Perfect score in BrainRot Quiz | 100 ⚡coins |
| ⚔️ Bolt Slayer | Clear all 5 floors in Dungeon Bolt | 200 ⚡coins |
| 🏗️ Tower Architect | Stack 50+ blocks in Pixel Tower | 50 ⚡coins |
| 🎯 Dead Eye | 95%+ accuracy in Sniper Elite | 75 ⚡coins |

**Rarity tiers:** Common → Uncommon → Rare → Epic → ⚡Legendary

Achievement toasts use the JellyBolt neon style — green glow animation with bolt icon.

### 3.6 🔄 "One More Round" Hooks

Every game-over screen is designed to make you tap "Play Again":

1. **Near-miss**: "3 points from a new high score! ⚡" → Play Again button glows
2. **Progressive difficulty**: Rounds get slightly harder. You always feel "I almost had it"
3. **Reward tease**: "Next reward at Level 8 — just 2 more games! ⚡" on game-over
4. **Comeback power-up**: After 3 losses, offer a free power-up. Turns frustration into "let me try ONE more time"
5. **Session bonus**: "10-minute bonus: +5 ⚡coins!" — rewards longer play
6. **Urgency**: "Daily challenge resets in 4h 23m" — time pressure
7. **Social proof**: "You're top 15% in Bounce Blitz!" — competitive drive (local data)

### 3.7 💰 Currency System

```
 ⚡coins (common)              💎 Gems (premium)
 ─────────────────             ───────────────────
 Earned: gameplay,             Earned: achievements,
 streaks, dailies,             milestones, 30-day
 achievements                  streaks

 Spent: power-ups,             Spent: premium skins,
 common skins, streak          exclusive themes,
 protection                    legendary borders
```

- **No real-money purchases** at launch — everything is earned through play
- **Cross-game wallet**: ⚡coins earned in Dungeon Bolt can buy skins in Bounce Blitz
- **Future-ready for IAP**: ⚡coin packs, 💎gem packs, ad-skip passes (when we enable RevenueCat)

### 3.8 🛠️ Implementation — `jellybolt-core.js`

Single file (~15 KB), loaded via `<script>` in every game's `index.html`:

```javascript
// JellyBolt Core SDK — used by all 40+ games
JellyBolt.init('dungeon-bolt')          // Initialize for this game
JellyBolt.player.level                  // Current player level
JellyBolt.player.xp                     // XP progress
JellyBolt.player.coins                  // ⚡coin balance
JellyBolt.player.gems                   // 💎 Gem balance
JellyBolt.player.title                  // "⚡ Elite"
JellyBolt.addXP(25)                     // Award XP (auto level-up + toast)
JellyBolt.addCoins(10)                  // Award ⚡coins
JellyBolt.streak.check()               // Update streak on game launch
JellyBolt.daily.getChallenge()          // Today's challenge for this game
JellyBolt.daily.complete()              // Mark daily done, award rewards
JellyBolt.achieve.unlock('bolt-slayer') // Unlock achievement + toast
JellyBolt.leaderboard.submit(1500)      // Submit score
JellyBolt.ui.showGameOver(score)        // Branded game-over with all hooks
JellyBolt.ui.showLevelUp()              // ⚡ Level-up celebration
```

All data in `localStorage('jb_*')`. Works offline. No backend. No accounts.

---

## 4. 💰 Monetization Strategy

Aligned with our [Revenue Strategy](REVENUE_STRATEGY.md): ethical monetization, no pay-to-win.

### Current Revenue Streams

| Stream | Platform | Status | How It Works |
|--------|----------|--------|-------------|
| **Game Sales** | itch.io | ✅ Active | Free + PWYW ($1–$2) + Premium ($2.99 Dungeon Bolt) |
| **Game Bundle** | Gumroad | ✅ Active | "JellyBolt Games Starter Pack" — all games downloadable |
| **Tips** | Buy Me a Coffee | ⏳ Setup | buymeacoffee.com/jellyboltgames |
| **Ad Revenue** | AdMob (in-app) | ✅ Integrated | Rewarded video ads for bonus ⚡coins |
| **Ad Revenue** | AdSense (web) | ⏳ Pending | Landing page ad placements |

### Expansion Monetization Plan

| Phase | Revenue Addition | Expected Impact |
|-------|-----------------|----------------|
| **Phase 1** | 3 new standalone apps on Play Store → more ad inventory | +30% ad impressions |
| **Phase 2** | JellyBolt Core → longer sessions, daily returns | +50% DAU, +2x session length |
| **Phase 3** | 5 more standalone apps → broader keyword coverage on Play Store | +100% organic installs |
| **Phase 4** | Premium new-IP games (Neon Drift, Phantom Arena) | New $2.99+ pricing tier |
| **Phase 5** | RevenueCat IAP → ⚡coin packs, 💎gem packs, ad-skip | First real IAP revenue |

### Pricing by Distribution Channel

| Channel | Free Games | Tip Games | Premium |
|---------|-----------|-----------|---------|
| **Play Store** | Free + ads | Free + ads | Free + ads (premium via IAP later) |
| **itch.io** | $0 PWYW | $1–$2 PWYW | $2.99 fixed |
| **Gumroad** | — | — | Bundle: $4.99 (all games) |
| **Web** | Free (AdSense) | Free (AdSense) | Free (AdSense) |

### What We Don't Do

Per our [Revenue Strategy](REVENUE_STRATEGY.md):
- ❌ No loot boxes with real money
- ❌ No pay-to-win mechanics
- ❌ No energy systems that block play
- ❌ No aggressive push notifications
- ❌ No data selling
- ❌ No fake scarcity timers

---

## 5. 🔧 Utility Apps Line — Passive Ad Revenue

### The Strategy

Games drive engagement. Utilities drive **steady, predictable ad revenue**.

Every utility app is a separate Play Store listing under the "JellyBolt Games" publisher. They're fast to build (HTML5 WebView or lightweight native), solve a real problem, and monetize through banner + interstitial ads. A user who opens a calculator 5 times a day is 5 ad impressions a day, 365 days a year — without us building any new content.

### Why This Works for JellyBolt

| Factor | Games | Utilities |
|--------|-------|-----------|
| **User sessions** | 1–3/day, 5–15 min each | 3–10/day, 30 sec–2 min each |
| **Retention** | Needs constant content updates | "Set and forget" — user keeps the app forever |
| **Ad revenue model** | Rewarded video (higher CPM, lower volume) | Banner + interstitial (lower CPM, MUCH higher volume) |
| **Build effort** | Medium–High (game design, balance, art) | Low (standard UI, solved problems) |
| **Competition** | High (millions of games) | High but **discoverable** (utility keywords are high-intent search) |
| **Cross-promotion** | Utility users → see JellyBolt games in ads | Game users → see utility apps in "More by JellyBolt" |

### Utility App Portfolio

#### 🥇 Tier 1 — Ship First (Highest ROI, Fastest Build)

| # | App Name | Package | Category | Build Method | Ad Strategy | Why First |
|---|----------|---------|----------|-------------|-------------|-----------|
| 1 | **⚡ Bolt Calculator** | `com.jellybolt.calculator` | `TOOLS` | WebView (HTML/CSS/JS) | Banner (bottom) + interstitial on mode switch | Everyone needs a calculator. ASO gold. 2-day build. |
| 2 | **⚡ Bolt QR** | `com.jellybolt.qrscanner` | `TOOLS` | Native (CameraX + ZXing) | Interstitial after each scan + banner | QR scanning = daily use. Camera permission = native build needed. |
| 3 | **⚡ Bolt Timer** | `com.jellybolt.timer` | `TOOLS` | WebView (HTML/CSS/JS) | Banner (bottom) + reward video for custom sounds | Timer/stopwatch/countdown. Gym, cooking, studying. Multiple sessions/day. |

#### 🥈 Tier 2 — Ship Second (Strong Utility, Moderate Effort)

| # | App Name | Package | Category | Build Method | Ad Strategy | Why |
|---|----------|---------|----------|-------------|-------------|-----|
| 4 | **⚡ Bolt Notes** | `com.jellybolt.notes` | `PRODUCTIVITY` | WebView (localStorage) | Banner + interstitial every 5th note open | Sticky utility. Users who adopt a notes app KEEP it. |
| 5 | **⚡ Bolt Convert** | `com.jellybolt.converter` | `TOOLS` | WebView (HTML/CSS/JS) | Banner + interstitial on category switch | Unit converter (length, weight, temp, currency). High search volume. |
| 6 | **⚡ Bolt Compass** | `com.jellybolt.compass` | `TOOLS` | Native (sensors) | Banner + interstitial on close | Compass + level + flashlight. Uses device sensors = native. |

#### 🥉 Tier 3 — Ship Third (Niche, Higher Effort)

| # | App Name | Package | Category | Build Method | Ad Strategy | Why |
|---|----------|---------|----------|-------------|-------------|-----|
| 7 | **⚡ Bolt WiFi** | `com.jellybolt.wifianalyzer` | `TOOLS` | Native (WiFi APIs) | Banner + interstitial | WiFi analyzer. Niche but dedicated audience. Requires native APIs. |
| 8 | **⚡ Bolt Weather** | `com.jellybolt.weather` | `WEATHER` | WebView + API | Banner + interstitial | Weather widget. Needs API key (OpenWeatherMap free tier). |
| 9 | **⚡ Bolt Files** | `com.jellybolt.filemanager` | `TOOLS` | Native (Storage APIs) | Banner + interstitial | File manager lite. Complex permissions but high retention. |
| 10 | **⚡ Bolt Battery** | `com.jellybolt.battery` | `TOOLS` | Native (BatteryManager) | Banner + notification ads | Battery stats widget. Ongoing background presence = constant impressions. |

### Naming Convention

All utility apps follow the **"Bolt [Function]"** pattern:
- **Bolt Calculator**, **Bolt QR**, **Bolt Timer**, **Bolt Notes**, etc.
- Publisher remains "JellyBolt Games" on Play Store
- Package: `com.jellybolt.{function}`
- Icon: ⚡ bolt motif + function-specific icon element (e.g., bolt + magnifying glass for QR)

### Utility App Tech Stack

| Approach | When to Use | Examples |
|----------|-------------|---------|
| **WebView (reuse game wrapper)** | No hardware APIs needed. Pure UI + logic. | Calculator, Timer, Notes, Unit Converter |
| **Native Android (Java/Kotlin)** | Needs camera, sensors, WiFi, filesystem | QR Scanner, Compass, WiFi Analyzer, File Manager |
| **WebView + Native bridge** | Mostly UI but one native feature | Weather (API call), Battery (BatteryManager) |

WebView utility apps reuse the exact same `GameActivity.java` WebView wrapper from our game apps — just point it at a different HTML file. Same build system, same signing, same CI/CD pipeline. Ship in hours.

### Visual Design — JellyBolt Utility Style

Utilities get the JellyBolt neon treatment but with a **functional twist**:

```
┌──────────────────────────────────────┐
│ ⚡ Bolt Calculator                   │
│ ──────────────────────────────────── │
│                                      │
│           1,234.56                   │  ← White text on dark (#050510)
│                                      │
│  ┌────┬────┬────┬────┐              │
│  │ 7  │ 8  │ 9  │ ÷  │              │  ← Dark cards (#0d0d24)
│  ├────┼────┼────┼────┤              │     with neon green borders
│  │ 4  │ 5  │ 6  │ ×  │              │
│  ├────┼────┼────┼────┤              │
│  │ 1  │ 2  │ 3  │ −  │              │
│  ├────┼────┼────┼────┤              │
│  │ 0  │ .  │ =  │ +  │              │  ← = button: neon green (#00ff88)
│  └────┴────┴────┴────┘              │
│                                      │
│ ┌──────────────────────────────────┐ │
│ │           [ Ad Banner ]          │ │  ← AdMob banner
│ └──────────────────────────────────┘ │
└──────────────────────────────────────┘
```

- Dark background (`#050510`) — same as games
- Neon green accents (`#00ff88`) — primary action buttons
- Electric cyan (`#58a6ff`) — secondary actions
- Functional first, beautiful second — utilities must feel fast and reliable
- ⚡ bolt icon in app bar

### Revenue Projections — Utility Apps

| Metric | Per Utility App (Month 6) | 10 Utility Apps Combined |
|--------|--------------------------|--------------------------|
| Daily Active Users | 500–2,000 | 5,000–20,000 |
| Sessions/day/user | 3–5 | 3–5 |
| Ad impressions/day | 1,500–10,000 | 15,000–100,000 |
| eCPM (banner) | $1–3 | $1–3 |
| eCPM (interstitial) | $5–15 | $5–15 |
| **Monthly revenue** | **$50–300** | **$500–3,000** |

Not game-changing individually, but **extremely low effort to maintain** once built. And every utility app cross-promotes the games.

---

## 6. 📅 Implementation Roadmap

### ⚡ Phase 1 — Standalone Flagships (Week 1–2) `← WE ARE HERE`

- [x] Created 3 standalone app scaffolds (Dungeon Bolt, Quest RPG, Escape Room)
- [x] Built signed AABs (~1.8 MB each)
- [ ] Create store listings (listing.md + metadata.json) for all 3
- [ ] Generate feature graphics and icons in JellyBolt neon style
- [ ] Upload AABs to Play Console as internal testing
- [ ] Promote BrainRot Quiz + Code Conquest: internal → closed testing

### ⚡ Phase 2 — JellyBolt Core SDK (Week 3–4)

- [ ] Build `jellybolt-core.js` (XP, ⚡coins, streaks, achievements)
- [ ] Design branded game-over overlay (neon style, bolt motif)
- [ ] Integrate into Dungeon Bolt, Quest RPG, BrainRot Quiz first
- [ ] Add "one more round" hooks to game-over flow
- [ ] Roll out to all 40 games
- [ ] Rebuild and re-upload all AABs with JellyBolt Core

### ⚡ Phase 2A — 💎 Gem Storm Flagship (Week 3–6) `🔴 HIGH PRIORITY`

- [ ] Set up Phaser 3 project structure (`games/gem-storm/`)
- [ ] Implement 8×8 match-3 board with gem swap + cascade logic
- [ ] Build power-up system (Lightning, Bolt, Storm, Mega Storm gems)
- [ ] Design 10 worlds × 10 levels = 100 level definitions (objectives, move limits, star thresholds)
- [ ] Implement level map UI (scrollable world map with progress)
- [ ] Lives system (5 lives, 20-min regen, ad refill, coin refill)
- [ ] Daily challenge level + streak bonus integration (JellyBolt Core)
- [ ] Neon-dark visual polish: gem glow, particle effects, screen-shake, cascades
- [ ] AdMob integration (banner + interstitial between levels + rewarded for extra moves)
- [ ] Create standalone app scaffold (`android-app-gemstorm/`)
- [ ] Build AAB + store listing
- [ ] Add to JellyBolt Collection app
- [ ] Upload to Play Console → fast-track to closed testing

### ⚡ Phase 3 — Portfolio Expansion (Week 5–6)

- [ ] Create standalone apps: Block Storm, Merge Master, Battle Royale
- [ ] Create standalone apps: Tower Defense, Bubble Pop
- [ ] Store listings + icons for all 5
- [ ] Upload to Play Console → closed testing
- [ ] Update itch.io pages with "Available on Google Play" badges
- [ ] Gumroad bundle update: "40+ games"

### ⚡ Phase 3B — Utility Apps Wave 1 (Week 5–6, parallel with Phase 3)

- [ ] Build Bolt Calculator (WebView — reuse game wrapper)
- [ ] Build Bolt Timer (WebView — stopwatch, countdown, intervals)
- [ ] Build Bolt Convert (WebView — unit/currency converter)
- [ ] AdMob integration for all 3 (banner + interstitial)
- [ ] Store listings + neon-dark icons
- [ ] Upload to Play Console → internal testing

### ⚡ Phase 3C — Chart-Inspired Games Wave 1 (Week 5–7)

- [ ] Build ⚡ Bolt Blocks (Canvas — block puzzle, 1–2 days)
- [ ] Build ⚡ Bolt Solitaire (Canvas — Klondike solitaire, 2–3 days)
- [ ] Build ⚡ Bolt Tiles (Canvas — Mahjong solitaire, 2–3 days)
- [ ] Build ⚡ Bolt Color (Canvas — color by number, 2–3 days)
- [ ] Standalone apps + store listings for all 4
- [ ] Add to JellyBolt Collection app

### ⚡ Phase 4 — Next-Gen Games (Week 7–10)

- [ ] Build Neon Drift (Three.js racing)
- [ ] Build Phantom Arena (Phaser 3 action RPG)
- [ ] Build Star Colonies (BabylonJS strategy)
- [ ] Standalone apps for each + store listings
- [ ] Add to JellyBolt Collection app
- [ ] itch.io + Gumroad listings for new games

### ⚡ Phase 4B — Utility Apps Wave 2 (Week 7–10, parallel with Phase 4)

- [ ] Build Bolt QR (Native — CameraX + ZXing library)
- [ ] Build Bolt Notes (WebView — localStorage-backed)
- [ ] Build Bolt Compass (Native — sensor APIs, includes level + flashlight)
- [ ] AdMob integration + store listings
- [ ] Upload to Play Console

### ⚡ Phase 4C — Chart-Inspired Games Wave 2 (Week 8–12)

- [ ] Build ⚡ Neon Dash (Phaser 3 — endless runner, 5–7 days)
- [ ] Build ⚡ Rhythm Bolt (Phaser 3 — rhythm platformer, 5–7 days)
- [ ] Build ⚡ Territory Bolt (Canvas — territory claim, 3–5 days)
- [ ] Standalone apps + store listings for all 3
- [ ] Add to JellyBolt Collection app

### ⚡ Phase 5 — Growth & Revenue (Week 11+)

- [ ] Firebase leaderboard backend → cloud leaderboards
- [ ] Push notifications for daily challenges
- [ ] RevenueCat integration → ⚡coin packs, ad-skip
- [ ] A/B test game-over screens for retention
- [ ] Play Store ASO optimization (screenshots, A/B test descriptions)
- [ ] Cross-promote: TechAI Explained → JellyBolt dev logs
- [ ] Utility apps Wave 3: Bolt WiFi, Bolt Weather, Bolt Files, Bolt Battery
- [ ] Chart-inspired Wave 3: Bolt Spinner, Saboteur
- [ ] Bolt World sandbox prototype (Three.js moonshot)

---

## 7. 🏗️ First 3 Standalone Apps — Technical Details

### App Structure

Each standalone follows the proven WebView wrapper pattern. Single activity, fullscreen, offline-capable:

```
android-app-{game}/
├── build.gradle                       # AGP 8.2.0
├── settings.gradle                    # rootProject.name
├── gradle.properties                  # AndroidX, JVM args
├── gradlew / gradlew.bat              # Gradle 8.2
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── app/
    ├── build.gradle                   # com.jellybolt.{id}, SDK 35
    └── src/main/
        ├── AndroidManifest.xml        # Single fullscreen activity
        ├── java/com/jellybolt/{id}/
        │   └── GameActivity.java      # WebView → file:///android_asset/
        └── assets/games/
            ├── {game-folder}/
            │   └── index.html         # The game itself
            └── shared/
                ├── ads.js             # AdMob integration
                ├── monetization.js    # Support links, cross-promo
                └── games-menu.html    # Back-to-collection link
```

### Built Apps

| App | Directory | Package | AAB Size |
|-----|-----------|---------|---------|
| ⚔️ Dungeon Bolt | `android-app-dungeonbolt/` | `com.jellybolt.dungeonbolt` | 1,866 KB |
| 🗡️ Quest RPG | `android-app-questrpg/` | `com.jellybolt.questrpg` | 1,870 KB |
| 🔐 Escape Room | `android-app-escaperoom/` | `com.jellybolt.escaperoom` | 1,867 KB |

### Build Command

```bash
cd android-app-dungeonbolt
cp /path/to/jellybolt-release.keystore .
./gradlew bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
```

> ⚠️ Keystore is gitignored. In CI/CD, pull from GitHub Actions secrets.

---

## Appendix A: Complete Package Registry

| # | App Title | Package | Play Store | itch.io |
|---|-----------|---------|-----------|---------|
| 1 | JellyBolt Games - Arcade Collection | `com.jellybolt.games` | ✅ Closed testing | — |
| 2 | BrainRot Quiz Battle | `com.jellybolt.brainrotquiz` | ✅ Internal | ✅ Published |
| 3 | Code Conquest | `com.jellybolt.codeconquest` | ✅ Internal | ✅ Published |
| 4 | Dungeon Bolt | `com.jellybolt.dungeonbolt` | 🆕 AAB ready | ✅ Published |
| 5 | Quest RPG | `com.jellybolt.questrpg` | 🆕 AAB ready | — |
| 6 | Escape Room | `com.jellybolt.escaperoom` | 🆕 AAB ready | — |
| 7 | 💎 **Gem Storm** | `com.jellybolt.gemstorm` | 🔴 **Phase 2A** | — |
| 8 | Block Storm | `com.jellybolt.blockstorm` | 📋 Phase 3 | — |
| 8 | Merge Master | `com.jellybolt.mergemaster` | 📋 Phase 3 | — |
| 9 | Battle Royale | `com.jellybolt.battleroyale` | 📋 Phase 3 | — |
| 10 | Tower Defense | `com.jellybolt.towerdefense` | 📋 Phase 3 | — |
| 11 | Bubble Pop | `com.jellybolt.bubblepop` | 📋 Phase 3 | — |
| 12 | Neon Drift | `com.jellybolt.neondrift` | 📋 Phase 4 | — |
| 13 | Phantom Arena | `com.jellybolt.phantomarena` | 📋 Phase 4 | — |
| 14 | Star Colonies | `com.jellybolt.starcolonies` | 📋 Phase 4 | — |
| | **── Chart-Inspired Games ──** | | | |
| 15 | ⚡ Bolt Blocks | `com.jellybolt.boltblocks` | 🔴 Phase 3C | — |
| 16 | ⚡ Bolt Solitaire | `com.jellybolt.boltsolitaire` | 🔴 Phase 3C | — |
| 17 | ⚡ Bolt Tiles | `com.jellybolt.bolttiles` | 📋 Phase 3C | — |
| 18 | ⚡ Bolt Color | `com.jellybolt.boltcolor` | 📋 Phase 3C | — |
| 19 | ⚡ Neon Dash | `com.jellybolt.neondash` | 📋 Phase 4C | — |
| 20 | ⚡ Rhythm Bolt | `com.jellybolt.rhythmbolt` | 📋 Phase 4C | — |
| 21 | ⚡ Territory Bolt | `com.jellybolt.territorybolt` | 📋 Phase 4C | — |
| 22 | ⚡ Bolt Spinner | `com.jellybolt.boltspinner` | 📋 Phase 5 | — |
| 23 | ⚡ Saboteur | `com.jellybolt.saboteur` | 📋 Phase 5 | — |
| 24 | ⚡ Bolt World | `com.jellybolt.boltworld` | ⚪ Moonshot | — |
| | **── Utility Apps ──** | | | |
| 25 | ⚡ Bolt Calculator | `com.jellybolt.calculator` | 📋 Phase 3B | — |
| 26 | ⚡ Bolt Timer | `com.jellybolt.timer` | 📋 Phase 3B | — |
| 27 | ⚡ Bolt Convert | `com.jellybolt.converter` | 📋 Phase 3B | — |
| 28 | ⚡ Bolt QR | `com.jellybolt.qrscanner` | 📋 Phase 4B | — |
| 29 | ⚡ Bolt Notes | `com.jellybolt.notes` | 📋 Phase 4B | — |
| 30 | ⚡ Bolt Compass | `com.jellybolt.compass` | 📋 Phase 4B | — |
| 31 | ⚡ Bolt WiFi | `com.jellybolt.wifianalyzer` | 📋 Phase 5 | — |
| 32 | ⚡ Bolt Weather | `com.jellybolt.weather` | 📋 Phase 5 | — |
| 33 | ⚡ Bolt Files | `com.jellybolt.filemanager` | 📋 Phase 5 | — |
| 34 | ⚡ Bolt Battery | `com.jellybolt.battery` | 📋 Phase 5 | — |

## Appendix B: Why NOT Unity/Godot

Our brand promise is **instant play**. WASM engines destroy that:

| Factor | Our JS Games | Unity WebGL | Godot HTML5 |
|--------|-------------|-------------|-------------|
| Load time | **< 100ms** | 3–10 seconds | 1–5 seconds |
| Game size | 7–65 KB | 15–50 MB | 5–15 MB |
| APK size | ~1.8 MB | ~20 MB+ | ~10 MB+ |
| RAM usage | 5–20 MB | 100–300 MB | 50–150 MB |
| Low-end devices | ✅ Works everywhere | ❌ Crashes on cheap phones | ⚠️ Laggy |
| WebView compat | ✅ Perfect | ❌ SharedArrayBuffer issues | ⚠️ WebGL1 only |

If we ever need AAA-quality native games, we'd build native Android apps — not WebView+WASM hybrids. For now, JavaScript IS our competitive moat.

---

<div align="center">

```
⚡ JellyBolt Games — Fun hits different. ⚡
```

*Built with lightning. Played with thunder.*

</div>
