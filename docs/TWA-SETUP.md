# TWA (Trusted Web Activity) Setup — JellyBolt Games

## Canonical URL Decision

**Canonical host:** `https://jellyboltgames.itch.io`

This is the authoritative origin for all JellyBolt Games TWA apps. The Android
TWA wrapper loads game content from this origin.

## Why jellyboltgames.itch.io?

- Existing itch.io presence with published games
- No additional hosting infrastructure required
- HTTPS enforced by default on itch.io
- Consistent with current distribution strategy

## assetlinks.json

The Digital Asset Links file lives at:

```
docs/.well-known/assetlinks.json
```

It is served via GitHub Pages at:

```
https://jellybolt-games.github.io/jellybolt-games/.well-known/assetlinks.json
```

**⚠️ Action required:** Update `PLACEHOLDER_FINGERPRINT` in `assetlinks.json`
once the release keystore is generated (see `PLAY-STORE-SIGNING.md`).

### Getting the SHA-256 fingerprint

```bash
keytool -list -v -keystore android-app/jellybolt-release.keystore \
  -alias jellybolt -storepass <password>
```

Copy the `SHA256:` line from the output (format: `AA:BB:CC:...:ZZ`) and
replace `PLACEHOLDER_FINGERPRINT` in `assetlinks.json`.

## TWA Configuration

The Android app in `android-app/` is a TWA pointing to:
- **Dungeon Bolt:** `https://jellyboltgames.itch.io/dungeon-bolt`

Update `android-app/app/src/main/AndroidManifest.xml` to set the TWA launch
URL to match the canonical origin above.
