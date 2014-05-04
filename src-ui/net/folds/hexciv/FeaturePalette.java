package net.folds.hexciv;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class FeaturePalette extends Panel {

    public FeaturePalette(CellDescriber parent) {
        super(parent);
        FeaturePaletteMouseListener m = new FeaturePaletteMouseListener();
        addMouseListener(m);
        addMouseMotionListener(m);
        setPreferredSize(new Dimension(110, 372));
    }

    public void paintComponent(Graphics comp) {
        super.paintComponent(comp);
        Graphics2D comp2D = (Graphics2D) comp;
        int h = getHeight() - 5; // available height for information
        int w = getWidth()  - 5; // available width  for information
        comp2D.drawRect(2, 2, (int) w, (int) h);

        int hexSideInPixels = 16;
        Rectangle margins = new Rectangle(10, 6, w - 8, h - 4);
        FeaturePaletteDrafter drafter = new FeaturePaletteDrafter(comp2D, hexSideInPixels, textDisplayer, margins);
        int spanInPixels = (int) h - 9;
        TerrainTypes desiredTerrain = parent.getDesiredTerrain();
        BitSet desiredFeatures = parent.getDesiredFeatures();
        drafter.drawPalette(spanInPixels, desiredTerrain, desiredFeatures);
    }

    private class FeaturePaletteMouseListener implements MouseListener, MouseMotionListener {
        public void mouseEntered(MouseEvent e) {updateLocale(e);}
        public void mouseMoved(MouseEvent   e) {updateLocale(e);}
        public void mouseDragged(MouseEvent e) {updateLocale(e);}
        public void mousePressed(MouseEvent e) {updateLocale(e);}
        public void mouseReleased(MouseEvent e) {
            updateChoice(e);
            updateLocale(e);
        }
        // useless, because it only occurs if no motion during click.
        public void mouseClicked(MouseEvent e) {
            updateLocale(e);
        }
        public void mouseExited(MouseEvent e) {updateLocale(e);}

        private void updateChoice(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int hexSideInPixels = 16;
            int topMargin = 6;
            int h = getHeight() - 5; // available height for information
            int spanInPixels = (int) h - 9;
            FeaturePaletteLoupe loupe = new FeaturePaletteLoupe(hexSideInPixels, topMargin, spanInPixels);
            Features feature = loupe.getFeature(y);
            parent.toggleFeature(feature);
        }

        private void updateLocale(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int hexSideInPixels = 16;
            int topMargin = 6;
            int h = getHeight() - 5; // available height for information
            int spanInPixels = (int) h - 9;
            FeaturePaletteLoupe loupe = new FeaturePaletteLoupe(hexSideInPixels, topMargin, spanInPixels);
            Features feature = loupe.getFeature(y);
            parent.updateLocale(feature, x, y);
        }
    }
}