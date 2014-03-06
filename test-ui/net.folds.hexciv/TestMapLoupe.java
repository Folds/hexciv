package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.awt.*;

/**
 * Created by jasper on Feb 07, 2014.
 */
public class TestMapLoupe {
    private WorldMap map;
    private MapLoupe loupe;

    TestMapLoupe() {
        map = new WorldMap();
        int hexWidthInPixels = 4;
        int hexSideInPixels = 2;
        int leftMarginInPixels = 4;
        int topMarginInPixels = 2;
        Rectangle margins = new Rectangle(leftMarginInPixels, topMarginInPixels, 600, 200);
        loupe = new MapLoupe(map, hexWidthInPixels, hexSideInPixels, margins);
    }

    int getCellId(int mouseX, int mouseY) {
        return loupe.getCellId(mouseX, mouseY);
    }

    void testPositionInRow(int mouseX, int mouseY, int expectedPositionInRow) {
        int cellId = getCellId(mouseX, mouseY);
        CellSnapshot cell = map.getCellSnapshot(cellId);
        int actual = cell.positionInRow;
        int expected = expectedPositionInRow;
        if (actual != expected) {
            int retry = getCellId(mouseX, mouseY);
            String suffix = Util.getSuffix(expected);
            String message = "Expected mouse at (" + mouseX + "," + mouseY + ") " +
                    "to be in " + expected + suffix + " cell of row.";
            Assert.assertEquals(actual,expected, message);
        }
    }

    @Test
    public void test3540() {
        testPositionInRow(26, 53, 5);
    }

    @Test
    public void test3548() {
        testPositionInRow(58, 53, 13);
    }

    @Test
    public void test3549() {
        testPositionInRow(62, 53, 14);
        testPositionInRow(86, 53, 14);
        testPositionInRow(90, 53, 14);
        testPositionInRow(94, 53, 14);
    }

    @Test
    public void test3550() {
        testPositionInRow(98, 53, 15);
    }

    @Test
    public void test4105() {
        testPositionInRow(14,29, 0);
    }

    @Test
    public void test4106() {
        testPositionInRow(18,29, 1);
    }
}