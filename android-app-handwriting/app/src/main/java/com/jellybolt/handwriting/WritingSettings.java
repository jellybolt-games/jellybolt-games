package com.jellybolt.handwriting;

import android.content.Context;
import android.content.SharedPreferences;

/** Shared recognition choices; no handwritten or editor text is saved here. */
public final class WritingSettings {
    public static final String PREFERENCES = "handwriting-recognition-options";
    public static final String WORD_MODE = "whole-word";
    public static final String MIRRORED = "mirrored-shapes";
    public static final String REVERSE_ORDER = "reverse-reading-order";

    private WritingSettings() {}

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static boolean wordMode(Context context) { return preferences(context).getBoolean(WORD_MODE, false); }
    public static boolean mirrored(Context context) { return preferences(context).getBoolean(MIRRORED, true); }
    public static boolean reverseOrder(Context context) { return preferences(context).getBoolean(REVERSE_ORDER, false); }

    public static void setWordMode(Context context, boolean value) {
        preferences(context).edit().putBoolean(WORD_MODE, value).apply();
    }
    public static void setMirrored(Context context, boolean value) {
        preferences(context).edit().putBoolean(MIRRORED, value).apply();
    }
    public static void setReverseOrder(Context context, boolean value) {
        preferences(context).edit().putBoolean(REVERSE_ORDER, value).apply();
    }

    public static int delay(Context context) {
        int configured = AutoInsertSettings.delay(context);
        return configured == 0 ? 0 : wordMode(context) ? Math.max(2000, configured) : configured;
    }
}
