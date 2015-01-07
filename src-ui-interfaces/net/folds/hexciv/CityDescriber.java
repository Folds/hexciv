package net.folds.hexciv;

import java.util.BitSet;

/**
 * Created by jasper on Apr 18, 2014.
 */
public interface CityDescriber extends CellDescriber {
    CitySnapshot getCitySnapshot(int cellId);
}
