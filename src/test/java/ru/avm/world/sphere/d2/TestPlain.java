package ru.avm.world.sphere.d2;

import junit.framework.Assert;
import org.junit.Test;
import ru.avm.world.sphere.Genom;
import ru.avm.world.sphere.Unit;

/**
 * 2D plain test.
 */
public class TestPlain {

    @Test
    public void testFind() {
        Plain plain = new Plain(10, 10);
        Unit unit;
        unit = new Unit(new Genom());
        unit.setPlace(new Place(1, 1));
        plain.add(unit);
        unit = new Unit(new Genom());
        unit.setPlace(new Place(2, 2));
        plain.add(unit);
        unit = new Unit(new Genom());
        unit.setPlace(new Place(3, 3));
        plain.add(unit);
        unit = new Unit(new Genom());
        unit.setPlace(new Place(4, 4));
        plain.add(unit);

        Place place = new Place(5, 5);
        unit = new Unit(new Genom());
        unit.setPlace(place);

        Assert.assertEquals(plain.nearest(unit, 1).size(), 0);
        Assert.assertEquals(plain.nearest(unit, 2).size(), 1);
        Assert.assertEquals(plain.nearest(unit, 3).size(), 2);
        Assert.assertEquals(plain.nearest(unit, 4).size(), 2);
        Assert.assertEquals(plain.nearest(unit, 5).size(), 3);
    }
}
