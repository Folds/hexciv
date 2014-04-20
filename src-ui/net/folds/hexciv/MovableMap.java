package net.folds.hexciv;

/**
 * Created by jasper on Apr 18, 2014.
 */
public interface MovableMap {
    WorldMap getMap();
    void recenterCanvas(int cellId);
    void updateLocale(int cellId, int x, int y);
    void updatePalettes(int cellId);
    void updateLocale(CellSnapshot cellSnapshot, int x, int y);
    void updateCell(int cellId);
}
