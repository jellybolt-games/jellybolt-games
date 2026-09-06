package com.jellybolt.handwriting;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import com.jellybolt.handwriting.core.Alphabet;
import com.jellybolt.handwriting.core.Ink;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ProfileStoreTest {
    private ProfileStore store;
    private Context context;
    private final Ink ink = new Ink(Collections.singletonList(
            Arrays.asList(new Ink.Point(1, 2), new Ink.Point(3, 4))));

    @Before public void setUp() {
        context = RuntimeEnvironment.getApplication();
        context.deleteDatabase("personal-handwriting.db");
        store = new ProfileStore(context);
    }

    @After public void tearDown() {
        store.close();
        context.deleteDatabase("personal-handwriting.db");
    }

    @Test public void localProfilesAndExactSamplesSurviveClosingAndReopening() {
        ProfileStore.Profile profile = store.createProfile("Child A");
        store.addExample(profile.id, Alphabet.DIGITS, "3", ink);
        store.close();
        store = new ProfileStore(context);
        assertEquals("Child A", store.profiles().get(0).name);
        assertEquals("3", store.examples(profile.id, Alphabet.DIGITS).get(0).label);
        assertArrayEquals(ink.encode(), store.examples(profile.id, Alphabet.DIGITS).get(0).ink.encode());
    }

    @Test public void profilesAndAlphabetsAreIsolated() {
        ProfileStore.Profile a = store.createProfile("A");
        ProfileStore.Profile b = store.createProfile("B");
        store.addExample(a.id, Alphabet.DIGITS, "0", ink);
        store.addExample(b.id, Alphabet.HEBREW, "\u05da", ink);
        assertEquals(1, store.examples(a.id, Alphabet.DIGITS).size());
        assertTrue(store.examples(b.id, Alphabet.DIGITS).isEmpty());
        assertTrue(store.examples(a.id, Alphabet.HEBREW).isEmpty());
        assertEquals("\u05da", store.examples(b.id, Alphabet.HEBREW).get(0).label);
    }

    @Test public void deletesChildSamplesWithoutAffectingOtherProfiles() {
        ProfileStore.Profile a = store.createProfile("A");
        ProfileStore.Profile b = store.createProfile("B");
        store.addExample(a.id, Alphabet.DIGITS, "0", ink);
        store.addExample(b.id, Alphabet.DIGITS, "1", ink);
        store.deleteProfile(a.id);
        assertTrue(store.examples(a.id, Alphabet.DIGITS).isEmpty());
        assertEquals(1, store.examples(b.id, Alphabet.DIGITS).size());
        assertEquals(b.id, store.profiles().get(0).id);
        assertThrows(IllegalArgumentException.class, () -> store.deleteProfile(a.id));
    }

    @Test public void trainingUndoOnlyRemovesMostRecentSelectedClassExample() {
        long id = store.createProfile("A").id;
        store.addExample(id, Alphabet.DIGITS, "0", ink);
        store.addExample(id, Alphabet.DIGITS, "1", ink);
        store.addExample(id, Alphabet.DIGITS, "0", ink);
        long first = store.examples(id, Alphabet.DIGITS).get(0).id;
        assertTrue(store.deleteLastExample(id, Alphabet.DIGITS, "0"));
        assertEquals(2, store.examples(id, Alphabet.DIGITS).size());
        assertEquals(first, store.examples(id, Alphabet.DIGITS).get(0).id);
        assertFalse(store.deleteLastExample(id, Alphabet.DIGITS, "9"));
    }

    @Test public void preventsOrphanedExamplesAndMislabeledAlphabets() {
        assertThrows(SQLiteConstraintException.class, () -> store.addExample(999, Alphabet.DIGITS, "1", ink));
        long id = store.createProfile("A").id;
        assertThrows(IllegalArgumentException.class, () -> store.addExample(id, Alphabet.DIGITS, "A", ink));
        assertThrows(IllegalArgumentException.class, () -> store.addExample(id, Alphabet.DIGITS, "0",
                new Ink(Collections.emptyList())));
        assertTrue(store.examples(id, Alphabet.DIGITS).isEmpty());
    }

    @Test public void boundsSampleGrowthWithoutSilentlyDiscardingExistingTraining() {
        long id = store.createProfile("A").id;
        for (int i = 0; i < ProfileStore.MAX_EXAMPLES_PER_LABEL; i++) {
            store.addExample(id, Alphabet.DIGITS, "0", ink);
        }
        assertThrows(IllegalStateException.class, () -> store.addExample(id, Alphabet.DIGITS, "0", ink));
        assertEquals(200, store.examples(id, Alphabet.DIGITS).size());
        assertTrue(store.deleteLastExample(id, Alphabet.DIGITS, "0"));
        store.addExample(id, Alphabet.DIGITS, "0", ink);
        assertEquals(200, store.examples(id, Alphabet.DIGITS).size());
    }

    @Test public void aliasesAreTrimmedUniqueAndBounded() {
        assertEquals("Child", store.createProfile("  Child  ").name);
        assertThrows(SQLiteConstraintException.class, () -> store.createProfile("Child"));
        assertThrows(IllegalArgumentException.class, () -> store.createProfile(" "));
        assertThrows(IllegalArgumentException.class, () -> store.createProfile("x".repeat(41)));
        for (int i = 1; i < ProfileStore.MAX_PROFILES; i++) store.createProfile("Child " + i);
        assertThrows(IllegalStateException.class, () -> store.createProfile("One too many"));
        assertEquals(20, store.profiles().size());
    }
}
