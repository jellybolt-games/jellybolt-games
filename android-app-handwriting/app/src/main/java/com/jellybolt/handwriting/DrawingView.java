package com.jellybolt.handwriting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jellybolt.handwriting.core.Ink;

import java.util.ArrayList;
import java.util.List;

public final class DrawingView extends View {
    private final List<List<Ink.Point>> strokes = new ArrayList<>();
    private final Paint inkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint surfacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF bounds = new RectF();
    private List<Ink.Point> current;
    private int pointerId = -1;
    private int pointCount;
    private Runnable onInkChanged;
    private Runnable onLimitReached;

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inkPaint.setColor(Color.rgb(31, 72, 83));
        inkPaint.setStrokeWidth(dp(5));
        inkPaint.setStrokeCap(Paint.Cap.ROUND);
        inkPaint.setStrokeJoin(Paint.Join.ROUND);
        inkPaint.setStyle(Paint.Style.STROKE);
        hintPaint.setColor(Color.rgb(86, 108, 112));
        hintPaint.setTextSize(22 * getResources().getDisplayMetrics().scaledDensity);
        hintPaint.setTextAlign(Paint.Align.CENTER);
        setContentDescription(context.getString(R.string.drawing_description));
        setFocusable(true);
        setClickable(true);
        setSaveEnabled(false);
        setMinimumHeight((int) dp(220));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    public void setOnInkChangedListener(Runnable listener) {
        onInkChanged = listener;
    }

    public void setOnLimitReachedListener(Runnable listener) {
        onLimitReached = listener;
    }

    public boolean isDrawing() {
        return current != null;
    }

    public Ink getInk() {
        // A partial or cancelled gesture must never become a training example.
        return new Ink(strokes);
    }

    public void setInk(Ink ink) {
        if (ink == null) throw new IllegalArgumentException("Ink must not be null");
        cancelCurrent();
        strokes.clear();
        pointCount = 0;
        for (List<Ink.Point> stroke : ink.strokes) {
            strokes.add(new ArrayList<>(stroke));
            pointCount += stroke.size();
        }
        changed();
    }

    public void clear() {
        cancelCurrent();
        strokes.clear();
        pointCount = 0;
        changed();
    }

    public void undoStroke() {
        if (current != null) {
            cancelCurrent();
        } else if (!strokes.isEmpty()) {
            pointCount -= strokes.remove(strokes.size() - 1).size();
        }
        changed();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bounds.set(dp(1), dp(1), getWidth() - dp(1), getHeight() - dp(1));
        surfacePaint.setStyle(Paint.Style.FILL);
        surfacePaint.setColor(Color.WHITE);
        canvas.drawRoundRect(bounds, dp(16), dp(16), surfacePaint);
        surfacePaint.setStyle(Paint.Style.STROKE);
        surfacePaint.setStrokeWidth(dp(2));
        surfacePaint.setColor(Color.rgb(112, 146, 151));
        canvas.drawRoundRect(bounds, dp(16), dp(16), surfacePaint);
        if (strokes.isEmpty() && current == null) {
            canvas.drawText(getResources().getString(R.string.draw_here),
                    getWidth() / 2f, getHeight() / 2f - (hintPaint.ascent() + hintPaint.descent()) / 2f,
                    hintPaint);
        }
        for (List<Ink.Point> stroke : strokes) drawStroke(canvas, stroke);
        if (current != null) drawStroke(canvas, current);
    }

    private void drawStroke(Canvas canvas, List<Ink.Point> stroke) {
        if (stroke.isEmpty()) return;
        Ink.Point first = stroke.get(0);
        if (stroke.size() == 1) {
            inkPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(screenX(first.x), screenY(first.y), inkPaint.getStrokeWidth() / 2f, inkPaint);
            inkPaint.setStyle(Paint.Style.STROKE);
            return;
        }
        path.reset();
        path.moveTo(screenX(first.x), screenY(first.y));
        for (int i = 1; i < stroke.size(); i++) {
            Ink.Point point = stroke.get(i);
            path.lineTo(screenX(point.x), screenY(point.y));
        }
        canvas.drawPath(path, inkPaint);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            cancelCurrent();
            if (strokes.size() >= Ink.MAX_STROKES || pointCount >= Ink.MAX_POINTS) {
                limitReached();
                return true;
            }
            pointerId = event.getPointerId(0);
            current = new ArrayList<>();
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
            addPoint(event.getX(), event.getY());
            changed();
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_DOWN
                || action == MotionEvent.ACTION_POINTER_UP) {
            cancelCurrent();
            changed();
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
            if (current == null) return true;
            int index = event.findPointerIndex(pointerId);
            if (index < 0) {
                cancelCurrent();
                changed();
                return true;
            }
            for (int i = 0; i < event.getHistorySize() && current != null; i++) {
                addPoint(event.getHistoricalX(index, i), event.getHistoricalY(index, i));
            }
            if (current != null) addPoint(event.getX(index), event.getY(index));
            if (action == MotionEvent.ACTION_UP && current != null) {
                if (!current.isEmpty()) {
                    strokes.add(current);
                    pointCount += current.size();
                }
                current = null;
                pointerId = -1;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                performClick();
            }
            changed();
            return true;
        }
        return true;
    }

    private void addPoint(float x, float y) {
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            cancelCurrent();
            return;
        }
        float size = Math.max(1, Math.min(getWidth(), getHeight()));
        // A centered isotropic coordinate system preserves shape when the device rotates.
        Ink.Point point = new Ink.Point(
                (Math.max(0, Math.min(getWidth(), x)) - getWidth() / 2f) * 1000f / size + 500,
                (Math.max(0, Math.min(getHeight(), y)) - getHeight() / 2f) * 1000f / size + 500);
        if (!current.isEmpty()) {
            Ink.Point previous = current.get(current.size() - 1);
            if (point.x == previous.x && point.y == previous.y) return;
        }
        if (pointCount + current.size() >= Ink.MAX_POINTS) {
            cancelCurrent();
            limitReached();
            return;
        }
        current.add(point);
    }

    private void cancelCurrent() {
        current = null;
        pointerId = -1;
        if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
    }

    private void limitReached() {
        announceForAccessibility(getResources().getString(R.string.ink_limit));
        if (onLimitReached != null) onLimitReached.run();
        invalidate();
    }

    private void changed() {
        invalidate();
        if (onInkChanged != null) onInkChanged.run();
    }

    private float screenX(float x) {
        return (x - 500) * Math.min(getWidth(), getHeight()) / 1000f + getWidth() / 2f;
    }

    private float screenY(float y) {
        return (y - 500) * Math.min(getWidth(), getHeight()) / 1000f + getHeight() / 2f;
    }

    @Override protected void onDetachedFromWindow() {
        cancelCurrent();
        super.onDetachedFromWindow();
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
