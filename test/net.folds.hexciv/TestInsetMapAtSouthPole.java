package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Vector;

/**
 * Created by jasper on Feb 13, 2014.
 */
public class TestInsetMapAtSouthPole {
    int numRows = 13;
    int numColumns = 23;
    int centerCellId = 0;
    InsetMap insetMap;

    public TestInsetMapAtSouthPole() {
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
        Assert.assertEquals(insetMap.getCellId(6,12), 3,
                            "Expected cell east of 0 to be 3.");
        Assert.assertEquals(insetMap.getCellId(5,12), 4,
                            "Expected cell southeast of 0 to be 4.");
        Assert.assertEquals(insetMap.getCellId(4,10), 15,
                            "Expected cell east of 14 to be 15.");
        Assert.assertEquals(insetMap.getCellId(5,10), 16,
                            "Expected cell east of 15 to be 16.");
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

    @Test
    public void testSlashes() {
        int minSlash = insetMap.getMinSlash();
        int maxSlash = insetMap.getMaxSlash();
        int numSlashes = insetMap.countSlashes();
        Assert.assertEquals(maxSlash - minSlash + 1, numSlashes,
                            "Expected " + numSlashes + " slashes.");
        int tally = 0;
        for (int slash = minSlash; slash < maxSlash + 1; slash++) {
            int maxRow = insetMap.getMaxRowOfSlash(slash);
            Assert.assertTrue(maxRow < numRows,
                              "Expected slash " + slash + " to not extend past row" + (numRows - 1));
            int minRow = insetMap.getMinRowOfSlash(slash);
            Assert.assertTrue(minRow >= 0,
                              "Expected slash " + slash + " to not include negative rows.");
            int current = maxRow - minRow + 1;
            Assert.assertTrue(current >= 1, "Expected slash " + slash + " to include at least one row.");
            if (slash == minSlash) {
                Assert.assertTrue(current <= 2, "Expected first slash to not be longer than two rows.");
            }
            if (slash == maxSlash) {
                Assert.assertTrue(current <= 2, "Expected last slash to not be longer than two rows.");
            }
            tally = tally + current;
        }
        Assert.assertEquals(tally, insetMap.countCells(),
                            "Expected total cells in all slashes to equal total cells in all rows.");
    }

    @Test
    public void testWhacks() {
        int minWhack = insetMap.getMinWhack();
        int maxWhack = insetMap.getMaxWhack();
        int numWhacks = insetMap.countWhacks();
        Assert.assertEquals(maxWhack - minWhack + 1, numWhacks,
                            "Expected " + numWhacks + " whacks.");
        int tally = 0;
        for (int whack = minWhack; whack < maxWhack + 1; whack++) {
            int maxRow = insetMap.getMaxRowOfWhack(whack);
            Assert.assertTrue(maxRow < numRows,
                              "Expected whack " + whack + " to not extend past row" + (numRows - 1));
            int minRow = insetMap.getMinRowOfWhack(whack);
            Assert.assertTrue(minRow >= 0,
                              "Expected whack " + whack + " to not include negative rows.");
            int current = maxRow - minRow + 1;
            Assert.assertTrue(current >= 1, "Expected whack " + whack + " to include at least one row.");
            if (whack == minWhack) {
                Assert.assertTrue(current <= 2, "Expected first whack to not be longer than two rows.");
            }
            if (whack == maxWhack) {
                Assert.assertTrue(current <= 2, "Expected last whack to not be longer than two rows.");
            }
            tally = tally + current;
        }
        Assert.assertEquals(tally, insetMap.countCells(),
                "Expected total cells in all whacks to equal total cells in all rows.");
    }
}
