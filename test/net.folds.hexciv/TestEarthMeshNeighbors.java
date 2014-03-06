package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Vector;

/**
 * Created by jasper on Feb 04, 2014.
 */
public class TestEarthMeshNeighbors {
    private final IcosahedralMesh mesh = new IcosahedralMesh();

    @Test
    public void testNeighboringIsReciprocal() {
        Vector<Integer> problems = mesh.listCellsWithUnreciprocatedNeighbors();
        if (problems.size() != 0) {
            Util.printList("earth problems", problems);
            mesh.printNeighborhood(problems.get(0));
            Assert.assertEquals(problems.size(), 0,
                    "Expected no cells in the earth mesh to have unreciprocated neighbors.");
        }
    }

    @Test
    public void testNeighborListsDoNotContainDuplicates() {
        Vector<Integer> problems = mesh.listCellsWithDuplicatedNeighbors();
        if (problems.size() != 0) {
            Util.printList("earth duplicated neighbor problems", problems);
            mesh.printInfo(problems.get(0));
            Assert.assertEquals(problems.size(), 0,
                    "Expected no cells in the earth mesh to have duplicated neighbors.");
        }
    }

    void testNeighbors(int cellId, int[] expectedNeighbors, String especiallyExpected) {
        Vector<Integer> neighbors = mesh.getNeighbors(cellId);
        boolean actualResult = Util.doListsContainSameItems(neighbors, expectedNeighbors);
        if (!actualResult) {
            mesh.printInfo(cellId);
            Vector<Integer> retry = mesh.getNeighbors(cellId);
            String message = "Expected earth cell "+cellId+"'s neighbors to include "+especiallyExpected+".";
            Assert.assertTrue(actualResult, message);
        }
    }

    @Test
    public void testNeighborsEarth0003() {
        // mesh.printInfo(3);
        int[] expectedNeighbors={0,2,10,11,12,4};
        Vector<Integer> neighbors = mesh.getNeighbors(3);
        Assert.assertTrue(Util.doListsContainSameItems(neighbors, expectedNeighbors),
                          "Expected earth cell 3's neighbors to include 10.");
        Assert.assertEquals(mesh.getNeighbor(3, Directions.west), 10,
                            "Expected earth cell 3's northwest neighbor to be 10.");
    }

    @Test
    public void testNeighborsEarth0006() {
        int[] expectedNeighbors={0,5,16,17,18,1};
        testNeighbors(6, expectedNeighbors, "16");
    }

    @Test
    public void testNeighborsEarth2756() {
        int[] expectedNeighbors={2755,2863,2864,2757,2648,2647};
        testNeighbors(2756, expectedNeighbors, "2647 and 2648");
        Assert.assertEquals(mesh.getNeighbor(2756, Directions.southwest), 2647,
                "Expected earth cell 2756's southwest neighbor to be 2647.");
        Assert.assertEquals(mesh.getNeighbor(2756, Directions.southeast), 2648,
                "Expected earth cell 2756's southeast neighbor to be 2648.");
    }

    @Test
    public void testNeighborsEarth2774() {
        int[] expectedNeighbors={2773,2880,2881,2775,2666,2665};
        testNeighbors(2774, expectedNeighbors, "2880");
        Assert.assertEquals(mesh.getNeighbor(2774, Directions.northwest), 2880,
                "Expected earth cell 2774's northwest neighbor to be 2880.");
    }

    @Test
    public void testNeighborsEarth2880() {
        int[] expectedNeighbors={2879,2881,2772,2985,2773,2774};
        testNeighbors(2880, expectedNeighbors, "2774");
        Assert.assertEquals(mesh.getNeighbor(2880, Directions.southeast), 2774,
                "Expected earth cell 2880's southeast neighbor to be 2774.");
    }

    @Test
    public void testNeighborsEarth2881() {
        int[] expectedNeighbors={2880,2882,2985,2986,2774,2775};
        testNeighbors(2881, expectedNeighbors, "2774 and 2775");
        Assert.assertEquals(mesh.getNeighbor(2881, Directions.southwest), 2774,
                "Expected earth cell 2881's southwest neighbor to be 2774.");
        Assert.assertEquals(mesh.getNeighbor(2881, Directions.southeast), 2775,
                "Expected earth cell 2881's southeast neighbor to be 2775.");
    }

    @Test
    public void testNeighborsEarth3865() {
        int[] expectedNeighbors={3864,3866,3789,3936,3790,3791};
        testNeighbors(3865, expectedNeighbors, "3789");
        Assert.assertEquals(mesh.getNeighbor(3865, Directions.west), 3789,
                "Expected earth cell 3865's west neighbor to be 3789.");
    }

    @Test
    public void testNeighborsEarth3925() {
        int[] expectedNeighbors={3926,3990,3991,3924,3853,3854};
        testNeighbors(3925, expectedNeighbors, "3924");
        Assert.assertEquals(mesh.getNeighbor(3925, Directions.west), 3924,
                "Expected earth cell 3925's west neighbor to be 3924.");
    }

}
