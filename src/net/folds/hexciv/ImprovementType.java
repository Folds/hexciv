package net.folds.hexciv;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class ImprovementType {
    int id;
    String name;
    int capitalCost;     // in shields
    int resaleValue;     // in gold
    int upkeepCost;      // in gold
    int defenseBonus;    // in percent
    int scienceBonus;    // in percent
    int productionBonus; // in percent
    int tradeBonus;      // in percent
    int tradeBonusPerTradeCell;
    boolean isElectrified;
    boolean isGranary;
    boolean preventsPlague;
    boolean preventsFlood;
    boolean preventsFire;
    boolean isBarracks;
    int technologyIndex;

    public ImprovementType(int id, String name, int capitalCost, int upkeepCost, int technologyIndex) {
        this.id = id;
        this.name = name;
        this.capitalCost = capitalCost;
        if (upkeepCost == 0) {
            this.resaleValue = 0;
        } else {
            this.resaleValue = capitalCost;
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
