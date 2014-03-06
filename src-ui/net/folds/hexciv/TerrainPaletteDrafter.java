package net.folds.hexciv;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class TerrainPaletteDrafter extends Drafter {

    TerrainPaletteDrafter(Graphics2D graphics2D, int hexSideInPixels,
                          TextDisplayer textDisplayer, Rectangle margins) {
        super(graphics2D, hexSideInPixels, textDisplayer, margins);
    }

    public void drawPalette(int spanInPixels, int heightInPixels,
                            TerrainTypes desiredTerrain, Vector<Boolean> desiredFeatures) {
        TerrainTypes[] allTypes = TerrainTypes.values();
        int numCells = allTypes.length;

        int internalMarginInPixels = (spanInPixels - numCells * hexWidthInPixels) / (numCells - 1);
        for (int i = 0; i < numCells; i++) {
            TerrainTypes terrain = allTypes[i];
            int xAtLeftEdgeOfHex = getLeftMargin() + i * (internalMarginInPixels + hexWidthInPixels);
            int yAdjustment = 6 * (i % 2);
            int yAtTopPointOfHex = getTopMargin() + yAdjustment;
            if (terrain == desiredTerrain) {
                Polygon highlight = new Polygon();
                int xLeft = xAtLeftEdgeOfHex - (internalMarginInPixels + 1) / 2;
                int xRight = xLeft + hexWidthInPixels + internalMarginInPixels;
                if (xRight > margins.getMaxX() - 8) {
                    xRight = (int) margins.getMaxX() - 8;
                }
                int yTop = getTopMargin() + 1;
                int yBottom = heightInPixels - 2;
                highlight.addPoint(xLeft, yTop);
                highlight.addPoint(xRight, yTop);
                highlight.addPoint(xRight, yBottom);
                highlight.addPoint(xLeft,  yBottom);
                Color highlightColor = new Color(255, 255, 153);
                fillPolygon(highlight, highlightColor);
            }
            Polygon hex = getHex(xAtLeftEdgeOfHex, yAtTopPointOfHex);
            Color color = terrain.getColor();
            fillPolygon(hex, color);
            String abbr = terrain.getAbbreviation();

            int abbrWidth = getWidthInPixels(abbr);
            int x = xAtLeftEdgeOfHex + hexWidthInPixels / 2 - abbrWidth / 2;
            textDisplayer.beginUsing(comp2D, x, 50 + 2 * yAdjustment, 12);
            textDisplayer.typeLine(abbr);
            textDisplayer.finishUsing();
            if (terrain == desiredTerrain) {
                Point cellCenter = new Point(xAtLeftEdgeOfHex + hexWidthInPixels / 2,
                                             yAtTopPointOfHex + hexSideInPixels);
                if (Features.railroad.isChosen(desiredFeatures)) {
                    drawSampleRailroad(cellCenter);
                } else if (Features.road.isChosen(desiredFeatures)) {
                    drawSampleRoad(cellCenter);
                }
                labelFeatures(cellCenter, terrain, desiredFeatures);
            }
        }
    }
    }