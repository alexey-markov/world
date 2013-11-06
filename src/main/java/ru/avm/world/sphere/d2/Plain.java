package ru.avm.world.sphere.d2;

import ru.avm.world.sphere.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 2D plain.
 */
public class Plain {

    private static final Map<Integer, Integer> counters = new HashMap<Integer, Integer>();

    private static synchronized int index(int generation) {
        Integer count = counters.get(generation);
        if (count == null) {
            counters.put(generation, 2);
            return 1;
        } else {
            counters.put(generation, count + 1);
            return count;
        }
    }

    private final Set<Unit> units = new TreeSet<Unit>(new Comparator<Unit>() {

        @Override
        public int compare(Unit o1, Unit o2) {
            int compare = Double.compare(o2.getGenom().getSpeed(), o1.getGenom().getSpeed());
            return (compare == 0) ? o1.compareTo(o2) : compare;
        }
    });

    private int width;

    private int height;

    public Plain(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Place random() {
        return new Place(Math.random() * width, Math.random() * height);
    }

    public synchronized void add(Unit unit) {
        unit.setIndex(index(unit.getGeneration()));
        units.add(unit);
        unit.setAlive(true);
    }

    public synchronized void remove(Unit unit) {
        units.remove(unit);
        unit.setAlive(false);
    }

    public synchronized List<Unit> getUnits() {
        return new ArrayList<Unit>(units);
    }

    public synchronized int size() {
        return units.size();
    }

    public synchronized List<Unit> nearest(Unit source, double radius) {
        List<Unit> result = new ArrayList<Unit>();
        Place place = source.getPlace();
        for (Unit target : units) {
            if (source.loyal(target)) {
                continue;
            }
            if (Place.length(place, target.getPlace()) < radius) {
                result.add(target);
            }
        }
        return result;
    }

    public synchronized Unit nearest(double x, double y) {
        final Place place = new Place(x, y);
        List<Unit> result = new ArrayList<Unit>();
        for (Unit target : units) {
            if (Place.length(place, target.getPlace()) < 0.5D) {
                result.add(target);
            }
        }
        if (result.isEmpty()) { return null; }
        Collections.sort(result, new Comparator<Unit>() {

            @Override
            public int compare(Unit o1, Unit o2) {
                int compare = Double.compare(Place.length(o1.getPlace(), place), Place.length(o2.getPlace(), place));
                return (compare == 0) ? Double.compare(o2.getGenom().getSpeed(), o1.getGenom().getSpeed()) : compare;
            }
        });
        return result.get(0);
    }

}
