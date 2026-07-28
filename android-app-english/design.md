# JellyBolt English MVP Design

## 1. Game name

Name options:
1. **JellyBolt Word Sparks** — joyful, energetic, clearly JellyBolt-branded.
2. **Bolt Words** — short and simple, but less playful.
3. **Jelly Pop English** — fun, but less direct about learning words.

**Working name: JellyBolt Word Sparks**

Original IP direction: a happy jelly lightning mascot, spark trails, stars, and colorful word cards. No copyrighted characters, brands, music, or art.

## 2. Core loop

Target pace: one word round takes ~10-20 seconds; a 5-word set takes ~60-90 seconds.

1. App randomly picks an English word from the approved word list.
2. App shows the English word, says it aloud, and optionally shows a simple image/icon.
3. Kid answers by choosing Hebrew translation or speaking the English word.
4. App gives warm feedback, shows the right answer, awards stars/streak progress, then moves to the next word.

## 3. Game modes

### A. Multiple Choice mode

- UI language: Hebrew.
- Round content:
  - Big English word card.
  - TTS button using `expo-speech` to replay the English word.
  - Optional image/icon if available.
  - Hebrew answer choices: 3/4/5 based on difficulty setting.
- Correct answer:
  - Confetti burst, cheerful sound, +1 spark/star.
  - Show: English word + Hebrew translation.
- Wrong answer:
  - No punishment and no harsh red failure state.
  - Gentle message: "כמעט! בוא ננסה שוב".
  - Show correct answer after retry or after second miss.
- Distractors:
  - Use other Hebrew translations from the word list.
  - Avoid obviously unrelated duplicates and avoid invented translations.

### B. Speak mode

- Round content:
  - Big English word card.
  - App says the word using `expo-speech`.
  - Microphone button: "תגיד את המילה".
- Input/validation:
  - Use `expo-av` to record audio.
  - Send audio to a speech-to-text adapter for English recognition.
  - Compare normalized transcript to target word.
  - Accept close matches for child pronunciation when confidence is reasonable.
- Privacy requirement:
  - Prefer on-device Android speech recognition where available.
  - Do not store recordings.
  - Do not collect names, accounts, contacts, location, or analytics for MVP.
- Reality/risk note:
  - Expo has TTS via `expo-speech`; speech-to-text may require a compatible STT package or dev build. If STT is unavailable on a test device, Speak mode must say so clearly rather than pretending to validate.

## 4. Progression / juice for a 9-year-old

- **Stars per set:** 1-3 stars based on practice completion, not perfection.
- **Spark streak:** consecutive correct answers light up a bolt trail.
- **Level names:** Spark Starter, Word Zapper, Jelly Champ, Bolt Hero.
- **Celebration:** small confetti, bounce animation, happy chime.
- **Encouragement:** positive Hebrew messages: "מעולה!", "איזה יופי!", "נסה שוב, אתה קרוב!".
- **Mistakes:** no lives, no losing, no shame. A mistake becomes a retry/practice moment.
- **End summary:** show words practiced, stars earned, and one encouraging sentence.

## 5. Minimum viable screens

### Home
- Hebrew title and JellyBolt Word Sparks logo text.
- Mode buttons: "בחירה", "דיבור", "מספרים", "אותיות".
- Current score/stars.
- Settings button.

### Choice mode round
- English word card.
- Speaker replay button.
- Optional image/icon area.
- Hebrew answer buttons.
- Progress indicator: word 2/5, 4/10, etc.

### Speak mode round
- English word card.
- Speaker replay button.
- Microphone hold/tap button.
- Listening state animation.
- Transcript preview only if useful and not distracting.

### Round result
- Correct / try-again state.
- Correct answer detail: English word + Hebrew translation, digit/number word, or letter pair.
- Next button.
- Celebration or gentle retry animation.

### End-of-set summary
- Stars earned.
- Words/items practiced.
- Best streak.
- Buttons: play again, change mode, home.

## 6. RTL layout requirements

- App shell is RTL Hebrew-first.
- Use RTL layout for menus, buttons, settings, score, and instructions.
- Keep English words LTR and visually isolated inside the word card.
- Right-align Hebrew text; center-align big game words.
- Do not mirror speaker/microphone meaning in a confusing way.
- Use large tap targets for a child: minimum ~48dp.
- Avoid tiny text; support Android font scaling.

## 7. Settings

- **Difficulty / number of choices:** 3, 4, or 5.
- **Sound:** on/off for TTS, chimes, and celebration sounds.
- **Words per set:** 5, 10, or 20.
- Store settings locally on device.

## 8. Hard constraints

- Android only.
- Original IP only.
- No copyrighted or trademarked characters, music, art, or names.
- No gambling, loot boxes, betting, or randomized paid rewards.
- No pay-to-win.
- Privacy-first: no PII, no accounts, no backend required for MVP.
- Fully offline if possible; if STT needs OS services, no app-owned audio storage.
- The word list must come from the provided source images or human-approved additions. Do not invent uncertain words/translations.

## MVP cut for today

Ship the smallest joyful version:
1. Hebrew RTL Home.
2. Local word list extracted from the three source images.
3. Multiple Choice mode fully playable.
4. Speak mode playable on Android if STT works; otherwise clear fallback notice.
5. Numbers Mode short sets for 0-12.
6. Letter Match Mode for the worksheet letters only.
7. Stars, streaks, sounds, and end-of-set summary.

## Test-Day Extras (Numbers + Letters)

This is a speed-priority add-on for the June 7 test. Keep it small: two new round components, the existing Home shell, shared result feedback, and the shared end-of-set summary.

### Verified worksheet scope

- **Numbers:** 0-12 only.
- **Letters:** exactly 16 uppercase/lowercase pairs: `A/a`, `B/b`, `C/c`, `D/d`, `E/e`, `G/g`, `H/h`, `I/i`, `J/j`, `L/l`, `M/m`, `N/n`, `O/o`, `P/p`, `S/s`, `T/t`.
- Confirmed visually from `word-source-images/page-1.jpg`, which lists `Aa, Bb, Cc, Dd, Ee, Gg, Hh, Ii, Jj, Ll, Mm, Nn, Oo, Pp, Ss, Tt`.
- Do **not** include `F`, `K`, `Q`, `R`, `U`, `V`, `W`, `X`, `Y`, or `Z` as prompts or distractors.

### Home screen update

Use four Hebrew mode buttons:

1. `בחירה` — Vocab Choice.
2. `דיבור` — Speak.
3. `מספרים` — Numbers.
4. `אותיות` — Letters.

Keep the same Zivvy/JellyBolt header, score/stars, and settings entry.

### Shared juice across all modes

- Reuse Yoshi's Zivvy mascot, JellyBolt palette, rounded cards, spark/streak meter, confetti burst, chime, and warm Hebrew encouragement.
- Correct answers in Numbers and Letters should feel like Choice mode: bounce/confetti, +1 spark/star, streak continuation.
- Wrong answers stay gentle: no lives, no shame, no negative scoring.
- Sound-off setting mutes TTS, chimes, and celebration sounds in all four modes.
- The 3/4/5 choice-count setting stays for vocabulary Choice mode; Numbers and Letters are fixed at four answer buttons to match the test-day spec.

### Numbers Mode (0-12)

**Goal:** short listening/recognition practice for English numbers.

**Round variants:**

1. **Listen -> digit:** TTS says the English number, e.g. `seven`; four buttons show digits, e.g. `3`, `7`, `9`, `5`; kid taps `7`.
2. **Digit -> word:** prompt shows the digit, e.g. `7`; four buttons show English number words, e.g. `three`, `seven`, `nine`, `five`; kid taps `seven`.

**Screen pattern:** mirror ChoiceRound.

- Prompt card:
  - Listen variant: big speaker/Zivvy prompt with `הקשב למספר`.
  - Digit variant: big digit card, e.g. `7`.
- Speaker replay button: `השמע שוב`.
- Four large answer buttons.
- Progress indicator using the existing set counter.
- Shared Round result shows both forms: `7 — seven`.

**Hebrew UI strings:**

| Use | Hebrew |
| --- | --- |
| Home/mode title | `מספרים` |
| Listen prompt | `הקשב למספר ובחר את הספרה` |
| Digit prompt | `איזו מילה מתאימה למספר?` |
| Replay | `השמע שוב` |
| Correct result | `נכון! זה {digit} — {word}` |
| Try again | `כמעט! נסה שוב` |

**Data shape:**

```ts
type NumberRoundVariant = 'listenToDigit' | 'digitToWord';

type NumberItem = {
  id: string;        // number-0 ... number-12
  value: number;     // 0 ... 12
  word: string;      // zero ... twelve
  ttsText: string;   // same as word for MVP
};

const NUMBERS_0_TO_12: NumberItem[] = [
  { id: 'number-0', value: 0, word: 'zero', ttsText: 'zero' },
  { id: 'number-1', value: 1, word: 'one', ttsText: 'one' },
  { id: 'number-2', value: 2, word: 'two', ttsText: 'two' },
  { id: 'number-3', value: 3, word: 'three', ttsText: 'three' },
  { id: 'number-4', value: 4, word: 'four', ttsText: 'four' },
  { id: 'number-5', value: 5, word: 'five', ttsText: 'five' },
  { id: 'number-6', value: 6, word: 'six', ttsText: 'six' },
  { id: 'number-7', value: 7, word: 'seven', ttsText: 'seven' },
  { id: 'number-8', value: 8, word: 'eight', ttsText: 'eight' },
  { id: 'number-9', value: 9, word: 'nine', ttsText: 'nine' },
  { id: 'number-10', value: 10, word: 'ten', ttsText: 'ten' },
  { id: 'number-11', value: 11, word: 'eleven', ttsText: 'eleven' },
  { id: 'number-12', value: 12, word: 'twelve', ttsText: 'twelve' },
];
```

**Distractors:** choose three other entries from `NUMBERS_0_TO_12`; never use values below 0 or above 12.

### Letter Match Mode

**Goal:** uppercase/lowercase recognition for the worksheet letters only.

**Round variants:**

1. **Uppercase -> lowercase:** show `G`; four lowercase buttons, e.g. `g`, `p`, `b`, `m`; kid taps `g`.
2. **Lowercase -> uppercase:** show `g`; four uppercase buttons, e.g. `G`, `P`, `B`, `M`; kid taps `G`.

**Screen pattern:** mirror ChoiceRound.

- Big prompt card with one letter centered.
- Optional speaker button: `השמע אות`, pronouncing the letter name/sound only when a real TTS/audio mapping exists.
- Four large answer buttons.
- Progress indicator using the existing set counter.
- Shared Round result shows the pair: `G ↔ g`.

**Hebrew UI strings:**

| Use | Hebrew |
| --- | --- |
| Home/mode title | `אותיות` |
| Upper -> lower prompt | `מצא את האות הקטנה` |
| Lower -> upper prompt | `מצא את האות הגדולה` |
| Replay letter | `השמע אות` |
| Correct result | `נכון! {upper} מתאימה ל-{lower}` |
| Try again | `כמעט! נסה שוב` |

**Data shape:**

```ts
type LetterRoundVariant = 'upperToLower' | 'lowerToUpper';

type LetterItem = {
  id: string;      // letter-a, letter-b, ...
  upper: string;   // A, B, C...
  lower: string;   // a, b, c...
  ttsText: string; // MVP: uppercase letter for OS TTS letter name
};

const WORKSHEET_LETTERS: LetterItem[] = [
  { id: 'letter-a', upper: 'A', lower: 'a', ttsText: 'A' },
  { id: 'letter-b', upper: 'B', lower: 'b', ttsText: 'B' },
  { id: 'letter-c', upper: 'C', lower: 'c', ttsText: 'C' },
  { id: 'letter-d', upper: 'D', lower: 'd', ttsText: 'D' },
  { id: 'letter-e', upper: 'E', lower: 'e', ttsText: 'E' },
  { id: 'letter-g', upper: 'G', lower: 'g', ttsText: 'G' },
  { id: 'letter-h', upper: 'H', lower: 'h', ttsText: 'H' },
  { id: 'letter-i', upper: 'I', lower: 'i', ttsText: 'I' },
  { id: 'letter-j', upper: 'J', lower: 'j', ttsText: 'J' },
  { id: 'letter-l', upper: 'L', lower: 'l', ttsText: 'L' },
  { id: 'letter-m', upper: 'M', lower: 'm', ttsText: 'M' },
  { id: 'letter-n', upper: 'N', lower: 'n', ttsText: 'N' },
  { id: 'letter-o', upper: 'O', lower: 'o', ttsText: 'O' },
  { id: 'letter-p', upper: 'P', lower: 'p', ttsText: 'P' },
  { id: 'letter-s', upper: 'S', lower: 's', ttsText: 'S' },
  { id: 'letter-t', upper: 'T', lower: 't', ttsText: 'T' },
];
```

**TTS note:** use OS TTS for the letter name in MVP. If real phonics audio is added later, map it explicitly per letter; do not guess sounds.

**Distractors:** choose three other letters from `WORKSHEET_LETTERS`; do not use excluded letters even if they look like good visual distractors.

### Screen list addition for Sonic

- Add `NumbersRound` using ChoiceRound's layout contract: prompt card, replay button, four answer buttons, progress, submit/select handler.
- Add `LetterMatchRound` using the same layout contract.
- Do **not** create separate result or summary screens. Use the existing Round result and End-of-set summary patterns.
- The navigation/home mode enum should now cover: `choice`, `speak`, `numbers`, `letters`.

### Summary for Sonic (≤200 words)

Letter scope is exactly 16 worksheet pairs: `A/a`, `B/b`, `C/c`, `D/d`, `E/e`, `G/g`, `H/h`, `I/i`, `J/j`, `L/l`, `M/m`, `N/n`, `O/o`, `P/p`, `S/s`, `T/t`; exclude `F`, `K`, `Q`, `R`, `U`, `V`, `W`, `X`, `Y`, `Z` everywhere. Numbers data is `NumberItem { id, value: 0-12, word: zero-twelve, ttsText }`; letters data is `LetterItem { id, upper, lower, ttsText }`. Add two round screens/components only: `NumbersRound` and `LetterMatchRound`, both mirroring ChoiceRound; reuse shared Round result and End-of-set summary. Home buttons become `בחירה`, `דיבור`, `מספרים`, `אותיות`. Gotchas: Numbers/Letters fixed at four choices; keep English words/letters LTR inside RTL UI, never use out-of-scope letter distractors, sound-off must mute TTS, and use OS TTS letter names unless explicit phonics audio is provided.
