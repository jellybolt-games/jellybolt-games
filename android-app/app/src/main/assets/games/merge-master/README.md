# 🏆 Merge Master

> A 5×5 merge puzzle game by **JellyBolt Games**. Drag matching items onto each other to merge them into higher tiers — all the way from ⚡ Spark to 🏆 Master!

---

## Gameplay

| Action | How |
|--------|-----|
| **Merge** | Drag an item onto another item of the **same tier** |
| **Chain** | 3+ connected cells of the same tier merge at once (+bonus coins) |
| **Spawn** | After every merge, a tier-1 or tier-2 item drops into a random empty cell |
| **Game Over** | Board is full AND no valid merge remains |

---

## 12 Item Tiers

| # | Emoji | Name    | # | Emoji | Name    |
|---|-------|---------|---|-------|---------|
| 1 | ⚡    | Spark   | 7 | 🌙    | Moon    |
| 2 | 🔥    | Flame   | 8 | ☀️    | Sun     |
| 3 | 💧    | Drop    | 9 | 🌊    | Wave    |
| 4 | 🌿    | Leaf    |10 | 🌋    | Volcano |
| 5 | 💎    | Gem     |11 | 🌌    | Cosmos  |
| 6 | ⭐    | Star    |12 | 🏆    | Master  |

---

## Power-ups

| Button | Cost | Effect |
|--------|------|--------|
| 🗑️ Remove | 50 🪙 | Tap any item to delete it from the board |
| 🔀 Shuffle | 100 🪙 | Randomly rearrange all items |
| ➕ Extra Space | 75 🪙 | Adds a 6th column for 30 seconds |

Coins are earned with every merge: **tier × 10**.  
Chain merges award **bonus coins** proportional to the group size.

---

## Scoring

```
Score = maxTierReached × 100  +  mergesCompleted × 10
```

High score and best tier are saved in `localStorage`.

---

## Daily Challenge

- Tap **📅 Daily Challenge** on the menu.
- Every player on the same calendar day gets the **identical starting board** (seeded with `mulberry32(YYYYMMDD)`).
- Your best daily score is stored separately in `localStorage` under key `mm_daily`.

---

## Technical Notes

- **Single file** — `index.html` only; vanilla JS + CSS, `'use strict'`
- **CSS Grid** — 5×5 board (30-cell 5×6 when Extra Space is active)
- **Pointer Events API** — unified mouse + touch drag-and-drop
- **mulberry32** seeded RNG for reproducible daily boards
- JellyBolt shared monetization via `../shared/monetization.js`

---

## File Structure

```
games/merge-master/
├── index.html   ← complete game (single file)
└── README.md    ← this file
```

---

*© JellyBolt Games — [jellyboltgames.itch.io](https://jellyboltgames.itch.io)*
