package ru.avm.world.sphere;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Genom
 */
public class Genom {

    public static double beam(double x) {
        //return (1D / (1D - x) - 1D) + x;
        return (1D / (1D - x) - 1D) + 10D * x;
    }

    public static double norm(double x) {
        //return (1D - 1D / (1D + x));
        return (-1D * Math.sqrt(x * x - 18D * x + 121D) + x + 11D) / 20D;
    }

    // ToDo: immutable
    public static final Map<String, Boolean> values = new LinkedHashMap<String, Boolean>();

    static {
        // true  for values with the range [0..∞]
        values.put("limit", true);
        values.put("faith", true);
        values.put("sight", true);
        values.put("speed", true);
        values.put("cargo", true);
        values.put("greed", true);
        values.put("stale", true);
        // false for values with the range [0..1]
        values.put("sleep", false);
        values.put("fight", false);
        values.put("peril", false);
        values.put("multi", false);
        values.put("nurse", false);
    }

    private static Random random = new Random(System.currentTimeMillis());

    private static double mutable(double param, double min, double max, double factor) {
        double sign = (random.nextDouble() > 0.5D) ? +1D : -1D;
        double ugly = random.nextDouble() * ((random.nextDouble() < factor) ? (1D - factor) : factor);
        double next = Math.min(max, Math.max(min, param + sign * ugly));
        return next;
    }

    private Map<String, Double> dnk = new HashMap<String, Double>(values.size());

    public static Genom random(double min, double max, double factor) {
        double[] values = new double[Genom.values.size()];
        for (int i = 0; i < Genom.values.size(); i++) {
            values[i] = mutable(random.nextDouble(), min, max, factor);
        }
        return new Genom(values);
    }

    public Genom(double... values) {
        int i = 0;
        for (Iterator<String> iterator = Genom.values.keySet().iterator(); iterator.hasNext() && (i < values.length); i++) {
            set(iterator.next(), values[i]);
        }
    }

    public Genom(Map<String, Double> values) {
        for (String name : Genom.values.keySet()) {
            dnk.put(name, values.get(name));
        }
    }

    /**
     * Gets actual value.
     *
     * @param name param name
     * @return actual value
     */
    public double value(String name) {
        Double value = dnk.get(name);
        return (value == null) ? 0D : value;
    }

    /**
     * Gets norm value.
     *
     * @param name param name
     * @return norm value
     */
    public double get(String name) {
        Double value = value(name);
        return values.get(name) ? norm(value) : value;
    }

    private void set(String name, double value) {
        dnk.put(name, values.get(name) ? beam(value) : value);
    }

    public double getFaith() {
        return value("faith");
    }

    public double getFight() {
        return value("fight");
    }

    public double getPeril() {
        return value("peril");
    }

    public double getSpeed() {
        return value("speed");
    }

    public double getSight() {
        return value("sight");
    }

    public double getCargo() {
        return value("cargo");
    }

    public double getMulti() {
        return value("multi");
    }

    public double getLimit() {
        return value("limit");
    }

    public double getStale() {
        return value("stale");
    }

    public double getGreed() {
        return value("greed");
    }

    public double getSleep() {
        return value("sleep");
    }

    public double getNurse() {
        return value("nurse");
    }

    public Genom birth(double min, double max, double factor) {
        double[] values = new double[Genom.values.size()];
        int i = 0;
        for (Iterator<String> iterator = Genom.values.keySet().iterator(); iterator.hasNext(); i++) {
            values[i] = mutable(get(iterator.next()), min, max, factor);
        }
        return new Genom(values);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
