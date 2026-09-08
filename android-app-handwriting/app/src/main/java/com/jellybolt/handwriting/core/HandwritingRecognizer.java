package com.jellybolt.handwriting.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Offline stroke-shape matching, optionally with original hand-authored starter
 * examples and optional labeled left/right reflections. No neural model, rotation, stroke-order requirements,
 * or language-model autocorrection.
 */
public final class HandwritingRecognizer {
    private static final int GRID = 32;
    private static final float DIAGONAL = (float) Math.sqrt(2);

    private HandwritingRecognizer() {}

    public static final class Example {
        public final long id;
        public final String label;
        public final Ink ink;
        private volatile Shape cachedShape;

        public Example(long id, String label, Ink ink) {
            if (label == null || label.isEmpty() || ink == null || ink.isEmpty()) {
                throw new IllegalArgumentException("A labeled drawing is required");
            }
            this.id = id;
            this.label = label;
            this.ink = ink;
        }

        private Shape shape() {
            if (cachedShape == null) cachedShape = new Shape(ink);
            return cachedShape;
        }
    }

    public static final class Candidate {
        public final String label;
        /** A relative shape-match score, NOT a calibrated probability. */
        public final int similarity;

        private Candidate(String label, double distance) {
            this.label = label;
            similarity = shapeSimilarity(distance);
        }
    }

    private static int shapeSimilarity(double distance) {
        return (int) Math.round(100 * Math.exp(-12 * distance));
    }

    public static final class Result {
        public final List<Candidate> candidates;
        public final boolean uncertain;
        public final int trainedLabels;

        private Result(List<Candidate> candidates, boolean uncertain, int trainedLabels) {
            this.candidates = Collections.unmodifiableList(new ArrayList<>(candidates));
            this.uncertain = uncertain;
            this.trainedLabels = trainedLabels;
        }
    }

    public static Result recognize(Ink ink, List<Example> examples) {
        if (ink == null || ink.isEmpty()) throw new IllegalArgumentException("Draw a character first");
        if (examples == null) throw new IllegalArgumentException("Examples are required");
        Map<String, List<Double>> distances = new LinkedHashMap<>();
        Shape query = new Shape(ink);
        for (Example example : examples) {
            if (Thread.currentThread().isInterrupted()) {
                throw new java.util.concurrent.CancellationException("Recognition cancelled");
            }
            distances.computeIfAbsent(example.label, key -> new ArrayList<>())
                    .add(query.distance(example.shape()));
        }
        if (distances.size() < 2) return new Result(Collections.emptyList(), true, distances.size());
        Map<String, Double> ranked = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> entry : distances.entrySet()) {
            List<Double> values = entry.getValue();
            Collections.sort(values);
            int neighbors = Math.min(3, values.size());
            double mean = 0;
            for (int i = 0; i < neighbors; i++) mean += values.get(i) / neighbors;
            // Preserve rare personal variants while slightly rewarding repeated support.
            ranked.put(entry.getKey(), 0.9 * values.get(0) + 0.1 * mean);
        }
        List<Map.Entry<String, Double>> ordered = new ArrayList<>(ranked.entrySet());
        ordered.sort(Map.Entry.<String, Double>comparingByValue()
                .thenComparing(Map.Entry.comparingByKey()));
        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < Math.min(3, ordered.size()); i++) {
            candidates.add(new Candidate(ordered.get(i).getKey(), ordered.get(i).getValue()));
        }
        Candidate first = candidates.get(0);
        boolean uncertain = first.similarity < 55
                || first.similarity - candidates.get(1).similarity < 12
                || distances.get(first.label).size() < 3;
        return new Result(candidates, uncertain, distances.size());
    }

    /**
     * Matches this alphabet's bundled examples plus the child's own examples.
     * trainedLabels still counts only personal labels, not the bundled alphabet.
     */
    public static Result recognize(Ink ink, String group, List<Example> personal) {
        return recognize(ink, group, personal, false);
    }

    public static Result recognize(Ink ink, String group, List<Example> personal, boolean mirrored) {
        return recognizeGroups(ink, Collections.singletonList(group),
                Collections.singletonMap(group, personal), mirrored);
    }

    // A single shared ranking is necessary for word-mode digits and letters: merging
    // separately ranked suggestions would lose the personal-source tie preference.
    static Result recognizeGroups(Ink ink, List<String> groups,
                                  Map<String, List<Example>> personalByGroup, boolean mirrored) {
        return recognizeGroups(ink, groups, personalByGroup, mirrored, false);
    }

    static Result recognizeGroups(Ink ink, List<String> groups,
                                  Map<String, List<Example>> personalByGroup, boolean mirrored,
                                  boolean flexibleWidth) {
        if (ink == null || ink.isEmpty()) throw new IllegalArgumentException("Draw a character first");
        checkCancelled();
        Map<String, LabelMatch> matches = new LinkedHashMap<>();
        List<Example> personal = new ArrayList<>();
        List<Example> bundled = new ArrayList<>();
        for (String group : groups) {
            if (!Alphabet.isGroup(group)) throw new IllegalArgumentException("Unknown alphabet: " + group);
            List<Example> examples = personalByGroup.get(group);
            if (examples == null) throw new IllegalArgumentException("Examples are required");
            List<String> labels = Alphabet.labels(group);
            for (String label : labels) matches.put(label, new LabelMatch(label));
            for (Example example : examples) {
                checkCancelled();
                if (example == null || !labels.contains(example.label)) {
                    throw new IllegalArgumentException("Personal example is outside alphabet: " + group);
                }
                personal.add(example);
            }
            bundled.addAll(DefaultSamples.examples(group, mirrored));
        }
        // Validate before matching; mixing alphabets must never silently discard saved data.
        int trainedLabels = 0;
        for (Example example : personal) {
            checkCancelled();
            LabelMatch match = matches.get(example.label);
            if (!match.hasPersonal) {
                match.hasPersonal = true;
                trainedLabels++;
            }
        }
        Shape query = new Shape(ink);
        float[] widthFactors = flexibleWidth ? new float[]{1, .65f, .8f, 1.25f, 1.5f, 1.75f} : new float[]{1};
        Shape[] queries = new Shape[widthFactors.length];
        queries[0] = query;
        for (int i = 1; i < queries.length; i++) queries[i] = new Shape(ink, widthFactors[i]);
        for (Example example : bundled) {
            checkCancelled();
            LabelMatch match = matches.get(example.label);
            match.bundledDistance = Math.min(match.bundledDistance,
                    widthTolerantDistance(queries, widthFactors, example));
        }
        for (Example example : personal) {
            checkCancelled();
            LabelMatch match = matches.get(example.label);
            match.personalDistance = Math.min(match.personalDistance,
                    widthTolerantDistance(queries, widthFactors, example));
        }
        /*
         * Each source contributes only its nearest shape per label, not votes or
         * a pooled neighbor mean. Thus many starters cannot drown one rare or
         * mirrored personal example, and many personal samples cannot swamp an
         * untrained label. A 0.8 personal-distance multiplier favors the child;
         * exact ties prefer the personal source, then the label for determinism.
         * Displayed similarity uses the winning source's UNWEIGHTED shape distance:
         * it is a match score, never a calibrated probability or sample count.
         */
        List<LabelMatch> ordered = new ArrayList<>(matches.values());
        ordered.sort((left, right) -> {
            int distanceOrder = Double.compare(left.rankDistance(), right.rankDistance());
            if (distanceOrder != 0) return distanceOrder;
            if (left.usesPersonal() != right.usesPersonal()) return left.usesPersonal() ? -1 : 1;
            return left.label.compareTo(right.label);
        });
        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < Math.min(3, ordered.size()); i++) {
            LabelMatch match = ordered.get(i);
            candidates.add(new Candidate(match.label, match.shapeDistance()));
        }
        checkCancelled();
        Candidate first = candidates.get(0);
        int competingSimilarity = 0;
        // Personal ranking can reorder raw similarities. Check every rival rather
        // than assuming the second-ranked class has the next-closest raw shape.
        for (int i = 1; i < ordered.size(); i++) {
            competingSimilarity = Math.max(competingSimilarity, shapeSimilarity(ordered.get(i).shapeDistance()));
        }
        boolean uncertain = first.similarity < 55
                || first.similarity - competingSimilarity < 12
                // These pairs become the same intended geometry under reflection.
                // Differences in our hand-authored exemplars are not evidence of intent.
                || (mirrored && groups.contains(Alphabet.ENGLISH_LOWER)
                    && ("b".equals(first.label) || "d".equals(first.label)
                        || "p".equals(first.label) || "q".equals(first.label)));
        return new Result(candidates, uncertain, trainedLabels);
    }

    private static double widthTolerantDistance(Shape[] queries, float[] factors, Example example) {
        Shape template = example.shape();
        // Oval 0/O/o templates differ mainly in proportions after bounding-box
        // normalization. Removing that signal would turn ordinary "mom" into "m0m".
        if ("0".equals(example.label) || "O".equals(example.label) || "o".equals(example.label)) {
            return queries[0].distance(template);
        }
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < queries.length; i++) {
            checkCancelled();
            // Print within a word is often narrower than an isolated training glyph.
            // A bounded width search penalizes deformation; it never changes ink,
            // case, orientation, stroke order, or the legacy single-character metric.
            double penalty = .025 * Math.abs(Math.log(factors[i]));
            best = Math.min(best, queries[i].distance(template) + penalty);
        }
        return best;
    }

    private static void checkCancelled() {
        if (Thread.currentThread().isInterrupted()) {
            throw new java.util.concurrent.CancellationException("Recognition cancelled");
        }
    }

    private static final class LabelMatch {
        final String label;
        boolean hasPersonal;
        double bundledDistance = Double.POSITIVE_INFINITY;
        double personalDistance = Double.POSITIVE_INFINITY;

        LabelMatch(String label) {
            this.label = label;
        }

        boolean usesPersonal() {
            return personalDistance * 0.8 <= bundledDistance;
        }

        double rankDistance() {
            return Math.min(bundledDistance, personalDistance * 0.8);
        }

        double shapeDistance() {
            return usesPersonal() ? personalDistance : bundledDistance;
        }
    }

    private static final class Shape {
        final boolean[] mask = new boolean[GRID * GRID];
        final float[] distance = new float[GRID * GRID];
        final int[] occupied;

        Shape(Ink ink) {
            this(ink, 1);
        }

        Shape(Ink ink, float widthFactor) {
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (List<Ink.Point> stroke : ink.strokes) {
                checkCancelled();
                for (Ink.Point p : stroke) {
                    minX = Math.min(minX, p.x);
                    maxX = Math.max(maxX, p.x);
                    minY = Math.min(minY, p.y);
                    maxY = Math.max(maxY, p.y);
                }
            }
            float extent = Math.max((maxX - minX) * widthFactor, maxY - minY);
            float scale = extent == 0 ? 1 : (GRID - 5) / extent;
            float centerX = minX + (maxX - minX) / 2;
            float centerY = minY + (maxY - minY) / 2;
            for (List<Ink.Point> stroke : ink.strokes) {
                checkCancelled();
                Ink.Point previous = null;
                for (Ink.Point point : stroke) {
                    checkCancelled();
                    float x = (point.x - centerX) * scale * widthFactor + (GRID - 1) / 2f;
                    float y = (point.y - centerY) * scale + (GRID - 1) / 2f;
                    if (previous == null) {
                        mark(x, y);
                    } else {
                        float px = (previous.x - centerX) * scale * widthFactor + (GRID - 1) / 2f;
                        float py = (previous.y - centerY) * scale + (GRID - 1) / 2f;
                        int steps = Math.max(1, (int) Math.ceil(Math.hypot(x - px, y - py) * 2));
                        for (int i = 0; i <= steps; i++) {
                            mark(px + (x - px) * i / steps, py + (y - py) * i / steps);
                        }
                    }
                    previous = point;
                }
            }
            int count = 0;
            for (boolean pixel : mask) if (pixel) count++;
            occupied = new int[count];
            Arrays.fill(distance, GRID * 2);
            int position = 0;
            for (int i = 0; i < mask.length; i++) {
                if (mask[i]) {
                    occupied[position++] = i;
                    distance[i] = 0;
                }
            }
            // Two-pass chamfer distance field: O(grid area), independent of stroke sampling density.
            for (int y = 0; y < GRID; y++) {
                checkCancelled();
                for (int x = 0; x < GRID; x++) {
                    int i = y * GRID + x;
                    if (x > 0) relax(i, i - 1, 1);
                    if (y > 0) {
                        relax(i, i - GRID, 1);
                        if (x > 0) relax(i, i - GRID - 1, DIAGONAL);
                        if (x < GRID - 1) relax(i, i - GRID + 1, DIAGONAL);
                    }
                }
            }
            for (int y = GRID - 1; y >= 0; y--) {
                checkCancelled();
                for (int x = GRID - 1; x >= 0; x--) {
                    int i = y * GRID + x;
                    if (x < GRID - 1) relax(i, i + 1, 1);
                    if (y < GRID - 1) {
                        relax(i, i + GRID, 1);
                        if (x > 0) relax(i, i + GRID - 1, DIAGONAL);
                        if (x < GRID - 1) relax(i, i + GRID + 1, DIAGONAL);
                    }
                }
            }
        }

        private void mark(float x, float y) {
            int px = Math.max(0, Math.min(GRID - 1, Math.round(x)));
            int py = Math.max(0, Math.min(GRID - 1, Math.round(y)));
            mask[py * GRID + px] = true;
        }

        private void relax(int to, int from, float cost) {
            distance[to] = Math.min(distance[to], distance[from] + cost);
        }

        double distance(Shape other) {
            double sum = 0;
            for (int i : occupied) sum += other.distance[i] / occupied.length;
            for (int i : other.occupied) sum += distance[i] / other.occupied.length;
            return sum / (2 * (GRID - 5));
        }
    }
}
