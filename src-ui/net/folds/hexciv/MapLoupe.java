package net.folds.hexciv;

import java.awt.*;

/**
 * Created by jasper on Jan 8, 2012.
 */
public class MapLoupe extends Loupe {

    MapLoupe(WorldMap map, int hexWidthInPixels, int hexSideInPixels, Rectangle margins) {
        super(map, hexWidthInPixels, hexSideInPixels, margins);
    }

    protected Rectangle getAffectedArea(int cellId) {
        int row = map.getRow(cellId);
        int halfColWidth = hexWidthInPixels / 2;
        int xAtLeftEdgeOfHex = halfColWidth * map.getHalfCol(cellId) + getLeftMargin();
        int offsetInHexes = (int) (2 * map.numHexesFromEquatorToCenterOfPole());
        int yAtTopPointOfHex = (offsetInHexes - row) * hexSideInPixels * 3 / 2 + getTopMargin();
        return new Rectangle(xAtLeftEdgeOfHex, yAtTopPointOfHex, hexWidthInPixels, 2 * hexSideInPixels);
    }

    public int getCellId(int xInPixels, int yInPixels) {

        int netX = xInPixels - getLeftMargin();
        int netY = yInPixels - getTopMargin();
        int phaseX = netX % hexWidthInPixels;
        int cycleX = netX / hexWidthInPixels;
        int phaseY = netY % (3 * hexSideInPixels);
        int cycleY = netY / (3 * hexSideInPixels);
        Directions dir = getCellOffset(phaseX, phaseY);
        int rowOffset = 0;
        if (dir.isNorthward()) {
            rowOffset = 1;
        }
        if (dir.isSouthward()) {
            rowOffset = -1;
        }
        int maxRow = map.getRow(map.countCells() - 1);
        int row = maxRow - 2 * cycleY + rowOffset;

        int halfColWidth = hexWidthInPixels / 2;
        int halfCol = netX / halfColWidth;
        // int colOffset = getMap.getOffsetOfRow(row);
        int colOffset = 0;
        if (dir.isWestward()) {
            colOffset = -1;
        }
        int col = cycleX + colOffset;
        int skippedCells = countSkippedCells(row, col);
        int positionInRow = col - skippedCells;
        return map.getCellId(row, positionInRow);
    }

    boolean isVoid(int row, int col) {
        int n = (map.numHexesFromEquatorToCenterOfPole() * 2) / 5;
        if ((col < 0) || (col >= 9*n) || (row < 0) || (row > 5*n)) {
            return true;
        }
        if ((row == 0) && (col > 0)) {
            return true;
        }
        if ((row == 5*n) && (col > 0)) {
            return true;
        }
        int tier = row / n;
        int slant = row / 2;
        int offset = row % 2;
        int slash = (col - slant + 3*n ) / n - 3;
        int whack = (col + slant + offset) / n;
        boolean trianglePointsNorth = ((tier + slash + whack) % 2 == 0);
        if (tier == 0) {
            return ((trianglePointsNorth) || (whack % 3 == 0));
        } else if (tier == 1) {
            return ((trianglePointsNorth) && (whack % 3 == 0));
        } else if (tier == 2) {
            return false;
        } else if (tier == 3) {
            return ((!trianglePointsNorth) && (whack % 3 == 0));
        } else return ((!trianglePointsNorth) || (whack % 3 == 0));
    }

    int positionInTriangle(int row, int col) {
        int n = (map.numHexesFromEquatorToCenterOfPole() * 2) / 5;
        int slant = row / 2;
        int offset = row % 2;
        int slashPos = (col - slant + 3*n ) % n;
        int whackPos = (col + slant + offset) % n;
        return Math.min(slashPos, whackPos);
    }

    int countSkippedCells(int row, int col) {
        int n = (map.numHexesFromEquatorToCenterOfPole() * 2) / 5;
        if ((col <= 0) || (row <= 0) || (row > 5*n)) {
            return col;
        }
        if (col >= 9*n) {
            return countSkippedCells(row, 9*n - 1) + col - 9*n + 1;
        }
        int tier = row / n;
        int slant = row / 2;
        int offset = row % 2;
        int slash = (col - slant + 3*n ) / n - 3;
        int whack = (col + slant + offset) / n;
        int bonus = 0;
        if (isVoid(row, col)) {
            bonus = positionInTriangle(row, col);
        }
        int countA = (tier + 1)*n - row; // width of a north-pointing triangle
        int countV = row - tier*n;       // width of a south-pointing triangle
        // countA + countV = (tier + 1)*n - row + row - tier*n
        //                 = (tier - tier + 1)*n -row + row
        //                 = n

        if (tier == 0) {
            if (whack == 0) {
                return col;
            } else {
                return n - slant - offset + (whack-1)*countA + (slash/3)*countV + bonus;
            }
        } else if (tier == 1) {
            if (whack == 0) {
                return col;
            } else {
                return n - slant - offset + (slash/3)*countA + bonus;
            }
        } else if (tier == 2) {
            return 0;
        } else if (tier == 3) {
            return ((slash + 3)/3)*countV + bonus;
        } else {
            return slant - 2*n + (slash+2)*countV + ((whack-1) / 3)*countA+bonus;
        }
    }


}