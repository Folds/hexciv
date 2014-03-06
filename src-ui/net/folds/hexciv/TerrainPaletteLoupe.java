package net.folds.hexciv;

/**
 * Created by jasper on Feb 16, 2014.
 */
public class TerrainPaletteLoupe {
    int hexWidthInPixels;
    int hexSideInPixels;
    int leftMarginInPixels;
    int width;

    TerrainPaletteLoupe(int hexSideInPixels, int leftMarginInPixels, int width) {
        this.hexSideInPixels = hexSideInPixels;
        hexWidthInPixels = (int) Drafter.roundToNearestEvenNumber(Math.sqrt(3.0) * hexSideInPixels);
        this.leftMarginInPixels = leftMarginInPixels;
        this.width = width;
    }


    TerrainTypes getTerrain(int mouseX) {
        TerrainTypes[] allTypes = TerrainTypes.values();
        int numCells = allTypes.length;
        int spanInPixels = width - 9 - 5;
        int internalMarginInPixels = (spanInPixels - numCells * hexWidthInPixels) / (numCells - 1);
        int highlightWidth = internalMarginInPixels + hexWidthInPixels;
        int paletteIndex  =  (mouseX - leftMarginInPixels) /  highlightWidth;
        return allTypes[paletteIndex];
    }


}
