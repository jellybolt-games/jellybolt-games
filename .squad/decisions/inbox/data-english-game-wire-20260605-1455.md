# English game wire-up — 2026-06-05 14:55 +03:00

## Context
- [KNOWN] Speed-priority Expo Go target for `android-app-english` ahead of the June 7 English test.
- [KNOWN] Design scope came from `android-app-english/design.md`, `acceptance.md`, `art-direction.md`, and `src/data/words.ts`.

## Decision
- [KNOWN] Use the real worksheet data from `src/data/words.ts` through `src/data/wordEntries.ts`; keep the old sample data renamed as `src/data/words.sample.unused`.
- [KNOWN] Add test-day modes as Expo Router routes: `app/numbers.tsx` and `app/letters.tsx`.
- [KNOWN] Persist minimal progress in one AsyncStorage key: `jellybolt-english-state`.
- [KNOWN] Keep Speak mode honest in Expo Go: TTS/practice only, no fake speech validation.

## Verification
Referenced paths verified: `android-app-english/design.md`, `android-app-english/acceptance.md`, `android-app-english/art-direction.md`, `android-app-english/src/data/words.ts`, `android-app-english/app`, `android-app-english/src`.
