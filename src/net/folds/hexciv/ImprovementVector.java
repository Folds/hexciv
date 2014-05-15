package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on May 13, 2014.
 */
public class ImprovementVector {
    Vector<ImprovementType> improvements;

    ImprovementVector() {
        improvements = new Vector<>();
    }

    protected void initialize() {
        add("Barracks",    40, 1, 10); getLast().isBarracks = true;
        add("Temple",      40, 1, 16);
        add("Granary",     60, 1, 19); getLast().isGranary = true;
        add("City Walls", 120, 2, 19); getLast().preventsFlood = true;
        add("Palace",     200, 5, 19);
        add("Library",     80, 1, 21); getLast().scienceBonus = 50;
        add("Market",      80, 1, 24); getLast().tradeBonus = 50;
        add("Courthouse",  80, 1, 25);
        add("Aqueduct",   120, 2, 32); getLast().preventsFire = true;
        add("Colosseum",  100, 4, 32);
        add("Cathedral",  160, 3, 40);
        add("University", 160, 3, 45); getLast().scienceBonus = 100;
        add("Bank",       120, 3, 51); getLast().tradeBonus = 100;
        add("Factory",    200, 4, 62);
        add("Hydro Plant",240, 4, 65);
        add("Power Plant",160, 4, 72);
        add("Subway",     160, 4, 81);
        add("Recycler",   200, 2, 83);
        add("Manufactory",320, 6, 85);
        add("Nuclear Plant",160,2,90);
        add("Missile Defense",200,4,91);
    }

    static ImprovementVector proposeImprovements() {
        ImprovementVector result = new ImprovementVector();
        result.initialize();
        return result;
    }

    protected void add(int id, String name, int capitalCost, int upkeepCost, int technologyIndex) {
        improvements.add(new ImprovementType(id, name, capitalCost, upkeepCost, technologyIndex));
    }

    protected void add(String name, int capitalCost, int upkeepCost, int technologyIndex) {
        int id = improvements.size();
        add(id, name, capitalCost, upkeepCost, technologyIndex);
    }

    protected int countTypes() {
        return improvements.size();
    }

    protected ImprovementType get(int id) {
        return improvements.get(id);
    }

    protected int getHighestResaleValue() {
        int result = 0;
        for (ImprovementType improvement : improvements) {
            if (improvement.resaleValue > result) {
                result = improvement.resaleValue;
            }
        }
        return result;
    }

    protected ImprovementType getLast() {
        if (improvements == null) {
            return null;
        }
        if (improvements.size() == 0) {
            return null;
        }
        return improvements.get(improvements.size() - 1);
    }

}
