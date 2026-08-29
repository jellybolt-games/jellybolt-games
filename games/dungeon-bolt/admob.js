/**
 * JellyBolt Games — Dungeon Bolt AdMob Integration
 * =================================================
 * Rewarded ad load/show with fallback for no-fill and test mode.
 *
 * Usage:
 *   <script src="admob.js"></script>
 *   DungeonBoltAdMob.preload();
 *   DungeonBoltAdMob.showRewardedAd(onReward, onSkip);
 *
 * IMPORTANT: Set IS_TESTING = false and fill in real IDs before release.
 */

const DungeonBoltAdMob = (() => {
  'use strict';

  // ─── Configuration ────────────────────────────────────────────────────────
  const IS_TESTING = true; // Switch to false for production

  // Replace with real IDs from AdMob console before shipping
  const ADMOB_APP_ID       = 'ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX';
  const REWARDED_AD_UNIT_ID = IS_TESTING
    ? 'ca-app-pub-3940256099942544/5224354917' // Google test ID
    : 'ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX'; // Real rewarded unit ID

  // ─── State ────────────────────────────────────────────────────────────────
  let _adLoaded    = false;
  let _adShowing   = false;
  let _loadAttempts = 0;
  const MAX_LOAD_RETRIES = 3;

  // ─── Internal: build the rewarded ad overlay (web fallback) ───────────────
  function _createOverlay() {
    if (document.getElementById('jb-rewarded-overlay')) return;

    const overlay = document.createElement('div');
    overlay.id = 'jb-rewarded-overlay';
    overlay.style.cssText = [
      'display:none',
      'position:fixed',
      'top:0','left:0','width:100%','height:100%',
      'background:rgba(5,5,20,0.96)',
      'z-index:99999',
      'align-items:center',
      'justify-content:center',
      'flex-direction:column',
      'font-family:"Segoe UI",system-ui,sans-serif',
    ].join(';');

    overlay.innerHTML = `
      <div style="text-align:center;max-width:340px;padding:24px">
        <div style="font-size:2.5rem;margin-bottom:8px">📺</div>
        <h2 style="color:#00ff88;font-size:1.4rem;margin-bottom:4px">Watch Ad for Extra Life</h2>
        <p style="color:#888;font-size:.85rem;margin-bottom:20px">
          Support JellyBolt Games by watching a short ad!
        </p>
        <div id="jb-ad-placeholder"
             style="width:300px;height:250px;background:#111128;border:1px dashed #333;
                    border-radius:12px;display:flex;align-items:center;justify-content:center;
                    color:#444;font-size:.8rem;letter-spacing:1px;margin:0 auto 16px">
          AD LOADING…
        </div>
        <div id="jb-ad-timer" style="color:#888;font-size:.85rem;margin-bottom:12px">
          Please wait…
        </div>
        <button id="jb-ad-claim"
                style="display:none;padding:12px 32px;background:linear-gradient(135deg,#00ff88,#00cc6a);
                       color:#000;border:none;border-radius:10px;font-weight:700;font-size:1rem;
                       cursor:pointer;margin-bottom:8px">
          ✅ Claim Extra Life
        </button>
        <button id="jb-ad-skip"
                style="padding:8px 20px;background:transparent;color:#555;border:1px solid #333;
                       border-radius:8px;font-size:.8rem;cursor:pointer;margin-top:4px">
          Skip (no reward)
        </button>
      </div>
    `;
    document.body.appendChild(overlay);
  }

  // ─── Internal: simulate ad load (web — no native AdMob SDK available) ────
  function _simulateAdLoad(onSuccess, onFail) {
    // In a real Android/TWA build this is replaced by the native AdMob SDK.
    // For the web version, we simulate a short load delay.
    setTimeout(() => {
      if (IS_TESTING || Math.random() > 0.1) {
        _adLoaded = true;
        onSuccess?.();
        _fireEvent('onAdLoaded');
      } else {
        _adLoaded = false;
        onFail?.();
        _fireEvent('onAdFailed', 'No fill');
      }
    }, IS_TESTING ? 300 : 800);
  }

  // ─── Internal: simulate ad playback ──────────────────────────────────────
  function _playAd(onReward, onSkip) {
    const overlay   = document.getElementById('jb-rewarded-overlay');
    const timerEl   = document.getElementById('jb-ad-timer');
    const claimBtn  = document.getElementById('jb-ad-claim');
    const skipBtn   = document.getElementById('jb-ad-skip');
    const placeholder = document.getElementById('jb-ad-placeholder');

    if (!overlay) return;

    overlay.style.display = 'flex';
    claimBtn.style.display = 'none';
    _adShowing = true;
    _fireEvent('onAdOpened');

    placeholder.textContent = IS_TESTING ? '[ TEST AD — 5s ]' : '[ AD PLAYING ]';

    let secondsLeft = IS_TESTING ? 5 : 15;
    timerEl.textContent = `Ad ends in ${secondsLeft}s…`;

    const interval = setInterval(() => {
      secondsLeft--;
      if (secondsLeft > 0) {
        timerEl.textContent = `Ad ends in ${secondsLeft}s…`;
      } else {
        clearInterval(interval);
        timerEl.textContent = 'Ad complete! Claim your reward.';
        claimBtn.style.display = 'block';
        _fireEvent('onRewardEarned');
      }
    }, 1000);

    // Claim button — grants reward
    claimBtn.onclick = () => {
      _closeOverlay(overlay);
      _adLoaded = false;
      _adShowing = false;
      onReward?.();
      _fireEvent('onAdClosed');
    };

    // Skip button — no reward
    skipBtn.onclick = () => {
      clearInterval(interval);
      _closeOverlay(overlay);
      _adLoaded = false;
      _adShowing = false;
      onSkip?.();
      _fireEvent('onAdClosed');
    };
  }

  function _closeOverlay(overlay) {
    overlay.style.display = 'none';
    const claimBtn = document.getElementById('jb-ad-claim');
    const skipBtn  = document.getElementById('jb-ad-skip');
    if (claimBtn) claimBtn.onclick = null;
    if (skipBtn)  skipBtn.onclick  = null;
  }

  // ─── Internal: event bus ─────────────────────────────────────────────────
  const _listeners = {};
  function _fireEvent(name, data) {
    (_listeners[name] || []).forEach(fn => fn(data));
  }

  // ─── Public API ──────────────────────────────────────────────────────────

  /**
   * Preload a rewarded ad so it's ready instantly when the player requests it.
   * Call this right after the game loads or after a previous ad is dismissed.
   */
  function preload() {
    if (_adLoaded || _adShowing) return;
    _loadAttempts++;
    if (_loadAttempts > MAX_LOAD_RETRIES) {
      console.warn('[DungeonBoltAdMob] Max load retries reached, giving up.');
      return;
    }
    _createOverlay();
    console.log('[DungeonBoltAdMob] Loading rewarded ad…', { unit: REWARDED_AD_UNIT_ID, test: IS_TESTING });
    _simulateAdLoad(
      () => console.log('[DungeonBoltAdMob] Ad loaded and ready.'),
      () => {
        console.warn('[DungeonBoltAdMob] Ad load failed. Will retry in 30s.');
        setTimeout(preload, 30000);
      }
    );
  }

  /**
   * Show the rewarded ad.
   * @param {Function} onReward  - Called when player earns the reward (extra life, bonus, etc.)
   * @param {Function} onSkip    - Called when player skips or ad is unavailable
   */
  function showRewardedAd(onReward, onSkip) {
    if (_adShowing) return;

    if (!_adLoaded) {
      console.warn('[DungeonBoltAdMob] No ad ready — showing fallback.');
      _showNoAdFallback(onSkip);
      // Kick off a preload for next time
      preload();
      return;
    }

    _playAd(
      () => {
        onReward?.();
        // Preload next ad immediately
        _adLoaded = false;
        setTimeout(preload, 1000);
      },
      () => {
        onSkip?.();
        _adLoaded = false;
        setTimeout(preload, 1000);
      }
    );
  }

  /** Show a graceful message when no ad is available */
  function _showNoAdFallback(onSkip) {
    const msg = document.createElement('div');
    msg.style.cssText = [
      'position:fixed','top:50%','left:50%',
      'transform:translate(-50%,-50%)',
      'background:#14142a','border:1px solid #333','border-radius:12px',
      'padding:24px 32px','text-align:center',
      'font-family:"Segoe UI",system-ui,sans-serif',
      'z-index:99999','max-width:280px',
    ].join(';');
    msg.innerHTML = `
      <div style="font-size:2rem;margin-bottom:8px">😔</div>
      <p style="color:#ccc;font-size:.95rem;margin-bottom:16px">
        No ad available right now.<br>Try again in a moment!
      </p>
      <button id="jb-noad-ok"
              style="padding:10px 28px;background:#333;color:#ccc;border:none;
                     border-radius:8px;cursor:pointer;font-size:.9rem">
        OK
      </button>
    `;
    document.body.appendChild(msg);
    document.getElementById('jb-noad-ok').onclick = () => {
      document.body.removeChild(msg);
      onSkip?.();
    };
  }

  /**
   * Register an event listener.
   * Events: 'onAdLoaded' | 'onAdFailed' | 'onAdOpened' | 'onAdClosed' | 'onRewardEarned'
   */
  function on(event, fn) {
    if (!_listeners[event]) _listeners[event] = [];
    _listeners[event].push(fn);
  }

  /** Returns true if a rewarded ad is loaded and ready to show. */
  function isReady() {
    return _adLoaded && !_adShowing;
  }

  // Auto-preload on script load
  document.addEventListener('DOMContentLoaded', preload);

  return {
    preload,
    showRewardedAd,
    isReady,
    on,
    // Exposed for debugging
    _config: { ADMOB_APP_ID, REWARDED_AD_UNIT_ID, IS_TESTING }
  };
})();
