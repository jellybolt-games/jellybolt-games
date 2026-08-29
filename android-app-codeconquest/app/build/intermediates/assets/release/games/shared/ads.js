/**
 * JellyBolt Games — Shared Monetization & Ad Management
 * ======================================================
 * Drop-in AdSense, interstitial, and reward video support.
 * Replace PLACEHOLDER values with real ad IDs when ready.
 */

const JellyBoltAds = (() => {
  // ─── Configuration (swap in real IDs later) ───
  const CONFIG = {
    adClient: 'ca-pub-PLACEHOLDER',
    slots: {
      topBanner:    'PLACEHOLDER_TOP',
      bottomBanner: 'PLACEHOLDER_BOTTOM',
      interstitial: 'PLACEHOLDER_INTERSTITIAL',
      rewardVideo:  'PLACEHOLDER_REWARD'
    },
    interstitialFrequency: 2,   // show every N level-ends
    cooldownMs:            60000 // min ms between interstitials
  };

  let _levelCount      = 0;
  let _lastInterstitial = 0;

  // ─── Initialise AdSense ───
  function init() {
    if (window.adsbygoogle) return;
    const s = document.createElement('script');
    s.async = true;
    s.src = `https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${CONFIG.adClient}`;
    s.crossOrigin = 'anonymous';
    document.head.appendChild(s);
    window.adsbygoogle = window.adsbygoogle || [];
  }

  // Push an ad unit
  function pushAd() {
    try { (window.adsbygoogle = window.adsbygoogle || []).push({}); } catch (_) {}
  }

  // ─── Banner helpers ───
  function refreshBanners() {
    document.querySelectorAll('.adsbygoogle').forEach(() => pushAd());
  }

  // ─── Interstitial Ad (between levels) ───
  function showInterstitialAd(onClose) {
    _levelCount++;
    const now = Date.now();
    if (_levelCount % CONFIG.interstitialFrequency !== 0) { onClose?.(); return; }
    if (now - _lastInterstitial < CONFIG.cooldownMs)       { onClose?.(); return; }

    _lastInterstitial = now;
    const overlay = document.getElementById('jb-interstitial');
    if (!overlay) { onClose?.(); return; }

    overlay.style.display = 'flex';
    overlay.querySelector('.jb-interstitial-close')
      ?.addEventListener('click', () => {
        overlay.style.display = 'none';
        onClose?.();
      }, { once: true });

    // auto-close after 5 s
    setTimeout(() => {
      if (overlay.style.display !== 'none') {
        overlay.style.display = 'none';
        onClose?.();
      }
    }, 5000);
  }

  // ─── Reward Video Placeholder ───
  function showRewardVideo(onReward, onSkip) {
    // Replace with real rewarded-ad SDK call when available
    console.log('[JellyBoltAds] Reward video requested — using placeholder');
    const granted = confirm('Watch a short ad for a bonus? (placeholder)');
    granted ? onReward?.() : onSkip?.();
  }

  // ─── Social Share ───
  function shareScore(gameName, score) {
    const url  = encodeURIComponent(window.location.href);
    const text = encodeURIComponent(`I scored ${score} in ${gameName}! 🎮 Play now:`);
    window.open(`https://twitter.com/intent/tweet?text=${text}&url=${url}`, '_blank');
  }

  // ─── Public API ───
  return {
    CONFIG,
    init,
    pushAd,
    refreshBanners,
    showInterstitialAd,
    showRewardVideo,
    shareScore
  };
})();

// Auto-init when script loads
document.addEventListener('DOMContentLoaded', () => JellyBoltAds.init());
