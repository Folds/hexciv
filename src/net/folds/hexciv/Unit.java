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
    int wipTurns;
    boolean isMining;
    boolean isIrrigating;
    boolean isBuildingRoad;

    protected Unit(UnitType unitType, int cellId) {
        this.unitType = unitType;
        this.cellId = cellId;
        wipTurns = 0;
        isFortified = false;
        isSentried = false;
        isMining = false;
        isIrrigating = false;
        isBuildingRoad = false;
    }

    protected int getLocation() {
        return cellId;
    }
}
