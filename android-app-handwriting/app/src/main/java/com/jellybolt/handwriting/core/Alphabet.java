package com.jellybolt.handwriting.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Alphabet {
    public static final String DIGITS = "digits";
    public static final String ENGLISH_UPPER = "english-upper";
    public static final String ENGLISH_LOWER = "english-lower";
    public static final String HEBREW = "hebrew";

    private Alphabet() {}

    public static List<String> labels(String group) {
        String characters;
        switch (group) {
            case DIGITS: characters = "0123456789"; break;
            case ENGLISH_UPPER: characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; break;
            case ENGLISH_LOWER: characters = "abcdefghijklmnopqrstuvwxyz"; break;
            case HEBREW:
                characters = "\u05d0\u05d1\u05d2\u05d3\u05d4\u05d5\u05d6\u05d7\u05d8"
                        + "\u05d9\u05db\u05da\u05dc\u05de\u05dd\u05e0\u05df\u05e1\u05e2"
                        + "\u05e4\u05e3\u05e6\u05e5\u05e7\u05e8\u05e9\u05ea";
                break;
            default: throw new IllegalArgumentException("Unknown alphabet: " + group);
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < characters.length(); i++) {
            labels.add(characters.substring(i, i + 1));
        }
        return Collections.unmodifiableList(labels);
    }
}
