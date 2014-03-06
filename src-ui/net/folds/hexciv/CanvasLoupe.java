package net.folds.hexciv;

import java.awt.*;

/**
 * Created by jasper on Feb 14, 2014.
 */
public class CanvasLoupe extends Loupe {
    InsetMap insetMap;

    CanvasLoupe (WorldMap map, int hexWidthInPixels, int hexSideInPixels, Rectangle margins) {
        super(map, hexWidthInPixels, hexSideInPixels, margins);
    }

    protected Rectangle getAffectedArea(int cellId) {
        int index = insetMap.cellIds.indexOf(cellId);
        int row = insetMap.getRow(index);
        int col = insetMap.getColumn(index);
        int halfColWidth = hexWidthInPixels / 2;
        int halfCol = insetMap.getHalfCol(row, col);
        int xAtLeftEdgeOfHex = halfColWidth * halfCol + getLeftMargin() - halfColWidth;
        int centerYinPixels = margins.y + margins.height / 2;
        int yAtCenterOfHex = centerYinPixels + (insetMap.getCenterRow() - row) * hexSideInPixels * 3 / 2;
        int yAtTopPointOfHex = yAtCenterOfHex - hexSideInPixels ;
        return new Rectangle(xAtLeftEdgeOfHex - halfColWidth, yAtTopPointOfHex - hexSideInPixels,
                             2 * hexWidthInPixels, 4 * hexSideInPixels);
    }

    public int getCellId(int xInPixels, int yInPixels,
                         int centerXinPixels, int centerYinPixels,
                         int centerCellId) {
        updateInsetMap(centerXinPixels, centerYinPixels, centerCellId);
        int maxRow = insetMap.numRows - 1;

        int yAtCenterOfRowZero = centerYinPixels + insetMap.getCenterRow() * hexSideInPixels * 3 / 2;
        int yAtTopPointOfRowZero = yAtCenterOfRowZero - hexSideInPixels;
        int extraRow = 1 - ((maxRow - insetMap.getCenterRow()) % 2);
        int yBaseline = yAtTopPointOfRowZero - (maxRow + extraRow) * hexSideInPixels * 3 / 2;
        int netX = xInPixels - getLeftMargin() + hexWidthInPixels / 2;
        int netY = yInPixels - yBaseline;
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
        int row = (maxRow + extraRow) - 2 * cycleY + rowOffset;
        int halfColWidth = hexWidthInPixels / 2;
        int halfCol = netX / halfColWidth;
        // int colOffset = getMap.getOffsetOfRow(row);
        int colOffset = 0;
        if (dir.isWestward()) {
            colOffset = -1;
        }
        int col = cycleX + colOffset;

        if (row < 0) {
            row = 0;
        }
        if (row > maxRow) {
            row = maxRow;
        }
        if (col < 0) {
            col = 0;
        }
        if (col >= insetMap.numColumns) {
            col = insetMap.numColumns - 1;
        }
        return insetMap.getCellId(row, col);
    }

    void updateInsetMap(int centerXinPixels, int centerYinPixels,
                        int centerCellId) {

        int numHalfColumns = (2 * (int) (centerXinPixels - getLeftMargin())) / (hexWidthInPixels / 2);
        if (((2 * (int) (centerXinPixels - getLeftMargin())) % (hexWidthInPixels / 2)) > 0) {
            numHalfColumns = numHalfColumns + 1;
        }
        int numColumns = (numHalfColumns / 2) + 1;
        int numHexSides = (2 * (int) (centerYinPixels - getTopMargin())) / hexSideInPixels;
        if (((2 * (int) (centerYinPixels - getTopMargin())) % hexSideInPixels) > 0) {
            numHexSides = numHexSides + 1;
        }
        int numRows = 1 + 2 * ((numHexSides + 1) / 3);
        if (hasValidInsetMap(numRows, numColumns, centerCellId)) {
            return;
        }
        insetMap = new InsetMap(map.mesh, centerCellId, numRows, numColumns);
    }

    boolean hasValidInsetMap(int numRows, int numColumns, int centerCellId) {
        if (insetMap == null) {
            return false;
        }
        if (   (insetMap.numRows == numRows)
                && (insetMap.numColumns == numColumns)
                && (insetMap.getCellId(insetMap.getCenterIndex()) == centerCellId)
                ) {
            return true;
        }
        return false;
    }
}