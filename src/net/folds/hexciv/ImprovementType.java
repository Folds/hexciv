package net.folds.hexciv;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class ImprovementType {
    int id;
    String name;
    String plural;
    int technologyIndex;
    int obsolescerTechnologyIndex;
    int capitalCost;     // in shields
    int resaleValue;     // in gold
    int upkeepCost;      // in gold
    int defenseBonus;    // in percent
    int happyBonusPerCity;
    int productionBonus; // in percent
    int sailBonus;       // in movement points per turn, at sea.
    int scienceBonus;    // in percent
    int techBonus;       // immediately obtain this many technologies
    int templeBonus;     // in percent
    int tradeBonus;      // in percent
    int tradeBonusPerTradeCell;
    int unhappyReductionPerCity;
    int unhappyReductionPerMissedExplorer;
    boolean allowsAnyGovernmentType;
    boolean allowsNukes;
    boolean allowsSpaceship;
    boolean canImposePeace;
    boolean copiesTech;
    boolean isBarracks;
    boolean isElectrified;
    boolean isGranary;
    boolean preventsFire;
    boolean preventsFlood;
    boolean preventsPlague;
    boolean seesAllCities;
    boolean shortensRevolutions;
    boolean affectsContinent;
    boolean affectsAllContinents;
    boolean affectsAllCivilizations;

    protected int getValue(int id) {
        switch (id) {
            case  0: return capitalCost;
            case  1: return resaleValue;
            case  2: return upkeepCost;
            case  3: return defenseBonus;
            case  4: return happyBonusPerCity;
            case  5: return productionBonus;
            case  6: return sailBonus;
            case  7: return scienceBonus;
            case  8: return techBonus;
            case  9: return templeBonus;
            case 10: return tradeBonus;
            case 11: return tradeBonusPerTradeCell;
            case 12: return unhappyReductionPerCity;
            case 13: return unhappyReductionPerMissedExplorer;
            default: return 0;
        }
    }

    protected boolean getBoolean(int id) {
        switch (id) {
            case  0: return allowsAnyGovernmentType;
            case  1: return allowsNukes;
            case  2: return allowsSpaceship;
            case  3: return canImposePeace;
            case  4: return copiesTech;
            case  5: return isBarracks;
            case  6: return isElectrified;
            case  7: return isGranary;
            case  8: return preventsFire;
            case  9: return preventsFlood;
            case 10: return preventsPlague;
            case 11: return seesAllCities;
            case 12: return shortensRevolutions;
            default: return false;
        }
    }

    public ImprovementType(int id, String name, int capitalCost, int upkeepCost, int technologyIndex) {
        int obsolescerTechnologyIndex = -1;
        initialize(id, name, capitalCost, upkeepCost, technologyIndex, obsolescerTechnologyIndex);
    }

    public ImprovementType(int id, String name, int capitalCost, int upkeepCost,
                           int technologyIndex, int obsolescerTechnologyIndex) {
        initialize(id, name, capitalCost, upkeepCost, technologyIndex, obsolescerTechnologyIndex);
    }

    protected void initialize(int id, String name, int capitalCost, int upkeepCost,
                              int technologyIndex, int obsolescerTechnologyIndex) {
        this.id = id;
        this.name = name;
        if ((this.name != null) && (this.name.length() > 0)) {
            String lastLetter = this.name.substring(this.name.length() - 1);
            if (lastLetter.equals("y")) {
                this.plural = this.name.substring(0, this.name.length() - 1) + "ies";
            } else if (lastLetter.equals("s")) {
                this.plural = this.name;
            } else {
                this.plural = name + "s";
            }
        } else {
            this.plural = name + "s";
        }
        this.capitalCost = capitalCost;
        affectsAllCivilizations = false;
        if (upkeepCost == 0) {
            this.resaleValue = 0;
            affectsContinent = true;
            affectsAllContinents = true;
        } else {
            this.resaleValue = capitalCost;
            affectsContinent = false;
            affectsAllContinents = false;
        }
        this.upkeepCost = upkeepCost;
        this.technologyIndex = technologyIndex;
        defenseBonus = 0;
        scienceBonus = 0;
        productionBonus = 0;
        tradeBonus = 0;
        tradeBonusPerTradeCell = 0;
        preventsFire = false;
        preventsFlood = false;
        preventsPlague = false;
        isElectrified = false;
        isBarracks = false;
        this.obsolescerTechnologyIndex = obsolescerTechnologyIndex;
        happyBonusPerCity = 0;
        unhappyReductionPerCity = 0;
        canImposePeace = false;
        shortensRevolutions = false;
        allowsAnyGovernmentType = false;
        templeBonus = 0;
        copiesTech = false;
        sailBonus = 0;
        techBonus = 0;
        unhappyReductionPerMissedExplorer = 0;
        seesAllCities = false;
        allowsSpaceship = false;
        allowsNukes = false;
    }

    public static ImprovementType get(int index) {
        return null;
    }

    public boolean isWonder() {
        if ((resaleValue == 0) && (upkeepCost == 0)) {
            return true;
        }
        return false;
    }

}
