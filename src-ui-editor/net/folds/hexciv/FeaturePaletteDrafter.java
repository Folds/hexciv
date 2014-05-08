package net.folds.hexciv;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class FeaturePaletteDrafter extends Drafter {

    FeaturePaletteDrafter(Graphics2D graphics2D, int hexSideInPixels,
                          TextDisplayer textDisplayer, Rectangle margins) {
        super(graphics2D, hexSideInPixels, textDisplayer,  margins);
    }

    public void drawPalette(int spanInPixels, TerrainTypes terrain, BitSet desiredFeatures) {
        String[] features = {"Bonus","Road","Railroad","Pollution","Irrigation","Mine","Village","City"};
        String[] abbr     = {"+",    "",    "",        "P",        "i",         "m" ,  "V",      "1"};
        features[0] = terrain.getBonusDescription();
        boolean[] valid = {true, true, true, true, terrain.isIrrigable(),
                terrain.isLand(), terrain.isLand(), terrain.isLand()};
        int numCells = features.length;

        int internalMarginInPixels = (spanInPixels - numCells * 2 * hexSideInPixels) / (numCells - 1);
        for (int i = 0; i < desiredFeatures.size(); i++) {
            if (desiredFeatures.get(i)) {
                Polygon highlight = new Polygon();
                int xLeft = 3;
                int xRight = xLeft + margins.width + 7;
                int yAtTopPointOfHex = getTopMargin() + i * (internalMarginInPixels + 2 * hexSideInPixels);
                int yTop = yAtTopPointOfHex - internalMarginInPixels / 2;
                int yBottom = yTop + internalMarginInPixels + 2 * hexSideInPixels;
                if (i == 0) {
                    yTop = 3;
                }
                if (i == desiredFeatures.size() - 1) {
                    yBottom = spanInPixels + 11;
                }
                highlight.addPoint(xLeft,  yTop);
                highlight.addPoint(xRight, yTop);
                highlight.addPoint(xRight, yBottom);
                highlight.addPoint(xLeft,  yBottom);
                Color highlightColor = new Color(255, 255, 153);
                fillPolygon(highlight, highlightColor);
            }
        }
        for (int i = 0; i < numCells; i++) {
            if (valid[i]) {
                String feature = features[i];
                int xAtLeftEdgeOfHex = getLeftMargin();
                int yAtTopPointOfHex = getTopMargin() + i * (internalMarginInPixels + 2 * hexSideInPixels);
                Polygon hex = getHex(xAtLeftEdgeOfHex, yAtTopPointOfHex);
                Color color = terrain.getColor();
                fillPolygon(hex, color);
                if (i == 1) {
                    Point center = new Point(xAtLeftEdgeOfHex + hexWidthInPixels / 2,
                            yAtTopPointOfHex + hexSideInPixels);
                    drawSampleRoad(center);
                } else if (i == 2) {
                    Point center = new Point(xAtLeftEdgeOfHex + hexWidthInPixels / 2,
                                             yAtTopPointOfHex + hexSideInPixels);
                    drawSampleRailroad(center);
                }
                int abbrWidth = getWidthInPixels(abbr[i]);
                int x1 = xAtLeftEdgeOfHex + (hexWidthInPixels - abbrWidth) / 2;
                int x2 = xAtLeftEdgeOfHex + hexWidthInPixels + 5;
                int y = yAtTopPointOfHex + (hexSideInPixels * 3 / 2) - getHeightInPixels(feature) / 3;
                textDisplayer.beginUsing(comp2D, x1, y, 12);
                textDisplayer.typeLine(abbr[i]);
                textDisplayer.finishUsing();
                textDisplayer.beginUsing(comp2D, x2, y, 12);
                textDisplayer.typeLine(feature);
                textDisplayer.finishUsing();
            }
        }
    }
}