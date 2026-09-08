package com.jellybolt.handwriting;

import android.os.Looper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.time.Duration;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@LooperMode(LooperMode.Mode.PAUSED)
public class AutoInsertControllerTest {
    @Test public void runsExactlyOnceAfterTheRequestedPause() {
        AutoInsertController controller = new AutoInsertController();
        int[] calls = {0};
        controller.arm(1200, () -> calls[0]++);
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1199));
        assertEquals(0, calls[0]);
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1));
        assertEquals(1, calls[0]);
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(10));
        assertEquals(1, calls[0]);
    }

    @Test public void replacementResetsTheFullPauseAndCancellationPreventsExecution() {
        AutoInsertController controller = new AutoInsertController();
        int[] calls = {0};
        controller.arm(1200, () -> calls[0] += 100);
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(800));
        controller.arm(1200, () -> calls[0]++);
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(800));
        assertEquals(0, calls[0]);
        controller.cancel();
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2));
        assertEquals(0, calls[0]);
    }

    @Test public void disabledDelayNeverRunsAndInvalidInputsAreRejected() {
        AutoInsertController controller = new AutoInsertController();
        controller.arm(0, () -> fail("Disabled automatic input must not fire"));
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(10));
        assertThrows(IllegalArgumentException.class, () -> controller.arm(-1, () -> {}));
        assertThrows(IllegalArgumentException.class, () -> controller.arm(1200, null));
    }

    @Test public void sharedSettingHasAStableDefaultAndSupportsOffAndLongerPauses() {
        android.content.Context context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences(AutoInsertSettings.PREFERENCES, 0).edit().clear().commit();
        assertEquals(1200, AutoInsertSettings.delay(context));
        assertEquals(2, AutoInsertSettings.selection(context));
        AutoInsertSettings.select(context, 0);
        assertEquals(0, AutoInsertSettings.delay(context));
        AutoInsertSettings.select(context, 3);
        assertEquals(2000, AutoInsertSettings.delay(context));
        AutoInsertSettings.select(context, 1);
        assertEquals(800, AutoInsertSettings.delay(context));
        assertThrows(IllegalArgumentException.class, () -> AutoInsertSettings.select(context, 4));
    }
}
