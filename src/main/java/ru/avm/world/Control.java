package ru.avm.world;

import java.util.Properties;

/**
 * An important value.
 */
public class Control {

    private final String name;

    private Number min;

    private Number max;

    private double factor;

    private Number value;

    public Control(String name, Number min, Number max, Number value) {
        this(name, min, max, value, 1D);
    }

    public Control(String name, Number min, Number max, Number value, double factor) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.value = value;
        this.factor = factor;
    }

    public String getName() {
        return name;
    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value = value;
    }

    public Number getMin() {
        return min;
    }

    public Number getMax() {
        return max;
    }

    public double getFactor() {
        return factor;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    protected void load(Properties properties) {
        value = Double.parseDouble(properties.getProperty(name + ".value", value.toString()));
        min = Double.parseDouble(properties.getProperty(name + ".min", min.toString()));
        max = Double.parseDouble(properties.getProperty(name + ".max", max.toString()));
        factor = Double.parseDouble(properties.getProperty(name + ".factor", String.valueOf(factor)));
    }

    protected void save(Properties properties) {
        properties.setProperty(name + ".value", value.toString());
        properties.setProperty(name + ".min", min.toString());
        properties.setProperty(name + ".max", max.toString());
        properties.setProperty(name + ".factor", String.valueOf(factor));
    }
}
