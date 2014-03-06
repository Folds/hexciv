package net.folds.hexciv;

import java.awt.*;

/**
 * Created by Jasper on Jan 8, 2012.
 */
public class MapDrafter extends Drafter {
    WorldMap map;

    MapDrafter(WorldMap argMap, Graphics2D graphics2D, int hexSideInPixels,
               Rectangle margins) {
        super(graphics2D, hexSideInPixels, null, margins);
        map = argMap;
        comp2D = graphics2D;
    }

    public void drawMap() {
        int numCells = (int) map.countCells();
        int currentRow = map.getRow(0) - 1;
        int currentSegment = map.getSegment(0);
        TerrainTypes currentTerrain = TerrainTypes.ocean;
        int firstCellInCurrentBatch = -1;
        for (int i = 0; i < numCells; i++) {
            int row = map.getRow(i);
            int segment = map.getSegment(i);
            TerrainTypes terrain = map.getTerrain(i);
            if (   (row != currentRow)
                || (segment != currentSegment)
                || (terrain != currentTerrain)
                || (i == numCells - 1)
               ) {
                if (firstCellInCurrentBatch >= 0) {
                    drawCells(firstCellInCurrentBatch, i - 1);
                }
                currentRow = row;
                currentSegment = segment;
                currentTerrain = terrain;
                firstCellInCurrentBatch = i;
            }
        }
        drawCells(numCells - 1, numCells - 1);
        drawLatitudes();
        drawLongitudes();
    }

    private void drawCells(int fromCellId, int toCellId) {
        int halfColWidth = hexWidthInPixels / 2;
        int xAtLeftEdgeOfHex = halfColWidth * map.getHalfCol(fromCellId) + getLeftMargin();
        int offsetInHexes = (int) (2 * map.numHexesFromEquatorToCenterOfPole());
        int v = map.getRow(fromCellId);
        int yAtTopPointOfHex = (offsetInHexes - v) * hexSideInPixels * 3 / 2 + getTopMargin();
        int numHexes = toCellId - fromCellId + 1;
        Rectangle multiHexArea = new Rectangle(xAtLeftEdgeOfHex, yAtTopPointOfHex,
                                               numHexes * hexWidthInPixels, 2 * hexSideInPixels);
        Rectangle redrawArea = comp2D.getClipBounds();
        if (multiHexArea.intersects(redrawArea)) {
            Polygon multiHex = getMultiHex(xAtLeftEdgeOfHex, yAtTopPointOfHex, numHexes);
            drawAndFillPolygon(multiHex, map.getTerrain(fromCellId).getColor());
        }
    }

    private void drawLatitudes() {
        for(int latitude=-75; latitude < 90; latitude = latitude + 15) {
            drawLatitude(latitude);
        }
    }

    private void drawLatitude(int latitude) {
        int mapHeightInHexSides = map.mapHeightInHexSides();
        int pixelsBetweenPoles = (mapHeightInHexSides - 2) * hexSideInPixels;
        double row = map.mesh.getRow((double) latitude);
        int y = (int) (getTopMargin() + hexSideInPixels + pixelsBetweenPoles - row * (hexSideInPixels * 3)/2 );
        Rectangle redrawArea = comp2D.getClipBounds();
        if (   (redrawArea.getMinY() > y + (hexSideInPixels * 3) / 2)
            || (redrawArea.getMaxY() < y - (hexSideInPixels * 3) / 2)
           ) {
            return;
        }
        int roundedRow = (int) row;
        int halfColWidth = hexWidthInPixels / 2;
        int extraX = 0;
        if (1.5 * row < 0.4 * (mapHeightInHexSides - 2)) {
            if (row - roundedRow < -1.0/3.0) {
                extraX = (int) ((-1) * Math.floor((3.0 * (roundedRow - row) - 1.0) * halfColWidth));
            } else if (row - roundedRow > 1.0/3.0) {
                extraX = (int) Math.floor((3.0 * (row - roundedRow) - 1.0) * halfColWidth);
            }
        } else if (1.5 * row > 0.6 * (mapHeightInHexSides - 2)) {
            if (row - roundedRow < -1.0/3.0) {
                extraX = (int) Math.floor((3.0 * (roundedRow - row) - 1.0) * halfColWidth);
            } else if (row - roundedRow > 1.0/3.0) {
                extraX = (int) ((-1) * Math.floor((3.0 * (row - roundedRow) - 1.0) * halfColWidth));
            }
        }
        int numCellsInRow = map.mesh.getNumCellsInRow(roundedRow);
        int firstCell = map.mesh.getCellId(roundedRow, 0);
        int lastCell = firstCell + map.mesh.getNumCellsInRow(roundedRow);
        int xLeft = halfColWidth * map.getHalfCol(firstCell) + getLeftMargin() - extraX;
        int xRight;
        Color color = new Color(255,0,0);
        for (int cellId = firstCell; cellId < lastCell + 1; cellId++) {
            if (map.mesh.isFirstCellInSegment(cellId)) {
                if (cellId != firstCell) {
                    xRight = halfColWidth * (2 + map.getHalfCol(cellId - 1)) + getLeftMargin() - 1 + extraX;
                    drawLine(xLeft, y, xRight, y, color);
                }
                xLeft = halfColWidth * map.getHalfCol(cellId) + getLeftMargin() - extraX;
            }
            if (cellId == lastCell) {
                xRight = halfColWidth * (2 + map.getHalfCol(cellId)) + getLeftMargin() - 1 + extraX;
                drawLine(xLeft, y, xRight, y, color);
            }
        }
    }

    private void drawLongitudes() {
        for(int longitude=0; longitude < 360; longitude = longitude + 30) {
            drawLongitude(longitude, -60, 60);
        }
        drawLongitude(15, -90, 90);
        for(int longitude=45; longitude < 360; longitude = longitude + 30) {
            drawLongitude(longitude, -75, 75);
        }
    }

    private void drawLongitude(int longitude, int southLatitude, int northLatitude) {
        if (   (southLatitude < -90) || (southLatitude > 90)
            || (northLatitude < -90) || (northLatitude > 90)
            || (southLatitude > northLatitude)) {
            return;
        }
        double polarLatitude = Degrees.arcsin(0.8);
        double tropicalLatitude = Degrees.arcsin(0.3);
        if (southLatitude < -polarLatitude) {
            drawLongitudeSegment(longitude, southLatitude, Math.min(northLatitude, -polarLatitude));
            if (northLatitude < -polarLatitude)    {return;}
        }
        if (southLatitude < -tropicalLatitude) {
            drawLongitudeSegment(longitude, Math.max(southLatitude,    -polarLatitude),
                                            Math.min(northLatitude, -tropicalLatitude));
            if (northLatitude < -tropicalLatitude) {return;}
        }
        if (southLatitude < tropicalLatitude) {
            drawLongitudeSegment(longitude, Math.max(southLatitude, -tropicalLatitude),
                                            Math.min(northLatitude,  tropicalLatitude));
            if (northLatitude <  tropicalLatitude) {return;}
        }
        if (southLatitude < polarLatitude) {
            drawLongitudeSegment(longitude, Math.max(southLatitude, tropicalLatitude),
                                            Math.min(northLatitude,    polarLatitude));
            if (northLatitude <     polarLatitude) {return;}
        }
        drawLongitudeSegment(longitude, Math.max(southLatitude, polarLatitude), northLatitude);
    }

    private void drawLongitudeSegment(int longitude, double southLatitude, double northLatitude) {
        if (   (southLatitude < -90) || (southLatitude > 90)
            || (northLatitude < -90) || (northLatitude > 90)
            || (southLatitude > northLatitude)) {
            return;
        }
        int southY = getYinPixels(southLatitude);
        int northY = getYinPixels(northLatitude);
        Rectangle redrawArea = comp2D.getClipBounds();
        if (   (redrawArea.getMinY() > northY + (hexSideInPixels * 3) / 2)
            || (redrawArea.getMaxY() < southY - (hexSideInPixels * 3) / 2)
                ) {
            return;
        }
        int southX = getXinPixels(longitude, southLatitude);
        int northX = getXinPixels(longitude, northLatitude);
        Color color = new Color(255,0,0);
        drawLine(northX, northY, southX, southY, color);
    }

    private int getXinPixels(int longitude, double latitude) {
        int cellId = map.mesh.getCellId(longitude, latitude);
        int halfColWidth = hexWidthInPixels / 2;
        double row = map.mesh.getRow(latitude);
        double rowLength = map.mesh.getRowLength(row);
        double cenRow = map.getRow(cellId);
        double cenRowLength = map.mesh.getRow(cenRow);
        double cenLongitude = map.mesh.getLongitudeInDegrees(cellId);
        double result = getLeftMargin();
        if (longitude % 60 == 0) {
            result = result + halfColWidth * (map.getHalfCol(cellId + 1) - 1);
        } else {
            result = result + halfColWidth * (map.getHalfCol(cellId) + 1);
        }
        if (rowLength > 0) {
            result = result + (longitude - cenLongitude) / 360.0 * cenRowLength * hexWidthInPixels;
        }
        int segment = map.getSegment(cellId);
        double dxPerDy = dxPerDy(latitude, longitude, segment);
        result = result + dxPerDy * (row - cenRow) * hexWidthInPixels;
        return (int) result;
    }

    private double dxPerDy(double latitude, double longitude, int segment) {
        double row = map.mesh.getRow(latitude);
        int n = map.mesh.n;
        if (row <= 0) {
            return 0;
        }
        if (row <= n) {
            if (Math.floor(longitude / 60.0) != segment) {
                return -0.5;
            }
            return ((longitude % 60.0) / 60.0) - 0.5;
        }
        if (row <= 2*n) {
            if (Math.floor(longitude / 120.0) != segment) {
                return -0.5;
            }
            return ((longitude % 120.0) / 120.0) - 0.5;
        }
        if (row <= 3*n) {
            return 0;
        }
        if (row <= 4*n) {
            if (Math.floor((longitude + 60.0) / 120.0) != segment) {
                return 0.5;
            }
            return 0.5 - (((longitude + 60.0) % 120.0) / 120.0);
        }
        if (row < 5*n) {
            if (Math.floor(longitude / 60.0) != segment) {
                return 0.5;
            }
            return 0.5 - ((longitude % 60.0) / 60.0);
        }
        return 0;
    }

    private int getYinPixels(double latitudeInDegrees) {
        int mapHeightInHexSides = map.mapHeightInHexSides();
        int pixelsBetweenPoles = (mapHeightInHexSides - 2) * hexSideInPixels;
        double row = map.mesh.getRow((double) latitudeInDegrees);
        return (int) (getTopMargin() + hexSideInPixels + pixelsBetweenPoles - row * (hexSideInPixels * 3)/2 );
    }

}