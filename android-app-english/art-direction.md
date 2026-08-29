# English Game Art Direction

## 1. Mood & audience
- **Audience:** 9-year-old Hebrew speaker learning English on Android.
- **Tone:** joyful, clever, encouraging, energetic; feels like a friendly learning quest, not a preschool app.
- **Pressure level:** personal progress only. Avoid scary failure states, timers-as-threats, harsh reds, rankings, or competitive stress.
- **UI language:** Hebrew for instructions and feedback, with English words large, centered, and clearly LTR.
- **Visual feel:** rounded cards, chunky shapes, warm background, bright accents, satisfying micro-rewards.

## 2. Original mascot concept — Zivvy
**Zivvy** is an original "spark-kite" companion: a small floating word buddy shaped like a rounded lightning seed with tiny kite fins and expressive eyes. Zivvy is curious, quick, and loyal; it celebrates correct answers with a proud little bounce, and on mistakes it gently tilts, blinks, and says the Hebrew equivalent of "close one — try again" without shame. Zivvy should feel cool enough for a 9-year-old: playful, not babyish; encouraging, not hyperactive.

- **Species/type:** original spark-kite word companion.
- **Personality:** upbeat teammate, curious, patient, quietly funny.
- **Signature color:** Bolt Blue `#2F6BFF` with Sunny Yellow accents.
- **Correct response:** bounce + sparkle + thumbs-up pose.
- **Wrong response:** soft wobble + sympathetic eyes + retry gesture.
- **Do not:** resemble any existing game/cartoon character or use copyrighted/trademarked traits.

## 3. Palette
| Role | Hex | Use |
|---|---:|---|
| Primary / Zivvy | `#2F6BFF` | main buttons, mascot body, selected cards |
| Success | `#24B26B` | correct state, checkmarks, progress wins |
| Gentle error | `#FF6B6B` | wrong state accents only; pair with kind copy |
| Background | `#FFF8E7` | warm app backdrop |
| Accent / reward | `#FFC857` | stars, streaks, highlights, coins |
| Text | `#1F2A44` | primary readable text on light backgrounds |

Implementation notes: use white cards on `#FFF8E7`; keep text `#1F2A44`; reserve red for small feedback accents, not full-screen failure.

## 4. Typography
- **Display:** `Varela Round` — Google Font, supports Hebrew + Latin, rounded and friendly for headers/buttons.
- **Body:** `Rubik` — Google Font, supports Hebrew + Latin, very readable for instructions, answers, and labels.
- **Fallbacks:** `System`, `Arial`, `sans-serif`.
- **Sizing:** English target words 36–48sp; Hebrew instructions 20–24sp; answers 22–28sp; feedback 24–32sp.

## 5. Screen mockup specs

### Home
- **Layout:** full warm background; top-right Hebrew greeting; centered Zivvy placeholder built from RN circles/ovals; large title card; primary CTA button; smaller progress pill below.
- **Key components:** title "מסע מילים באנגלית"; CTA "מתחילים"; progress pill "רצף: 0"; settings/sound icon as emoji button.
- **Motion notes:** Zivvy floats up/down slowly; CTA scales to 0.96 on press; small stars drift behind title.

### ChoiceRound
- **Layout:** top progress bar and streak pill; center large English word card; below it 2–4 Hebrew answer cards in a vertical stack; Zivvy peeks from bottom corner.
- **Key components:** English word in LTR card; Hebrew prompt "מה הפירוש?"; answer buttons with rounded corners; speaker replay button.
- **Motion notes:** selected card lifts 4px; correct card flashes success then confetti; wrong card gentle wobble, then correct card pulses once.

### SpeakRound
- **Layout:** top progress/streak; center prompt card with Hebrew instruction; large English word or Hebrew meaning; bottom oversized mic button.
- **Key components:** mic button `🎙️`; listen button `🔊`; confidence meter as 3 rounded bars; helper copy "תגיד בקול: ...".
- **Motion notes:** mic button expands with breathing ring while listening; Zivvy leans toward the mic; success stars pop from the mic.

### RoundResult
- **Layout:** modal-like centered card over dimmed warm background; Zivvy large at top; result text; mini explanation; next button.
- **Key components:** correct: "כל הכבוד!"; wrong: "כמעט! ננסה שוב"; show English + Hebrew pair; next CTA.
- **Motion notes:** correct = spring bounce + 8 confetti pieces; wrong = soft side-to-side wobble under 300ms, no harsh shake.

### SetSummary
- **Layout:** celebratory summary card; row of earned stars; stats grid; large continue/home buttons.
- **Key components:** "סיימת סט!"; words learned count; accuracy; streak badge; review missed words list if needed.
- **Motion notes:** stars pop in one-by-one; streak badge glows if streak 3+; Zivvy does a small victory loop.

## 6. Juice list
- **Correct answer:** small confetti burst using RN Views/emoji (`✨`, `⭐`, `🎉`) with random rotation and fade.
- **Wrong answer:** gentle wobble on chosen card; Zivvy empathizes; no buzzer, no punishment.
- **Streak 3+:** show `🔥` streak icon with Sunny Yellow ring and tiny pop animation.
- **Near miss:** reveal correct answer with a soft green pulse and copy "עוד ניסיון אחד".
- **Set complete:** 3–5 stars pop with spring animation and short celebratory sound.
- **Sound cues:** use only free/CC0 sources such as Kenney Interface Sounds, OpenGameArt CC0 UI sounds, or Freesound CC0 clips; track filenames/licenses in an asset credits file. Keep cues short, warm, and low volume.

## 7. Icon + splash placeholder spec
- **Adaptive icon background:** `#FFF8E7`.
- **Foreground concept:** transparent PNG later: rounded Bolt Blue speech bubble with a Sunny Yellow mini-bolt/star and white letter "A". Keep all critical shapes inside Android safe area.
- **Shape language:** one big rounded blob, one small star/bolt, no detailed character until final mascot art.
- **Splash background:** `#FFF8E7`.
- **Splash center placeholder:** simple stacked shapes/text: Bolt Blue rounded pill, Sunny Yellow star, title "English Quest" or Hebrew title in Text. If Expo requires an image, create a simple generated PNG later from these shapes; do not block v1 on final art.

## 8. Asset acquisition plan
### Use emoji + RN shapes for v1 today
- Zivvy placeholder: RN circles/ovals + two Text eyes + small yellow star/bolt shape.
- Rewards: emoji stars/confetti/fire; RN particle rectangles/circles.
- Controls: emoji `🎙️`, `🔊`, `⭐`, `🔥`, `✅` inside rounded buttons.
- Backgrounds: flat warm background, subtle dot/star pattern from small Views.
- Progress: rounded bars, pills, cards, shadows.

### Draw later
- Final Zivvy mascot: neutral, happy, encouraging, thinking, gentle-mistake poses.
- App icon and splash foreground art.
- Reward badges, streak badge, word-set icons.
- Optional illustrated background pattern.
- Sound pack selection and license/credit file.
