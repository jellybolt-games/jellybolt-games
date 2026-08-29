/**
 * JellyBolt Games — Monetization & Engagement Module
 * Adds: AdSense placeholders, Support button, Cross-promo, Social sharing
 * Include via <script src="../shared/monetization.js"></script> before </body>
 */
(function(){
'use strict';

/* ── Game catalog ── */
const GAMES=[
{id:'asteroid-dash',name:'Asteroid Dash',emoji:'🚀',genre:'Space Shooter'},
{id:'bolt-breaker',name:'Bolt Breaker',emoji:'⚡',genre:'Breakout'},
{id:'bounce-blitz',name:'Bounce Blitz',emoji:'🏐',genre:'Arcade'},
{id:'brainrot-quiz-battle',name:'BrainRot Quiz Battle',emoji:'🧠',genre:'Trivia'},
{id:'card-clash',name:'Card Clash',emoji:'🃏',genre:'Card Battle'},
{id:'code-conquest',name:'Code Conquest',emoji:'💻',genre:'Strategy'},
{id:'dungeon-bolt',name:'Dungeon Bolt',emoji:'⚔️',genre:'Roguelike'},
{id:'escape-room',name:'Escape Room',emoji:'🔐',genre:'Puzzle Adventure'},
{id:'gravity-dash',name:'Gravity Dash',emoji:'🌀',genre:'Platformer'},
{id:'hex-match',name:'Hex Match',emoji:'🔷',genre:'Puzzle'},
{id:'light-trail',name:'Light Trail',emoji:'💡',genre:'Arcade'},
{id:'memory-matrix',name:'Memory Matrix',emoji:'🧩',genre:'Memory'},
{id:'neon-snake',name:'Neon Snake',emoji:'🐍',genre:'Classic'},
{id:'pixel-tower',name:'Pixel Tower',emoji:'🏗️',genre:'Stacking'},
{id:'rhythm-tap',name:'Rhythm Tap',emoji:'🎵',genre:'Rhythm'},
{id:'space-trader',name:'Space Trader',emoji:'🛸',genre:'Trading'},
{id:'word-rush',name:'Word Rush',emoji:'⌨️',genre:'Typing'},
{id:'bolt-blocks',name:'Bolt Blocks',emoji:'🧱',genre:'Block Puzzle'},
{id:'bolt-solitaire',name:'Bolt Solitaire',emoji:'🃏',genre:'Card Game'}
];

/* ── Detect current game ── */
const pathParts = window.location.pathname.split('/').filter(Boolean);
const currentGame = GAMES.find(g => pathParts.includes(g.id));
const gameName = currentGame ? currentGame.name : document.title.replace(/ — JellyBolt Games/,'');

/* ── Inject global styles ── */
const style = document.createElement('style');
style.textContent = `
/* === Ad Slots === */
.jb-ad-banner{width:100%;max-width:728px;height:90px;margin:0 auto;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#111128,#1a1a3a);border:1px dashed #333;color:#555;font-family:'Segoe UI',system-ui,sans-serif;font-size:12px;letter-spacing:1px;z-index:9990;overflow:hidden;position:relative}
.jb-ad-top{position:fixed;top:0;left:50%;transform:translateX(-50%);border-radius:0 0 8px 8px;border-top:none}
.jb-ad-bottom{position:fixed;bottom:0;left:50%;transform:translateX(-50%);border-radius:8px 8px 0 0;border-bottom:none}
.jb-ad-banner ins{display:block;width:728px;height:90px}
.jb-ad-interstitial{display:none;position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.85);z-index:10000;align-items:center;justify-content:center;flex-direction:column;font-family:'Segoe UI',system-ui,sans-serif}
.jb-ad-interstitial.active{display:flex}
.jb-ad-interstitial .ad-container{width:300px;height:250px;background:linear-gradient(135deg,#111128,#1a1a3a);border:1px dashed #444;border-radius:12px;display:flex;align-items:center;justify-content:center;color:#555;font-size:12px;letter-spacing:1px;margin:16px}
.jb-ad-interstitial .ad-skip{margin-top:12px;padding:10px 28px;background:linear-gradient(135deg,#00ff88,#00cc6a);color:#000;border:none;border-radius:8px;font-weight:700;cursor:pointer;font-size:14px;opacity:0;pointer-events:none;transition:opacity .3s}
.jb-ad-interstitial .ad-skip.visible{opacity:1;pointer-events:auto}
.jb-ad-interstitial .ad-timer{color:#888;font-size:13px;margin-top:8px}
.jb-ad-loading{display:none;position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(5,5,20,.95);z-index:10001;align-items:center;justify-content:center;flex-direction:column;font-family:'Segoe UI',system-ui,sans-serif}
.jb-ad-loading.active{display:flex}
.jb-ad-loading .ad-container{width:300px;height:250px;background:linear-gradient(135deg,#111128,#1a1a3a);border:1px dashed #444;border-radius:12px;display:flex;align-items:center;justify-content:center;color:#555;font-size:12px;letter-spacing:1px}
.jb-ad-loading h2{color:#00ff88;font-size:1.5rem;margin-bottom:16px}
.jb-ad-loading .loading-bar{width:200px;height:4px;background:#1a1a3a;border-radius:4px;margin-top:16px;overflow:hidden}
.jb-ad-loading .loading-fill{width:0%;height:100%;background:linear-gradient(90deg,#00ff88,#58a6ff);border-radius:4px;transition:width .1s linear}

/* === Support Button === */
.jb-support-btn{position:fixed;bottom:100px;right:16px;z-index:9995;padding:8px 14px;background:linear-gradient(135deg,#ffdd00,#ff9900);color:#000;border:none;border-radius:20px;font-weight:700;font-size:13px;cursor:pointer;text-decoration:none;display:flex;align-items:center;gap:5px;box-shadow:0 4px 15px rgba(255,153,0,.3);transition:all .2s;font-family:'Segoe UI',system-ui,sans-serif}
.jb-support-btn:hover{transform:scale(1.08);box-shadow:0 6px 20px rgba(255,153,0,.5)}

/* === More Games Button === */
.jb-more-games-btn{position:fixed;bottom:100px;left:16px;z-index:9995;padding:8px 14px;background:linear-gradient(135deg,#58a6ff,#3b82f6);color:#fff;border:none;border-radius:20px;font-weight:700;font-size:13px;cursor:pointer;text-decoration:none;display:flex;align-items:center;gap:5px;box-shadow:0 4px 15px rgba(88,166,255,.3);transition:all .2s;font-family:'Segoe UI',system-ui,sans-serif}
.jb-more-games-btn:hover{transform:scale(1.08);box-shadow:0 6px 20px rgba(88,166,255,.5)}

/* === More Games Modal === */
.jb-games-modal{display:none;position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.88);z-index:10002;align-items:center;justify-content:center;font-family:'Segoe UI',system-ui,sans-serif}
.jb-games-modal.active{display:flex}
.jb-games-modal-content{background:#12122a;border:1px solid #2a2a4a;border-radius:16px;padding:24px;max-width:600px;width:90%;max-height:80vh;overflow-y:auto}
.jb-games-modal h2{text-align:center;margin-bottom:16px;background:linear-gradient(135deg,#00ff88,#58a6ff);-webkit-background-clip:text;-webkit-text-fill-color:transparent;font-size:1.4rem}
.jb-games-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(120px,1fr));gap:10px}
.jb-game-card{background:linear-gradient(145deg,#1a1a3a,#12122a);border:1px solid #2a2a4a;border-radius:10px;padding:12px 8px;text-align:center;cursor:pointer;transition:all .2s;text-decoration:none;color:#e0e0e0}
.jb-game-card:hover{transform:translateY(-3px);border-color:#00ff88;box-shadow:0 6px 20px rgba(0,255,136,.12)}
.jb-game-card .ge{font-size:1.8rem;display:block;margin-bottom:4px}
.jb-game-card .gn{font-weight:700;font-size:.8rem;color:#fff}
.jb-game-card .gg{font-size:.65rem;color:#666;margin-top:2px}
.jb-games-modal .close-btn{display:block;margin:16px auto 0;padding:8px 24px;background:linear-gradient(135deg,#ff6b6b,#ff4444);color:#fff;border:none;border-radius:8px;font-weight:700;cursor:pointer;font-size:13px}

/* === Social Share Overlay === */
.jb-share-overlay{display:none;position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.8);z-index:10003;align-items:center;justify-content:center;font-family:'Segoe UI',system-ui,sans-serif}
.jb-share-overlay.active{display:flex}
.jb-share-box{background:#12122a;border:1px solid #2a2a4a;border-radius:16px;padding:28px;text-align:center;max-width:380px;width:90%}
.jb-share-box h3{color:#00ff88;font-size:1.3rem;margin-bottom:16px}
.jb-share-box .score-display{font-size:2rem;font-weight:700;color:#ffcc00;margin-bottom:20px}
.jb-share-btns{display:flex;gap:10px;justify-content:center;flex-wrap:wrap;margin-bottom:16px}
.jb-share-btns a{padding:10px 20px;border-radius:8px;text-decoration:none;font-weight:700;font-size:14px;display:flex;align-items:center;gap:6px;transition:transform .2s}
.jb-share-btns a:hover{transform:scale(1.06)}
.jb-share-x{background:#000;color:#fff;border:1px solid #333}
.jb-share-reddit{background:#ff4500;color:#fff}
.jb-share-box .close-share{margin-top:8px;padding:8px 20px;background:#333;color:#ccc;border:none;border-radius:8px;cursor:pointer;font-size:13px}

/* === Responsive === */
@media(max-width:768px){
  .jb-ad-banner{max-width:320px;height:50px}
  .jb-ad-banner ins{width:320px;height:50px}
  .jb-support-btn,.jb-more-games-btn{font-size:11px;padding:6px 10px;bottom:60px}
  .jb-games-grid{grid-template-columns:repeat(auto-fill,minmax(90px,1fr));gap:8px}
}
`;
document.head.appendChild(style);

/* ── Helper: create element ── */
function el(tag,attrs,html){
  const e=document.createElement(tag);
  if(attrs)Object.keys(attrs).forEach(k=>e.setAttribute(k,attrs[k]));
  if(html)e.innerHTML=html;
  return e;
}

/* ══════════════════════════════════════
   1. AD SLOTS — AdSense Placeholders
   (skip if inline ads already present)
   ══════════════════════════════════════ */

if(!document.getElementById('ad-top')){
  const adTop=el('div',{class:'jb-ad-banner jb-ad-top',id:'jb-ad-top'},`
    <ins class="adsbygoogle" style="display:inline-block;width:728px;height:90px"
         data-ad-client="ca-pub-8350556723025693" data-ad-slot="TOP_BANNER"></ins>
    <span style="position:absolute">728×90 Ad</span>
  `);
  document.body.prepend(adTop);
}

if(!document.getElementById('ad-bottom')){
  const adBottom=el('div',{class:'jb-ad-banner jb-ad-bottom',id:'jb-ad-bottom'},`
    <ins class="adsbygoogle" style="display:inline-block;width:728px;height:90px"
         data-ad-client="ca-pub-8350556723025693" data-ad-slot="BOTTOM_BANNER"></ins>
    <span style="position:absolute">728×90 Ad</span>
  `);
  document.body.appendChild(adBottom);
}

/* Interstitial ad — skip if already present */
if(!document.getElementById('jb-interstitial')){
  const adInterstitial=el('div',{class:'jb-ad-interstitial',id:'jb-interstitial'},`
    <div class="ad-container">
      <ins class="adsbygoogle" style="display:inline-block;width:300px;height:250px"
           data-ad-client="ca-pub-8350556723025693" data-ad-slot="INTERSTITIAL"></ins>
      <span style="position:absolute">300×250 Ad</span>
    </div>
    <div class="ad-timer" id="jb-interstitial-timer"></div>
    <button class="ad-skip" id="jb-interstitial-skip">Continue Playing ▶</button>
  `);
  document.body.appendChild(adInterstitial);
}

/* Loading screen ad */
const adLoading=el('div',{class:'jb-ad-loading',id:'jb-loading-ad'},`
  <h2>⚡ ${gameName}</h2>
  <div class="ad-container">
    <ins class="adsbygoogle" style="display:inline-block;width:300px;height:250px"
         data-ad-client="ca-pub-8350556723025693" data-ad-slot="LOADING_AD"></ins>
    <span style="position:absolute">300×250 Ad</span>
  </div>
  <div class="loading-bar"><div class="loading-fill" id="jb-loading-fill"></div></div>
`);
document.body.appendChild(adLoading);

/* Interstitial logic */
let _interstitialCount = 0;
const skipEl=document.getElementById('jb-interstitial-skip');
if(skipEl) skipEl.addEventListener('click',function(){
  document.getElementById('jb-interstitial').classList.remove('active');
  document.getElementById('jb-interstitial').style.display='none';
});

window.jbShowInterstitial = function(){
  _interstitialCount++;
  if(_interstitialCount % 2 === 0){
    const el=document.getElementById('jb-interstitial');
    if(!el)return;
    el.classList.add('active');
    el.style.display='flex';
    const skipBtn=document.getElementById('jb-interstitial-skip');
    const timerEl=document.getElementById('jb-interstitial-timer');
    if(skipBtn)skipBtn.classList.remove('visible');
    let countdown=3;
    if(timerEl)timerEl.textContent='Continue in '+countdown+'s...';
    const iv=setInterval(()=>{
      countdown--;
      if(countdown<=0){
        clearInterval(iv);
        if(timerEl)timerEl.textContent='';
        if(skipBtn)skipBtn.classList.add('visible');
      } else {
        if(timerEl)timerEl.textContent='Continue in '+countdown+'s...';
      }
    },1000);
  }
};

/* Loading ad — shows for 3 seconds then auto-dismisses */
window.jbShowLoadingAd = function(){
  adLoading.classList.add('active');
  const fill=document.getElementById('jb-loading-fill');
  let pct=0;
  const iv=setInterval(()=>{
    pct+=3.33;
    fill.style.width=Math.min(pct,100)+'%';
    if(pct>=100){
      clearInterval(iv);
      setTimeout(()=>adLoading.classList.remove('active'),200);
    }
  },100);
};

/* ══════════════════════════════════════
   2. SUPPORT / TIP BUTTON (skip if monetization bar present)
   ══════════════════════════════════════ */
if(!document.querySelector('.jb-monetization-bar')){
  const supportBtn=el('a',{
    class:'jb-support-btn',
    href:'https://buymeacoffee.com/jellyboltgames',
    target:'_blank',
    rel:'noopener noreferrer',
    title:'Support JellyBolt Games'
  },'☕ Support Us');
  document.body.appendChild(supportBtn);
}

/* ══════════════════════════════════════
   3. CROSS-PROMOTION — More Games
   ══════════════════════════════════════ */
const moreBtn=el('button',{class:'jb-more-games-btn',id:'jb-more-games-btn'},'🎮 More Games');
document.body.appendChild(moreBtn);

/* Modal */
const modal=el('div',{class:'jb-games-modal',id:'jb-games-modal'});
let gridHTML='<div class="jb-games-modal-content"><h2>⚡ More JellyBolt Games</h2><div class="jb-games-grid">';
GAMES.forEach(g=>{
  if(currentGame && g.id===currentGame.id)return;
  gridHTML+=`<a class="jb-game-card" href="../${g.id}/index.html"><span class="ge">${g.emoji}</span><div class="gn">${g.name}</div><div class="gg">${g.genre}</div></a>`;
});
gridHTML+='</div><button class="close-btn" id="jb-close-games">✕ Close</button></div>';
modal.innerHTML=gridHTML;
document.body.appendChild(modal);

moreBtn.addEventListener('click',()=>modal.classList.add('active'));
document.getElementById('jb-close-games').addEventListener('click',()=>modal.classList.remove('active'));
modal.addEventListener('click',e=>{if(e.target===modal)modal.classList.remove('active');});

/* ══════════════════════════════════════
   4. SOCIAL SHARING
   ══════════════════════════════════════ */
const shareOverlay=el('div',{class:'jb-share-overlay',id:'jb-share-overlay'},`
  <div class="jb-share-box">
    <h3>🏆 Share Your Score!</h3>
    <div class="score-display" id="jb-share-score"></div>
    <div class="jb-share-btns">
      <a id="jb-share-x" class="jb-share-x" href="#" target="_blank" rel="noopener noreferrer">𝕏 Post</a>
      <a id="jb-share-reddit" class="jb-share-reddit" href="#" target="_blank" rel="noopener noreferrer">🔺 Reddit</a>
    </div>
    <button class="close-share" id="jb-close-share">Close</button>
  </div>
`);
document.body.appendChild(shareOverlay);

document.getElementById('jb-close-share').addEventListener('click',()=>shareOverlay.classList.remove('active'));
shareOverlay.addEventListener('click',e=>{if(e.target===shareOverlay)shareOverlay.classList.remove('active');});

const gameUrl = window.location.href;

window.jbShareScore = function(score){
  document.getElementById('jb-share-score').textContent=score;
  const text=encodeURIComponent(`I scored ${score} in ${gameName} by @JellyBoltGames! Play it here: ${gameUrl}`);
  const redditTitle=encodeURIComponent(`I scored ${score} in ${gameName}!`);
  const redditUrl=encodeURIComponent(gameUrl);
  document.getElementById('jb-share-x').href=`https://x.com/intent/tweet?text=${text}`;
  document.getElementById('jb-share-reddit').href=`https://www.reddit.com/submit?url=${redditUrl}&title=${redditTitle}`;
  shareOverlay.classList.add('active');
};

/* ── AdSense script placeholder (uncomment with real pub ID) ── */
// const adsScript=document.createElement('script');
// adsScript.async=true;
// adsScript.src='https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-8350556723025693';
// adsScript.crossOrigin='anonymous';
// document.head.appendChild(adsScript);

/* ── Adjust body padding only if we created the fixed ad banners ── */
if(document.getElementById('jb-ad-top')){
  document.body.style.paddingTop = (parseInt(document.body.style.paddingTop)||0) + 90 + 'px';
}
if(document.getElementById('jb-ad-bottom')){
  document.body.style.paddingBottom = (parseInt(document.body.style.paddingBottom)||0) + 90 + 'px';
}

})();
