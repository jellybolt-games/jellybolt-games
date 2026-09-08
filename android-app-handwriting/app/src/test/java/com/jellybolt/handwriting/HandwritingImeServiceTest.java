package com.jellybolt.handwriting;

import android.content.Context;
import android.content.Intent;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
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
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@LooperMode(LooperMode.Mode.PAUSED)
public class HandwritingImeServiceTest {
    private Context context;
    private ProfileStore store;
    private ServiceController<TestIme> controller;
    private TestIme service;
    private RecordingConnection connection;
    private View root;
    private long profileId;
    private final Ink ink = new Ink(Collections.singletonList(Arrays.asList(
            new Ink.Point(100, 100), new Ink.Point(700, 200), new Ink.Point(300, 900))));

    public static class TestIme extends HandwritingImeService {
        InputConnection connection;

        @Override protected InputConnection editorConnection() {
            return connection;
        }
    }

    private static class RecordingConnection extends BaseInputConnection {
        final Editable text = new SpannableStringBuilder();
        int action = -1;
        boolean accept = true;
        boolean allowRead = true;
        int commits;
        int deletes;

        RecordingConnection(Context context) {
            super(new View(context), true);
            Selection.setSelection(text, 0);
        }

        @Override public Editable getEditable() {
            return text;
        }

        @Override public boolean commitText(CharSequence value, int cursor) {
            commits++;
            return accept && super.commitText(value, cursor);
        }

        @Override public boolean deleteSurroundingText(int before, int after) {
            deletes++;
            return super.deleteSurroundingText(before, after);
        }

        @Override public CharSequence getTextBeforeCursor(int length, int flags) {
            return allowRead ? super.getTextBeforeCursor(length, flags) : null;
        }

        @Override public boolean performEditorAction(int id) {
            if (!accept) return false;
            action = id;
            return true;
        }
    }

    @Before public void setUp() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE).edit()
                .clear().putString("language", "en").commit();
        context.getSharedPreferences("handwriting-ime", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(AutoInsertSettings.PREFERENCES, Context.MODE_PRIVATE)
                .edit().clear().commit();
        context.getSharedPreferences(WritingSettings.PREFERENCES, Context.MODE_PRIVATE)
                .edit().clear().commit();
        store = new ProfileStore(context);
    }

    @After public void tearDown() {
        if (controller != null) controller.destroy();
        store.close();
        shadowOf(Looper.getMainLooper()).idle();
    }

    private EditorInfo editor(int inputType, int options) {
        EditorInfo info = new EditorInfo();
        info.inputType = inputType;
        info.imeOptions = options;
        info.initialSelStart = info.initialSelEnd = 0;
        return info;
    }

    private EditorInfo textEditor() {
        return editor(InputType.TYPE_CLASS_TEXT, EditorInfo.IME_ACTION_DONE);
    }

    private void createProfile() {
        profileId = store.createProfile("Local learner").id;
        context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE).edit()
                .putLong("profile", profileId).commit();
    }

    private void launch(boolean handwriting, EditorInfo info) {
        context.getSharedPreferences("handwriting-ime", Context.MODE_PRIVATE).edit()
                .putBoolean("handwriting", handwriting).commit();
        controller = Robolectric.buildService(TestIme.class).create();
        service = controller.get();
        connection = new RecordingConnection(context);
        service.connection = connection;
        service.onStartInput(info, false);
        root = service.onCreateInputView();
        layoutKeyboard();
    }

    @Test public void typingWorksWithoutAnyProfileAndNeverLearnsTypedText() {
        launch(false, textEditor());
        clickKey(R.id.ime_key_rows, "q");
        click(R.id.ime_shift);
        clickKey(R.id.ime_key_rows, "A");
        click(R.id.ime_space);
        clickKey(R.id.ime_key_rows, "b");
        assertEquals("qA b", connection.text.toString());
        assertTrue(store.profiles().isEmpty());
        assertEquals("Inserted", status());
        assertFalse(service.onEvaluateFullscreenMode());
    }

    @Test public void hebrewLayoutKeepsLogicalOrderAndContainsAllFinalForms() {
        launch(false, textEditor());
        click(R.id.ime_typing_language);
        for (String letter : Arrays.asList("ש", "ל", "ו", "ם", "ך", "ן", "ף", "ץ")) {
            clickKey(R.id.ime_key_rows, letter);
        }
        assertEquals("שלוםךןףץ", connection.text.toString());
        ViewGroup rows = root.findViewById(R.id.ime_key_rows);
        for (int i = 0; i < rows.getChildCount(); i++) {
            assertEquals(View.LAYOUT_DIRECTION_LTR, rows.getChildAt(i).getLayoutDirection());
        }
    }

    @Test public void symbolLayoutAndBackspaceUseTheCurrentEditor() {
        launch(false, textEditor());
        click(R.id.ime_symbols);
        clickKey(R.id.ime_key_rows, "7");
        clickKey(R.id.ime_key_rows, "@");
        click(R.id.ime_backspace);
        assertEquals("7", connection.text.toString());
        click(R.id.ime_enter);
        assertEquals(EditorInfo.IME_ACTION_DONE, connection.action);
    }

    @Test public void secondSymbolPageSupportsPasswordAndProgrammingPunctuation() {
        launch(false, textEditor());
        click(R.id.ime_symbols);
        click(R.id.ime_symbol_page);
        for (String symbol : Arrays.asList("_", "\\", "|", "[", "]", "{", "}", "<", ">", "~", "`", "^")) {
            clickKey(R.id.ime_key_rows, symbol);
        }
        assertEquals("_\\|[]{}<>~`^", connection.text.toString());
        click(R.id.ime_symbol_page);
        clickKey(R.id.ime_key_rows, "7");
        assertTrue(connection.text.toString().endsWith("7"));
    }

    @Test public void actionLabelsAndCustomActionsFollowEachEditor() {
        launch(false, editor(InputType.TYPE_CLASS_TEXT, EditorInfo.IME_ACTION_SEARCH));
        assertEquals("Search", ((Button) root.findViewById(R.id.ime_enter)).getText().toString());
        click(R.id.ime_enter);
        assertEquals(EditorInfo.IME_ACTION_SEARCH, connection.action);
        EditorInfo next = textEditor();
        next.actionLabel = "Continue";
        next.actionId = 90;
        service.onStartInput(next, false);
        assertEquals("Continue", ((Button) root.findViewById(R.id.ime_enter)).getText().toString());
        click(R.id.ime_enter);
        assertEquals(90, connection.action);
        next = editor(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE,
                EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        service.onStartInput(next, false);
        click(R.id.ime_enter);
        assertEquals("\n", connection.text.toString());
    }

    @Test public void numericAndPhoneEditorsNeverOfferLetterLayouts() {
        launch(false, editor(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_VARIATION_PASSWORD, EditorInfo.IME_ACTION_DONE));
        assertFalse(root.findViewById(R.id.ime_typing_language).isEnabled());
        click(R.id.ime_typing_language);
        assertNotNull(findKey(root.findViewById(R.id.ime_key_rows), "7"));
        assertNull(findKey(root.findViewById(R.id.ime_key_rows), "q"));
        click(R.id.ime_mode);
        Spinner alphabet = root.findViewById(R.id.ime_alphabet);
        assertEquals(0, alphabet.getSelectedItemPosition());
        assertFalse(alphabet.isEnabled());
        choose("7");
        click(R.id.ime_confirm);
        assertEquals("7", connection.text.toString());
        service.onStartInput(editor(InputType.TYPE_CLASS_PHONE, EditorInfo.IME_ACTION_NEXT), false);
        assertEquals(0, ((Spinner) root.findViewById(R.id.ime_alphabet)).getSelectedItemPosition());
    }

    @Test public void absentAndRejectingConnectionsProduceFeedback() {
        launch(false, textEditor());
        service.connection = null;
        clickKey(R.id.ime_key_rows, "a");
        assertEquals(context.getString(R.string.ime_input_failed), status());
        click(R.id.ime_backspace);
        assertEquals(context.getString(R.string.ime_input_failed), status());
        service.connection = connection;
        connection.accept = false;
        clickKey(R.id.ime_key_rows, "b");
        click(R.id.ime_enter);
        assertEquals("", connection.text.toString());
        assertEquals(context.getString(R.string.ime_input_failed), status());
    }

    @Test public void noProfilesStillAllowsManualConfirmationWithoutEmptyLearning() throws Exception {
        launch(true, textEditor());
        await(() -> !status().equals(context.getString(R.string.ime_loading_profiles)));
        assertFalse(root.findViewById(R.id.ime_recognize).isEnabled());
        choose("7");
        assertEquals("", connection.text.toString());
        assertFalse(learn().isEnabled());
        learn().setChecked(true);
        click(R.id.ime_confirm);
        assertEquals("7", connection.text.toString());
        assertTrue(store.profiles().isEmpty());
        assertFalse(learn().isChecked());
        assertEquals(context.getString(R.string.ime_nothing_selected), selected());
        assertFalse(status().contains("7"));
    }

    @Test public void explicitDrawingCorrectionLearnsOnlyAfterSuccessfulInsertion() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        drawing().setInk(ink);
        choose("7");
        assertFalse(learn().isChecked());
        learn().setChecked(true);
        click(R.id.ime_confirm);
        assertEquals("7", connection.text.toString());
        await(() -> status().equals(context.getString(R.string.ime_saved)));
        assertEquals("7", store.examples(profileId, Alphabet.DIGITS).get(0).label);
        assertTrue(drawing().getInk().isEmpty());
        assertFalse(learn().isChecked());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        assertFalse(selected().contains("7"));
        assertFalse(status().contains("7"));
    }

    @Test public void failedHandwritingInsertionDoesNotSaveAndResetsConsent() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        drawing().setInk(ink);
        choose("7");
        learn().setChecked(true);
        connection.accept = false;
        click(R.id.ime_confirm);
        assertEquals(context.getString(R.string.ime_input_failed), status());
        assertFalse(learn().isChecked());
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
        connection.accept = true;
        click(R.id.ime_confirm);
        assertEquals("7", connection.text.toString());
        service.onStartInput(textEditor(), false);
        awaitProfiles();
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
    }

    @Test public void failedSampleSaveAfterInsertionCannotDuplicateTheAcceptedCharacter() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        drawing().setInk(ink);
        choose("7");
        learn().setChecked(true);
        // Simulate a profile removed in the training app after the keyboard loaded its list.
        store.deleteProfile(profileId);
        click(R.id.ime_confirm);
        assertEquals("7", connection.text.toString());
        assertTrue(drawing().getInk().isEmpty());
        assertFalse(learn().isChecked());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        await(() -> status().equals(context.getString(R.string.ime_save_error)));
        assertEquals(context.getString(R.string.ime_nothing_selected), selected());
        assertFalse(status().contains("7"));
        click(R.id.ime_confirm);
        assertEquals("7", connection.text.toString());
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
    }

    @Test public void emptyInkCannotBeSavedEvenWithForcedConsent() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        choose("4");
        learn().setChecked(true);
        click(R.id.ime_confirm);
        assertEquals("4", connection.text.toString());
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
    }

    @Test public void passwordVariantsAndNoLearningFlagCannotLearnWithForcedCheckbox() throws Exception {
        createProfile();
        launch(true, textEditor());
        int[] types = {
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD,
                InputType.TYPE_CLASS_TEXT
        };
        for (int i = 0; i < types.length; i++) {
            EditorInfo info = editor(types[i], EditorInfo.IME_ACTION_DONE
                    | (i == types.length - 1 ? EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING : 0));
            service.onStartInput(info, false);
            awaitProfiles();
            drawing().setInk(ink);
            choose("7");
            assertFalse(learn().isEnabled());
            learn().setChecked(true);
            click(R.id.ime_confirm);
            assertFalse(learn().isChecked());
            assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
        }
        assertEquals("77777", connection.text.toString());
        service.onStartInput(textEditor(), false);
        awaitProfiles();
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
    }

    @Test public void editorChangeDiscardsInkSelectionConsentAndPendingRecognition() throws Exception {
        createProfile();
        store.addExample(profileId, Alphabet.DIGITS, "7", ink);
        store.addExample(profileId, Alphabet.DIGITS, "1",
                new Ink(Collections.singletonList(Arrays.asList(
                        new Ink.Point(500, 100), new Ink.Point(500, 900)))));
        launch(true, textEditor());
        awaitProfiles();
        drawing().setInk(ink);
        choose("7");
        learn().setChecked(true);
        service.onStartInput(editor(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD, EditorInfo.IME_ACTION_DONE), false);
        assertTrue(drawing().getInk().isEmpty());
        assertFalse(learn().isChecked());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        awaitProfiles();
        drawing().setInk(ink);
        click(R.id.ime_recognize);
        service.onStartInput(textEditor(), false);
        // Profile loading is queued behind comparison; its completion also drains stale result posts.
        awaitProfiles();
        assertEquals(0, ((ViewGroup) root.findViewById(R.id.ime_candidates)).getChildCount());
        assertTrue(drawing().getInk().isEmpty());
        assertEquals("", connection.text.toString());
        assertEquals(2, store.examples(profileId, Alphabet.DIGITS).size());
    }

    @Test public void explicitRecognitionRequiresSelectionAndNeverInsertsAutomatically() throws Exception {
        createProfile();
        store.addExample(profileId, Alphabet.DIGITS, "7", ink);
        store.addExample(profileId, Alphabet.DIGITS, "1",
                new Ink(Collections.singletonList(Arrays.asList(
                        new Ink.Point(500, 100), new Ink.Point(500, 900)))));
        launch(true, textEditor());
        awaitProfiles();
        drawing().setInk(ink);
        click(R.id.ime_recognize);
        await(() -> ((ViewGroup) root.findViewById(R.id.ime_candidates)).getChildCount() > 0);
        assertEquals("", connection.text.toString());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        assertTrue(status().contains("similarity"));
        clickKey(R.id.ime_candidates, "7");
        assertEquals("", connection.text.toString());
        click(R.id.ime_confirm);
        assertEquals("7", connection.text.toString());
        assertEquals(2, store.examples(profileId, Alphabet.DIGITS).size());
    }

    @Test public void builtInShapesInsertAfterDefaultPauseWithoutCreatingAProfile() throws Exception {
        launch(true, textEditor());
        awaitReady();
        assertEquals(context.getString(R.string.ime_no_profiles), status());
        assertEquals(context.getString(R.string.starter_examples_profile),
                ((Spinner) root.findViewById(R.id.ime_profile)).getSelectedItem().toString());
        assertEquals(2, ((Spinner) root.findViewById(R.id.ime_auto_delay)).getSelectedItemPosition());
        stroke();
        assertTrue(root.findViewById(R.id.ime_recognize).isEnabled());
        advance(1199);
        drainWorker();
        assertEquals("", connection.text.toString());
        assertEquals(0, connection.commits);
        advance(1);
        await(() -> connection.commits == 1);
        assertTrue(Alphabet.labels(Alphabet.DIGITS).contains(connection.text.toString()));
        assertTrue(drawing().getInk().isEmpty());
        assertTrue(store.profiles().isEmpty());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        advance(5000);
        drainWorker();
        assertEquals(1, connection.commits);
    }

    @Test public void additionalCompletedStrokeRestartsTheWholePause() throws Exception {
        launch(true, textEditor());
        awaitReady();
        stroke();
        advance(1000);
        stroke();
        assertEquals(2, drawing().getInk().strokes.size());
        advance(1199);
        drainWorker();
        assertEquals(0, connection.commits);
        advance(1);
        await(() -> connection.commits == 1);
        advance(3000);
        drainWorker();
        assertEquals(1, connection.commits);
    }

    @Test public void fingerHeldAndCancelledGesturesNeverStartOrFinishTheTimer() throws Exception {
        launch(true, textEditor());
        awaitReady();
        stroke();
        advance(1000);
        touch(MotionEvent.ACTION_DOWN, 0.25f, 0.25f);
        advance(3000);
        touch(MotionEvent.ACTION_MOVE, 0.7f, 0.6f);
        advance(3000);
        drainWorker();
        assertEquals(0, connection.commits);
        touch(MotionEvent.ACTION_CANCEL, 0.7f, 0.6f);
        advance(3000);
        touch(MotionEvent.ACTION_UP, 0.7f, 0.6f);
        advance(3000);
        drainWorker();
        assertEquals(0, connection.commits);
        assertEquals(1, drawing().getInk().strokes.size());
    }

    @Test public void programmaticInkAndStrokeUndoNeverArmAutomaticInsertion() throws Exception {
        launch(true, textEditor());
        awaitReady();
        drawing().setInk(ink);
        advance(2000);
        drainWorker();
        assertEquals(0, connection.commits);
        stroke();
        click(R.id.ime_undo);
        advance(2000);
        drainWorker();
        assertEquals(0, connection.commits);
        assertEquals(1, drawing().getInk().strokes.size());
    }

    @Test public void manualSettingKeepsStrokeRecognitionAndInsertionExplicit() throws Exception {
        AutoInsertSettings.select(context, 0);
        launch(true, textEditor());
        awaitReady();
        stroke();
        advance(5000);
        drainWorker();
        assertEquals(0, connection.commits);
        click(R.id.ime_recognize);
        await(() -> ((ViewGroup) root.findViewById(R.id.ime_candidates)).getChildCount() > 0);
        assertEquals(0, connection.commits);
        Button candidate = (Button) ((ViewGroup) root.findViewById(R.id.ime_candidates)).getChildAt(0);
        String label = (String) candidate.getTag();
        candidate.performClick();
        click(R.id.ime_confirm);
        assertEquals(label, connection.text.toString());
        assertEquals(1, connection.commits);
    }

    @Test public void explicitRecognitionCancelsTheAutomaticTimerForThatStroke() throws Exception {
        launch(true, textEditor());
        awaitReady();
        stroke();
        advance(800);
        click(R.id.ime_recognize);
        await(() -> ((ViewGroup) root.findViewById(R.id.ime_candidates)).getChildCount() > 0);
        advance(3000);
        drainWorker();
        assertEquals(0, connection.commits);
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
    }

    @Test public void delayChangesCancelPendingStrokeAndAreSharedWithTheApp() throws Exception {
        launch(true, textEditor());
        awaitReady();
        stroke();
        advance(1000);
        Spinner delay = root.findViewById(R.id.ime_auto_delay);
        delay.setSelection(3);
        layoutKeyboard();
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(2000, AutoInsertSettings.delay(context));
        advance(3000);
        drainWorker();
        assertEquals(0, connection.commits);
        stroke();
        advance(1999);
        assertEquals(0, connection.commits);
        advance(1);
        await(() -> connection.commits == 1);
        stroke();
        AutoInsertSettings.select(context, 0);
        shadowOf(Looper.getMainLooper()).idle();
        advance(5000);
        drainWorker();
        assertEquals(1, connection.commits);
        service.onStartInputView(textEditor(), false);
        awaitReady();
        assertEquals(0, ((Spinner) root.findViewById(R.id.ime_auto_delay)).getSelectedItemPosition());
    }

    @Test public void navigationAndManualActionsCancelPendingAutomaticInsertion() throws Exception {
        createProfile();
        store.createProfile("Second learner");
        launch(true, textEditor());
        awaitProfiles();
        for (int action : new int[]{R.id.ime_clear, R.id.ime_manual, R.id.ime_mode,
                R.id.ime_space, R.id.ime_backspace, R.id.ime_enter, R.id.ime_training, R.id.ime_picker}) {
            if (drawing() == null) click(R.id.ime_mode);
            stroke();
            advance(800);
            click(action);
            int attempts = connection.commits;
            advance(3000);
            drainWorker();
            assertEquals("Unexpected automatic commit after action " + action, attempts, connection.commits);
        }
        service.onStartInput(textEditor(), false);
        awaitProfiles();
        stroke();
        ((Spinner) root.findViewById(R.id.ime_alphabet)).setSelection(3);
        layoutKeyboard();
        shadowOf(Looper.getMainLooper()).idle();
        int attempts = connection.commits;
        advance(3000);
        drainWorker();
        assertEquals(attempts, connection.commits);
        stroke();
        ((Spinner) root.findViewById(R.id.ime_profile)).setSelection(1);
        layoutKeyboard();
        shadowOf(Looper.getMainLooper()).idle();
        advance(3000);
        drainWorker();
        assertEquals(attempts, connection.commits);
    }

    @Test public void editorAndInputViewLifecyclesCancelPendingInsertion() throws Exception {
        launch(true, textEditor());
        awaitReady();
        stroke();
        service.onFinishInputView(false);
        advance(2000);
        drainWorker();
        assertEquals(0, connection.commits);
        service.onStartInputView(textEditor(), false);
        awaitReady();
        stroke();
        service.onFinishInput();
        advance(2000);
        drainWorker();
        assertEquals(0, connection.commits);
        service.onStartInput(textEditor(), false);
        awaitReady();
        stroke();
        service.connection = new RecordingConnection(context);
        service.onStartInput(textEditor(), false);
        awaitReady();
        advance(2000);
        drainWorker();
        assertEquals(0, connection.commits);
        assertEquals(0, ((RecordingConnection) service.connection).commits);
    }

    @Test public void queuedRecognitionCannotCommitAfterNewFingerDown() throws Exception {
        launch(true, textEditor());
        awaitReady();
        CountDownLatch release = blockWorker();
        try {
            stroke();
            advance(1200);
            assertEquals(context.getString(R.string.ime_recognizing), status());
            touch(MotionEvent.ACTION_DOWN, 0.4f, 0.2f);
            touch(MotionEvent.ACTION_MOVE, 0.4f, 0.5f);
        } finally {
            release.countDown();
        }
        drainWorker();
        advance(3000);
        drainWorker();
        assertEquals(0, connection.commits);
        assertEquals(0, ((ViewGroup) root.findViewById(R.id.ime_candidates)).getChildCount());
    }

    @Test public void completedRecognitionCannotCommitAfterANewFingerDown() throws Exception {
        launch(true, textEditor());
        awaitReady();
        CountDownLatch release = blockWorker();
        try {
            stroke();
            advance(1200);
        } finally {
            release.countDown();
        }
        worker().submit(() -> {}).get(10, TimeUnit.SECONDS);
        touch(MotionEvent.ACTION_DOWN, 0.4f, 0.2f);
        shadowOf(Looper.getMainLooper()).idle();
        advance(3000);
        drainWorker();
        assertEquals(0, connection.commits);
        touch(MotionEvent.ACTION_UP, 0.4f, 0.8f);
        advance(1200);
        await(() -> connection.commits == 1);
    }

    @Test public void destroyingServiceCancelsTheTimerAndClearsAutomaticUndo() throws Exception {
        launch(true, textEditor());
        awaitReady();
        stroke();
        advance(1200);
        await(() -> connection.commits == 1);
        View undo = root.findViewById(R.id.ime_auto_undo);
        assertEquals(View.VISIBLE, undo.getVisibility());
        stroke();
        ExecutorService executor = worker();
        controller.destroy();
        controller = null;
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        advance(5000);
        assertEquals(1, connection.commits);
        assertEquals(View.GONE, undo.getVisibility());
    }

    @Test public void completedWorkerResultCannotCommitAfterAnEditorOrSettingChange() throws Exception {
        launch(true, textEditor());
        awaitReady();
        CountDownLatch release = blockWorker();
        try {
            stroke();
            advance(1200);
        } finally {
            release.countDown();
        }
        // Drain comparisons without executing the result posted to the main looper.
        worker().submit(() -> {}).get(10, TimeUnit.SECONDS);
        AutoInsertSettings.select(context, 0);
        AutoInsertSettings.select(context, 2);
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(0, connection.commits);
        release = blockWorker();
        try {
            stroke();
            advance(1200);
        } finally {
            release.countDown();
        }
        worker().submit(() -> {}).get(10, TimeUnit.SECONDS);
        service.onStartInput(textEditor(), false);
        awaitReady();
        assertEquals(0, connection.commits);
    }

    @Test public void autoInsertionUsesPersonalSamplesButNeverLearnsWithForcedConsent() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        stroke();
        Ink personalInk = drawing().getInk();
        store.addExample(profileId, Alphabet.DIGITS, "7", personalInk);
        learn().setChecked(true);
        advance(1200);
        await(() -> connection.commits == 1);
        assertEquals("7", connection.text.toString());
        assertEquals(1, store.examples(profileId, Alphabet.DIGITS).size());
        assertFalse(learn().isChecked());
    }

    @Test public void automaticPrivateFieldsNeverLearnOrRetainUndoAndNeverEchoTheirCharacter() throws Exception {
        createProfile();
        launch(true, textEditor());
        for (int type : new int[]{InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD,
                InputType.TYPE_CLASS_TEXT}) {
            service.onStartInput(editor(type, EditorInfo.IME_ACTION_DONE
                    | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING), false);
            awaitProfiles();
            stroke();
            learn().setChecked(true);
            int attempts = connection.commits;
            advance(1200);
            await(() -> connection.commits == attempts + 1);
            assertEquals(View.GONE, root.findViewById(R.id.ime_auto_undo).getVisibility());
            assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
            assertFalse(learn().isChecked());
            assertFalse(status().matches(".*[0-9].*"));
            assertTrue(status().contains("private"));
        }
    }

    @Test public void rejectedAutomaticCommitLeavesInkAndNeverRetriesOrLearns() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        stroke();
        learn().setChecked(true);
        connection.accept = false;
        advance(1200);
        await(() -> status().equals(context.getString(R.string.ime_input_failed)));
        assertEquals("", connection.text.toString());
        assertFalse(drawing().getInk().isEmpty());
        assertEquals(View.GONE, root.findViewById(R.id.ime_auto_undo).getVisibility());
        connection.accept = true;
        advance(5000);
        drainWorker();
        assertEquals(1, connection.commits);
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
        choose("7");
        click(R.id.ime_confirm);
        assertEquals("7", connection.text.toString());
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
    }

    @Test public void deletedSelectedProfileReportsAnErrorInsteadOfSilentlyUsingDefaults() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        store.deleteProfile(profileId);
        stroke();
        advance(1200);
        await(() -> status().equals(context.getString(R.string.ime_recognition_error)));
        assertEquals(0, connection.commits);
        assertFalse(drawing().getInk().isEmpty());
    }

    @Test public void automaticUndoRemovesOnlyLastCharacterAndRestoresInkWithoutRearming() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        connection.text.append("prefix");
        Selection.setSelection(connection.text, 6);
        service.onUpdateSelection(0, 0, 6, 6, -1, -1);
        stroke();
        byte[] originalInk = drawing().getInk().encode();
        advance(1200);
        await(() -> connection.commits == 1);
        assertEquals(7, connection.text.length());
        service.onUpdateSelection(6, 6, 7, 7, -1, -1);
        assertEquals(View.VISIBLE, root.findViewById(R.id.ime_auto_undo).getVisibility());
        click(R.id.ime_auto_undo);
        assertEquals("prefix", connection.text.toString());
        assertArrayEquals(originalInk, drawing().getInk().encode());
        assertEquals(context.getString(R.string.auto_insert_undone), status());
        assertEquals(1, connection.deletes);
        advance(4000);
        drainWorker();
        assertEquals(1, connection.commits);
        choose("7");
        learn().setChecked(true);
        click(R.id.ime_confirm);
        await(() -> status().equals(context.getString(R.string.ime_saved)));
        assertEquals("prefix7", connection.text.toString());
        assertEquals(1, store.examples(profileId, Alphabet.DIGITS).size());
    }

    @Test public void typingDrawingAndMovedCaretInvalidateAutomaticUndo() throws Exception {
        launch(true, textEditor());
        awaitReady();
        for (int action = 0; action < 3; action++) {
            service.onStartInput(textEditor(), false);
            connection.text.clear();
            Selection.setSelection(connection.text, 0);
            awaitReady();
            stroke();
            int attempts = connection.commits;
            advance(1200);
            await(() -> connection.commits == attempts + 1);
            View undo = root.findViewById(R.id.ime_auto_undo);
            assertEquals(View.VISIBLE, undo.getVisibility());
            if (action == 0) {
                click(R.id.ime_space);
            } else if (action == 1) {
                touch(MotionEvent.ACTION_DOWN, 0.3f, 0.2f);
            } else {
                Selection.setSelection(connection.text, 0);
                service.onUpdateSelection(1, 1, 0, 0, -1, -1);
                Selection.setSelection(connection.text, 1);
                service.onUpdateSelection(0, 0, 1, 1, -1, -1);
            }
            String expected = connection.text.toString();
            assertEquals(View.GONE, undo.getVisibility());
            undo.performClick();
            assertEquals(expected, connection.text.toString());
            assertEquals(0, connection.deletes);
            assertEquals(context.getString(R.string.ime_auto_undo_unavailable), status());
        }
    }

    @Test public void automaticUndoRefusesAnUnreportedMoveMissingTextOrNewConnection() throws Exception {
        launch(true, textEditor());
        awaitReady();
        for (int action = 0; action < 3; action++) {
            service.connection = connection;
            service.onStartInput(textEditor(), false);
            connection.text.clear();
            connection.allowRead = true;
            Selection.setSelection(connection.text, 0);
            awaitReady();
            stroke();
            int attempts = connection.commits;
            advance(1200);
            await(() -> connection.commits == attempts + 1);
            if (action == 0) {
                Selection.setSelection(connection.text, 0);
            } else if (action == 1) {
                connection.allowRead = false;
            } else {
                service.connection = new RecordingConnection(context);
            }
            String expected = connection.text.toString();
            click(R.id.ime_auto_undo);
            assertEquals(expected, connection.text.toString());
            assertEquals(0, connection.deletes);
            assertEquals(context.getString(R.string.ime_auto_undo_unavailable), status());
        }
    }

    @Test public void automaticHebrewKeepsRawLogicalOrderAndUndoOnlyRemovesTheLastLetter() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        ((Spinner) root.findViewById(R.id.ime_alphabet)).setSelection(3);
        layoutKeyboard();
        shadowOf(Looper.getMainLooper()).idle();
        stroke();
        store.addExample(profileId, Alphabet.HEBREW, "ם", drawing().getInk());
        advance(1200);
        await(() -> connection.commits == 1);
        assertEquals("ם", connection.text.toString());
        stroke();
        advance(1200);
        await(() -> connection.commits == 2);
        assertEquals("םם", connection.text.toString());
        click(R.id.ime_auto_undo);
        assertEquals("ם", connection.text.toString());
        assertEquals(1, store.examples(profileId, Alphabet.HEBREW).size());
    }

    @Test public void uncertainTopPredictionStillInsertsWithoutManualApproval() throws Exception {
        launch(true, textEditor());
        awaitReady();
        touch(MotionEvent.ACTION_DOWN, 0.5f, 0.5f);
        touch(MotionEvent.ACTION_UP, 0.5f, 0.5f);
        HandwritingRecognizer.Result expected = HandwritingRecognizer.recognize(
                drawing().getInk(), Alphabet.DIGITS, Collections.emptyList(), true);
        assertTrue(expected.uncertain);
        assertFalse(expected.candidates.isEmpty());
        advance(1200);
        await(() -> connection.commits == 1);
        assertEquals(expected.candidates.get(0).label, connection.text.toString());
        assertEquals(context.getString(R.string.auto_insert_uncertain), status());
    }

    @Test public void drawingAlphabetProfileAndModeChangesResetLearningAndSelection() throws Exception {
        createProfile();
        long second = store.createProfile("Second learner").id;
        launch(true, textEditor());
        awaitProfiles();
        drawing().setInk(ink);
        choose("7");
        learn().setChecked(true);
        drawing().setInk(ink);
        assertFalse(learn().isChecked());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        choose("7");
        learn().setChecked(true);
        ((Spinner) root.findViewById(R.id.ime_alphabet)).setSelection(1);
        layoutKeyboard();
        shadowOf(Looper.getMainLooper()).idle();
        assertFalse(learn().isChecked());
        assertTrue(drawing().getInk().isEmpty());
        drawing().setInk(ink);
        choose("A");
        learn().setChecked(true);
        ((Spinner) root.findViewById(R.id.ime_profile)).setSelection(1);
        layoutKeyboard();
        shadowOf(Looper.getMainLooper()).idle();
        assertFalse(learn().isChecked());
        assertEquals(second, context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE)
                .getLong("profile", -1));
        assertTrue(drawing().getInk().isEmpty());
        drawing().setInk(ink);
        choose("A");
        learn().setChecked(true);
        click(R.id.ime_mode);
        click(R.id.ime_mode);
        assertFalse(learn().isChecked());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        assertTrue(drawing().getInk().isEmpty());
    }

    @Test public void finishingInputOrInputViewDropsDraftsAndBlocksDetachedEditorActions() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        drawing().setInk(ink);
        choose("7");
        learn().setChecked(true);
        service.onFinishInputView(false);
        assertTrue(drawing().getInk().isEmpty());
        assertFalse(learn().isChecked());
        click(R.id.ime_space);
        assertEquals("", connection.text.toString());
        service.onStartInputView(textEditor(), false);
        awaitProfiles();
        drawing().setInk(ink);
        choose("7");
        service.onFinishInput();
        assertTrue(drawing().getInk().isEmpty());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        click(R.id.ime_enter);
        assertEquals("", connection.text.toString());
    }

    @Test public void languagePreferenceLocalizesUiWithoutChangingTypingGroup() {
        context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE).edit()
                .putString("language", "he").commit();
        launch(false, textEditor());
        assertEquals("כתב יד", ((Button) root.findViewById(R.id.ime_mode)).getText().toString());
        assertEquals(View.LAYOUT_DIRECTION_RTL, root.getLayoutDirection());
        clickKey(R.id.ime_key_rows, "q");
        assertEquals("q", connection.text.toString());
        ViewGroup rows = root.findViewById(R.id.ime_key_rows);
        assertEquals(View.LAYOUT_DIRECTION_LTR, rows.getChildAt(0).getLayoutDirection());
        context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE).edit()
                .putString("language", "en").commit();
        service.onStartInput(textEditor(), false);
        assertEquals("Handwrite", ((Button) root.findViewById(R.id.ime_mode)).getText().toString());
    }

    @Test public void navigationInsetsDoNotBecomeAnExtraEditorSizedPadding() {
        launch(false, textEditor());
        root.dispatchApplyWindowInsets(new WindowInsets.Builder()
                .setInsets(WindowInsets.Type.systemBars(), Insets.of(0, 24, 0, 30))
                .setInsets(WindowInsets.Type.displayCutout(), Insets.of(20, 0, 0, 0))
                .setInsets(WindowInsets.Type.ime(), Insets.of(0, 0, 0, 500)).build());
        assertEquals(20, root.getPaddingLeft());
        assertEquals(30, root.getPaddingBottom());
        root.measure(View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.AT_MOST));
        assertTrue(root.getMeasuredHeight() < 450);
        assertTrue(((ViewGroup) root).getChildAt(0).getMeasuredWidth() <= 380);
        assertFalse(service.onEvaluateFullscreenMode());
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-mdpi")
    public void handwritingActionsStayVisibleWithoutScrollingAwayFromCanvas() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        root.dispatchApplyWindowInsets(new WindowInsets.Builder()
                .setInsets(WindowInsets.Type.systemBars(), Insets.of(0, 24, 0, 24)).build());
        root.measure(View.MeasureSpec.makeMeasureSpec(360, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(640, View.MeasureSpec.AT_MOST));
        root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        assertTrue("Canvas=" + drawing().getHeight() + ", keyboard=" + root.getHeight()
                + ", screen=" + context.getResources().getDisplayMetrics().heightPixels,
                drawing().getHeight() >= 80);
        for (int id : new int[]{R.id.ime_recognize, R.id.ime_manual, R.id.ime_confirm, R.id.ime_space}) {
            View control = root.findViewById(id);
            Rect visible = new Rect();
            assertTrue(control.getGlobalVisibleRect(visible));
            assertEquals(control.getHeight(), visible.height());
        }
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp-mdpi")
    public void normalKeysAndEditorActionsFitInLandscape() {
        launch(false, textEditor());
        root.dispatchApplyWindowInsets(new WindowInsets.Builder()
                .setInsets(WindowInsets.Type.systemBars(), Insets.of(0, 24, 0, 24)).build());
        root.measure(View.MeasureSpec.makeMeasureSpec(640, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(360, View.MeasureSpec.AT_MOST));
        root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        assertTrue(root.getHeight() <= 280);
        for (View control : new View[]{findKey(root, "m"), root.findViewById(R.id.ime_enter),
                root.findViewById(R.id.ime_backspace)}) {
            assertNotNull(control);
            Rect visible = new Rect();
            assertTrue(control.getGlobalVisibleRect(visible));
            assertEquals(control.getHeight(), visible.height());
        }
    }

    @Test public void trainingIsOnlyOpenedByAnExplicitButton() {
        launch(false, textEditor());
        assertNull(shadowOf(service).getNextStartedActivity());
        click(R.id.ime_training);
        Intent intent = shadowOf(service).getNextStartedActivity();
        assertNotNull(intent);
        assertEquals(MainActivity.class.getName(), intent.getComponent().getClassName());
        assertNotEquals(0, intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private void awaitProfiles() throws Exception {
        await(() -> root.findViewById(R.id.ime_profile).isEnabled());
    }

    private void awaitReady() throws Exception {
        await(() -> drawing() != null && drawing().isEnabled()
                && !status().equals(context.getString(R.string.ime_loading_profiles)));
    }

    private void advance(long millis) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis));
    }

    private ExecutorService worker() throws Exception {
        Field field = HandwritingImeService.class.getDeclaredField("worker");
        field.setAccessible(true);
        return (ExecutorService) field.get(service);
    }

    private void drainWorker() throws Exception {
        worker().submit(() -> {}).get(10, TimeUnit.SECONDS);
        shadowOf(Looper.getMainLooper()).idle();
    }

    private CountDownLatch blockWorker() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        worker().execute(() -> {
            started.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(started.await(10, TimeUnit.SECONDS));
        return release;
    }

    private void stroke() {
        touch(MotionEvent.ACTION_DOWN, 0.5f, 0.15f);
        touch(MotionEvent.ACTION_MOVE, 0.5f, 0.5f);
        touch(MotionEvent.ACTION_UP, 0.5f, 0.85f);
    }

    private void touch(int action, float x, float y) {
        DrawingView pad = drawing();
        long time = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(time, time, action,
                x * pad.getWidth(), y * pad.getHeight(), 0);
        try {
            assertTrue(pad.onTouchEvent(event));
        } finally {
            event.recycle();
        }
    }

    @Test public void trainButtonCarriesTypingLanguageInsteadOfAlwaysOpeningDigits() {
        launch(false, textEditor());
        click(R.id.ime_training);
        assertEquals(Alphabet.ENGLISH_LOWER, shadowOf(service).getNextStartedActivity()
                .getStringExtra(MainActivity.EXTRA_TRAINING_ALPHABET));
        click(R.id.ime_shift);
        click(R.id.ime_training);
        assertEquals(Alphabet.ENGLISH_UPPER, shadowOf(service).getNextStartedActivity()
                .getStringExtra(MainActivity.EXTRA_TRAINING_ALPHABET));
        click(R.id.ime_typing_language);
        click(R.id.ime_training);
        assertEquals(Alphabet.HEBREW, shadowOf(service).getNextStartedActivity()
                .getStringExtra(MainActivity.EXTRA_TRAINING_ALPHABET));
        service.onStartInput(editor(InputType.TYPE_CLASS_NUMBER, EditorInfo.IME_ACTION_DONE), false);
        click(R.id.ime_training);
        assertEquals(Alphabet.DIGITS, shadowOf(service).getNextStartedActivity()
                .getStringExtra(MainActivity.EXTRA_TRAINING_ALPHABET));
    }

    @Test public void trainButtonCarriesSelectedHandwritingAlphabet() throws Exception {
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        ((Spinner) root.findViewById(R.id.ime_alphabet)).setSelection(3);
        layoutKeyboard();
        shadowOf(Looper.getMainLooper()).idle();
        click(R.id.ime_training);
        assertEquals(Alphabet.HEBREW, shadowOf(service).getNextStartedActivity()
                .getStringExtra(MainActivity.EXTRA_TRAINING_ALPHABET));
    }

    @Test public void wholeNumberAutomaticallyCommitsOnceOnlyAfterTheTwoSecondMinimum() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        assertEquals(2, ((Spinner) root.findViewById(R.id.ime_auto_delay)).getSelectedItemPosition());
        assertTrue(((Spinner) root.findViewById(R.id.ime_auto_delay)).getSelectedItem().toString().contains("2s"));
        drawLine(Alphabet.DIGITS, "23");
        advance(1999);
        drainWorker();
        assertEquals(0, connection.commits);
        advance(1);
        await(() -> connection.commits == 1);
        assertEquals("23", connection.text.toString());
        assertEquals(-1, connection.action);
        assertTrue(drawing().getInk().isEmpty());
        assertTrue(store.profiles().isEmpty());
        advance(5000);
        drainWorker();
        assertEquals(1, connection.commits);
    }

    @Test public void anotherCharacterRestartsTheWholeWordPause() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        drawGlyph(Alphabet.DIGITS, "2", 0, 2);
        advance(1700);
        drawGlyph(Alphabet.DIGITS, "3", 1, 2);
        advance(1999);
        drainWorker();
        assertEquals(0, connection.commits);
        advance(1);
        await(() -> connection.commits == 1);
        assertEquals("23", connection.text.toString());
    }

    @Test public void hebrewWholeWordsAndDigitRunsAreInsertedInRecognizerLogicalOrder() throws Exception {
        WritingSettings.setWordMode(context, true);
        context.getSharedPreferences("handwriting-ime", Context.MODE_PRIVATE).edit()
                .putString("alphabet", Alphabet.HEBREW).commit();
        launch(true, textEditor());
        awaitReady();
        drawLine(Alphabet.HEBREW, "םולש");
        advance(2000);
        await(() -> connection.commits == 1);
        assertEquals("שלום", connection.text.toString());
        drawLine(Alphabet.HEBREW, "ם12ש");
        advance(2000);
        await(() -> connection.commits == 2);
        assertEquals("שלוםש12ם", connection.text.toString());
        click(R.id.ime_auto_undo);
        assertEquals("שלום", connection.text.toString());
    }

    @Test public void reverseReadingOrderChangesTheWordButNotMirrorShapeRecognition() throws Exception {
        WritingSettings.setWordMode(context, true);
        WritingSettings.setReverseOrder(context, true);
        launch(true, textEditor());
        awaitReady();
        drawLine(Alphabet.DIGITS, "23");
        advance(2000);
        await(() -> connection.commits == 1);
        assertEquals("32", connection.text.toString());
        assertTrue(WritingSettings.mirrored(context));
    }

    @Test public void manualWholeWordReviewChangesOnlySelectedCharacterAndInsertsOnce() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        drawLine(Alphabet.DIGITS, "23");
        advance(1500);
        click(R.id.ime_recognize);
        awaitWordReview();
        assertTrue(selected().contains("23"));
        assertEquals(0, connection.commits);
        Spinner positions = root.findViewById(R.id.ime_word_character);
        assertEquals(2, positions.getCount());
        positions.setSelection(1);
        layoutKeyboard();
        shadowOf(Looper.getMainLooper()).idle();
        choose("7");
        assertTrue(selected().contains("27"));
        assertFalse(learn().isEnabled());
        learn().setChecked(true);
        advance(5000);
        drainWorker();
        assertEquals(0, connection.commits);
        click(R.id.ime_confirm);
        click(R.id.ime_confirm);
        assertEquals("27", connection.text.toString());
        assertEquals(1, connection.commits);
        assertTrue(store.profiles().isEmpty());
        assertFalse(learn().isChecked());
    }

    @Test public void wordCandidateButtonsChangeTheLocalWordWithoutCommittingOrRearming() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        drawLine(Alphabet.DIGITS, "23");
        click(R.id.ime_recognize);
        awaitWordReview();
        ViewGroup choices = root.findViewById(R.id.ime_candidates);
        Button alternative = (Button) choices.getChildAt(choices.getChildCount() - 1);
        String corrected = alternative.getTag() + "3";
        alternative.performClick();
        assertTrue(selected().contains(corrected));
        advance(5000);
        drainWorker();
        assertEquals(0, connection.commits);
        click(R.id.ime_confirm);
        assertEquals(corrected, connection.text.toString());
        assertEquals(1, connection.commits);
    }

    @Test public void wordRejectShowsSeparationHelpAndNeverCommitsAnyPart() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        drawLine(Alphabet.DIGITS, "22222222222222222");
        advance(2000);
        await(() -> status().equals(context.getString(R.string.word_separation_needed)));
        assertFalse(drawing().getInk().isEmpty());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        advance(5000);
        drainWorker();
        assertEquals(0, connection.commits);
        click(R.id.ime_recognize);
        await(() -> status().equals(context.getString(R.string.word_separation_needed)));
        assertEquals(0, connection.commits);
    }

    @Test public void wordCorrectionsNeverTrainEvenWithForcedConsentInAnyEditor() throws Exception {
        WritingSettings.setWordMode(context, true);
        createProfile();
        launch(true, textEditor());
        for (int type : new int[]{InputType.TYPE_CLASS_TEXT,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD}) {
            service.onStartInput(editor(type, EditorInfo.IME_ACTION_DONE), false);
            awaitProfiles();
            drawLine(Alphabet.DIGITS, "23");
            click(R.id.ime_recognize);
            awaitWordReview();
            assertFalse(learn().isEnabled());
            assertEquals(context.getString(R.string.word_no_learning), learn().getText().toString());
            learn().setChecked(true);
            click(R.id.ime_confirm);
            assertFalse(learn().isChecked());
            assertFalse(selected().contains("23"));
            assertFalse(status().contains("23"));
        }
        drainWorker();
        assertEquals("232323", connection.text.toString());
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
    }

    @Test public void automaticPrivateWholeNumberDoesNotLearnEchoOrRememberUndo() throws Exception {
        WritingSettings.setWordMode(context, true);
        createProfile();
        launch(true, editor(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD,
                EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING));
        awaitProfiles();
        drawLine(Alphabet.DIGITS, "23");
        learn().setChecked(true);
        advance(2000);
        await(() -> connection.commits == 1);
        assertEquals("23", connection.text.toString());
        assertEquals(View.GONE, root.findViewById(R.id.ime_auto_undo).getVisibility());
        assertFalse(learn().isChecked());
        assertFalse(status().contains("23"));
        assertFalse(selected().contains("23"));
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
    }

    @Test public void wordsLoadPersonalExamplesForTheSelectedAlphabetAndDigitsOnly() throws Exception {
        WritingSettings.setWordMode(context, true);
        createProfile();
        store.addExample(profileId, Alphabet.ENGLISH_UPPER, "Z", sample(Alphabet.ENGLISH_UPPER, "A"));
        store.addExample(profileId, Alphabet.DIGITS, "7", sample(Alphabet.DIGITS, "2"));
        context.getSharedPreferences("handwriting-ime", Context.MODE_PRIVATE).edit()
                .putString("alphabet", Alphabet.ENGLISH_UPPER).commit();
        launch(true, textEditor());
        awaitProfiles();
        drawLine(Alphabet.ENGLISH_UPPER, "A2");
        advance(2000);
        await(() -> connection.commits == 1);
        assertEquals("Z7", connection.text.toString());
        assertEquals(1, store.examples(profileId, Alphabet.ENGLISH_UPPER).size());
        assertEquals(1, store.examples(profileId, Alphabet.DIGITS).size());
        assertTrue(store.examples(profileId, Alphabet.ENGLISH_LOWER).isEmpty());
    }

    @Test public void rejectedWholeWordCommitRetainsInkAndRequiresAnotherExplicitAttempt() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        drawLine(Alphabet.DIGITS, "23");
        byte[] original = drawing().getInk().encode();
        connection.accept = false;
        advance(2000);
        await(() -> status().equals(context.getString(R.string.ime_input_failed)));
        assertArrayEquals(original, drawing().getInk().encode());
        assertEquals("", connection.text.toString());
        assertEquals(View.GONE, root.findViewById(R.id.ime_auto_undo).getVisibility());
        connection.accept = true;
        advance(5000);
        drainWorker();
        assertEquals(1, connection.commits);
        click(R.id.ime_recognize);
        awaitWordReview();
        click(R.id.ime_confirm);
        assertEquals("23", connection.text.toString());
        assertEquals(2, connection.commits);
    }

    @Test public void writingOptionChangesInvalidateExistingWholeWordUndo() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        drawLine(Alphabet.DIGITS, "23");
        advance(2000);
        await(() -> connection.commits == 1);
        assertEquals(View.VISIBLE, root.findViewById(R.id.ime_auto_undo).getVisibility());
        WritingSettings.setReverseOrder(context, true);
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(View.GONE, root.findViewById(R.id.ime_auto_undo).getVisibility());
        click(R.id.ime_auto_undo);
        assertEquals("23", connection.text.toString());
        assertEquals(0, connection.deletes);
    }

    @Test public void wholeWordUndoRestoresTheCompleteLineWithoutRearming() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        connection.text.append("prefix");
        Selection.setSelection(connection.text, 6);
        service.onUpdateSelection(0, 0, 6, 6, -1, -1);
        drawLine(Alphabet.DIGITS, "23");
        byte[] original = drawing().getInk().encode();
        advance(2000);
        await(() -> connection.commits == 1);
        assertEquals("prefix23", connection.text.toString());
        service.onUpdateSelection(6, 6, 8, 8, -1, -1);
        click(R.id.ime_auto_undo);
        assertEquals("prefix", connection.text.toString());
        assertArrayEquals(original, drawing().getInk().encode());
        assertEquals(1, connection.deletes);
        advance(5000);
        drainWorker();
        assertEquals(1, connection.commits);
        click(R.id.ime_recognize);
        awaitWordReview();
        assertFalse(learn().isEnabled());
    }

    @Test public void wordUndoRejectsChangedSuffixAndUnreportedCaretWithoutDeleting() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        for (boolean moved : new boolean[]{false, true}) {
            connection.text.clear();
            Selection.setSelection(connection.text, 0);
            service.onStartInput(textEditor(), false);
            awaitReady();
            drawLine(Alphabet.DIGITS, "23");
            int attempts = connection.commits;
            advance(2000);
            await(() -> connection.commits == attempts + 1);
            if (moved) Selection.setSelection(connection.text, 1);
            else connection.text.replace(0, 2, "24");
            click(R.id.ime_auto_undo);
            assertEquals(moved ? "23" : "24", connection.text.toString());
            assertEquals(0, connection.deletes);
            assertEquals(context.getString(R.string.ime_auto_undo_unavailable), status());
        }
    }

    @Test public void wordSettingsProfileAndEditorChangesDiscardQueuedRecognition() throws Exception {
        WritingSettings.setWordMode(context, true);
        createProfile();
        store.createProfile("Second learner");
        launch(true, textEditor());
        awaitProfiles();
        for (int action = 0; action < 6; action++) {
            WritingSettings.setWordMode(context, true);
            shadowOf(Looper.getMainLooper()).idle();
            awaitReady();
            CountDownLatch release = blockWorker();
            try {
                drawLine(Alphabet.DIGITS, "23");
                advance(2000);
                assertEquals("Action " + action, context.getString(R.string.ime_recognizing), status());
                switch (action) {
                    case 0: WritingSettings.setWordMode(context, false); break;
                    case 1: WritingSettings.setMirrored(context, !WritingSettings.mirrored(context)); break;
                    case 2: WritingSettings.setReverseOrder(context, !WritingSettings.reverseOrder(context)); break;
                    case 3:
                        ((Spinner) root.findViewById(R.id.ime_profile)).setSelection(1);
                        layoutKeyboard();
                        shadowOf(Looper.getMainLooper()).idle();
                        break;
                    case 4: service.onStartInput(textEditor(), false); break;
                    default: click(R.id.ime_mode); break;
                }
            } finally {
                release.countDown();
            }
            drainWorker();
            advance(4000);
            drainWorker();
            assertEquals(0, connection.commits);
        }
    }

    @Test public void completedWordResultCannotOutliveSharedMirrorSettingChanges() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        CountDownLatch release = blockWorker();
        try {
            drawLine(Alphabet.DIGITS, "23");
            advance(2000);
        } finally {
            release.countDown();
        }
        worker().submit(() -> {}).get(10, TimeUnit.SECONDS);
        WritingSettings.setMirrored(context, false);
        WritingSettings.setMirrored(context, true);
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(0, connection.commits);
        assertTrue(drawing().getInk().isEmpty());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
    }

    @Test public void selectedMissingProfileIsAnErrorForWholeWordsRatherThanDefaultFallback() throws Exception {
        WritingSettings.setWordMode(context, true);
        createProfile();
        launch(true, textEditor());
        awaitProfiles();
        store.deleteProfile(profileId);
        drawLine(Alphabet.DIGITS, "23");
        advance(2000);
        await(() -> status().equals(context.getString(R.string.ime_recognition_error)));
        assertEquals(0, connection.commits);
    }

    @Test public void mirrorPreferenceRoutesSingleCharacterRecognitionThroughTheFourArgumentContract() throws Exception {
        assertTrue(WritingSettings.mirrored(context));
        assertFalse(WritingSettings.wordMode(context));
        launch(true, textEditor());
        awaitReady();
        Ink reflected = DefaultSamples.mirror(sample(Alphabet.DIGITS, "2"));
        for (boolean mirrored : new boolean[]{false, true}) {
            WritingSettings.setMirrored(context, mirrored);
            shadowOf(Looper.getMainLooper()).idle();
            drawing().setInk(reflected);
            click(R.id.ime_recognize);
            await(() -> ((ViewGroup) root.findViewById(R.id.ime_candidates)).getChildCount() > 0);
            HandwritingRecognizer.Result expected = HandwritingRecognizer.recognize(
                    reflected, Alphabet.DIGITS, Collections.emptyList(), mirrored);
            ViewGroup choices = root.findViewById(R.id.ime_candidates);
            assertEquals(expected.candidates.size(), choices.getChildCount());
            for (int i = 0; i < expected.candidates.size(); i++) {
                HandwritingRecognizer.Candidate candidate = expected.candidates.get(i);
                assertEquals(candidate.label + " · " + candidate.similarity,
                        ((Button) choices.getChildAt(i)).getText().toString());
            }
            if (mirrored) assertEquals("2", expected.candidates.get(0).label);
        }
        assertEquals(0, connection.commits);
    }

    @Test public void wordNumericEditorOverridesSavedAlphabetAndWordManualModeStaysOff() throws Exception {
        WritingSettings.setWordMode(context, true);
        AutoInsertSettings.select(context, 0);
        context.getSharedPreferences("handwriting-ime", Context.MODE_PRIVATE).edit()
                .putString("alphabet", Alphabet.HEBREW).commit();
        launch(true, editor(InputType.TYPE_CLASS_NUMBER, EditorInfo.IME_ACTION_DONE));
        awaitReady();
        Spinner alphabet = root.findViewById(R.id.ime_alphabet);
        assertEquals(0, alphabet.getSelectedItemPosition());
        assertFalse(alphabet.isEnabled());
        drawLine(Alphabet.DIGITS, "23");
        advance(5000);
        drainWorker();
        assertEquals(0, connection.commits);
        click(R.id.ime_recognize);
        awaitWordReview();
        click(R.id.ime_confirm);
        assertEquals("23", connection.text.toString());
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-mdpi")
    public void wordOptionsAndReviewPreserveUsablePadAndVisibleActionsWithoutExtraRows() throws Exception {
        WritingSettings.setWordMode(context, true);
        launch(true, textEditor());
        awaitReady();
        root.dispatchApplyWindowInsets(new WindowInsets.Builder()
                .setInsets(WindowInsets.Type.systemBars(), Insets.of(0, 24, 0, 24)).build());
        layoutWordKeyboard();
        int height = drawing().getHeight();
        assertTrue("Word canvas height=" + height, height >= 80);
        for (int id : new int[]{R.id.ime_writing_options, R.id.ime_recognize, R.id.ime_manual,
                R.id.ime_confirm, R.id.ime_space, R.id.ime_auto_delay}) {
            assertFullyVisible(id);
        }
        drawLine(Alphabet.DIGITS, "23");
        click(R.id.ime_recognize);
        awaitWordReview();
        layoutWordKeyboard();
        assertEquals(height, drawing().getHeight());
        assertFullyVisible(R.id.ime_word_character);
        assertFullyVisible(R.id.ime_confirm);
        WritingSettings.setMirrored(context, false);
        shadowOf(Looper.getMainLooper()).idle();
        layoutWordKeyboard();
        assertEquals(height, drawing().getHeight());
        assertTrue(drawing().getInk().isEmpty());
        assertFalse(root.findViewById(R.id.ime_confirm).isEnabled());
        assertEquals(View.GONE, root.findViewById(R.id.ime_word_character).getVisibility());
        assertFullyVisible(R.id.ime_auto_delay);
        advance(5000);
        drainWorker();
        assertEquals(0, connection.commits);
    }

    private void layoutWordKeyboard() {
        root.measure(View.MeasureSpec.makeMeasureSpec(360, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(640, View.MeasureSpec.AT_MOST));
        root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
    }

    private void assertFullyVisible(int id) {
        View control = root.findViewById(id);
        Rect visible = new Rect();
        assertTrue("Hidden control " + id, control.getGlobalVisibleRect(visible));
        assertEquals(control.getHeight(), visible.height());
    }

    private void awaitWordReview() throws Exception {
        await(() -> root.findViewById(R.id.ime_word_character).getVisibility() == View.VISIBLE
                && root.findViewById(R.id.ime_confirm).isEnabled());
    }

    private Ink sample(String group, String label) {
        for (HandwritingRecognizer.Example example : DefaultSamples.examples(group)) {
            if (label.equals(example.label)) return example.ink;
        }
        throw new AssertionError("Missing starter " + label);
    }

    private void drawLine(String group, String visualLeftToRight) {
        for (int i = 0; i < visualLeftToRight.length(); i++) {
            String label = visualLeftToRight.substring(i, i + 1);
            drawGlyph(Character.isDigit(label.charAt(0)) ? Alphabet.DIGITS : group,
                    label, i, visualLeftToRight.length());
        }
    }

    private void drawGlyph(String group, String label, int index, int slots) {
        float scale = Math.min((drawing().getWidth() - 20f) / (slots * 1.2f), drawing().getHeight() * 0.7f);
        for (List<Ink.Point> stroke : sample(group, label).strokes) {
            for (int i = 0; i < stroke.size(); i++) {
                Ink.Point point = stroke.get(i);
                float x = (10 + (index * 1.2f + point.x) * scale) / drawing().getWidth();
                float y = ((drawing().getHeight() - scale) / 2 + point.y * scale) / drawing().getHeight();
                touch(i == 0 ? MotionEvent.ACTION_DOWN
                        : i == stroke.size() - 1 ? MotionEvent.ACTION_UP : MotionEvent.ACTION_MOVE, x, y);
                if (stroke.size() == 1) touch(MotionEvent.ACTION_UP, x, y);
            }
        }
    }

    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (System.nanoTime() < deadline) {
            layoutKeyboard();
            shadowOf(Looper.getMainLooper()).idle();
            if (condition.getAsBoolean()) return;
            Thread.sleep(10);
        }
        fail("Timed out waiting for keyboard work");
    }

    private void click(int id) {
        View control = root.findViewById(id);
        assertNotNull(control);
        control.performClick();
    }

    private void layoutKeyboard() {
        root.measure(View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.AT_MOST));
        root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
    }

    private void clickKey(int panelId, String label) {
        View key = findKey(root.findViewById(panelId), label);
        assertNotNull("Missing key: " + label, key);
        key.performClick();
    }

    private static View findKey(View view, String label) {
        if (label.equals(view.getTag())) return view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View key = findKey(group.getChildAt(i), label);
                if (key != null) return key;
            }
        }
        return null;
    }

    private void choose(String label) {
        click(R.id.ime_manual);
        clickKey(R.id.ime_manual_panel, label);
    }

    private DrawingView drawing() {
        return root.findViewById(R.id.ime_drawing);
    }

    private CheckBox learn() {
        return root.findViewById(R.id.ime_learn);
    }

    private String selected() {
        return ((TextView) root.findViewById(R.id.ime_selected)).getText().toString();
    }

    private String status() {
        return ((TextView) root.findViewById(R.id.ime_status)).getText().toString();
    }
}
