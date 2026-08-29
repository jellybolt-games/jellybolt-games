# English Game Scaffold Decision — 2026-06-05T14:43:07+03:00

- **Decision:** Bootstrap `android-app-english` as a managed Expo + React Native + TypeScript app using Expo Router.
- **Rationale:** Studio decision log already standardizes on React Native + Expo, and Expo Router is the current router used by the latest Expo template. This keeps the app runnable quickly while preserving Android-only packaging through `games.jellybolt.english`.
- **Implementation notes:** Hebrew RTL is enabled at runtime with `I18nManager.forceRTL(true)` and during Android prebuild with a small local config plugin that sets `android:supportsRtl="true"`.
- **Uncertainty flagged:** Sibling Android apps in this worktree are native Gradle/WebView apps and do not contain `package.json` or `app.json`; Expo versions were therefore pinned from `create-expo-app@latest` on npm, not from siblings.
- **Decided by:** Sonic (data) for HQ #3729 scaffold.
