# AdMob Rewarded Ads — Integration Guide

**Publisher/Developer:** JellyBolt Games  
**Tech Stack:** Vanilla HTML5 / JavaScript (single-file games)

---

## 1. AdMob Account Setup

### Step 1 — Create an AdMob Account
1. Go to [https://admob.google.com](https://admob.google.com)
2. Sign in with the JellyBolt Games Google account
3. Click **Get started** → accept the Terms of Service
4. Enter your payment information (payments require AdMob approval)

### Step 2 — Add Your App
1. In the AdMob console, click **Apps** → **Add app**
2. Choose platform:
   - **Android** for the TWA/Play Store build
   - **Web** is not directly supported; use Google Ad Manager or AdSense for the browser version
3. Select **Yes, my app is listed on a supported app store** (if already published) or **No** for testing
4. Enter App name: `Dungeon Bolt`
5. Click **Add** — AdMob assigns an **App ID** like `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX`
6. Record this App ID — it goes in the Android manifest and in `admob.js`

### Step 3 — Create Rewarded Ad Units
1. In AdMob console, go to **Apps** → **Dungeon Bolt** → **Ad units** → **Add ad unit**
2. Select **Rewarded** as the ad format
3. Name it: `dungeon-bolt-rewarded-extra-life`
4. Leave reward settings as default (1 reward item = 1 extra life)
5. Click **Create ad unit** — copy the **Ad Unit ID**: `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX`

Repeat for additional placements if needed (e.g., `dungeon-bolt-rewarded-continue`).

---

## 2. Test Ad Unit IDs

Use these Google-provided test IDs during development. **Never use real IDs in dev builds.**

| Platform | Format    | Test Ad Unit ID                                      |
|----------|-----------|------------------------------------------------------|
| Android  | Rewarded  | `ca-app-pub-3940256099942544/5224354917`             |
| Android  | Banner    | `ca-app-pub-3940256099942544/6300978111`             |
| Android  | Interstitial | `ca-app-pub-3940256099942544/1033173712`          |
| Web/JS   | (test)    | Use placeholder mode — see `admob.js`                |

---

## 3. JavaScript / HTML5 Integration

### 3a. Include `admob.js` in the game page

Add before `</body>` in `games/dungeon-bolt/index.html`:

```html
<script src="admob.js"></script>
```

### 3b. Where to Call `showRewardedAd()`

| Trigger Event                     | How to wire it up                                          |
|-----------------------------------|------------------------------------------------------------|
| Player clicks "Watch ad for extra life" button on the Death screen | Call `DungeonBoltAdMob.showRewardedAd(onReward, onSkip)` inside the button's `onclick` handler |
| Player clicks "Continue?" after game over | Same pattern — show ad, then resume if reward earned |
| Floor transition (between floors) | Optional interstitial — use `DungeonBoltAdMob.showInterstitial()` |

#### Death Screen — add the Watch Ad button

In `index.html`, replace the death screen `<div id="death">` section:

```html
<div id="death" class="screen">
  <div style="font-size:3.5em">💀</div>
  <div style="font-size:2em;font-weight:900;color:#ff6b6b;margin:10px">YOU DIED</div>
  <div style="color:#8b8070" id="d-stats"></div>

  <!-- Rewarded Ad: extra life -->
  <button class="btn" id="btn-watch-ad"
    style="background:linear-gradient(135deg,#00ff88,#00cc6a);color:#000;margin-top:12px"
    onclick="onWatchAdForLife()">
    📺 Watch Ad for Extra Life
  </button>

  <button class="btn btn-play" onclick="startRun()">⚡ Try Again</button>
  <div class="credit">⚡ JellyBolt Games</div>
</div>
```

Wire up the handler in `index.html` or a separate script:

```js
function onWatchAdForLife() {
  DungeonBoltAdMob.showRewardedAd(
    function onReward() {
      // Grant extra life: restore HP and resume game
      player.hp = Math.floor(player.maxHp * 0.5);
      showScreen('game');
      enterRoom();
    },
    function onSkip() {
      // Player skipped — do nothing, stay on death screen
      console.log('[AdMob] Ad skipped or not available');
    }
  );
}
```

#### Victory Screen — optional rewarded ad (score multiplier)

```js
function onWatchAdForBonus() {
  DungeonBoltAdMob.showRewardedAd(
    function onReward() {
      goldCollected = Math.floor(goldCollected * 1.5);
      document.getElementById('v-stats').textContent =
        `Gold ×1.5 bonus applied! Total: ${goldCollected}`;
    },
    function onSkip() {}
  );
}
```

---

## 4. Event Handlers Summary

Defined in `admob.js` — all callbacks are optional:

| Callback       | When it fires                                  |
|----------------|------------------------------------------------|
| `onAdLoaded`   | Ad preloaded and ready to show                 |
| `onAdFailed`   | Ad failed to load (network error, no fill)     |
| `onAdOpened`   | Ad started showing (UI paused automatically)   |
| `onAdClosed`   | Ad closed — fires regardless of reward         |
| `onRewardEarned` | User watched enough to earn reward           |

---

## 5. Production Deployment Checklist

- [ ] AdMob account approved and payment method added
- [ ] Real App ID entered in `admob.js` (`ADMOB_APP_ID`)
- [ ] Real Rewarded Ad Unit ID entered (`REWARDED_AD_UNIT_ID`)
- [ ] `IS_TESTING` flag set to `false` in `admob.js`
- [ ] Android `AndroidManifest.xml` updated with `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" ...>`
- [ ] Play Store build tested end-to-end with real ads
- [ ] Verified reward is only granted once per ad view
- [ ] Verified fallback (no fill) shows graceful message
- [ ] Privacy Policy updated to mention ad personalization
- [ ] GDPR/CCPA consent flow implemented if targeting EU/CA users
- [ ] AdMob test mode disabled in release build
- [ ] App published to Play Store with AdMob SDK dependency in `build.gradle`

---

## 6. Android Native (TWA) Integration

In `android-app/app/build.gradle` add:

```gradle
dependencies {
    implementation 'com.google.android.gms:play-services-ads:23.0.0'
}
```

In `android-app/app/src/main/AndroidManifest.xml` add inside `<application>`:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"/>
```

Replace the placeholder with the real App ID from the AdMob console.
