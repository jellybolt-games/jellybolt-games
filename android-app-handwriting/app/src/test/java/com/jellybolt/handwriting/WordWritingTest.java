package com.jellybolt.handwriting;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.jellybolt.handwriting.core.Alphabet;
import com.jellybolt.handwriting.core.DefaultSamples;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@LooperMode(LooperMode.Mode.PAUSED)
public class WordWritingTest {
    private Context context;
    private MainActivity activity;
    private ActivityController<MainActivity> controller;
    private ProfileStore store;
    private long time;

    @Before public void before() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("handwriting-ui", 0).edit().clear().putString("language", "en").commit();
        context.getSharedPreferences(WritingSettings.PREFERENCES, 0).edit().clear().commit();
        context.getSharedPreferences(AutoInsertSettings.PREFERENCES, 0).edit().clear().commit();
        store = new ProfileStore(context);
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
    }

    @After public void after() {
        controller.pause().stop().destroy();
        store.close();
    }

    @Test public void mirroredDefaultsAreEnabledAndWholeWordModeIsOptional() {
        assertTrue(((CheckBox) activity.findViewById(R.id.mirrored_handwriting)).isChecked());
        assertFalse(WritingSettings.wordMode(context));
        assertTrue(WritingSettings.mirrored(context));
        assertFalse(WritingSettings.reverseOrder(context));
    }

    @Test public void fullNumberIsInsertedInOnePassAfterTheLongerWordPause() throws Exception {
        wordMode(R.id.train_digits);
        draw(compose(Alphabet.DIGITS, "57"));
        advance(1999);
        assertEquals("", output());
        advance(1);
        await(() -> !output().isEmpty());
        assertEquals("57", output());
        assertTrue(drawing().getInk().isEmpty());
        assertTrue(store.profiles().isEmpty());
        advance(4000);
        assertEquals("57", output());
    }

    @Test public void manualWordRecognitionAndCorrectionInsertEntireTextWithoutTraining() throws Exception {
        long profile = store.createProfile("Learner").id;
        context.getSharedPreferences("handwriting-ui", 0).edit().putLong("profile", profile).commit();
        controller.pause().stop().destroy();
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
        AutoInsertSettings.select(context, 0);
        wordMode(R.id.train_english_lower);
        drawing().setInk(compose(Alphabet.ENGLISH_LOWER, "cat"));
        activity.findViewById(R.id.recognize_button).performClick();
        await(() -> activity.findViewById(R.id.confirm_character_button).isEnabled());
        assertEquals("", output());
        assertFalse(((CheckBox) activity.findViewById(R.id.learn_checkbox)).isEnabled());
        click(R.string.word_correct);
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        EditText correction = findType(dialog.getWindow().getDecorView(), EditText.class);
        assertNotNull(correction);
        correction.setText("cat7");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        ((CheckBox) activity.findViewById(R.id.learn_checkbox)).setChecked(true);
        activity.findViewById(R.id.confirm_character_button).performClick();
        assertEquals("cat7", output());
        assertTrue(store.examples(profile, Alphabet.ENGLISH_LOWER).isEmpty());
        assertTrue(store.examples(profile, Alphabet.DIGITS).isEmpty());
    }

    @Test public void hebrewWordUsesRightToLeftSpatialOrderWithoutReversingOutputAgain() throws Exception {
        AutoInsertSettings.select(context, 0);
        wordMode(R.id.train_hebrew);
        // Geometric left-to-right samples become logical shin-lamed-mem.
        drawing().setInk(compose(Alphabet.HEBREW, "\u05de\u05dc\u05e9"));
        activity.findViewById(R.id.recognize_button).performClick();
        await(() -> activity.findViewById(R.id.confirm_character_button).isEnabled());
        activity.findViewById(R.id.confirm_character_button).performClick();
        assertEquals("\u05e9\u05dc\u05de", output());
    }

    @Test public void hebrewScriptAutomaticallyInsertsOrdinaryLetterWithoutAStyleSwitchOrProfile() throws Exception {
        click(R.string.writing_mode);
        activity.findViewById(R.id.train_hebrew).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        drawing().layout(0, 0, 500, 220);
        draw(composeGlyphs(hebrewScript("\u05d0")));
        advance(1200);
        await(() -> !output().isEmpty());
        assertEquals("\u05d0", output());
        assertFalse(WritingSettings.wordMode(context));
        assertTrue(store.profiles().isEmpty());
    }

    @Test public void hebrewWordCombinesScriptAndPrintWithoutChangingItsAlphabet() throws Exception {
        wordMode(R.id.train_hebrew);
        Ink printedBet = DefaultSamples.examples(Alphabet.HEBREW).stream()
                .filter(example -> example.label.equals("\u05d1")).findFirst().get().ink;
        draw(composeGlyphs(hebrewScript("\u05d0"), printedBet, hebrewScript("\u05d0")));
        advance(2000);
        await(() -> !output().isEmpty());
        assertEquals("\u05d0\u05d1\u05d0", output());
        assertTrue(store.profiles().isEmpty());
        activity.findViewById(R.id.undo_auto_insert).performClick();
        assertEquals("", output());
        assertFalse(drawing().getInk().isEmpty());
    }

    @Test public void wholeWordUndoRemovesOnlyTheLatestInsertedSequence() throws Exception {
        wordMode(R.id.train_digits);
        draw(compose(Alphabet.DIGITS, "57"));
        advance(2000);
        await(() -> !output().isEmpty());
        String first = output();
        draw(compose(Alphabet.DIGITS, "23"));
        advance(2000);
        await(() -> output().length() > first.length());
        activity.findViewById(R.id.undo_auto_insert).performClick();
        assertEquals(first, output());
        assertFalse(drawing().getInk().isEmpty());
        advance(4000);
        assertEquals(first, output());
    }

    @Test public void wordModeAndMirrorChangesCancelPendingInsertion() {
        wordMode(R.id.train_digits);
        draw(compose(Alphabet.DIGITS, "57"));
        advance(1000);
        ((CheckBox) activity.findViewById(R.id.mirrored_handwriting)).setChecked(false);
        advance(4000);
        assertEquals("", output());
        draw(compose(Alphabet.DIGITS, "57"));
        ((CheckBox) activity.findViewById(R.id.whole_word_mode)).setChecked(false);
        advance(4000);
        assertEquals("", output());
    }

    @Test public void inseparableLineKeepsInkAndRequestsSpacingWithoutAutomaticInsertion() throws Exception {
        wordMode(R.id.train_english_lower);
        Ink joined = new Ink(java.util.Collections.singletonList(java.util.Arrays.asList(
                new Ink.Point(20, 100), new Ink.Point(400, 102))));
        draw(joined);
        advance(2000);
        await(() -> activity.getString(R.string.word_separation_needed).equals(
                ((TextView) activity.findViewById(R.id.status_text)).getText().toString()));
        assertEquals("", output());
        assertFalse(drawing().getInk().isEmpty());
        assertFalse(activity.findViewById(R.id.confirm_character_button).isEnabled());
        advance(4000);
        assertEquals("", output());
        assertTrue(store.profiles().isEmpty());
    }

    @Test public void wordCorrectionRejectsUnsupportedCharactersAndDoesNotOverflowDraft() {
        wordMode(R.id.train_digits);
        click(R.string.word_correct);
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        EditText correction = findType(dialog.getWindow().getDecorView(), EditText.class);
        correction.setText("A");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        assertNotNull(correction.getError());
        assertTrue(dialog.isShowing());
        assertEquals("", output());
    }

    @Test public void leavingWordModeNeverSubmitsAnAlreadyDrawnWordAsOneLetter() {
        wordMode(R.id.train_english_lower);
        draw(compose(Alphabet.ENGLISH_LOWER, "cat"));
        click(R.string.training_mode);
        advance(4000);
        assertEquals("", output());
        assertTrue(store.profiles().isEmpty());
    }

    @Test public void wordOptionsSurviveReopeningButDraftDoesNotAutomaticallySubmit() {
        wordMode(R.id.train_hebrew);
        ((CheckBox) activity.findViewById(R.id.reverse_word_order)).setChecked(true);
        ((CheckBox) activity.findViewById(R.id.mirrored_handwriting)).setChecked(false);
        draw(compose(Alphabet.HEBREW, "\u05dc\u05de"));
        android.os.Bundle saved = new android.os.Bundle();
        controller.saveInstanceState(saved).pause().stop().destroy();
        controller = Robolectric.buildActivity(MainActivity.class).create(saved).start().resume().visible();
        activity = controller.get();
        assertTrue(WritingSettings.wordMode(context));
        assertTrue(WritingSettings.reverseOrder(context));
        assertFalse(WritingSettings.mirrored(context));
        advance(4000);
        assertEquals("", output());
    }

    @Test public void fullWordCannotExceedTheRemainingOutputCapacity() throws Exception {
        controller.pause().stop().destroy();
        android.os.Bundle saved = new android.os.Bundle();
        saved.putLong("profile", -1);
        saved.putBoolean("training", false);
        saved.putString("group", Alphabet.DIGITS);
        saved.putString("output", "1".repeat(4095));
        controller = Robolectric.buildActivity(MainActivity.class).create(saved).start().resume().visible();
        activity = controller.get();
        wordMode(R.id.train_digits);
        AutoInsertSettings.select(context, 0);
        drawing().setInk(compose(Alphabet.DIGITS, "57"));
        activity.findViewById(R.id.recognize_button).performClick();
        await(() -> activity.findViewById(R.id.confirm_character_button).isEnabled());
        activity.findViewById(R.id.confirm_character_button).performClick();
        assertEquals(4095, output().length());
        assertFalse(drawing().getInk().isEmpty());
        assertEquals(activity.getString(R.string.output_limit),
                ((TextView) activity.findViewById(R.id.status_text)).getText().toString());
    }

    private void wordMode(int groupId) {
        click(R.string.writing_mode);
        ((CheckBox) activity.findViewById(R.id.whole_word_mode)).setChecked(true);
        activity.findViewById(groupId).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        drawing().layout(0, 0, 500, 220);
    }

    private static Ink compose(String alphabet, String leftToRight) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (int i = 0; i < leftToRight.length(); i++) {
            String label = leftToRight.substring(i, i + 1);
            Ink glyph = DefaultSamples.examples(alphabet).stream()
                    .filter(example -> example.label.equals(label)).findFirst().get().ink;
            for (List<Ink.Point> stroke : glyph.strokes) {
                List<Ink.Point> points = new ArrayList<>();
                for (Ink.Point p : stroke) points.add(new Ink.Point(20 + i * 90 + p.x * 70, 30 + p.y * 120));
                strokes.add(points);
            }
        }
        return new Ink(strokes);
    }

    private static Ink hebrewScript(String label) {
        for (HandwritingRecognizer.Example example : DefaultSamples.hebrewScriptExamples()) {
            if (example.label.equals(label)) return example.ink;
        }
        throw new AssertionError("Missing Hebrew script starter " + label);
    }

    private static Ink composeGlyphs(Ink... glyphs) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (int i = 0; i < glyphs.length; i++) {
            for (List<Ink.Point> stroke : glyphs[i].strokes) {
                List<Ink.Point> points = new ArrayList<>();
                for (Ink.Point p : stroke) points.add(new Ink.Point(20 + i * 140 + p.x * 110, 30 + p.y * 110));
                strokes.add(points);
            }
        }
        return new Ink(strokes);
    }

    private void draw(Ink ink) {
        drawing().clear();
        for (List<Ink.Point> stroke : ink.strokes) {
            for (int i = 0; i < stroke.size(); i++) {
                Ink.Point p = stroke.get(i);
                int action = i == 0 ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_MOVE;
                event(action, p.x, p.y);
            }
            Ink.Point last = stroke.get(stroke.size() - 1);
            event(MotionEvent.ACTION_UP, last.x, last.y);
        }
    }

    private void event(int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(0, time += 16, action, x, y, 0);
        try { assertTrue(drawing().onTouchEvent(event)); } finally { event.recycle(); }
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
    private static Button findButton(View view, String text) {
        if (view instanceof Button && ((Button) view).getText().toString().equals(text)) return (Button) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                Button found = findButton(group.getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }
    private static <T extends View> T findType(View view, Class<T> type) {
        if (type.isInstance(view)) return type.cast(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                T found = findType(group.getChildAt(i), type);
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
        fail("Word recognition did not finish");
    }
}
