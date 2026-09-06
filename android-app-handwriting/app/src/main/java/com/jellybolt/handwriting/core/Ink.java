package com.jellybolt.handwriting.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Ink {
    public static final int MAX_POINTS = 8192;
    public static final int MAX_STROKES = 128;
    private static final int MAGIC = 0x494E4B31;
    public final List<List<Point>> strokes;

    public static final class Point {
        public final float x;
        public final float y;

        public Point(float x, float y) {
            if (!Float.isFinite(x) || !Float.isFinite(y)
                    || Math.abs(x) > 1_000_000 || Math.abs(y) > 1_000_000) {
                throw new IllegalArgumentException("Invalid ink coordinate");
            }
            this.x = x;
            this.y = y;
        }
    }

    public Ink(List<List<Point>> strokes) {
        if (strokes == null || strokes.size() > MAX_STROKES) {
            throw new IllegalArgumentException("Too many strokes");
        }
        List<List<Point>> copy = new ArrayList<>();
        int count = 0;
        for (List<Point> stroke : strokes) {
            if (stroke == null || stroke.isEmpty()) {
                throw new IllegalArgumentException("Empty stroke");
            }
            count += stroke.size();
            if (count > MAX_POINTS || stroke.contains(null)) {
                throw new IllegalArgumentException("Invalid or excessive ink points");
            }
            copy.add(Collections.unmodifiableList(new ArrayList<>(stroke)));
        }
        this.strokes = Collections.unmodifiableList(copy);
    }

    public boolean isEmpty() {
        return strokes.isEmpty();
    }

    public byte[] encode() {
        int size = 8;
        for (List<Point> stroke : strokes) size += 4 + stroke.size() * 8;
        ByteBuffer data = ByteBuffer.allocate(size);
        data.putInt(MAGIC).putInt(strokes.size());
        for (List<Point> stroke : strokes) {
            data.putInt(stroke.size());
            for (Point point : stroke) data.putFloat(point.x).putFloat(point.y);
        }
        return data.array();
    }

    public static Ink decode(byte[] bytes) {
        if (bytes == null || bytes.length < 8
                || bytes.length > 8 + MAX_STROKES * 4 + MAX_POINTS * 8) {
            throw new IllegalArgumentException("Invalid saved ink length");
        }
        ByteBuffer data = ByteBuffer.wrap(bytes);
        if (data.getInt() != MAGIC) throw new IllegalArgumentException("Unknown ink format");
        int strokes = data.getInt();
        if (strokes < 0 || strokes > MAX_STROKES) {
            throw new IllegalArgumentException("Invalid saved stroke count");
        }
        List<List<Point>> result = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < strokes; i++) {
            if (data.remaining() < 4) throw new IllegalArgumentException("Truncated saved ink");
            int points = data.getInt();
            if (points <= 0 || points > MAX_POINTS - total || data.remaining() < points * 8) {
                throw new IllegalArgumentException("Invalid saved point count");
            }
            total += points;
            List<Point> stroke = new ArrayList<>();
            for (int j = 0; j < points; j++) {
                stroke.add(new Point(data.getFloat(), data.getFloat()));
            }
            result.add(stroke);
        }
        if (data.hasRemaining()) throw new IllegalArgumentException("Trailing saved ink data");
        return new Ink(result);
    }
}
