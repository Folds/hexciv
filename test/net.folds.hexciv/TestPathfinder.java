package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Oct 06, 2014.
 */
public class TestPathfinder {
    private final WorldMap map =  WorldMap.getEarthMap();


    @Test
    public void testCrowFliesAcrossItaly() {
        int andalusia = 3534;
        int sparta = 3453;
        Assert.assertEquals(map.getDistanceInCells(andalusia, sparta), 6, "Expected 6 cells.");
    }

    @Test
    public void testDetourAroundItaly() {
        int andalusia = 3534;
        int sparta = 3453;
        // BitSet allCells = map.getAllCells();
        Vector<Integer> region = map.getRegion(andalusia, 8);
        BitSet regionBits = Util.convertToBitSet(region);
        Pathfinder pathfinder = new Pathfinder(map, regionBits);
        int journey = pathfinder.getLandDistanceInCells(andalusia, sparta);
        Assert.assertEquals(journey, 8, "Expected 8 cells.");
    }

    @Test
    public void testCrowFliesAcrossMalta() {
        int tunisia = 3360;
        int pompeii = 3451;
        Assert.assertEquals(map.getDistanceInCells(tunisia, pompeii), 2, "Expected 2 cells.");
    }

    @Test
    public void testDetourAroundEasternMed() {
        int tunisia = 3360;
        int pompeii = 3451;
        // BitSet allCells = map.getAllCells();
        Vector<Integer> region = map.getRegion(tunisia, 12);
        BitSet regionBits = Util.convertToBitSet(region);
        Pathfinder pathfinder = new Pathfinder(map, regionBits);
        int journey = pathfinder.getLandDistanceInCells(tunisia, pompeii);
        Assert.assertEquals(journey, 20, "Expected 20 cells.");
    }

    @Test
    public void testCrowFliesAcrossAdriatic() {
        int roma = 3538;
        int albania = 3540;
        Assert.assertEquals(map.getDistanceInCells(roma, albania), 2, "Expected 2 cells.");
    }

    @Test
    public void testDetourAroundAdriatic() {
        int roma = 3538;
        int albania = 3540;
        // crow-path: 3538, 3539, 3540
        // left-path: 3538, 3622, 3623, 3540
        // right-path: 3538, 3541, 3538 = oops.
        Vector<Integer> region = map.getRegion(roma, 4);
        BitSet regionBits = Util.convertToBitSet(region);
        Pathfinder pathfinder = new Pathfinder(map, regionBits);
        int journey = pathfinder.getLandDistanceInCells(roma, albania);
        Assert.assertEquals(journey, 3, "Expected 3 cells.");
    }

}
