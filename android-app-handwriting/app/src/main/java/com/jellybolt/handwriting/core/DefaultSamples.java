package com.jellybolt.handwriting.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Original, hand-authored pen centerlines, not font outlines, a downloaded
 * dataset, or a trained neural model. Coordinates use a unit square with y
 * increasing downwards. Only position and uniform scale are normalized by the
 * recognizer: case, proportions, reflection and orientation remain meaningful.
 * These immutable starters are never written to a child's profile.
 *
 * תבניות קו מקוריות שנכתבו ידנית, ללא מודל עצבי או הורדות.
 * הדוגמאות המובנות אינן נשמרות בפרופיל האישי של הילד.
 */
public final class DefaultSamples {
    private DefaultSamples() {}

    public static List<HandwritingRecognizer.Example> examples(String group) {
        if (!Alphabet.isGroup(group)) throw new IllegalArgumentException("Unknown alphabet: " + group);
        return Samples.BY_GROUP.get(group);
    }

    /** Cached script-family examples, also included after the original Hebrew print examples. */
    public static List<HandwritingRecognizer.Example> hebrewScriptExamples() {
        return HebrewScriptSamples.examples();
    }

    /** Optional left/right reflections retain the child's intended character label. */
    public static List<HandwritingRecognizer.Example> examples(String group, boolean mirrored) {
        if (!mirrored) return examples(group);
        if (!Alphabet.isGroup(group)) throw new IllegalArgumentException("Unknown alphabet: " + group);
        return MirroredSamples.get().get(group);
    }

    public static Ink mirror(Ink ink) {
        if (ink == null || ink.isEmpty()) throw new IllegalArgumentException("Draw a character first");
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        for (List<Ink.Point> stroke : ink.strokes) {
            checkCancelled();
            for (Ink.Point point : stroke) {
                minX = Math.min(minX, point.x);
                maxX = Math.max(maxX, point.x);
            }
        }
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (List<Ink.Point> stroke : ink.strokes) {
            checkCancelled();
            List<Ink.Point> points = new ArrayList<>();
            for (Ink.Point point : stroke) {
                points.add(new Ink.Point((float) ((double) minX + maxX - point.x), point.y));
            }
            strokes.add(points);
        }
        return new Ink(strokes);
    }

    private static void checkCancelled() {
        if (Thread.currentThread().isInterrupted()) {
            throw new java.util.concurrent.CancellationException("Recognition cancelled");
        }
    }

    private static final class Samples {
        static final Map<String, List<HandwritingRecognizer.Example>> BY_GROUP = create();
    }

    private static final class MirroredSamples {
        private static Map<String, List<HandwritingRecognizer.Example>> byGroup;

        static synchronized Map<String, List<HandwritingRecognizer.Example>> get() {
            // Cancellation during the first request must not poison a class initializer.
            if (byGroup == null) byGroup = createMirrored();
            return byGroup;
        }
    }

    private static Map<String, List<HandwritingRecognizer.Example>> createMirrored() {
        Map<String, List<HandwritingRecognizer.Example>> groups = new LinkedHashMap<>();
        for (Map.Entry<String, List<HandwritingRecognizer.Example>> entry : Samples.BY_GROUP.entrySet()) {
            List<HandwritingRecognizer.Example> examples = new ArrayList<>(entry.getValue());
            for (HandwritingRecognizer.Example original : entry.getValue()) {
                // The original ids start at -1; this separate range is global across alphabets.
                examples.add(new HandwritingRecognizer.Example(Long.MIN_VALUE - original.id,
                        original.label, mirror(original.ink)));
            }
            groups.put(entry.getKey(), Collections.unmodifiableList(examples));
        }
        return Collections.unmodifiableMap(groups);
    }

    private static Map<String, List<HandwritingRecognizer.Example>> create() {
        Builder b = new Builder();
        digits(b);
        upper(b);
        lower(b);
        hebrew(b);
        Map<String, List<HandwritingRecognizer.Example>> groups = new LinkedHashMap<>(b.finish());
        List<HandwritingRecognizer.Example> hebrew = new ArrayList<>(groups.get(Alphabet.HEBREW));
        hebrew.addAll(hebrewScriptExamples());
        groups.put(Alphabet.HEBREW, Collections.unmodifiableList(hebrew));
        return Collections.unmodifiableMap(groups);
    }

    private static void digits(Builder b) {
        b.group(Alphabet.DIGITS);
        b.add("0", oval(.5, .5, .31, .44));
        b.add("0", line(.46, .07, .25, .16, .17, .43, .23, .79, .43, .94,
                .68, .87, .8, .62, .77, .28, .61, .08, .46, .07));
        b.add("1", line(.5, .08, .5, .93));
        b.add("1", line(.27, .29, .52, .08, .52, .92), line(.26, .92, .76, .92));
        b.add("2", line(.18, .26, .25, .13, .44, .07, .64, .1, .78, .24,
                .75, .4, .57, .57, .19, .92, .81, .92));
        b.add("2", line(.17, .23, .34, .08, .58, .08, .76, .23, .7, .42,
                .2, .86, .23, .94, .49, .87, .79, .94));
        b.add("3", line(.2, .14, .46, .07, .67, .13, .78, .27, .69, .42,
                .48, .49, .68, .53, .8, .69, .74, .86, .52, .95, .23, .87));
        b.add("3", line(.19, .09, .79, .09, .46, .44, .66, .46, .8, .6,
                .78, .79, .63, .93, .38, .94, .17, .84));
        b.add("4", line(.65, .08, .17, .64, .84, .64), line(.66, .08, .66, .94));
        b.add("4", line(.25, .08, .2, .59, .81, .59), line(.66, .22, .66, .94));
        b.add("5", line(.78, .09, .25, .09, .21, .46, .47, .41, .68, .47,
                .79, .64, .74, .84, .54, .94, .29, .91, .15, .79));
        b.add("5", line(.27, .08, .23, .48, .48, .43, .7, .5, .78, .68,
                .66, .88, .44, .94, .18, .86), line(.27, .08, .8, .08));
        b.add("6", line(.73, .1, .54, .07, .34, .2, .2, .43, .18, .68,
                .28, .88, .49, .94, .7, .86, .79, .67, .7, .49,
                .5, .43, .29, .51, .18, .68));
        b.add("6", line(.72, .07, .46, .2, .25, .48, .22, .74, .36, .92,
                .61, .93, .78, .76, .73, .55, .55, .45, .34, .49, .23, .64));
        b.add("7", line(.16, .09, .82, .09, .37, .94));
        b.add("7", line(.17, .22, .18, .09, .81, .09, .38, .94),
                line(.3, .51, .71, .51));
        b.add("8", line(.48, .49, .26, .37, .24, .2, .39, .08, .62, .09,
                .76, .23, .7, .4, .48, .49, .25, .62, .19, .79,
                .34, .93, .61, .94, .79, .8, .75, .63, .48, .49));
        b.add("8", oval(.5, .28, .25, .21), oval(.5, .72, .31, .23));
        b.add("9", line(.76, .37, .67, .14, .48, .07, .28, .15, .19, .34,
                .27, .51, .48, .56, .68, .49, .76, .3, .76, .62, .65, .85, .42, .95));
        b.add("9", oval(.46, .3, .27, .23), line(.73, .28, .73, .94));
    }

    private static void upper(Builder b) {
        b.group(Alphabet.ENGLISH_UPPER);
        b.add("A", line(.12, .94, .5, .07, .88, .94), line(.27, .61, .73, .61));
        b.add("A", line(.13, .94, .48, .09, .56, .09, .87, .94),
                line(.28, .59, .74, .59));
        b.add("B", line(.22, .93, .22, .08, .53, .08, .73, .17, .76, .31,
                .66, .44, .22, .48, .58, .48, .78, .58, .8, .76,
                .67, .9, .51, .93, .22, .93));
        b.add("C", line(.81, .2, .66, .09, .42, .07, .22, .2, .13, .48,
                .19, .77, .4, .93, .66, .91, .81, .8));
        b.add("D", line(.2, .94, .2, .08, .49, .08, .7, .2, .83, .45,
                .78, .71, .61, .88, .4, .94, .2, .94));
        b.add("E", line(.8, .08, .2, .08, .2, .93, .81, .93),
                line(.2, .49, .69, .49));
        b.add("F", line(.22, .94, .22, .08, .82, .08), line(.22, .48, .7, .48));
        b.add("G", line(.8, .21, .62, .09, .4, .08, .2, .24, .13, .53,
                .23, .79, .42, .93, .67, .88, .81, .75, .81, .53, .52, .53));
        b.add("G", line(.78, .2, .61, .08, .38, .09, .2, .27, .15, .57,
                .26, .85, .46, .94, .7, .86, .78, .7),
                line(.51, .52, .83, .52, .83, .94));
        b.add("H", line(.18, .08, .18, .94), line(.82, .08, .82, .94),
                line(.18, .49, .82, .49));
        b.add("I", line(.23, .08, .77, .08), line(.5, .08, .5, .94),
                line(.23, .94, .77, .94));
        b.add("I", line(.5, .08, .5, .94));
        b.add("J", line(.24, .09, .8, .09), line(.67, .09, .67, .74,
                .59, .89, .43, .95, .26, .89, .17, .74));
        b.add("J", line(.72, .08, .72, .73, .64, .91, .44, .94, .25, .82, .21, .67));
        b.add("K", line(.2, .08, .2, .94), line(.81, .08, .2, .55, .83, .94));
        b.add("L", line(.23, .08, .23, .93, .82, .93));
        b.add("M", line(.12, .94, .12, .08, .5, .6, .88, .08, .88, .94));
        b.add("M", line(.1, .94, .21, .08, .5, .69, .79, .08, .9, .94));
        b.add("N", line(.18, .94, .18, .08, .82, .94, .82, .08));
        b.add("O", oval(.5, .51, .35, .44));
        b.add("P", line(.22, .94, .22, .08, .56, .08, .76, .18, .79, .36,
                .65, .5, .43, .53, .22, .53));
        b.add("Q", oval(.47, .46, .32, .39), line(.55, .68, .86, .95));
        b.add("R", line(.21, .94, .21, .08, .56, .08, .77, .2, .77, .37,
                .61, .5, .21, .51), line(.5, .51, .83, .94));
        b.add("S", line(.81, .19, .63, .08, .4, .08, .22, .21, .2, .37,
                .36, .49, .65, .57, .8, .7, .76, .85, .57, .94, .34, .92, .16, .81));
        b.add("T", line(.12, .09, .88, .09), line(.5, .09, .5, .94));
        b.add("U", line(.18, .08, .18, .71, .27, .89, .49, .95,
                .71, .9, .82, .73, .82, .08));
        b.add("V", line(.12, .08, .49, .94, .88, .08));
        b.add("W", line(.08, .08, .27, .94, .5, .35, .73, .94, .93, .08));
        b.add("W", line(.07, .08, .24, .94, .5, .08, .76, .94, .93, .08));
        b.add("X", line(.15, .08, .85, .94), line(.85, .08, .15, .94));
        b.add("Y", line(.13, .08, .5, .5, .87, .08), line(.5, .5, .5, .94));
        b.add("Z", line(.16, .09, .84, .09, .16, .93, .84, .93));
        b.add("Z", line(.16, .09, .84, .09, .16, .93, .84, .93),
                line(.32, .51, .68, .51));
    }

    private static void lower(Builder b) {
        b.group(Alphabet.ENGLISH_LOWER);
        // Single-storey and double-storey a/g are distinct pen paths, not jittered copies.
        b.add("a", oval(.43, .6, .25, .31), line(.68, .3, .68, .9, .8, .91));
        b.add("a", line(.22, .33, .39, .22, .59, .25, .71, .4, .71, .91),
                line(.71, .51, .49, .46, .27, .53, .2, .7, .27, .85,
                        .47, .9, .71, .76));
        b.add("b", line(.22, .07, .22, .93), line(.22, .47, .4, .32,
                .61, .33, .77, .48, .8, .68, .69, .86, .46, .93, .22, .83));
        b.add("c", line(.79, .34, .58, .24, .36, .28, .2, .43, .17, .64,
                .27, .83, .49, .92, .72, .86));
        b.add("d", oval(.44, .63, .26, .29), line(.7, .07, .7, .92));
        b.add("e", line(.19, .56, .78, .56, .74, .38, .56, .26, .35, .3,
                .2, .47, .19, .69, .34, .86, .56, .92, .78, .8));
        b.add("f", line(.78, .13, .63, .07, .47, .17, .42, .38, .42, .95),
                line(.19, .4, .7, .4));
        b.add("f", line(.78, .12, .64, .06, .48, .12, .43, .3, .43, .83,
                .35, .95, .23, .9), line(.21, .39, .72, .39));
        b.add("g", oval(.43, .37, .25, .25), line(.68, .13, .68, .75,
                .61, .9, .44, .96, .24, .87));
        b.add("g", oval(.44, .29, .23, .2), line(.63, .14, .81, .08),
                line(.33, .47, .26, .59, .43, .66, .63, .68, .73, .79,
                        .66, .92, .43, .96, .23, .88, .21, .75, .34, .66));
        b.add("h", line(.21, .07, .21, .94), line(.21, .52, .37, .36,
                .56, .34, .72, .44, .76, .61, .76, .94));
        b.add("i", line(.5, .36, .5, .92), line(.5, .12, .5, .14));
        b.add("i", line(.46, .36, .46, .82, .53, .91, .66, .88),
                line(.46, .12, .46, .14));
        b.add("j", line(.63, .32, .63, .75, .55, .91, .39, .95, .23, .84),
                line(.63, .09, .63, .11));
        b.add("k", line(.23, .07, .23, .94), line(.73, .34, .23, .66, .79, .94));
        b.add("l", line(.49, .07, .49, .92));
        b.add("l", line(.42, .07, .42, .8, .49, .92, .65, .89));
        b.add("m", line(.1, .92, .1, .3, .1, .49, .25, .31, .4, .34, .48, .5, .48, .92),
                line(.48, .5, .61, .32, .77, .33, .9, .48, .9, .92));
        b.add("n", line(.2, .92, .2, .3, .2, .49, .38, .31, .58, .31,
                .75, .44, .8, .62, .8, .92));
        b.add("o", oval(.49, .59, .31, .32));
        b.add("p", line(.23, .12, .23, .95), line(.23, .25, .42, .12,
                .64, .15, .78, .31, .77, .48, .6, .62, .4, .62, .23, .5));
        b.add("q", oval(.44, .36, .26, .25), line(.7, .12, .7, .95));
        b.add("q", oval(.44, .36, .26, .25), line(.7, .12, .7, .94, .87, .79));
        b.add("r", line(.25, .92, .25, .29, .25, .51, .41, .31, .6, .29, .76, .4));
        b.add("s", line(.77, .35, .6, .26, .39, .28, .24, .41, .29, .55,
                .55, .62, .73, .73, .68, .86, .49, .93, .26, .85));
        b.add("t", line(.45, .08, .45, .77, .52, .9, .69, .92, .79, .83),
                line(.19, .35, .75, .35));
        b.add("u", line(.19, .29, .19, .72, .29, .88, .47, .92, .64, .83,
                .77, .65), line(.77, .29, .77, .92));
        b.add("v", line(.17, .28, .49, .92, .83, .28));
        b.add("w", line(.07, .29, .26, .92, .49, .42, .72, .92, .93, .29));
        b.add("w", line(.08, .28, .22, .83, .31, .92, .41, .85, .51, .43,
                .65, .85, .75, .92, .84, .8, .93, .28));
        b.add("x", line(.18, .28, .81, .92), line(.81, .28, .18, .92));
        b.add("y", line(.17, .12, .48, .62, .77, .12),
                line(.77, .12, .49, .73, .33, .92, .18, .95));
        b.add("y", line(.2, .13, .2, .42, .31, .57, .49, .59, .7, .44),
                line(.7, .13, .7, .73, .58, .9, .4, .96, .23, .88));
        b.add("z", line(.19, .3, .81, .3, .19, .91, .81, .91));
        b.add("z", line(.19, .3, .81, .3, .19, .91, .81, .91),
                line(.33, .6, .66, .6));
    }

    private static void hebrew(Builder b) {
        b.group(Alphabet.HEBREW);
        // Recognizable unpointed print centerlines. Descenders retain proportions
        // relative to the letter body; originals here are never reflected in place.
        b.add("\u05d0", line(.2, .14, .8, .9),
                line(.76, .13, .68, .4, .49, .5),
                line(.43, .46, .3, .58, .21, .9)); // alef
        b.add("\u05d1", line(.19, .2, .72, .2, .8, .31, .8, .83),
                line(.13, .85, .9, .85)); // bet: base extends past the right stem
        b.add("\u05d2", line(.4, .18, .62, .2, .63, .66, .81, .88),
                line(.62, .61, .39, .83, .18, .9)); // gimel
        b.add("\u05d3", line(.12, .2, .87, .2), line(.71, .2, .71, .9)); // dalet
        b.add("\u05d4", line(.17, .2, .76, .2, .8, .28, .8, .9),
                line(.22, .47, .22, .9)); // he: left leg is detached
        b.add("\u05d5", line(.38, .17, .58, .2, .61, .32, .61, .9)); // vav
        b.add("\u05d6", line(.28, .18, .76, .18),
                line(.57, .18, .48, .43, .48, .9)); // zayin
        b.add("\u05d7", line(.21, .9, .21, .2, .8, .2, .8, .9)); // het
        b.add("\u05d8", line(.2, .2, .2, .67, .3, .85, .53, .92, .75, .8,
                .81, .58, .78, .31, .65, .2, .5, .29, .46, .48)); // tet
        b.add("\u05d9", line(.34, .2, .63, .24, .65, .39, .59, .53)); // yod
        b.add("\u05db", line(.19, .2, .6, .2, .77, .32, .82, .53,
                .77, .73, .61, .86, .18, .86)); // kaf
        b.add("\u05da", line(.22, .13, .65, .13, .72, .22, .72, .96)); // final kaf
        b.add("\u05dc", line(.25, .06, .25, .35, .8, .35, .78, .58,
                .61, .78, .43, .93)); // lamed
        b.add("\u05de", line(.14, .91, .33, .33, .47, .21, .7, .24,
                .81, .44, .81, .9, .49, .9),
                line(.2, .21, .34, .39, .43, .52)); // mem, open at lower left
        b.add("\u05dd", line(.2, .89, .2, .2, .78, .2, .8, .89, .2, .89)); // final mem
        b.add("\u05e0", line(.43, .2, .69, .2, .7, .79, .63, .89, .19, .89)); // nun
        b.add("\u05df", line(.43, .1, .57, .13, .57, .96)); // final nun
        b.add("\u05e1", line(.25, .2, .69, .2, .82, .34, .81, .65,
                .67, .84, .46, .91, .25, .79, .17, .55, .19, .3, .25, .2)); // samekh
        b.add("\u05e2", line(.23, .19, .38, .65, .51, .82),
                line(.78, .2, .73, .58, .58, .8, .36, .91, .15, .91)); // ayin
        b.add("\u05e4", line(.48, .51, .26, .51, .21, .36, .29, .22,
                .62, .2, .8, .34, .83, .59, .75, .8, .55, .9, .18, .9)); // pe
        b.add("\u05e3", line(.46, .43, .24, .43, .21, .27, .3, .12,
                .61, .12, .74, .26, .76, .96)); // final pe
        b.add("\u05e6", line(.22, .18, .4, .47, .77, .84, .17, .88),
                line(.76, .18, .66, .4, .45, .51)); // tsadi
        b.add("\u05e5", line(.28, .1, .48, .38, .57, .5, .57, .96),
                line(.81, .12, .7, .32, .48, .38)); // final tsadi
        b.add("\u05e7", line(.16, .16, .81, .16, .78, .46, .67, .64, .51, .71),
                line(.27, .39, .27, .96)); // qof, left descender detached
        b.add("\u05e8", line(.2, .2, .59, .2, .77, .27, .81, .42, .81, .9)); // resh
        b.add("\u05e9", line(.13, .2, .2, .68, .34, .87, .55, .89,
                .77, .72, .87, .2), line(.49, .2, .46, .55, .3, .78)); // shin
        b.add("\u05ea", line(.17, .2, .73, .2, .8, .32, .8, .89),
                line(.29, .2, .29, .78, .21, .89, .1, .89)); // tav, left foot

        // A few confidently distinct common forms: angular kaf, curved lamed,
        // a rounder samekh and a three-pronged handwritten shin.
        b.add("\u05db", line(.19, .2, .79, .2, .79, .86, .19, .86));
        b.add("\u05dc", line(.25, .07, .23, .33, .33, .39, .73, .39,
                .82, .46, .75, .64, .58, .83, .38, .93));
        b.add("\u05e1", line(.31, .2, .64, .2, .79, .3, .85, .5, .78, .74,
                .6, .89, .36, .86, .19, .67, .16, .44, .22, .27, .31, .2));
        b.add("\u05e9", line(.13, .2, .19, .71, .31, .88, .69, .88, .8, .72, .87, .2),
                line(.5, .2, .5, .87));
    }

    private static List<Ink.Point> line(double... xy) {
        if (xy.length < 4 || xy.length % 2 != 0) {
            throw new IllegalArgumentException("A starter stroke needs coordinate pairs");
        }
        List<Ink.Point> points = new ArrayList<>(xy.length / 2);
        for (int i = 0; i < xy.length; i += 2) {
            if (!Double.isFinite(xy[i]) || !Double.isFinite(xy[i + 1])
                    || xy[i] < 0 || xy[i] > 1 || xy[i + 1] < 0 || xy[i + 1] > 1) {
                throw new IllegalArgumentException("Starter coordinates must be in the unit square");
            }
            points.add(new Ink.Point((float) xy[i], (float) xy[i + 1]));
        }
        return points;
    }

    private static List<Ink.Point> oval(double cx, double cy, double rx, double ry) {
        double[] xy = new double[50];
        for (int i = 0; i <= 24; i++) {
            double angle = i * Math.PI / 12;
            xy[i * 2] = cx + rx * Math.cos(angle);
            xy[i * 2 + 1] = cy + ry * Math.sin(angle);
        }
        return line(xy);
    }

    private static final class Builder {
        private final Map<String, List<HandwritingRecognizer.Example>> groups = new LinkedHashMap<>();
        private String currentGroup;
        private long nextId = -1;

        void group(String group) {
            if (!Alphabet.isGroup(group) || groups.containsKey(group)) {
                throw new IllegalArgumentException("Invalid or duplicate starter alphabet");
            }
            currentGroup = group;
            groups.put(group, new ArrayList<>());
        }

        @SafeVarargs
        final void add(String label, List<Ink.Point>... strokes) {
            if (currentGroup == null || !Alphabet.labels(currentGroup).contains(label)) {
                throw new IllegalArgumentException("Starter label is outside its alphabet");
            }
            Ink ink = new Ink(Arrays.asList(strokes));
            groups.get(currentGroup).add(new HandwritingRecognizer.Example(nextId--, label, ink));
        }

        Map<String, List<HandwritingRecognizer.Example>> finish() {
            for (String group : Arrays.asList(Alphabet.DIGITS, Alphabet.ENGLISH_UPPER,
                    Alphabet.ENGLISH_LOWER, Alphabet.HEBREW)) {
                List<HandwritingRecognizer.Example> examples = groups.get(group);
                if (examples == null) throw new IllegalStateException("Missing starter alphabet: " + group);
                Set<String> labels = new HashSet<>();
                for (HandwritingRecognizer.Example example : examples) labels.add(example.label);
                if (!labels.equals(new HashSet<>(Alphabet.labels(group)))) {
                    throw new IllegalStateException("Incomplete starter alphabet: " + group);
                }
                groups.put(group, Collections.unmodifiableList(new ArrayList<>(examples)));
            }
            return Collections.unmodifiableMap(new LinkedHashMap<>(groups));
        }
    }
}
