package net.folds.hexciv;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class Drafter {
    Graphics2D comp2D;
    int hexWidthInPixels;
    int hexSideInPixels;
    TextDisplayer textDisplayer;
    Rectangle margins;

    Drafter(Graphics2D graphics2D, int hexSideInPixels,
            TextDisplayer textDisplayer, Rectangle margins) {
        comp2D = graphics2D;
        this.hexSideInPixels = hexSideInPixels;
        hexWidthInPixels = (int) roundToNearestEvenNumber(Math.sqrt(3.0) * hexSideInPixels);
        this.textDisplayer = textDisplayer;
        this.margins = margins;
    }

    protected int getLeftMargin() {
        return margins.x;
    }

    protected int getTopMargin() {
        return margins.y;
    }

    protected static long roundToNearestEvenNumber(double arg) {
        return 2 * Math.round(arg / 2.0);
    }

    protected Polygon getHex(int xLeft, int yTop) {
        return getMultiHex(xLeft, yTop, 1);
    }

    protected Polygon getMultiHex(int xLeft, int yTop, int numHexes) {
        int numPoints = 8*numHexes;
        int x[] = new int[numPoints];
        int y[] = new int[numPoints];
        int yUpper = yTop + hexSideInPixels / 2;
        int yLower = yUpper + hexSideInPixels - 1;
        int yBottom = yTop + (hexSideInPixels * 2) - 1;
        for (int i=0; i<numHexes; i++) {
            x[4*i]     = xLeft + hexWidthInPixels * i;
            x[4*i + 1] = x[4*i] + hexWidthInPixels / 2 - 1;
            x[4*i + 2] = x[4*i] + hexWidthInPixels / 2;
            x[4*i + 3] = x[4*i] + hexWidthInPixels - 1;
            x[numPoints - 4*i - 1] = x[4*i];
            x[numPoints - 4*i - 2] = x[4*i + 1];
            x[numPoints - 4*i - 3] = x[4*i + 2];
            x[numPoints - 4*i - 4] = x[4*i + 3];
            y[4*i]     = yUpper;
            y[4*i + 1] = yTop;
            y[4*i + 2] = yTop;
            y[4*i + 3] = yUpper;
            y[numPoints - 4*i - 1] = yLower;
            y[numPoints - 4*i - 2] = yBottom;
            y[numPoints - 4*i - 3] = yBottom;
            y[numPoints - 4*i - 4] = yLower;
        }
        return new Polygon(x, y, x.length);
    }

    protected void fillPolygon(Polygon polygon, Color color, boolean drawOutline) {
        Color oldColor = comp2D.getColor();
        boolean isNewColor = true;
        if (oldColor == color) {
            isNewColor = false;
        }
        if (isNewColor) {
            comp2D.setColor(color);
        }
        comp2D.fillPolygon(polygon);
        if (drawOutline) {
            comp2D.drawPolygon(polygon);
        }
        if (isNewColor) {
            comp2D.setColor(oldColor);
        }
    }

    protected void fillPolygon(Polygon polygon, Color color) {
        fillPolygon(polygon, color, false);
    }

    protected void drawAndFillPolygon(Polygon polygon, Color color) {
        fillPolygon(polygon, color, true);
    }

    protected void drawLine(int x1, int y1, int x2, int y2, Color color, Stroke stroke) {
        Stroke oldStroke = comp2D.getStroke();
        boolean isNewStroke = true;
        if (oldStroke.equals(stroke)) {
            isNewStroke = false;
        }
        if (isNewStroke) {
            comp2D.setStroke(stroke);
        }
        drawLine(x1, y1, x2, y2, color);
        if (isNewStroke) {
            comp2D.setStroke(oldStroke);
        }
    }

    protected void drawLine(int x1, int y1, int x2, int y2, Color color) {
        Color oldColor = comp2D.getColor();
        boolean isNewColor = true;
        if (oldColor.equals(color)) {
            isNewColor = false;
        }
        if (isNewColor) {
            comp2D.setColor(color);
        }

        int minX = (int) margins.getMinX();
        if ((x1 < minX) && (x2 > minX)) {
            y1 = y1 + ((minX - x1) * (y2 - y1)) / (x2 - x1);
            x1 = minX;
        }
        int maxX = (int) margins.getMaxX();
        if ((x1 < maxX) && (x2 > maxX)) {
            y2 = y1 + ((maxX - x1) * (y2 - y1)) / (x2 - x1);
            x2 = maxX;
        }
        int minY = (int) margins.getMinY();
        if ((y1 < minY) && (y2 > minY)) {
            x1 = x1 + ((minY - y1) * (x2 - x1)) / (y2 - y1);
            y1 = minY;
        }
        int maxY = (int) margins.getMaxY();
        if ((y1 < maxY) && (y2 > maxY)) {
            x2 = x1 + ((maxY - y1) * (x2 - x1)) / (y2 - y1);
            y2 = maxY;
        }
        comp2D.drawLine(x1, y1, x2, y2);
        if (isNewColor) {
            comp2D.setColor(oldColor);
        }
    }

    //    http://www.coderanch.com/t/417236/java/java/Pixel-width-String
    int getWidthInPixels(String arg) {
        FontMetrics metrics = comp2D.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(arg, null);
        return (int) bounds.getWidth();
    }

    int getHeightInPixels(String arg) {
        FontMetrics metrics = comp2D.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(arg, null);
        return (int) bounds.getHeight();
    }

    protected void drawSampleRoad(Point cellCenter) {
        drawRoad(cellCenter, Directions.west);
        drawRoad(cellCenter, Directions.none);
        drawRoad(cellCenter, Directions.northeast);
    }

    protected void drawSampleRailroad(Point cellCenter) {
        drawRailroad(cellCenter, Directions.southwest);
        drawRailroad(cellCenter, Directions.none);
        drawRailroad(cellCenter, Directions.east);
    }

    protected void drawRoad(Point cellCenter, Directions dir) {
        Color color = Features.road.getColor();
        BasicStroke stroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
        drawRoad(cellCenter, dir, color, stroke);
    }

    protected void drawRailroad(Point cellCenter, Directions dir) {
        Color color = Features.railroad.getColor();
        BasicStroke stroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
        drawRoad(cellCenter, dir, color, stroke);
    }

    protected void drawRoad(Point cellCenter, Directions dir, Color color, BasicStroke stroke) {
        Point end = getMidEdge(cellCenter, dir);
        drawRoad(cellCenter, end, color, stroke);
    }

    protected void drawRoad(Point start, Point end, Color color, BasicStroke stroke) {
        if (start.equals(end)) {
            BasicStroke circle = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_ROUND, stroke.getLineJoin());
            drawLine(start.x, start.y, end.x, end.y, color, circle);
        } else {
            drawLine(start.x, start.y, end.x, end.y, color, stroke);
        }
    }

    protected Point getMidEdge(Point cellCenter, Directions dir) {
        int x1 = cellCenter.x - hexWidthInPixels / 2;
        int x2 = cellCenter.x - hexWidthInPixels / 4;
        int x3 = cellCenter.x;
        int x4 = cellCenter.x + (hexWidthInPixels - 1) / 4;
        int x5 = cellCenter.x + (hexWidthInPixels - 1) / 2;
        int y1 = cellCenter.y - (3 * hexSideInPixels)/4;
        int y2 = cellCenter.y;
        int y3 = cellCenter.y + (3 * hexSideInPixels)/4;
        switch(dir) {
            case northwest:  return new Point(x2, y1);
            case northeast:  return new Point(x4, y1);
            case west:       return new Point(x1, y2);
            case east:       return new Point(x5, y2);
            case southwest:  return new Point(x2, y3);
            case southeast:  return new Point(x4, y3);
            case none: default: return cellCenter;
        }
    }

    protected void labelFeatures(Point cellCenter, TerrainTypes terrain, Vector<Boolean> features) {
        String label = Features.toString(terrain, features);
        int featuresWidth = getWidthInPixels(label);
        int x = cellCenter.x - featuresWidth / 2;
        int y = cellCenter.y + hexSideInPixels / 2 - getHeightInPixels(label) / 3;
        textDisplayer.beginUsing(comp2D, x, y, 12);
        textDisplayer.typeLine(label);
        textDisplayer.finishUsing();
    }
}