package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jasper on Feb 13, 2014.
 */
public class TestDirections {

    @Test
    public void testDirections() {
        Assert.assertEquals(Directions.northwest.rotate( 60),Directions.west,
                            "Expected  60° CCW of northwest to be west.");
        Assert.assertEquals(Directions.northeast.rotate(180),Directions.southwest,
                            "Expected 180° CCW of northeast to be southwest.");
    }


    /*
        switch (rotation % 360) {
            case 60: {
                switch (dir) {
                    case northwest: return Directions.west;
                    case northeast: return Directions.northwest;
                    case east:      return Directions.northeast;
                    case southeast: return Directions.east;
                    case southwest: return Directions.southeast;
                    case west:      return Directions.southwest;
                    default:        return dir;
                }
            } case 120: {
                switch (dir) {
                    case northwest: return Directions.southwest;
                    case northeast: return Directions.west;
                    case east:      return Directions.northwest;
                    case southeast: return Directions.northeast;
                    case southwest: return Directions.east;
                    case west:      return Directions.southeast;
                    default:        return dir;
                }
            } case 180: {
                switch (dir) {
                    case northwest: return Directions.southeast;
                    case northeast: return Directions.southwest;
                    case east:      return Directions.west;
                    case southeast: return Directions.northwest;
                    case southwest: return Directions.northeast;
                    case west:      return Directions.east;
                    default:        return dir;
                }
            } case 240: {
                switch (dir) {
                    case northwest: return Directions.east;
                    case northeast: return Directions.southeast;
                    case east:      return Directions.southwest;
                    case southeast: return Directions.west;
                    case southwest: return Directions.northwest;
                    case west:      return Directions.northeast;
                    default:        return dir;
                }
            } case 300: {
                switch (dir) {
                    case northwest: return Directions.northeast;
                    case northeast: return Directions.east;
                    case east:      return Directions.southeast;
                    case southeast: return Directions.southwest;
                    case southwest: return Directions.west;
                    case west:      return Directions.northwest;
                    default:        return dir;
                }
            } default: {
                return dir;
            }
        }
*/

}
