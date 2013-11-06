package ru.avm.world.sphere.d2;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 2D point.
 */
public class Place {

    private final double x;

    private final double y;

    public Place(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static double length(Place one, Place two) {
        double x1 = one.getX();
        double y1 = one.getY();
        double x2 = two.getX();
        double y2 = two.getY();
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
