package com.jellybolt.handwriting.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

/**
 * Offline, single-line recognition of separated characters, not joined-cursive OCR.
 * Stroke projections preserve pen-up boundaries; no connectors are cut or invented.
 * זיהוי מקומי של תווים נפרדים, כולל אותיות כתב בעברית, לא של אותיות מחוברות.
 */
public final class WordRecognizer {
    public static final int MAX_CHARACTERS = 16;
    private static final int MAX_COMPONENTS_PER_CHARACTER = 4;
    private static final double MAX_ASPECT = 1.85;

    private WordRecognizer() {}

    public static List<String> personalGroups(String group) {
        if (!Alphabet.isGroup(group)) throw new IllegalArgumentException("Unknown alphabet: " + group);
        return Alphabet.DIGITS.equals(group) ? Collections.singletonList(group)
                : Collections.unmodifiableList(Arrays.asList(group, Alphabet.DIGITS));
    }

    public static final class CharacterResult {
        public final Ink ink;
        public final List<HandwritingRecognizer.Candidate> candidates;
        public final String label;
        public final boolean uncertain;

        private CharacterResult(Ink ink, HandwritingRecognizer.Result result) {
            this.ink = ink;
            candidates = Collections.unmodifiableList(new ArrayList<>(result.candidates));
            label = candidates.get(0).label;
            uncertain = result.uncertain;
        }
    }

    public static final class Result {
        public final String text;
        /** Logical insertion order, including the order of candidates and correction ink. */
        public final List<CharacterResult> characters;
        public final boolean uncertain;
        public final boolean needsSeparation;

        private Result(List<CharacterResult> characters, boolean needsSeparation, boolean ambiguousSplit) {
            this.characters = Collections.unmodifiableList(new ArrayList<>(characters));
            this.needsSeparation = needsSeparation;
            StringBuilder text = new StringBuilder();
            boolean uncertain = needsSeparation || ambiguousSplit;
            for (CharacterResult character : characters) {
                text.append(character.label);
                uncertain |= character.uncertain;
            }
            this.text = needsSeparation ? "" : text.toString();
            this.uncertain = uncertain;
        }
    }

    public static Result recognize(Ink ink, String group,
                                   Map<String, List<HandwritingRecognizer.Example>> personalByGroup,
                                   boolean mirrored, boolean reverseOrder) {
        checkCancelled();
        if (ink == null || ink.isEmpty()) throw new IllegalArgumentException("Draw a word first");
        List<String> groups = personalGroups(group);
        Map<String, List<HandwritingRecognizer.Example>> personal = validatePersonal(groups, personalByGroup);
        List<Part> strokes = new ArrayList<>();
        Bounds line = new Bounds();
        for (int i = 0; i < ink.strokes.size(); i++) {
            checkCancelled();
            Part part = new Part(i, ink.strokes.get(i));
            strokes.add(part);
            line.include(part);
        }
        if (line.width() == 0 && line.height() == 0) {
            throw new IllegalArgumentException("A dot is not a word");
        }
        if (line.height() == 0 || hasSeparateRows(strokes, line)) return rejected();

        List<Part> components = components(strokes, line.height() * .003);
        int count = components.size();
        // Each transition consumes at most four components. Never build an unbounded graph.
        if (count > MAX_CHARACTERS * MAX_COMPONENTS_PER_CHARACTER) return rejected();
        double[] costs = new double[count + 1];
        Arrays.fill(costs, Double.POSITIVE_INFINITY);
        costs[0] = 0;
        int[] previous = new int[count + 1];
        CharacterResult[] selected = new CharacterResult[count + 1];
        boolean[] ambiguous = new boolean[count + 1];
        for (int start = 0; start < count; start++) {
            checkCancelled();
            if (!Double.isFinite(costs[start])) continue;
            Bounds bounds = new Bounds();
            List<Integer> strokeIndices = new ArrayList<>();
            double joinPenalty = 0;
            for (int end = start; end < Math.min(count, start + MAX_COMPONENTS_PER_CHARACTER); end++) {
                checkCancelled();
                Part next = components.get(end);
                if (end > start && !canJoin(components.get(end - 1), next, line.height())) break;
                if (end > start && bothMainParts(components.get(end - 1), next, line.height())) {
                    joinPenalty += .3;
                }
                bounds.include(next);
                strokeIndices.addAll(next.indices);
                if (!plausible(bounds, line)) continue;
                Ink characterInk = subset(ink, strokeIndices);
                HandwritingRecognizer.Result recognition =
                        HandwritingRecognizer.recognizeGroups(characterInk, groups, personal, mirrored, true);
                int similarity = recognition.candidates.get(0).similarity;
                double error = 1 - similarity / 100.0;
                double cost = costs[start] + .08 + 4 * error * error + (end - start) * .04 + joinPenalty;
                if (end + 1 < count && canJoin(next, components.get(end + 1), line.height())
                        && !bothMainParts(next, components.get(end + 1), line.height())) {
                    // Tiny gaps beside narrow pen-up pieces favor a good whole-glyph fit,
                    // but cannot turn two confidently recognized full letters into one.
                    cost += .16;
                }
                if (cost < costs[end + 1]) {
                    boolean closeAlternative = Double.isFinite(costs[end + 1])
                            && costs[end + 1] - cost < .06;
                    costs[end + 1] = cost;
                    previous[end + 1] = start;
                    selected[end + 1] = new CharacterResult(characterInk, recognition);
                    ambiguous[end + 1] = ambiguous[start] || closeAlternative;
                } else if (cost - costs[end + 1] < .06) {
                    ambiguous[end + 1] = true;
                }
            }
        }
        if (selected[count] == null) return rejected();
        List<CharacterResult> characters = new ArrayList<>();
        for (int cursor = count; cursor > 0; cursor = previous[cursor]) {
            checkCancelled();
            characters.add(selected[cursor]);
        }
        if (characters.size() > MAX_CHARACTERS) return rejected();
        Collections.reverse(characters);
        if (!sameLine(characters, line.height())) return rejected();
        order(characters, group, reverseOrder);
        return new Result(characters, false, ambiguous[count]);
    }

    private static Map<String, List<HandwritingRecognizer.Example>> validatePersonal(
            List<String> groups, Map<String, List<HandwritingRecognizer.Example>> source) {
        if (source == null) throw new IllegalArgumentException("Personal groups are required");
        Map<String, List<HandwritingRecognizer.Example>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<HandwritingRecognizer.Example>> entry : source.entrySet()) {
            checkCancelled();
            if (!Alphabet.isGroup(entry.getKey()) || entry.getValue() == null) {
                throw new IllegalArgumentException("Invalid personal alphabet");
            }
            List<String> labels = Alphabet.labels(entry.getKey());
            for (HandwritingRecognizer.Example example : entry.getValue()) {
                checkCancelled();
                if (example == null || !labels.contains(example.label)) {
                    throw new IllegalArgumentException("Personal example is outside alphabet: " + entry.getKey());
                }
            }
        }
        for (String group : groups) {
            result.put(group, new ArrayList<>(source.getOrDefault(group, Collections.emptyList())));
        }
        return result;
    }

    private static List<Part> components(List<Part> strokes, double tolerance) {
        List<Part> sorted = new ArrayList<>(strokes);
        sorted.sort(Comparator.comparingDouble((Part p) -> p.minX).thenComparingDouble(p -> p.minY));
        List<Part> result = new ArrayList<>();
        for (Part part : sorted) {
            checkCancelled();
            if (result.isEmpty() || part.minX > result.get(result.size() - 1).maxX + tolerance) {
                result.add(new Part(part));
            } else {
                Part last = result.get(result.size() - 1);
                last.include(part);
                last.indices.addAll(part.indices);
            }
        }
        return result;
    }

    private static boolean canJoin(Part left, Part right, double height) {
        double gap = right.minX - left.maxX;
        if (gap <= height * .12 && (isSatellite(left, right, height) || isSatellite(right, left, height))) {
            return true;
        }
        return gap <= height * .045
                && (left.width() < height * .16 || right.width() < height * .16
                    || left.height() < height * .28 || right.height() < height * .28);
    }

    private static boolean isSatellite(Part small, Part main, double height) {
        return small.height() <= height * .08 && small.width() <= height * .12
                && main.height() >= height * .35
                && (small.maxY < main.minY || small.minY > main.maxY);
    }

    private static boolean bothMainParts(Part left, Part right, double height) {
        return left.height() >= height * .7 && right.height() >= height * .7;
    }

    private static boolean plausible(Bounds bounds, Bounds line) {
        if (bounds.height() <= 0 || bounds.width() > MAX_ASPECT * bounds.height()) return false;
        // An isolated dot/diacritic must attach to a main glyph, not become another digit.
        return bounds.height() >= line.height() * .18 || bounds.width() >= line.height() * .18;
    }

    private static boolean hasSeparateRows(List<Part> strokes, Bounds line) {
        List<Part> sorted = new ArrayList<>(strokes);
        sorted.sort(Comparator.comparingDouble(p -> p.minY));
        double bottom = sorted.get(0).maxY;
        for (int i = 1; i < sorted.size(); i++) {
            checkCancelled();
            Part next = sorted.get(i);
            if (next.minY - bottom > line.height() * .12
                    && bottom - line.minY > line.height() * .2
                    && line.maxY - next.minY > line.height() * .2) return true;
            bottom = Math.max(bottom, next.maxY);
        }
        return false;
    }

    private static boolean sameLine(List<CharacterResult> characters, double height) {
        Bounds reference = null;
        for (CharacterResult character : characters) {
            Bounds bounds = new Bounds(character.ink);
            if (bounds.height() < height * .4) continue;
            if (reference != null) {
                double overlap = Math.min(reference.maxY, bounds.maxY) - Math.max(reference.minY, bounds.minY);
                if (overlap < .15 * Math.min(reference.height(), bounds.height())) return false;
            }
            if (reference == null || bounds.height() > reference.height()) reference = bounds;
        }
        return true;
    }

    private static Ink subset(Ink original, List<Integer> indices) {
        List<Integer> sorted = new ArrayList<>(indices);
        Collections.sort(sorted);
        List<List<Ink.Point>> strokes = new ArrayList<>();
        for (int index : sorted) {
            checkCancelled();
            strokes.add(original.strokes.get(index));
        }
        return new Ink(strokes);
    }

    private static void order(List<CharacterResult> characters, String group, boolean reverse) {
        boolean hebrew = Alphabet.HEBREW.equals(group);
        if (hebrew != reverse) Collections.reverse(characters);
        if (hebrew) {
            // Hebrew letter order is RTL, but each contiguous number remains LTR.
            // Reversing the entire physical sequence also reverses its numeric runs.
            for (int start = 0; start < characters.size();) {
                checkCancelled();
                if (!isDigit(characters.get(start).label)) {
                    start++;
                    continue;
                }
                int end = start + 1;
                while (end < characters.size() && isDigit(characters.get(end).label)) end++;
                Collections.reverse(characters.subList(start, end));
                start = end;
            }
        }
    }

    private static boolean isDigit(String label) {
        return label.length() == 1 && label.charAt(0) >= '0' && label.charAt(0) <= '9';
    }

    private static Result rejected() {
        return new Result(Collections.emptyList(), true, true);
    }

    private static void checkCancelled() {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException("Recognition cancelled");
    }

    private static class Bounds {
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        Bounds() {}

        Bounds(Ink ink) {
            for (List<Ink.Point> stroke : ink.strokes) include(stroke);
        }

        void include(List<Ink.Point> stroke) {
            for (Ink.Point p : stroke) {
                checkCancelled();
                minX = Math.min(minX, p.x);
                maxX = Math.max(maxX, p.x);
                minY = Math.min(minY, p.y);
                maxY = Math.max(maxY, p.y);
            }
        }

        void include(Bounds other) {
            minX = Math.min(minX, other.minX);
            maxX = Math.max(maxX, other.maxX);
            minY = Math.min(minY, other.minY);
            maxY = Math.max(maxY, other.maxY);
        }

        double width() { return maxX - minX; }
        double height() { return maxY - minY; }
    }

    private static final class Part extends Bounds {
        final List<Integer> indices = new ArrayList<>();

        Part(int index, List<Ink.Point> stroke) {
            include(stroke);
            indices.add(index);
        }

        Part(Part source) {
            include(source);
            indices.addAll(source.indices);
        }
    }
}
