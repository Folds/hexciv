package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jasper on Sep 24, 2011.
 */
public class TestMapPanel {
    private MapPanel pane;

    TestMapPanel() {
        WorldMap map = new WorldMap();
        pane = new MapPanel(map);
    }

    @Test
    public void testMinMapSize() {
        int w = 50; // pixels
        int h = 30; // pixels
        Assert.assertEquals(pane.hexWidthInPixels(w,  h),    4, "Expected hex at least 4 pixels wide.");
        Assert.assertEquals(pane.hexSideInPixels(w,   h),    2, "Expected hex at least 2 pixels on side.");
        Assert.assertEquals(pane.mapWidthInPixels(w,  h),  436, "Expected getMap at least 436 pixels wide.");
        Assert.assertEquals(pane.mapHeightInPixels(w, h),  184, "Expected getMap at least 184 pixels high.");
    }

    @Test
    public void testSmallMapSize() {
        int w = 800; // pixels
        int h = 200; // pixels
        Assert.assertEquals(pane.hexWidthInPixels(w,  h),    4, "Expected hex 4 pixels wide.");
        Assert.assertEquals(pane.hexSideInPixels(w,   h),    2, "Expected hex 2 pixels on side.");
        Assert.assertEquals(pane.mapWidthInPixels(w,  h),  436, "Expected getMap 436 pixels wide.");
        Assert.assertEquals(pane.mapHeightInPixels(w, h),  184, "Expected getMap 184 pixels high.");
    }

    @Test
    public void testMediumMapSize() {
        int w = 655; // pixels
        int h = 600; // pixels
        Assert.assertEquals(pane.hexWidthInPixels(w,  h),    6, "Expected hex 6 pixels wide.");
        Assert.assertEquals(pane.hexSideInPixels(w,   h),    4, "Expected hex 4 pixels on side.");
        Assert.assertEquals(pane.mapWidthInPixels(w,  h),  654, "Expected getMap 654 pixels wide.");
        Assert.assertEquals(pane.mapHeightInPixels(w, h),  368, "Expected getMap 368 pixels high.");
    }

    @Test
    public void testLargeMapSize() {
        int w = 9000; // pixels
        int h = 4000; // pixels
        Assert.assertEquals(pane.hexWidthInPixels(w,  h),   72, "Expected hex 4 pixels wide.");
        Assert.assertEquals(pane.hexSideInPixels(w,   h),   42, "Expected hex 42 pixels on side.");
        Assert.assertEquals(pane.mapWidthInPixels(w,  h), 7848, "Expected getMap 7,848 pixels wide.");
        Assert.assertEquals(pane.mapHeightInPixels(w, h), 3864, "Expected getMap 3,864 pixels high.");
    }

}
