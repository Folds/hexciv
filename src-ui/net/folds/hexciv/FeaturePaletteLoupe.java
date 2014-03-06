package net.folds.hexciv;

/**
 * Created by jasper on Feb 23, 2014.
 */
public class FeaturePaletteLoupe {
    int hexSideInPixels;
    int topMargin;
    int internalMarginInPixels;

    FeaturePaletteLoupe(int hexSideInPixels, int topMargin, int spanInPixels) {
        this.hexSideInPixels = hexSideInPixels;
        this.topMargin = topMargin;
        this.internalMarginInPixels = internalMarginInPixels;
        int numCells = 6;
        internalMarginInPixels = (spanInPixels - numCells * 2 * hexSideInPixels) / (numCells - 1);
    }

    Features getFeature(int mouseY) {
        int numFeatures = Features.count();
        if (hexSideInPixels <= 0) {
            return Features.getFeature(0);
        }
        int spacing = 2 * hexSideInPixels + internalMarginInPixels;
        int i = (mouseY - topMargin + internalMarginInPixels / 2) /  (spacing);
        if (i < 0) {
            return Features.getFeature(0);
        }
        if (i >= numFeatures) {
            return Features.getFeature(numFeatures - 1);
        }
        return Features.getFeature(i);
    }
}

