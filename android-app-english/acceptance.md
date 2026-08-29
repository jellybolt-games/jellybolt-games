# JellyBolt English MVP Acceptance Checklist

## Done today means

- [ ] App launches on an Android device/emulator from the `android-app-english` project.
- [ ] UI is Hebrew-first and RTL.
- [ ] English words remain readable left-to-right.
- [ ] Word data is loaded locally from the source-image extraction.
- [ ] No unclear image text is guessed; uncertain entries are flagged for review.
- [ ] Home screen shows game title, score/stars, settings, and four Hebrew mode buttons: "בחירה", "דיבור", "מספרים", "אותיות".
- [ ] Settings support choices count 3/4/5, sound on/off, and words per set 5/10/20.
- [ ] Multiple Choice mode randomly selects words and shows English word + audio + Hebrew options.
- [ ] Multiple Choice mode validates the selected Hebrew translation correctly.
- [ ] Speak mode says the English word with `expo-speech`.
- [ ] Speak mode records speech input using `expo-av` and validates through STT on a tested Android device.
- [ ] If STT is unavailable, the app clearly reports that Speak mode needs speech recognition setup and does not fake correctness.
- [ ] Numbers data includes exactly 0-12 with digit value and English word (`zero` through `twelve`).
- [ ] Numbers Mode supports listen-to-digit: TTS says an English number and 4 digit buttons validate correctly.
- [ ] Numbers Mode supports digit-to-word: prompt shows a digit and 4 English number-word buttons validate correctly.
- [ ] Letter Match data includes exactly 16 worksheet letters: `A`, `B`, `C`, `D`, `E`, `G`, `H`, `I`, `J`, `L`, `M`, `N`, `O`, `P`, `S`, `T`.
- [ ] Letter Match excludes `F`, `K`, `Q`, `R`, `U`, `V`, `W`, `X`, `Y`, `Z` from prompts and distractors.
- [ ] Letter Match supports uppercase-to-lowercase and lowercase-to-uppercase variants.
- [ ] Numbers and Letter Match reuse ChoiceRound-like layout and the shared Round result / End-of-set summary patterns.
- [ ] Numbers and Letter Match always show four answer buttons, regardless of the vocabulary choice-count setting.
- [ ] Round result screen shows correct/try-again feedback and the right answer (Hebrew translation, digit/number word, or letter pair).
- [ ] Mistakes are gentle: no lives, no punishment, no negative scoring.
- [ ] End-of-set summary shows stars earned, words/items practiced, and best streak.
- [ ] Zivvy mascot, JellyBolt palette, confetti/chime, and streak feedback are shared across all modes.
- [ ] Confetti/chime or equivalent joyful feedback appears for correct answers.
- [ ] Sound-off setting mutes TTS/chimes/celebrations in Choice, Speak, Numbers, and Letter Match.
- [ ] No login, name, email, location, contact access, or persistent audio storage.
- [ ] No ads, purchases, gambling, loot boxes, or pay-to-win in MVP.
- [ ] All visual/audio assets are original, generated for JellyBolt, or permissively licensed and documented.

## Quick playtest pass

- [ ] A 5-word Multiple Choice set can be completed in ~60-90 seconds.
- [ ] A 5-round Numbers set and a 5-round Letter Match set can each be completed quickly without new navigation screens.
- [ ] A child can understand what to tap without reading English instructions.
- [ ] At least one wrong answer path shows encouragement and the right solution.
- [ ] Restarting a set works without restarting the app.
- [ ] The app remains usable with sound off.
