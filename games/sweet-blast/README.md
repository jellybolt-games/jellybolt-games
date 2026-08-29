# 🍬 Sweet Blast

**Sweet Blast** is a Match-3 puzzle game in the style of Candy Crush Saga, built as a single `index.html` for the JellyBolt Games portfolio.

## Gameplay

Swap adjacent candies to create matches of 3 or more in a row or column. Matched candies burst, new ones fall from the top, and cascades score multiplied combos. Complete all 20 levels by reaching the score target within the move limit!

## Features

- **7×7 grid** — 6 candy types (Red, Blue, Green, Yellow, Purple, Orange)
- **Special candies** created by large matches:
  - 🟠 **Striped H/V** (4-in-a-row) — clears the entire row or column
  - 🔮 **Wrapped** (L/T shape) — explodes a 3×3 area, then a wider ring
  - 💣 **Color Bomb** (5-in-a-row) — destroys all candies of the swapped type
- **Cascade combos** with a ×1.5 score multiplier per chain step
- **20 levels** with escalating score targets and move limits (20–30 moves)
- **3-star rating** per level based on score ratio (1×, 1.5×, 2× target)
- **Progress saved** in `localStorage`

## Controls

| Platform | Action |
|----------|--------|
| Desktop  | Click to select, click adjacent to swap |
| Mobile   | Tap to select, tap adjacent — or swipe directly |

## Technical

- Pure **vanilla JS + Canvas 2D** — zero external dependencies
- Single `index.html` (~1,800 lines)
- `'use strict'` throughout, 22 clearly commented sections
- Responsive canvas: fills available space on any device
- AdSense interstitial every 3 level completions (placeholder slot ID)

## JellyBolt Integration

- JellyBolt top bar branding (⚡)
- AdSense `ca-pub-8350556723025693` with interstitial overlay
- Follows the shared dark-theme design language (`#1a0a2e` background)
