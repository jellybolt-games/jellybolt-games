# 2026-06-05 — JellyBolt Word Sparks web deploy

- **Decision:** Use Expo web export deployed from the public repository `jellybolt-games/jellybolt-word-sparks` on branch `gh-pages` as the no-install Android fallback.
- **Rationale:** Native APK build was blocked by Windows C++ issues; GitHub Pages provides an immediate playable URL for kids tonight.
- **Deployment:** `dist-web` was exported from `android-app-english`, asset paths were made relative to `./_expo/`, `.nojekyll` was added, and commit `e390dc1` was pushed to `origin/gh-pages`.
- **Verification:** `https://jellybolt-games.github.io/jellybolt-word-sparks/` returned HTTP 200 and the referenced JS bundle returned HTTP 200 with JavaScript content.
- **Decided by:** JellyBolt Games; executed by Sonic (data).

Referenced paths verified: `.squad/agents/sonic/charter.md`, `.squad/decisions.md`, `.squad/decisions/inbox/`.
