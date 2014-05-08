package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class UnitType {
    String name;
    int attackStrength;
    int defenseStrength;
    int mobility;
    int technologyIndex;
    int capitalCost;   // in shields
    int logisticsCost; // in shields per turn
    int feedingCost;   // in food per turn
    int range;
    boolean hasLimitedRange;
    boolean isAerial;
    boolean isDiplomat;
    boolean isNaval;
    boolean isSettler;
    boolean isTerrestrial;

    protected UnitType(String name) {
        this.name = name;
        this.attackStrength = 1;
        this.defenseStrength = 1;
        this.mobility = 1;
        this.technologyIndex = -1; // available immediately
        this.capitalCost = 600;
        this.logisticsCost = 1;
        this.feedingCost = 0;
        this.range = 0;
        this.hasLimitedRange = false;
        this.isAerial = false;
        this.isDiplomat = false;
        this.isNaval = false;
        this.isSettler = false;
        this.isTerrestrial = true;
    }

    protected static Vector<UnitType> getChoices() {
        Vector<UnitType> result = new Vector<>(2);
        UnitType settler = proposeSettler();
        UnitType militia = proposeMilitia();
        result.add(settler);
        result.add(militia);
        return result;
    }

    protected static UnitType proposeMilitia() {
        UnitType militia = new UnitType("Militia");
        militia.capitalCost = 10;
        return militia;
    }

    protected static UnitType proposeSettler() {
        UnitType settler = new UnitType("Settler");
        settler.capitalCost = 40;
        settler.attackStrength = 0;
        settler.feedingCost = 1;
        settler.isSettler = true;
        return settler;
    }

    protected static UnitType lookupUnitType(Vector<UnitType> unitTypes, String name) {
        for (UnitType unitType : unitTypes) {
            if (unitType.name.equalsIgnoreCase(name)) {
                return unitType;
            }
        }
        return null;
    }
}
