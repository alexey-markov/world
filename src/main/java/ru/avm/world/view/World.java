package ru.avm.world.view;

import ru.avm.world.Control;
import ru.avm.world.Life;
import ru.avm.world.Rules;
import ru.avm.world.sphere.Genom;
import ru.avm.world.sphere.Unit;
import ru.avm.world.sphere.d2.Place;
import ru.avm.world.sphere.d2.Plain;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.Collections;
import java.util.Comparator;

/**
 * Show & control frame.
 */
public class World extends JFrame implements Life.LoopListener {

    private static final String ICON_FILE = "/images/icons/pause.png";

    private static final int UI_FACTOR = 50;

    private final Life life;

    private final int size;

    private final Plain plain;

    private final JPanel panel;

    public World(Life life) {
        super("World");
        this.life = life;
        this.size = life.getSize();
        this.plain = life.getPlain();
        // tools
        add(tools(life), BorderLayout.PAGE_START);
        // show
        panel = new Show();
        panel.setPreferredSize(new Dimension((size + 2) * UI_FACTOR, (size + 2) * UI_FACTOR));
        panel.addMouseListener(new MouseHandler());
        add(panel, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JToolBar tools(Life life) {
        Rules rules = life.getRules();
        JToolBar tools = new JToolBar();
        tools.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        // delay
        final Control control = rules.delay;
        final JSlider delay = new JSlider(control.getMin().intValue(), control.getMax().intValue(), control.getValue().intValue());
        final JLabel value = new JLabel(String.format("%d", delay.getValue()));
        final JToggleButton pause = new JToggleButton(new ImageIcon(World.class.getResource(ICON_FILE)));
        delay.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                value.setText(String.format("%d", delay.getValue()));
                if (!pause.isSelected()) {
                    control.setValue(delay.getValue());
                }
            }
        });
        pause.setAction(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (pause.isSelected()) {
                    control.setValue(0);
                } else {
                    control.setValue(delay.getValue());
                    synchronized (World.this.life) {
                        World.this.life.notifyAll();
                    }
                }
            }
        });
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
        c.gridy = 0;
        tools.add(pause, c);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        tools.add(delay, c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridx = 2;
        c.gridy = 0;
        tools.add(new JLabel(control.getName() + ": "), c);
        c.gridx = 3;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        tools.add(value, c);
        // energy
        control(tools, rules.energyTotal, 1);
        // volume
        control(tools, rules.energyVolume, 2);
        // lightness
        control(tools, rules.unbearableLightnessOfBeing, 3);
        // minimum
        control(tools, rules.normMinimum, 4);
        // maximum
        control(tools, rules.normMaximum, 5);
        // mutable
        control(tools, rules.mutableFactor, 6);
        // limit
        control(tools, rules.unitLimit, 7);
        return tools;
    }

    private void control(JToolBar tools, final Control control, int row) {
        GridBagConstraints c = new GridBagConstraints();
        final double factor = control.getFactor();
        final JSlider slider = new JSlider((int) (control.getMin().doubleValue() * factor),
                                           (int) (control.getMax().doubleValue() * factor),
                                           (int) (control.getValue().doubleValue() * factor));
        final JLabel value = new JLabel(format(control, slider.getValue()));
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                value.setText(format(control, slider.getValue()));
                control.setValue(slider.getValue() / factor);
            }
        });
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 1;
        c.gridy = row;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        tools.add(slider, c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridx = 2;
        c.gridy = row;
        tools.add(new JLabel(control.getName() + ": "), c);
        c.gridx = 3;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        tools.add(value, c);
    }

    private String format(Control control, int value) {
        double factor = control.getFactor();
        return (factor > 1D) ? String.format("%." + (int) Math.log10(factor) + "f", value / factor) : String.format("%d", value);
    }

    @Override
    public void next() {
        panel.repaint();
    }

    private class Show extends JPanel {

        private AlphaComposite crystal = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

        private AlphaComposite visible = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.clearRect(UI_FACTOR, UI_FACTOR, size * UI_FACTOR, size * UI_FACTOR);
            java.util.List<Unit> units = plain.getUnits();
            Collections.sort(units, new Comparator<Unit>() {

                @Override
                public int compare(Unit o1, Unit o2) {
                    int compare = Boolean.compare(o1.isSelected(), o2.isSelected());
                    return (compare == 0) ? Double.compare(o1.getGenom().getSpeed(), o2.getGenom().getSpeed()) : compare;
                }
            });
            for (Unit unit : units) {
                Place place = unit.getPlace();
                double d = Genom.norm(unit.getGenom().getCargo()) * UI_FACTOR;
                double x = UI_FACTOR + place.getX() * UI_FACTOR - d / 2;
                double y = UI_FACTOR + place.getY() * UI_FACTOR - d / 2;
                Ellipse2D.Double circle = new Ellipse2D.Double(x, y, d, d);
                double rang = unit.ratio();
                if (unit.power() > 0D) {
                    g2d.setColor(new Color(Math.max(0, Math.min(255, (int) (255 * (1d - rang)))),
                                           Math.max(0, Math.min(255, (int) (255 * rang))),
                                           0));
                } else {
                    g2d.setColor(new Color(0, 0, Math.max(0, Math.min(255, (int) (255 * rang)))));
                }
                g2d.setComposite(crystal);
                g2d.fill(circle);
                if (unit.isSelected()) {
                    g2d.setStroke(new BasicStroke(3));
                    g2d.setColor(new Color(255, 255, 0));
                    g2d.setComposite(visible);
                    g2d.draw(circle);
                }
            }
        }

    }

    private class MouseHandler extends MouseAdapter {

        private long time = 0;

        @Override
        public void mousePressed(MouseEvent e) {
            time = System.currentTimeMillis();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            double x = (e.getX() - UI_FACTOR) / 1D / UI_FACTOR;
            double y = (e.getY() - UI_FACTOR) / 1D / UI_FACTOR;
            double cargo = (System.currentTimeMillis() - time) / 10;
            if (SwingUtilities.isRightMouseButton(e)) {
                for (Unit unit : plain.getUnits()) {
                    unit.setSelected(false);
                }
            } else if (SwingUtilities.isLeftMouseButton(e)) {
                if (cargo > 50D) {
                    Unit unit = new Unit(new Genom(Collections.singletonMap("cargo", cargo)));
                    unit.incrementTime();
                    unit.setEnergy(unit.cargo());
                    unit.setPlace(new Place(x, y));
                    plain.add(unit);
                } else {
                    Unit unit = plain.nearest(x, y);
                    if (unit != null) {
                        unit.setSelected(true);
                    }
                }
            }
            panel.repaint();
        }

    }
}
