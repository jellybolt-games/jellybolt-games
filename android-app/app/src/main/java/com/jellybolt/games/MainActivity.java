package com.jellybolt.games;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "file:///android_asset/games/";
    private AdView bannerAdView;

    private static final String[][] GAMES = {
        {"neon-snake", "Neon Snake", "\uD83D\uDC0D", "Classic Snake"},
        {"asteroid-dash", "Asteroid Dash", "\uD83D\uDE80", "Space Shooter"},
        {"memory-matrix", "Memory Matrix", "\uD83E\uDDE9", "Memory Puzzle"},
        {"bolt-breaker", "Bolt Breaker", "⚡", "Breakout"},
        {"bounce-blitz", "Bounce Blitz", "\uD83C\uDFD0", "Arcade"},
        {"brainrot-quiz-battle", "BrainRot Quiz", "\uD83E\uDDE0", "Trivia"},
        {"card-clash", "Card Clash", "\uD83C\uDCCF", "Card Battle"},
        {"code-conquest", "Code Conquest", "\uD83D\uDCBB", "Strategy"},
        {"color-match", "Color Match", "\uD83C\uDFA8", "Color Puzzle"},
        {"cube-runner", "Cube Runner", "\uD83D\uDFE6", "Runner"},
        {"dungeon-bolt", "Dungeon Bolt", "⚔\uFE0F", "Roguelike"},
        {"gravity-dash", "Gravity Dash", "\uD83C\uDF00", "Platformer"},
        {"hex-match", "Hex Match", "\uD83D\uDD37", "Puzzle"},
        {"light-trail", "Light Trail", "\uD83D\uDCA1", "Arcade"},
        {"maze-runner", "Maze Runner", "\uD83C\uDFAE", "Maze"},
        {"pixel-tower", "Pixel Tower", "\uD83C\uDFD7\uFE0F", "Stacking"},
        {"planet-defense", "Planet Defense", "\uD83C\uDF0D", "Tower Defense"},
        {"rhythm-tap", "Rhythm Tap", "\uD83C\uDFB5", "Rhythm"},
        {"riddle-master", "Riddle Master", "\uD83E\uDD14", "Puzzle"},
        {"space-orbit", "Space Orbit", "\uD83C\uDF1F", "Orbital"},
        {"space-trader", "Space Trader", "\uD83D\uDEF8", "Trading"},
        {"tower-stack", "Tower Stack", "\uD83D\uDDFC", "Stacking"},
        {"typing-blitz", "Typing Blitz", "⌨\uFE0F", "Speed Typing"},
        {"word-rush", "Word Rush", "\uD83D\uDCDD", "Word Game"},
        {"archery-quest", "Archery Quest", "\uD83C\uDFF9", "Archery"},
        {"battle-arena", "Battle Arena", "\uD83E\uDD3C", "Fighting"},
        {"battle-royale", "Battle Royale", "\uD83D\uDCA5", "Battle Royale"},
        {"block-storm", "Block Storm", "\uD83E\uDDF1", "Tetris-like"},
        {"bubble-pop", "Bubble Pop", "\uD83E\uDEE7", "Bubble Shooter"},
        {"crystal-caves", "Crystal Caves", "\uD83D\uDC8E", "Adventure"},
        {"dungeon-crawler", "Dungeon Crawler", "\uD83D\uDDFA\uFE0F", "Dungeon RPG"},
        {"escape-room", "Escape Room", "\uD83D\uDD13", "Escape"},
        {"merge-master", "Merge Master", "\uD83E\uDDE9", "Merge"},
        {"quest-rpg", "Quest RPG", "\uD83D\uDDE1\uFE0F", "RPG"},
        {"racing-3d", "Racing 3D", "\uD83C\uDFCE\uFE0F", "Racing"},
        {"sniper-elite", "Sniper Elite", "\uD83C\uDFAF", "Shooter"},
        {"space-jump", "Space Jump", "\uD83D\uDE80", "Endless Runner"},
        {"space-shooter-3d", "Space Shooter 3D", "\uD83D\uDE80", "3D Shooter"},
        {"tower-defense", "Tower Defense", "\uD83C\uDFF0", "Tower Defense"},
        {"zombie-shooter", "Zombie Shooter", "\uD83E\uDDDF", "Shooter"},
        {"bolt-blocks", "Bolt Blocks", "\uD83E\uDDF1", "Block Puzzle"},
        {"bolt-solitaire", "Bolt Solitaire", "\uD83C\uDCCF", "Card Game"},
        {"neon-dash", "Neon Dash", "\uD83D\uDCA8", "Endless Runner"},
        {"territory-bolt", "Territory Bolt", "\uD83D\uDDFA\uFE0F", "Territory"},
        {"bolt-tiles", "Bolt Tiles", "\uD83C\uDC04", "Mahjong"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        MobileAds.initialize(this, initStatus -> {});

        // Root layout: game list + banner at bottom
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(0xFF0A0A1A);

        ScrollView scrollView = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 48, 32, 100); // extra bottom padding for banner

        // Title
        TextView title = new TextView(this);
        title.setText("⚡ JellyBolt Games");
        title.setTextSize(28);
        title.setTextColor(0xFF00FF88);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 16);
        layout.addView(title);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Tap a game to play!");
        subtitle.setTextSize(16);
        subtitle.setTextColor(0xFF8888AA);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 32);
        layout.addView(subtitle);

        // Game cards
        for (String[] game : GAMES) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setPadding(24, 20, 24, 20);
            card.setBackgroundColor(0xFF1A1A3A);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 12);
            card.setLayoutParams(cardParams);

            // Emoji
            TextView emoji = new TextView(this);
            emoji.setText(game[2]);
            emoji.setTextSize(32);
            emoji.setPadding(0, 0, 16, 0);
            card.addView(emoji);

            // Name + genre
            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setGravity(Gravity.CENTER_VERTICAL);

            TextView name = new TextView(this);
            name.setText(game[1]);
            name.setTextSize(18);
            name.setTextColor(0xFFFFFFFF);
            textLayout.addView(name);

            TextView genre = new TextView(this);
            genre.setText(game[3]);
            genre.setTextSize(12);
            genre.setTextColor(0xFF888888);
            textLayout.addView(genre);

            card.addView(textLayout);

            final String gameId = game[0];
            card.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("game_url", BASE_URL + gameId + "/index.html");
                intent.putExtra("game_name", game[1]);
                startActivity(intent);
            });

            layout.addView(card);
        }

        scrollView.addView(layout);
        rootLayout.addView(scrollView);

        // Banner ad at bottom
        bannerAdView = new AdView(this);
        bannerAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        bannerAdView.setAdSize(AdSize.BANNER);
        FrameLayout.LayoutParams adParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );
        rootLayout.addView(bannerAdView, adParams);
        bannerAdView.loadAd(new AdRequest.Builder().build());

        setContentView(rootLayout);
    }

    @Override
    protected void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }
}
