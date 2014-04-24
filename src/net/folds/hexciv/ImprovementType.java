package net.folds.hexciv;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class ImprovementType {
    String name;
    int capitalCost;     // in shields
    int resaleValue;     // in gold
    int maintenanceCost; // in gold
    int defenseBonus;    // in percent
    int scienceBonus;    // in percent
    int productionBonus; // in percent
    int tradeBonus;      // in percent
    int tradeBonusPerTradeCell;
    boolean isGranary;
    boolean preventsPlague;
    boolean preventsFlood;
    boolean preventsFire;
    boolean isBarracks;
    int technologyIndex;
}
