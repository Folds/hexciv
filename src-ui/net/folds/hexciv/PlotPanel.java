package net.folds.hexciv;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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

    private Vector<StatColumn> backgroundColumns = new Vector<StatColumn>(5);
    private Vector<Color>      backgroundColors  = new Vector<Color>(5);
    private Vector<StatColumn> foregroundColumns = new Vector<StatColumn>(5);
    private Vector<Color>      foregroundColors  = new Vector<Color>(5);

    public PlotPanel() {
        super();
        PlotMouseListener m = new PlotMouseListener();
        addMouseListener(m);
        addMouseMotionListener(m);
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
            result = result + backgroundColumns.get(i).lookUp(x);
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

    private int getBackgroundColumnId(int screenX, int screenY) {
        int numColumns = backgroundColumns.size();
        int functionX = toFunctionX(screenX);
        for (int i = 0; i < numColumns; i++) {
            int functionY = stackBackgrounds(i, functionX);
            int minFunctionY = backgroundColumns.get(i).getMinRange();
            int maxFunctionY = backgroundColumns.get(i).getMaxRange();
            int potentialScreenY = toScreenY(functionY, minFunctionY, maxFunctionY);
            if (potentialScreenY <= screenY) {
                return i;
            }
        }
        return -1;
    }

    private int getForegroundColumnId(int screenX, int screenY) {
        int numColumns = foregroundColumns.size();
        int functionX = toFunctionX(screenX);
        int result = -1;
        int leastDifference = 2 * this.getHeight();
        for (int i = 0; i < numColumns; i++) {
            int functionY = foregroundColumns.get(i).lookUp(functionX);
            int minFunctionY = foregroundColumns.get(i).getMinRange();
            int maxFunctionY = foregroundColumns.get(i).getMaxRange();
            int potentialScreenY = toScreenY(functionY, minFunctionY, maxFunctionY);
            int difference = Math.abs(potentialScreenY - screenY);
            if (difference <= leastDifference) {
                result = i;
                leastDifference = difference;
            }
        }
        return result;
    }

    private class PlotMouseListener implements MouseListener, MouseMotionListener {
        public void mouseEntered(MouseEvent e)  {updateToolTip(e);}
        public void mouseMoved(MouseEvent e)    {updateToolTip(e);}
        public void mouseDragged(MouseEvent e)  {updateToolTip(e);}
        public void mousePressed(MouseEvent e)  {updateToolTip(e);}
        public void mouseReleased(MouseEvent e) {updateToolTip(e);}

        // useless, because it only occurs if no motion during click.
        public void mouseClicked(MouseEvent e)  {updateToolTip(e);}
        public void mouseExited(MouseEvent e)   {updateToolTip(e);}

        private void updateToolTip(MouseEvent e) {
            String str = describe(e.getX(), e.getY());
            setToolTipText(str);
        }
    }

    private String combineTips(String a, String b, String c) {
        if ((a == null) || (a.equals(""))) {
            return combineTips(b, c);
        }
        if ((b == null) || (b.equals(""))) {
            return combineTips(a, c);
        }
        if ((c == null) || (c.equals(""))) {
            return combineTips(a, b);
        }
        return "<HTML>" + a + "<BR>" + b + "<BR>" + c + "</HTML>";
    }

    private String combineTips(String a, String b) {
        if ((a == null) || (a.equals(""))) {
            return b;
        }
        if ((b == null) || (b.equals(""))) {
            return a;
        }
        return "<HTML>" + a + "<BR>" + b + "</HTML>";
    }

    private String describe(int screenX, int screenY) {
        int turn = toFunctionX(screenX);
        int backgroundColumnId = getBackgroundColumnId(screenX, screenY);
        String strBackground = describeBackground(backgroundColumnId, turn);
        int foregroundColumnId = getForegroundColumnId(screenX, screenY);
        String strForeground = describeForeground(foregroundColumnId, turn);
        String strTurn = describeFunctionX(turn);
        return combineTips(strTurn, strBackground, strForeground);
    }

    private String describe(Vector<StatColumn> columns, int columnId, int functionX) {
        if ((columns == null) || (columnId < 0)) {
            return "";
        }
        StatColumn column = columns.get(columnId);
        if ((column.name == null) || (column.name.equals(""))) {
            return "";
        }
        int columnFunctionY = column.lookUp(functionX);
        if (   (columnFunctionY >= 0)
            && (column.valueNames != null)
            && (columnFunctionY < column.valueNames.size())
            && (column.valueNames.get(columnFunctionY) != null)
            && (!(column.valueNames.get(columnFunctionY).equals("")))
           ) {
            if (column.name.endsWith("=")) {
                return column.name + column.valueNames.get(columnFunctionY);
            } else {
                return column.name + "=" + column.valueNames.get(columnFunctionY);
            }
        }
        if (isPrefix(column.name)) {
            return column.name + columnFunctionY;
        }
        if (needsSpace(column.name)) {
            return columnFunctionY + " " + column.name;
        }
        return columnFunctionY + column.name;

    }

    private String describeBackground(int columnId, int functionX) {
        return describe(backgroundColumns, columnId, functionX);
    }

    private String describeForeground(int columnId, int functionX) {
        return describe(foregroundColumns, columnId, functionX);
    }

    private String describeFunctionX(int functionX) {
        int year = getYear(functionX);
        String strYear = formatYear(year);
        return strYear + " (turn " + functionX + "/" + maxX + ")";
    }

    // To-do:  recombine with GameScreen.formatYear(int year)
    protected String formatYear(int year) {
        if (year == 0) { return "1 A.D."; }
        if (year > 0)  { return year + " A.D."; }
        return -year + " B.C.";
    }

    // To-do:  recombine with GameState.getYear(int turn)
    protected int getYear(int turn) {
        if (turn <= 0)   { return -4004; }
        if (turn == 200) { return 1; }
        if (turn <= 250) { return 20 * turn - 4000; }
        if (turn <= 300) { return 10 * turn - 1500; }
        if (turn <= 350) { return  5 * turn; }
        if (turn <= 400) { return  2 * turn + 1050; }
        return turn + 1450;
    }

    private boolean isPrefix(String arg) {
        if (arg == null) {
            return false;
        }
        if (arg.length() < 1) {
            return false;
        }
        if (arg.endsWith("=")) {
            return true;
        }
        return false;
    }

    private boolean needsSpace(String suffix) {
        if (suffix == null) {
            return false;
        }
        if (suffix.length() < 1) {
            return false;
        }
        if (suffix.startsWith("%")) {
            return false;
        }
        if (suffix.startsWith("0")) {
            return false;
        }
        return true;
    }

}
