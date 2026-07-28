# Rush Lanes — JellyBolt Games

A **2D Endless Runner** with neon/futuristic city tunnel aesthetics, built as a single `index.html` using pure vanilla JS and Canvas 2D.

## Gameplay

Run through a neon city tunnel across **3 lanes**. Dodge obstacles, collect coins, and activate power-ups to survive as long as possible. Speed increases every 500 meters!

### Controls

| Action | Keyboard | Mobile Swipe |
|--------|----------|-------------|
| Switch Left | ← / A | Swipe Left |
| Switch Right | → / D | Swipe Right |
| Jump | ↑ / W | Swipe Up |
| Roll/Slide | ↓ / S | Swipe Down |
| Pause | Esc / P | — |

### Obstacles

| Type | How to Avoid |
|------|-------------|
| **Barrier** (full lane) | Switch lane |
| **Low Barrier** | Jump over OR switch lane |
| **Overhead Bar** | Roll/slide under |
| **Double Wall** (2 lanes blocked) | Move to open lane |

### Power-Ups

| Icon | Name | Duration | Effect |
|------|------|----------|--------|
| 🧲 | Magnet | 5s | Auto-collects nearby coins |
| 🛡️ | Shield | 5s | Absorbs one hit |
| ⚡×2 | Score Multiplier | 8s | Double all score gains |
| 🚀 | Jetpack | 4s | Fly above all obstacles |

## Technical Details

- **Engine:** Pure vanilla JS, Canvas 2D API, `'use strict'`
- **Loop:** `requestAnimationFrame` with delta-time capped at 50ms
- **Architecture:**
  - `GameLoop` — master update/render loop, state machine
  - `Player` — lane sliding (ease-in-out), jump arc, roll, power-up states
  - `ObstacleManager` — typed obstacles with perspective-aware rendering
  - `CoinManager` — line & zigzag spawn patterns, magnet physics
  - `PowerUpManager` — four types, exclusive activation
  - `Renderer` — screen shake, flash overlay, HUD updates
  - `Background` — parallax building layers + pseudo-3D track perspective
  - `ParticleSystem` — sparkle/crash effects
  - `Input` — keyboard + touch swipe detection
- **Storage:** `localStorage` for best score and total coins
- **Ads:** JellyBolt AdSense banners + interstitial every 3 deaths

## Files

```
games/rush-lanes/
├── index.html   — complete self-contained game
└── README.md    — this file
```

## Building / Running

Open `index.html` in any modern browser — no build step required.

---

*© JellyBolt Games — [jellyboltgames.com](https://jellybolt-games.github.io/jellybolt-games)*