package net.folds.hexciv;

import java.awt.*;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class MousePanel extends Panel {
    private int mouseXinPixels;
    private int mouseYinPixels;
    private int cellId;
    private Features feature;

    public MousePanel(CellDescriber parent) {
        super(parent);
        setPreferredSize(new Dimension(150, 188));
    }

    protected void setX(int x) {
        if (mouseXinPixels != x) {
            mouseXinPixels = x;
            this.repaint();
        }
    }

    protected void setY(int y) {
        if (mouseYinPixels != y) {
            mouseYinPixels = y;
            this.repaint();
        }
    }

    protected void setCellId(int cellId) {
        if ((cellId != this.cellId) || (feature != null)) {
            this.cellId = cellId;
            feature = null;
            this.repaint();
        }
    }

    protected void setFeature(Features feature) {
        if ((this.feature == null) || (!this.feature.equals(feature))) {
            this.feature = feature;
            this.repaint();
        }
    }

    public void paintComponent(Graphics comp) {
        super.paintComponent(comp);
        Graphics2D comp2D = (Graphics2D) comp;
        long h = getHeight() - 5; // available height for information
        long w = getWidth()  - 5; // available width  for information
        comp2D.drawRect(2, 2, (int) w, (int) h);
        if (feature == null) {
            CellSnapshot cell = parent.getCellSnapshot(cellId);

            beginUsingTextArea(comp2D, 5, 18);
            typeLine("Mouse X = " + mouseXinPixels);
            typeLine("Mouse Y = " + mouseYinPixels);
            typeLine("");
            typeLine("Adjacent Cells:");
            typeLine("    NW:" + getNwCellId() + " NE:" + getNeCellId());
            typeLine("W:" + getWCellId() + "            E:" + getECellId());
            typeLine("    SW:" + getSwCellId() + " SE:" + getSeCellId());
            finishUsingTextArea();
        } else {
            beginUsingTextArea(comp2D, 5, 18);
            typeLine("Mouse X = " + mouseXinPixels);
            typeLine("Mouse Y = " + mouseYinPixels);
            typeLine("");
            typeLine(feature.toString());
            finishUsingTextArea();
        }
    }

    protected int getNwCellId() {
        return parent.getAdjacentCellId(cellId, Directions.northwest);
    }
    protected int getNeCellId() {
        return parent.getAdjacentCellId(cellId, Directions.northeast);
    }
    protected int getWCellId() {
        return parent.getAdjacentCellId(cellId, Directions.west);
    }
    protected int getECellId() {
        return parent.getAdjacentCellId(cellId, Directions.east);
    }
    protected int getSwCellId() {
        return parent.getAdjacentCellId(cellId, Directions.southwest);
    }
    protected int getSeCellId() {
        return parent.getAdjacentCellId(cellId, Directions.southeast);
    }
}