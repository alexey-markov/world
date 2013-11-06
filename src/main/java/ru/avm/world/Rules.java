package ru.avm.world;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

/**
 * Set of controls.
 */
public class Rules {

    private static final String FILE_NAME = "values.properties";

    public final Control delay = new Control("delay", 1L, 1000L, 50L);

    public final Control energyTotal = new Control("energy", 0D, 50000D, 0D);

    public final Control energyVolume = new Control("volume", 0.01D, 3D, 0.01D, 100D);

    public final Control unbearableLightnessOfBeing = new Control("lightness", 0.01D, 3D, 1.0D, 100D);

    public final Control normMinimum = new Control("min", 0.0001D, 0.1D, 0.01D, 10000D);

    public final Control normMaximum = new Control("max", 0.9D, 0.9999D, 0.99D, 10000D);

    public final Control mutableFactor = new Control("factor", 0.01D, 1D, 0.1D, 100D);

    public final Control unitLimit = new Control("limit", 10, 5000, 1600);

    public void load() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(FILE_NAME));
        } catch (IOException e) {
            return;
        }
        for (Field field : Rules.class.getFields()) {
            if (Control.class.equals(field.getType())) {
                try {
                    Control control = (Control) field.get(this);
                    control.load(properties);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void save() {
        Properties properties = new Properties();
        for (Field field : Rules.class.getFields()) {
            if (Control.class.equals(field.getType())) {
                try {
                    Control control = (Control) field.get(this);
                    control.save(properties);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            properties.store(new FileWriter(FILE_NAME, false), new Date().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
