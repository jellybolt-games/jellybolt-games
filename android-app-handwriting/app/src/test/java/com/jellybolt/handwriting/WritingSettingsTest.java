package com.jellybolt.handwriting;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class WritingSettingsTest {
    @Test public void mirrorsAreAvailableByDefaultButWholeWordsAreOptIn() {
        android.content.Context context = RuntimeEnvironment.getApplication();
        assertTrue(WritingSettings.mirrored(context));
        assertFalse(WritingSettings.wordMode(context));
        assertFalse(WritingSettings.reverseOrder(context));
        assertEquals(1200, WritingSettings.delay(context));
    }

    @Test public void wholeWordPauseHasATwoSecondFloorButOffStaysOff() {
        android.content.Context context = RuntimeEnvironment.getApplication();
        WritingSettings.setWordMode(context, true);
        assertEquals(2000, WritingSettings.delay(context));
        AutoInsertSettings.select(context, 1);
        assertEquals(2000, WritingSettings.delay(context));
        AutoInsertSettings.select(context, 0);
        assertEquals(0, WritingSettings.delay(context));
        AutoInsertSettings.select(context, 3);
        assertEquals(2000, WritingSettings.delay(context));
        WritingSettings.setWordMode(context, false);
        AutoInsertSettings.select(context, 2);
        assertEquals(1200, WritingSettings.delay(context));
    }

    @Test public void mirrorAndReadingOrderAreIndependentPersistentChoices() {
        android.content.Context context = RuntimeEnvironment.getApplication();
        WritingSettings.setMirrored(context, false);
        WritingSettings.setReverseOrder(context, true);
        WritingSettings.setWordMode(context, true);
        assertFalse(WritingSettings.mirrored(context));
        assertTrue(WritingSettings.reverseOrder(context));
        assertTrue(WritingSettings.wordMode(context));
        assertEquals(3, context.getSharedPreferences(WritingSettings.PREFERENCES, 0).getAll().size());
    }
}
