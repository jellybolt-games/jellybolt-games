package com.jellybolt.handwriting;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jellybolt.handwriting.core.Alphabet;
import com.jellybolt.handwriting.core.HandwritingRecognizer;
import com.jellybolt.handwriting.core.Ink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Optional, local-only input method. Android settings and the user own its activation. */
public class HandwritingImeService extends InputMethodService {
    private static final String[] GROUPS = {
            Alphabet.DIGITS, Alphabet.ENGLISH_UPPER, Alphabet.ENGLISH_LOWER, Alphabet.HEBREW
    };
    private static final int[] GROUP_NAMES = {
            R.string.ime_digits, R.string.ime_upper, R.string.ime_lower, R.string.ime_hebrew
    };
    private final Handler main = new Handler(Looper.getMainLooper());
    private final AutoInsertController autoInsert = new AutoInsertController();
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private ProfileStore store;
    private SharedPreferences appPreferences;
    private SharedPreferences imePreferences;
    private SharedPreferences autoPreferences;
    private final SharedPreferences.OnSharedPreferenceChangeListener autoSettingsListener =
            (preferences, key) -> {
                if (AutoInsertSettings.DELAY_KEY.equals(key) && !this.destroyed) refreshAutoSettings();
            };
    private Context ui;
    private EditorInfo editor;
    private boolean editing;
    private volatile boolean destroyed;
    private long sessionRevision;
    private long drawingRevision;
    private long profileRequest;
    private long profileId = -1;
    private String group = Alphabet.DIGITS;
    private boolean handwriting;
    private boolean hebrewTyping;
    private boolean shift;
    private boolean symbols;
    private boolean extraSymbols;
    private boolean loadingProfiles;
    private boolean profilesFailed;
    private boolean recognizing;
    private boolean changingDrawing;
    private boolean manualOpen;
    private Future<?> recognitionTask;
    private int autoDelay;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private boolean committingEditor;
    private AutomaticInsertion automaticInsertion;
    private List<ProfileStore.Profile> profiles = new ArrayList<>();
    private String selectedLabel;
    private Draft selectedDraft;
    private FrameLayout root;
    private LinearLayout content;
    private LinearLayout body;
    private LinearLayout keys;
    private LinearLayout candidates;
    private LinearLayout manualPanel;
    private ScrollView manualScroller;
    private DrawingView drawing;
    private Spinner profileSpinner;
    private Spinner alphabetSpinner;
    private Spinner autoSpinner;
    private Button undoAutomatic;
    private Button recognize;
    private Button confirm;
    private CheckBox learn;
    private TextView selected;
    private TextView status;

    @Override public void onCreate() {
        super.onCreate();
        store = new ProfileStore(getApplicationContext());
        appPreferences = getSharedPreferences("handwriting-ui", MODE_PRIVATE);
        imePreferences = getSharedPreferences("handwriting-ime", MODE_PRIVATE);
        autoPreferences = getSharedPreferences(AutoInsertSettings.PREFERENCES, MODE_PRIVATE);
        autoDelay = AutoInsertSettings.delay(this);
        autoPreferences.registerOnSharedPreferenceChangeListener(autoSettingsListener);
        handwriting = imePreferences.getBoolean("handwriting", false);
        hebrewTyping = "he".equals(imePreferences.getString("typing-language", "en"));
        String savedGroup = imePreferences.getString("alphabet", Alphabet.DIGITS);
        for (String valid : GROUPS) if (valid.equals(savedGroup)) group = valid;
        updateLocale();
    }

    @Override public boolean onEvaluateFullscreenMode() {
        return false;
    }

    /** Normal editor seam; no editor text is cached by this service. */
    protected InputConnection editorConnection() {
        return getCurrentInputConnection();
    }

    @Override public View onCreateInputView() {
        updateLocale();
        root = new FrameLayout(ui) {
            @Override protected void onMeasure(int widthSpec, int heightSpec) {
                int height = keyboardHeight();
                if (MeasureSpec.getMode(heightSpec) != MeasureSpec.UNSPECIFIED) {
                    height = Math.min(height, MeasureSpec.getSize(heightSpec));
                }
                if (getChildCount() > 0 && MeasureSpec.getMode(widthSpec) != MeasureSpec.UNSPECIFIED) {
                    getChildAt(0).getLayoutParams().width = Math.min(dp(720),
                            Math.max(0, MeasureSpec.getSize(widthSpec) - getPaddingLeft() - getPaddingRight()));
                }
                super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        };
        root.setId(R.id.ime_root);
        root.setSaveEnabled(false);
        root.setBackgroundColor(Color.rgb(231, 241, 241));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.displayCutout());
                view.setPadding(bars.left, 0, bars.right, bars.bottom);
            } else {
                view.setPadding(insets.getSystemWindowInsetLeft(), 0,
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        buildContent();
        return root;
    }

    @Override public void onStartInput(EditorInfo info, boolean restarting) {
        super.onStartInput(info, restarting);
        sessionRevision++;
        editor = info;
        editing = info != null;
        selectionStart = info == null ? -1 : info.initialSelStart;
        selectionEnd = info == null ? -1 : info.initialSelEnd;
        autoDelay = AutoInsertSettings.delay(this);
        invalidateDraft(true);
        shift = false;
        symbols = KeyboardEditor.isNumeric(info);
        extraSymbols = false;
        String saved = imePreferences.getString("alphabet", Alphabet.DIGITS);
        group = Alphabet.DIGITS;
        if (!symbols) for (String valid : GROUPS) if (valid.equals(saved)) group = valid;
        updateLocale();
        if (root != null) buildContent();
        loadProfiles();
    }

    @Override public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        sessionRevision++;
        editor = info;
        editing = info != null;
        selectionStart = info == null ? -1 : info.initialSelStart;
        selectionEnd = info == null ? -1 : info.initialSelEnd;
        autoDelay = AutoInsertSettings.delay(this);
        invalidateDraft(true);
        updateLocale();
        if (root != null) buildContent();
        loadProfiles();
    }

    @Override public void onFinishInputView(boolean finishingInput) {
        editing = false;
        sessionRevision++;
        profileRequest++;
        invalidateDraft(true);
        showStatus(R.string.ime_ready);
        super.onFinishInputView(finishingInput);
    }

    @Override public void onFinishInput() {
        editing = false;
        editor = null;
        sessionRevision++;
        profileRequest++;
        invalidateDraft(true);
        showStatus(R.string.ime_ready);
        super.onFinishInput();
    }

    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart,
            int newSelEnd, int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        boolean moved = selectionStart != newSelStart || selectionEnd != newSelEnd;
        selectionStart = newSelStart;
        selectionEnd = newSelEnd;
        if (moved) {
            boolean hadUndo = automaticInsertion != null;
            if (committingEditor) {
                // An editor may transform the inserted character or place its cursor elsewhere.
                // The accepted stroke is still consumed, but cannot be safely undone.
                automaticInsertion = null;
                cancelRecognition();
                updateHandwritingControls();
                return;
            }
            invalidateDraft(false);
            if (hadUndo) showStatus(R.string.ime_auto_undo_unavailable);
        }
    }

    @Override public void onDestroy() {
        destroyed = true;
        editing = false;
        editor = null;
        sessionRevision++;
        invalidateDraft(true);
        if (autoPreferences != null) {
            autoPreferences.unregisterOnSharedPreferenceChangeListener(autoSettingsListener);
        }
        main.removeCallbacksAndMessages(null);
        // The helper belongs to the worker queue; closing it earlier races outstanding reads/saves.
        if (store != null) worker.execute(store::close);
        worker.shutdown();
        super.onDestroy();
    }

    private void updateLocale() {
        String device = getResources().getConfiguration().getLocales().get(0).getLanguage();
        String fallback = ("he".equals(device) || "iw".equals(device)) ? "he" : "en";
        Locale locale = Locale.forLanguageTag(
                "he".equals(appPreferences.getString("language", fallback)) ? "he" : "en");
        Configuration configuration = new Configuration(getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        ui = new ContextThemeWrapper(createConfigurationContext(configuration),
                android.R.style.Theme_Material_Light_NoActionBar);
    }

    private int keyboardHeight() {
        int screen = getResources().getDisplayMetrics().heightPixels;
        int configured = getResources().getConfiguration().screenHeightDp;
        if (configured > 0) screen = Math.min(screen, dp(configured));
        int wanted = handwriting ? Math.min(dp(460), Math.max(dp(380), Math.round(screen * 0.66f)))
                : Math.min(dp(310), Math.max(dp(280), Math.round(screen * 0.54f)));
        return Math.min(wanted, Math.max(dp(120), screen - dp(80)));
    }

    private void buildContent() {
        invalidateDraft(true);
        root.removeAllViews();
        drawing = null;
        candidates = null;
        selected = null;
        learn = null;
        confirm = null;
        recognize = null;
        manualPanel = null;
        manualScroller = null;
        profileSpinner = null;
        alphabetSpinner = null;
        autoSpinner = null;
        undoAutomatic = null;
        keys = null;
        root.setLayoutDirection(ui.getResources().getConfiguration().getLayoutDirection());
        int width = Math.min(getResources().getDisplayMetrics().widthPixels, dp(720));
        content = column();
        content.setPadding(dp(3), dp(3), dp(3), dp(3));
        boolean cramped = keyboardHeight() < dp(300)
                || ui.getResources().getConfiguration().fontScale > 1.3f;
        if (cramped) {
            ScrollView scroll = new ScrollView(ui);
            scroll.setId(R.id.ime_scroll);
            scroll.setSaveEnabled(false);
            scroll.addView(content, new ScrollView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            root.addView(scroll, new FrameLayout.LayoutParams(width,
                    ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL));
        } else {
            root.addView(content, new FrameLayout.LayoutParams(width,
                    ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL));
        }

        LinearLayout toolbar = row(false);
        content.addView(toolbar);
        addKey(toolbar, button(R.id.ime_mode, text(handwriting ? R.string.ime_type
                : R.string.ime_handwrite), view -> {
            invalidateDraft(true);
            handwriting = !handwriting;
            imePreferences.edit().putBoolean("handwriting", handwriting).apply();
            buildContent();
        }), 1.2f);
        if (!handwriting) {
            Button language = button(R.id.ime_typing_language, hebrewTyping ? "עברית" : "EN", view -> {
                if (KeyboardEditor.isNumeric(editor)) return;
                invalidateDraft(true);
                hebrewTyping = !hebrewTyping;
                symbols = false;
                extraSymbols = false;
                shift = false;
                imePreferences.edit().putString("typing-language", hebrewTyping ? "he" : "en").apply();
                buildContent();
            });
            language.setContentDescription(text(R.string.ime_typing_language));
            language.setEnabled(!KeyboardEditor.isNumeric(editor));
            addKey(toolbar, language, 0.8f);
            Button layout = button(R.id.ime_symbols, text(symbols ? R.string.ime_letters
                    : R.string.ime_symbols), view -> {
                if (KeyboardEditor.isNumeric(editor)) return;
                invalidateDraft(true);
                symbols = !symbols;
                extraSymbols = false;
                buildContent();
            });
            layout.setEnabled(!KeyboardEditor.isNumeric(editor));
            addKey(toolbar, layout, 1);
        }
        body = column();
        if (cramped) {
            content.addView(body);
        } else if (handwriting) {
            content.addView(body, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        } else {
            ScrollView scroll = new ScrollView(ui);
            scroll.setId(R.id.ime_scroll);
            scroll.setFillViewport(false);
            scroll.setSaveEnabled(false);
            content.addView(scroll, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            scroll.addView(body, new ScrollView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        if (handwriting) buildHandwriting(toolbar, cramped);
        else buildKeys();
        Button training = button(R.id.ime_training, text(R.string.ime_training), view -> openTraining());
        training.setContentDescription(text(R.string.ime_open_training));
        addKey(toolbar, training, 0.9f);
        buildBottomRow();
        status = new TextView(ui);
        status.setId(R.id.ime_status);
        status.setTextSize(12);
        status.setTextColor(Color.rgb(35, 65, 71));
        status.setPadding(dp(6), dp(2), dp(6), dp(2));
        // Keep the drawing area stable when status messages change between strokes.
        status.setLines(1);
        status.setEllipsize(TextUtils.TruncateAt.END);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        status.setSaveEnabled(false);
        content.addView(status);
        showDefaultStatus();
        updateHandwritingControls();
        root.requestLayout();
    }

    private void buildKeys() {
        keys = column();
        keys.setId(R.id.ime_key_rows);
        body.addView(keys);
        String[] rows;
        if (symbols) rows = extraSymbols
                ? new String[]{"[]{}<>\\|", "_~`^€£¥", ".,?!'\":;/="}
                : new String[]{"1234567890", "@#$%&*-+()", ".,?!'\":;/="};
        else if (hebrewTyping) rows = new String[]{"/'קראטוןםפ", "שדגכעיחלךף", "זסבהנמצתץ."};
        else rows = new String[]{"qwertyuiop", "asdfghjkl", "zxcvbnm"};
        for (int index = 0; index < rows.length; index++) {
            LinearLayout line = row(true);
            keys.addView(line);
            if (symbols && index == 2 && !KeyboardEditor.isNumeric(editor)) {
                Button page = button(R.id.ime_symbol_page, extraSymbols ? "2/2" : "1/2", view -> {
                    extraSymbols = !extraSymbols;
                    invalidateDraft(true);
                    buildContent();
                });
                page.setContentDescription(text(R.string.ime_more_symbols));
                addKey(line, page, 1.3f);
            }
            if (!symbols && !hebrewTyping && index == 2) {
                Button shiftKey = button(R.id.ime_shift, shift ? "⇧ •" : "⇧", view -> {
                    shift = !shift;
                    invalidateDraft(true);
                    buildContent();
                });
                shiftKey.setContentDescription(text(shift ? R.string.ime_shift_on : R.string.ime_shift));
                addKey(line, shiftKey, 1.3f);
            }
            String characters = shift && !hebrewTyping ? rows[index].toUpperCase(Locale.ROOT) : rows[index];
            for (int i = 0; i < characters.length(); i++) {
                String character = characters.substring(i, i + 1);
                Button key = button(View.NO_ID, character, view -> commitTyped(character));
                key.setTag(character);
                key.setTextDirection(View.TEXT_DIRECTION_LTR);
                key.setTextSize(20);
                addKey(line, key, 1);
            }
            if (!symbols && !hebrewTyping && index == 2) {
                addKey(line, button(View.NO_ID, ".", view -> commitTyped(".")), 1);
            }
        }
    }

    private void buildHandwriting(LinearLayout toolbar, boolean cramped) {
        profileSpinner = new Spinner(ui, Spinner.MODE_DROPDOWN);
        profileSpinner.setId(R.id.ime_profile);
        profileSpinner.setContentDescription(text(R.string.ime_profile));
        addKey(toolbar, profileSpinner, 1.2f);
        populateProfiles();
        alphabetSpinner = new Spinner(ui, Spinner.MODE_DROPDOWN);
        alphabetSpinner.setId(R.id.ime_alphabet);
        alphabetSpinner.setContentDescription(text(R.string.ime_alphabet));
        List<String> names = new ArrayList<>();
        for (int resource : GROUP_NAMES) names.add(text(resource));
        alphabetSpinner.setAdapter(adapter(names));
        for (int i = 0; i < GROUPS.length; i++) if (GROUPS[i].equals(group)) alphabetSpinner.setSelection(i);
        alphabetSpinner.setEnabled(!KeyboardEditor.isNumeric(editor));
        alphabetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent != alphabetSpinner || position < 0 || position >= GROUPS.length) return;
                if (KeyboardEditor.isNumeric(editor)) {
                    if (position != 0) alphabetSpinner.setSelection(0);
                    return;
                }
                if (group.equals(GROUPS[position])) return;
                group = GROUPS[position];
                imePreferences.edit().putString("alphabet", group).apply();
                invalidateDraft(true);
                buildManualPanel();
                showDefaultStatus();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        addKey(toolbar, alphabetSpinner, 1.2f);

        drawing = new DrawingView(ui);
        drawing.setId(R.id.ime_drawing);
        drawing.setMinimumHeight(0);
        drawing.setContentDescription(text(R.string.ime_drawing_description));
        body.addView(drawing, cramped
                ? new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(142))
                : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        drawing.setOnInkChangedListener(() -> {
            if (changingDrawing) return;
            invalidateDraft(false);
            showDefaultStatus();
        });
        drawing.setOnLimitReachedListener(() -> {
            invalidateDraft(false);
            showStatus(R.string.ime_ink_limit);
        });
        drawing.setOnDrawingBlockedListener(() -> showStatus(
                loadingProfiles ? R.string.ime_loading_profiles : R.string.ime_input_failed));
        drawing.setOnStrokeFinishedListener(this::armAutomaticInsertion);
        LinearLayout controls = row(false);
        content.addView(controls);
        addKey(controls, button(R.id.ime_undo, text(R.string.ime_undo), view -> drawing.undoStroke()), 1);
        addKey(controls, button(R.id.ime_clear, text(R.string.ime_clear), view -> {
            invalidateDraft(true);
            showDefaultStatus();
        }), 1);
        recognize = button(R.id.ime_recognize, text(R.string.ime_recognize), view -> recognize());
        addKey(controls, recognize, 1.5f);
        addKey(controls, button(R.id.ime_manual, text(R.string.ime_manual), view -> {
            boolean open = !manualOpen;
            invalidateDraft(false);
            manualOpen = open;
            manualScroller.setVisibility(open ? View.VISIBLE : View.GONE);
            drawing.setVisibility(open ? View.GONE : View.VISIBLE);
            showStatus(open ? R.string.ime_manual_hint : R.string.ime_draw_hint);
        }), 1);
        manualPanel = column();
        manualPanel.setId(R.id.ime_manual_panel);
        manualScroller = new ScrollView(ui);
        manualScroller.setSaveEnabled(false);
        manualScroller.addView(manualPanel);
        body.addView(manualScroller, cramped
                ? new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(142))
                : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        buildManualPanel();
        LinearLayout predictionRow = row(false);
        autoSpinner = new Spinner(ui, Spinner.MODE_DROPDOWN);
        autoSpinner.setId(R.id.ime_auto_delay);
        autoSpinner.setContentDescription(text(R.string.auto_insert_title));
        autoSpinner.setAdapter(adapter(Arrays.asList(
                ui.getResources().getStringArray(R.array.auto_insert_delays_short))));
        autoSpinner.setSelection(AutoInsertSettings.selection(this));
        autoSpinner.setSaveEnabled(false);
        autoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != parent.getSelectedItemPosition()) return;
                if (parent != autoSpinner || position == AutoInsertSettings.selection(HandwritingImeService.this)) return;
                AutoInsertSettings.select(HandwritingImeService.this, position);
                refreshAutoSettings();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        addKey(predictionRow, autoSpinner, 1.2f);
        candidates = row(true);
        candidates.setId(R.id.ime_candidates);
        addKey(predictionRow, candidates, 3);
        undoAutomatic = button(R.id.ime_auto_undo, text(R.string.ime_auto_undo),
                view -> undoAutomaticInsertion());
        undoAutomatic.setContentDescription(text(R.string.auto_insert_undo));
        addKey(predictionRow, undoAutomatic, 3);
        undoAutomatic.setVisibility(View.GONE);
        content.addView(predictionRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        LinearLayout confirmation = row(false);
        selected = new TextView(ui);
        selected.setId(R.id.ime_selected);
        selected.setSaveEnabled(false);
        selected.setTextSize(14);
        selected.setGravity(Gravity.CENTER_VERTICAL);
        selected.setPadding(dp(5), 0, dp(5), 0);
        addKey(confirmation, selected, 1.1f);
        learn = new CheckBox(ui);
        learn.setId(R.id.ime_learn);
        learn.setSaveEnabled(false);
        learn.setTextSize(12);
        learn.setMaxLines(2);
        learn.setMinHeight(dp(48));
        learn.setContentDescription(text(R.string.ime_learning_description));
        addKey(confirmation, learn, 1.9f);
        confirm = button(R.id.ime_confirm, text(R.string.ime_confirm), view -> confirmCharacter());
        addKey(confirmation, confirm, 1);
        content.addView(confirmation);
    }

    private void buildManualPanel() {
        if (manualPanel == null) return;
        manualPanel.removeAllViews();
        List<String> labels = Alphabet.labels(group);
        LinearLayout line = null;
        for (int i = 0; i < labels.size(); i++) {
            if (i % 10 == 0) {
                line = row(true);
                manualPanel.addView(line);
            }
            String label = labels.get(i);
            Button key = button(View.NO_ID, label, view -> {
                if (!editing || drawing.isDrawing()) {
                    showStatus(editing ? R.string.ime_draw_first : R.string.ime_input_failed);
                    return;
                }
                select(label, new Draft());
                manualOpen = false;
                manualScroller.setVisibility(View.GONE);
                drawing.setVisibility(View.VISIBLE);
            });
            key.setTag(label);
            key.setTextSize(20);
            addKey(line, key, 1);
        }
        manualScroller.setVisibility(View.GONE);
    }

    private void buildBottomRow() {
        LinearLayout bottom = row(true);
        content.addView(bottom);
        Button picker = button(R.id.ime_picker, text(R.string.ime_picker), view -> {
            invalidateDraft(true);
            showDefaultStatus();
            InputMethodManager manager = getSystemService(InputMethodManager.class);
            if (manager == null) {
                showStatus(R.string.ime_picker_error);
                return;
            }
            try {
                manager.showInputMethodPicker();
            } catch (IllegalStateException | SecurityException exception) {
                showStatus(R.string.ime_picker_error);
            }
        });
        picker.setContentDescription(text(R.string.ime_picker_description));
        addKey(bottom, picker, 1);
        addKey(bottom, button(R.id.ime_space, text(R.string.ime_space), view -> commitTyped(" ")), 2);
        Button backspace = button(R.id.ime_backspace, "⌫", view -> {
            invalidateDraft(true);
            boolean accepted = editing && KeyboardEditor.backspace(editorConnection());
            selectionStart = selectionEnd = -1;
            showStatus(accepted ? R.string.ime_edited : R.string.ime_input_failed);
        });
        backspace.setContentDescription(text(R.string.ime_backspace));
        addKey(bottom, backspace, 1);
        addKey(bottom, button(R.id.ime_enter, actionLabel(), view -> {
            invalidateDraft(true);
            boolean accepted = editing && KeyboardEditor.enter(editorConnection(), editor);
            selectionStart = selectionEnd = -1;
            showStatus(accepted ? R.string.ime_edited : R.string.ime_input_failed);
        }), 1.4f);
    }

    private CharSequence actionLabel() {
        KeyboardEditor.Action action = KeyboardEditor.action(editor);
        if (action.newline) return text(R.string.ime_enter);
        if (action.customLabel != null) return action.customLabel;
        switch (action.id) {
            case EditorInfo.IME_ACTION_DONE: return text(R.string.ime_done);
            case EditorInfo.IME_ACTION_NEXT: return text(R.string.ime_next);
            case EditorInfo.IME_ACTION_PREVIOUS: return text(R.string.ime_previous);
            case EditorInfo.IME_ACTION_SEARCH: return text(R.string.ime_search);
            case EditorInfo.IME_ACTION_SEND: return text(R.string.ime_send);
            case EditorInfo.IME_ACTION_GO: return text(R.string.ime_go);
            default: return text(R.string.ime_action);
        }
    }

    private void commitTyped(String character) {
        invalidateDraft(true);
        boolean accepted = editing && commitCharacter(character);
        if (accepted && shift && !symbols && !hebrewTyping) {
            shift = false;
            buildContent();
        }
        showStatus(accepted ? R.string.ime_inserted : R.string.ime_input_failed);
    }

    private boolean commitCharacter(String label) {
        int oldStart = selectionStart;
        int oldEnd = selectionEnd;
        int end = oldStart < 0 || oldEnd < 0 ? -1 : Math.min(oldStart, oldEnd) + label.length();
        // Android may report this selection synchronously from commitText or on a later frame.
        selectionStart = selectionEnd = end;
        committingEditor = true;
        try {
            if (KeyboardEditor.commit(editorConnection(), label)) return true;
        } finally {
            committingEditor = false;
        }
        selectionStart = oldStart;
        selectionEnd = oldEnd;
        return false;
    }

    private void refreshAutoSettings() {
        autoDelay = AutoInsertSettings.delay(this);
        invalidateDraft(false);
        if (autoSpinner != null) autoSpinner.setSelection(AutoInsertSettings.selection(this));
        showStatus(R.string.auto_insert_settings_changed);
    }

    private void armAutomaticInsertion() {
        if (!editing || !handwriting || loadingProfiles || profilesFailed || manualOpen
                || drawing == null || drawing.isDrawing() || drawing.getInk().isEmpty()
                || autoDelay == 0) return;
        Draft pending = new Draft();
        autoInsert.arm(autoDelay, () -> {
            if (automaticCurrent(pending)) recognize(true);
        });
        showStatus(R.string.auto_insert_waiting);
    }

    private boolean automaticCurrent(Draft draft) {
        return current(draft) && !loadingProfiles && !profilesFailed && !manualOpen
                && drawing != null && !drawing.isDrawing() && !drawing.getInk().isEmpty()
                && draft.delay > 0 && draft.delay == autoDelay
                && draft.delay == AutoInsertSettings.delay(this);
    }

    private List<HandwritingRecognizer.Example> personalExamples(Draft request) {
        if (request.profile < 0) return Collections.emptyList();
        List<HandwritingRecognizer.Example> examples = store.examples(request.profile, request.alphabet);
        boolean exists = false;
        for (ProfileStore.Profile profile : store.profiles()) {
            if (profile.id == request.profile) {
                exists = true;
                break;
            }
        }
        if (!exists) throw new IllegalStateException("The selected profile is no longer available");
        return examples;
    }

    private void insertAutomatically(String label, Ink ink, Draft request, boolean uncertain) {
        if (!automaticCurrent(request) || !Alphabet.labels(group).contains(label)) return;
        boolean privateField = !KeyboardEditor.allowsLearning(editor);
        AutomaticInsertion insertion = !privateField && selectionStart >= 0
                && selectionStart == selectionEnd
                ? new AutomaticInsertion(label, ink, request, selectionEnd + label.length()) : null;
        automaticInsertion = insertion;
        // Automatic insertion never calls the training path, even with a forced checked checkbox.
        if (!commitCharacter(label)) {
            automaticInsertion = null;
            updateHandwritingControls();
            showStatus(R.string.ime_input_failed);
            return;
        }
        boolean retainUndo = insertion != null && automaticInsertion == insertion
                && insertion.contextMatches();
        if (!current(request)) {
            automaticInsertion = null;
            updateHandwritingControls();
            return;
        }
        invalidateDraft(true);
        if (retainUndo) automaticInsertion = insertion;
        updateHandwritingControls();
        showStatus(privateField ? uncertain ? R.string.ime_auto_private_uncertain : R.string.ime_auto_private
                : uncertain ? R.string.auto_insert_uncertain
                : retainUndo ? R.string.auto_insert_done : R.string.ime_auto_without_undo);
    }

    private final class AutomaticInsertion {
        final String label;
        final Ink ink;
        final Draft draft;
        final int expectedEnd;

        AutomaticInsertion(String label, Ink ink, Draft draft, int expectedEnd) {
            this.label = label;
            this.ink = ink;
            this.draft = draft;
            this.expectedEnd = expectedEnd;
        }

        boolean contextMatches() {
            return !destroyed && editing && handwriting && KeyboardEditor.allowsLearning(editor)
                    && draft.session == sessionRevision && draft.profile == profileId
                    && draft.alphabet.equals(group) && draft.connection == editorConnection()
                    && selectionStart == expectedEnd && selectionEnd == expectedEnd;
        }
    }

    private void undoAutomaticInsertion() {
        AutomaticInsertion insertion = automaticInsertion;
        cancelRecognition();
        if (insertion == null || !insertion.contextMatches()
                || !KeyboardEditor.undoAutomaticInsertion(editorConnection(), insertion.label,
                        insertion.expectedEnd, selectionStart, selectionEnd)) {
            automaticInsertion = null;
            updateHandwritingControls();
            showStatus(R.string.ime_auto_undo_unavailable);
            return;
        }
        selectionStart = selectionEnd = insertion.expectedEnd - insertion.label.length();
        invalidateDraft(true);
        changingDrawing = true;
        try {
            drawing.setInk(insertion.ink);
        } finally {
            changingDrawing = false;
        }
        updateHandwritingControls();
        showStatus(R.string.auto_insert_undone);
    }

    private void recognize() {
        recognize(false);
    }

    private void recognize(boolean automatic) {
        if (!editing || drawing == null || drawing.isDrawing() || drawing.getInk().isEmpty()) {
            showStatus(R.string.ime_draw_first);
            return;
        }
        if (loadingProfiles || profilesFailed) {
            showStatus(loadingProfiles ? R.string.ime_loading_profiles : R.string.ime_profile_error);
            return;
        }
        invalidateDraft(false);
        Draft request = new Draft();
        Ink ink = drawing.getInk();
        recognizing = true;
        updateHandwritingControls();
        showStatus(R.string.ime_recognizing);
        recognitionTask = worker.submit(() -> {
            try {
                HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(
                        ink, request.alphabet, personalExamples(request));
                main.post(() -> {
                    if (!current(request) || (automatic && !automaticCurrent(request))) return;
                    recognizing = false;
                    recognitionTask = null;
                    if (automatic && !result.candidates.isEmpty()) {
                        insertAutomatically(result.candidates.get(0).label, ink, request, result.uncertain);
                        return;
                    }
                    for (HandwritingRecognizer.Candidate candidate : result.candidates) {
                        Button choice = button(View.NO_ID, candidate.label + " · " + candidate.similarity,
                                view -> select(candidate.label, request));
                        choice.setTag(candidate.label);
                        choice.setContentDescription(ui.getString(R.string.ime_candidate_description,
                                candidate.label, candidate.similarity));
                        addKey(candidates, choice, 1);
                    }
                    showStatus(result.candidates.isEmpty() ? R.string.ime_need_training
                            : result.uncertain ? R.string.ime_uncertain : R.string.ime_similarity);
                    updateHandwritingControls();
                });
            } catch (CancellationException exception) {
                // Draft changes deliberately cancel comparisons, without publishing stale suggestions.
            } catch (SQLiteException | IllegalArgumentException | IllegalStateException exception) {
                main.post(() -> {
                    if (!current(request)) return;
                    recognizing = false;
                    recognitionTask = null;
                    updateHandwritingControls();
                    showStatus(R.string.ime_recognition_error);
                });
            }
        });
    }

    private void select(String label, Draft request) {
        if (!current(request) || !Alphabet.labels(group).contains(label) || drawing.isDrawing()) return;
        invalidateDraft(false);
        selectedLabel = label;
        selectedDraft = new Draft();
        learn.setChecked(false);
        updateHandwritingControls();
        showStatus(R.string.ime_manual_hint);
    }

    private void confirmCharacter() {
        cancelRecognition();
        boolean consent = learn != null && learn.isChecked();
        if (learn != null) learn.setChecked(false);
        if (selectedLabel == null || selectedDraft == null || !current(selectedDraft)
                || drawing.isDrawing() || !Alphabet.labels(group).contains(selectedLabel)) {
            showStatus(R.string.ime_nothing_selected);
            return;
        }
        String label = selectedLabel;
        Ink ink = drawing.getInk();
        long confirmedProfile = profileId;
        String confirmedGroup = group;
        // Never trust enabled/checked UI state as the privacy gate.
        boolean save = consent && KeyboardEditor.allowsLearning(editor)
                && confirmedProfile >= 0 && !loadingProfiles && !ink.isEmpty();
        if (!commitCharacter(label)) {
            showStatus(R.string.ime_input_failed);
            return;
        }
        invalidateDraft(true);
        showStatus(R.string.ime_inserted);
        if (!save) return;
        Draft afterCommit = new Draft();
        worker.execute(() -> {
            try {
                store.addExample(confirmedProfile, confirmedGroup, label, ink);
                main.post(() -> {
                    if (current(afterCommit)) showStatus(R.string.ime_saved);
                });
            } catch (SQLiteException | IllegalArgumentException | IllegalStateException exception) {
                main.post(() -> {
                    if (current(afterCommit)) showStatus(R.string.ime_save_error);
                });
            }
        });
    }

    private void loadProfiles() {
        if (destroyed) return;
        long request = ++profileRequest;
        long session = sessionRevision;
        long preferred = appPreferences.getLong("profile", -1);
        loadingProfiles = true;
        profilesFailed = false;
        profileId = -1;
        profiles = new ArrayList<>();
        populateProfiles();
        updateHandwritingControls();
        worker.execute(() -> {
            try {
                List<ProfileStore.Profile> loaded = store.profiles();
                main.post(() -> {
                    if (destroyed || request != profileRequest || session != sessionRevision || !editing) return;
                    loadingProfiles = false;
                    profiles = loaded;
                    profileId = loaded.isEmpty() ? -1 : loaded.get(0).id;
                    for (ProfileStore.Profile profile : loaded) if (profile.id == preferred) profileId = preferred;
                    invalidateDraft(true);
                    populateProfiles();
                    showDefaultStatus();
                });
            } catch (SQLiteException | IllegalArgumentException | IllegalStateException exception) {
                main.post(() -> {
                    if (destroyed || request != profileRequest || session != sessionRevision || !editing) return;
                    loadingProfiles = false;
                    profilesFailed = true;
                    profileId = -1;
                    profiles = new ArrayList<>();
                    populateProfiles();
                    updateHandwritingControls();
                    showStatus(R.string.ime_profile_error);
                });
            }
        });
    }

    private void populateProfiles() {
        if (profileSpinner == null) return;
        List<String> names = new ArrayList<>();
        int position = 0;
        for (int i = 0; i < profiles.size(); i++) {
            names.add(profiles.get(i).name);
            if (profiles.get(i).id == profileId) position = i;
        }
        if (names.isEmpty()) names.add(text(loadingProfiles ? R.string.ime_loading_profiles
                : profilesFailed ? R.string.ime_no_profile : R.string.starter_examples_profile));
        profileSpinner.setOnItemSelectedListener(null);
        profileSpinner.setAdapter(adapter(names));
        profileSpinner.setSelection(position);
        profileSpinner.setEnabled(!loadingProfiles && !profiles.isEmpty());
        profileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int selectedPosition, long id) {
                if (parent != profileSpinner || loadingProfiles || selectedPosition < 0
                        || selectedPosition >= profiles.size()) return;
                long selectedProfile = profiles.get(selectedPosition).id;
                if (selectedProfile == profileId) return;
                profileId = selectedProfile;
                appPreferences.edit().putLong("profile", profileId).apply();
                invalidateDraft(true);
                showDefaultStatus();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void invalidateDraft(boolean eraseInk) {
        drawingRevision++;
        cancelRecognition();
        automaticInsertion = null;
        selectedLabel = null;
        selectedDraft = null;
        manualOpen = false;
        if (candidates != null) candidates.removeAllViews();
        if (manualScroller != null) manualScroller.setVisibility(View.GONE);
        if (learn != null) learn.setChecked(false);
        if (drawing != null) {
            drawing.setVisibility(View.VISIBLE);
            if (eraseInk) {
                changingDrawing = true;
                try {
                    drawing.clear();
                } finally {
                    changingDrawing = false;
                }
            }
        }
        updateHandwritingControls();
    }

    private void cancelRecognition() {
        autoInsert.cancel();
        if (recognitionTask != null) recognitionTask.cancel(true);
        recognitionTask = null;
        recognizing = false;
    }

    private void updateHandwritingControls() {
        boolean completeInk = drawing != null && !drawing.isDrawing() && !drawing.getInk().isEmpty();
        if (drawing != null) {
            drawing.setDisabledHint(loadingProfiles ? R.string.drawing_loading_hint : R.string.drawing_editor_hint);
            drawing.setEnabled(editing && !loadingProfiles);
        }
        if (recognize != null) recognize.setEnabled(editing && !recognizing && !loadingProfiles
                && !profilesFailed && completeInk);
        if (undoAutomatic != null) {
            undoAutomatic.setVisibility(automaticInsertion == null ? View.GONE : View.VISIBLE);
            candidates.setVisibility(automaticInsertion == null ? View.VISIBLE : View.GONE);
        }
        if (confirm != null) confirm.setEnabled(editing && selectedLabel != null
                && selectedDraft != null && current(selectedDraft) && !drawing.isDrawing());
        if (selected != null) selected.setText(selectedLabel == null ? text(R.string.ime_nothing_selected)
                : ui.getString(R.string.ime_selected, selectedLabel));
        if (learn != null) {
            boolean policy = editing && KeyboardEditor.allowsLearning(editor);
            learn.setText(text(policy ? R.string.ime_learn : R.string.ime_learning_private));
            learn.setEnabled(policy && profileId >= 0 && !loadingProfiles && completeInk && selectedLabel != null);
            if (!learn.isEnabled()) learn.setChecked(false);
        }
    }

    private final class Draft {
        final long session = sessionRevision;
        final long revision = drawingRevision;
        final long profile = profileId;
        final String alphabet = group;
        final int delay = autoDelay;
        final InputConnection connection = editorConnection();
    }

    private boolean current(Draft draft) {
        return !destroyed && editing && handwriting && draft.session == sessionRevision
                && draft.revision == drawingRevision && draft.profile == profileId && draft.alphabet.equals(group)
                && draft.connection == editorConnection();
    }

    private void openTraining() {
        String trainingAlphabet = handwriting ? group : KeyboardEditor.isNumeric(editor)
                ? Alphabet.DIGITS : hebrewTyping ? Alphabet.HEBREW
                : shift ? Alphabet.ENGLISH_UPPER : Alphabet.ENGLISH_LOWER;
        invalidateDraft(true);
        showDefaultStatus();
        try {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(MainActivity.EXTRA_TRAINING_ALPHABET, trainingAlphabet));
            requestHideSelf(0);
        } catch (ActivityNotFoundException | SecurityException exception) {
            showStatus(R.string.ime_training_error);
        }
    }

    private void showDefaultStatus() {
        showStatus(!handwriting ? R.string.ime_ready : loadingProfiles ? R.string.ime_loading_profiles
                : profilesFailed ? R.string.ime_profile_error
                : profileId < 0 ? R.string.ime_no_profiles
                : autoDelay > 0 ? R.string.ime_auto_draw_hint : R.string.ime_draw_hint);
    }

    private void showStatus(int resource) {
        if (status != null) status.setText(text(resource));
    }

    private String text(int resource) {
        return ui.getString(resource);
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(ui);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setSaveEnabled(false);
        return layout;
    }

    private LinearLayout row(boolean leftToRight) {
        LinearLayout layout = new LinearLayout(ui);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setSaveEnabled(false);
        if (leftToRight) layout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        return layout;
    }

    private Button button(int id, CharSequence label, View.OnClickListener listener) {
        Button button = new Button(ui);
        if (id != View.NO_ID) button.setId(id);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(dp(48));
        button.setMinimumHeight(dp(48));
        button.setPadding(dp(2), dp(2), dp(2), dp(2));
        button.setSingleLine(true);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setSaveEnabled(false);
        button.setOnClickListener(listener);
        return button;
    }

    private void addKey(LinearLayout row, View view, float weight) {
        row.addView(view, new LinearLayout.LayoutParams(0, dp(48), weight));
    }

    private ArrayAdapter<String> adapter(List<String> labels) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ui, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
