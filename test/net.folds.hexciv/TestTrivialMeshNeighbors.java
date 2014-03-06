package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Vector;

/**
 * Created by jasper on Feb 04, 2014.
 */
public class TestTrivialMeshNeighbors {
    private final IcosahedralMesh mesh = new IcosahedralMesh(1);

    @Test
    public void testNeighboringIsReciprocal() {
        Vector<Integer> trivialProblems = mesh.listCellsWithUnreciprocatedNeighbors();
        if (trivialProblems.size() != 0) {
            Util.printList("trivialProblems", trivialProblems);
            mesh.printNeighborhood(trivialProblems.get(0));
            Assert.assertEquals(trivialProblems.size(), 0,
                                "Expected no cells in the trivial mesh to have unreciprocated neighbors.");
        }
    }

    void testNeighbors(int cellId, int[] expectedNeighbors, String especiallyExpected) {
        Vector<Integer> neighbors = mesh.getNeighbors(cellId);
        boolean actualResult = Util.doListsContainSameItems(neighbors, expectedNeighbors);
        if (!actualResult) {
            mesh.printInfo(cellId);
            Vector<Integer> retry = mesh.getNeighbors(cellId);
            String message = "Expected trivial cell "+cellId+"'s neighbors to include "+especiallyExpected+".";
            Assert.assertTrue(actualResult, message);
        }
    }

    @Test
    public void testNeighborsTrivial06() {
        int[] expectedNeighbors={0,5,14,15,1};
        testNeighbors(6, expectedNeighbors, "1 and 14");
        Assert.assertEquals(mesh.getNeighbor(6, Directions.southwest), 0,
                "Expected trivial cell 6's southwest neighbor to be 0.");
        Assert.assertEquals(mesh.getNeighbor(6, Directions.southeast), 0,
                "Expected trivial cell 6's southeast neighbor to be 0.");
        Assert.assertEquals(mesh.getNeighbor(6, Directions.east), 1,
                "Expected trivial cell 6's east neighbor to be 1.");
        Assert.assertEquals(mesh.getNeighbor(6, Directions.northwest), 14,
                "Expected trivial cell 6's northwest neighbor to be 14.");
    }

    @Test
    public void testNeighborsTrivial09() {
        int[] expectedNeighbors={2,8,17,18,10,3};
        testNeighbors(9, expectedNeighbors, "3");
    }

    @Test
    public void testNeighborsTrivial11() {
        int[] expectedNeighbors={10,19,20,12,3,4};
        testNeighbors(11, expectedNeighbors, "3");
    }

    @Test
    public void testNeighborsTrivial18() {
        int[] expectedNeighbors={17,26,27,19,10,9};
        testNeighbors(18, expectedNeighbors, "9");
    }

    @Test
    public void testNeighborsTrivial26() {
        int[] expectedNeighbors={16,25,31,27,18,17};
        testNeighbors(26, expectedNeighbors, "16");
    }

}