package com.jellybolt.handwriting.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class HandwritingRecognizerTest {
    private static Ink ink(float... coordinates) {
        List<Ink.Point> points = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i += 2) {
            points.add(new Ink.Point(coordinates[i], coordinates[i + 1]));
        }
        return new Ink(Collections.singletonList(points));
    }

    private static final Ink VERTICAL = ink(10, 0, 10, 50, 10, 100);
    private static final Ink HORIZONTAL = ink(0, 10, 50, 10, 100, 10);
    private static final Ink THREE = ink(0, 0, 80, 0, 100, 20, 50, 50, 100, 80, 80, 100, 0, 100);
    private static final Ink MIRRORED_THREE = ink(100, 0, 20, 0, 0, 20, 50, 50, 0, 80, 20, 100, 100, 100);

    private static HandwritingRecognizer.Example example(String label, Ink ink) {
        return new HandwritingRecognizer.Example(1, label, ink);
    }

    private static List<HandwritingRecognizer.Example> repeat(String label, Ink ink, int count) {
        List<HandwritingRecognizer.Example> result = new ArrayList<>();
        for (int i = 0; i < count; i++) result.add(example(label, ink));
        return result;
    }

    @Test public void alphabetsIncludeAllCharactersAndHebrewFinals() {
        assertEquals(10, Alphabet.labels(Alphabet.DIGITS).size());
        assertEquals(26, Alphabet.labels(Alphabet.ENGLISH_UPPER).size());
        assertEquals(26, Alphabet.labels(Alphabet.ENGLISH_LOWER).size());
        List<String> hebrew = Alphabet.labels(Alphabet.HEBREW);
        assertEquals(27, hebrew.size());
        assertTrue(hebrew.containsAll(Arrays.asList("\u05da", "\u05dd", "\u05df", "\u05e3", "\u05e5")));
        assertThrows(IllegalArgumentException.class, () -> Alphabet.labels("unknown"));
    }

    @Test public void needsAtLeastTwoTrainedLabelsRatherThanInventingConfidence() {
        assertTrue(HandwritingRecognizer.recognize(VERTICAL, Collections.emptyList()).candidates.isEmpty());
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(VERTICAL, repeat("1", VERTICAL, 50));
        assertEquals(1, result.trainedLabels);
        assertTrue(result.uncertain);
        assertTrue(result.candidates.isEmpty());
    }

    @Test public void learnsUserMeaningInsteadOfAssumingStandardDigitShapes() {
        List<HandwritingRecognizer.Example> training = Arrays.asList(example("7", VERTICAL), example("1", HORIZONTAL));
        assertEquals("7", HandwritingRecognizer.recognize(VERTICAL, training).candidates.get(0).label);
        assertEquals("1", HandwritingRecognizer.recognize(HORIZONTAL, training).candidates.get(0).label);
    }

    @Test public void learnsBothOrdinaryAndRareMirroredVariantsForSameLabel() {
        List<HandwritingRecognizer.Example> training = repeat("3", THREE, 30);
        training.add(example("3", MIRRORED_THREE));
        training.addAll(repeat("1", VERTICAL, 20));
        assertEquals("3", HandwritingRecognizer.recognize(THREE, training).candidates.get(0).label);
        assertEquals("3", HandwritingRecognizer.recognize(MIRRORED_THREE, training).candidates.get(0).label);
    }

    @Test public void neverMakesReflectedOrRotatedClassesEquivalent() {
        List<HandwritingRecognizer.Example> training = Arrays.asList(
                example("a", THREE), example("b", MIRRORED_THREE),
                example("c", VERTICAL), example("d", HORIZONTAL));
        assertEquals("a", HandwritingRecognizer.recognize(THREE, training).candidates.get(0).label);
        assertEquals("b", HandwritingRecognizer.recognize(MIRRORED_THREE, training).candidates.get(0).label);
        assertEquals("c", HandwritingRecognizer.recognize(VERTICAL, training).candidates.get(0).label);
        assertEquals("d", HandwritingRecognizer.recognize(HORIZONTAL, training).candidates.get(0).label);
        assertTrue(HandwritingRecognizer.recognize(THREE, training).candidates.get(1).similarity < 100);
    }

    @Test public void positionAndUniformScaleDoNotChangeMatch() {
        Ink translated = transform(THREE, 2, 150, -200);
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(translated,
                Arrays.asList(example("3", THREE), example("1", VERTICAL)));
        assertEquals("3", result.candidates.get(0).label);
        assertEquals(100, result.candidates.get(0).similarity);
    }

    @Test public void ignoresStrokeOrderAndDrawingDirectionButPreservesPenLifts() {
        Ink disconnected = new Ink(Arrays.asList(VERTICAL.strokes.get(0), transform(VERTICAL, 1, 100, 0).strokes.get(0)));
        List<Ink.Point> reversedLeft = new ArrayList<>(disconnected.strokes.get(0));
        List<Ink.Point> reversedRight = new ArrayList<>(disconnected.strokes.get(1));
        Collections.reverse(reversedLeft);
        Collections.reverse(reversedRight);
        Ink reversed = new Ink(Arrays.asList(reversedRight, reversedLeft));
        Ink connected = ink(10, 0, 10, 100, 110, 0, 110, 100);
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(reversed,
                Arrays.asList(example("H", disconnected), example("N", connected)));
        assertEquals("H", result.candidates.get(0).label);
        assertEquals(100, result.candidates.get(0).similarity);
        assertTrue(result.candidates.get(1).similarity < 100);
    }

    @Test public void identicalClassesAreExplicitlyAmbiguousEvenWithManyExamples() {
        List<HandwritingRecognizer.Example> training = repeat("0", THREE, 20);
        training.addAll(repeat("9", THREE, 20));
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(THREE, training);
        assertTrue(result.uncertain);
        assertEquals(100, result.candidates.get(0).similarity);
        assertEquals(100, result.candidates.get(1).similarity);
    }

    @Test public void insufficientExamplesStillAskForConfirmation() {
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(VERTICAL,
                Arrays.asList(example("1", VERTICAL), example("2", HORIZONTAL)));
        assertTrue(result.uncertain);
    }

    @Test public void separatesWellSupportedDistinctShapes() {
        List<HandwritingRecognizer.Example> training = repeat("1", VERTICAL, 20);
        training.addAll(repeat("2", HORIZONTAL, 20));
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(VERTICAL, training);
        assertFalse(result.uncertain);
        assertEquals(100, result.candidates.get(0).similarity);
    }

    @Test public void returnsNoMoreThanThreeCandidatesWithDeterministicTies() {
        List<HandwritingRecognizer.Example> training = Arrays.asList(
                example("d", VERTICAL), example("c", VERTICAL),
                example("b", VERTICAL), example("a", VERTICAL));
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(VERTICAL, training);
        assertEquals(4, result.trainedLabels);
        assertEquals(3, result.candidates.size());
        assertEquals("a", result.candidates.get(0).label);
        assertTrue(result.uncertain);
    }

    @Test public void acceptsPointOnlyWritingWithoutNaN() {
        Ink dot = ink(10, 10);
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(dot,
                Arrays.asList(example("a", dot), example("b", THREE)));
        assertEquals("a", result.candidates.get(0).label);
        assertEquals(100, result.candidates.get(0).similarity);
    }

    @Test public void acceptsHebrewLabelsAndCorrectionsImmediately() {
        List<HandwritingRecognizer.Example> training = new ArrayList<>(
                Arrays.asList(example("\u05d0", THREE), example("\u05da", VERTICAL)));
        assertEquals("\u05da", HandwritingRecognizer.recognize(VERTICAL, training).candidates.get(0).label);
        training.add(example("\u05dd", HORIZONTAL));
        assertEquals("\u05dd", HandwritingRecognizer.recognize(HORIZONTAL, training).candidates.get(0).label);
    }

    @Test public void recognizesUnseenPerturbationsAfterTwentyExamplesPerDigit() {
        List<Ink> digits = sevenSegmentDigits();
        List<HandwritingRecognizer.Example> training = new ArrayList<>();
        for (int digit = 0; digit < 10; digit++) {
            for (int sample = 0; sample < 20; sample++) {
                training.add(example(Integer.toString(digit), perturb(digits.get(digit), sample * 0.7)));
            }
        }
        for (int digit = 0; digit < 10; digit++) {
            Ink unseen = transform(perturb(digits.get(digit), 100.3), 1.5f, 12, 23);
            assertEquals("held-out synthetic digit " + digit, Integer.toString(digit),
                    HandwritingRecognizer.recognize(unseen, training).candidates.get(0).label);
        }
    }

    @Test public void emptyDrawingCannotBeClassified() {
        assertThrows(IllegalArgumentException.class, () -> HandwritingRecognizer.recognize(
                new Ink(Collections.emptyList()), Collections.emptyList()));
    }

    private static Ink transform(Ink source, float scale, float dx, float dy) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (List<Ink.Point> stroke : source.strokes) {
            List<Ink.Point> points = new ArrayList<>();
            for (Ink.Point point : stroke) points.add(new Ink.Point(point.x * scale + dx, point.y * scale + dy));
            strokes.add(points);
        }
        return new Ink(strokes);
    }

    private static Ink perturb(Ink source, double phase) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        int index = 0;
        for (List<Ink.Point> stroke : source.strokes) {
            List<Ink.Point> points = new ArrayList<>();
            for (Ink.Point point : stroke) {
                points.add(new Ink.Point(point.x + (float) Math.sin(phase + index) * 1.5f,
                        point.y + (float) Math.cos(phase + index++) * 1.5f));
            }
            strokes.add(points);
        }
        return new Ink(strokes);
    }

    private static List<Ink> sevenSegmentDigits() {
        List<Ink> segments = Arrays.asList(
                ink(0, 0, 60, 0), ink(60, 0, 60, 50), ink(60, 50, 60, 100),
                ink(0, 100, 60, 100), ink(0, 50, 0, 100), ink(0, 0, 0, 50), ink(0, 50, 60, 50));
        String[] patterns = {"012345", "12", "01643", "01236", "5612", "05623", "054326", "012", "0123456", "012356"};
        List<Ink> result = new ArrayList<>();
        for (String pattern : patterns) {
            List<List<Ink.Point>> strokes = new ArrayList<>();
            for (char digit : pattern.toCharArray()) strokes.add(segments.get(digit - '0').strokes.get(0));
            result.add(new Ink(strokes));
        }
        return result;
    }
}
