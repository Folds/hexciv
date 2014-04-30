package net.folds.hexciv;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class City {
    Civilization civ;
    String name;
    int size;
    int precedence;
    int storedFood;
    int storedProduction;
    int location;
    int numTaxMen;
    int numScientists;
    int numEntertainers;

    protected BitSet improvementFlags;
    Vector<Unit> units;
    Vector<Integer> farms;
    Vector<Integer> tradePartnerLocations;

    protected City(Civilization civ, int cellId, String name) {
        this.civ = civ;
        // To-do:  initialize improvementIndexes to all false,
        //         for each possible city improvement.
        this.name = name;
        size = 1;
        storedFood = 0;
        storedProduction = 0;
        location = cellId;
        numTaxMen = 0;
        numScientists = 0;
        numEntertainers = 1;
        tradePartnerLocations = new Vector<Integer>(3);
        for (int i = 0; i < 3; i++) {
            tradePartnerLocations.add(-1);
        }

    }

    protected void add(Unit unit) {
        if (units == null) {
            units = new Vector<Unit>(1);
        }
        if (unit.unitType == null) {
            return;
        }
        units.add(unit);
    }

    protected boolean hasGranary() {
        return false;
    }

    protected int countSettlers() {
        int result = 0;
        for (Unit unit : units) {
            if (unit.unitType.isSettler) {
                result = result + 1;
            }
        }
        return result;
    }

    protected int countUnits() {
        return units.size();
    }

    protected String describeUnit(int id) {
        String result = units.get(id).unitType.name;
        if (units.get(id).isVeteran) {
            result = result + " (V)";
        }
        return result;
    }

    protected Unit getUnit(int id) {
        return units.get(id);
    }

    protected Vector<Integer> getUnitLocations() {
        int numUnits = countUnits();
        Vector<Integer> result = new Vector<>(numUnits);
        for (Unit unit : units) {
            result.add(unit.getLocation());
        }
        return result;
    }

    protected static City proposeNone(Civilization civ) {
        City none = new City(civ, -1, "None");
        none.size = 0;
        none.precedence = 0;
        none.storedFood = 0;
        none.storedProduction = 0;
        return none;
    }

    protected boolean isNone() {
        if (location < 0) {
            return false;
        }
        return true;
    }

    protected int getUpkeepCost() {
        int result = 0;
        for (int i = 0; i < improvementFlags.size(); i++) {
            if (improvementFlags.get(i)) {
                ImprovementType improvementType = ImprovementType.get(i);
                if (improvementType != null) {
                    result = result + improvementType.upkeepCost;
                }
            }
        }
        return result;
    }
}
