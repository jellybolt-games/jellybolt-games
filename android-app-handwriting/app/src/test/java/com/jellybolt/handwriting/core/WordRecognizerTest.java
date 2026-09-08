package com.jellybolt.handwriting.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import static org.junit.Assert.*;

public class WordRecognizerTest {
    @Test public void requestedGroupsAreExplicitImmutableAndOnlyAddDigits() {
        assertEquals(Collections.singletonList(Alphabet.DIGITS), WordRecognizer.personalGroups(Alphabet.DIGITS));
        assertEquals(Arrays.asList(Alphabet.ENGLISH_LOWER, Alphabet.DIGITS),
                WordRecognizer.personalGroups(Alphabet.ENGLISH_LOWER));
        assertEquals(Arrays.asList(Alphabet.ENGLISH_UPPER, Alphabet.DIGITS),
                WordRecognizer.personalGroups(Alphabet.ENGLISH_UPPER));
        assertEquals(Arrays.asList(Alphabet.HEBREW, Alphabet.DIGITS), WordRecognizer.personalGroups(Alphabet.HEBREW));
        assertThrows(UnsupportedOperationException.class, () -> WordRecognizer.personalGroups(Alphabet.HEBREW).clear());
        assertThrows(IllegalArgumentException.class, () -> WordRecognizer.personalGroups(null));
        assertThrows(IllegalArgumentException.class, () -> WordRecognizer.personalGroups("English"));
    }

    @Test public void wholeNumbersAndRepeatedCharactersSplitBySpaceNotStrokeCount() {
        for (String text : Arrays.asList("12", "125", "507", "57", "1001", "888", "0123456789")) {
            assertText(text, recognize(compose(Alphabet.DIGITS, text, 24), Alphabet.DIGITS));
        }
        for (String text : Arrays.asList("cat", "mom", "minimum")) {
            assertText(text, recognize(compose(Alphabet.ENGLISH_LOWER, text, 22), Alphabet.ENGLISH_LOWER));
        }
    }

    @Test public void hostDrawingScaleAndSpacingKeepTextAndCorrectionInkInLogicalOrder() {
        String[] groups = {Alphabet.DIGITS, Alphabet.DIGITS, Alphabet.ENGLISH_LOWER, Alphabet.HEBREW};
        String[] spatial = {"57", "23", "cat", "\u05de\u05dc\u05e9"};
        String[] logical = {"57", "23", "cat", "\u05e9\u05dc\u05de"};
        for (int i = 0; i < groups.length; i++) {
            List<List<Ink.Point>> strokes = new ArrayList<>();
            for (int c = 0; c < spatial[i].length(); c++) {
                Ink glyph = sample(groups[i], spatial[i].substring(c, c + 1));
                for (List<Ink.Point> stroke : glyph.strokes) {
                    List<Ink.Point> points = new ArrayList<>();
                    for (Ink.Point p : stroke) {
                        points.add(new Ink.Point(20 + c * 90 + p.x * 70, 30 + p.y * 120));
                    }
                    strokes.add(points);
                }
            }
            Ink input = new Ink(strokes);
            WordRecognizer.Result result = WordRecognizer.recognize(
                    input, groups[i], Collections.emptyMap(), true, false);
            assertText(logical[i], result);
            assertPiecesPreserved(input, result);
            for (int c = 1; c < result.characters.size(); c++) {
                boolean leftToRight = minX(result.characters.get(c - 1).ink) < minX(result.characters.get(c).ink);
                assertEquals(!Alphabet.HEBREW.equals(groups[i]), leftToRight);
            }
        }
    }

    @Test public void independentlyDrawnNumbersVaryInShapeScaleBaselineAndSampling() {
        Ink one = draw(path(11, 5, 12, 49, 13, 96));
        Ink two = draw(path(8, 24, 19, 8, 37, 5, 61, 16, 66, 31,
                53, 50, 9, 91, 70, 95));
        Ink five = draw(path(75, 4, 21, 7, 18, 46, 40, 42,
                65, 49, 76, 70, 63, 89, 37, 95, 13, 84));
        Ink zero = draw(path(43, 5, 20, 14, 11, 42, 16, 76, 32, 96,
                58, 90, 72, 60, 69, 25, 54, 7, 43, 5));
        Ink seven = draw(path(9, 8, 82, 9, 34, 99));
        assertText("125", recognize(compose(18, one, shift(two, .93f, 0, 4), five), Alphabet.DIGITS));
        assertText("507", recognize(compose(21, five, zero, shift(seven, .97f, 0, -2)), Alphabet.DIGITS));
        assertText("12", recognize(compose(4, one, two), Alphabet.DIGITS));
    }

    @Test public void independentlyDrawnEnglishWordsDoNotRequireTemplateComposition() {
        Ink c = draw(path(67, 35, 50, 25, 28, 29, 12, 45, 10, 65,
                20, 82, 40, 91, 62, 86));
        Ink a = draw(path(58, 39, 44, 27, 24, 30, 9, 52, 11, 73, 26, 91,
                48, 88, 60, 68), path(61, 27, 61, 92, 73, 93));
        Ink t = draw(path(35, 5, 34, 80, 43, 94, 60, 94, 71, 84), path(7, 33, 66, 35));
        Ink m = draw(path(8, 96, 9, 29), path(9, 49, 25, 31, 40, 35, 47, 51, 47, 96),
                path(47, 51, 62, 31, 79, 35, 92, 53, 92, 96));
        Ink o = draw(path(67, 58, 64, 41, 46, 27, 25, 32, 12, 52, 14, 74,
                30, 91, 51, 87, 66, 73, 67, 58));
        assertText("cat", recognize(compose(12, c, a, t), Alphabet.ENGLISH_LOWER));
        assertText("mom", recognize(compose(10, m, o, shift(m, .95f, 0, 3)), Alphabet.ENGLISH_LOWER));
    }

    @Test public void dotsAndMultiplePenLiftsRemainOneCharacterRegardlessOfWritingOrder() {
        Ink h = draw(path(14, 8, 16, 96), path(85, 6, 84, 97), path(16, 51, 84, 50));
        Ink a = draw(path(9, 98, 48, 6, 89, 97), path(27, 63, 72, 61));
        WordRecognizer.Result upper = recognize(compose(9, h, a), Alphabet.ENGLISH_UPPER);
        assertText("HA", upper);
        assertEquals(3, upper.characters.get(0).ink.strokes.size());
        assertEquals(2, upper.characters.get(1).ink.strokes.size());
        Ink dotted = compose(Alphabet.ENGLISH_LOWER, "ijii", 9);
        List<List<Ink.Point>> reordered = new ArrayList<>(dotted.strokes);
        Collections.reverse(reordered);
        WordRecognizer.Result result = recognize(new Ink(reordered), Alphabet.ENGLISH_LOWER);
        assertText("ijii", result);
        for (WordRecognizer.CharacterResult character : result.characters) assertEquals(2, character.ink.strokes.size());
        assertPiecesPreserved(dotted, result);
    }

    @Test public void tinyInternalGapCanJoinPenUpPartsWithoutMergingCloseWholeLetters() {
        Ink brokenH = draw(path(0, 0, 0, 100), path(70, 0, 70, 100), path(2, 50, 68, 50));
        assertText("H", recognize(brokenH, Alphabet.ENGLISH_UPPER));
        assertText("cat", recognize(compose(Alphabet.ENGLISH_LOWER, "cat", 3), Alphabet.ENGLISH_LOWER));
        assertText("11", recognize(compose(Alphabet.DIGITS, "11", 4), Alphabet.DIGITS));
    }

    @Test public void anOffsetDotAboveTheStemAttachesWithoutCreatingAnotherCharacter() {
        Ink dottedI = draw(path(20, 35, 21, 99), path(27, 7, 28, 9));
        WordRecognizer.Result result = recognize(dottedI, Alphabet.ENGLISH_LOWER);
        assertFalse(result.needsSeparation);
        assertEquals(1, result.characters.size());
        assertEquals(2, result.characters.get(0).ink.strokes.size());
        assertTrue(result.characters.get(0).candidates.stream().anyMatch(c -> c.label.equals("i")));
        assertPiecesPreserved(dottedI, result);
    }

    @Test public void hebrewLettersUseRtlAndEveryContiguousNumericRunUsesLtr() {
        assertText("\u05e9\u05dc\u05de", recognize(
                compose(Alphabet.HEBREW, "\u05de\u05dc\u05e9", 20), Alphabet.HEBREW));
        assertText("\u05d0" + "12" + "\u05d1" + "507" + "\u05e9", recognize(
                compose(Alphabet.HEBREW, "\u05e9" + "507" + "\u05d1" + "12" + "\u05d0", 22),
                Alphabet.HEBREW));
        assertText("125", recognize(compose(Alphabet.HEBREW, "125", 20), Alphabet.HEBREW));
        Ink alef = draw(path(18, 10, 83, 94), path(78, 9, 70, 39, 50, 51), path(43, 46, 28, 60, 17, 94));
        Ink bet = draw(path(16, 18, 72, 18, 81, 29, 81, 86), path(9, 88, 94, 88));
        assertText("\u05d0\u05d1", recognize(compose(15, bet, alef), Alphabet.HEBREW));
    }

    @Test public void reverseToggleChangesSequenceNotIndividualCharacterGeometry() {
        Ink cat = compose(Alphabet.ENGLISH_LOWER, "tac", 20);
        assertText("tac", recognize(cat, Alphabet.ENGLISH_LOWER));
        assertText("cat", WordRecognizer.recognize(cat, Alphabet.ENGLISH_LOWER, Collections.emptyMap(), false, true));
        Ink digits = compose(Alphabet.DIGITS, "705", 20);
        assertText("507", WordRecognizer.recognize(digits, Alphabet.DIGITS, Collections.emptyMap(), false, true));
        Ink reversedHebrew = compose(Alphabet.HEBREW, "\u05d0" + "21" + "\u05d1", 20);
        assertText("\u05d0" + "12" + "\u05d1", WordRecognizer.recognize(
                reversedHebrew, Alphabet.HEBREW, Collections.emptyMap(), false, true));
    }

    @Test public void mirroredCharactersAreIndependentOfWordReadingDirection() {
        Ink word = compose(24, DefaultSamples.mirror(sample(Alphabet.DIGITS, "5")),
                sample(Alphabet.DIGITS, "0"), DefaultSamples.mirror(sample(Alphabet.DIGITS, "7")));
        // Samples are unit-sized; a large gap is still valid and has no dictionary meaning.
        assertText("507", WordRecognizer.recognize(word, Alphabet.DIGITS, Collections.emptyMap(), true, false));
        Ink normal = compose(Alphabet.ENGLISH_LOWER, "cat", 20);
        assertText("cat", WordRecognizer.recognize(DefaultSamples.mirror(normal),
                Alphabet.ENGLISH_LOWER, Collections.emptyMap(), true, true));
        assertText("cat", WordRecognizer.recognize(normal, Alphabet.ENGLISH_LOWER, Collections.emptyMap(), true, false));
        Ink hebrew = compose(Alphabet.HEBREW, "\u05d1" + "125" + "\u05e9", 20);
        assertText("\u05e9" + "125" + "\u05d1", WordRecognizer.recognize(DefaultSamples.mirror(hebrew),
                Alphabet.HEBREW, Collections.emptyMap(), true, true));
    }

    @Test public void personalIntendedLabelWinsAcrossAlphabetAndDigitCompetitors() {
        Ink one = sample(Alphabet.DIGITS, "1");
        Map<String, List<HandwritingRecognizer.Example>> personal = Collections.singletonMap(Alphabet.ENGLISH_LOWER,
                Collections.singletonList(new HandwritingRecognizer.Example(7, "z", one)));
        WordRecognizer.Result result = WordRecognizer.recognize(compose(20, one, one),
                Alphabet.ENGLISH_LOWER, personal, true, false);
        assertText("zz", result);
        assertTrue("Preference cannot remove an intrinsic ambiguity", result.uncertain);
        assertEquals(100, result.characters.get(0).candidates.get(0).similarity);
        Map<String, List<HandwritingRecognizer.Example>> digits = Collections.singletonMap(Alphabet.DIGITS,
                Collections.singletonList(new HandwritingRecognizer.Example(8, "7", one)));
        assertText("7", WordRecognizer.recognize(one, Alphabet.ENGLISH_LOWER, digits, true, false));
        assertTrue(recognize(sample(Alphabet.ENGLISH_LOWER, "l"), Alphabet.ENGLISH_LOWER).uncertain);
        Ink mirroredFive = DefaultSamples.mirror(sample(Alphabet.DIGITS, "5"));
        Map<String, List<HandwritingRecognizer.Example>> mirroredPersonal = Collections.singletonMap(Alphabet.DIGITS,
                Collections.singletonList(new HandwritingRecognizer.Example(9, "2", mirroredFive)));
        WordRecognizer.Result corrected = WordRecognizer.recognize(compose(20, mirroredFive, mirroredFive),
                Alphabet.DIGITS, mirroredPersonal, true, false);
        assertText("22", corrected);
        assertTrue(corrected.uncertain);
    }

    @Test public void logicalCharacterInkAndCollectionsAreImmutableAndContainEveryOriginalPiece() {
        Ink ink = compose(Alphabet.HEBREW, "\u05d1" + "12" + "\u05d0", 20);
        WordRecognizer.Result result = recognize(ink, Alphabet.HEBREW);
        assertText("\u05d0" + "12" + "\u05d1", result);
        assertPiecesPreserved(ink, result);
        assertTrue(minX(result.characters.get(0).ink) > minX(result.characters.get(3).ink));
        assertTrue(minX(result.characters.get(1).ink) < minX(result.characters.get(2).ink));
        assertThrows(UnsupportedOperationException.class, result.characters::clear);
        assertThrows(UnsupportedOperationException.class, result.characters.get(0).candidates::clear);
        assertThrows(UnsupportedOperationException.class, result.characters.get(0).ink.strokes::clear);
    }

    @Test public void emptyMalformedAndInvalidPersonalDataAreErrorsEvenWhenNotUsed() {
        Ink ink = sample(Alphabet.DIGITS, "1");
        assertThrows(IllegalArgumentException.class, () -> recognize(null, Alphabet.DIGITS));
        assertThrows(IllegalArgumentException.class, () -> recognize(new Ink(Collections.emptyList()), Alphabet.DIGITS));
        assertThrows(IllegalArgumentException.class, () -> recognize(draw(path(5, 5)), Alphabet.DIGITS));
        assertThrows(IllegalArgumentException.class, () -> recognize(ink, "invalid"));
        assertThrows(IllegalArgumentException.class, () -> WordRecognizer.recognize(ink, Alphabet.DIGITS, null, false, false));
        for (String badGroup : Arrays.asList("unknown", null)) {
            Map<String, List<HandwritingRecognizer.Example>> invalid = new HashMap<>();
            invalid.put(badGroup, Collections.emptyList());
            assertThrows(IllegalArgumentException.class,
                    () -> WordRecognizer.recognize(ink, Alphabet.DIGITS, invalid, false, false));
        }
        Map<String, List<HandwritingRecognizer.Example>> mislabeled = Collections.singletonMap(Alphabet.HEBREW,
                Collections.singletonList(new HandwritingRecognizer.Example(4, "A", ink)));
        assertThrows(IllegalArgumentException.class,
                () -> WordRecognizer.recognize(ink, Alphabet.DIGITS, mislabeled, false, false));
        assertThrows(IllegalArgumentException.class, () -> WordRecognizer.recognize(ink, Alphabet.DIGITS,
                Collections.singletonMap(Alphabet.DIGITS, null), false, false));
        assertThrows(IllegalArgumentException.class, () -> WordRecognizer.recognize(ink, Alphabet.DIGITS,
                Collections.singletonMap(Alphabet.DIGITS, Collections.singletonList(null)), false, false));
        assertThrows(IllegalArgumentException.class, () -> new Ink(Collections.nCopies(129, ink.strokes.get(0))));
        assertThrows(IllegalArgumentException.class, () -> new Ink(
                Collections.singletonList(Collections.nCopies(8193, new Ink.Point(0, 0)))));
    }

    @Test public void multilineConnectedWideAndTooManyCharactersRequestSeparationWithoutPartialText() {
        Ink first = compose(Alphabet.DIGITS, "12", 20);
        Ink second = shift(first, 1, 0, 150);
        List<List<Ink.Point>> twoRows = new ArrayList<>(first.strokes);
        twoRows.addAll(second.strokes);
        assertRejected(new Ink(twoRows), Alphabet.DIGITS);
        assertRejected(compose(20, sample(Alphabet.DIGITS, "2"),
                shift(sample(Alphabet.DIGITS, "5"), 1, 0, 1.7f)), Alphabet.DIGITS);
        assertRejected(draw(path(0, 0, 100, 80, 160, 0, 230, 90, 320, 0, 420, 100)), Alphabet.ENGLISH_LOWER);
        assertRejected(draw(path(0, 20, 300, 20)), Alphabet.DIGITS);
        assertRejected(compose(Alphabet.DIGITS, "1".repeat(17), 20), Alphabet.DIGITS);
        assertText("1".repeat(16), recognize(compose(Alphabet.DIGITS, "1".repeat(16), 20), Alphabet.DIGITS));
    }

    @Test public void singleCharactersRemainAvailableIncludingWideLowercaseMAndW() {
        for (String label : Arrays.asList("m", "w", "i", "j", "a")) {
            assertText(label, recognize(sample(Alphabet.ENGLISH_LOWER, label), Alphabet.ENGLISH_LOWER));
        }
        assertText("5", recognize(sample(Alphabet.DIGITS, "5"), Alphabet.DIGITS));
    }

    @Test(timeout = 10000) public void maximumInputIsBoundedDeterministicAndCancellationIsHonored() {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (int s = 0; s < 128; s++) {
            List<Ink.Point> points = new ArrayList<>();
            for (int p = 0; p < 64; p++) points.add(new Ink.Point(s * 15, p));
            strokes.add(points);
        }
        Ink maximum = new Ink(strokes);
        assertRejected(maximum, Alphabet.DIGITS);
        List<List<Ink.Point>> denselySampled = new ArrayList<>();
        for (int s = 0; s < 128; s++) {
            List<Ink.Point> points = new ArrayList<>();
            for (int p = 0; p < 64; p++) points.add(new Ink.Point((s / 8) * 15, p));
            denselySampled.add(points);
        }
        WordRecognizer.Result maximumAccepted = recognize(new Ink(denselySampled), Alphabet.DIGITS);
        assertText("1".repeat(16), maximumAccepted);
        for (WordRecognizer.CharacterResult character : maximumAccepted.characters) {
            assertEquals(8, character.ink.strokes.size());
        }
        Ink ordinary = compose(Alphabet.ENGLISH_LOWER, "cat123", 20);
        String previous = null;
        for (int i = 0; i < 8; i++) {
            WordRecognizer.Result result = WordRecognizer.recognize(
                    ordinary, Alphabet.ENGLISH_LOWER, Collections.emptyMap(), true, false);
            if (previous != null) assertEquals(previous, result.text);
            previous = result.text;
        }
        Thread.currentThread().interrupt();
        try {
            assertThrows(CancellationException.class, () -> recognize(ordinary, Alphabet.ENGLISH_LOWER));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertText("cat123", recognize(ordinary, Alphabet.ENGLISH_LOWER));
    }

    private static WordRecognizer.Result recognize(Ink ink, String group) {
        return WordRecognizer.recognize(ink, group, Collections.emptyMap(), false, false);
    }

    private static void assertText(String expected, WordRecognizer.Result actual) {
        assertFalse("Unexpected separation request for " + expected, actual.needsSeparation);
        assertEquals(expected, actual.text);
        assertEquals(expected.length(), actual.characters.size());
        for (int i = 0; i < expected.length(); i++) {
            assertEquals(expected.substring(i, i + 1), actual.characters.get(i).label);
        }
    }

    private static void assertRejected(Ink ink, String group) {
        WordRecognizer.Result result = recognize(ink, group);
        assertTrue(result.needsSeparation);
        assertTrue(result.uncertain);
        assertEquals("", result.text);
        assertTrue(result.characters.isEmpty());
    }

    private static void assertPiecesPreserved(Ink input, WordRecognizer.Result result) {
        List<List<Ink.Point>> remaining = new ArrayList<>(input.strokes);
        for (WordRecognizer.CharacterResult character : result.characters) {
            for (List<Ink.Point> stroke : character.ink.strokes) assertTrue(remaining.remove(stroke));
        }
        assertTrue(remaining.isEmpty());
    }

    private static Ink sample(String group, String label) {
        if (label.matches("[0-9]")) group = Alphabet.DIGITS;
        for (HandwritingRecognizer.Example example : DefaultSamples.examples(group)) {
            if (example.label.equals(label)) return example.ink;
        }
        throw new AssertionError(label);
    }

    private static Ink compose(String group, String physicalLeftToRight, float gap) {
        List<Ink> letters = new ArrayList<>();
        for (int i = 0; i < physicalLeftToRight.length(); i++) {
            letters.add(shift(sample(group, physicalLeftToRight.substring(i, i + 1)), 100, 0, 0));
        }
        return compose(gap, letters.toArray(new Ink[0]));
    }

    private static Ink compose(float gap, Ink... characters) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        float cursor = 10;
        for (Ink character : characters) {
            Ink shifted = shift(character, 1, cursor - minX(character), 0);
            strokes.addAll(shifted.strokes);
            cursor = maxX(shifted) + gap;
        }
        return new Ink(strokes);
    }

    private static float minX(Ink ink) {
        float value = Float.MAX_VALUE;
        for (List<Ink.Point> stroke : ink.strokes) for (Ink.Point p : stroke) value = Math.min(value, p.x);
        return value;
    }

    private static float maxX(Ink ink) {
        float value = -Float.MAX_VALUE;
        for (List<Ink.Point> stroke : ink.strokes) for (Ink.Point p : stroke) value = Math.max(value, p.x);
        return value;
    }

    private static Ink shift(Ink ink, float scale, float dx, float dy) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (List<Ink.Point> stroke : ink.strokes) {
            List<Ink.Point> points = new ArrayList<>();
            for (Ink.Point p : stroke) points.add(new Ink.Point(p.x * scale + dx, p.y * scale + dy));
            strokes.add(points);
        }
        return new Ink(strokes);
    }

    private static float[] path(float... xy) { return xy; }

    private static Ink draw(float[]... paths) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (float[] path : paths) {
            List<Ink.Point> points = new ArrayList<>();
            for (int p = 0; p < path.length; p += 2) points.add(new Ink.Point(path[p], path[p + 1]));
            strokes.add(points);
        }
        return new Ink(strokes);
    }
}
