package net.folds.hexciv;

/**
 * Created by jasper on May 06, 2014.
 */
public class ProductType {
    UnitType unitType;
    ImprovementType improvementType;
    boolean isUnitType;

    protected ProductType(UnitType unitType) {
        this.unitType = unitType;
        isUnitType = true;
    }

    protected ProductType(ImprovementType improvementType) {
        this.improvementType = improvementType;
        isUnitType = false;
    }

    protected int getCapitalCost() {
        if (isUnitType) {
            return unitType.capitalCost;
        } else {
            return improvementType.capitalCost;
        }
    }
}
