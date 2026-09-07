package com.jellybolt.handwriting;

import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class KeyboardEditorTest {
    @Test public void commitsEnglishHebrewAndDigitsInLogicalOrder() {
        Connection connection = new Connection("");
        assertTrue(KeyboardEditor.commit(connection, "A"));
        assertTrue(KeyboardEditor.commit(connection, "\u05de"));
        assertTrue(KeyboardEditor.commit(connection, "\u05dd"));
        assertTrue(KeyboardEditor.commit(connection, "3"));
        assertEquals("A\u05de\u05dd3", connection.text.toString());
    }

    @Test public void commitsAtCursorAndReplacesSelection() {
        Connection connection = new Connection("abcd");
        Selection.setSelection(connection.text, 1, 3);
        assertTrue(KeyboardEditor.commit(connection, "X"));
        assertEquals("aXd", connection.text.toString());
        assertEquals(2, Selection.getSelectionStart(connection.text));
    }

    @Test public void failedOrMissingConnectionsDoNotPretendToCommit() {
        Connection connection = new Connection("unchanged");
        connection.allowCommit = false;
        assertFalse(KeyboardEditor.commit(connection, "X"));
        assertFalse(KeyboardEditor.commit(null, "X"));
        assertFalse(KeyboardEditor.commit(connection, ""));
        assertFalse(KeyboardEditor.commit(connection, null));
        assertEquals("unchanged", connection.text.toString());
    }

    @Test public void backspaceDeletesTheSelection() {
        Connection connection = new Connection("abcd");
        Selection.setSelection(connection.text, 1, 3);
        assertTrue(KeyboardEditor.backspace(connection));
        assertEquals("ad", connection.text.toString());
    }

    @Test public void backspaceDeletesWholeSurrogatePair() {
        Connection connection = new Connection("A\uD83D\uDE00");
        assertTrue(KeyboardEditor.backspace(connection));
        assertEquals("A", connection.text.toString());
    }

    @Test public void legacyEditorFallbackAlsoPreservesSurrogatePairs() {
        Connection connection = new Connection("A\uD83D\uDE00");
        connection.supportsCodePointDeletion = false;
        assertTrue(KeyboardEditor.backspace(connection));
        assertEquals("A", connection.text.toString());
        assertEquals(2, connection.lastLegacyDeletion);
    }

    @Test public void deletionReportsUnavailableEditorInsteadOfGuessing() {
        Connection connection = new Connection("A\uD83D\uDE00");
        connection.supportsCodePointDeletion = false;
        connection.allowReadingBeforeCursor = false;
        assertFalse(KeyboardEditor.backspace(connection));
        assertFalse(KeyboardEditor.backspace(null));
        assertEquals("A\uD83D\uDE00", connection.text.toString());
    }

    @Test public void backspaceAtBeginningDoesNotDeleteFollowingText() {
        Connection connection = new Connection("abc");
        Selection.setSelection(connection.text, 0);
        assertTrue(KeyboardEditor.backspace(connection));
        assertEquals("abc", connection.text.toString());
    }

    @Test public void allPasswordVariationsDisableLearning() {
        int[] variations = {
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        };
        for (int variation : variations) {
            assertFalse(KeyboardEditor.allowsLearning(info(InputType.TYPE_CLASS_TEXT | variation)));
        }
        assertFalse(KeyboardEditor.allowsLearning(info(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD)));
    }

    @Test public void privateInputFlagAlwaysDisablesLearning() {
        for (int inputType : new int[]{InputType.TYPE_CLASS_TEXT, InputType.TYPE_CLASS_NUMBER,
                InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_DATETIME}) {
            EditorInfo info = info(inputType);
            info.imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING | EditorInfo.IME_ACTION_NEXT;
            assertFalse(KeyboardEditor.allowsLearning(info));
        }
    }

    @Test public void absentAndUnknownEditorTypesCannotLearn() {
        assertFalse(KeyboardEditor.allowsLearning(null));
        assertFalse(KeyboardEditor.allowsLearning(info(InputType.TYPE_NULL)));
        assertFalse(KeyboardEditor.allowsLearning(info(15)));
        assertFalse(KeyboardEditor.allowsLearning(info(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)));
    }

    @Test public void normalEditorsAllowExplicitSampleLearning() {
        assertTrue(KeyboardEditor.allowsLearning(info(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)));
        assertTrue(KeyboardEditor.allowsLearning(info(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED)));
        assertTrue(KeyboardEditor.allowsLearning(info(InputType.TYPE_CLASS_PHONE)));
        assertTrue(KeyboardEditor.allowsLearning(info(InputType.TYPE_CLASS_DATETIME)));
    }

    @Test public void numericPhoneAndDateFieldsSelectNumberInput() {
        assertTrue(KeyboardEditor.isNumeric(info(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL)));
        assertTrue(KeyboardEditor.isNumeric(info(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_VARIATION_PASSWORD)));
        assertTrue(KeyboardEditor.isNumeric(info(InputType.TYPE_CLASS_PHONE)));
        assertTrue(KeyboardEditor.isNumeric(info(InputType.TYPE_CLASS_DATETIME)));
        assertFalse(KeyboardEditor.isNumeric(info(InputType.TYPE_CLASS_TEXT)));
        assertFalse(KeyboardEditor.isNumeric(null));
    }

    @Test public void standardEditorActionsArePerformedRatherThanInserted() {
        for (int action : new int[]{EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_ACTION_SEND, EditorInfo.IME_ACTION_NEXT,
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_PREVIOUS}) {
            Connection connection = new Connection("text");
            EditorInfo info = info(InputType.TYPE_CLASS_TEXT);
            info.imeOptions = action | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
            assertTrue(KeyboardEditor.enter(connection, info));
            assertEquals(action, connection.lastAction);
            assertEquals("text", connection.text.toString());
        }
    }

    @Test public void customActionUsesItsProvidedIdAndLabel() {
        Connection connection = new Connection("text");
        EditorInfo info = info(InputType.TYPE_CLASS_TEXT);
        info.actionId = 42;
        info.actionLabel = "Submit response";
        KeyboardEditor.Action action = KeyboardEditor.action(info);
        assertFalse(action.newline);
        assertEquals("Submit response", action.customLabel);
        assertTrue(KeyboardEditor.enter(connection, info));
        assertEquals(42, connection.lastAction);
    }

    @Test public void noEnterActionFlagTakesPrecedenceOverSendAndCustomActions() {
        Connection connection = new Connection("text");
        EditorInfo info = info(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        info.imeOptions = EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        info.actionId = 42;
        info.actionLabel = "Submit response";
        assertTrue(KeyboardEditor.action(info).newline);
        assertTrue(KeyboardEditor.enter(connection, info));
        assertEquals("text\n", connection.text.toString());
        assertEquals(-1, connection.lastAction);
    }

    @Test public void unspecifiedActionInsertsNewline() {
        Connection connection = new Connection("text");
        assertTrue(KeyboardEditor.enter(connection, info(InputType.TYPE_CLASS_TEXT)));
        assertEquals("text\n", connection.text.toString());
    }

    @Test public void failedActionDoesNotInsertAnUnexpectedNewline() {
        Connection connection = new Connection("text");
        connection.allowAction = false;
        EditorInfo info = info(InputType.TYPE_CLASS_TEXT);
        info.imeOptions = EditorInfo.IME_ACTION_SEND;
        assertFalse(KeyboardEditor.enter(connection, info));
        assertEquals("text", connection.text.toString());
        assertFalse(KeyboardEditor.enter(null, info));
        assertFalse(KeyboardEditor.enter(connection, null));
    }

    private static EditorInfo info(int type) {
        EditorInfo info = new EditorInfo();
        info.inputType = type;
        return info;
    }

    private static final class Connection extends BaseInputConnection {
        final Editable text;
        boolean allowCommit = true;
        boolean allowAction = true;
        boolean supportsCodePointDeletion = true;
        boolean allowReadingBeforeCursor = true;
        int lastAction = -1;
        int lastLegacyDeletion = -1;

        Connection(String initial) {
            super(new View(RuntimeEnvironment.getApplication()), true);
            text = new SpannableStringBuilder(initial);
            Selection.setSelection(text, text.length());
        }

        @Override public Editable getEditable() { return text; }

        @Override public boolean commitText(CharSequence value, int cursor) {
            return allowCommit && super.commitText(value, cursor);
        }

        @Override public boolean deleteSurroundingTextInCodePoints(int before, int after) {
            return supportsCodePointDeletion && super.deleteSurroundingTextInCodePoints(before, after);
        }

        @Override public boolean deleteSurroundingText(int before, int after) {
            lastLegacyDeletion = before;
            return super.deleteSurroundingText(before, after);
        }

        @Override public CharSequence getTextBeforeCursor(int length, int flags) {
            return allowReadingBeforeCursor ? super.getTextBeforeCursor(length, flags) : null;
        }

        @Override public boolean performEditorAction(int action) {
            lastAction = action;
            return allowAction;
        }
    }
}
