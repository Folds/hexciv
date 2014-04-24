package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class Unit {
    int cellId;
    UnitType unitType;
    boolean isVeteran;
    int remainingRange;
    boolean isFortified;
    boolean isSentried;

    protected Unit(UnitType unitType, int cellId) {
        this.unitType = unitType;
        this.cellId = cellId;
    }

    protected int getLocation() {
        return cellId;
    }
}
