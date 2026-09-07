package com.jellybolt.handwriting;

import android.content.Context;
import android.content.Intent;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.View;
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

        RecordingConnection(Context context) {
            super(new View(context), true);
            Selection.setSelection(text, 0);
        }

        @Override public Editable getEditable() {
            return text;
        }

        @Override public boolean commitText(CharSequence value, int cursor) {
            return accept && super.commitText(value, cursor);
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

    @Test public void recognitionRequiresSelectionAndNeverInsertsAutomatically() throws Exception {
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
