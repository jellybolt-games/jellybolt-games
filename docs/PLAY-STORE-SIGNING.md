# Google Play Store Signing — JellyBolt Games

## Keystore Status

A release keystore already exists at `android-app/jellybolt-release.keystore`.

> **⚠️ NEVER commit the keystore to git.** Verify `.gitignore` excludes `*.keystore`.
> Store it securely and back it up — losing it means you can never update the app.

## Keystore Setup (if regenerating)

```bash
keytool -genkey -v -keystore jellybolt-release.keystore \
  -alias jellybolt -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=JellyBolt Games, OU=Mobile, O=JellyBolt Games, L=Unknown, S=Unknown, C=US"
```

## GitHub Secrets (required for CI build)

Add these four secrets in GitHub → Settings → Secrets → Actions:

| Secret name        | Value                                         |
|--------------------|-----------------------------------------------|
| `KEYSTORE_BASE64`  | `base64 -w 0 jellybolt-release.keystore`      |
| `KEYSTORE_PASSWORD`| Keystore password set during keytool creation |
| `KEY_ALIAS`        | `jellybolt`                                   |
| `KEY_PASSWORD`     | Key password (same as keystore password)      |

## Build AAB with GitHub Actions

See `.github/workflows/build-aab.yml` for the automated build workflow.

To trigger manually:
1. Go to **Actions** → **Build Signed AAB**
2. Click **Run workflow**
3. Select game (`dungeon-bolt` or `all`)
4. Download the AAB artifact from the completed run

## Upload to Play Console

1. Go to [play.google.com/console](https://play.google.com/console)
2. Select **com.jellybolt.games**
3. Navigate to **Release** → **Internal testing**
4. Upload the `.aab` artifact
5. Fill in release notes

## SHA-256 Fingerprint for assetlinks.json

After building with the keystore:

```bash
keytool -list -v \
  -keystore android-app/jellybolt-release.keystore \
  -alias jellybolt -storepass <password>
```

Update `docs/.well-known/assetlinks.json` with the SHA256 fingerprint value.
