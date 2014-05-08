package net.folds.hexciv;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.BitSet;

/**
 * Created by Jasper on Sep 24, 2011.
 */
public class MapPanel extends JPanel {
    private MovableMap parent;
    private WorldMap map;
    private int recentHexWidthInPixels;
    private int recentHexSideInPixels;
    private BitSet seenCells;

    public MapPanel(MovableMap parent) {
        super();
        this.parent = parent;
        this.map = parent.getMap();
        seenCells = new BitSet(map.countCells());
        seeAll();
        MapMouseListener m = new MapMouseListener();
        addMouseListener(m);
        addMouseMotionListener(m);
        setPreferredSize(new Dimension(440, 188));
    }

    public MapPanel(WorldMap map) {
        super();
        this.map = map;
        seenCells = new BitSet(map.countCells());
        seeAll();
        setPreferredSize(new Dimension(440, 188));
    }

    protected void seeAll() {
        seenCells.clear();
        seenCells.set(0, map.countCells());
    }

    protected void setMap(WorldMap map) {
        this.map = map;
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

    int getMapHeightInHexSides() {
        return map.mapHeightInHexSides();
    }

    int getMapWidthInHexesAcrossFlats() {
        return map.mapWidthInHexesAcrossFlats();
    }

    private class MapMouseListener implements MouseListener, MouseMotionListener {
        public void mouseEntered(MouseEvent e)  {updateLocale(e);}
        public void mouseMoved(MouseEvent e)    {updateLocale(e);}
        public void mouseDragged(MouseEvent e)  {updateLocale(e);}
        public void mousePressed(MouseEvent e)  {updateLocale(e);}
        public void mouseReleased(MouseEvent e) {
            updateLocale(e);
            int cellId = getCellId(e);
            parent.recenterCanvas(cellId);
        }

        // useless, because it only occurs if no motion during click.
        public void mouseClicked(MouseEvent e)  {updateLocale(e);}
        public void mouseExited(MouseEvent e)   {updateLocale(e);}

        private void updateLocale(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cellId = getCellId(e);
            parent.updateLocale(cellId, x, y);
        }

        private int getCellId(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int leftMarginInPixels = 2 + recentHexWidthInPixels / 4 + 1;
            int topMarginInPixels = 2;
            Rectangle margins = new Rectangle(leftMarginInPixels, topMarginInPixels,
                                              getWidth() - leftMarginInPixels - 2,
                                              getHeight() - topMarginInPixels - 2);
            MapLoupe loupe = new MapLoupe(map, recentHexWidthInPixels, recentHexSideInPixels, margins);
            return loupe.getCellId(x, y);
        }
    }

    protected void repaint(BitSet updatedCells) {
        for (int i = 0; i < map.countCells(); i++) {
            if (updatedCells.get(i)) {
                repaint(i);
            }
        }
    }

    protected void repaint(int cellId) {
        if (seenCells.get(cellId)) {
            int leftMarginInPixels = 2 + recentHexWidthInPixels / 4 + 1;
            int topMarginInPixels = 2;
            Rectangle margins = new Rectangle(leftMarginInPixels, topMarginInPixels,
                    getWidth() - leftMarginInPixels - 2,
                    getHeight() - topMarginInPixels - 2);
            MapLoupe loupe = new MapLoupe(map, recentHexWidthInPixels, recentHexSideInPixels, margins);
            Rectangle rect = loupe.getAffectedArea(cellId);
            repaint(rect.x, rect.y, rect.width, rect.height);
        }
    }

    public void paintComponent(Graphics comp) {
        super.paintComponent(comp);
        Graphics2D comp2D = (Graphics2D) comp;
        int h = getHeight() - 5; // available height for getMap
        int w = getWidth()  - 5; // available width  for getMap
        comp2D.drawRect(2, 2, mapWidthInPixels(w, h), mapHeightInPixels(w, h));
        recentHexWidthInPixels = (int) hexWidthInPixels(w, h);
        recentHexSideInPixels = hexSideInPixels(w, h);
        int leftMarginInPixels =  2 + recentHexWidthInPixels / 4 + 1;
        int topMarginInPixels = 2;
        Rectangle margins = new Rectangle(leftMarginInPixels, topMarginInPixels, w, h);
        MapDrafter drafter = new MapDrafter(map, comp2D, recentHexSideInPixels, margins);
        drafter.drawMap(seenCells);
    }

    public int hexSideInPixels(int availableWidthForMapInPixels,
                               int availableHeightForMapInPixels) {
        int h = availableHeightForMapInPixels;
        int w = availableWidthForMapInPixels;
        int resultBasedOnHeight = (int) Drafter.roundToNearestEvenNumber(h / getMapHeightInHexSides());
        if (resultBasedOnHeight * getMapHeightInHexSides() > h) {
            resultBasedOnHeight = resultBasedOnHeight - 2;
        }
        if (resultBasedOnHeight < 2) {
            resultBasedOnHeight = 2;
        }

        int estimatedMaxWidth = (int) Drafter.roundToNearestEvenNumber(w / getMapWidthInHexesAcrossFlats());
        if (estimatedMaxWidth * getMapWidthInHexesAcrossFlats() > w) {
            estimatedMaxWidth = estimatedMaxWidth - 2;
        }
        if (estimatedMaxWidth < 4) {
            estimatedMaxWidth = 4;
        }

        int resultBasedOnWidth = (int) Drafter.roundToNearestEvenNumber(estimatedMaxWidth / Math.sqrt(3.0));
        int correspondingWidth = (int) Drafter.roundToNearestEvenNumber(resultBasedOnWidth * Math.sqrt(3.0));
        if (correspondingWidth * getMapWidthInHexesAcrossFlats() > w) {
            resultBasedOnWidth = resultBasedOnWidth - 2;
        }
        if (resultBasedOnWidth < 2) {
            resultBasedOnWidth = 2;
        }

        if (resultBasedOnWidth < resultBasedOnHeight) {
            return resultBasedOnWidth;
        }
        return resultBasedOnHeight;
    }

    public int hexWidthInPixels(int availableWidthForMapInPixels,
                                int availableHeightForMapInPixels) {
        int h = availableHeightForMapInPixels;
        int w = availableWidthForMapInPixels;
        return (int) Drafter.roundToNearestEvenNumber(Math.sqrt(3.0) * hexSideInPixels(w, h));
    }

    public int mapWidthInPixels(int availableWidthForMapInPixels,
                                int availableHeightForMapInPixels) {
        int h = availableHeightForMapInPixels;
        int w = availableWidthForMapInPixels;
        return getMapWidthInHexesAcrossFlats() * hexWidthInPixels(w, h);
    }

    public int mapHeightInPixels(int availableWidthForMapInPixels,
                                 int availableHeightForMapInPixels) {
        int h = availableHeightForMapInPixels;
        int w = availableWidthForMapInPixels;
        return getMapHeightInHexSides() * hexSideInPixels(w, h);
    }
}