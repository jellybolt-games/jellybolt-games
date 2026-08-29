# Merge Master

> A 5x5 merge puzzle game by **JellyBolt Games**. Drag matching items onto each other to merge them into higher tiers -- all the way from Spark to Master!

## Gameplay

| Action | How |
|--------|-----|
| **Merge** | Drag an item onto another item of the **same tier** |
| **Chain** | 3 or more connected cells of the same tier merge at once (+bonus coins) |
| **Spawn** | After every merge, a tier-1 or tier-2 item drops into a random empty cell |
| **Game Over** | Board is full AND no valid merge remains |

## 12 Item Tiers

| # | Emoji | Name    | # | Emoji | Name    |
|---|-------|---------|---|-------|---------|
| 1 | Spark  (lightning) | Spark   | 7 | Moon  | Moon    |
| 2 | Flame  | Flame   | 8 | Sun   | Sun     |
| 3 | Drop   | Drop    | 9 | Wave  | Wave    |
| 4 | Leaf   | Leaf    |10 | Volcano | Volcano |
| 5 | Gem    | Gem     |11 | Cosmos | Cosmos  |
| 6 | Star   | Star    |12 | Trophy | Master  |

## Power-ups

| Button | Cost | Effect |
|--------|------|--------|
| Remove | 50 coins | Tap any item to delete it from the board |
| Shuffle | 100 coins | Randomly rearrange all items |
| Extra Space | 75 coins | Adds a 6th column for 30 seconds |

Coins are earned with every merge: **tier x 10**.
Chain merges award **bonus coins** proportional to the group size.

## Scoring

```
Score = maxTierReached x 100  +  mergesCompleted x 10
```

High score and best tier are saved in `localStorage`.

## Daily Challenge

- Tap **Daily Challenge** on the menu.
- Every player on the same calendar day gets the **identical starting board** (seeded with `mulberry32(YYYYMMDD)`).
- Your best daily score is stored separately in `localStorage` under key `mm_daily`.

## Technical Notes

- **Single file** -- `index.html` only; vanilla JS + CSS, `'use strict'`
- **CSS Grid** -- 5x5 board (expands to 5x6 when Extra Space is active)
- **Pointer Events API** -- unified mouse + touch drag-and-drop
- **mulberry32** seeded RNG for reproducible daily boards
- JellyBolt shared monetization via `../shared/monetization.js`
- Chain merge detection uses BFS over the connected component of equal-tier cells

## File Structure

```
games/merge-master/
+-- index.html   <- complete game (single file)
+-- README.md    <- this file
```

---

*JellyBolt Games -- https://jellyboltgames.itch.io*