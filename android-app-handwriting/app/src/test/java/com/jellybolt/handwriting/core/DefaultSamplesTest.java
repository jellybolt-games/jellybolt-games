package com.jellybolt.handwriting.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;

import static org.junit.Assert.*;

public class DefaultSamplesTest {
    private static final List<String> GROUPS = Arrays.asList(Alphabet.DIGITS,
            Alphabet.ENGLISH_UPPER, Alphabet.ENGLISH_LOWER, Alphabet.HEBREW);

    @Test public void coversAll89LabelsWithValidOriginalStrokesAndGloballyUniqueNegativeIds() {
        Set<Long> ids = new HashSet<>();
        int labelCount = 0;
        for (String group : GROUPS) {
            Set<String> labels = new HashSet<>();
            Set<String> encoded = new HashSet<>();
            for (HandwritingRecognizer.Example example : DefaultSamples.examples(group)) {
                assertTrue(example.id < 0);
                assertTrue("Duplicate starter id", ids.add(example.id));
                assertTrue(Alphabet.labels(group).contains(example.label));
                labels.add(example.label);
                assertFalse(example.ink.isEmpty());
                assertTrue("Duplicated ink within " + group,
                        encoded.add(Arrays.toString(example.ink.encode())));
                for (List<Ink.Point> stroke : example.ink.strokes) {
                    assertTrue(stroke.size() >= 2);
                    for (Ink.Point point : stroke) {
                        assertTrue(Float.isFinite(point.x) && point.x >= 0 && point.x <= 1);
                        assertTrue(Float.isFinite(point.y) && point.y >= 0 && point.y <= 1);
                    }
                }
            }
            assertEquals(new HashSet<>(Alphabet.labels(group)), labels);
            labelCount += labels.size();
        }
        assertEquals(89, labelCount);
        assertTrue("Include genuine alternate forms, not only one shape each", ids.size() > 100);
    }

    @Test public void cachedExamplesAndTheirInkAreImmutable() {
        for (String group : GROUPS) {
            List<HandwritingRecognizer.Example> examples = DefaultSamples.examples(group);
            assertSame(examples, DefaultSamples.examples(group));
            assertThrows(UnsupportedOperationException.class, () -> examples.remove(0));
            assertThrows(UnsupportedOperationException.class, () -> examples.add(examples.get(0)));
            Ink ink = examples.get(0).ink;
            assertThrows(UnsupportedOperationException.class, () -> ink.strokes.clear());
            assertThrows(UnsupportedOperationException.class, () -> ink.strokes.get(0).clear());
        }
    }

    @Test public void everyExactStarterRanksFirstWithoutCrossLabelRasterDuplicates() {
        for (String group : GROUPS) {
            for (HandwritingRecognizer.Example example : DefaultSamples.examples(group)) {
                HandwritingRecognizer.Result result = recognize(example.ink, group);
                assertEquals(group + " starter " + example.id, example.label, result.candidates.get(0).label);
                assertEquals(100, result.candidates.get(0).similarity);
                assertTrue(group + " has identical normalized geometry for " + example.label
                                + " and " + result.candidates.get(1).label,
                        result.candidates.get(1).similarity < 100);
                assertEquals(0, result.trainedLabels);
            }
        }
    }

    @Test public void everyStarterSurvivesTranslationAndUniformScale() {
        for (String group : GROUPS) {
            for (HandwritingRecognizer.Example example : DefaultSamples.examples(group)) {
                for (float scale : new float[]{37, 213}) {
                    HandwritingRecognizer.Result result = recognize(transform(example.ink, scale, 117, -83), group);
                    assertEquals(group + " transformed " + example.id,
                            example.label, result.candidates.get(0).label);
                    assertTrue(result.candidates.get(0).similarity >= 95);
                }
            }
        }
    }

    @Test public void heldOutOrdinaryDigitsWorkWithoutPersonalTraining() {
        assertRecognized("0", Alphabet.DIGITS, draw(path(48, 3, 25, 13, 16, 43, 20, 75,
                38, 96, 65, 91, 79, 62, 76, 27, 61, 6, 48, 3)));
        assertRecognized("1", Alphabet.DIGITS, draw(path(42, 4, 44, 54, 45, 99)));
        assertRecognized("2", Alphabet.DIGITS, draw(path(14, 25, 28, 7, 55, 4, 78, 19,
                80, 34, 65, 53, 18, 94, 84, 96)));
        assertRecognized("3", Alphabet.DIGITS, draw(path(18, 12, 43, 4, 71, 13, 78, 31,
                56, 49, 76, 62, 79, 80, 64, 94, 36, 96, 14, 86)));
        assertRecognized("4", Alphabet.DIGITS, draw(path(64, 5, 12, 67, 85, 68),
                path(67, 7, 67, 97)));
        assertRecognized("5", Alphabet.DIGITS, draw(path(80, 6, 27, 8, 24, 47, 49, 44,
                71, 53, 81, 72, 66, 91, 41, 97, 18, 86)));
        assertRecognized("6", Alphabet.DIGITS, draw(path(71, 7, 48, 10, 28, 32, 18, 60,
                22, 81, 40, 96, 61, 92, 78, 76, 74, 54, 55, 45, 32, 52, 19, 65)));
        assertRecognized("7", Alphabet.DIGITS, draw(path(12, 6, 87, 8, 37, 97)));
        assertRecognized("8", Alphabet.DIGITS, draw(path(49, 50, 26, 35, 29, 15, 47, 5,
                68, 13, 75, 31, 49, 50, 23, 65, 20, 82, 43, 97,
                68, 90, 79, 75, 68, 60, 49, 50)));
        assertRecognized("9", Alphabet.DIGITS, draw(path(77, 33, 65, 10, 41, 6, 23, 19,
                20, 38, 38, 54, 63, 51, 76, 35, 76, 62, 66, 85, 44, 97)));
    }

    @Test public void heldOutEnglishStrokesAndMultiplePenLiftsWorkWithoutTraining() {
        assertRecognized("A", Alphabet.ENGLISH_UPPER, draw(path(9, 98, 48, 6, 89, 97),
                path(27, 63, 72, 61)));
        assertRecognized("B", Alphabet.ENGLISH_UPPER, draw(path(19, 97, 20, 5),
                path(20, 5, 57, 7, 75, 21, 72, 39, 54, 50, 20, 50),
                path(20, 50, 57, 49, 80, 64, 75, 87, 57, 97, 19, 97)));
        assertRecognized("E", Alphabet.ENGLISH_UPPER, draw(path(83, 6, 18, 6, 18, 97, 84, 97),
                path(18, 50, 68, 48)));
        assertRecognized("H", Alphabet.ENGLISH_UPPER, draw(path(14, 8, 16, 96),
                path(85, 6, 84, 97), path(16, 51, 84, 50)));
        assertRecognized("M", Alphabet.ENGLISH_UPPER, draw(path(8, 97, 10, 4, 51, 60, 91, 5, 92, 98)));
        assertRecognized("R", Alphabet.ENGLISH_UPPER, draw(path(17, 98, 17, 5, 58, 6,
                78, 21, 74, 40, 54, 51, 17, 51), path(49, 51, 85, 98)));
        assertRecognized("U", Alphabet.ENGLISH_UPPER, draw(path(15, 5, 15, 72, 27, 94,
                50, 98, 74, 91, 84, 69, 84, 5)));
        assertRecognized("X", Alphabet.ENGLISH_UPPER, draw(path(11, 5, 87, 98), path(88, 4, 12, 97)));
        assertRecognized("a", Alphabet.ENGLISH_LOWER, draw(path(66, 39, 52, 27, 32, 30,
                17, 52, 19, 73, 34, 91, 56, 88, 68, 68), path(69, 27, 69, 92, 81, 93)));
        assertRecognized("e", Alphabet.ENGLISH_LOWER, draw(path(16, 56, 79, 53, 71, 33,
                49, 25, 29, 36, 16, 57, 23, 80, 45, 93, 69, 89, 81, 78)));
        assertRecognized("g", Alphabet.ENGLISH_LOWER, draw(path(69, 27, 55, 12, 32, 13,
                17, 31, 21, 51, 42, 61, 59, 56, 69, 39),
                path(69, 13, 69, 75, 60, 92, 41, 97, 23, 87)));
        assertRecognized("h", Alphabet.ENGLISH_LOWER, draw(path(18, 5, 18, 96),
                path(18, 51, 39, 32, 61, 35, 76, 50, 77, 96)));
        assertRecognized("i", Alphabet.ENGLISH_LOWER, draw(path(51, 38, 49, 96), path(50, 8, 51, 11)));
        assertRecognized("m", Alphabet.ENGLISH_LOWER, draw(path(8, 96, 9, 29),
                path(9, 49, 25, 31, 40, 35, 47, 51, 47, 96),
                path(47, 51, 62, 31, 79, 35, 92, 53, 92, 96)));
        assertRecognized("t", Alphabet.ENGLISH_LOWER, draw(path(45, 5, 44, 80, 53, 94, 70, 94, 81, 84),
                path(17, 33, 76, 35)));
        assertRecognized("y", Alphabet.ENGLISH_LOWER, draw(path(15, 9, 47, 61, 80, 9),
                path(80, 9, 47, 77, 31, 94, 16, 98)));
    }

    @Test public void heldOutHebrewIncludingFinalFormsWorksWithoutTraining() {
        assertRecognized("\u05d0", Alphabet.HEBREW, draw(path(18, 10, 83, 94),
                path(78, 9, 70, 39, 50, 51), path(43, 46, 28, 60, 17, 94)));
        assertRecognized("\u05d1", Alphabet.HEBREW, draw(path(16, 18, 72, 18, 81, 29, 81, 86),
                path(9, 88, 94, 88)));
        assertRecognized("\u05d4", Alphabet.HEBREW, draw(path(14, 17, 78, 18, 83, 30, 83, 94),
                path(19, 48, 20, 93)));
        assertRecognized("\u05d7", Alphabet.HEBREW, draw(path(17, 94, 18, 17, 83, 17, 83, 94)));
        assertRecognized("\u05db", Alphabet.HEBREW, draw(path(15, 19, 61, 18, 79, 34,
                84, 54, 75, 77, 58, 89, 14, 89)));
        assertRecognized("\u05da", Alphabet.HEBREW, draw(path(21, 9, 65, 10, 73, 22, 74, 99)));
        assertRecognized("\u05dc", Alphabet.HEBREW, draw(path(22, 3, 22, 34, 83, 34,
                79, 59, 60, 80, 40, 97)));
        assertRecognized("\u05de", Alphabet.HEBREW, draw(path(11, 95, 31, 36, 49, 18,
                72, 24, 84, 45, 84, 94, 49, 94), path(18, 18, 33, 38, 43, 54)));
        assertRecognized("\u05dd", Alphabet.HEBREW, draw(path(17, 92, 17, 17, 82, 17, 83, 92, 17, 92)));
        assertRecognized("\u05df", Alphabet.HEBREW, draw(path(42, 5, 57, 9, 58, 99)));
        assertRecognized("\u05e3", Alphabet.HEBREW, draw(path(45, 43, 21, 43, 18, 26,
                29, 9, 62, 9, 76, 24, 79, 99)));
        assertRecognized("\u05e5", Alphabet.HEBREW, draw(path(25, 6, 47, 36, 58, 51, 58, 99),
                path(84, 8, 72, 30, 47, 36)));
        assertRecognized("\u05e7", Alphabet.HEBREW, draw(path(13, 13, 84, 13, 80, 45,
                67, 65, 49, 73), path(25, 38, 25, 99)));
        assertRecognized("\u05e9", Alphabet.HEBREW, draw(path(10, 16, 18, 70, 32, 91,
                56, 93, 80, 73, 91, 16), path(48, 17, 44, 57, 29, 81)));
    }

    @Test public void personalMeaningWinsAnExactGenericCompetitorEvenWhenLexicallyLater() {
        Ink genericOne = first("1", Alphabet.DIGITS).ink;
        List<HandwritingRecognizer.Example> personal = Collections.singletonList(
                new HandwritingRecognizer.Example(7, "7", genericOne));
        byte[] before = personal.get(0).ink.encode();
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(genericOne, Alphabet.DIGITS, personal);
        assertEquals("7", result.candidates.get(0).label);
        assertEquals(100, result.candidates.get(0).similarity);
        assertEquals(1, result.trainedLabels);
        assertTrue("Identical shapes remain honestly ambiguous", result.uncertain);
        assertEquals(1, personal.size());
        assertEquals(7, personal.get(0).id);
        assertArrayEquals(before, personal.get(0).ink.encode());
        assertRecognized("2", Alphabet.DIGITS, first("2", Alphabet.DIGITS).ink, personal);
        // Removing a profile's examples cannot remove the read-only baseline.
        assertRecognized("1", Alphabet.DIGITS, genericOne);
    }

    @Test public void rareMirroredPersonalVariantIsNotDilutedByFiftyOrdinaryExamples() {
        Ink normal = first("3", Alphabet.DIGITS).ink;
        Ink mirrored = reflect(normal);
        List<HandwritingRecognizer.Example> personal = new ArrayList<>();
        for (int i = 1; i <= 49; i++) personal.add(new HandwritingRecognizer.Example(i, "3", normal));
        personal.add(new HandwritingRecognizer.Example(50, "3", mirrored));
        assertRecognized("3", Alphabet.DIGITS, mirrored, personal);
        assertEquals(100, HandwritingRecognizer.recognize(mirrored, Alphabet.DIGITS, personal)
                .candidates.get(0).similarity);
        assertRecognized("3", Alphabet.DIGITS, normal, personal);
        assertTrue("Reflection must not be normalized away",
                recognize(mirrored, Alphabet.DIGITS).candidates.get(0).similarity < 100);
    }

    @Test public void personalPreferenceAlsoAppliesToNearbyNotJustExactShapes() {
        Ink query = draw(path(0, 0, 10, 100));
        Ink childsSeven = draw(path(0, 0, 24, 100));
        assertRecognized("1", Alphabet.DIGITS, query);
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(query, Alphabet.DIGITS,
                Collections.singletonList(new HandwritingRecognizer.Example(1, "7", childsSeven)));
        assertEquals("7", result.candidates.get(0).label);
        assertEquals("1", result.candidates.get(1).label);
        assertTrue("Preference must not inflate raw similarity",
                result.candidates.get(0).similarity < result.candidates.get(1).similarity);
        assertTrue(result.uncertain);
    }

    @Test public void largePersonalSetKeepsRareCorrectionsAndUntrainedDefaultsAvailable() {
        List<HandwritingRecognizer.Example> personal = new ArrayList<>();
        long id = 1;
        for (String label : Alphabet.labels(Alphabet.ENGLISH_UPPER)) {
            if (label.equals("Z")) continue;
            for (int i = 0; i < 50; i++) {
                personal.add(new HandwritingRecognizer.Example(id++, label, first(label, Alphabet.ENGLISH_UPPER).ink));
            }
        }
        Ink genericI = first("I", Alphabet.ENGLISH_UPPER).ink;
        personal.add(new HandwritingRecognizer.Example(id, "Y", genericI));
        // This tie is between two actual personal meanings; deterministic lexical order is honest.
        assertTrue(HandwritingRecognizer.recognize(genericI, Alphabet.ENGLISH_UPPER, personal).uncertain);
        assertRecognized("Z", Alphabet.ENGLISH_UPPER, first("Z", Alphabet.ENGLISH_UPPER).ink, personal);
        assertEquals(25, HandwritingRecognizer.recognize(genericI, Alphabet.ENGLISH_UPPER, personal).trainedLabels);
    }

    @Test public void rawSimilarityIsNotInflatedByThePersonalRankingPreference() {
        Ink handwritten = draw(path(40, 0, 43, 48, 47, 100));
        HandwritingRecognizer.Example personal = new HandwritingRecognizer.Example(1, "7",
                draw(path(40, 0, 44, 45, 51, 100)));
        HandwritingRecognizer.Result mixed = HandwritingRecognizer.recognize(handwritten,
                Alphabet.DIGITS, Collections.singletonList(personal));
        HandwritingRecognizer.Result personalOnly = HandwritingRecognizer.recognize(handwritten,
                Arrays.asList(personal, new HandwritingRecognizer.Example(2, "2", first("2", Alphabet.DIGITS).ink)));
        HandwritingRecognizer.Candidate mixedSeven = candidate(mixed, "7");
        assertNotNull(mixedSeven);
        assertEquals(candidate(personalOnly, "7").similarity, mixedSeven.similarity);
    }

    @Test public void invalidInputsAreRejectedInsteadOfFallingBackToAnotherAlphabet() {
        Ink ink = first("1", Alphabet.DIGITS).ink;
        assertThrows(IllegalArgumentException.class, () -> DefaultSamples.examples(null));
        assertThrows(IllegalArgumentException.class, () -> DefaultSamples.examples("unknown"));
        assertThrows(IllegalArgumentException.class, () -> HandwritingRecognizer.recognize(ink, null, Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> HandwritingRecognizer.recognize(ink, "unknown", Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> HandwritingRecognizer.recognize(ink, Alphabet.DIGITS, null));
        assertThrows(IllegalArgumentException.class, () -> HandwritingRecognizer.recognize(ink, Alphabet.DIGITS,
                Collections.singletonList(new HandwritingRecognizer.Example(1, "A", ink))));
        assertThrows(IllegalArgumentException.class, () -> HandwritingRecognizer.recognize(ink, Alphabet.DIGITS,
                Collections.singletonList(null)));
        assertThrows(IllegalArgumentException.class, () -> HandwritingRecognizer.recognize(null, Alphabet.DIGITS, Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> HandwritingRecognizer.recognize(
                new Ink(Collections.emptyList()), Alphabet.DIGITS, Collections.emptyList()));
    }

    @Test public void cancellationIsHonoredEvenWithoutPersonalExamples() {
        Ink ink = first("1", Alphabet.DIGITS).ink;
        Thread.currentThread().interrupt();
        try {
            assertThrows(CancellationException.class, () -> recognize(ink, Alphabet.DIGITS));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private static HandwritingRecognizer.Example first(String label, String group) {
        for (HandwritingRecognizer.Example example : DefaultSamples.examples(group)) {
            if (example.label.equals(label)) return example;
        }
        throw new AssertionError("Missing " + label);
    }

    private static HandwritingRecognizer.Candidate candidate(HandwritingRecognizer.Result result, String label) {
        for (HandwritingRecognizer.Candidate candidate : result.candidates) {
            if (candidate.label.equals(label)) return candidate;
        }
        return null;
    }

    private static HandwritingRecognizer.Result recognize(Ink ink, String group) {
        return HandwritingRecognizer.recognize(ink, group, Collections.emptyList());
    }

    private static void assertRecognized(String label, String group, Ink ink) {
        assertRecognized(label, group, ink, Collections.emptyList());
    }

    private static void assertRecognized(String label, String group, Ink ink,
                                        List<HandwritingRecognizer.Example> personal) {
        HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(ink, group, personal);
        assertEquals("Held-out " + group + " " + label, label, result.candidates.get(0).label);
    }

    private static float[] path(float... xy) {
        return xy;
    }

    private static Ink draw(float[]... paths) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (float[] path : paths) {
            List<Ink.Point> points = new ArrayList<>();
            for (int i = 0; i < path.length; i += 2) points.add(new Ink.Point(path[i], path[i + 1]));
            strokes.add(points);
        }
        return new Ink(strokes);
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

    private static Ink reflect(Ink source) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (List<Ink.Point> stroke : source.strokes) {
            List<Ink.Point> points = new ArrayList<>();
            for (Ink.Point point : stroke) points.add(new Ink.Point(1 - point.x, point.y));
            strokes.add(points);
        }
        return new Ink(strokes);
    }
}
