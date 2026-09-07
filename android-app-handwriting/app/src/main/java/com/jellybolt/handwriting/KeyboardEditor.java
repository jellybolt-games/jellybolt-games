package com.jellybolt.handwriting;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public final class KeyboardEditor {
    private KeyboardEditor() {}

    public static boolean allowsLearning(EditorInfo info) {
        if (info == null || info.inputType == InputType.TYPE_NULL
                || (info.imeOptions & EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0) {
            return false;
        }
        int inputClass = info.inputType & InputType.TYPE_MASK_CLASS;
        int variation = info.inputType & InputType.TYPE_MASK_VARIATION;
        if (inputClass == InputType.TYPE_CLASS_TEXT) {
            return variation != InputType.TYPE_TEXT_VARIATION_PASSWORD
                    && variation != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    && variation != InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
        }
        if (inputClass == InputType.TYPE_CLASS_NUMBER) {
            return variation != InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        }
        return inputClass == InputType.TYPE_CLASS_PHONE || inputClass == InputType.TYPE_CLASS_DATETIME;
    }

    public static boolean isNumeric(EditorInfo info) {
        if (info == null) return false;
        int inputClass = info.inputType & InputType.TYPE_MASK_CLASS;
        return inputClass == InputType.TYPE_CLASS_NUMBER
                || inputClass == InputType.TYPE_CLASS_PHONE
                || inputClass == InputType.TYPE_CLASS_DATETIME;
    }

    public static boolean commit(InputConnection connection, String text) {
        return connection != null && text != null && !text.isEmpty()
                && connection.commitText(text, 1);
    }

    public static boolean backspace(InputConnection connection) {
        if (connection == null) return false;
        CharSequence selection = connection.getSelectedText(0);
        if (selection != null && selection.length() > 0) {
            return connection.commitText("", 1);
        }
        if (connection.deleteSurroundingTextInCodePoints(1, 0)) return true;
        // Some editors only implement the older UTF-16 API. Inspect at most one surrogate pair.
        CharSequence previous = connection.getTextBeforeCursor(2, 0);
        if (previous == null) return false;
        int length = previous.length();
        int count = length >= 2 && Character.isSurrogatePair(
                previous.charAt(length - 2), previous.charAt(length - 1)) ? 2 : 1;
        return connection.deleteSurroundingText(count, 0);
    }

    public static final class Action {
        public final boolean newline;
        public final int id;
        public final CharSequence customLabel;

        private Action(boolean newline, int id, CharSequence customLabel) {
            this.newline = newline;
            this.id = id;
            this.customLabel = customLabel;
        }
    }

    public static Action action(EditorInfo info) {
        if (info == null || (info.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            return new Action(true, EditorInfo.IME_ACTION_NONE, null);
        }
        if (info.actionLabel != null && info.actionLabel.length() > 0 && info.actionId != 0) {
            return new Action(false, info.actionId, info.actionLabel.toString());
        }
        int id = info.imeOptions & EditorInfo.IME_MASK_ACTION;
        boolean newline = id == EditorInfo.IME_ACTION_NONE || id == EditorInfo.IME_ACTION_UNSPECIFIED;
        return new Action(newline, id, null);
    }

    public static boolean enter(InputConnection connection, EditorInfo info) {
        if (connection == null || info == null) return false;
        Action action = action(info);
        return action.newline ? commit(connection, "\n") : connection.performEditorAction(action.id);
    }
}
