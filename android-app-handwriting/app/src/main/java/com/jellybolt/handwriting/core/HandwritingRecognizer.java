package com.jellybolt.handwriting.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-user exemplar matching. No pretrained alphabet, rotation, mirroring,
 * stroke-order requirements, or language-model autocorrection.
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
            similarity = (int) Math.round(100 * Math.exp(-12 * distance));
        }
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

    private static final class Shape {
        final boolean[] mask = new boolean[GRID * GRID];
        final float[] distance = new float[GRID * GRID];
        final int[] occupied;

        Shape(Ink ink) {
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (List<Ink.Point> stroke : ink.strokes) {
                for (Ink.Point p : stroke) {
                    minX = Math.min(minX, p.x);
                    maxX = Math.max(maxX, p.x);
                    minY = Math.min(minY, p.y);
                    maxY = Math.max(maxY, p.y);
                }
            }
            float extent = Math.max(maxX - minX, maxY - minY);
            float scale = extent == 0 ? 1 : (GRID - 5) / extent;
            float centerX = minX + (maxX - minX) / 2;
            float centerY = minY + (maxY - minY) / 2;
            for (List<Ink.Point> stroke : ink.strokes) {
                Ink.Point previous = null;
                for (Ink.Point point : stroke) {
                    float x = (point.x - centerX) * scale + (GRID - 1) / 2f;
                    float y = (point.y - centerY) * scale + (GRID - 1) / 2f;
                    if (previous == null) {
                        mark(x, y);
                    } else {
                        float px = (previous.x - centerX) * scale + (GRID - 1) / 2f;
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
