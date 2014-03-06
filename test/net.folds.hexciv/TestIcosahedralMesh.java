package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Vector;

/**
 * Created by jasper on Feb 01, 2014.
 */
public class TestIcosahedralMesh {
    private final IcosahedralMesh trivialMesh = new IcosahedralMesh(1);
    private final IcosahedralMesh earthMesh   = new IcosahedralMesh();

    @Test
    public void testTrivialRowCount() {
        Assert.assertEquals(trivialMesh.countRows(), 6, "Expected 6 rows on mesh with n=1");
    }

    @Test
    public void testTrivialCellCount() {
        Assert.assertEquals(trivialMesh.countCells(), 32, "Expected 32 cells on mesh with n=1");
    }

    @Test
    public void testTrivialPentagonCount() {
        int tally = 0;
        for(int cellId=0; cellId < trivialMesh.countCells(); cellId++) {
            if(trivialMesh.isPentagon(cellId)) {
                tally = tally + 1;
            }
        }
        Assert.assertEquals(tally, 12, "Expected 12 pentagons on mesh with n=1");
    }
    @Test
    public void testEarthRowCount() {
        Assert.assertEquals(earthMesh.countRows(), 61, "Expected 61 rows in default mesh (n=12)");
    }

    @Test
    public void testEarthCellCount() {
        Assert.assertEquals(earthMesh.countCells(), 4322, "Expected 4322 cells on default mesh (n=12)");
        int tally = 0;
        for(int row=0; row<=5*12; row++) {
            tally = tally + earthMesh.getNumCellsInRow(row);
            /*
            System.out.println("row=" + row + "; " +
                               "increment=" + earthMesh.getNumCellsInRow(row) + "; " +
                               "tally=" + tally);
            */
        }
        Assert.assertEquals(tally, 4322, "Expected 4322 tallied cells on default mesh (n=12)");
    }

    @Test
    public void testEarthRow() {
        int penultimateCellId = earthMesh.countCells()-2;
        int penultimateRow = earthMesh.getRow(penultimateCellId);
        Assert.assertEquals(penultimateRow,59,"Expected penultimate cell in default mesh to be in row 59.");
    }

    @Test
    public void testEarthPentagonCount() {
        int tally = 0;
        for(int cellId=0; cellId < earthMesh.countCells(); cellId++) {
            if(earthMesh.isPentagon(cellId)) {
                tally = tally + 1;
                /*
                if (earthMesh.getRow(cellId) == 24) {
                    System.out.println("cellId=" + cellId + "; " +
                            "row=" + earthMesh.getRow(cellId) + "; " +
                            "posInRow=" + earthMesh.getPositionInRow(cellId) + "; " +
                            "tally=" + tally);
                }
                */
            }
        }
        Assert.assertEquals(tally, 12, "Expected 12 pentagons on default mesh (n=12)");
    }

    @Test
    public void testEquatorLength() {
        Assert.assertEquals(earthMesh.getEquatorLength(),108,
                            "Expected equator to be 108 cells long on default mesh (n=12)");
        Assert.assertEquals(trivialMesh.getEquatorLength(),9,
                            "Expected equator to be 9 cells long on trivial mesh (n=1)");
    }

    @Test
    public void testDistanceInCellsBetweenPoles() {
        Assert.assertEquals(earthMesh.getDistanceInCellsBetweenPoles(), 60,
                "Expected earth's poles to be 60 rows apart.");
        Assert.assertEquals(trivialMesh.getDistanceInCellsBetweenPoles(), 5,
                "Expected trivial mesh's poles to be 5 rows apart.");
    }

    @Test
    public void testCellIdsGivenLongsAndLats() {
        Assert.assertEquals(earthMesh.getCellId(  0.0,-90.0),    0, "Expected south pole cellId = 0");
        Assert.assertEquals(earthMesh.getCellId(  0.0,  0.0), 2107, "Expected prime equator cellId = 2107");
        Assert.assertEquals(earthMesh.getCellId(180.0,  0.0), 2161, "Expected dateline equator cellId = 2161");
        Assert.assertEquals(earthMesh.getCellId(  0.0, 90.0), 4321, "Expected north pole cellId = 4321");
    }

    @Test
    public void testEstimateDistanceBetweenCells() {
        Assert.assertEquals(earthMesh.estimateDistanceInCells(0,0),0,
                            "Expected no distance between south pole and itself.");
        Assert.assertEquals(earthMesh.estimateDistanceInCells(0,4321),54,6,
                            "Expected distance between south and north pole ~ 54±6 cells");
        Assert.assertEquals(earthMesh.estimateDistanceInCells(4321,0),
                            earthMesh.estimateDistanceInCells(0,4321),
                           "Expected distance between north and south pole to not depend on direction of travel");
        Assert.assertEquals(earthMesh.estimateDistanceInCells(0,2107),27,3,
                            "Expected distance between south pole and prime equator ~ 27±3 cells");
        Assert.assertEquals(earthMesh.estimateDistanceInCells(0,2107),
                            earthMesh.estimateDistanceInCells(0,2161),
                            "Expected distance between south pole and equator to be same along equator.");
        Assert.assertEquals(earthMesh.estimateDistanceInCells(4321,2107),
                            earthMesh.estimateDistanceInCells(0,2107),
                            "Expected prime equator to be halfway between north and south pole.");
        Assert.assertEquals(earthMesh.estimateDistanceInCells(2107,2161),54,6,
                            "Expected distance between prime equator and dateline equator ~ 54±6 cells");
        Assert.assertEquals(earthMesh.estimateDistanceInCells(2161,2107),
                            earthMesh.estimateDistanceInCells(2107,2161),
                            "Expected distance along equator to not depend on direction of travel");
    }

    @Test
    public void testLatitude() {
        Assert.assertEquals(earthMesh.getLatitudeInDegrees(30.0),0.0,
                            "Expected equator to have lat=0 degrees");
        Assert.assertEquals(earthMesh.getLatitudeInDegrees(24.0),-17.46,0.01,
                            "Expected row 2*n to have lat ~ -17.46±0.01 degrees");
        Assert.assertEquals(earthMesh.getLatitudeInDegrees(12.0),-53.13,0.01,
                            "Expected row   n to have lat ~ -53.13±0.01 degrees");
        Assert.assertEquals(earthMesh.getLatitudeInDegrees( 0.0),-90,   0.01,
                            "Expected south pole to have lat = -90 degrees");
        Assert.assertEquals(earthMesh.getLatitudeInDegrees(18.0),-35.10,0.01,
                            "Expected row 18  to have lat ~ -35.10±0.01 degrees");
        Assert.assertEquals(earthMesh.getLatitudeInDegrees(19.0),-32.18,0.01,
                            "Expected row 19  to have lat ~ -32.18±0.01 degrees");

        Assert.assertEquals(earthMesh.getLatitudeInDegrees(36.0), 17.46,0.01,
                            "Expected row 3*n to have lat ~ 17.46±0.01 degrees");
        Assert.assertEquals(earthMesh.getLatitudeInDegrees(48.0), 53.13,0.01,
                            "Expected row 4*n to have lat ~ 53.13±0.01 degrees");
        Assert.assertEquals(earthMesh.getLatitudeInDegrees(60.0), 90,   0.01,
                            "Expected north pole to have lat = 90 degrees");
        Assert.assertEquals(earthMesh.getLatitudeInDegrees(42.0), 35.10,0.01,
                            "Expected row 18  to have lat ~ 35.10±0.01 degrees");
        Assert.assertEquals(earthMesh.getLatitudeInDegrees(41.0), 32.18,0.01,
                            "Expected row 19  to have lat ~ 32.18±0.01 degrees");
    }

    @Test
    public void testDistanceBetweenCells() {
        Assert.assertEquals(earthMesh.getDistanceInCells(0,0),0,
                            "Expected no distance between south pole and itself.");
        Assert.assertEquals(earthMesh.getDistanceInCells(0,1),1,
                            "Expected one cell distance between south pole and adjacent cell (#1).");
        Assert.assertEquals(earthMesh.getDistanceInCells(0,7),2,
                            "Expected 2 cell distance between south pole and cell two rows away (#7).");
        Assert.assertEquals(earthMesh.getDistanceInCells(2107,2104),3,
                            "Expected 3 cell distance between prime equator and nearby cell.");
        Assert.assertEquals(earthMesh.getDistanceInCells(2161, 2594), 4,
                "Expected 4 cell distance between dateline equator and nearby cell.");
        Assert.assertEquals(earthMesh.getDistanceInCells(0,4321),60,
                            "Expected distance between south and north pole = 60 cells");
        Assert.assertEquals(earthMesh.getDistanceInCells(4321,0),60,
                            "Expected distance between north and south pole = 60 cells");
        Assert.assertEquals(earthMesh.getDistanceInCells(0, 2107), 30,
                            "Expected distance between south pole and prime equator = 30 cells");
        Assert.assertEquals(earthMesh.getDistanceInCells(4321,2107),30,
                            "Expected prime equator to be halfway between north and south pole.");
        Assert.assertEquals(earthMesh.getDistanceInCells(0, 2161),30,
                            "Expected distance between south pole and equator to be same along equator.");
        Assert.assertEquals(earthMesh.getDistanceInCells(2107,2161),54,
                            "Expected distance between prime equator and dateline equator = 54 cells");
        Assert.assertEquals(earthMesh.getDistanceInCells(2161, 2107),54,
                            "Expected distance between dateline equator and prime equator = 54 cells");
    }

    @Test
    public void testSorting() {
        Vector<Integer> sortedNeighbors = earthMesh.getSortedNeighbors(20);
        int southeastValue = Directions.southeast.getValue();
        int southeastNeighbor = sortedNeighbors.get(southeastValue);
        Assert.assertEquals(southeastNeighbor, 8, "Expected southeast neighbor of cell 20 to be 8.");
    }
}
