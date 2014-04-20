package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Apr 18, 2014.
 */
public interface CellDescriber {
    CellSnapshot getCellSnapshot(int cellId);
    int getAdjacentCellId(int cellId, Directions dir);
    TerrainTypes getDesiredTerrain();
    Vector<Boolean> getDesiredFeatures();
    void chooseTerrain(TerrainTypes terrain);
    void toggleFeature(Features feature);
    void updateLocale(Features feature, int x, int y);
}
