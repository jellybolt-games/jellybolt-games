# 🚀 Google Play Store Launch Checklist — JellyBolt Games

**Issue:** #20 (ventures-ip)  
**Goal:** Grow to 12+ testers → Launch on Google Play Store  
**Current Status:** TWA infrastructure exists for 3 games (Dungeon Bolt, BrainRot Quiz, Code Conquest)

---

## 📋 Pre-Launch Checklist

### ✅ Account & Infrastructure (DONE)
- [x] Google Play Console account set up
- [x] Keystore generated and secured (`jellybolt-release.keystore`)
- [x] GitHub Actions CI/CD configured (`.github/workflows/build-aab.yml`)
- [x] TWA setup documented (`docs/TWA-SETUP.md`)
- [x] Privacy policy published (`store-listings/privacy-policy.html`)
- [x] Developer contact email: `tdsquadai@gmail.com`

### 📱 App Builds

#### Priority 1: Dungeon Bolt (Premium Flagship - $2.99)
- [ ] Build signed AAB via GitHub Actions
- [ ] Test AAB on physical device (Android 8.0+)
- [ ] Verify TWA correctly loads `https://jellyboltgames.itch.io/dungeon-bolt`
- [ ] Test offline functionality
- [ ] Verify AdMob integration (rewarded video for extra life)
- [ ] Upload AAB to Play Console → Internal Testing

#### Priority 2: BrainRot Quiz Battle (Free + Ads)
- [ ] Build signed AAB via GitHub Actions
- [ ] Test AAB on physical device
- [ ] Verify TWA loads `https://jellyboltgames.itch.io/brainrot-quiz-battle`
- [ ] Test AdMob integration (rewarded video, interstitial)
- [ ] Upload AAB to Play Console → Internal Testing

#### Priority 3: Code Conquest (Free)
- [ ] Build signed AAB via GitHub Actions
- [ ] Test AAB on physical device
- [ ] Verify TWA loads `https://jellyboltgames.itch.io/code-conquest`
- [ ] Upload AAB to Play Console → Internal Testing

#### Future Games (Phase 2)
- [ ] Bounce Blitz
- [ ] Neon Snake
- [ ] Asteroid Dash
- [ ] Hex Match
- [ ] Pixel Tower
- [ ] Memory Matrix
- [ ] Word Rush
- [ ] Card Clash
- [ ] Light Trail
- [ ] Gravity Dash
- [ ] Rhythm Tap
- [ ] Space Trader

---

## 🎨 Store Listings (Per Game)

### Assets Required for Each Game

#### App Icon
- **Size:** 512×512 px (high-res PNG)
- **Format:** PNG with transparency
- **Guidelines:** 
  - Icon should be clear and recognizable at small sizes
  - Follow Material Design icon guidelines
  - No text in icon (use visual symbols only)
- **Status:**
  - [ ] Dungeon Bolt icon
  - [ ] BrainRot Quiz icon
  - [ ] Code Conquest icon

#### Feature Graphic
- **Size:** 1024×500 px
- **Format:** JPG or PNG
- **Guidelines:**
  - Showcase game branding + key visual
  - No text (Play Store may reject)
  - High contrast, visually striking
- **Status:**
  - [ ] Dungeon Bolt feature graphic
  - [ ] BrainRot Quiz feature graphic
  - [ ] Code Conquest feature graphic

#### Screenshots (Phone)
- **Min required:** 2 (recommended: 7)
- **Size:** 1080×1920 px (portrait) or 1920×1080 px (landscape)
- **Format:** PNG or JPG
- **Guidelines:**
  - Show actual gameplay (no mockups)
  - Capture key features in each screenshot
  - Add overlay text to highlight features (optional but recommended)
- **Status:**
  - [ ] Dungeon Bolt (7 screenshots)
  - [ ] BrainRot Quiz (7 screenshots)
  - [ ] Code Conquest (7 screenshots)

#### Screenshots (Tablet - Optional)
- **Size:** 1536×2048 px (7-inch) or 2048×2732 px (10-inch)
- **Status:** Not critical for Phase 1

#### Video (Optional but Recommended)
- **Format:** YouTube URL
- **Length:** 30 seconds - 2 minutes
- **Guidelines:**
  - Show core gameplay loop
  - No third-party ads in video
- **Status:**
  - [ ] Dungeon Bolt gameplay video
  - [ ] BrainRot Quiz gameplay video
  - [ ] Code Conquest gameplay video

### Text Content

#### Short Description (80 chars max)
✅ See `docs/play-store/store-listings/{game-name}.md`

#### Full Description (4000 chars max)
✅ See `docs/play-store/store-listings/{game-name}.md`

#### What's New (500 chars)
Template for all games:
```
Welcome to [Game Name]!

This is the initial Google Play release of [Game Name], previously available on itch.io. Now optimized for Android with native app performance.

Features:
• Smooth touch controls
• Offline play
• Regular updates
• No paywalls

Enjoy the game! ⚡
```

---

## 🔒 Content Rating (IARC Questionnaire)

All JellyBolt games target **EVERYONE (E)** rating.

### Questionnaire Answers (Standard for All Games)

**Violence:**
- Does the app contain violence? **NO**
- Does the app contain realistic violence? **NO**
- Does the app contain blood? **NO**

**Sexual Content:**
- Does the app contain sexual content? **NO**
- Does the app contain nudity? **NO**

**Language:**
- Does the app contain profanity? **NO**
- Does the app contain sexual references? **NO**

**Controlled Substances:**
- Does the app contain drug, alcohol, or tobacco references? **NO**

**Gambling:**
- Does the app simulate gambling? **NO**
- Does the app allow users to purchase digital goods with real money that can be used for gambling? **NO**

**User Interaction:**
- Does the app allow users to interact with each other? **NO**
- Does the app share user location with other users? **NO**
- Does the app allow unrestricted internet access? **NO** (TWA loads specific game URL only)

**Advertising:**
- Does the app contain ads? **YES** (BrainRot Quiz only, Dungeon Bolt has optional rewarded ads)
- Are the ads from a certified ad network? **YES** (Google AdMob)

**Data Collection:**
- Does the app collect personal data? **NO**
- Does the app share personal data with third parties? **NO**

**Expected Rating:** EVERYONE (ESRB), PEGI 3, USK 0, ACB G

---

## 🔐 Privacy & Compliance

### Privacy Policy
- [x] Published at: `https://jellybolt-games.github.io/jellybolt-games/store-listings/privacy-policy.html`
- [x] Privacy policy linked in all store listings
- [x] Privacy policy states:
  - No personal data collection
  - AdMob privacy policy linked
  - Contact email for questions

### Data Safety Form (Play Console)
**All games:**
- Data collected: **NONE**
- Data shared: **NONE**
- Security practices: N/A (no user data)
- AdMob SDK: Uses advertising ID (user can opt out via device settings)

### App Access (Special Permissions)
**All games require ZERO special permissions.**
- No camera, microphone, location, contacts, storage
- Only INTERNET permission for TWA + AdMob

---

## 🧪 Closed Testing Track

### Setup
1. Go to Play Console → Select app → Testing → Closed testing
2. Create track: "Closed Beta"
3. Add testers via email list OR generate opt-in link
4. Upload AAB to Closed Beta track
5. Submit for review (2-7 days)

### Target: 12 Beta Testers

**Recruitment Channels:**

#### Reddit (Primary)
- [ ] r/androidgaming — post beta recruitment (see `tester-recruitment.md`)
- [ ] r/indiegaming — cross-post
- [ ] r/playmyapp — share beta link
- [ ] r/gamedev — seek feedback from developers

#### Beta Testing Platforms
- [ ] BetaFamily — create beta campaign (FREE tier: 50 testers)
- [ ] BetaBound — list beta program
- [ ] Reddit r/betatests — post recruitment thread

#### Social Media
- [ ] Twitter/X — tweet beta recruitment with gameplay GIF
- [ ] Discord communities — post in #beta-testing channels
- [ ] Hacker News Show HN (if applicable)

#### Existing Players
- [ ] itch.io announcement — invite existing Dungeon Bolt players
- [ ] Email list (if available)

**Tester Incentive:**
- All testers who submit feedback receive **exclusive golden dungeon key cosmetic** in v1.1
- Name in credits (opt-in)

### Testing Instructions for Testers
✅ See `tester-recruitment.md` for full testing guide

### Feedback Collection
- [ ] Create Google Form for structured feedback
- [ ] Questions:
  - Device model & Android version
  - Any crashes or bugs?
  - Performance (smooth/laggy/freezing)
  - Controls (easy/hard/confusing)
  - Overall enjoyment (1-5 stars)
  - Suggestions for improvement
  - Gmail for beta tester reward

---

## 📊 Pre-Launch Testing Checklist

### Internal Testing (Team + Friends)
- [ ] 3+ devices tested (different manufacturers: Samsung, Pixel, Xiaomi)
- [ ] Android versions tested: 8.0, 10, 12, 14
- [ ] Screen sizes tested: Small (5"), Medium (6"), Large (6.7"+)
- [ ] Network conditions: WiFi, 4G, Airplane mode (offline)
- [ ] AdMob test ads displaying correctly
- [ ] TWA splash screen shows correctly
- [ ] Game loads within 5 seconds
- [ ] No crashes during 15-minute play session

### Closed Beta Testing (12+ Testers)
- [ ] 12+ testers recruited
- [ ] Testers play 3-5 sessions over 1 week
- [ ] Feedback form responses collected
- [ ] Critical bugs identified and fixed
- [ ] Positive feedback (>80% satisfaction)

---

## 🚢 Launch Sequence

### Phase 1: Closed Beta (Week 1-2)
- [x] Upload AAB to Closed Testing track
- [ ] Add 12+ testers to beta tester list
- [ ] Testers install and play for 1 week
- [ ] Collect feedback via Google Form
- [ ] Fix critical bugs
- [ ] Iterate based on feedback

### Phase 2: Open Beta (Week 3-4)
- [ ] Move AAB to Open Testing track (anyone can join)
- [ ] Announce on Reddit, Twitter, itch.io
- [ ] Monitor reviews and crash reports
- [ ] Aim for 100+ installs
- [ ] Target 4+ star average rating
- [ ] Stability: <1% crash rate

### Phase 3: Production Launch (Week 5+)
- [ ] Promote AAB from Open Testing → Production
- [ ] Set availability: All countries
- [ ] Pricing: Dungeon Bolt ($2.99), others (Free)
- [ ] Monitor for first 48 hours:
  - Crash rate
  - User reviews
  - ANRs (App Not Responding)
- [ ] Respond to reviews within 24 hours
- [ ] Post "We're Live!" announcement on all channels

---

## 📈 Post-Launch Monitoring

### Week 1
- [ ] Daily checks of crash reports (Play Console → Vitals)
- [ ] Respond to user reviews
- [ ] Monitor download numbers
- [ ] Track revenue (Dungeon Bolt)

### Month 1
- [ ] A/B test store listing (screenshots, description)
- [ ] Analyze retention: D1, D7, D30
- [ ] Gather feature requests from reviews
- [ ] Plan v1.1 update

### Ongoing
- [ ] Monthly updates with new features
- [ ] Seasonal events (if applicable)
- [ ] Monitor Play Store policies for changes
- [ ] Expand to tablet optimization
- [ ] Consider Android TV version (future)

---

## 🛠️ Technical Requirements

### All Games Must Have:
- [x] Minimum SDK: 26 (Android 8.0)
- [x] Target SDK: 34 (Android 14)
- [x] 64-bit native libraries (if applicable)
- [x] App Bundle (AAB) format (NOT APK)
- [x] Signed with release keystore
- [x] ProGuard enabled (minification)
- [x] Version code increments with each upload
- [x] Version name follows semantic versioning (e.g., 1.0.0)

### TWA-Specific:
- [x] Digital Asset Links file at origin (`.well-known/assetlinks.json`)
- [x] SHA-256 fingerprint updated in assetlinks.json
- [x] TWA manifest points to correct game URL
- [x] Canonical URLs use HTTPS (itch.io enforces this)

---

## 🎯 Success Metrics (Per Game)

### Week 1
- **Installs:** 50+ (Closed Beta)
- **Crash-free rate:** >99%
- **Average rating:** 4.0+

### Month 1
- **Installs:** 500+
- **D1 Retention:** >40%
- **D7 Retention:** >20%
- **Average rating:** 4.2+
- **Reviews:** 20+ (mostly positive)

### Month 3
- **Installs:** 5,000+
- **Monthly Active Users:** 1,000+
- **Revenue (Dungeon Bolt):** $500+

---

## 🚨 Rollback Plan

If critical bug is discovered post-launch:
1. **Halt rollout** — Play Console → Release → Pause rollout
2. **Fix bug** — hotfix branch
3. **Build new AAB** — increment version code
4. **Test fix** — internal testing
5. **Upload new version** — replace broken version
6. **Resume rollout** — staged rollout (10% → 50% → 100%)

---

## 📞 Support & Contact

**Developer Email:** tdsquadai@gmail.com  
**Play Console Support:** https://support.google.com/googleplay/android-developer  
**Store Listings Privacy Policy:** https://jellybolt-games.github.io/jellybolt-games/store-listings/privacy-policy.html

---

## 🎉 Launch Day Announcements

### itch.io
```
🎉 Dungeon Bolt is now on Google Play!

After months of development, Dungeon Bolt is officially available on the Google Play Store as a native Android app!

🔗 Download: [INSERT PLAY STORE LINK]

All existing itch.io players: The browser version will continue to be updated alongside the Android version. No need to switch if you're happy playing in your browser!

Android players: Enjoy native performance, offline play, and a smoother experience.

Thank you for your support! ⚡
```

### Reddit (r/androidgaming)
```
[DEV] Dungeon Bolt — My roguelite dungeon crawler is now on Google Play! 🗡️

After beta testing with 12+ amazing testers from this community, Dungeon Bolt is officially live on Google Play!

What it is:
• 5-floor roguelite dungeon crawler
• 11 enemy types (slimes → dragon 🐉)
• Loot, shops, bosses, permadeath
• $2.99 (no IAP, no ads except optional rewarded video)

🔗 Play Store: [INSERT LINK]
🌐 itch.io (browser): https://jellyboltgames.itch.io/dungeon-bolt

Thanks to everyone who beta tested — your feedback made this launch possible! ⚡
```

### Twitter/X
```
🎮 Dungeon Bolt is LIVE on Google Play! 🗡️

Roguelite dungeon crawler with 5 floors, 11 enemies, loot, shops, and a dragon boss. $2.99, no IAP, offline play.

📱 Download: [LINK]

⚡ Built by @JellyBoltGames
```

---

**Last Updated:** 2026-03-29  
**Owner:** Nog (Revenue Ops, Ventures Squad)  
**Next Review:** After Closed Beta completion
