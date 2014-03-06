package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.awt.*;

/**
 * Created by jasper on Feb 14, 2014.
 */
public class TestCanvasLoupe {
    WorldMap map;
    CanvasLoupe loupe;
    int centerXinPixels = 271;
    int centerYinPixels = 32;
    int centerCellId;

    TestCanvasLoupe() {
        map = new WorldMap();
        int hexWidthInPixels = 28;
        int hexSideInPixels = 16;
        int leftMarginInPixels = 2;
        int topMarginInPixels = 2;
        Rectangle margins = new Rectangle(leftMarginInPixels, topMarginInPixels,
                                          2 * centerXinPixels - 2 * leftMarginInPixels,
                                          2 * centerYinPixels - 2 * topMarginInPixels);
        loupe = new CanvasLoupe(map, hexWidthInPixels, hexSideInPixels, margins);
        centerCellId = 0;
    }

    int getCellId(int xInPixels, int yInPixels) {
        return loupe.getCellId(xInPixels, yInPixels,
                               centerXinPixels, centerYinPixels,
                               centerCellId);
    }

    void testCellId(int mouseX, int mouseY, int expectedCellId) {
        int actual = getCellId(mouseX, mouseY);
        if (actual != expectedCellId) {
            int retry = getCellId(mouseX, mouseY);
            String suffix = Util.getSuffix(expectedCellId);
            String message = "Expected mouse at (" + mouseX + "," + mouseY + ") " +
                    "to be in cell " + expectedCellId + ".";
            Assert.assertEquals(actual,expectedCellId, message);
        }
    }


    @Test
    public void test0000() {
        // testCellId(254, 80, 0); (for cen x,y = 229,80)
        testCellId(272, 32, 0);
    }

    @Test
    public void test0003() {
        // testCellId(282, 80, 3); (for cen x,y = 229,80)
        testCellId(300, 32, 3);
    }

}