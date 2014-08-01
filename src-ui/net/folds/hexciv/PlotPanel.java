package net.folds.hexciv;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Created by jasper on Jul 28, 2014.
 * Adapted from Marco13's post on Feb 19, 2014 at:
 * http://stackoverflow.com/questions/21868284/how-to-create-a-jpanel-for-graphing
 */
public class PlotPanel extends JPanel {

    private int minX = 0;
    private int maxX = 550;
    // private int minY = 0;
    // private int maxY = 100;

    private Vector<StatColumn> backgroundColumns = new Vector<StatColumn>(4);
    private Vector<Color>      backgroundColors  = new Vector<Color>(4);
    private Vector<StatColumn> foregroundColumns = new Vector<StatColumn>(5);
    private Vector<Color>      foregroundColors  = new Vector<Color>(5);

    public PlotPanel() {
        super();
    }

    protected void addToBackground(StatColumn column, Color color) {
        backgroundColumns.add(column);
        backgroundColors.add(color);
    }

    protected void addToForeground(StatColumn column, Color color) {
        foregroundColumns.add(column);
        foregroundColors.add(color);
    }

    @Override
    protected void paintComponent(Graphics gr)
    {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D)gr;
        g.setColor(Color.WHITE);
        g.fillRect(0,0,getWidth(),getHeight());

        paintBackground(g);
        paintForeground(g);
        paintAxes(g);
    }

    private void paintBackground(Graphics2D g) {
        int numColumns = backgroundColumns.size();
        int numColors  = backgroundColors.size();
        int numPairs = Math.min(numColumns, numColors);
        for (int i = numPairs - 1; i >= 0; i--) {
            paintBackground(g, i);
        }
    }

    private void paintForeground(Graphics2D g) {
        int numColumns = foregroundColumns.size();
        int numColors  = foregroundColors.size();
        int numPairs = Math.min(numColumns, numColors);
        for (int i = 0; i < numPairs; i++) {
            paintForeground(g, i);
        }
    }

    // Paints some coordinate axes into the given Graphics
    private void paintAxes(Graphics2D g)
    {
        int x0 = toScreenX(0);
        int y0 = toScreenY(0, 0, 1);
        Color oldColor = g.getColor();
        g.setColor(Color.BLACK);
        g.drawLine(0,y0,getWidth(),y0);
        g.drawLine(x0,0,x0,getHeight());
        g.setColor(oldColor);
    }

    // Paints the function into the given Graphics
    private void paintBackground(Graphics2D g, int id)
    {
        Color oldColor = g.getColor();
        Color color = backgroundColors.get(id);
        g.setColor(color);

        StatColumn statColumn = backgroundColumns.get(id);
        int minY = backgroundColumns.get(id).getMinRange();
        int maxY = backgroundColumns.get(id).getMaxRange();

        int minFunctionX = statColumn.getStartTurn();
        int maxFunctionX = statColumn.getCurrentTurn();
        int leftScreenX  = toScreenX(minFunctionX);
        int rightScreenX = toScreenX(maxFunctionX);
        int width = getWidth();
        int maxScreenX = Math.min(width, rightScreenX);

        int previousScreenX = Math.max(0, leftScreenX);
        int secondScreenX = previousScreenX + 1;
        int previousFunctionX = toFunctionX(previousScreenX);
        int previousFunctionY = stackBackgrounds(id, previousFunctionX);
        int previousScreenY = toScreenY(previousFunctionY, minY, maxY);

        Polygon polygon = new Polygon();
        polygon.addPoint(maxScreenX, toScreenY(minY, minY, maxY));
        polygon.addPoint(previousScreenX, toScreenY(minY, minY, maxY));
        polygon.addPoint(previousScreenX, previousScreenY);
        for (int screenX = secondScreenX; screenX <= maxScreenX; screenX++)
        {
            int functionX = toFunctionX(screenX);
            int functionY = stackBackgrounds(id, functionX);
            int screenY = toScreenY(functionY, minY, maxY);

            if ((screenY != previousScreenY) || (screenX >= maxScreenX)) {
                if (screenX > previousScreenX + 1) {
                    polygon.addPoint(screenX - 1, previousScreenY);
                }
                polygon.addPoint(screenX, screenY);
                previousScreenX = screenX;
                previousScreenY = screenY;
            }
        }
        g.fillPolygon(polygon);
        g.setColor(oldColor);
    }

    private int stackBackgrounds(int id, int x) {
        int result = 0;
        for (int i = 0; i <= id; i++) {
            result = result + backgroundColumns.get(id).lookUp(x);
        }
        return result;
    }

    // Paints the function into the given Graphics
    private void paintForeground(Graphics2D g, int id)
    {
        Color oldColor = g.getColor();
        Color color = foregroundColors.get(id);
        g.setColor(color);

        StatColumn statColumn = foregroundColumns.get(id);
        int minY = foregroundColumns.get(id).getMinRange();
        int maxY = foregroundColumns.get(id).getMaxRange();
        int minFunctionX = statColumn.getStartTurn();
        int maxFunctionX = statColumn.getCurrentTurn();
        int leftScreenX  = toScreenX(minFunctionX);
        int rightScreenX = toScreenX(maxFunctionX);
        int width = getWidth();
        int maxScreenX = Math.min(width, rightScreenX);

        int previousScreenX = Math.max(0, leftScreenX);
        int secondScreenX = previousScreenX + 1;
        int previousFunctionX = toFunctionX(previousScreenX);
        int previousFunctionY = statColumn.lookUp(previousFunctionX);
        int previousScreenY = toScreenY(previousFunctionY, minY, maxY);

        for (int screenX = secondScreenX; screenX <= maxScreenX; screenX++)
        {
            int functionX = toFunctionX(screenX);
            int functionY = statColumn.lookUp(functionX);
            int screenY = toScreenY(functionY, minY, maxY);

            if ((screenY != previousScreenY) || (screenX >= maxScreenX)) {
                if (screenX > previousScreenX + 1) {
                    g.drawLine(previousScreenX, previousScreenY, screenX - 1, previousScreenY);
                }
                g.drawLine(screenX - 1, previousScreenY, screenX, screenY);
                previousScreenX = screenX;
                previousScreenY = screenY;
            }
        }
        g.setColor(oldColor);
    }

    // Converts an x-coordinate on this panel into an x-coordinate
    private int toFunctionX(int x)
    {
        double relativeX = (double)x/getWidth();
        return (int) (minX + relativeX * (maxX - minX));
    }

    // Converts an x-coordinate of the function into an x-value of this panel
    private int toScreenX(int x)
    {
        double relativeX = ((double)x-minX)/(maxX-minX);
        int screenX = (int)(getWidth() * relativeX);
        return screenX;
    }

    // Converts an y-coordinate of the function into an y-value of this panel
    private int toScreenY(int y, int minY, int maxY)
    {
        double relativeY = ((double)y-minY)/(maxY-minY);
        int screenY = getHeight() - 1 - (int)(getHeight() * relativeY);
        return screenY;
    }

}
