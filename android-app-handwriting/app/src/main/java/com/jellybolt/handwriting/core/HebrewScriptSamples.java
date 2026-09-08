package com.jellybolt.handwriting.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Original pen centerlines for ordinary modern Israeli handwriting (כתב יד).
 * Letter topology was checked against the contemporary, not historical, chart:
 * https://en.wikipedia.org/wiki/Cursive_Hebrew#Contemporary_forms
 * Coordinates are authored here, not traced outlines, font assets, or downloaded
 * training data. Script and print share the same Unicode Hebrew labels.
 *
 * תבניות קו מקוריות לכתב יד עברי מודרני, כולל אותיות סופיות.
 * הן מצטרפות לאותיות הדפוס באותה קבוצת עברית ואינן נשמרות בפרופיל.
 */
public final class HebrewScriptSamples {
    private HebrewScriptSamples() {}

    /** Read-only script-family fixtures; DefaultSamples also includes these objects. */
    public static List<HandwritingRecognizer.Example> examples() {
        return Samples.EXAMPLES;
    }

    private static final class Samples {
        static final List<HandwritingRecognizer.Example> EXAMPLES = create();
    }

    private static List<HandwritingRecognizer.Example> create() {
        List<HandwritingRecognizer.Example> samples = new ArrayList<>();
        // Alef: a long rising diagonal, with a separate small open bowl on its right.
        add(samples, "\u05d0", path(.15, .91, .3, .54, .49, .11),
                path(.77, .59, .58, .64, .42, .75, .45, .86, .63, .92, .87, .88));
        // Bet's returning lower stroke, gimel's lower bowl, and dalet's small knot
        // are deliberately not rounded versions of their square-print skeletons.
        add(samples, "\u05d1", path(.2, .28, .41, .27, .62, .35, .76, .51,
                .8, .68, .72, .75, .51, .68, .35, .71, .17, .85));
        add(samples, "\u05d2", path(.28, .26, .45, .35, .53, .47, .47, .57,
                .29, .7, .2, .8, .25, .89, .42, .94, .64, .89));
        add(samples, "\u05d3", path(.16, .28, .37, .24, .64, .26, .82, .33,
                .7, .46, .52, .58, .41, .57, .39, .52, .49, .51,
                .58, .61, .6, .75, .54, .89));
        add(samples, "\u05d4", path(.19, .36, .39, .29, .63, .28, .8, .37,
                .86, .55, .83, .84), path(.4, .6, .5, .62, .53, .7, .5, .82));
        add(samples, "\u05d5", path(.48, .22, .51, .4, .5, .65, .45, .91));
        add(samples, "\u05d6", path(.71, .24, .55, .34, .49, .44, .53, .53,
                .68, .67, .69, .76, .54, .87, .33, .96, .14, .97));
        add(samples, "\u05d7", path(.28, .24, .26, .56, .19, .88),
                path(.21, .39, .42, .38, .66, .49, .77, .64, .77, .82));
        add(samples, "\u05d8", path(.77, .1, .61, .28, .44, .57, .32, .81,
                .34, .92, .47, .93, .65, .84, .83, .65));
        add(samples, "\u05d9", path(.56, .3, .54, .38, .49, .48));
        add(samples, "\u05db", path(.24, .26, .45, .24, .65, .33, .76, .49,
                .73, .66, .59, .79, .4, .84, .21, .78));
        add(samples, "\u05da", path(.27, .2, .44, .16, .64, .23, .75, .35,
                .74, .44, .61, .54, .44, .62, .36, .76, .29, .97));
        // Lamed has a tall ascender with a small loop at its foot.
        add(samples, "\u05dc", path(.29, .65, .2, .71, .14, .86, .16, .94,
                .23, .91, .3, .77, .35, .39, .41, .19, .54, .09, .69, .07, .83, .12));
        add(samples, "\u05de", path(.13, .9, .29, .66, .43, .4, .51, .31,
                .58, .35, .68, .64, .76, .81, .81, .72, .89, .25));
        // Final mem: the closed bowl at the right has an entry tail from the left.
        add(samples, "\u05dd", path(.14, .85, .28, .7, .45, .59, .59, .57,
                .72, .7, .87, .62, .91, .46, .84, .32, .7, .28,
                .56, .34, .53, .44, .59, .57));
        add(samples, "\u05e0", path(.61, .25, .68, .44, .7, .55, .61, .67,
                .41, .81, .18, .91));
        add(samples, "\u05df", path(.55, .18, .54, .38, .49, .67, .42, .91, .45, .97));
        add(samples, "\u05e1", path(.8, .55, .76, .38, .65, .27, .48, .24,
                .32, .3, .22, .44, .2, .61, .27, .77, .42, .86,
                .59, .84, .74, .73, .8, .55));
        // Ayin crosses above its lower loop; pe curls inward rather than closing.
        add(samples, "\u05e2", path(.2, .28, .41, .32, .58, .44, .67, .66,
                .64, .83, .52, .9, .42, .84, .39, .69, .47, .47, .64, .3, .8, .29));
        add(samples, "\u05e4", path(.18, .37, .39, .28, .61, .31, .73, .46,
                .71, .62, .57, .79, .36, .87, .22, .81, .2, .71, .27, .59, .39, .49));
        // The long final pe and tsadi use upper and lower loops, with different arms.
        add(samples, "\u05e3", path(.25, .54, .18, .45, .24, .35, .43, .24,
                .64, .16, .82, .07, .86, .02, .75, .02, .63, .12,
                .57, .34, .55, .67, .48, .87, .39, .95, .33, .91,
                .35, .8, .45, .68, .55, .62));
        add(samples, "\u05e6", path(.28, .29, .49, .26, .67, .32, .74, .42,
                .65, .54, .54, .66, .57, .75, .66, .86, .55, .94, .34, .98, .15, .97));
        add(samples, "\u05e5", path(.22, .09, .19, .22, .24, .32, .37, .35, .48, .34),
                path(.48, .34, .52, .15, .65, .05, .79, .03, .88, .1,
                        .85, .21, .71, .31, .48, .4, .48, .7, .41, .87,
                        .29, .96, .22, .93, .24, .82, .37, .72, .48, .67));
        add(samples, "\u05e7", path(.19, .28, .38, .23, .59, .28, .75, .42, .79, .58),
                path(.47, .43, .43, .65, .36, .97));
        add(samples, "\u05e8", path(.2, .33, .41, .27, .62, .3, .78, .43, .83, .6, .8, .78));
        // Shin's small initial loop opens into a broad upward exit, like a cursive e.
        add(samples, "\u05e9", path(.22, .67, .39, .54, .53, .4, .52, .3,
                .43, .25, .31, .31, .2, .47, .17, .65, .24, .8,
                .43, .85, .63, .77, .85, .6));
        add(samples, "\u05ea", path(.14, .9, .36, .82, .53, .69, .63, .52,
                .66, .34, .58, .27, .68, .3, .83, .38, .91, .5, .88, .63));

        // Without a baseline or size context, plain vav, yod and final nun are
        // intrinsically indistinguishable. Keep all intended labels as tied options.
        add(samples, "\u05d5", path(.5, .24, .5, .88));
        add(samples, "\u05d9", path(.5, .28, .5, .46));
        add(samples, "\u05df", path(.5, .2, .5, .97));

        Set<String> labels = new HashSet<>();
        for (HandwritingRecognizer.Example sample : samples) labels.add(sample.label);
        if (!labels.equals(new HashSet<>(Alphabet.labels(Alphabet.HEBREW)))) {
            throw new IllegalStateException("Incomplete Hebrew handwriting family");
        }
        return Collections.unmodifiableList(samples);
    }

    private static List<Ink.Point> path(double... xy) {
        if (xy.length < 4 || xy.length % 2 != 0) throw new IllegalArgumentException("Invalid script stroke");
        List<Ink.Point> points = new ArrayList<>();
        for (int i = 0; i < xy.length; i += 2) {
            if (xy[i] < 0 || xy[i] > 1 || xy[i + 1] < 0 || xy[i + 1] > 1) {
                throw new IllegalArgumentException("Script coordinates must be in the unit square");
            }
            points.add(new Ink.Point((float) xy[i], (float) xy[i + 1]));
        }
        return points;
    }

    @SafeVarargs
    private static void add(List<HandwritingRecognizer.Example> samples, String label, List<Ink.Point>... strokes) {
        if (!Alphabet.labels(Alphabet.HEBREW).contains(label)) throw new IllegalArgumentException("Unknown Hebrew letter");
        samples.add(new HandwritingRecognizer.Example(-1001 - samples.size(), label, new Ink(Arrays.asList(strokes))));
    }
}
