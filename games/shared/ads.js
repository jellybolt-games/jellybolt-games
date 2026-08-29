/**
 * JellyBolt Games — Shared Monetization & Ad Management
 * ======================================================
 * Hybrid ad system: AdMob (via Capacitor) on mobile apps,
 * AdSense on web. Auto-detects environment.
 */
const JellyBoltAds = (() => {
  const CONFIG = {
    adClient: 'ca-pub-8350556723025693',
    slots: {
      topBanner:    '8171848729',
      bottomBanner: '8171848729',
      interstitial: '5123448325',
      rewardVideo:  '5123448325'
    },
    admob: {
      appId:        'ca-app-pub-8350556723025693~3277051594',
      banner:       'ca-app-pub-8350556723025693/8171848729',
      interstitial: 'ca-app-pub-8350556723025693/5123448325',
      reward:       'ca-app-pub-8350556723025693/5123448325'
    },
    interstitialFrequency: 2,
    cooldownMs:            60000
  };

  let _levelCount      = 0;
  let _lastInterstitial = 0;
  let _isNative = false;
  let _admobPlugin = null;

  async function init() {
    _isNative = typeof window.Capacitor !== 'undefined' && window.Capacitor.isNativePlatform();

    if (_isNative) {
      try {
        const { AdMob, BannerAdSize, BannerAdPosition } = await import('@capacitor-community/admob');
        _admobPlugin = AdMob;
        await AdMob.initialize({ initializeForTesting: false });
        await AdMob.showBanner({
          adId: CONFIG.admob.banner,
          adSize: BannerAdSize.ADAPTIVE_BANNER,
          position: BannerAdPosition.BOTTOM_CENTER,
          isTesting: false
        });
        console.log('[JellyBoltAds] AdMob initialized (native)');
      } catch (e) {
        console.warn('[JellyBoltAds] AdMob init failed, falling back to web ads', e);
        _isNative = false;
        _initWebAds();
      }
    } else {
      _initWebAds();
    }
  }

  function _initWebAds() {
    if (window.adsbygoogle) return;
    const s = document.createElement('script');
    s.async = true;
    s.src = `https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${CONFIG.adClient}`;
    s.crossOrigin = 'anonymous';
    document.head.appendChild(s);
    window.adsbygoogle = window.adsbygoogle || [];
  }

  function pushAd() {
    try { (window.adsbygoogle = window.adsbygoogle || []).push({}); } catch (_) {}
  }

  function refreshBanners() {
    if (_isNative) return;
    document.querySelectorAll('.adsbygoogle').forEach(() => pushAd());
  }

  async function showInterstitialAd(onClose) {
    _levelCount++;
    const now = Date.now();
    if (_levelCount % CONFIG.interstitialFrequency !== 0) { onClose?.(); return; }
    if (now - _lastInterstitial < CONFIG.cooldownMs)       { onClose?.(); return; }
    _lastInterstitial = now;

    if (_isNative && _admobPlugin) {
      try {
        const { AdMob } = await import('@capacitor-community/admob');
        await AdMob.prepareInterstitial({ adId: CONFIG.admob.interstitial, isTesting: false });
        await AdMob.showInterstitial();
        onClose?.();
      } catch (e) {
        console.warn('[JellyBoltAds] Interstitial failed', e);
        onClose?.();
      }
      return;
    }

    const overlay = document.getElementById('jb-interstitial');
    if (!overlay) { onClose?.(); return; }
    overlay.style.display = 'flex';
    overlay.querySelector('.jb-interstitial-close')
      ?.addEventListener('click', () => {
        overlay.style.display = 'none';
        onClose?.();
      }, { once: true });
    setTimeout(() => {
      if (overlay.style.display !== 'none') {
        overlay.style.display = 'none';
        onClose?.();
      }
    }, 5000);
  }

  async function showRewardVideo(onReward, onSkip) {
    if (_isNative && _admobPlugin) {
      try {
        const { AdMob } = await import('@capacitor-community/admob');
        AdMob.addListener('onRewardedVideoAdReward', () => onReward?.());
        await AdMob.prepareRewardVideoAd({ adId: CONFIG.admob.reward, isTesting: false });
        await AdMob.showRewardVideoAd();
      } catch (e) {
        console.warn('[JellyBoltAds] Reward ad failed', e);
        onSkip?.();
      }
      return;
    }
    console.log('[JellyBoltAds] Reward video requested');
    const granted = confirm('Watch a short ad for a bonus?');
    granted ? onReward?.() : onSkip?.();
  }

  function shareScore(gameName, score) {
    const url  = encodeURIComponent(window.location.href);
    const text = encodeURIComponent(`I scored ${score} in ${gameName}! Play now:`);
    window.open(`https://twitter.com/intent/tweet?text=${text}&url=${url}`, '_blank');
  }

  return {
    CONFIG, init, pushAd, refreshBanners,
    showInterstitialAd, showRewardVideo, shareScore
  };
})();

document.addEventListener('DOMContentLoaded', () => JellyBoltAds.init());
