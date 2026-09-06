package com.jellybolt.handwriting;

import android.view.MotionEvent;

import com.jellybolt.handwriting.core.Ink;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class DrawingViewTest {
    private DrawingView view;
    private long time;

    @Before public void setUp() {
        view = new DrawingView(RuntimeEnvironment.getApplication());
        view.layout(0, 0, 400, 400);
    }

    @Test public void fingerDrawingKeepsSeparateStrokesAndNotifiesChanges() {
        int[] changes = {0};
        view.setOnInkChangedListener(() -> changes[0]++);
        event(MotionEvent.ACTION_DOWN, 20, 30);
        event(MotionEvent.ACTION_MOVE, 40, 50);
        event(MotionEvent.ACTION_UP, 60, 70);
        event(MotionEvent.ACTION_DOWN, 100, 110);
        event(MotionEvent.ACTION_UP, 120, 130);
        assertEquals(2, view.getInk().strokes.size());
        assertTrue(changes[0] > 0);
        assertTrue(view.getInk().strokes.get(0).size() >= 2);
    }

    @Test public void cancelledFingerStrokeIsNotSavedAsTraining() {
        event(MotionEvent.ACTION_DOWN, 20, 30);
        event(MotionEvent.ACTION_MOVE, 40, 50);
        event(MotionEvent.ACTION_CANCEL, 60, 70);
        assertTrue(view.getInk().isEmpty());
    }

    @Test public void supportsUndoClearAndDraftRoundtrip() {
        event(MotionEvent.ACTION_DOWN, 20, 30);
        event(MotionEvent.ACTION_UP, 60, 70);
        event(MotionEvent.ACTION_DOWN, 100, 110);
        event(MotionEvent.ACTION_UP, 120, 130);
        Ink draft = view.getInk();
        view.undoStroke();
        assertEquals(1, view.getInk().strokes.size());
        view.clear();
        assertTrue(view.getInk().isEmpty());
        view.setInk(Ink.decode(draft.encode()));
        assertArrayEquals(draft.encode(), view.getInk().encode());
    }

    private void event(int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(0, time += 10, action, x, y, 0);
        try {
            assertTrue(view.onTouchEvent(event));
        } finally {
            event.recycle();
        }
    }
}
