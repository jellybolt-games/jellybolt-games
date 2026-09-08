package com.jellybolt.handwriting;

import android.content.Context;

public final class AutoInsertSettings {
    public static final String PREFERENCES = "handwriting-auto-insert";
    public static final String DELAY_KEY = "pause-millis";
    public static final int DEFAULT_DELAY = 1200;
    private static final int[] DELAYS = {0, 800, 1200, 2000};

    private AutoInsertSettings() {}

    public static int delay(Context context) {
        int saved = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getInt(DELAY_KEY, DEFAULT_DELAY);
        for (int delay : DELAYS) if (delay == saved) return saved;
        return DEFAULT_DELAY;
    }

    public static int selection(Context context) {
        int current = delay(context);
        for (int i = 0; i < DELAYS.length; i++) if (DELAYS[i] == current) return i;
        throw new IllegalStateException("Invalid automatic insertion setting");
    }

    public static void select(Context context, int index) {
        if (index < 0 || index >= DELAYS.length) throw new IllegalArgumentException("Invalid pause choice");
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
                .putInt(DELAY_KEY, DELAYS[index]).apply();
    }
}
