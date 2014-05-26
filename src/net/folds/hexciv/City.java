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
    boolean isDestroyed;

    ImprovementKey improvements;
    Vector<Unit> units;
    Vector<Integer> farms;
    Vector<Integer> tradePartnerLocations;
    ProductType wip;

    protected City(Civilization civ, int cellId, String name) {
        this.civ = civ;
        // To-do:  initialize improvementIndexes to all false,
        //         for each possible city improvement.
        this.name = name;
        isDestroyed = false;
        storedFood = 0;
        storedProduction = 0;
        location = cellId;
        if (cellId < 0) {
            size = 0;
            numEntertainers = 0;
        } else {
            size = 1;
            numEntertainers = 1;
        }
        numTaxMen = 0;
        numScientists = 0;
        tradePartnerLocations = new Vector<Integer>(3);
        for (int i = 0; i < 3; i++) {
            tradePartnerLocations.add(-1);
        }
        improvements = new ImprovementKey(civ.improvements);
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

    protected int countSettlers() {
        int result = 0;
        if (units == null) {
            return result;
        }
        for (Unit unit : units) {
            if (unit.unitType.isSettler) {
                result = result + 1;
            }
        }
        return result;
    }

    protected int countUnits() {
        if (units == null) {
            return 0;
        }
        return units.size();
    }

    protected int countWonders() {
        return improvements.countWonders();
    }

    protected String describeUnit(int id) {
        String result = units.get(id).unitType.name;
        if (units.get(id).isVeteran) {
            result = result + " (V)";
        }
        return result;
    }

    protected int getLowestValueImprovement() {
        return improvements.getLowestValueImprovement();
    }

    protected Unit getUnit(int id) {
        return units.get(id);
    }

    protected Vector<Integer> getUnitLocations() {
        int numUnits = countUnits();
        Vector<Integer> result = new Vector<>(numUnits);
        if (numUnits == 0) {
            return result;
        }
        for (Unit unit : units) {
            result.add(unit.getLocation());
        }
        return result;
    }

    protected int getUpkeepCost() {
        return improvements.getUpkeepCost();
    }

    protected boolean hasGranary() {
        return false;
    }

    protected boolean isNone() {
        if (location < 0) {
            return true;
        }
        return false;
    }

    protected void produce(GameListener listener, ClaimReferee referee) {
        storedProduction = 0;
        if (wip.isUnitType) {
            Unit unit = new Unit(wip.unitType, location);
            add(unit);
        } else {
            if (wip.improvementType.id >= 0) {
                improvements.set(wip.improvementType.id);
                if (wip.improvementType.isWonder()) {
                    referee.claimWonder(wip.improvementType.id);
                    listener.celebrateWonder(this, wip.improvementType.id);
                }
            }
        }
    }

    protected boolean hasElectrifiedImprovement() {
            int numImprovements = improvements.key.cardinality();
            int improvementId = -1;
            for (int i = 0; i < numImprovements; i++) {
                improvementId = improvements.key.nextSetBit(improvementId + 1);
                if (improvementId < 0) {
                    break;
                }
                ImprovementType improvement = improvements.types.get(improvementId);
                if (improvement.isElectrified) {
                        return true;
                }
        }
        return false;
    }

    protected static City proposeNone(Civilization civ) {
        City none = new City(civ, -1, "None");
        none.size = 0;
        none.precedence = 0;
        none.storedFood = 0;
        none.storedProduction = 0;
        return none;
    }

}