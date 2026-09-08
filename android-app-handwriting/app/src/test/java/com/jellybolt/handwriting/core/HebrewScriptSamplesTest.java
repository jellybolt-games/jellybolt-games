package com.jellybolt.handwriting.core;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class HebrewScriptSamplesTest {
    @Test public void thirtyOriginalScriptSamplesCoverAll27LabelsAndAreAutomaticallyBundled() {
        List<HandwritingRecognizer.Example> script = HebrewScriptSamples.examples();
        assertSame(script, HebrewScriptSamples.examples());
        assertSame(script, DefaultSamples.hebrewScriptExamples());
        assertSame(DefaultSamples.hebrewScriptExamples(), DefaultSamples.hebrewScriptExamples());
        assertEquals(30, script.size());
        assertEquals(61, DefaultSamples.examples(Alphabet.HEBREW).size());
        assertThrows(UnsupportedOperationException.class, script::clear);
        for (int i = 0; i < 31; i++) assertTrue(DefaultSamples.examples(Alphabet.HEBREW).get(i).id >= -119);
        for (int i = 0; i < script.size(); i++) {
            assertSame(script.get(i), DefaultSamples.examples(Alphabet.HEBREW).get(i + 31));
        }
        Set<String> labels = new HashSet<>();
        Set<Long> ids = new HashSet<>();
        Set<String> geometry = new HashSet<>();
        for (HandwritingRecognizer.Example example : script) {
            assertTrue(example.id <= -1001);
            assertTrue(ids.add(example.id));
            assertTrue(geometry.add(Arrays.toString(example.ink.encode())));
            assertTrue(DefaultSamples.examples(Alphabet.HEBREW).contains(example));
            labels.add(example.label);
            for (List<Ink.Point> stroke : example.ink.strokes) {
                assertTrue(stroke.size() >= 2);
                for (Ink.Point p : stroke) {
                    assertTrue(p.x >= 0 && p.x <= 1 && p.y >= 0 && p.y <= 1);
                }
            }
            assertThrows(UnsupportedOperationException.class, example.ink.strokes::clear);
        }
        assertEquals(new HashSet<>(Alphabet.labels(Alphabet.HEBREW)), labels);
    }

    @Test public void all31OriginalPrintIdsLabelsAndEveryCoordinateRemainByteForByteUnchanged() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        int count = 0;
        for (HandwritingRecognizer.Example example : DefaultSamples.examples(Alphabet.HEBREW)) {
            if (example.id < -119) continue;
            count++;
            data.writeLong(example.id);
            data.writeUTF(example.label);
            data.write(example.ink.encode());
        }
        assertEquals(31, count);
        assertEquals("3a389aa40911f0807445e55f9c53d5bfdab1e6c0589175293a4629dcff6e1b7e",
                HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray())));
    }

    @Test public void allScriptSamplesAndReflectionsRetainTheirIntendedLabelsIncludingRealTies() {
        for (HandwritingRecognizer.Example example : HebrewScriptSamples.examples()) {
            for (boolean mirrored : new boolean[]{false, true}) {
                Ink ink = mirrored ? DefaultSamples.mirror(example.ink) : example.ink;
                for (float scale : new float[]{1, 97, 231}) {
                    HandwritingRecognizer.Result result = recognize(transform(ink, scale, 32, -18), mirrored);
                    HandwritingRecognizer.Candidate candidate = intended(result, example.label);
                    assertNotNull("Missing script label " + hex(example.label) + " mirrored=" + mirrored, candidate);
                    assertTrue("Low self-match for " + hex(example.label), candidate.similarity >= 95);
                    if (!result.candidates.get(0).label.equals(example.label)) assertTrue(result.uncertain);
                    assertEquals(0, result.trainedLabels);
                }
            }
        }
    }

    @Test public void distinctiveModernTopologiesAreNotMerelySquarePrintRescaled() {
        List<HandwritingRecognizer.Example> print = new ArrayList<>();
        for (HandwritingRecognizer.Example example : DefaultSamples.examples(Alphabet.HEBREW)) {
            if (example.id >= -119) print.add(example);
        }
        for (String label : Arrays.asList("\u05d0", "\u05d1", "\u05d2", "\u05d3", "\u05de", "\u05dd", "\u05e9")) {
            HandwritingRecognizer.Result oldShapes = HandwritingRecognizer.recognize(first(label).ink, print);
            assertTrue(hex(label) + " must add a new modern skeleton", oldShapes.candidates.get(0).similarity < 90);
            assertEquals(label, recognize(first(label).ink, false).candidates.get(0).label);
        }
    }

    @Test public void independentlyDrawnScriptLettersIncludingAllFiveFinalsAreRecognized() {
        for (Map.Entry<String, Ink> fixture : heldOut().entrySet()) {
            HandwritingRecognizer.Result result = recognize(fixture.getValue(), false);
            assertEquals("Held-out script " + hex(fixture.getKey()), fixture.getKey(), result.candidates.get(0).label);
        }
    }

    @Test public void mirroredHeldOutModernFormsWorkWithoutPersonalTraining() {
        for (Map.Entry<String, Ink> fixture : heldOut().entrySet()) {
            HandwritingRecognizer.Result result = recognize(DefaultSamples.mirror(fixture.getValue()), true);
            HandwritingRecognizer.Candidate candidate = intended(result, fixture.getKey());
            assertNotNull("Mirrored held-out " + hex(fixture.getKey()), candidate);
            if (!result.candidates.get(0).label.equals(fixture.getKey())) {
                assertTrue("A competing reflected form must stay uncertain", result.uncertain);
            }
        }
    }

    @Test public void bareVavYodAndFinalNunAreHonestTiesAndPersonalMeaningStillWins() {
        Ink straight = draw(path(44, 5, 44, 98));
        HandwritingRecognizer.Result baseline = recognize(straight, false);
        assertTrue(baseline.uncertain);
        for (String label : Arrays.asList("\u05d5", "\u05d9", "\u05df")) {
            assertEquals(100, intended(baseline, label).similarity);
            List<HandwritingRecognizer.Example> personal = Collections.singletonList(
                    new HandwritingRecognizer.Example(75, label, straight));
            HandwritingRecognizer.Result trained = HandwritingRecognizer.recognize(straight, Alphabet.HEBREW, personal, true);
            assertEquals(label, trained.candidates.get(0).label);
            assertTrue(trained.uncertain);
            assertEquals(1, trained.trainedLabels);
            assertEquals(1, personal.size());
            assertEquals(75, personal.get(0).id);
        }
        Ink scriptShin = first("\u05e9").ink;
        HandwritingRecognizer.Result corrected = HandwritingRecognizer.recognize(scriptShin, Alphabet.HEBREW,
                Collections.singletonList(new HandwritingRecognizer.Example(77, "\u05ea", scriptShin)), true);
        assertEquals("\u05ea", corrected.candidates.get(0).label);
        assertTrue(corrected.uncertain);
    }

    @Test public void printScriptWordsAndDigitsShareOneRtlAlphabetWithoutReversingNumbers() {
        Ink shin = first("\u05e9").ink;
        Ink lamed = first("\u05dc").ink;
        Ink mem = first("\u05dd").ink;
        Ink printVav = DefaultSamples.examples(Alphabet.HEBREW).stream()
                .filter(e -> e.label.equals("\u05d5")).findFirst().get().ink;
        assertWord("\u05e9\u05dc\u05d5\u05dd", compose(mem, printVav, lamed, shin), false, false);
        assertWord("\u05d0\u05d1\u05d2", compose(first("\u05d2").ink, first("\u05d1").ink, first("\u05d0").ink), false, false);
        Ink one = DefaultSamples.examples(Alphabet.DIGITS).stream().filter(e -> e.label.equals("1")).findFirst().get().ink;
        Ink two = DefaultSamples.examples(Alphabet.DIGITS).stream().filter(e -> e.label.equals("2")).findFirst().get().ink;
        Ink mixed = compose(mem, one, two, shin);
        assertWord("\u05e9" + "12" + "\u05dd", mixed, true, false);
        assertWord("\u05e9" + "12" + "\u05dd", DefaultSamples.mirror(mixed), true, true);
        Map<String, Ink> held = heldOut();
        assertWord("\u05d0\u05d1\u05d2", compose(held.get("\u05d2"), held.get("\u05d1"), held.get("\u05d0")), false, false);
    }

    @Test public void everyPrimaryScriptFormIsOneWordSegmentWithAnIntendedSuggestion() {
        for (HandwritingRecognizer.Example example : HebrewScriptSamples.examples().subList(0, 27)) {
            for (boolean mirrored : new boolean[]{false, true}) {
                Ink ink = mirrored ? DefaultSamples.mirror(example.ink) : example.ink;
                WordRecognizer.Result result = WordRecognizer.recognize(
                        transform(ink, 110, 20, 30), Alphabet.HEBREW, Collections.emptyMap(), mirrored, false);
                assertFalse("Cannot segment script " + hex(example.label), result.needsSeparation);
                assertEquals("Split script " + hex(example.label), 1, result.characters.size());
                assertTrue("Missing word suggestion " + hex(example.label),
                        result.characters.get(0).candidates.stream().anyMatch(c -> c.label.equals(example.label)));
                if (!result.text.equals(example.label)) assertTrue(result.uncertain);
                assertEquals(example.ink.strokes.size(), result.characters.get(0).ink.strokes.size());
            }
        }
    }

    private static Map<String, Ink> heldOut() {
        Map<String, Ink> samples = new LinkedHashMap<>();
        samples.put("\u05d0", draw(path(13, 95, 23, 66, 34, 38, 46, 8),
                path(79, 61, 60, 66, 39, 79, 46, 90, 66, 96, 91, 90)));
        samples.put("\u05d1", draw(path(18, 31, 38, 27, 62, 36, 78, 55, 83, 74,
                70, 79, 52, 72, 33, 77, 15, 91)));
        samples.put("\u05d2", draw(path(25, 23, 43, 32, 54, 46, 46, 60, 25, 75,
                18, 84, 30, 96, 50, 98, 67, 88)));
        samples.put("\u05d3", draw(path(12, 27, 32, 23, 61, 23, 85, 33, 72, 49, 48, 61),
                path(48, 61, 37, 58, 41, 51, 52, 54, 62, 70, 55, 94)));
        samples.put("\u05d4", draw(path(16, 39, 38, 29, 66, 28, 84, 40, 88, 61, 81, 90),
                path(38, 62, 48, 62, 54, 72, 49, 86)));
        samples.put("\u05d5", draw(path(49, 19, 52, 43, 49, 70, 43, 96)));
        samples.put("\u05d6", draw(path(74, 20, 54, 34, 49, 45, 57, 58, 72, 70,
                68, 81, 49, 91, 29, 98, 10, 97)));
        samples.put("\u05d7", draw(path(29, 20, 26, 57, 16, 94),
                path(20, 40, 39, 38, 67, 51, 80, 68, 77, 86)));
        samples.put("\u05d8", draw(path(82, 7, 64, 26, 46, 56, 30, 84,
                35, 97, 52, 95, 72, 82, 89, 62)));
        samples.put("\u05d9", draw(path(57, 27, 55, 35, 50, 46)));
        samples.put("\u05db", draw(path(19, 26, 42, 22, 65, 31, 78, 48, 76, 67,
                60, 82, 38, 89, 16, 79)));
        samples.put("\u05da", draw(path(25, 21, 43, 15, 66, 23, 78, 36,
                72, 48, 57, 57, 42, 63, 32, 82, 28, 99)));
        samples.put("\u05dc", draw(path(29, 64, 19, 72, 12, 91, 19, 97, 29, 82,
                34, 50, 37, 27, 46, 13, 64, 6, 85, 13)));
        samples.put("\u05de", draw(path(10, 96, 26, 70, 43, 42, 52, 29, 60, 39,
                67, 65, 76, 85, 83, 68, 91, 21)));
        samples.put("\u05dd", draw(path(10, 89, 27, 72, 47, 60, 60, 60),
                path(60, 60, 72, 73, 89, 63, 95, 45, 82, 30, 65, 28, 53, 39, 54, 50, 60, 60)));
        samples.put("\u05e0", draw(path(58, 22, 66, 40, 72, 58, 61, 72, 36, 87, 13, 96)));
        samples.put("\u05df", draw(path(56, 17, 55, 38, 48, 70, 40, 93, 44, 99)));
        samples.put("\u05e1", draw(path(82, 55, 76, 35, 58, 23, 38, 28, 22, 43,
                18, 63, 29, 82, 48, 89, 68, 81, 80, 66, 82, 55)));
        samples.put("\u05e2", draw(path(16, 27, 38, 31, 57, 43, 69, 66,
                66, 86, 51, 94, 39, 85, 38, 65, 50, 45, 66, 27, 83, 31)));
        samples.put("\u05e4", draw(path(14, 39, 39, 27, 64, 32, 76, 49, 71, 68,
                54, 84, 32, 89, 19, 82, 18, 70, 26, 57, 39, 46)));
        samples.put("\u05e3", draw(path(24, 55, 16, 44, 25, 34, 47, 21, 70, 13,
                88, 4, 85, 0, 72, 4, 60, 20, 56, 42, 54, 71, 46, 89,
                35, 97, 31, 91, 35, 79, 47, 66, 56, 61)));
        samples.put("\u05e6", draw(path(24, 29, 47, 24, 68, 31, 77, 42,
                65, 56, 51, 67, 56, 78, 66, 87, 49, 97, 24, 99, 10, 95)));
        samples.put("\u05e5", draw(path(21, 6, 17, 23, 26, 35, 48, 35),
                path(48, 35, 53, 14, 68, 3, 84, 6, 89, 16, 77, 28, 48, 42,
                        48, 73, 40, 90, 26, 99, 19, 93, 25, 80, 39, 70, 48, 67)));
        samples.put("\u05e7", draw(path(15, 29, 39, 21, 63, 30, 78, 44, 81, 62),
                path(48, 41, 43, 65, 34, 99)));
        samples.put("\u05e8", draw(path(17, 34, 41, 25, 65, 31, 83, 46, 86, 65, 80, 82)));
        samples.put("\u05e9", draw(path(20, 69, 41, 52, 55, 37, 48, 26, 35, 29,
                20, 45, 15, 65, 23, 82, 42, 91, 66, 79, 89, 58)));
        samples.put("\u05ea", draw(path(10, 95, 36, 84, 54, 70, 65, 49, 66, 33,
                57, 28, 70, 32, 87, 41, 94, 55, 88, 66)));
        return samples;
    }

    private static HandwritingRecognizer.Example first(String label) {
        for (HandwritingRecognizer.Example example : HebrewScriptSamples.examples()) {
            if (example.label.equals(label)) return example;
        }
        throw new AssertionError(label);
    }

    private static HandwritingRecognizer.Result recognize(Ink ink, boolean mirrored) {
        return HandwritingRecognizer.recognize(ink, Alphabet.HEBREW, Collections.emptyList(), mirrored);
    }

    private static HandwritingRecognizer.Candidate intended(HandwritingRecognizer.Result result, String label) {
        for (HandwritingRecognizer.Candidate candidate : result.candidates) if (candidate.label.equals(label)) return candidate;
        return null;
    }

    private static String hex(String label) { return Integer.toHexString(label.charAt(0)); }

    private static void assertWord(String expected, Ink ink, boolean mirrored, boolean reverse) {
        WordRecognizer.Result result = WordRecognizer.recognize(ink, Alphabet.HEBREW, Collections.emptyMap(), mirrored, reverse);
        assertFalse("Needs separation for " + expected, result.needsSeparation);
        assertEquals(expected, result.text);
        assertEquals(expected.length(), result.characters.size());
        int strokes = 0;
        for (int i = 0; i < result.characters.size(); i++) {
            WordRecognizer.CharacterResult character = result.characters.get(i);
            assertEquals(expected.substring(i, i + 1), character.label);
            strokes += character.ink.strokes.size();
        }
        assertEquals(ink.strokes.size(), strokes);
    }

    private static Ink compose(Ink... glyphs) {
        List<List<Ink.Point>> strokes = new ArrayList<>();
        float x = 10;
        for (Ink glyph : glyphs) {
            float min = Float.MAX_VALUE, max = -Float.MAX_VALUE, height = 0;
            for (List<Ink.Point> stroke : glyph.strokes) for (Ink.Point p : stroke) {
                min = Math.min(min, p.x);
                max = Math.max(max, p.x);
                height = Math.max(height, p.y);
            }
            float scale = height < 2 ? 100 : 1;
            strokes.addAll(transform(glyph, scale, x - min * scale, 20).strokes);
            x += (max - min) * scale + 25;
        }
        return new Ink(strokes);
    }

    private static Ink transform(Ink ink, float scale, float dx, float dy) {
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
            for (int i = 0; i < path.length; i += 2) points.add(new Ink.Point(path[i], path[i + 1]));
            strokes.add(points);
        }
        return new Ink(strokes);
    }
}
