package ru.avm.world.sphere;

import ru.avm.world.Rules;
import ru.avm.world.sphere.d2.Place;
import ru.avm.world.sphere.d2.Plain;

import java.util.List;
import java.util.Random;

/**
 * Life laws.
 */
public class Law {

    private static Random random = new Random(System.currentTimeMillis());

    private Rules rules;

    public Law(Rules rules) {
        this.rules = rules;
    }

    private double reward(Unit source, Unit target) {
        double charge = charge(source, Place.length(source.getPlace(), target.getPlace()));
        return battle(source, target, charge) - charge;
    }

    private double charge(Unit unit, double length) {
        double mass = mass(unit);
        double value = rules.unbearableLightnessOfBeing.getValue().doubleValue();
        double charge = mass * value;
        if (length > 0D) {
            double speed = unit.speed() * value;
            charge += mass * speed * speed * length / 2;
        }
        return charge;
    }

    private double battle(Unit source, Unit target, double charge) {
        double take = source.want(-charge);
        double lost = target.want(0D);
        double want = 0D;
        if ((lost <= 0D) || (lost > source.getGenom().getStale()) || (source.speed() > target.speed())) {
            want += take;
        }
        if ((take <= 0D) || (take > target.getGenom().getStale()) || (target.speed() > source.speed())) {
            want -= lost;
        }
        return (want >= 0) ? Math.min(want, target.getEnergy()) : Math.max(want, -source.getEnergy());
    }

    private void energy(Unit unit, double value) {
        unit.setEnergy(Math.min(Math.max(0D, value), unit.cargo()));
    }

    public void look(final Unit unit, final Plain plain) {
        double sight = unit.sight();
        Genom genom = unit.getGenom();
        boolean sleep = (genom.getSleep() > random.nextDouble());
        if (sleep || (sight <= 0D)) {
            return;
        }
        List<Unit> nearest = plain.nearest(unit, sight);
        double value = rules.unbearableLightnessOfBeing.getValue().doubleValue();
        energy(unit, unit.getEnergy() - sight * value * sight * value);
        if (nearest.isEmpty()) {
            return;
        }
        boolean fight = (genom.getFight() > random.nextDouble()); // more fight = more chance to find food
        boolean peril = (genom.getPeril() > random.nextDouble()); // more peril = less chance to find warn
        double greed = genom.getGreed();
        double stale = -genom.getStale();
        Place place = unit.getPlace();
        Unit food = null;
        Unit warn = null;
        for (Unit target : nearest) {
            if (fight) {
                double reward = reward(unit, target);
                if (reward > greed) {
                    if ((food == null) || (reward > reward(unit, food))) {
                        food = target;
                    }
                    continue; // it's not warn exactly
                }
            }
            if (peril) {continue;}
            double battle = battle(unit, target, 0D);
            double length = Place.length(place, target.getPlace());
            if ((battle < stale) && (target.sight() >= length) && (target.speed() >= length)) {
                if ((warn == null) || (battle < battle(unit, warn, 0D))) {
                    warn = target;
                }
            }
        }
        unit.setTarget((warn == null) ? food : warn);
    }

    public void move(final Unit unit, final Plain plain) {
        double energy = unit.getEnergy();
        if (unit.power() <= 0D) {
            energy(unit, energy - charge(unit, 0D));
            return;
        }
        Genom genom = unit.getGenom();
        Place place = unit.getPlace();
        Unit target = unit.getTarget();
        boolean fear = (target != null) && battle(unit, target, 0D) < 0D;
        if (!fear && (unit.ratio() > genom.getMulti()) && (plain.size() < rules.unitLimit.getValue().intValue())) {
            double nurse = genom.getNurse();
            double value = energy * (1D - nurse);
            energy(unit, value);
            energy(unit, value - charge(unit, 0D));
            Unit baby = unit.birth(rules.normMinimum.getValue().doubleValue(),
                                   rules.normMaximum.getValue().doubleValue(),
                                   rules.mutableFactor.getValue().doubleValue());
            energy(baby, energy * nurse);
            plain.add(baby);
        } else if ((target == null) || (target.getEnergy() <= 0D)) {
            energy(unit, energy - charge(unit, 0D));
        } else if (fear) {
            double speed = unit.speed();
            Place address = plain.random();
            double length = Place.length(place, address);
            if ((length <= 0D) || (length < speed)) {
                unit.setPlace(new Place(address.getX(), address.getY()));
                energy(unit, energy - charge(unit, length));
            } else {
                unit.setPlace(new Place(place.getX() + (address.getX() - place.getX()) * speed / length,
                                        place.getY() + (address.getY() - place.getY()) * speed / length));
                energy(unit, energy - charge(unit, speed));
            }
        } else {
            double speed = unit.speed();
            Place address = target.getPlace();
            double length = Place.length(place, address);
            if ((length <= 0D) || (length < speed)) {
                unit.setPlace(new Place(address.getX(), address.getY()));
                double reward = reward(unit, target);
                double battle = battle(target, unit, 0D);
                double want = energy + reward;
                double lost = target.getEnergy() + battle;
                if (want + lost > energy + target.getEnergy()) {
                    // ToDo: throw new IllegalStateException("energy grows");
                }
                energy(unit, want);
                energy(target, lost);
            } else {
                unit.setPlace(new Place(place.getX() + (address.getX() - place.getX()) * speed / length,
                                        place.getY() + (address.getY() - place.getY()) * speed / length));
                energy(unit, energy - charge(unit, speed));
            }
        }
        unit.setTarget(null);
    }

    public double mass(Unit unit) {
        return (unit.cargo() * rules.unbearableLightnessOfBeing.getValue().doubleValue() + unit.getEnergy())
               * rules.energyVolume.getValue().doubleValue();
    }

}
