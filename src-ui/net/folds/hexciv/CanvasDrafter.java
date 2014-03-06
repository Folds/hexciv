package net.folds.hexciv;

import java.awt.*;
import java.util.Vector;

/**
 * Created by jasper on Feb 12, 2014.
 */
public class CanvasDrafter extends Drafter {
    WorldMap map;
    InsetMap insetMap;

    CanvasDrafter(WorldMap argMap, Graphics2D graphics2D, int hexSideInPixels,
                  TextDisplayer textDisplayer, Rectangle margins) {
        super(graphics2D, hexSideInPixels, textDisplayer, margins);
        map = argMap;
        comp2D = graphics2D;
    }

    protected void setMap(WorldMap map) {
        this.map = map;
    }

    public void drawMap(int widthBetweenMarginsInPixels, int heightBetweenMarginsInPixels, int centerCellId) {
        margins.width = widthBetweenMarginsInPixels;
        margins.height = heightBetweenMarginsInPixels;
        updateInsetMap(centerCellId);
        drawCells();
        drawLatitudes();
        drawLongitudes();
        drawRoads();
        drawRailroads();
        drawLabels();
    }

    void updateInsetMap(int centerCellId) {

        int numHalfColumns = margins.width / (hexWidthInPixels / 2);
        if ((margins.width % (hexWidthInPixels / 2)) > 0) {
            numHalfColumns = numHalfColumns + 1;
        }
        int numColumns = (numHalfColumns / 2) + 1;
        int numHexSides = margins.height / hexSideInPixels;
        if ((margins.height % hexSideInPixels) > 0) {
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

    private void drawCells() {
        for (int row=0; row< insetMap.numRows; row++) {
            drawCells(row);
        }
    }

    private void drawCells(int row) {
        TerrainTypes currentTerrain = TerrainTypes.ocean;
        int maxCol = insetMap.numColumns - 1;
        int firstColInCurrentBatch = insetMap.numColumns;
        for (int col=0; col < maxCol + 1; col++) {
            int cellId = insetMap.getCellId(row, col);
            if (cellId < 0) {
                if ((firstColInCurrentBatch % insetMap.numColumns) != col) {
                    drawCells(insetMap, row, firstColInCurrentBatch % insetMap.numColumns, col - 1);
                }
                firstColInCurrentBatch = col + 1;
            } else {
                TerrainTypes terrain = map.getTerrain(insetMap.getCellId(row, col));
                if ((terrain != currentTerrain) || (col == maxCol)) {
                    int lastCol = col - 1;
                    if ((terrain == currentTerrain) && (col == maxCol)) {
                        lastCol = col;
                    }
                    drawCells(insetMap, row, firstColInCurrentBatch % insetMap.numColumns, lastCol);
                    currentTerrain = terrain;
                    firstColInCurrentBatch = lastCol + 1;
                }
            }
        }
        drawCells(insetMap, row, maxCol, maxCol);
    }

    public void drawCells(InsetMap insetMap, int row, int fromColumn, int toColumn) {
        int halfColWidth = hexWidthInPixels / 2;
        int halfCol = insetMap.getHalfCol(row, fromColumn);
        int xAtLeftEdgeOfHex = halfColWidth * halfCol + getLeftMargin() - halfColWidth;

        int numHexes = toColumn - fromColumn + 1;
        int yAtCenterOfHex = getCenterYinPixels() + (insetMap.getCenterRow() - row) * hexSideInPixels * 3 / 2;
        int yAtTopPointOfHex = yAtCenterOfHex - hexSideInPixels ;
        Rectangle multiHexArea = new Rectangle(xAtLeftEdgeOfHex, yAtTopPointOfHex,
                                               numHexes * hexWidthInPixels, 2 * hexSideInPixels);
        Rectangle redrawArea = comp2D.getClipBounds();
        if (multiHexArea.intersects(redrawArea)) {
            if (!isVoid(insetMap.getIndex(row, fromColumn))) {
                Polygon multiHex = getMultiHex(xAtLeftEdgeOfHex, yAtTopPointOfHex, numHexes);
                int fromCellId = insetMap.getCellId(row, fromColumn);
                fillPolygon(multiHex, map.getTerrain(fromCellId).getColor());
            }
        }
    }

    private void drawLabels() {
        for (int row=0; row < insetMap.numRows; row++) {
            for (int col=0; col < insetMap.numColumns; col++) {
                int cellId = insetMap.getCellId(row, col);
                if ((cellId >= 0) && (cellId < map.countCells())) {
                    Point cellCenter = getCellCenter(row, col);
                    TerrainTypes terrain = map.getTerrain(cellId);
                    Vector<Boolean> features = map.getFeatures(cellId);
                    labelFeatures(cellCenter, terrain, features);
                }
            }
        }
    }

    private void drawRoads() {
        drawSlashRoads();
        drawWhackRoads();
        drawEastWestRoads();
    }

    private void drawRailroads() {
        drawSlashRailroads();
        drawWhackRailroads();
        drawEastWestRailroads();
    }

    private void drawSlashRoads() {
        int minSlash = insetMap.getMinSlash();
        int maxSlash = insetMap.getMaxSlash();
        for (int slash = minSlash; slash < maxSlash + 1; slash++) {
            Slash band = new Slash(slash);
            band.drawRoads();
        }
    }

    private void drawWhackRoads() {
        int minWhack = insetMap.getMinWhack();
        int maxWhack = insetMap.getMaxWhack();
        for (int whack = minWhack; whack < maxWhack + 1; whack++) {
            Whack band = new Whack(whack);
            band.drawRoads();
        }
    }

    private void drawEastWestRoads() {
        for (int row = 0; row < insetMap.numRows; row++) {
            Row band = new Row(row);
            band.drawRoads();
        }
    }

    private void drawSlashRailroads() {
        int minSlash = insetMap.getMinSlash();
        int maxSlash = insetMap.getMaxSlash();
        for (int slash = minSlash; slash < maxSlash + 1; slash++) {
            Slash band = new Slash(slash);
            band.drawRailroads();
        }
    }

    private void drawWhackRailroads() {
        int minWhack = insetMap.getMinWhack();
        int maxWhack = insetMap.getMaxWhack();
        for (int whack = minWhack; whack < maxWhack + 1; whack++) {
            Whack band = new Whack(whack);
            band.drawRailroads();
        }
    }

    private void drawEastWestRailroads() {
        for (int row = 0; row < insetMap.numRows; row++) {
            Row band = new Row(row);
            band.drawRailroads();
        }
    }

    private boolean isVoid(int index) {
        int cellId = insetMap.getCellId(index);
        if ((cellId < 0) || (cellId >= map.countCells())) {
            return true;
        }
        return false;
    }

    private boolean doesIndexPointToFeature(int index, Directions dir, Features feature) {
        int neighboringCellId = insetMap.getNeighboringCellIdIgnoringRotation(index, dir);
        Vector<Boolean> neighborFeatures = map.getFeatures(neighboringCellId);
        return feature.isChosen(neighborFeatures);
    }

    protected class Band {
        protected int tier;
        Directions backwards;
        Directions forwards;

        protected Band(int tier) {
            this.tier = tier;
            backwards = Directions.east;
            forwards  = Directions.west;
        }

        protected void drawRailroads() {
            int startItem = -2;
            int endItem = -2;
            boolean startAtCellCenter = false;
            boolean endAtCellCenter = false;
            boolean roadInProgress = false;
            boolean roadJustEnded = false;
            int minItem = getMinItem();
            int maxItem = getMaxItem();
            for (int item = minItem; item < maxItem + 1; item++) {
                int index = getIndex(item);
                int cellId = insetMap.getCellId(index);
                boolean isVoid = isVoid(index);
                Vector<Boolean> cellFeatures = map.getFeatures(cellId);
                boolean railroad = Features.railroad.isChosen(cellFeatures);
                if (railroad) {
                    if (roadInProgress) {
                        roadInProgress = true;
                        roadJustEnded = false;
                    } else {
                        roadInProgress = true;
                        roadJustEnded = false;
                        int prevIndex = getIndex(item - 1);
                        if ((item == 0) || (isVoid(prevIndex))) {
                            if (doesIndexPointToFeature(index, backwards, Features.railroad)) {
                                startAtCellCenter = false;
                                startItem = item - 1;
                            } else {
                                startAtCellCenter = true;
                                startItem = item;
                            }
                        } else {
                            startAtCellCenter = true;
                            startItem = item;
                        }
                    }
                } else if (isVoid) {
                    if (roadInProgress) {
                        roadInProgress = false;
                        roadJustEnded = true;
                        int prevIndex = getIndex(item - 1);
                        if (doesIndexPointToFeature(prevIndex, forwards, Features.railroad)) {
                            endAtCellCenter = false;
                            endItem = item;
                        } else {
                            endAtCellCenter = true;
                            endItem = item - 1;
                        }
                    } else {
                        roadInProgress = false;
                        roadJustEnded = false;
                        endAtCellCenter = true;
                        endItem = item - 1;
                    }
                } else {
                    if (roadInProgress) {
                        roadInProgress = false;
                        roadJustEnded = true;
                        endAtCellCenter = true;
                        endItem = item - 1;
                    } else {
                        roadInProgress = false;
                        roadJustEnded = false;
                    }
                }
                if (item == maxItem) {
                    if (roadInProgress) {
                        if (doesIndexPointToFeature(index, forwards, Features.railroad)) {
                            roadInProgress = false;
                            roadJustEnded = true;
                            endAtCellCenter = false;
                            endItem = item + 1;
                        } else {
                            roadInProgress = false;
                            roadJustEnded = true;
                            endAtCellCenter = true;
                            endItem = item;
                        }
                    }
                }
                if (roadJustEnded) {
                    Bandlet bandlet = getBandlet(tier, startItem, endItem, startAtCellCenter, endAtCellCenter);
                    bandlet.draw(Features.railroad);
                }
            }
        }

        protected void drawRoads() {
            int startItem = -2;
            int endItem = -2;
            boolean startAtCellCenter = false;
            boolean endAtCellCenter = false;
            boolean roadInProgress = false;
            boolean roadJustEnded = false;
            int minItem = getMinItem();
            int maxItem = getMaxItem();
            for (int item = minItem; item < maxItem + 1; item++) {
                int index = getIndex(item);
                int cellId = insetMap.getCellId(index);
                boolean isVoid = isVoid(index);
                Vector<Boolean> cellFeatures = map.getFeatures(cellId);
                boolean road = Features.road.isChosen(cellFeatures);
                boolean railroad = Features.railroad.isChosen(cellFeatures);
                if (road) {
                    if (roadInProgress) {
                        roadInProgress = true;
                        roadJustEnded = false;
                    } else {
                        roadInProgress = true;
                        roadJustEnded = false;
                        int prevIndex = getIndex(item - 1);
                        if ((item == minItem) || (isVoid(prevIndex))) {
                            if (   (doesIndexPointToFeature(index, backwards, Features.road))
                                || (doesIndexPointToFeature(index, backwards, Features.railroad))
                               ) {
                                startAtCellCenter = false;
                                startItem = item - 1;
                            } else {
                                startAtCellCenter = true;
                                startItem = item;
                            }
                        } else {
                            startAtCellCenter = true;
                            startItem = item;
                        }
                    }
                } else if (railroad) {
                    if (roadInProgress) {
                        if (doesIndexPointToFeature(index, forwards, Features.road)) {
                            roadInProgress = true;
                            roadJustEnded = false;
                        } else {
                            roadInProgress = false;
                            roadJustEnded = true;
                            endAtCellCenter = true;
                            endItem = item;
                        }
                    } else {
                        roadInProgress = true;
                        roadJustEnded = false;
                        int prevIndex = getIndex(item - 1);
                        if ((item == minItem) || (isVoid(prevIndex))) {
                            if (doesIndexPointToFeature(index, backwards, Features.road)) {
                                startAtCellCenter = false;
                                startItem = item - 1;
                            } else {
                                startAtCellCenter = true;
                                startItem = item;
                            }
                        } else {
                            startAtCellCenter = true;
                            startItem = item;
                        }
                    }
                } else if (isVoid) {
                    if (roadInProgress) {
                        roadInProgress = false;
                        roadJustEnded = true;
                        int prevIndex = getIndex(item - 1);
                        if (   (doesIndexPointToFeature(prevIndex, forwards, Features.road))
                            || (doesIndexPointToFeature(prevIndex, forwards, Features.railroad))
                           ) {
                            endAtCellCenter = false;
                            endItem = item;
                        } else {
                            endAtCellCenter = true;
                            endItem = item - 1;
                        }
                    } else {
                        roadInProgress = false;
                        roadJustEnded = false;
                        endAtCellCenter = true;
                        endItem = item - 1;
                    }
                } else {
                    if (roadInProgress) {
                        roadInProgress = false;
                        roadJustEnded = true;
                        endAtCellCenter = true;
                        endItem = item - 1;
                    } else {
                        roadInProgress = false;
                        roadJustEnded = false;
                    }
                }
                if (item == maxItem) {
                    if (roadInProgress) {
                        if (   (doesIndexPointToFeature(index, forwards, Features.road))
                            || (doesIndexPointToFeature(index, forwards, Features.railroad))
                           ) {
                            roadInProgress = false;
                            roadJustEnded = true;
                            endAtCellCenter = false;
                            endItem = item + 1;
                        } else {
                            roadInProgress = false;
                            roadJustEnded = true;
                            endAtCellCenter = true;
                            endItem = item;
                        }
                    }
                }
                if (roadJustEnded) {
                    Bandlet bandlet = getBandlet(tier, startItem, endItem, startAtCellCenter, endAtCellCenter);
                    bandlet.draw(Features.road);
                }
            }
        }

        protected int getMinItem() {
            return 0;
        }

        protected int getMaxItem() {
            return insetMap.numColumns - 1;
        }

        protected int getIndex(int item) {
            return insetMap.getIndex(tier, item);
        }

        protected Bandlet getBandlet(int tier, int startItem, int endItem,
                                     boolean startAtCellCenter, boolean endAtCellCenter) {
            return new Bandlet(tier, startItem, endItem, startAtCellCenter, endAtCellCenter);
        }
    }

    protected class Bandlet {
        protected int tier;
        protected int startItem;
        protected int endItem;
        protected boolean startAtCellCenter;
        protected boolean endAtCellCenter;

        protected Bandlet(int tier, int startItem, int endItem, boolean startAtCellCenter, boolean endAtCellCenter) {
            this.tier = tier;
            this.startItem = startItem;
            this.endItem = endItem;
            this.startAtCellCenter = startAtCellCenter;
            this.endAtCellCenter = endAtCellCenter;
        }

        protected void draw(Features feature) {
            if ((startItem == endItem) && (!startAtCellCenter) && (!endAtCellCenter)) {
                return;
            }
            Color color = feature.getColor();
            BasicStroke stroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
            Point startCellCenter = getCellCenter(tier, startItem);
            Point endCellCenter = getCellCenter(tier, endItem);
            if ((startItem == endItem) && (startAtCellCenter) && (endAtCellCenter)) {
                drawRoad(startCellCenter, endCellCenter, color, stroke);
                return;
            }
            Point start = getStartPoint();
            Point end = getEndPoint();
            drawRoad(start, end, color, stroke);
        }

        protected Point getStartPoint() {
            return new Point(0, 0);
        };

        protected Point getEndPoint() {
            return new Point(0, 0);
        };

    }

    protected class Row extends Band {
        Directions backwards;
        Directions forwards;

        protected Row(int tier) {
            super(tier);
            backwards = Directions.west;
            forwards = Directions.east;
        }

        protected Rowlet getBandlet(int tier, int startItem, int endItem,
                                    boolean startAtCellCenter, boolean endAtCellCenter) {
            return new Rowlet(tier, startItem, endItem, startAtCellCenter, endAtCellCenter);
        }
    }

    protected class Rowlet extends Bandlet {
        protected Rowlet(int tier, int startItem, int endItem, boolean startAtCellCenter, boolean endAtCellCenter) {
            super(tier, startItem, endItem, startAtCellCenter, endAtCellCenter);
        }

        protected Point getStartPoint() {
            Point cellCenter = getCellCenter(tier, startItem);
            Point result = new Point(cellCenter.x, cellCenter.y);
            if (!startAtCellCenter) {
                result.x = result.x + hexWidthInPixels / 2;
            }
            return result;
        }

        protected Point getEndPoint() {
            Point cellCenter = getCellCenter(tier, endItem);
            Point result = new Point(cellCenter.x, cellCenter.y);
            if (!endAtCellCenter) {
                result.x = result.x - hexWidthInPixels / 2 - 1;
            }
            return result;
        }

    }

    protected class Slash extends Band {
        protected Slash(int tier) {
            super(tier);
            backwards = Directions.southwest;
            forwards = Directions.northeast;
        }

        protected int getMinItem() {
            return insetMap.getMinRowOfSlash(tier);
        }

        protected int getMaxItem() {
            return insetMap.getMaxRowOfSlash(tier);
        }

        protected int getIndex(int item) {
            return insetMap.getIndexFromSlash(tier, item);
        }

        protected Slashlet getBandlet(int tier, int startItem, int endItem,
                                      boolean startAtCellCenter, boolean endAtCellCenter) {
            return new Slashlet(tier, startItem, endItem, startAtCellCenter, endAtCellCenter);
        }
    }

    private class Slashlet extends Bandlet {
        protected Slashlet(int tier, int startItem, int endItem, boolean startAtCellCenter, boolean endAtCellCenter) {
            super(tier, startItem, endItem, startAtCellCenter, endAtCellCenter);
        }

        protected Point getStartPoint() {
            int index = insetMap.getIndexFromSlash(tier, startItem);
            Point cellCenter = getCellCenterFromIndex(index);
            Point result = new Point(cellCenter.x, cellCenter.y);
            if (!startAtCellCenter) {
                result.x = result.x + hexWidthInPixels / 4 - 1;
                result.y = result.y - (3 * hexSideInPixels) / 4;
            }
            return result;
        }

        protected Point getEndPoint() {
            int index = insetMap.getIndexFromSlash(tier, endItem);
            Point cellCenter = getCellCenterFromIndex(index);
            Point result = new Point(cellCenter.x, cellCenter.y);
            if (!startAtCellCenter) {
                result.x = result.x - hexWidthInPixels / 4;
                result.y = result.y + (3 * hexSideInPixels) / 4;
            }
            return result;
        }
    }

    protected class Whack extends Band {
        protected Whack(int tier) {
            super(tier);
            backwards = Directions.southeast;
            forwards = Directions.northwest;
        }

        protected int getMinItem() {
            return insetMap.getMinRowOfWhack(tier);
        }

        protected int getMaxItem() {
            return insetMap.getMaxRowOfWhack(tier);
        }

        protected int getIndex(int item) {
            return insetMap.getIndexFromWhack(tier, item);
        }

        protected Whacklet getBandlet(int tier, int startItem, int endItem,
                                     boolean startAtCellCenter, boolean endAtCellCenter) {
            return new Whacklet(tier, startItem, endItem, startAtCellCenter, endAtCellCenter);
        }
    }

    private class Whacklet extends Bandlet {
        Whacklet(int tier, int startItem, int endItem, boolean startAtCellCenter, boolean endAtCellCenter) {
            super(tier, startItem, endItem, startAtCellCenter, endAtCellCenter);
        }

        protected Point getStartPoint() {
            int index = insetMap.getIndexFromWhack(tier, startItem);
            Point cellCenter = getCellCenterFromIndex(index);
            Point result = new Point(cellCenter.x, cellCenter.y);
            if (!startAtCellCenter) {
                result.x = result.x - hexWidthInPixels / 4;
                result.y = result.y - (3 * hexSideInPixels) / 4;
            }
            return result;
        }

        protected Point getEndPoint() {
            int index = insetMap.getIndexFromWhack(tier, endItem);
            Point cellCenter = getCellCenterFromIndex(index);
            Point result = new Point(cellCenter.x, cellCenter.y);
            if (!startAtCellCenter) {
                result.x = result.x + hexWidthInPixels / 4 - 1;
                result.y = result.y + (3 * hexSideInPixels) / 4;
            }
            return result;
        }
    }

    private void drawLatitudes() {
        for (int latitude = -75; latitude < 90; latitude = latitude + 15) {
            for (int longitude = 0; longitude < 360; longitude = longitude + 15) {
                drawLatitudeSegment(latitude, longitude, longitude + 15);
            }
        }
    }

    private void drawLongitudes() {
//        drawLongitudeSegment(30, 17, 18);
        for (int latitude = -60; latitude < 60; latitude = latitude + 15) {
            for (int longitude = 0; longitude < 360; longitude = longitude + 15) {
                drawLongitudeSegment(longitude, latitude, latitude + 15);
            }
        }
        for (int latitude = -90; latitude < -60; latitude = latitude + 15) {
            for (int longitude = 15; longitude < 360; longitude = longitude + 30) {
                drawLongitudeSegment(longitude, latitude, latitude + 15);
            }
        }
        for (int latitude =  60; latitude <  90; latitude = latitude + 15) {
            for (int longitude = 15; longitude < 360; longitude = longitude + 30) {
                drawLongitudeSegment(longitude, latitude, latitude + 15);
            }
        }
    }

    private void drawLatitudeSegment(int latitude, int startLongitude, int endLongitude) {
        int startCellId = map.mesh.getCellId((double) startLongitude, (double) latitude);
        int   endCellId = map.mesh.getCellId((double) endLongitude,   (double) latitude);
        if (   (!insetMap.alreadyIncludes(startCellId))
            && (!insetMap.alreadyIncludes(endCellId))
                ) {
            return;
        }
        if (   (insetMap.alreadyIncludes(startCellId))
            && (insetMap.alreadyIncludes(endCellId))
                ) {
            drawGraticuleSegment(startLongitude, latitude, endLongitude, latitude);
            return;
        }
        if (Math.abs(subtract(endLongitude, startLongitude)) > 1.9) {
            int midLongitude = Math.round(startLongitude + (int) subtract(endLongitude, startLongitude) / 2);
            drawLatitudeSegment(latitude, startLongitude, midLongitude);
            drawLatitudeSegment(latitude, midLongitude, endLongitude);
        }
    }

    private void drawLongitudeSegment(int longitude, int startLatitude, int endLatitude) {
        if (startLatitude == endLatitude) {
            return;
        }
        int startCellId = map.mesh.getCellId((double) longitude, (double) startLatitude);
        int   endCellId = map.mesh.getCellId((double) longitude, (double)   endLatitude);
        if (   (!insetMap.alreadyIncludes(startCellId))
            && (!insetMap.alreadyIncludes(endCellId))
                ) {
            return;
        }
        if (   (insetMap.alreadyIncludes(startCellId))
            && (insetMap.alreadyIncludes(endCellId))
                ) {
            if (endLatitude != startLatitude + 1) {
                if ((startLatitude <= -54) && (endLatitude >= -53)) {
                    drawLongitudeSegment(longitude, startLatitude, -54);
                    drawLongitudeSegment(longitude, -54, -53);
                    drawLongitudeSegment(longitude, -53, endLatitude);
                    return;
                }
                if ((startLatitude <= -18) && (endLatitude >= -17)) {
                    drawLongitudeSegment(longitude, startLatitude, -18);
                    drawLongitudeSegment(longitude, -18, -17);
                    drawLongitudeSegment(longitude, -17, endLatitude);
                    return;
                }
                if ((startLatitude <= 17) && (endLatitude >= 18)) {
                    drawLongitudeSegment(longitude, startLatitude, 17);
                    drawLongitudeSegment(longitude, 17, 18);
                    drawLongitudeSegment(longitude, 18, endLatitude);
                    return;
                }
                if ((startLatitude <= 53) && (endLatitude >= 54)) {
                    drawLongitudeSegment(longitude, startLatitude, 53);
                    drawLongitudeSegment(longitude, 53, 54);
                    drawLongitudeSegment(longitude, 54, endLatitude);
                    return;
                }
            }
            drawGraticuleSegment(longitude, startLatitude, longitude, endLatitude);
            return;
        }
        if (Math.abs(endLatitude - startLatitude) > 1) {
            int midLatitude = (startLatitude + endLatitude) / 2;
            drawLongitudeSegment(longitude, startLatitude, midLatitude);
            drawLongitudeSegment(longitude, midLatitude, endLatitude);
        }
    }

    private void drawGraticuleSegment(int startLongitude, int startLatitude,
                                      int   endLongitude, int   endLatitude) {
        int startCellId = map.mesh.getCellId((double) startLongitude, (double) startLatitude);
        int   endCellId = map.mesh.getCellId((double) endLongitude,   (double) endLatitude);
        if (   (!insetMap.alreadyIncludes(startCellId))
            || (!insetMap.alreadyIncludes(endCellId))
           ) {
            return;
        }
        Color color = new Color(255,0,0);
        Point start = getPoint(startLongitude, startLatitude);
        Point end   = getPoint(endLongitude,     endLatitude);

        drawLine(start.x, start.y, end.x, end.y, color);
    }

    private Vector<Integer> sortByCloseness(Vector<Integer> indexes, double longitude, double latitude) {
        int numChoices = indexes.size();
        Vector<Double> distances = new Vector<>(numChoices);
        for (int i=0; i < numChoices; i++) {
            int index = indexes.get(i);
            int cellId = insetMap.getCellId(index);
            double cellLongitude = map.mesh.getLongitudeInDegrees(cellId);
            double cellLatitude  = map.mesh.getLatitudeInDegrees(cellId);
            double distance = Degrees.sphericalDistance(longitude, latitude, cellLongitude, cellLatitude);
            distances.add(distance);
        }
        Vector<Integer> result = new Vector<>(numChoices);
        Vector<Integer> clone  = (Vector<Integer>) indexes.clone();
        for (int j=0; j < numChoices; j++) {
            int choice = getIndexOfMinimumValue(distances);
            result.add(clone.get(choice));
            distances.remove(choice);
            clone.remove(choice);
        }
        return result;
    }

    private int getIndexOfMinimumValue(Vector<Double> values) {
        if (values.size() < 1) {
            return -1;
        }
        int result = 0;
        double minValue = values.get(0);
        for (int i=0; i < values.size(); i++) {
            if (values.get(i) < minValue) {
                result = i;
                minValue = values.get(i);
            }
        }
        return result;
    }

    private double getDistanceInDegreesFromCellCenter(double longitude, double latitude, int cellId) {
        double cellLongitude = map.mesh.getLongitudeInDegrees(cellId);
        double cellLatitude  = map.mesh.getLatitudeInDegrees(cellId);
        return Degrees.sphericalDistance(longitude, latitude, cellLongitude, cellLatitude);
    }

    // for planar geometry:
    // http://www.calculator.net/triangle-calculator.html
    // returns the component of XY (projected onto XZ) divided by the length of XZ.
    private double splitTriangleAlongXZ(double distanceXY, double distanceYZ, double distanceXZ) {
        if (distanceXZ == 0) {
            return 0;
        }
        double d2XY = distanceXY * distanceXY;
        double d2YZ = distanceYZ * distanceYZ;
        double d2XZ = distanceXZ * distanceXZ;
       return (d2XZ + d2XY - d2YZ)/(2*d2XZ);
    }

    private class DoublePoint {
        double x;
        double y;

        DoublePoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        DoublePoint(Point arg) {
            this.x = arg.x;
            this.y = arg.y;
        }

        DoublePoint partwayTo(DoublePoint end, double partway) {
            return new DoublePoint(x + partway * (end.x - x),
                                   y + partway * (end.y - y));
        }

        DoublePoint plus(DoublePoint addend) {
            return new DoublePoint(x + addend.x, y + addend.y);
        }

        DoublePoint minus(DoublePoint subtend) {
            return new DoublePoint(x - subtend.x, y - subtend.y);
        }

        Point toPoint() {
            return new Point((int) x, (int) y);
        }
    }

    private class LongLat extends DoublePoint {
        LongLat(double longitude, double latitude) {
            super(longitude, latitude);
        }
        LongLat(int cellId) {
            super(map.mesh.getLongitudeInDegrees(cellId),
                  map.mesh.getLatitudeInDegrees(cellId));
        }
        Double getLongitude() {
            return x;
        }
        Double getLatitude() {
            return y;
        }

        Double sphericalDistance(LongLat arg) {
            return Degrees.sphericalDistance(getLongitude(), getLatitude(),
                                             arg.getLongitude(), arg.getLatitude());
        }
        LongLat partwayTo(LongLat end, double partway) {
            double startLongitude = getLongitude();
            double startLatitude  = getLatitude();
            double endLongitude   = end.getLongitude();
            double endLatitude    = end.getLatitude();
            double deltaLongitude = subtract(endLongitude, startLongitude);
            double deltaLatitude  = endLatitude - startLatitude;
            return new LongLat(x + partway * deltaLongitude,
                               y + partway * deltaLatitude);
        }
    }

    private Point getPoint(int longitude, int latitude, int cellA, int cellB, int cellC) {
        LongLat longLat  = new LongLat(longitude, latitude);
        LongLat longLatA = new LongLat(cellA);
        LongLat longLatB = new LongLat(cellB);
        LongLat longLatC = new LongLat(cellC);
        double distanceA  =  longLat.sphericalDistance(longLatA);
        double distanceB  =  longLat.sphericalDistance(longLatB);
        double distanceAB = longLatA.sphericalDistance(longLatB);
        double distanceAC = longLatA.sphericalDistance(longLatC);
        double distanceBC = longLatB.sphericalDistance(longLatC);
        double distanceC  =  longLat.sphericalDistance(longLatC);

        double alpha = splitTriangleAlongXZ(distanceA, distanceB, distanceAB);
        LongLat longLatD = longLatA.partwayTo(longLatB, alpha);
        double distanceD  = longLat.sphericalDistance(longLatD);
        double distanceCD = longLatC.sphericalDistance(longLatD);
        double beta = splitTriangleAlongXZ(distanceD, distanceC, distanceCD);
        double gamma = splitTriangleAlongXZ(distanceAB, distanceBC, distanceAC);
        DoublePoint a = new DoublePoint(getCellCenter(cellA));
        DoublePoint b = new DoublePoint(getCellCenter(cellB));
        DoublePoint c = new DoublePoint(getCellCenter(cellC));
        DoublePoint d = a.partwayTo(b, alpha);
        DoublePoint f = a.partwayTo(b, gamma);
        DoublePoint g = f.partwayTo(c, beta);
        DoublePoint e = d.minus(f).plus(g);
        return e.toPoint();
    }

    private Point getPoint(int longitude, int latitude) {
        int cellA = map.mesh.getCellId((double) longitude, (double) latitude);
        Point pointA = getCellCenter(cellA);
        if ((insetMap.numRows == 1) || (insetMap.numColumns == 1)) {
            return pointA;
        }
        int indexA = insetMap.cellIds.indexOf(cellA);

        Vector<Integer> neighbors = insetMap.getNeighboringIndexes(indexA);
        if (neighbors.size() < 2) {
            return pointA;
        }
        Vector<Integer> closest = sortByCloseness(neighbors, longitude, latitude);
        int indexB = closest.get(0);
        int cellB = insetMap.getCellId(indexB);
        int indexC = closest.get(1);
        int cellC = insetMap.getCellId(indexC);

        return getPoint(longitude, latitude, cellA, cellB, cellC);
    }

    private double subtract(double longitudeX, double longitudeY) {
        double result = (720.0 + longitudeX - longitudeY) % 360.0;
        if (result > 180.0) {
            result = 360.0 - result;
        }
        return result;
    }

    private Point getCellCenter(int cellId) {
        int index = insetMap.cellIds.indexOf(cellId);
        return getCellCenterFromIndex(index);
    }

    private Point getCellCenterFromIndex(int index) {
        int row = insetMap.getRow(index);
        int col = insetMap.getColumn(index);
        return getCellCenter(row, col);
    }

    private Point getCellCenter(int row, int column) {
        int halfColWidth = hexWidthInPixels / 2;
        int halfCol = insetMap.getHalfCol(row, column);
        int x = halfColWidth * halfCol + getLeftMargin();
        int y = getCenterYinPixels() + (insetMap.getCenterRow() - row) * hexSideInPixels * 3 / 2;
        return new Point(x, y);
    }

    protected int getCenterYinPixels() {
        return margins.x + margins.height / 2;
    }
}