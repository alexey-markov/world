package ru.avm.world;

import ru.avm.world.sphere.Genom;
import ru.avm.world.sphere.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Statistics aggregator.
 */
public class Meter {

    private static final int LENGTH = Genom.values.size();

    private static final String[] RANGES = {"➊", "➋", "➌", "➍", "➎", "➏", "➐", "➑", "➒"};

    private static final int HEIGHT = Math.min(14, RANGES.length * 2);

    private static final String[][] SPECIES = {{"╲", "☓", "⦰", "⍀"},
                                               {null, "╱", "∅", "⌿"},
                                               {"㋛", "⦻", "◯", "⦵"},
                                               {"⧀", "✶", "⧁", "─"}};

    private static final int SERIES = Math.min(4, SPECIES.length);

    private final Rules rules;

    private final Map<Signature, Signature> signatures;

    private int total = 0;

    private int worn = 0;

    private long age = 0L;

    private double t = 0D;

    private double[][][] data;

    private Unit best;

    public Meter(Rules rules) {
        this.rules = rules;
        this.signatures = new HashMap<Signature, Signature>(rules.unitLimit.getMax().intValue());
    }

    public void reset() {
        total = 0;
        worn = 0;
        age = 0L;
        t = 0D;
        data = new double[2][LENGTH][HEIGHT + 1];
        signatures.clear();
    }

    public void check(Unit unit) {
        total++;
        int time = unit.getTime();
        if ((best == null) || !best.isAlive()) {
            best = unit;
        } else if (time > best.getTime()) {
            best.setSelected(false);
            best = unit;
            best.setSelected(true);
        }
        worn = Math.max(worn, time);
        age += time;
        double ratio = unit.ratio();
        t += ratio;
        Genom genom = unit.getGenom();
        int[] signature = new int[LENGTH];
        int col = 0;
        for (Iterator<Map.Entry<String,Boolean>> iterator = Genom.values.entrySet().iterator(); iterator.hasNext(); col++) {
            Map.Entry<String, Boolean> entry = iterator.next();
            int row = index(genom.value(entry.getKey()), entry.getValue());
            signature[col] = row;
            data[0][col][row]++;
            data[1][col][row] += ratio;
        }
        Signature next = new Signature(signature);
        Signature prev = signatures.get(next);
        if (prev == null) {
            signatures.put(next, next);
            next.count = 1;
            next.worn = time;
            next.age = time;
            next.t = ratio;
        } else {
            prev.count++;
            prev.worn = Math.max(prev.worn, next.worn);
            prev.age += time;
            prev.t += ratio;
        }
    }

    private int index(double value, Boolean infinity) {
        double max = infinity ? Genom.beam(rules.normMaximum.getValue().doubleValue()) : rules.normMaximum.getValue().doubleValue();
        double min = infinity ? Genom.beam(rules.normMinimum.getValue().doubleValue()) : rules.normMinimum.getValue().doubleValue();
        return (int) Math.max(0D, Math.min(HEIGHT, (value - min) / (max - min) * HEIGHT + 0.5D));
    }

    public int total() {
        return total;
    }

    public double temperature() {
        return temperature(t, total);
    }

    private double temperature(double t, int count) {
        return (count == 0) ? 0D : t / count;
    }

    public int age() {
        return age(age, total);
    }

    private int age(long age, int count) {
        return (int) (age / count);
    }

    public int worn() {
        return worn;
    }

    private List<List<Range>> ranges() {
        List<List<Range>> ranges = new ArrayList<List<Range>>(LENGTH);
        for (int col = 0; col < LENGTH; col++) {
            ranges.add(extreme(data[0][col]));
        }
        return ranges;
    }

    private List<Range> extreme(double[] array) {
        return narrow(ranges(array), array);
    }

    private List<Range> ranges(double[] array) {
        List<Range> ranges = new ArrayList<Range>();
        int head = 0;
        int tail = array.length - 1;
        // ranges
        Range range = new Range(head);
        range.peak = (int) array[head];
        range.spot = head;
        for (int i = 1; i < array.length - 1; i++) {
            double to = array[i] - array[i - 1];
            double ot = array[i + 1] - array[i];
            if (array[i] > range.peak) {
                range.peak = (int) array[i];
                range.spot = i;
            }
            if ((to < 0D) && (ot > 0D)) {
                range.max = i;
                ranges.add(range);
                range = new Range(i + 1);
            }
        }
        if (array[tail] > range.peak) {
            range.peak = (int) array[tail];
            range.spot = tail;
        }
        range.max = tail;
        ranges.add(range);
        return ranges;
    }

    private List<Range> narrow(List<Range> ranges, double[] array) {
        int bar = (int) Math.floor(Util.deviation(array, 0, array.length));
        for (Iterator<Range> iterator = ranges.iterator(); iterator.hasNext(); ) {
            Range range = iterator.next();
            if (range.peak < bar) {
                iterator.remove();
                continue;
            }
            if (range.min == range.max) {
                continue;
            }
            boolean rise = true;
            for (int i = range.min; i < range.max + 1; i++) {
                int value = (int) array[i];
                if (value == range.peak) {
                    rise = false;
                    continue;
                }
                if (value < bar) {
                    if (rise) {
                        range.min = i + 1;
                    } else {
                        range.max = i - 1;
                    }
                }
            }
        }
        return ranges;
    }

    private List<Signature> species(List<List<Range>> ranges) {
        // merge by ranges = align to peak
        ArrayList<Signature> plain;
        plain = new ArrayList<Signature>(signatures.keySet());
        for (Signature signature : plain) {
            for (int col = 0; col < LENGTH; col++) {
                for (Range range : ranges.get(col)) {
                    if (range.contains(signature.indices[col])) {
                        signature.indices[col] = range.spot;
                    }
                }
            }
        }
        // merge by ranges = collect again
        signatures.clear();
        for (Signature next : plain) {
            Signature prev = signatures.get(next);
            if (prev == null) {
                signatures.put(next, next);
            } else {
                prev.count += next.count;
                prev.worn = Math.max(prev.worn, next.worn);
                prev.age += next.age;
                prev.t += next.t;
            }
        }
        // get top signatures = select species
        plain = new ArrayList<Signature>(signatures.keySet());
        Collections.sort(plain, new Comparator<Signature>() {

            public int compare(Signature o1, Signature o2) {
                return Integer.compare(o2.count, o1.count);
            }
        });
        return plain.subList(0, Math.min(plain.size(), SERIES));
    }

    private String line(int row, double beam, double norm, List<List<Range>> ranges, List<Signature> species) {
        StringBuilder builder = new StringBuilder();
        for (int col = 0; col < LENGTH; col++) {
            builder.append(String.format("\t%4d%s%.2f%s",
                                         (int) data[0][col][row],
                                         range(row, ranges.get(col)),
                                         temperature(data[1][col][row], (int) data[0][col][row]),
                                         spice(col, row, species)));
        }
        return String.format("\n%6.1f%s\t%.2f", beam, builder.toString(), norm);
    }

    private int percent(double count) {
        return (int) (count / total * 100D);
    }

    private String range(int row, List<Range> ranges) {
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).contains(row)) {
                return RANGES[i];
            }
        }
        return "/";
    }

    private String spice(int col, int row, List<Signature> species) {
        int size = Math.min(species.size(), SERIES);
        List<Integer> stack = new ArrayList<Integer>(size);
        for (int i = 0; i < size; i++) {
            if (species.get(i).indices[col] == row) {
                stack.add(i);
            }
        }
        int i, j;
        switch (stack.size()) {
            default:
                return " ";
            case 1:
                i = stack.get(0);
                j = stack.get(0);
                return SPECIES[i][j];
            case 2:
                i = stack.get(0);
                j = stack.get(1);
                return SPECIES[i][j];
            case 3:
                i = stack.get(2);
                j = (stack.get(1) + stack.get(0)) % 3;
                return SPECIES[i][j];
            case 4: // it's max size for int[][] with crosses
                return SPECIES[2][0];
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("\t");
        for (String name : Genom.values.keySet()) {
            builder.append("\t\t").append(name).append(Genom.values.get(name) ? "∞" : "①");
        }
        List<List<Range>> ranges = ranges();
        List<Signature> species = species(ranges);
        double nmin = rules.normMinimum.getValue().doubleValue();
        double nmax = rules.normMaximum.getValue().doubleValue();
        double bmin = Genom.beam(nmin);
        double bmax = Genom.beam(nmax);
        for (int row = HEIGHT; row >= 0; row--) {
            double norm = nmin + row * (nmax - nmin) / HEIGHT;
            double beam = bmin + row * (bmax - bmin) / HEIGHT;
            builder.append(line(row, beam, norm, ranges, species));
        }
        for (int i = 0; i < species.size(); i++) {
            Signature signature = species.get(i);
            builder.append((i > 0) ? "; " : "\n")
                   .append(String.format("'%s' %4d/%.2f/%d[%d]",
                                         SPECIES[i][i],
                                         signature.count,
                                         temperature(signature.t, signature.count),
                                         age(signature.age, signature.count),
                                         signature.worn));
        }
        return builder.toString();
    }

    private static class Signature implements Comparable<Signature> {

        private final int[] indices;

        protected int count = 0;

        protected int worn = 0;

        protected long age = 0L;

        protected double t = 0D;

        private Signature(int[] indices) {
            this.indices = indices;
        }

        @Override
        public int compareTo(Signature o) {
            for (int i = 0; i < indices.length; i++) {
                int compare = Integer.compare(indices[i], o.indices[i]);
                if (compare != 0) {
                    return compare;
                }
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            return compareTo((Signature) o) == 0;
        }

        @Override
        public int hashCode() {
            return indices[0];
        }
    }

    private static class Range {

        private int min;

        private int max;

        private int peak;

        private int spot;

        private Range(int min) {
            this.min = min;
        }

        public boolean contains(int index) {
            return (min <= index) && (index <= max);
        }
    }

}

