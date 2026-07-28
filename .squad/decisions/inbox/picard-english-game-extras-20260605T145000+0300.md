# English Game Extras Decision — Numbers + Letters

Date: 2026-06-05T14:50:00+03:00
Decided by: Mario (picard)
Requested by: JellyBolt Games

## Decision

Add two tight test-day modes to JellyBolt Word Sparks:

- **Numbers Mode:** numbers 0-12, with listen-to-digit and digit-to-word variants.
- **Letter Match Mode:** uppercase/lowercase matching for the worksheet letters only.

Home now needs four Hebrew mode buttons: `בחירה`, `דיבור`, `מספרים`, `אותיות`.

## Verified worksheet source

`android-app-english\word-source-images\page-1.jpg` was visually checked. The letter line shows `Aa, Bb, Cc, Dd, Ee, Gg, Hh, Ii, Jj, Ll, Mm, Nn, Oo, Pp, Ss, Tt`.

Therefore prompts and distractors must use exactly these 16 letters and must exclude `F`, `K`, `Q`, `R`, `U`, `V`, `W`, `X`, `Y`, `Z`.

## Rationale

The June 7 English test includes numbers and letter matching in addition to vocabulary. These modes preserve the existing round/result pattern so Sonic can wire them quickly without creating extra screen types.

## Implementation direction

- Update `android-app-english\design.md` with the new test-day section, data shapes, Hebrew strings, and screen/component notes.
- Update `android-app-english\acceptance.md` with checklist coverage for Home, Numbers, Letters, shared feedback, and no extra result screens.
- Reuse Zivvy, JellyBolt palette, confetti, streaks, shared Round result, and shared End-of-set summary.

## Self-verification

Referenced paths verified:

- `.squad\agents\mario\charter.md`
- `.squad\team.md`
- `.squad\decisions.md`
- `.squad\decisions\inbox\`
- `android-app-english\design.md`
- `android-app-english\acceptance.md`
- `android-app-english\word-source-images\page-1.jpg`

No skill paths or policy paths are referenced in this decision. `.squad\mcp-servers.md` is not present, so MCP-server identity cross-check is unavailable; Mario's picard mapping was verified from `.squad\agents\mario\charter.md` and `.squad\team.md`.
