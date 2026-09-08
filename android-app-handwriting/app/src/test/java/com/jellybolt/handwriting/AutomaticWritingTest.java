package com.jellybolt.handwriting;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.jellybolt.handwriting.core.Alphabet;
import com.jellybolt.handwriting.core.HandwritingRecognizer;
import com.jellybolt.handwriting.core.Ink;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowAlertDialog;

import java.time.Duration;
import java.util.Collections;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@LooperMode(LooperMode.Mode.PAUSED)
public class AutomaticWritingTest {
    private Context context;
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private ProfileStore store;
    private long eventTime;

    @Before public void before() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("handwriting-ui", 0).edit().clear().putString("language", "en").commit();
        context.getSharedPreferences(AutoInsertSettings.PREFERENCES, 0).edit().clear().commit();
        store = new ProfileStore(context);
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
    }

    @After public void after() {
        controller.pause().stop().destroy();
        store.close();
    }

    @Test public void freshAppUsesStarterShapesAndInsertsAfterPauseWithoutAProfile() throws Exception {
        writing();
        assertTrue(drawing().isEnabled());
        stroke();
        String predicted = predict();
        advance(1199);
        assertEquals("", output());
        advance(1);
        await(() -> !output().isEmpty());
        assertEquals(predicted, output());
        assertTrue(drawing().getInk().isEmpty());
        assertTrue(store.profiles().isEmpty());
        advance(3000);
        assertEquals(predicted, output());
    }

    @Test public void extraStrokeResetsTimerAndHeldFingerCannotInsert() throws Exception {
        writing();
        stroke();
        advance(900);
        event(MotionEvent.ACTION_DOWN, 30, 60);
        advance(1500);
        assertEquals("", output());
        event(MotionEvent.ACTION_MOVE, 150, 60);
        event(MotionEvent.ACTION_UP, 150, 60);
        advance(1199);
        assertEquals("", output());
        advance(1);
        await(() -> !output().isEmpty());
        assertEquals(1, output().length());
    }

    @Test public void cancellationDoesNotAutoSubmitTheEarlierStrokes() {
        writing();
        stroke();
        advance(500);
        event(MotionEvent.ACTION_DOWN, 30, 60);
        event(MotionEvent.ACTION_CANCEL, 150, 60);
        advance(3000);
        assertEquals("", output());
    }

    @Test public void trainingNeverAutomaticallySavesOrInserts() {
        ProfileStore.Profile profile = store.createProfile("Learner");
        context.getSharedPreferences("handwriting-ui", 0).edit().putLong("profile", profile.id).commit();
        controller.pause().stop().destroy();
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
        stroke();
        advance(3000);
        assertEquals("", output());
        assertTrue(store.examples(profile.id, Alphabet.DIGITS).isEmpty());
    }

    @Test public void offSettingPreservesManualRecognitionAndConfirmation() throws Exception {
        AutoInsertSettings.select(context, 0);
        writing();
        stroke();
        advance(3000);
        assertEquals("", output());
        activity.findViewById(R.id.recognize_button).performClick();
        await(() -> candidate() != null);
        candidate().performClick();
        activity.findViewById(R.id.confirm_character_button).performClick();
        assertEquals(1, output().length());
    }

    @Test public void changingDelayOrAlphabetCancelsExistingTimer() {
        writing();
        stroke();
        advance(500);
        ((Spinner) activity.findViewById(R.id.auto_insert_delay)).setSelection(0);
        layout();
        shadowOf(Looper.getMainLooper()).idle();
        advance(3000);
        assertEquals("", output());
        AutoInsertSettings.select(context, 2);
        stroke();
        activity.findViewById(R.id.train_hebrew).performClick();
        advance(3000);
        assertEquals("", output());
        assertTrue(drawing().getInk().isEmpty());
    }

    @Test public void leavingWritingOrPausingCancelsPendingInsertion() {
        writing();
        stroke();
        click(R.string.training_mode);
        advance(3000);
        assertEquals("", output());
        writing();
        stroke();
        controller.pause();
        advance(3000);
        assertEquals("", output());
        controller.resume();
        advance(3000);
        assertEquals("", output());
    }

    @Test public void undoRestoresInkForCorrectionWithoutAutomaticallyReinserting() throws Exception {
        writing();
        stroke();
        Ink draft = drawing().getInk();
        advance(1200);
        await(() -> !output().isEmpty());
        activity.findViewById(R.id.undo_auto_insert).performClick();
        assertEquals("", output());
        assertArrayEquals(draft.encode(), drawing().getInk().encode());
        advance(3000);
        assertEquals("", output());
        click(R.string.manual_correction);
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getListView().performItemClick(null, 7, 7);
        shadowOf(Looper.getMainLooper()).idle();
        activity.findViewById(R.id.confirm_character_button).performClick();
        assertEquals("7", output());
        assertTrue(store.profiles().isEmpty());
    }

    @Test public void autoInsertNeverLearnsEvenWithForcedConsent() throws Exception {
        long profile = store.createProfile("Learner").id;
        context.getSharedPreferences("handwriting-ui", 0).edit().putLong("profile", profile).commit();
        controller.pause().stop().destroy();
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
        writing();
        stroke();
        ((CheckBox) activity.findViewById(R.id.learn_checkbox)).setChecked(true);
        advance(1200);
        await(() -> !output().isEmpty());
        assertTrue(store.examples(profile, Alphabet.DIGITS).isEmpty());
    }

    @Test public void openingManualCorrectionCancelsAutomaticInsertion() {
        writing();
        stroke();
        click(R.string.manual_correction);
        advance(3000);
        assertEquals("", output());
    }

    @Test public void uncertaintyDoesNotForceAManualButtonWhenAutoIsEnabled() throws Exception {
        long profile = store.createProfile("Learner").id;
        context.getSharedPreferences("handwriting-ui", 0).edit().putLong("profile", profile).commit();
        controller.pause().stop().destroy();
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
        writing();
        stroke();
        Ink sameShape = drawing().getInk();
        store.addExample(profile, Alphabet.DIGITS, "0", sameShape);
        store.addExample(profile, Alphabet.DIGITS, "9", sameShape);
        advance(1200);
        await(() -> !output().isEmpty());
        assertEquals(1, output().length());
        assertEquals(activity.getString(R.string.auto_insert_uncertain),
                ((TextView) activity.findViewById(R.id.status_text)).getText().toString());
        assertEquals(2, store.examples(profile, Alphabet.DIGITS).size());
    }

    @Test public void undoCannotDeleteTextFromAnEarlierAutomaticInsertion() throws Exception {
        writing();
        stroke();
        advance(1200);
        await(() -> output().length() == 1);
        String first = output();
        stroke();
        advance(1200);
        await(() -> output().length() == 2);
        activity.findViewById(R.id.undo_auto_insert).performClick();
        assertEquals(first, output());
        assertTrue(activity.findViewById(R.id.undo_auto_insert).getVisibility() == View.GONE);
    }

    @Test public void restoredInkDoesNotStartAutomaticInsertionUntilAnotherUserStroke() {
        writing();
        Ink restored = new Ink(Collections.singletonList(java.util.Arrays.asList(
                new Ink.Point(100, 100), new Ink.Point(100, 800))));
        drawing().setInk(restored);
        advance(4000);
        assertEquals("", output());
    }

    private void writing() {
        click(R.string.writing_mode);
        layout();
    }

    private void layout() {
        View root = activity.findViewById(R.id.window_content);
        root.measure(View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, 400, 800);
        drawing().layout(0, 0, 240, 240);
    }

    private void stroke() {
        event(MotionEvent.ACTION_DOWN, 120, 20);
        event(MotionEvent.ACTION_MOVE, 120, 120);
        event(MotionEvent.ACTION_UP, 120, 200);
    }

    private void event(int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(0, eventTime += 16, action, x, y, 0);
        try {
            assertTrue(drawing().onTouchEvent(event));
        } finally { event.recycle(); }
    }

    private String predict() {
        return HandwritingRecognizer.recognize(drawing().getInk(), Alphabet.DIGITS,
                Collections.emptyList()).candidates.get(0).label;
    }

    private DrawingView drawing() { return activity.findViewById(R.id.drawing_area); }
    private String output() { return ((TextView) activity.findViewById(R.id.output_text)).getText().toString(); }
    private void advance(long millis) { shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis)); }

    private void click(int resource) {
        Button button = findButton(activity.getWindow().getDecorView(), activity.getString(resource));
        assertNotNull(button);
        button.performClick();
        shadowOf(Looper.getMainLooper()).idle();
    }

    private Button candidate() {
        return findButton(activity.getWindow().getDecorView(), "match similarity");
    }

    private static Button findButton(View view, String text) {
        if (view instanceof Button && ((Button) view).getText().toString().contains(text)) return (Button) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                Button found = findButton(group.getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + 5_000_000_000L;
        do {
            shadowOf(Looper.getMainLooper()).idle();
            if (condition.getAsBoolean()) return;
            Thread.sleep(10);
        } while (System.nanoTime() < deadline);
        fail("Automatic recognition did not finish");
    }
}
