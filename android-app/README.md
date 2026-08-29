# JellyBolt Games - Android App

## Building

### Prerequisites
- Java 17+
- Android SDK (auto-downloaded by Gradle)

### Build Debug APK
```bash
cd android-app
./gradlew assembleDebug
```

The APK will be in `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK
```bash
./gradlew assembleRelease
```

## Architecture
- **MainActivity**: Game launcher menu listing all 18 JellyBolt games
- **GameActivity**: WebView wrapper that loads games from GitHub Pages
- Games URL: `https://jellybolt-games.github.io/jellybolt-games/games/{game-id}/index.html`

## Publishing
Upload the signed APK to Google Play Console under "JellyBolt Games - Arcade Collection"
