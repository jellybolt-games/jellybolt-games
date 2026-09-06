package com.jellybolt.handwriting;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.jellybolt.handwriting.core.Alphabet;
import com.jellybolt.handwriting.core.HandwritingRecognizer;
import com.jellybolt.handwriting.core.Ink;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/** Run only on a disposable emulator: Gradle may uninstall the app after these non-UI checks. */
@RunWith(AndroidJUnit4.class)
public class DeviceRecognitionTest {
    private static final String DATABASE = "instrumented-validation.db";
    private Context context;
    private ProfileStore store;
    private long child;
    private final Ink normal = ink(0, 0, 80, 0, 100, 20, 50, 50, 100, 80, 80, 100, 0, 100);
    private final Ink mirrored = ink(100, 0, 20, 0, 0, 20, 50, 50, 0, 80, 20, 100, 100, 100);
    private final Ink line = ink(10, 0, 10, 100);

    @Before public void before() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(DATABASE);
        store = new ProfileStore(context, DATABASE);
        child = store.createProfile("Device test only").id;
    }

    @After public void after() {
        store.close();
        context.deleteDatabase(DATABASE);
    }

    @Test public void learnsBothPersonalVariantsAndPersistsAfterReopeningDatabase() {
        for (int i = 0; i < 25; i++) {
            store.addExample(child, Alphabet.DIGITS, "3", normal);
            store.addExample(child, Alphabet.DIGITS, "3", mirrored);
            store.addExample(child, Alphabet.DIGITS, "1", line);
        }
        store.close();
        store = new ProfileStore(context, DATABASE);
        List<HandwritingRecognizer.Example> examples = store.examples(child, Alphabet.DIGITS);
        assertEquals(75, examples.size());
        assertEquals("3", HandwritingRecognizer.recognize(normal, examples).candidates.get(0).label);
        assertEquals("3", HandwritingRecognizer.recognize(mirrored, examples).candidates.get(0).label);
        assertEquals("1", HandwritingRecognizer.recognize(line, examples).candidates.get(0).label);
    }

    @Test public void allCharacterGroupsAcceptFiftyExamplesPerLabel() {
        String[] groups = {Alphabet.DIGITS, Alphabet.ENGLISH_UPPER, Alphabet.ENGLISH_LOWER, Alphabet.HEBREW};
        int total = 0;
        for (String group : groups) {
            for (String label : Alphabet.labels(group)) {
                for (int i = 0; i < 50; i++) store.addExample(child, group, label, normal);
            }
            List<HandwritingRecognizer.Example> examples = store.examples(child, group);
            assertEquals(Alphabet.labels(group).size() * 50, examples.size());
            HandwritingRecognizer.Result result = HandwritingRecognizer.recognize(normal, examples);
            assertEquals(Alphabet.labels(group).size(), result.trainedLabels);
            assertTrue("Identical shapes for different labels must stay ambiguous", result.uncertain);
            total += examples.size();
        }
        assertEquals(4450, total);
    }

    @Test public void correctionsRemainIsolatedByChildAndAlphabet() {
        long otherChild = store.createProfile("Separate device test").id;
        store.addExample(child, Alphabet.HEBREW, "\u05da", normal);
        store.addExample(child, Alphabet.HEBREW, "\u05dd", mirrored);
        store.addExample(otherChild, Alphabet.DIGITS, "7", mirrored);
        assertTrue(store.examples(otherChild, Alphabet.HEBREW).isEmpty());
        assertEquals("\u05dd", HandwritingRecognizer.recognize(mirrored,
                store.examples(child, Alphabet.HEBREW)).candidates.get(0).label);
        store.deleteProfile(child);
        assertTrue(store.examples(child, Alphabet.HEBREW).isEmpty());
        assertEquals(1, store.examples(otherChild, Alphabet.DIGITS).size());
    }

    @Test public void rawInkKeepsSeparatePenLiftsOnDevice() {
        Ink separate = new Ink(Arrays.asList(normal.strokes.get(0), line.strokes.get(0)));
        store.addExample(child, Alphabet.ENGLISH_UPPER, "A", separate);
        Ink restored = store.examples(child, Alphabet.ENGLISH_UPPER).get(0).ink;
        assertEquals(2, restored.strokes.size());
        assertArrayEquals(separate.encode(), restored.encode());
    }

    private static Ink ink(float... coordinates) {
        List<Ink.Point> stroke = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i += 2) {
            stroke.add(new Ink.Point(coordinates[i], coordinates[i + 1]));
        }
        return new Ink(Collections.singletonList(stroke));
    }
}
