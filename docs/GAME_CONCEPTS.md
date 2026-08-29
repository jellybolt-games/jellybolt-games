# 🎮 JellyBolt Games — Game Concepts

## Game 1: BrainRot Quiz Battle
**Repo:** [jellybolt-games/brainrot-quiz-battle](https://github.com/jellybolt-games/brainrot-quiz-battle)
**Genre:** Multiplayer Trivia / Quiz Battle
**Status:** 🟡 In Development

### Concept
Real-time multiplayer trivia game where players battle in quiz rounds. AI-generated questions keep content fresh and infinite. "BrainRot" theme appeals to Gen-Z/Alpha humor and TikTok culture.

### Core Loop
1. Match with opponent (or play solo)
2. Answer trivia questions (10 per round, timed)
3. Earn XP + coins for correct answers
4. Climb leaderboards
5. Unlock cosmetics with coins

### Monetization
- **Cosmetics:** Avatar skins, name effects, victory animations
- **Battle Pass:** Seasonal pass with exclusive cosmetics (₪20/season)
- **Rewarded Ads:** Watch ad → bonus coins
- **Premium Question Packs:** Themed question sets (₪5-10)

### Tech Stack
- React Native + Expo (Android)
- Supabase Realtime (multiplayer matchmaking)
- OpenAI API (AI question generation)
- Firebase Analytics

### Estimated Build Time
- **MVP:** Already in development (4-6 weeks from start)
- **Play Store Launch:** 8-10 weeks total

---

## Game 2: Bounce Blitz ⚡
**Repo:** [jellybolt-games/bounce-blitz](https://github.com/jellybolt-games/bounce-blitz)
**Genre:** One-Tap Arcade / Endless Runner
**Status:** 🔵 Design Phase

### Concept
A hyper-casual one-tap bouncing ball game. Tap to bounce, avoid obstacles, collect coins. Simple as Flappy Bird but with more style — procedurally generated levels, increasing speed, and addictive "one more try" gameplay.

**TikTok Viral Hook:** "Can you pass Level 100? 99% of players can't!"

### Core Loop
1. Tap to bounce
2. Navigate through procedurally generated obstacles
3. Collect coins and power-ups
4. Beat your high score / climb daily leaderboard
5. Unlock ball skins and trail effects

### Game Mechanics
- **One-tap control:** Tap anywhere to bounce
- **Procedural levels:** Infinite unique levels generated from seed
- **Speed increase:** Gets faster every 10 levels
- **Daily challenges:** Specific seed + modifiers for competitive play
- **Combo system:** Chain perfect bounces for multiplier

### Monetization
- **Ball Skins:** 50+ unique designs (neon, galaxy, food, animals) — ₪5-15
- **Trail Effects:** Particle trails behind the ball — ₪5-10
- **Battle Pass:** Weekly challenges + exclusive skins (₪15/season)
- **Rewarded Ads:** Watch ad → continue from checkpoint (1 per game)
- **Remove Ads:** One-time ₪20 purchase

### Tech Stack
- React Native + Expo (Android)
- React Native Game Engine (2D physics)
- Supabase (leaderboards, user profiles)
- Firebase Analytics + Remote Config
- RevenueCat (IAP)

### Why This Will Work
- **Ultra-simple mechanics** — 5 seconds to learn
- **Infinite replayability** — procedural generation
- **Perfect for short sessions** — bus rides, waiting rooms
- **Highly shareable** — screenshot/record high scores for TikTok
- **Low dev cost** — 1 developer, 2-4 weeks to MVP

### Estimated Build Time
- **Prototype:** 1-2 weeks
- **MVP (Play Store ready):** 3-4 weeks
- **Polish + Monetization:** 5-6 weeks total

---

## Game 3: Idle Critter Farm
**Repo:** [jellybolt-games/idle-critter-farm](https://github.com/jellybolt-games/idle-critter-farm)
**Genre:** Idle / Merge / Collection
**Status:** 🔵 Design Phase

### Concept
An idle game where you collect, merge, and evolve adorable original critters on your farm. Critters automatically earn coins while you're away. Merge two identical critters to evolve them into a new, rarer species. Discover all 100+ critter species.

**Hebrew + English** — dual language from day one for Israeli market advantage.

### Core Loop
1. Critters produce coins automatically (idle income)
2. Spend coins to buy new critter eggs
3. Merge two same-tier critters → evolve to next tier
4. Discover new species (collection / album style)
5. Unlock new farm areas
6. Come back later — critters earned coins while you were away!

### Game Mechanics
- **Idle income:** Critters earn coins 24/7, even when app closed
- **Merge system:** Drag & drop same critters to merge → evolve
- **Evolution tree:** 10 tiers x 10+ species per tier = 100+ critters
- **Farm expansion:** Unlock meadow, forest, ocean, volcano, cloud areas
- **Daily rewards:** Log in daily for free critter eggs
- **Mini-games:** Quick tap games for bonus coins (30-second bursts)
- **Hebrew support:** Full RTL UI, Hebrew critter names, dual language toggle

### Monetization
- **Premium Critters:** Exclusive species not available for free (₪10-25)
- **Critter Eggs Bundle:** Buy egg packs to speed up collection (₪5-15)
- **VIP Farm Pass:** 2x idle income + exclusive area (₪15/month)
- **Battle Pass:** Seasonal content with exclusive critters (₪20/season)
- **Rewarded Ads:** Watch ad → speed up time / bonus eggs
- **Remove Ads:** One-time ₪25 purchase

### Design Rules
- **No critter is locked behind gambling** — premium critters are direct purchase
- **Egg packs are NOT loot boxes** — contents are shown before purchase
- **VIP is a time-saver, not required** — free players can unlock everything eventually
- **All critters are ORIGINAL designs** — no copyrighted characters

### Tech Stack
- React Native + Expo (Android)
- React Native Reanimated (smooth animations)
- Supabase (save data, leaderboards, cross-device sync)
- Firebase Analytics + Remote Config
- RevenueCat (IAP + subscriptions)
- i18n (Hebrew + English)

### Estimated Build Time
- **Prototype:** 2-3 weeks
- **MVP (Play Store ready):** 5-6 weeks
- **Polish + Full content:** 8 weeks total

---

## Portfolio Strategy

| Metric | BrainRot Quiz | Bounce Blitz | Idle Critter Farm |
|--------|--------------|-------------|------------------|
| **Time to MVP** | 4-6 weeks | 2-4 weeks | 5-6 weeks |
| **Build Complexity** | Medium | Low | Medium |
| **Viral Potential** | High (multiplayer) | Very High (shareable) | Medium (word of mouth) |
| **Retention** | Medium (needs content) | Low-Medium (casual) | Very High (idle + collection) |
| **Revenue/User** | Medium | Low-Medium | High (subscriptions) |
| **Hebrew Market** | English-first | English-first | Hebrew + English |

### Launch Order
1. **Bounce Blitz** — fastest to build, test monetization pipeline
2. **BrainRot Quiz Battle** — already in development, launch second
3. **Idle Critter Farm** — highest revenue potential, launch third
