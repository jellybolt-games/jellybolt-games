package com.jellybolt.handwriting;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.jellybolt.handwriting.core.Ink;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;

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

    @Test public void disabledPadDoesNotTurnDrawingIntoPageScrolling() {
        ScrollView page = scrollingPage();
        view.setEnabled(false);
        drag(page, 230, 100);
        assertEquals(250, page.getScrollY());
        assertTrue(view.getInk().isEmpty());
    }

    @Test public void enabledPadKeepsItsStrokeInsideAScrollingPage() {
        ScrollView page = scrollingPage();
        drag(page, 230, 100);
        assertEquals(250, page.getScrollY());
        assertEquals(1, view.getInk().strokes.size());
    }

    @Test public void gesturesOutsideThePadStillScroll() {
        ScrollView page = scrollingPage();
        view.setEnabled(false);
        drag(page, 350, 160);
        assertTrue(page.getScrollY() > 250);
        assertTrue(view.getInk().isEmpty());
    }

    @Test public void disabledDrawingExplainsTheProblemOncePerGesture() {
        ScrollView page = scrollingPage();
        int[] blocked = {0};
        view.setOnDrawingBlockedListener(() -> blocked[0]++);
        view.setDisabledHint(R.string.drawing_loading_hint);
        view.setEnabled(false);
        drag(page, 230, 100);
        assertEquals(1, blocked[0]);
        AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
        view.onInitializeAccessibilityNodeInfo(node);
        assertEquals(RuntimeEnvironment.getApplication().getString(R.string.drawing_loading_hint),
                node.getContentDescription());
        view.setEnabled(true);
        drag(page, 230, 100);
        assertEquals(1, blocked[0]);
        assertEquals(1, view.getInk().strokes.size());
    }

    @Test public void strokeLimitDoesNotHandAnOngoingDragToThePage() {
        ScrollView page = scrollingPage();
        view.setInk(new Ink(Collections.nCopies(Ink.MAX_STROKES,
                Collections.singletonList(new Ink.Point(10, 10)))));
        drag(page, 230, 100);
        assertEquals(250, page.getScrollY());
        assertEquals(Ink.MAX_STROKES, view.getInk().strokes.size());
    }

    @Test public void cancelledMultitouchStrokeStillOwnsTheDragUntilFingerUp() {
        ScrollView page = scrollingPage();
        dispatch(page, MotionEvent.ACTION_DOWN, 230);
        MotionEvent.PointerProperties first = new MotionEvent.PointerProperties();
        first.id = 0;
        first.toolType = MotionEvent.TOOL_TYPE_FINGER;
        MotionEvent.PointerProperties second = new MotionEvent.PointerProperties();
        second.id = 1;
        second.toolType = MotionEvent.TOOL_TYPE_FINGER;
        MotionEvent.PointerCoords firstPoint = new MotionEvent.PointerCoords();
        firstPoint.x = 100;
        firstPoint.y = 180;
        MotionEvent.PointerCoords secondPoint = new MotionEvent.PointerCoords();
        secondPoint.x = 150;
        secondPoint.y = 160;
        MotionEvent extraFinger = MotionEvent.obtain(0, time += 16,
                MotionEvent.ACTION_POINTER_DOWN | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                2, new MotionEvent.PointerProperties[]{first, second},
                new MotionEvent.PointerCoords[]{firstPoint, secondPoint},
                0, 0, 1, 1, 0, 0, android.view.InputDevice.SOURCE_TOUCHSCREEN, 0);
        try {
            assertTrue(view.onTouchEvent(extraFinger));
        } finally {
            extraFinger.recycle();
        }
        dispatch(page, MotionEvent.ACTION_MOVE, 100);
        dispatch(page, MotionEvent.ACTION_UP, 100);
        assertEquals(250, page.getScrollY());
        assertTrue(view.getInk().isEmpty());
        drag(page, 350, 160);
        assertTrue(page.getScrollY() > 250);
    }

    @Test public void onlyAFinishedUserStrokeCanArmAutomaticInsertion() {
        int[] finished = {0};
        view.setOnStrokeFinishedListener(() -> finished[0]++);
        event(MotionEvent.ACTION_DOWN, 20, 30);
        event(MotionEvent.ACTION_MOVE, 40, 50);
        assertEquals(0, finished[0]);
        event(MotionEvent.ACTION_UP, 60, 70);
        assertEquals(1, finished[0]);
        Ink draft = view.getInk();
        view.setInk(draft);
        view.undoStroke();
        view.clear();
        assertEquals(1, finished[0]);
        event(MotionEvent.ACTION_DOWN, 20, 30);
        event(MotionEvent.ACTION_CANCEL, 40, 50);
        event(MotionEvent.ACTION_UP, 40, 50);
        assertEquals(1, finished[0]);
    }

    @Test public void rejectedDownAtTheLimitStillInvalidatesThePendingTimer() {
        view.setInk(new Ink(Collections.nCopies(Ink.MAX_STROKES,
                Collections.singletonList(new Ink.Point(10, 10)))));
        int[] changes = {0};
        int[] completions = {0};
        view.setOnInkChangedListener(() -> changes[0]++);
        view.setOnStrokeFinishedListener(() -> completions[0]++);
        event(MotionEvent.ACTION_DOWN, 20, 30);
        assertEquals(1, changes[0]);
        event(MotionEvent.ACTION_UP, 20, 30);
        assertEquals(0, completions[0]);
        assertFalse(view.isDrawing());
    }

    private ScrollView scrollingPage() {
        ScrollView page = new ScrollView(RuntimeEnvironment.getApplication());
        LinearLayout content = new LinearLayout(RuntimeEnvironment.getApplication());
        content.setOrientation(LinearLayout.VERTICAL);
        content.addView(new View(RuntimeEnvironment.getApplication()),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
        content.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 240));
        content.addView(new View(RuntimeEnvironment.getApplication()),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 900));
        page.addView(content);
        page.measure(View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY));
        page.layout(0, 0, 400, 400);
        page.scrollTo(0, 250);
        assertEquals(250, page.getScrollY());
        return page;
    }

    private void drag(ScrollView page, float startY, float endY) {
        dispatch(page, MotionEvent.ACTION_DOWN, startY);
        dispatch(page, MotionEvent.ACTION_MOVE, (startY + endY) / 2);
        dispatch(page, MotionEvent.ACTION_MOVE, endY);
        dispatch(page, MotionEvent.ACTION_UP, endY);
    }

    private void dispatch(ScrollView page, int action, float y) {
        MotionEvent event = MotionEvent.obtain(0, time += 16, action, 100, y, 0);
        try {
            assertTrue(page.dispatchTouchEvent(event));
        } finally {
            event.recycle();
        }
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
