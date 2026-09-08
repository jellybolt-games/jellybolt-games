package com.jellybolt.handwriting;

import android.os.Handler;
import android.os.Looper;

/** Only arms after a completed stroke; hosts cancel on edits, navigation, or lifecycle changes. */
public final class AutoInsertController {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;
    private long revision;

    public void arm(int delayMillis, Runnable action) {
        cancel();
        if (delayMillis < 0) throw new IllegalArgumentException("Pause cannot be negative");
        if (action == null) throw new IllegalArgumentException("Insertion action is required");
        if (delayMillis == 0) return;
        long scheduled = revision;
        pending = () -> {
            if (scheduled != revision) return;
            pending = null;
            action.run();
        };
        handler.postDelayed(pending, delayMillis);
    }

    public void cancel() {
        revision++;
        if (pending != null) handler.removeCallbacks(pending);
        pending = null;
    }
}
