# Data English words parsed — 2026-06-05 14:46:38 +03:00

Referenced paths verified:
- `.squad/decisions/inbox/`
- `android-app-english/word-source-images/page-1.jpg`
- `android-app-english/word-source-images/page-2.jpg`
- `android-app-english/word-source-images/page-3.jpg`
- `android-app-english/src/data/words.ts`

## Decision

Transcribed only English words visible in the three provided source images. No per-word Hebrew translations were visible in the images, so every `he` value in `words.ts` is intentionally empty and every entry is flagged with `unclear`.

## Rationale

The task explicitly forbids inventing translations or adding words that are not present in the images.

## Notes

The words `add`, `plus`, and `in` are visible in both page-2 and page-3, so both source-page occurrences were preserved.
