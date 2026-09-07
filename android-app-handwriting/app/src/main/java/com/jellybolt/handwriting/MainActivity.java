package com.jellybolt.handwriting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.BidiFormatter;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jellybolt.handwriting.core.Alphabet;
import com.jellybolt.handwriting.core.HandwritingRecognizer;
import com.jellybolt.handwriting.core.Ink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class MainActivity extends Activity {
    public static final String EXTRA_TRAINING_ALPHABET = "com.jellybolt.handwriting.TRAINING_ALPHABET";
    private static final String PREFERENCES = "handwriting-ui";
    private static final int MAX_OUTPUT = 4096;
    private static final int INK_COLOR = Color.rgb(30, 64, 72);
    private static final int ACCENT = Color.rgb(37, 96, 102);
    private static final String[] GROUPS = {
            Alphabet.DIGITS, Alphabet.ENGLISH_UPPER, Alphabet.ENGLISH_LOWER, Alphabet.HEBREW
    };
    private static final int[] GROUP_NAMES = {
            R.string.digits, R.string.english_upper, R.string.english_lower, R.string.hebrew
    };
    private static final int[] GROUP_IDS = {
            R.id.train_digits, R.id.train_english_upper, R.id.train_english_lower, R.id.train_hebrew
    };

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final List<Button> profileControls = new ArrayList<>();
    private final Map<String, Button> groupButtons = new LinkedHashMap<>();
    private final Map<String, Button> characterButtons = new LinkedHashMap<>();
    private Map<String, Integer> characterCounts = Collections.emptyMap();
    private ProfileStore store;
    private SharedPreferences preferences;
    private List<ProfileStore.Profile> profiles = new ArrayList<>();
    private long profileId = -1;
    private String group = Alphabet.DIGITS;
    private String trainingLabel = "0";
    private String chosenLabel;
    private boolean training = true;
    private final StringBuilder output = new StringBuilder();
    private long draftGeneration;
    private long countRequest;
    private long recognitionRequest;
    private Future<?> recognitionTask;
    private Future<?> countTask;
    private volatile boolean destroyed;

    private DrawingView drawing;
    private Button profileButton;
    private Button trainingButton;
    private Button writingButton;
    private TextView trainingTarget;
    private TextView alphabetSummary;
    private GridLayout characterGrid;
    private Button saveButton;
    private Button undoExampleButton;
    private Button recognizeButton;
    private Button confirmButton;
    private Button correctionButton;
    private Button languageButton;
    private LinearLayout trainingPanel;
    private LinearLayout writingPanel;
    private LinearLayout candidateButtons;
    private TextView instructions;
    private TextView sampleCount;
    private TextView suggestionsStatus;
    private TextView chosenText;
    private TextView outputText;
    private TextView status;
    private CheckBox learnCheck;
    private ScrollView scroll;

    @Override protected void attachBaseContext(Context base) {
        SharedPreferences settings = base.getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        String deviceLanguage = base.getResources().getConfiguration().getLocales().get(0).getLanguage();
        String fallback = ("he".equals(deviceLanguage) || "iw".equals(deviceLanguage)) ? "he" : "en";
        String language = settings.getString("language", fallback);
        Locale locale = Locale.forLanguageTag("he".equals(language) ? "he" : "en");
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        super.attachBaseContext(base.createConfigurationContext(config));
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        store = new ProfileStore(getApplicationContext());
        profileId = preferences.getLong("profile", -1);
        String savedGroup = preferences.getString("training-alphabet", Alphabet.DIGITS);
        if (Alphabet.isGroup(savedGroup)) group = savedGroup;
        String requestedGroup = getIntent().getStringExtra(EXTRA_TRAINING_ALPHABET);
        if (state == null && Alphabet.isGroup(requestedGroup)) group = requestedGroup;
        trainingLabel = savedTrainingLabel(group);
        if (state != null) {
            profileId = state.getLong("profile", profileId);
            String restoredGroup = state.getString("group", group);
            for (String valid : GROUPS) if (valid.equals(restoredGroup)) group = valid;
            training = state.getBoolean("training", true);
            String restoredLabel = state.getString("trainingLabel", Alphabet.labels(group).get(0));
            trainingLabel = Alphabet.labels(group).contains(restoredLabel)
                    ? restoredLabel : Alphabet.labels(group).get(0);
        }
        rememberTrainingSelection();
        buildInterface();
        try {
            profiles = store.profiles();
            if (currentProfile() == null) profileId = profiles.isEmpty() ? -1 : profiles.get(0).id;
        } catch (IllegalArgumentException | IllegalStateException | SQLiteException error) {
            profileId = -1;
            showError(error);
        }
        updateProfileControls();
        updateMode();
        if (state != null && profileId != -1 && state.getLong("profile", -1) == profileId) {
            String savedOutput = state.getString("output", "");
            output.append(savedOutput, 0, Math.min(savedOutput.length(), MAX_OUTPUT));
            byte[] draft = state.getByteArray("ink");
            if (draft != null) {
                try {
                    drawing.setInk(Ink.decode(draft));
                } catch (IllegalArgumentException error) {
                    status.setText(R.string.restore_error);
                    new AlertDialog.Builder(this).setTitle(R.string.error_title)
                            .setMessage(R.string.restore_error).setPositiveButton(R.string.ok, null).show();
                }
            }
            int position = state.getInt("scroll", 0);
            scroll.post(() -> scroll.scrollTo(0, position));
        }
        updateOutput();
        refreshCount();
    }

    private void buildInterface() {
        getWindow().setStatusBarColor(ACCENT);
        getWindow().setNavigationBarColor(Color.rgb(242, 247, 246));
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(242, 247, 246));
        FrameLayout frame = new FrameLayout(this);
        scroll.addView(frame, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout content = column();
        content.setPadding(dp(20), dp(16), dp(20), dp(32));
        int width = Math.min(getResources().getDisplayMetrics().widthPixels, dp(760));
        FrameLayout.LayoutParams centered = new FrameLayout.LayoutParams(width,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        frame.addView(content, centered);
        frame.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int availableWidth = Math.min(right - left, dp(760));
            if (availableWidth > 0 && centered.width != availableWidth) {
                centered.width = availableWidth;
                content.setLayoutParams(centered);
            }
        });

        TextView title = text(R.string.app_name, 30);
        title.setTypeface(null, Typeface.BOLD);
        if (Build.VERSION.SDK_INT >= 28) title.setAccessibilityHeading(true);
        add(content, title);
        add(content, text(R.string.app_subtitle, 18));
        languageButton = button(R.string.language_toggle, view -> toggleLanguage());
        languageButton.setContentDescription(getString(R.string.language_description));
        add(content, languageButton);

        LinearLayout alphabetPanel = card(content);
        heading(alphabetPanel, R.string.training_alphabet_heading);
        add(alphabetPanel, text(R.string.training_alphabet_help, 16));
        for (int row = 0; row < 2; row++) {
            LinearLayout choices = new LinearLayout(this);
            choices.setOrientation(LinearLayout.HORIZONTAL);
            for (int column = 0; column < 2; column++) {
                int index = row * 2 + column;
                String choice = GROUPS[index];
                Button option = button(GROUP_NAMES[index], view -> changeGroup(choice));
                option.setId(GROUP_IDS[index]);
                option.setTextSize(16);
                option.setMinWidth(0);
                option.setMinimumWidth(0);
                groupButtons.put(choice, option);
                choices.addView(option, new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            }
            add(alphabetPanel, choices);
        }
        alphabetSummary = text(R.string.training_alphabet_help, 18);
        alphabetSummary.setId(R.id.training_alphabet_summary);
        add(alphabetPanel, alphabetSummary);

        LinearLayout profilePanel = card(content);
        heading(profilePanel, R.string.profile_heading);
        profileButton = button(R.string.no_profile, view -> chooseProfile());
        add(profilePanel, profileButton);
        Button addProfile = button(R.string.add_profile, view -> addProfile());
        Button deleteProfile = button(R.string.delete_profile, view -> deleteProfile());
        add(profilePanel, addProfile);
        add(profilePanel, deleteProfile);
        profileControls.add(deleteProfile);
        add(profilePanel, text(R.string.privacy_note, 16));
        add(profilePanel, button(R.string.privacy_details_button, view ->
                new AlertDialog.Builder(this).setTitle(R.string.privacy_details_button)
                        .setMessage(R.string.privacy_details).setPositiveButton(R.string.ok, null).show()));

        LinearLayout keyboardPanel = card(content);
        heading(keyboardPanel, R.string.keyboard_heading);
        add(keyboardPanel, text(R.string.keyboard_help, 16));
        Button enableKeyboard = button(R.string.keyboard_enable, view -> explainKeyboardSetup());
        enableKeyboard.setId(R.id.keyboard_enable_button);
        add(keyboardPanel, enableKeyboard);
        Button chooseKeyboard = button(R.string.keyboard_choose, view -> chooseKeyboard());
        chooseKeyboard.setId(R.id.keyboard_choose_button);
        add(keyboardPanel, chooseKeyboard);

        LinearLayout activityPanel = card(content);
        heading(activityPanel, R.string.mode_heading);
        trainingButton = button(R.string.training_mode, view -> changeMode(true));
        writingButton = button(R.string.writing_mode, view -> changeMode(false));
        add(activityPanel, trainingButton);
        add(activityPanel, writingButton);
        instructions = text(R.string.training_instructions, 18);
        add(activityPanel, instructions);

        trainingPanel = card(content);
        heading(trainingPanel, R.string.choose_training_label);
        add(trainingPanel, text(R.string.character_grid_help, 16));
        characterGrid = new GridLayout(this);
        characterGrid.setId(R.id.training_character_grid);
        characterGrid.setColumnCount(4);
        characterGrid.setSaveEnabled(false);
        add(trainingPanel, characterGrid);
        buildCharacterGrid();
        characterGrid.addOnLayoutChangeListener((view, left, top, right, bottom,
                                                oldLeft, oldTop, oldRight, oldBottom) -> {
            int columns = Math.max(1, Math.min(8, (right - left) / dp(56)));
            if (columns != characterGrid.getColumnCount()) {
                characterGrid.removeAllViews();
                characterGrid.setColumnCount(columns);
                buildCharacterGrid();
            }
        });
        sampleCount = text(R.string.count_loading, 18);
        sampleCount.setId(R.id.training_sample_count);
        sampleCount.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        add(trainingPanel, sampleCount);
        add(trainingPanel, text(R.string.training_goal, 16));

        LinearLayout inkPanel = card(content);
        heading(inkPanel, R.string.drawing_heading);
        trainingTarget = text(R.string.training_label, 28);
        trainingTarget.setId(R.id.training_target);
        trainingTarget.setTypeface(null, Typeface.BOLD);
        add(inkPanel, trainingTarget);
        drawing = new DrawingView(this);
        drawing.setId(R.id.drawing_area);
        int availableHeight = getResources().getConfiguration().screenHeightDp;
        int drawingHeight = Math.max(220, Math.min(380, availableHeight / 2));
        int drawingSide = Math.min(width - dp(72), dp(drawingHeight));
        LinearLayout.LayoutParams drawingSize = new LinearLayout.LayoutParams(drawingSide, drawingSide);
        drawingSize.gravity = Gravity.CENTER_HORIZONTAL;
        inkPanel.addView(drawing, drawingSize);
        inkPanel.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int side = Math.min(right - left - inkPanel.getPaddingLeft() - inkPanel.getPaddingRight(),
                    dp(drawingHeight));
            if (side > 0 && drawingSize.width != side) {
                drawingSize.width = side;
                drawingSize.height = side;
                drawing.setLayoutParams(drawingSize);
            }
        });
        drawing.setOnInkChangedListener(this::invalidateDraft);
        drawing.setOnDrawingBlockedListener(() -> status.setText(R.string.profile_required));
        drawing.setOnLimitReachedListener(() -> status.setText(R.string.ink_limit));
        add(inkPanel, text(R.string.drawing_help, 16));
        add(inkPanel, button(R.string.undo_stroke, view -> drawing.undoStroke()));
        add(inkPanel, button(R.string.clear_drawing, view -> {
            drawing.clear();
            status.setText(R.string.drawing_cleared);
        }));
        saveButton = button(R.string.save_sample, view -> saveSample());
        saveButton.setId(R.id.save_example_button);
        add(inkPanel, saveButton);
        undoExampleButton = button(R.string.undo_sample, view -> undoExample());
        add(inkPanel, undoExampleButton);
        recognizeButton = button(R.string.recognize, view -> recognize());
        recognizeButton.setId(R.id.recognize_button);
        add(inkPanel, recognizeButton);

        writingPanel = card(content);
        heading(writingPanel, R.string.suggestions_heading);
        add(writingPanel, text(R.string.score_explanation, 16));
        suggestionsStatus = text(R.string.suggestions_empty, 18);
        suggestionsStatus.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        add(writingPanel, suggestionsStatus);
        candidateButtons = column();
        add(writingPanel, candidateButtons);
        correctionButton = button(R.string.manual_correction, view -> chooseManualCharacter());
        add(writingPanel, correctionButton);
        chosenText = text(R.string.nothing_selected, 22);
        chosenText.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        add(writingPanel, chosenText);
        learnCheck = new CheckBox(this);
        learnCheck.setId(R.id.learn_checkbox);
        learnCheck.setSaveEnabled(false);
        learnCheck.setText(R.string.learn_checkbox);
        learnCheck.setTextColor(INK_COLOR);
        learnCheck.setTextSize(18);
        learnCheck.setMinHeight(dp(56));
        learnCheck.setPadding(dp(8), dp(8), dp(8), dp(8));
        learnCheck.setChecked(false);
        add(writingPanel, learnCheck);
        confirmButton = button(R.string.confirm_character, view -> confirmCharacter());
        confirmButton.setId(R.id.confirm_character_button);
        add(writingPanel, confirmButton);

        LinearLayout outputPanel = card(content);
        heading(outputPanel, R.string.output_heading);
        outputText = text(R.string.output_hint, 32);
        outputText.setId(R.id.output_text);
        outputText.setSaveEnabled(false);
        outputText.setTextIsSelectable(true);
        outputText.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_LTR);
        outputText.setMinHeight(dp(72));
        outputText.setPadding(dp(12), dp(12), dp(12), dp(12));
        outputText.setBackgroundColor(Color.rgb(235, 243, 243));
        add(outputPanel, outputText);
        add(outputPanel, text(R.string.output_help, 16));
        add(outputPanel, button(R.string.copy_output, view -> copyOutput()));
        add(outputPanel, button(R.string.undo_character, view -> {
            if (output.length() > 0) {
                int last = output.offsetByCodePoints(output.length(), -1);
                output.delete(last, output.length());
                updateOutput();
            } else status.setText(R.string.output_empty);
        }));
        add(outputPanel, button(R.string.add_space, view -> {
            if (requireProfile() && hasOutputRoom()) {
                output.append(' ');
                updateOutput();
            }
        }));
        add(outputPanel, button(R.string.clear_output, view -> clearOutput()));
        status = text(R.string.ready, 18);
        status.setId(R.id.status_text);
        status.setSaveEnabled(false);
        status.setPadding(dp(20), dp(10), dp(20), dp(10));
        status.setMinHeight(dp(56));
        status.setMaxLines(3);
        status.setEllipsize(TextUtils.TruncateAt.END);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        LinearLayout root = column();
        root.setId(R.id.window_content);
        root.setBackgroundColor(Color.rgb(242, 247, 246));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        root.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets safe = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
                return insets;
            });
            root.requestApplyInsets();
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                int lightBars = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(lightBars, lightBars);
            }
        }
    }

    private void toggleLanguage() {
        String current = getResources().getConfiguration().getLocales().get(0).getLanguage();
        preferences.edit().putString("language", ("he".equals(current) || "iw".equals(current)) ? "en" : "he").apply();
        recreate();
    }

    private void explainKeyboardSetup() {
        new AlertDialog.Builder(this).setTitle(R.string.keyboard_heading)
                .setMessage(R.string.keyboard_permission_help)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.keyboard_open_settings, (dialog, which) -> {
                    try {
                        startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
                    } catch (ActivityNotFoundException error) {
                        status.setText(R.string.keyboard_settings_unavailable);
                        new AlertDialog.Builder(this).setTitle(R.string.error_title)
                                .setMessage(R.string.keyboard_settings_unavailable)
                                .setPositiveButton(R.string.ok, null).show();
                    }
                }).show();
    }

    private void chooseKeyboard() {
        InputMethodManager manager = getSystemService(InputMethodManager.class);
        if (manager == null) {
            status.setText(R.string.keyboard_settings_unavailable);
            return;
        }
        try {
            manager.showInputMethodPicker();
        } catch (IllegalStateException | SecurityException error) {
            status.setText(R.string.keyboard_settings_unavailable);
        }
    }

    private void changeMode(boolean nextTraining) {
        if (training == nextTraining) return;
        training = nextTraining;
        invalidateDraft();
        updateMode();
        refreshCount();
        status.setText(R.string.mode_changed);
    }

    private void updateMode() {
        trainingPanel.setVisibility(training ? View.VISIBLE : View.GONE);
        writingPanel.setVisibility(training ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(training ? View.VISIBLE : View.GONE);
        undoExampleButton.setVisibility(training ? View.VISIBLE : View.GONE);
        recognizeButton.setVisibility(training ? View.GONE : View.VISIBLE);
        trainingButton.setSelected(training);
        writingButton.setSelected(!training);
        instructions.setText(training ? R.string.training_instructions : R.string.writing_instructions);
        trainingTarget.setVisibility(training ? View.VISIBLE : View.GONE);
        updateTrainingLabels();
        updateProfileControls();
    }

    private void changeGroup(String nextGroup) {
        if (!Alphabet.isGroup(nextGroup)) throw new IllegalArgumentException("Invalid training alphabet");
        if (group.equals(nextGroup)) return;
        group = nextGroup;
        trainingLabel = savedTrainingLabel(group);
        rememberTrainingSelection();
        characterCounts = Collections.emptyMap();
        drawing.clear();
        buildCharacterGrid();
        updateMode();
        refreshCount();
        status.setText(R.string.group_changed);
    }

    private String savedTrainingLabel(String alphabet) {
        List<String> labels = Alphabet.labels(alphabet);
        String saved = preferences.getString("training-label-" + alphabet, labels.get(0));
        return labels.contains(saved) ? saved : labels.get(0);
    }

    private void rememberTrainingSelection() {
        preferences.edit().putString("training-alphabet", group)
                .putString("training-label-" + group, trainingLabel).apply();
    }

    private void buildCharacterGrid() {
        characterGrid.removeAllViews();
        characterButtons.clear();
        characterGrid.setLayoutDirection(Alphabet.HEBREW.equals(group)
                ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        int columns = characterGrid.getColumnCount();
        List<String> labels = Alphabet.labels(group);
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            Button option = button(label, view -> {
                if (trainingLabel.equals(label)) return;
                trainingLabel = label;
                rememberTrainingSelection();
                drawing.clear();
                updateTrainingLabels();
                refreshCount();
                status.setText(getString(R.string.training_character_changed, isolated(label)));
            });
            option.setTag(label);
            option.setTextSize(22);
            option.setPadding(dp(2), dp(2), dp(2), dp(2));
            option.setMinWidth(0);
            option.setMinimumWidth(0);
            option.setSaveEnabled(false);
            GridLayout.LayoutParams cell = new GridLayout.LayoutParams(
                    GridLayout.spec(i / columns), GridLayout.spec(i % columns, 1f));
            cell.width = 0;
            cell.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            characterGrid.addView(option, cell);
            characterButtons.put(label, option);
        }
        updateTrainingLabels();
    }

    private void updateTrainingLabels() {
        for (Map.Entry<String, Button> entry : groupButtons.entrySet()) {
            entry.getValue().setSelected(group.equals(entry.getKey()));
        }
        if (alphabetSummary != null) {
            alphabetSummary.setText(getString(R.string.training_alphabet_selected,
                    getString(groupName()), Alphabet.labels(group).size()));
        }
        if (trainingTarget != null) {
            trainingTarget.setText(getString(R.string.training_label, isolated(trainingLabel)));
        }
        for (Map.Entry<String, Button> entry : characterButtons.entrySet()) {
            String label = entry.getKey();
            int count = characterCounts.getOrDefault(label, 0);
            entry.getValue().setText(label + "\n" + count);
            entry.getValue().setContentDescription(getString(
                    R.string.training_character_count, label, count));
            entry.getValue().setSelected(trainingLabel.equals(label));
        }
    }

    private void chooseManualCharacter() {
        if (!requireProfile()) return;
        List<String> labels = Alphabet.labels(group);
        new AlertDialog.Builder(this).setTitle(R.string.choose_character)
                .setSingleChoiceItems(labels.toArray(new String[0]), labels.indexOf(chosenLabel),
                        (dialog, which) -> {
                            dialog.dismiss();
                            selectCharacter(labels.get(which));
                        }).setNegativeButton(R.string.cancel, null).show();
    }

    private void selectCharacter(String label) {
        chosenLabel = label;
        learnCheck.setChecked(false);
        chosenText.setText(getString(R.string.chosen_character, isolated(label)));
        confirmButton.setEnabled(profileId != -1);
    }

    private void invalidateDraft() {
        draftGeneration++;
        recognitionRequest++;
        if (recognitionTask != null) recognitionTask.cancel(false);
        if (candidateButtons == null) return;
        candidateButtons.removeAllViews();
        chosenLabel = null;
        chosenText.setText(R.string.nothing_selected);
        suggestionsStatus.setText(R.string.suggestions_empty);
        learnCheck.setChecked(false);
        confirmButton.setEnabled(false);
        recognizeButton.setEnabled(profileId != -1);
        recognizeButton.setText(R.string.recognize);
    }

    private void recognize() {
        if (!requireProfile() || !completedDrawing()) return;
        invalidateDraft();
        final Ink ink = drawing.getInk();
        final long requestedProfile = profileId;
        final String requestedGroup = group;
        final long generation = draftGeneration;
        final long request = recognitionRequest;
        recognizeButton.setEnabled(false);
        recognizeButton.setText(R.string.recognizing);
        suggestionsStatus.setText(R.string.recognizing);
        recognitionTask = worker.submit(() -> {
            if (destroyed) return;
            try {
                HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(
                        ink, store.examples(requestedProfile, requestedGroup));
                runOnUiThread(() -> {
                    if (!matchesDraft(requestedProfile, requestedGroup, generation)
                            || request != recognitionRequest || training) return;
                    recognizeButton.setEnabled(true);
                    recognizeButton.setText(R.string.recognize);
                    candidateButtons.removeAllViews();
                    if (result.trainedLabels < 2) {
                        suggestionsStatus.setText(R.string.too_few_labels);
                        return;
                    }
                    suggestionsStatus.setText(result.uncertain ? R.string.uncertain_result : R.string.choose_result);
                    int shown = 0;
                    for (HandwritingRecognizer.Candidate candidate : result.candidates) {
                        if (shown++ >= 3) break;
                        Button option = button(getString(R.string.candidate_match,
                                isolated(candidate.label), candidate.similarity), view -> {
                            if (matchesDraft(requestedProfile, requestedGroup, generation)
                                    && request == recognitionRequest) selectCharacter(candidate.label);
                        });
                        add(candidateButtons, option);
                    }
                });
            } catch (IllegalArgumentException | IllegalStateException | SQLiteException error) {
                runOnUiThread(() -> {
                    if (!matchesDraft(requestedProfile, requestedGroup, generation)
                            || request != recognitionRequest) return;
                    recognizeButton.setEnabled(true);
                    recognizeButton.setText(R.string.recognize);
                    suggestionsStatus.setText(R.string.suggestions_empty);
                    showError(error);
                });
            }
        });
    }

    private boolean matchesDraft(long requestedProfile, String requestedGroup, long generation) {
        return !destroyed && profileId == requestedProfile && group.equals(requestedGroup)
                && draftGeneration == generation;
    }

    private void saveSample() {
        if (!requireProfile() || !completedDrawing()) return;
        try {
            store.addExample(profileId, group, trainingLabel, drawing.getInk());
            drawing.clear();
            refreshCount();
            status.setText(getString(R.string.sample_saved, isolated(trainingLabel)));
        } catch (IllegalArgumentException | IllegalStateException | SQLiteException error) {
            showError(error);
        }
    }

    private void undoExample() {
        if (!requireProfile()) return;
        final long requestedProfile = profileId;
        final String requestedGroup = group;
        final String label = trainingLabel;
        new AlertDialog.Builder(this).setTitle(R.string.undo_sample)
                .setMessage(getString(R.string.undo_sample_message, isolated(label)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (profileId != requestedProfile || !group.equals(requestedGroup)) return;
                    try {
                        boolean removed = store.deleteLastExample(requestedProfile, requestedGroup, label);
                        invalidateDraft();
                        refreshCount();
                        status.setText(getString(removed ? R.string.sample_removed : R.string.no_sample, isolated(label)));
                    } catch (IllegalArgumentException | IllegalStateException | SQLiteException error) {
                        showError(error);
                    }
                }).show();
    }

    private void confirmCharacter() {
        if (!requireProfile()) return;
        if (drawing.isDrawing()) {
            status.setText(R.string.finish_stroke);
            return;
        }
        if (chosenLabel == null || !Alphabet.labels(group).contains(chosenLabel)) {
            status.setText(R.string.choose_first);
            return;
        }
        if (!hasOutputRoom()) return;
        final String label = chosenLabel;
        final boolean learn = learnCheck.isChecked();
        if (learn && drawing.getInk().isEmpty()) {
            status.setText(R.string.learn_needs_ink);
            return;
        }
        try {
            if (learn) store.addExample(profileId, group, label, drawing.getInk());
            output.append(label);
            updateOutput();
            drawing.clear();
            if (learn) refreshCount();
            status.setText(getString(learn ? R.string.character_learned : R.string.character_appended, isolated(label)));
        } catch (IllegalArgumentException | IllegalStateException | SQLiteException error) {
            showError(error);
        }
    }

    private void refreshCount() {
        final long requestedProfile = profileId;
        final String requestedGroup = group;
        final String label = trainingLabel;
        final long request = ++countRequest;
        if (countTask != null) countTask.cancel(false);
        characterCounts = Collections.emptyMap();
        updateTrainingLabels();
        if (profileId == -1) {
            sampleCount.setText(R.string.profile_required);
            return;
        }
        sampleCount.setText(R.string.count_loading);
        countTask = worker.submit(() -> {
            if (destroyed) return;
            try {
                Map<String, Integer> counts = store.exampleCounts(requestedProfile, requestedGroup);
                final int count = counts.getOrDefault(label, 0);
                final int trainedCount = counts.size();
                runOnUiThread(() -> {
                    if (destroyed || request != countRequest || requestedProfile != profileId
                            || !requestedGroup.equals(group)) return;
                    sampleCount.setText(getString(R.string.sample_count, isolated(label), count, trainedCount));
                    characterCounts = counts;
                    updateTrainingLabels();
                });
            } catch (IllegalArgumentException | IllegalStateException | SQLiteException error) {
                runOnUiThread(() -> {
                    if (destroyed || request != countRequest) return;
                    sampleCount.setText(R.string.count_unavailable);
                    showError(error);
                });
            }
        });
    }

    private void addProfile() {
        EditText alias = new EditText(this);
        alias.setSingleLine(true);
        alias.setTextSize(20);
        alias.setMinHeight(dp(56));
        alias.setHint(R.string.profile_alias);
        alias.setContentDescription(getString(R.string.profile_alias));
        alias.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        alias.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        alias.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        LinearLayout contents = column();
        contents.setPadding(dp(24), dp(8), dp(24), dp(8));
        add(contents, text(R.string.alias_help, 18));
        add(contents, alias);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.add_profile)
                .setView(contents).setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.create_profile, null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String name = alias.getText().toString().trim();
            if (name.isEmpty() || name.length() > 40) {
                alias.setError(getString(R.string.alias_invalid));
                return;
            }
            try {
                ProfileStore.Profile created = store.createProfile(name);
                profiles.add(created);
                activateProfile(created.id);
                dialog.dismiss();
                status.setText(R.string.profile_created);
            } catch (SQLiteConstraintException error) {
                Log.w("Handwriting", "Profile alias was rejected by local storage", error);
                alias.setError(getString(R.string.alias_duplicate));
            } catch (IllegalArgumentException | IllegalStateException | SQLiteException error) {
                showError(error);
            }
        }));
        dialog.show();
    }

    private void chooseProfile() {
        try {
            profiles = store.profiles();
        } catch (IllegalArgumentException | IllegalStateException | SQLiteException error) {
            showError(error);
            return;
        }
        if (profiles.isEmpty()) {
            addProfile();
            return;
        }
        final List<ProfileStore.Profile> choices = new ArrayList<>(profiles);
        String[] names = new String[choices.size()];
        int selected = -1;
        for (int i = 0; i < names.length; i++) {
            names[i] = isolated(choices.get(i).name);
            if (choices.get(i).id == profileId) selected = i;
        }
        new AlertDialog.Builder(this).setTitle(R.string.switch_profile)
                .setSingleChoiceItems(names, selected, (dialog, which) -> {
                    dialog.dismiss();
                    ProfileStore.Profile selectedProfile = choices.get(which);
                    if (selectedProfile.id == profileId) return;
                    new AlertDialog.Builder(this).setTitle(R.string.switch_profile)
                            .setMessage(getString(R.string.switch_profile_message, isolated(selectedProfile.name)))
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.confirm, (confirmation, action) -> {
                                activateProfile(selectedProfile.id);
                                status.setText(R.string.profile_changed);
                            }).show();
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private void deleteProfile() {
        ProfileStore.Profile selectedProfile = currentProfile();
        if (selectedProfile == null) {
            status.setText(R.string.profile_required);
            return;
        }
        new AlertDialog.Builder(this).setTitle(R.string.delete_profile)
                .setMessage(getString(R.string.delete_profile_message, isolated(selectedProfile.name)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_permanently, (dialog, which) -> {
                    try {
                        store.deleteProfile(selectedProfile.id);
                        profiles.removeIf(profile -> profile.id == selectedProfile.id);
                        activateProfile(profiles.isEmpty() ? -1 : profiles.get(0).id);
                        status.setText(R.string.profile_deleted);
                    } catch (IllegalArgumentException | IllegalStateException | SQLiteException error) {
                        showError(error);
                    }
                }).show();
    }

    private void activateProfile(long nextProfile) {
        profileId = nextProfile;
        preferences.edit().putLong("profile", profileId).apply();
        drawing.clear();
        output.setLength(0);
        updateOutput();
        updateProfileControls();
        refreshCount();
    }

    private ProfileStore.Profile currentProfile() {
        for (ProfileStore.Profile profile : profiles) if (profile.id == profileId) return profile;
        return null;
    }

    private void updateProfileControls() {
        ProfileStore.Profile profile = currentProfile();
        boolean enabled = profile != null;
        profileButton.setText(profile == null ? getString(R.string.no_profile)
                : getString(R.string.current_profile, isolated(profile.name)));
        for (Button control : profileControls) control.setEnabled(enabled);
        drawing.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        undoExampleButton.setEnabled(enabled);
        recognizeButton.setEnabled(enabled);
        correctionButton.setEnabled(enabled);
        learnCheck.setEnabled(enabled);
        confirmButton.setEnabled(enabled && chosenLabel != null);
    }

    private boolean requireProfile() {
        if (currentProfile() != null) return true;
        status.setText(R.string.profile_required);
        return false;
    }

    private boolean completedDrawing() {
        if (drawing.isDrawing()) {
            status.setText(R.string.finish_stroke);
            return false;
        }
        if (drawing.getInk().isEmpty()) {
            status.setText(R.string.draw_first);
            return false;
        }
        return true;
    }

    private boolean hasOutputRoom() {
        if (output.length() < MAX_OUTPUT) return true;
        status.setText(R.string.output_limit);
        return false;
    }

    private void updateOutput() {
        outputText.setText(output.toString());
        outputText.setHint(R.string.output_hint);
        outputText.setGravity(Gravity.TOP | Gravity.START);
        // Raw logical text is never reversed or decorated with invisible bidi marks.
        outputText.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_LTR);
    }

    private void copyOutput() {
        if (output.length() == 0) {
            status.setText(R.string.output_empty);
            return;
        }
        ClipboardManager clipboard = getSystemService(ClipboardManager.class);
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), output.toString()));
        status.setText(R.string.output_copied);
    }

    private void clearOutput() {
        if (output.length() == 0) {
            status.setText(R.string.output_empty);
            return;
        }
        new AlertDialog.Builder(this).setTitle(R.string.clear_output)
                .setMessage(R.string.clear_output_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    output.setLength(0);
                    updateOutput();
                    status.setText(R.string.output_cleared);
                }).show();
    }

    private void showError(RuntimeException error) {
        Log.e("Handwriting", "Local handwriting action failed", error);
        if (destroyed || isFinishing()) return;
        int messageResource = R.string.error_detail;
        if (error instanceof IllegalStateException) {
            if ("Example limit reached for this character".equals(error.getMessage())) {
                messageResource = R.string.example_limit_error;
            } else if ("Profile limit reached".equals(error.getMessage())) {
                messageResource = R.string.profile_limit_error;
            }
        }
        String message = getString(messageResource);
        status.setText(message);
        new AlertDialog.Builder(this).setTitle(R.string.error_title).setMessage(message)
                .setPositiveButton(R.string.ok, null).show();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putLong("profile", profileId);
        state.putString("group", group);
        state.putBoolean("training", training);
        state.putString("trainingLabel", trainingLabel);
        state.putString("output", output.toString());
        state.putByteArray("ink", drawing.getInk().encode());
        state.putInt("scroll", scroll.getScrollY());
    }

    @Override protected void onDestroy() {
        destroyed = true;
        if (recognitionTask != null) recognitionTask.cancel(false);
        if (countTask != null) countTask.cancel(false);
        // Close after any in-flight read; never close a helper underneath its worker.
        worker.execute(store::close);
        worker.shutdown();
        super.onDestroy();
    }

    private int groupName() {
        for (int i = 0; i < GROUPS.length; i++) if (GROUPS[i].equals(group)) return GROUP_NAMES[i];
        return R.string.digits;
    }

    private String isolated(String label) {
        return BidiFormatter.getInstance(getResources().getConfiguration().getLocales().get(0)).unicodeWrap(label);
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout card(LinearLayout parent) {
        LinearLayout panel = column();
        panel.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(18));
        panel.setBackground(background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(16);
        parent.addView(panel, params);
        return panel;
    }

    private TextView text(int stringResource, int sp) {
        TextView view = new TextView(this);
        view.setText(stringResource);
        view.setTextSize(sp);
        view.setTextColor(INK_COLOR);
        view.setLineSpacing(dp(3), 1f);
        return view;
    }

    private void heading(LinearLayout parent, int stringResource) {
        TextView heading = text(stringResource, 22);
        heading.setTypeface(null, Typeface.BOLD);
        if (Build.VERSION.SDK_INT >= 28) heading.setAccessibilityHeading(true);
        add(parent, heading);
    }

    private Button button(int stringResource, View.OnClickListener listener) {
        return button(getString(stringResource), listener);
    }

    private Button button(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(18);
        button.setMinHeight(dp(56));
        button.setMinimumHeight(dp(56));
        button.setPadding(dp(12), dp(10), dp(12), dp(10));
        button.setTextColor(new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{android.R.attr.state_selected}, new int[]{}
        }, new int[]{Color.rgb(89, 107, 111), Color.WHITE, INK_COLOR}));
        button.setBackgroundTintList(new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_selected}, new int[]{}
        }, new int[]{ACCENT, Color.rgb(222, 237, 237)}));
        button.setOnClickListener(listener);
        return button;
    }

    private void add(LinearLayout parent, View child) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(8);
        parent.addView(child, params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
