# JellyBolt English

## 🚀 To play tonight

1. `cd android-app-english`
2. `npm install` (only if this is the first time on this machine)
3. `npx expo start --tunnel`
4. Install **Expo Go** from the Play Store on the kid's Android phone
5. Scan the QR code with Expo Go → the game opens instantly

Fallback: if tunnel is slow, run `npx expo start` and scan from a phone on the same WiFi.

Expo + React Native Android game for JellyBolt English word practice.

## What works now

- Hebrew RTL home screen with four modes: `בחירה`, `דיבור`, `מספרים`, `אותיות`.
- Choice mode uses `src/data/words.ts` real worksheet words, TTS, 4 Hebrew options, 10 rounds per set.
- Numbers mode covers exactly 0–12 with listen-to-digit and digit-to-word rounds.
- Letters mode covers exactly `A B C D E G H I J L M N O P S T`, with upper/lower matching only.
- Round result + set summary are shared across modes, with Zivvy, haptics, persisted stars/streak, and simple RN confetti.
- Speak mode uses real words and TTS, but clearly states that Expo Go does not validate speech automatically.

## Expo notes

- Expo SDK is `~56.0.8`.
- `expo-av` is configured through `app.json` for future microphone/audio work.
- `expo-speech` speaks English at a child-friendly rate in `src/audio/tts.ts`.
- Local progress uses AsyncStorage key `jellybolt-english-state`.

## Later

1. Wire real Android speech-to-text if a dev build is allowed.
2. Add final Zivvy art/icon/splash and licensed sound cues.
3. If Q sends translation fixes, update `src/data/words.ts` only.
