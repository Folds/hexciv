package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Vector;

/**
 * Created by jasper on May 28, 2014.
 */
public class TestUnit {
    @Test
    public void testSettler() {
        UnitType settler = UnitType.proposeSettler();
        Assert.assertTrue(settler.isSettler);
        Assert.assertTrue(settler.isTerrestrial);
        Assert.assertFalse(settler.isAerial);
        Assert.assertFalse(settler.isNaval);
    }

    @Test
    public void testNoAmphibians() {
        Vector<UnitType> choices = UnitType.getChoices();
        for (UnitType unit : choices) {
            Assert.assertTrue(unit.isTerrestrial || unit.isAerial || unit.isNaval);
            Assert.assertFalse(unit.isTerrestrial && unit.isAerial);
            Assert.assertFalse(unit.isTerrestrial && unit.isNaval);
            Assert.assertFalse(unit.isAerial && unit.isNaval);
        }
    }
}
