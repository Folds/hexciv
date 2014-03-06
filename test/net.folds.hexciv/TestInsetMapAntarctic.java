package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Vector;

/**
 * Created by jasper on Feb 18, 2014.
 */
public class TestInsetMapAntarctic {
    int numRows = 13;
    int numColumns = 23;
    int centerCellId = 20;
    InsetMap insetMap;

    public TestInsetMapAntarctic() {
        WorldMap map = new WorldMap();
        insetMap = new InsetMap(map.mesh, centerCellId, numRows, numColumns);
    }

    @Test
    public void testConstructor() {
        Assert.assertEquals(insetMap.countCells(), numRows * numColumns,
                            "Expected 13 row x 23 col inset getMap to have 299 cells.");
        Assert.assertEquals(insetMap.getCenterRow(), numRows / 2,
                            "Expected center of 13rx23c inset getMap to be in row 6.");
        Assert.assertEquals(insetMap.getCenterColumn(), (numColumns - 1) / 2,
                            "Expected center of 13rx23c inset getMap to be in column 11.");
        Assert.assertEquals(insetMap.getCellId(6, 11), centerCellId,
                            "Expected center cell ID = 0");
        Assert.assertEquals(insetMap.getCellId(6,12), 21,
                            "Expected cell east of 20 to be 21.");
        Assert.assertEquals(insetMap.getCellId(5,12), 8,
                            "Expected cell southeast of 20 to be 8.");
    }

    @Test
    public void testCoil() {
        int numRows = 13;
        int numColumns = 23;
        WorldMap map = new WorldMap();
        int centerCellId = 0;
        InsetMap insetMap = new InsetMap(map.mesh, centerCellId, numRows, numColumns);
        Vector<Integer> coil = insetMap.getCoil();
        Assert.assertEquals(coil.size(), 299,
                            "Expected 13 row x 23 col inset getMap's coil to have 299 indexes.");
        Assert.assertEquals((int) coil.get(0), insetMap.getCenterIndex(),
                            "Expected inset getMap's coil's first cell to be center of getMap.");
        Assert.assertEquals((int) coil.get(1), insetMap.getCenterIndex() + 1,
                            "Expected inset getMap's coil's second cell to be immediately east of center of getMap.");
        Assert.assertEquals((int) coil.get(2), insetMap.getCenterIndex() - numColumns + 1);
        Assert.assertEquals((int) coil.get(3), insetMap.getCenterIndex() - numColumns);
        Assert.assertEquals((int) coil.get(4), insetMap.getCenterIndex() - 1);
        Assert.assertEquals((int) coil.get(8), insetMap.getCenterIndex() + 2);
        Assert.assertEquals((int) coil.get(12), insetMap.getCenterIndex() - 2 * numColumns - 1,
                            "Expected inset getMap's coil's 12th cell to be index 102.");
        Assert.assertEquals((int) coil.get(13), insetMap.getCenterIndex() - numColumns - 1,
                            "Expected inset getMap's coil's 13th cell to be index 125.");
        Assert.assertEquals((int) coil.get(18), insetMap.getCenterIndex() + 2 * numColumns + 1,
                            "Expected inset getMap's coil's 18th cell to be index 196.");
        Assert.assertEquals((int) coil.get(19), insetMap.getCenterIndex() + 2 * numColumns + 2,
                            "Expected inset getMap's coil's 19th cell to be index 197.");

//        Assert.assertEquals(coil.indexOf(-2), -1,
//                            "Did not expect to find any unset indexes in the coil.");
        Assert.assertEquals(coil.indexOf(-1), -1,
                            "Did not expect to find any invalid indexes in the coil.");
    }

    @Test
    public void testNeighboringIndex() {
        int numRows = 13;
        int numColumns = 23;
        WorldMap map = new WorldMap();
        int centerCellId = 0;
        InsetMap insetMap = new InsetMap(map.mesh, centerCellId, numRows, numColumns);
        Vector<Integer> coil = insetMap.getCoil();
        int firstNeighboringIndex = insetMap.getNeighboringIndex(coil, 1, -1);
        Assert.assertEquals(firstNeighboringIndex, 149,
                            "Expected first neighboring index of 150 to be 149.");
        int secondNeighboringIndex = insetMap.getNeighboringIndex(coil, 1, firstNeighboringIndex);
        Assert.assertNotEquals(secondNeighboringIndex, firstNeighboringIndex,
                            "Expected the first and second neighboring indexes to be invalid and/or different.");
        Assert.assertEquals(secondNeighboringIndex, -1,
                            "Expected an invalid second neighboring index of 150.");
    }

}
