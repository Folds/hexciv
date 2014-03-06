package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jasper on Feb 02, 2014.
 */
public class TestDegrees {

    @Test
    public void testDistanceInDegrees() {
        Assert.assertEquals(Degrees.sphericalDistance(0, 0, 0, 0), 0, 0.0001,
                "Expected no distance between (0,0) vs. (0,0)");
        Assert.assertEquals(Degrees.sphericalDistance(0,0,180.0,0),180.0,0.0001,
                "Expected 180 degrees between (0,0) vs. (180,0)");
        Assert.assertEquals(Degrees.sphericalDistance(0,0,0,90),90.0,0.0001,
                "Expected 90 degrees between (0,0) vs. (0,90)");
        Assert.assertEquals(Degrees.sphericalDistance(0,0,90,90),90.0,0.0001,
                "Expected 90 degrees between (0,0) vs. (90,90)");
    }

}
