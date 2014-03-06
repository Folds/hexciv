package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jasper on Feb 01, 2014.
 */
public class TestPlanet {
    private final Planet earth = new Planet();

    @Test
    public void testPolarCircumference() {
        Assert.assertEquals(earth.polarCircumferenceInKilometers(), 40000.0, 0.01, "Expected 40,000 km.");
    }

    @Test
    public void testArea() {
        Assert.assertEquals(earth.areaInSquareKilometers(), 509295818, 0.5, "Expected 509,295,818 sq. km.");
    }

}
