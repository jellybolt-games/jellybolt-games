package com.jellybolt.handwriting.core;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class InkTest {
    @Test public void serializationRetainsPenLiftsAndExactCoordinates() {
        Ink source = new Ink(Arrays.asList(
                Arrays.asList(new Ink.Point(1.25f, -2.5f), new Ink.Point(5, 6)),
                Collections.singletonList(new Ink.Point(9, 11))));
        Ink restored = Ink.decode(source.encode());
        assertEquals(2, restored.strokes.size());
        assertEquals(1.25f, restored.strokes.get(0).get(0).x, 0);
        assertEquals(-2.5f, restored.strokes.get(0).get(0).y, 0);
        assertArrayEquals(source.encode(), restored.encode());
    }

    @Test public void emptyDraftCanBeSavedButIsNotAnExample() {
        Ink empty = new Ink(Collections.emptyList());
        assertTrue(Ink.decode(empty.encode()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new HandwritingRecognizer.Example(1, "0", empty));
    }

    @Test public void constructorTakesDeepImmutableSnapshot() {
        List<Ink.Point> stroke = new ArrayList<>(Collections.singletonList(new Ink.Point(1, 2)));
        List<List<Ink.Point>> strokes = new ArrayList<>(Collections.singletonList(stroke));
        Ink source = new Ink(strokes);
        stroke.clear();
        strokes.clear();
        assertFalse(source.isEmpty());
        assertEquals(1, source.strokes.get(0).size());
        assertThrows(UnsupportedOperationException.class, () -> source.strokes.clear());
        assertThrows(UnsupportedOperationException.class, () -> source.strokes.get(0).clear());
    }

    @Test public void rejectsNonFiniteOrUnboundedCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Ink.Point(Float.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> new Ink.Point(0, Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new Ink.Point(2_000_000, 0));
    }

    @Test public void rejectsOversizedOrEmptyStrokes() {
        assertThrows(IllegalArgumentException.class, () -> new Ink(
                Collections.singletonList(Collections.emptyList())));
        assertThrows(IllegalArgumentException.class, () -> new Ink(Collections.nCopies(
                129, Collections.singletonList(new Ink.Point(1, 1)))));
        assertThrows(IllegalArgumentException.class, () -> new Ink(Collections.singletonList(
                Collections.nCopies(8193, new Ink.Point(1, 1)))));
    }

    @Test public void rejectsCorruptedVersionTruncationCountsAndTrailingBytes() {
        byte[] valid = new Ink(Collections.singletonList(
                Collections.singletonList(new Ink.Point(1, 2)))).encode();
        assertThrows(IllegalArgumentException.class, () -> Ink.decode(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> Ink.decode(Arrays.copyOf(valid, valid.length - 1)));
        assertThrows(IllegalArgumentException.class, () -> Ink.decode(Arrays.copyOf(valid, valid.length + 1)));
        byte[] invalidCount = valid.clone();
        ByteBuffer.wrap(invalidCount).putInt(8, Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> Ink.decode(invalidCount));
        byte[] invalidCoordinate = valid.clone();
        ByteBuffer.wrap(invalidCoordinate).putFloat(12, Float.NaN);
        assertThrows(IllegalArgumentException.class, () -> Ink.decode(invalidCoordinate));
        byte[] invalidVersion = valid.clone();
        invalidVersion[0] = 0;
        assertThrows(IllegalArgumentException.class, () -> Ink.decode(invalidVersion));
    }

    @Test public void canRoundtripLargestAllowedDrawing() {
        Ink source = new Ink(Collections.singletonList(Collections.nCopies(8192, new Ink.Point(1, 2))));
        assertArrayEquals(source.encode(), Ink.decode(source.encode()).encode());
    }
}
