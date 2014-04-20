package net.folds.hexciv;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class TerrainPalette extends Panel {
    public TerrainPalette(CellDescriber parent) {
        super(parent);
        this.parent = parent;
        TerrainPaletteMouseListener m = new TerrainPaletteMouseListener();
        addMouseListener(m);
        addMouseMotionListener(m);
        setPreferredSize(new Dimension(580, 86));
    }

    public void paintComponent(Graphics comp) {
        super.paintComponent(comp);
        Graphics2D comp2D = (Graphics2D) comp;
        int h = getHeight() - 5; // available height for information
        int w = getWidth()  - 5; // available width  for information
        comp2D.drawRect(2, 2, (int) w, (int) h);
        int hexSideInPixels = 16;
        Rectangle margins = new Rectangle(10, 6, w, h);
        TerrainPaletteDrafter drafter = new TerrainPaletteDrafter(comp2D, hexSideInPixels, textDisplayer, margins);
        int spanInPixels = (int) w - 9;
        TerrainTypes desiredTerrain = parent.getDesiredTerrain();
        Vector<Boolean> features = parent.getDesiredFeatures();
        drafter.drawPalette(spanInPixels, (int) h, desiredTerrain, features);
    }

    private class TerrainPaletteMouseListener implements MouseListener, MouseMotionListener {
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseMoved(MouseEvent e) {
        }
        public void mouseDragged(MouseEvent e) {
        }
        public void mousePressed(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {
            updateChoice(e);
        }
        // useless, because it only occurs if no motion during click.
        public void mouseClicked(MouseEvent e) {
            updateChoice(e);
        }
        public void mouseExited(MouseEvent e) {
        }

        private void updateChoice(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int hexSideInPixels = 16;
            TerrainPaletteLoupe loupe = new TerrainPaletteLoupe(hexSideInPixels, 2, getWidth());
            TerrainTypes terrain = loupe.getTerrain(x);
            parent.chooseTerrain(terrain);
        }
    }

}