package ru.avm.world.sphere;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.avm.world.sphere.d2.Place;

/**
 * A state.
 */
public abstract class State {

    private int time = 0;

    private Place place;

    private Unit target = null;

    private double energy;

    private boolean selected = false;

    protected State() {
    }

    protected State(State copy) {
        this.place = copy.place;
        this.energy = copy.energy;
        //this.selected = copy.selected;
    }

    public int getTime() {
        return time;
    }

    public void incrementTime() {
        this.time++;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Unit getTarget() {
        return target;
    }

    public void setTarget(Unit target) {
        this.target = target;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
