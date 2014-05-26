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
        add("Temple",      40, 1, 16); getLast().unhappyReductionPerCity = 1;
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
        add("Factory",    200, 4, 62); getLast().productionBonus = 50;
        add("Hydro Plant",240, 4, 65); getLast().isElectrified = true;
        add("Power Plant",160, 4, 72); getLast().isElectrified = true;
        add("Subway",     160, 4, 81);
        add("Recycler",   200, 2, 83);
        add("Manufactory",320, 6, 85); getLast().productionBonus = 100;
        add("Nuclear Plant",160,2,90); getLast().isElectrified = true;
        add("Missile Defense",200, 4, 91);
        add("Hanging Garden", 300, 0, 18, 44); getLast().happyBonusPerCity = 1;      // 21
        add("Great Wall",     300, 0, 19, 49); getLast().canImposePeace = true;
        add("Pyramids",       300, 0, 19, 64); getLast().shortensRevolutions = true;
                                               getLast().allowsAnyGovernmentType = true;
        add("Oracle",         300, 0, 22, 40); getLast().templeBonus = 100;
        add("Colossus",       200, 0, 23, 65); getLast().tradeBonusPerTradeCell = 1; // 25
        add("Lighthouse",     200, 0, 27, 57); getLast().sailBonus = 2;
        add("Great Library",  300, 0, 33, 45); getLast().copiesTech = true;
        add("Observatory",    300, 0, 36, 75); getLast().scienceBonus = 100;
        add("Auditorium",     400, 0, 40);     getLast().unhappyReductionPerCity = 2;
                                               getLast().affectsAllContinents = false;
        add("Sistine Chapel", 300, 0, 40, 64);                                       // 30
        add("Circumnavigation", 400, 0, 43);     getLast().sailBonus = 1;
        add("Globe Theatre",  400, 0, 50, 66); getLast().unhappyReductionPerCity = 99;
                                               getLast().affectsContinent = false;
                                               getLast().affectsAllContinents = false;
        add("Physics School", 400, 0, 52, 87); getLast().scienceBonus = 50;
        add("Beagle Voyage",  300, 0, 59);     getLast().techBonus = 2;
        add("Timocracy",      300, 0, 62);     getLast().unhappyReductionPerMissedExplorer = 1; // 35
        add("World League",   600, 0, 64);     getLast().canImposePeace = true;
        add("Great Dam",      600, 0, 66);     getLast().isElectrified = true;
                                               getLast().affectsAllContinents = false;
        add("Gemini",         300, 0, 79);     getLast().seesAllCities = true;
        add("Hypertext",      600, 0, 67);     getLast().scienceBonus = 50;
        add("Cancer Cure",    600, 0, 71);     getLast().happyBonusPerCity = 1;      // 40
        add("Space Station",  400, 0, 85);     getLast().allowsSpaceship = true;
                                               getLast().affectsAllCivilizations = true;
        add("Trinitite",      600, 0, 87);     getLast().allowsNukes = true;
                                               getLast().affectsAllCivilizations = true;
    }

    static ImprovementVector proposeImprovements() {
        ImprovementVector result = new ImprovementVector();
        result.initialize();
        return result;
    }

    protected void add(int id, String name, int capitalCost, int upkeepCost, int technologyIndex) {
        improvements.add(new ImprovementType(id, name, capitalCost, upkeepCost, technologyIndex));
    }

    protected void add(int id, String name, int capitalCost, int upkeepCost, int technologyIndex, int obsoleterIndex) {
        improvements.add(new ImprovementType(id, name, capitalCost, upkeepCost, technologyIndex, obsoleterIndex));
    }

    protected void add(String name, int capitalCost, int upkeepCost, int technologyIndex) {
        int id = improvements.size();
        add(id, name, capitalCost, upkeepCost, technologyIndex);
    }

    protected void add(String name, int capitalCost, int upkeepCost, int technologyIndex, int obsoleterIndex) {
        int id = improvements.size();
        add(id, name, capitalCost, upkeepCost, technologyIndex, obsoleterIndex);
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
