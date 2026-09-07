package com.jellybolt.handwriting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.graphics.Insets;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.GridLayout;

import com.jellybolt.handwriting.core.Alphabet;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@LooperMode(LooperMode.Mode.PAUSED)
public class MainActivityTest {
    private Context context;
    private ProfileStore store;
    private long profileId;
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private final Ink ink = new Ink(Collections.singletonList(Arrays.asList(
            new Ink.Point(100, 100), new Ink.Point(700, 200), new Ink.Point(300, 900))));

    @Before public void setUp() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE).edit().clear().commit();
        store = new ProfileStore(context);
        profileId = store.createProfile("First child").id;
        context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE).edit()
                .putLong("profile", profileId).putString("language", "en").commit();
    }

    @After public void tearDown() {
        if (controller != null) controller.pause().stop().destroy();
        store.close();
    }

    private void launch(Bundle state) {
        controller = Robolectric.buildActivity(MainActivity.class).create(state).start().resume().visible();
        activity = controller.get();
    }

    @Test public void edgeToEdgeKeepsContentOutsideBarsCutoutsAndKeyboard() {
        launch(null);
        View root = activity.findViewById(R.id.window_content);
        root.dispatchApplyWindowInsets(new WindowInsets.Builder()
                .setInsets(WindowInsets.Type.systemBars(), Insets.of(0, 28, 0, 24))
                .setInsets(WindowInsets.Type.displayCutout(), Insets.of(32, 0, 0, 0))
                .setInsets(WindowInsets.Type.ime(), Insets.of(0, 0, 0, 260))
                .build());
        assertEquals(32, root.getPaddingLeft());
        assertEquals(28, root.getPaddingTop());
        assertEquals(260, root.getPaddingBottom());
    }

    @Test public void keyboardEnableExplainsPrivacyAndRequiresSystemConfirmation() {
        launch(null);
        String previous = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        activity.findViewById(R.id.keyboard_enable_button).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        assertNull(shadowOf(activity).getNextStartedActivity());
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertTrue(dialog.isShowing());
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertNotNull(intent);
        assertEquals(Settings.ACTION_INPUT_METHOD_SETTINGS, intent.getAction());
        assertEquals(previous, Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD));
    }

    @Test public void freshInstallCanCreateProfileWithoutAnyExistingTraining() {
        store.deleteProfile(profileId);
        launch(null);
        click(R.string.add_profile);
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        EditText alias = findType(dialog.getWindow().getDecorView(), EditText.class);
        assertNotNull(alias);
        alias.setText("New child");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(1, store.profiles().size());
        assertEquals("New child", store.profiles().get(0).name);
        assertTrue(drawing().isEnabled());
    }

    @Test public void missingProfileProducesDrawingGuidanceInsteadOfIgnoringTheGesture() {
        store.deleteProfile(profileId);
        launch(null);
        assertFalse(drawing().isEnabled());
        android.view.MotionEvent event = android.view.MotionEvent.obtain(
                0, 0, android.view.MotionEvent.ACTION_DOWN, 20, 20, 0);
        try {
            assertTrue(drawing().onTouchEvent(event));
        } finally {
            event.recycle();
        }
        assertEquals(activity.getString(R.string.profile_required),
                ((TextView) activity.findViewById(R.id.status_text)).getText().toString());
        assertTrue(drawing().getInk().isEmpty());
    }

    @Test public void manualConfirmationDoesNotTrainWithoutConsent() {
        launch(null);
        click(R.string.writing_mode);
        chooseManual(7);
        assertFalse(((CheckBox) activity.findViewById(R.id.learn_checkbox)).isChecked());
        activity.findViewById(R.id.confirm_character_button).performClick();
        assertEquals("7", output());
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
    }

    @Test public void correctionCanTrainAndRecognitionNeverAutoAppends() throws Exception {
        launch(null);
        click(R.string.writing_mode);
        drawing().setInk(ink);
        chooseManual(7);
        ((CheckBox) activity.findViewById(R.id.learn_checkbox)).setChecked(true);
        activity.findViewById(R.id.confirm_character_button).performClick();
        assertEquals("7", output());
        assertEquals("7", store.examples(profileId, Alphabet.DIGITS).get(0).label);
        assertTrue(drawing().getInk().isEmpty());
        Ink other = new Ink(Collections.singletonList(Arrays.asList(
                new Ink.Point(100, 100), new Ink.Point(900, 100))));
        store.addExample(profileId, Alphabet.DIGITS, "1", other);
        drawing().setInk(ink);
        activity.findViewById(R.id.recognize_button).performClick();
        await(() -> candidate() != null);
        assertEquals("7", output());
        assertFalse(activity.findViewById(R.id.confirm_character_button).isEnabled());
        candidate().performClick();
        assertEquals("7", output());
        activity.findViewById(R.id.confirm_character_button).performClick();
        assertEquals("77", output());
        assertEquals(2, store.examples(profileId, Alphabet.DIGITS).size());
    }

    @Test public void drawingChangesInvalidateChosenCharactersAndLearningConsent() {
        launch(null);
        click(R.string.writing_mode);
        drawing().setInk(ink);
        chooseManual(3);
        ((CheckBox) activity.findViewById(R.id.learn_checkbox)).setChecked(true);
        assertTrue(activity.findViewById(R.id.confirm_character_button).isEnabled());
        drawing().clear();
        assertFalse(activity.findViewById(R.id.confirm_character_button).isEnabled());
        assertFalse(((CheckBox) activity.findViewById(R.id.learn_checkbox)).isChecked());
        assertEquals("", output());
    }

    @Test public void rotationRestoresDraftButNeverRestoresPendingAcceptance() {
        launch(null);
        click(R.string.writing_mode);
        chooseManual(2);
        activity.findViewById(R.id.confirm_character_button).performClick();
        drawing().setInk(ink);
        chooseManual(3);
        ((CheckBox) activity.findViewById(R.id.learn_checkbox)).setChecked(true);
        Bundle state = new Bundle();
        controller.saveInstanceState(state).pause().stop().destroy();
        controller = null;
        launch(state);
        assertEquals("2", output());
        assertArrayEquals(ink.encode(), drawing().getInk().encode());
        assertFalse(activity.findViewById(R.id.confirm_character_button).isEnabled());
        assertFalse(((CheckBox) activity.findViewById(R.id.learn_checkbox)).isChecked());
    }

    @Test public void hebrewInterfaceAndFinalLettersPreserveLogicalOutput() {
        context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE).edit()
                .putString("language", "he").commit();
        Bundle state = new Bundle();
        state.putLong("profile", profileId);
        state.putString("group", Alphabet.HEBREW);
        state.putBoolean("training", false);
        launch(state);
        assertEquals("\u05db\u05ea\u05d1 \u05d4\u05d9\u05d3 \u05e9\u05dc\u05d9", activity.getString(R.string.app_name));
        chooseManual(Alphabet.labels(Alphabet.HEBREW).indexOf("\u05de"));
        activity.findViewById(R.id.confirm_character_button).performClick();
        chooseManual(Alphabet.labels(Alphabet.HEBREW).indexOf("\u05dd"));
        activity.findViewById(R.id.confirm_character_button).performClick();
        assertEquals("\u05de\u05dd", output());
        assertEquals(View.LAYOUT_DIRECTION_RTL,
                activity.getResources().getConfiguration().getLayoutDirection());
    }

    @Test public void savingTrainingExampleIsExplicitAndSurvivesActivityRestart() {
        launch(null);
        drawing().setInk(ink);
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
        activity.findViewById(R.id.save_example_button).performClick();
        assertEquals("0", store.examples(profileId, Alphabet.DIGITS).get(0).label);
        assertTrue(drawing().getInk().isEmpty());
        controller.pause().stop().destroy();
        controller = null;
        launch(null);
        assertEquals(1, store.examples(profileId, Alphabet.DIGITS).size());
        assertTrue(drawing().isEnabled());
    }

    @Test public void switchingProfilesClearsInkOutputAndPendingLearning() {
        store.createProfile("Second child");
        launch(null);
        click(R.string.writing_mode);
        chooseManual(1);
        activity.findViewById(R.id.confirm_character_button).performClick();
        drawing().setInk(ink);
        chooseManual(2);
        ((CheckBox) activity.findViewById(R.id.learn_checkbox)).setChecked(true);
        Button profileButton = findButton(activity.getWindow().getDecorView(), "Profile:");
        assertNotNull(profileButton);
        profileButton.performClick();
        selectDialogItem(1);
        ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals("", output());
        assertTrue(drawing().getInk().isEmpty());
        assertFalse(activity.findViewById(R.id.confirm_character_button).isEnabled());
        assertFalse(((CheckBox) activity.findViewById(R.id.learn_checkbox)).isChecked());
        assertTrue(store.examples(profileId, Alphabet.DIGITS).isEmpty());
    }

    private void chooseManual(int index) {
        click(R.string.manual_correction);
        selectDialogItem(index);
    }

    @Test public void everyHebrewEnglishAndDigitLabelCanBeTaughtFromVisibleControls() throws Exception {
        launch(null);
        String[] alphabets = {Alphabet.HEBREW, Alphabet.ENGLISH_UPPER,
                Alphabet.ENGLISH_LOWER, Alphabet.DIGITS};
        int[] controls = {R.id.train_hebrew, R.id.train_english_upper,
                R.id.train_english_lower, R.id.train_digits};
        int total = 0;
        for (int i = 0; i < alphabets.length; i++) {
            activity.findViewById(controls[i]).performClick();
            assertTrue(activity.findViewById(controls[i]).isSelected());
            for (String label : Alphabet.labels(alphabets[i])) {
                selectTrainingCharacter(label);
                drawing().setInk(ink);
                activity.findViewById(R.id.save_example_button).performClick();
                assertTrue(drawing().getInk().isEmpty());
                total++;
            }
            final String alphabet = alphabets[i];
            await(() -> store.exampleCounts(profileId, alphabet).size() == Alphabet.labels(alphabet).size());
            for (String label : Alphabet.labels(alphabet)) {
                assertEquals(Integer.valueOf(1), store.exampleCounts(profileId, alphabet).get(label));
            }
        }
        assertEquals(89, total);
    }

    @Test public void letterTrainingSelectionSurvivesColdReopenAndInterfaceLanguageChange() {
        launch(null);
        activity.findViewById(R.id.train_hebrew).performClick();
        selectTrainingCharacter("\u05e3");
        drawing().setInk(ink);
        activity.findViewById(R.id.save_example_button).performClick();
        controller.pause().stop().destroy();
        controller = null;
        context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE).edit()
                .putString("language", "he").commit();
        launch(null);
        assertTrue(activity.findViewById(R.id.train_hebrew).isSelected());
        assertTrue(trainingCharacter("\u05e3").isSelected());
        assertEquals(1, store.examples(profileId, Alphabet.HEBREW).size());
        activity.findViewById(R.id.train_english_lower).performClick();
        selectTrainingCharacter("z");
        activity.findViewById(R.id.train_hebrew).performClick();
        assertTrue(trainingCharacter("\u05e3").isSelected());
        activity.findViewById(R.id.train_english_lower).performClick();
        assertTrue(trainingCharacter("z").isSelected());
    }

    @Test public void changingTrainingTargetClearsInkInsteadOfMislabelingIt() {
        launch(null);
        drawing().setInk(ink);
        activity.findViewById(R.id.train_english_upper).performClick();
        assertTrue(drawing().getInk().isEmpty());
        drawing().setInk(ink);
        selectTrainingCharacter("B");
        assertTrue(drawing().getInk().isEmpty());
        assertTrue(store.examples(profileId, Alphabet.ENGLISH_UPPER).isEmpty());
        assertTrue(((TextView) activity.findViewById(R.id.training_target)).getText().toString().contains("B"));
    }

    @Test public void keyboardTrainingIntentOpensItsAlphabetAndRestoresSafely() {
        context.getSharedPreferences("handwriting-ui", Context.MODE_PRIVATE).edit()
                .putString("training-alphabet", Alphabet.DIGITS).commit();
        Intent intent = new Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_TRAINING_ALPHABET, Alphabet.HEBREW);
        controller = Robolectric.buildActivity(MainActivity.class, intent).setup();
        activity = controller.get();
        assertTrue(activity.findViewById(R.id.train_hebrew).isSelected());
        assertEquals(27, ((GridLayout) activity.findViewById(R.id.training_character_grid)).getChildCount());
        activity.findViewById(R.id.train_english_upper).performClick();
        selectTrainingCharacter("Z");
        Bundle state = new Bundle();
        controller.saveInstanceState(state).pause().stop().destroy();
        controller = Robolectric.buildActivity(MainActivity.class, intent).create(state).start().resume().visible();
        activity = controller.get();
        assertTrue(activity.findViewById(R.id.train_english_upper).isSelected());
        assertTrue(trainingCharacter("Z").isSelected());
    }

    @Test public void keyboardCanReturnAnExistingActivityToTheRequestedLetterTraining() {
        launch(null);
        click(R.string.writing_mode);
        drawing().setInk(ink);
        activity.onNewIntent(new Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_TRAINING_ALPHABET, Alphabet.HEBREW));
        assertTrue(activity.findViewById(R.id.train_hebrew).isSelected());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.training_character_grid).getVisibility());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.save_example_button).getVisibility());
        assertTrue(drawing().getInk().isEmpty());
        selectTrainingCharacter("\u05da");
        click(R.string.writing_mode);
        activity.onNewIntent(new Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_TRAINING_ALPHABET, Alphabet.HEBREW));
        assertEquals(View.VISIBLE, activity.findViewById(R.id.save_example_button).getVisibility());
        assertTrue(trainingCharacter("\u05da").isSelected());
    }

    @Test public void trainedUnusualEnglishAndHebrewShapesAreRecognizedThroughTheUi() throws Exception {
        launch(null);
        int[] groups = {R.id.train_english_upper, R.id.train_english_lower, R.id.train_hebrew};
        String[][] labels = {{"A", "B"}, {"a", "b"}, {"\u05d0", "\u05da"}};
        Ink alternative = new Ink(Collections.singletonList(Arrays.asList(
                new Ink.Point(500, 100), new Ink.Point(500, 900))));
        for (int i = 0; i < groups.length; i++) {
            click(R.string.training_mode);
            activity.findViewById(groups[i]).performClick();
            for (int label = 0; label < 2; label++) {
                selectTrainingCharacter(labels[i][label]);
                drawing().setInk(label == 0 ? ink : alternative);
                activity.findViewById(R.id.save_example_button).performClick();
            }
            click(R.string.writing_mode);
            drawing().setInk(ink);
            activity.findViewById(R.id.recognize_button).performClick();
            await(() -> candidate() != null);
            assertTrue(candidate().getText().toString().contains(labels[i][0]));
            candidate().performClick();
            activity.findViewById(R.id.confirm_character_button).performClick();
        }
        assertEquals("Aa\u05d0", output());
    }

    private void selectTrainingCharacter(String label) {
        trainingCharacter(label).performClick();
    }

    private Button trainingCharacter(String label) {
        ViewGroup grid = activity.findViewById(R.id.training_character_grid);
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (label.equals(child.getTag())) return (Button) child;
        }
        throw new AssertionError("Missing training character: " + label);
    }

    private void selectDialogItem(int index) {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getListView().performItemClick(null, index, index);
        shadowOf(Looper.getMainLooper()).idle();
    }

    private void click(int resource) {
        Button button = findButton(activity.getWindow().getDecorView(), activity.getString(resource));
        assertNotNull(activity.getString(resource), button);
        button.performClick();
        shadowOf(Looper.getMainLooper()).idle();
    }

    private Button candidate() {
        return findButton(activity.getWindow().getDecorView(), "match similarity");
    }

    private DrawingView drawing() {
        return activity.findViewById(R.id.drawing_area);
    }

    private String output() {
        return ((TextView) activity.findViewById(R.id.output_text)).getText().toString();
    }

    private static Button findButton(View view, String text) {
        if (view instanceof Button && ((Button) view).getText().toString().contains(text)) return (Button) view;
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                Button found = findButton(parent.getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static <T extends View> T findType(View view, Class<T> type) {
        if (type.isInstance(view)) return type.cast(view);
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                T found = findType(parent.getChildAt(i), type);
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
        fail("Recognition did not publish suggestions");
    }
}
