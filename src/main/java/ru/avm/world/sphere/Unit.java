package ru.avm.world.sphere;

/**
 * A unit.
 */
public class Unit extends State implements Comparable<Unit> {

    private boolean alive = false;

    private final int[] stock;

    private final Genom genom;

    private double power = 0D;

    private double ratio = 0D;

    public Unit(Genom genom) {
        this.genom = genom;
        this.stock = new int[1];
    }

    protected Unit(Genom genom, Unit parent) {
        super(parent);
        this.genom = genom;
        int generation = parent.stock.length;
        this.stock = new int[generation + 1];
        System.arraycopy(parent.stock, 0, this.stock, 0, generation);
    }

    public Genom getGenom() {
        return genom;
    }

    public int getIndex() {
        return stock[stock.length - 1];
    }

    public void setIndex(int index) {
        if (stock[stock.length - 1] != 0) {
            throw new IllegalStateException();
        }
        stock[stock.length - 1] = index;
    }

    public int getGeneration() {
        return stock.length;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public Unit birth(double min, double max, double factor) {
        return new Unit(genom.birth(min, max, factor), this);
    }

    public double want(double diff) {
        return drive(diff) * Math.max(0D, cargo() - Math.max(0D, getEnergy() + diff));
    }

    @Override
    public void setEnergy(double energy) {
        super.setEnergy(energy);
        double cargo = cargo();
        ratio = (cargo > 0D) ? getEnergy() / cargo : 0D;
    }

    public double ratio() {
        return ratio;
    }

    public double ratio(double diff) {
        double cargo = cargo();
        return (cargo > 0D) ? Math.max(0D, Math.min(1D, ratio + diff / cargo)) : 0D;
    }

    @Override
    public void incrementTime() {
        super.incrementTime();
        double limit = getGenom().getLimit();
        double time = getTime();
        if (time > limit) {
            power = 0D;
        } else {
            double ratio = time / (limit + 1D);
            power = Math.max(0D, 2D + 1D / (ratio * ratio / 2 - 1));
        }
    }

    public double power() {
        return power;
    }

    public double drive() {
        return 4 * power * ratio * Math.max(0D, 1D - ratio);
    }

    public double drive(double diff) {
        double ratio = ratio(diff);
        return 4 * power * ratio * Math.max(0D, 1D - ratio);
    }

    public double cargo() {
        return getGenom().getCargo();
    }

    public double sight() {
        return drive() * getGenom().getSight();
    }

    public double speed() {
        return drive() * getGenom().getSpeed();
    }

    public boolean loyal(Unit unit) {
        int depth = (int) Math.floor(genom.getFaith());
        if (this.stock.length - unit.stock.length > depth) {
            return false;
        }
        int index = Math.max(0, this.stock.length - 1 - depth);
        return this.stock[index] == unit.stock[index];
    }

    @Override
    public int compareTo(Unit o) {
        int compare = Integer.compare(stock.length, o.stock.length);
        return (compare == 0) ? Integer.compare(stock[stock.length - 1], o.stock[stock.length - 1]) : compare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        return compareTo((Unit) o) == 0;

    }

    @Override
    public int hashCode() {
        return getIndex();
    }

    @Override
    public String toString() {
        return String.format("Unit[stock=%d[%d], genom=%s, ratio=%.2f, power=%.2f]",
                             stock[stock.length - 1], stock.length, genom, ratio, power);
    }

}
