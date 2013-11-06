package ru.avm.world;

import ru.avm.world.sphere.Genom;
import ru.avm.world.sphere.Law;
import ru.avm.world.sphere.Unit;
import ru.avm.world.sphere.d2.Plain;
import ru.avm.world.view.World;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Processing..
 */
public class Life {

    private final List<LoopListener> listeners = new ArrayList<LoopListener>();

    private final Rules rules = new Rules();

    private final Law law = new Law(rules);

    private final Meter meter = new Meter(rules);

    private final int size;

    private final Plain plain;

    private long circle = 0L;

    public Life(final int size) {
        this.size = size;
        plain = new Plain(size, size);
        for (int i = 0; i < rules.unitLimit.getValue().intValue(); i++) {
            Unit unit = new Unit(Genom.random(rules.normMinimum.getValue().doubleValue(),
                                              rules.normMaximum.getValue().doubleValue(),
                                              rules.mutableFactor.getValue().doubleValue()));
            unit.setPlace(plain.random());
            unit.setEnergy(unit.cargo() / 2);
            plain.add(unit);
            rules.energyTotal.setValue(rules.energyTotal.getValue().doubleValue() + unit.getEnergy());
        }
        rules.load();
    }

    public int getSize() {
        return size;
    }

    public Plain getPlain() {
        return plain;
    }

    public Rules getRules() {
        return rules;
    }

    public void addLoopListener(LoopListener listener) {
        listeners.add(listener);
    }

    public void removeLoopListener(LoopListener listener) {
        listeners.remove(listener);
    }

    private boolean process() {
        if (plain.size() <= 0) { return false; }
        // sight
        circle++;
        for (Unit unit : plain.getUnits()) {
            unit.incrementTime();
            law.look(unit, plain);
            if (unit.getEnergy() <= 0) { plain.remove(unit); }
        }
        if (plain.size() <= 0) { return false; }
        // move!
        double energy = 0D;
        int selects = 0;
        meter.reset();
        for (Unit unit : plain.getUnits()) {
            law.move(unit, plain);
            if (unit.getEnergy() <= 0) { plain.remove(unit); }
            energy += unit.getEnergy();
            if (unit.isSelected()) {
                selects++;
            }
            if (unit.power() <= 0D) {
                continue;
            }
            meter.check(unit);
        }
        if (meter.total() <= 0) {
            System.err.println(String.format("#%,8d; population=%d[%d]/%d; temperature=%.2f; energy=%.2f",
                                             circle, meter.total(), selects, plain.size(), meter.temperature(), energy));
            return false;
        }
        double maximum = rules.energyTotal.getValue().doubleValue();
        double ratio = maximum / energy;
        int reserve = size * (int) ratio;
        double piece = 0D;
        if (ratio > 1D) {
            piece = (maximum - energy) / reserve;
            for (int i = 0; i < reserve; i++) {
                Unit unit = new Unit(new Genom(Collections.singletonMap("cargo", piece)));
                unit.incrementTime();
                unit.setEnergy(unit.cargo());
                unit.setPlace(plain.random());
                plain.add(unit);
            }
        }
        System.err.println(String.format("#%,8d; population=%d[%d]/%d; temperature=%.2f; age=%d[%d]; energy=%.2f + %.2f*%d\n%s",
                                         circle, meter.total(), selects, plain.size(), meter.temperature(), meter.age(), meter.worn(),
                                         energy, piece, reserve, meter.toString()));
        for (LoopListener listener : listeners) {
            listener.next();
        }
        synchronized (this) {
            try { wait(rules.delay.getValue().longValue()); } catch (InterruptedException ignored) {}
        }
        return true;
    }

    public static void main(String[] args) {
        final Life life = new Life(10);

        System.setSecurityManager(new SaveOnExit(life));

        final World world = new World(life);
        world.pack();
        world.setVisible(true);

        life.addLoopListener(world);

        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean go;
                do {
                    go = life.process();
                } while (go);
            }
        }).start();

    }

    public static interface LoopListener {

        void next();
    }

    private static class SaveOnExit extends SecurityManager {

        private final Life life;

        public SaveOnExit(Life life) {
            this.life = life;
        }

        @Override
        public void checkPermission(Permission perm) {
        }

        @Override
        public void checkExit(int status) {
            life.rules.save();
        }

    }
}
