package com.jellybolt.handwriting;

import android.content.Context;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/** Platform checks with a disconnected editor; never enables or selects an input method. */
@RunWith(AndroidJUnit4.class)
public class KeyboardDeviceTest {
    @Test public void androidDiscoversThePermissionProtectedKeyboard() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InputMethodManager manager = context.getSystemService(InputMethodManager.class);
        assertNotNull(manager);
        InputMethodInfo found = null;
        for (InputMethodInfo method : manager.getInputMethodList()) {
            if (method.getPackageName().equals(context.getPackageName())
                    && method.getServiceName().equals("com.jellybolt.handwriting.HandwritingImeService")) {
                found = method;
            }
        }
        assertNotNull("Android must discover the keyboard after package installation", found);
        assertEquals("android.permission.BIND_INPUT_METHOD", found.getServiceInfo().permission);
        assertEquals(0, found.getIsDefaultResourceId());
        assertTrue(found.getSubtypeCount() > 0);
        assertTrue(found.getSubtypeAt(0).isAsciiCapable());
    }

    @Test public void editingKeepsHebrewOrderAndDeletesEmojiWithoutChangingDefaultIme() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String before = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        onMain(() -> {
            Connection connection = new Connection(context);
            assertTrue(KeyboardEditor.commit(connection, "A"));
            assertTrue(KeyboardEditor.commit(connection, "\u05de\u05dd"));
            assertTrue(KeyboardEditor.commit(connection, "\uD83D\uDE00"));
            assertTrue(KeyboardEditor.backspace(connection));
            assertEquals("A\u05de\u05dd", connection.text.toString());
            Selection.setSelection(connection.text, 1, 3);
            assertTrue(KeyboardEditor.commit(connection, "7"));
            assertEquals("A7", connection.text.toString());
        });
        assertEquals(before, Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD));
    }

    @Test public void privateEditorFlagsDisableLearningOnAndroid() {
        EditorInfo info = new EditorInfo();
        info.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
        assertFalse(KeyboardEditor.allowsLearning(info));
        info.inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        assertFalse(KeyboardEditor.allowsLearning(info));
        info.inputType = InputType.TYPE_CLASS_TEXT;
        info.imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING;
        assertFalse(KeyboardEditor.allowsLearning(info));
        info.imeOptions = EditorInfo.IME_ACTION_NEXT;
        assertTrue(KeyboardEditor.allowsLearning(info));
    }

    @Test public void editorActionsRespectMultilineFlagsOnAndroid() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        onMain(() -> {
            Connection connection = new Connection(context);
            EditorInfo info = new EditorInfo();
            info.inputType = InputType.TYPE_CLASS_TEXT;
            info.imeOptions = EditorInfo.IME_ACTION_SEND;
            assertTrue(KeyboardEditor.enter(connection, info));
            assertEquals(EditorInfo.IME_ACTION_SEND, connection.action);
            assertEquals("", connection.text.toString());
            info.imeOptions |= EditorInfo.IME_FLAG_NO_ENTER_ACTION;
            assertTrue(KeyboardEditor.enter(connection, info));
            assertEquals("\n", connection.text.toString());
        });
    }

    @Test public void drawingGesturesDoNotScrollEvenWhenAProfileIsMissing() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        onMain(() -> {
            ScrollView page = new ScrollView(context);
            LinearLayout content = new LinearLayout(context);
            content.setOrientation(LinearLayout.VERTICAL);
            content.addView(new View(context), new LinearLayout.LayoutParams(-1, 300));
            DrawingView drawing = new DrawingView(context);
            content.addView(drawing, new LinearLayout.LayoutParams(-1, 240));
            content.addView(new View(context), new LinearLayout.LayoutParams(-1, 900));
            page.addView(content);
            page.measure(View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY));
            page.layout(0, 0, 400, 400);
            page.scrollTo(0, 250);
            drawing.setEnabled(false);
            int[] blocked = {0};
            drawing.setOnDrawingBlockedListener(() -> blocked[0]++);
            touchDrag(page, 230, 100);
            assertEquals(250, page.getScrollY());
            assertEquals(1, blocked[0]);
            assertTrue(drawing.getInk().isEmpty());
            drawing.setEnabled(true);
            touchDrag(page, 230, 100);
            assertEquals(250, page.getScrollY());
            assertEquals(1, drawing.getInk().strokes.size());
            touchDrag(page, 350, 160);
            assertTrue(page.getScrollY() > 250);
        });
    }

    private static void touchDrag(ScrollView page, float startY, float endY) {
        int[] actions = {MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP};
        float[] positions = {startY, (startY + endY) / 2, endY, endY};
        long start = android.os.SystemClock.uptimeMillis();
        for (int i = 0; i < actions.length; i++) {
            MotionEvent event = MotionEvent.obtain(start, start + i * 16,
                    actions[i], 100, positions[i], 0);
            try {
                assertTrue(page.dispatchTouchEvent(event));
            } finally {
                event.recycle();
            }
        }
    }

    private static void onMain(Runnable operation) throws Exception {
        FutureTask<Void> task = new FutureTask<>(operation, null);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private static final class Connection extends BaseInputConnection {
        final Editable text = new SpannableStringBuilder();
        int action = -1;

        Connection(Context context) {
            super(new View(context), true);
            Selection.setSelection(text, 0);
        }

        @Override public Editable getEditable() { return text; }

        @Override public boolean performEditorAction(int id) {
            action = id;
            return true;
        }
    }
}
