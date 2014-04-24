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
    private BitSet improvementIndexes;
    Vector<Unit> units;
    Vector<Integer> terrain;
    Vector<Integer> tradePartnerLocations;

    protected City(Civilization civ) {
        this.civ = civ;
        // To-do:  initialize improvementIndexes to all false,
        //         for each possible city improvement.
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
        City none = new City(civ);
        none.name = "None";
        none.size = 0;
        none.precedence = 0;
        none.storedFood = 0;
        none.storedProduction = 0;
        none.location = -1;
        return none;
    }
}
