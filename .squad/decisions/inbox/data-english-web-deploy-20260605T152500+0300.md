# 2026-06-05 — JellyBolt Word Sparks web deploy

- **Decision:** Use Expo web export deployed from the public repository `jellybolt-games/jellybolt-word-sparks` on branch `gh-pages` as the no-install Android fallback.
- **Rationale:** Native APK build was blocked by Windows C++ issues; GitHub Pages provides an immediate playable URL for kids tonight.
- **Deployment:** `dist-web` was exported from `android-app-english`, asset paths were made relative to `./_expo/`, `.nojekyll` was added, and the build was pushed to the `gh-pages` branch of the brand org repository.
- **Migration (2026-07-28):** The site was re-hosted under the JellyBolt Games org so the public URL carries no operator identity. Canonical URL is now `https://jellybolt-games.github.io/jellybolt-word-sparks/`.
- **Verification:** `https://jellybolt-games.github.io/jellybolt-word-sparks/` returned HTTP 200 and the referenced JS bundle returned HTTP 200 with `application/javascript`.
- **Decided by:** JellyBolt Games; executed by Sonic (data).

Referenced paths verified: `.squad/agents/sonic/charter.md`, `.squad/decisions.md`, `.squad/decisions/inbox/`.
