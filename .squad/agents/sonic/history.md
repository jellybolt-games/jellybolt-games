# Sonic — Project Knowledge

## Project Context
JellyBolt Games — Free mobile games studio for kids & teens.
Stack: React Native, Expo, TypeScript, Supabase
Owner: tdsquadAI Team

## Tech Stack
- **Framework:** React Native + Expo
- **Language:** TypeScript
- **Game Engine:** React Native Game Engine
- **Build System:** EAS Build (Expo)
- **Target:** Android only (no iOS)

## Performance Targets
- 60 FPS on low-end Android devices
- < 50 MB app size per game
- < 3 second cold start time

## Learnings

### Tech Stack & Architecture (seeded 2026-03-22)
- **Mobile framework:** React Native + Expo (TypeScript) — not yet implemented for games, separate from HTML5 games
- **Build system:** EAS Build for Android APK generation and Play Store distribution
- **Game engine:** React Native Game Engine (when mobile versions launch)
- **Current status:** Browser games (HTML5 Canvas) are mature; mobile apps are in planning phase
- **Performance targets:** 60 FPS on low-end Android devices, <50 MB per app, <3 second cold start
- **Build infrastructure:** GitHub Actions CI/CD already configured (ci.yml, pages-deploy.yml)
- **Next technical priorities:** Migrate first game (Bounce Blitz) to React Native, establish EAS Build pipeline, test on Android devices, implement RevenueCat billing in mobile version

