package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Apr 18, 2014.
 */
interface PaintableScreen {
    void repaintOopses();
    void repaintMaps();
    void repaintMaps(Vector<Integer> cellIds);
    void repaintMaps(int cellId);
    void repaintPalettes();
}
