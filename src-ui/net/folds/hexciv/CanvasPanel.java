package net.folds.hexciv;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.BitSet;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class CanvasPanel extends Panel {
    private MovableMap movableMap;
    private WorldMap map;
    private int centerCellId = 0;
    private int hexWidthInPixels;
    private int hexSideInPixels;
    private CanvasLoupe loupe;
    private CanvasDrafter drafter;
    private BitSet seenCells;
    private ClaimReferee claimReferee;

    public CanvasPanel(MovableMap movableMap, CellDescriber cellDescriber, WorldMap map, int hexSideInPixels) {
        super(cellDescriber);
        this.movableMap = movableMap;
        setPreferredSize(new Dimension(500, 400));
        this.map = map;
        seenCells = new BitSet(map.countCells());
        this.hexSideInPixels = hexSideInPixels;
        this.hexWidthInPixels = (int) Drafter.roundToNearestEvenNumber(Math.sqrt(3.0) * hexSideInPixels);
        int leftMarginInPixels = 0;
        int topMarginInPixels = 0;
        Rectangle margins = new Rectangle(leftMarginInPixels, topMarginInPixels,
                                          getWidth() - leftMarginInPixels,
                                          getHeight() - topMarginInPixels);
        claimReferee = null;
        loupe = new CanvasLoupe(map, hexWidthInPixels, hexSideInPixels, margins);
        MapMouseListener m = new MapMouseListener();
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    protected void setCenterCell(int centerCellId) {
        if (this.centerCellId != centerCellId) {
            this.centerCellId = centerCellId;
            this.repaint();
        }
    }

    protected void setMap(WorldMap map) {
        this.map = map;
        loupe.setMap(map);
        if (drafter != null) {
            drafter.setMap(map);
        }
    }

    protected void setClaimReferee(ClaimReferee claimReferee) {
        this.claimReferee = claimReferee;
    }

    // http://www.leepoint.net/notes-java/GUI-lowlevel/mouse/20mousebuttons.html
    private class MapMouseListener implements MouseListener, MouseMotionListener {
        public void mouseEntered(MouseEvent e) {updateLocale(e);}
        public void mouseMoved(MouseEvent e)   {updateLocale(e);}
        public void mouseDragged(MouseEvent e) {updateLocale(e);}
        public void mousePressed(MouseEvent e) {updateLocale(e);}
        public void mouseReleased(MouseEvent e) {
            if (e.isControlDown() || e.isAltDown()) {
                // eyedropper mode, similar to Alt-Click in PhotoShop and Corel Paint.
                updatePalettes(e);
            } else if (e.isMetaDown()) {
                // right-click
                updateCell(e);
            } else {
                recenterCanvas(e);
            }
            updateLocale(e);
        }
        // useless, because it only occurs if no motion during click.
        public void mouseClicked(MouseEvent e) {updateLocale(e);}
        public void mouseExited(MouseEvent e) {updateLocale(e);}

        private void recenterCanvas(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cellId = loupe.getCellId(x, y, getWidth()/2, getHeight()/2, centerCellId);
            movableMap.recenterCanvas(cellId);
        }

        private void updateCell(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cellId = loupe.getCellId(x, y, getWidth() /2, getHeight()/2, centerCellId);
            movableMap.updateCell(cellId);
        }

        private void updateLocale(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cellId = loupe.getCellId(x, y, getWidth() /2, getHeight()/2, centerCellId);
            CellSnapshot cellSnapshot = getCellSnapshot(cellId);
            movableMap.updateLocale(cellSnapshot, x, y);
        }

        private void updatePalettes(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cellId = loupe.getCellId(x, y, getWidth() /2, getHeight()/2, centerCellId);
            if ((cellId >= 0) && (cellId < loupe.map.countCells())) {
                movableMap.updatePalettes(cellId);
            }
        }
    }

    public void paintComponent(Graphics comp) {
        super.paintComponent(comp);
        Graphics2D comp2D = (Graphics2D) comp;
        int h = getHeight() - 5; // available height for information
        int w = getWidth()  - 5; // available width  for information
        // comp2D.drawRect(2, 2, (int) w, (int) h);
        if (!hasValidDrafter(map, comp2D, hexSideInPixels)) {
            int leftMarginInPixels = 0;
            int topMarginInPixels = 0;
            Rectangle margins = new Rectangle(leftMarginInPixels, topMarginInPixels, w, h);
            drafter = new CanvasDrafter(map, comp2D, hexSideInPixels, textDisplayer, margins, claimReferee);
            loupe.setMargins(margins);
        }
        drafter.drawMap(w, h, centerCellId, seenCells);

        // beginUsingTextArea(comp2D, 5, 18);
        // typeLine("Canvas");
        // typeLine("w/2= "+(w/2)+" pixels; hexWidth = "+ hexWidthInPixels + " pixels; " + drafter.insetMap.numColumns + " columns");
        // typeLine("h/2= "+(h/2)+" pixels; hexSide  = "+ hexSideInPixels  + " pixels; " + drafter.insetMap.numRows    + " rows");
        // finishUsingTextArea();
    }

    protected void repaint(BitSet updatedCells) {
        for (int i = 0; i < map.countCells(); i++) {
            if (updatedCells.get(i)) {
                repaint(i);
            }
        }
    }

    protected void repaint(int cellId) {
        if (!hasValidDrafter(map, hexSideInPixels)) {
            repaint();
            return;
        }
        Rectangle rect = loupe.getAffectedArea(cellId);
        repaint(rect.x, rect.y, rect.width, rect.height);
    }

    private boolean hasValidDrafter(WorldMap map, int hexSideInPixels) {
        if (drafter == null) {
            return false;
        }
        if ((drafter.map == map) && (drafter.hexSideInPixels == hexSideInPixels)) {
            return true;
        }
        return false;
    }

    private boolean hasValidDrafter(WorldMap map, Graphics2D comp2D, int hexSideInPixels) {
        if (drafter == null) {
            return false;
        }
        if (   (drafter.map == map)
            && (drafter.comp2D == comp2D)
            && (drafter.hexSideInPixels == hexSideInPixels)
                ) {
            return true;
        }
        return false;
    }

    protected CellSnapshot getCellSnapshot(int cellId) {
        CellSnapshot result = map.getCellSnapshot(cellId);

        int index = loupe.insetMap.cellIds.indexOf(cellId);

        result.row = loupe.insetMap.getRow(index);
        result.positionInRow = loupe.insetMap.getColumn(index);
        return result;
    }

    void hideAll() {
        seenCells.clear();
        repaint();
    }

    protected void seeCells(BitSet seenCells) {
        BitSet newCells = (BitSet) seenCells.clone();
        newCells.andNot(this.seenCells);
        this.seenCells.or(newCells);
        repaint(newCells);
    }

    protected void seeAll() {
        seenCells.clear();
        seenCells.set(0, map.countCells());
    }

}