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
    int obsolescerTechnologyIndex;
    int capitalCost;   // in shields
    int logisticsCost; // in shields per turn
    int feedingCost;   // in food per turn
    int range;
    int capacity;      // in terrestrial units
    int aviatorCapacity; // in aerial units
    boolean hasLimitedRange;
    boolean isAerial;
    boolean isCaravan;
    boolean isDiplomat;
    boolean isNaval;
    boolean isSettler;
    boolean isSlippery = true;
    boolean isTerrestrial;
    boolean hasExtraNavalVision;
    boolean hasExtraLandVision;
    boolean ignoresWalls;

    protected UnitType(String name) {
        initialize(600, 1, 1, 1, name, -1, -1); // available immediately
    }

    protected UnitType(int capitalCost, int attackStrength, int defenseStrength,
                       int mobility, String name, int technologyIndex) {
        initialize(capitalCost, attackStrength, defenseStrength,
                   mobility, name, technologyIndex, -1);
    }

    protected UnitType(int capitalCost, int attackStrength, int defenseStrength,
                       int mobility, String name,
                       int technologyIndex, int obsolescerTechnologyIndex) {
        initialize(capitalCost, attackStrength, defenseStrength,
                mobility, name, technologyIndex, obsolescerTechnologyIndex);
    }

    protected void initialize(int capitalCost, int attackStrength, int defenseStrength,
                              int mobility, String name,
                              int technologyIndex, int obsolescerTechnologyIndex) {
        this.name = name;
        this.attackStrength = attackStrength;
        this.defenseStrength = defenseStrength;
        this.mobility = mobility;
        this.technologyIndex = technologyIndex;
        this.obsolescerTechnologyIndex = obsolescerTechnologyIndex;
        this.capitalCost = capitalCost;
        this.logisticsCost = 1;
        this.feedingCost = 0;
        this.range = 0;
        this.hasLimitedRange = false;
        this.isAerial = false;
        this.isDiplomat = false;
        this.isNaval = false;
        this.isSettler = false;
        this.isTerrestrial = true;
        this.hasExtraLandVision = false;
        this.hasExtraNavalVision = false;
        this.ignoresWalls = false;
        this.capacity = 0;
        this.isCaravan = false;
    }

    protected static Vector<UnitType> getChoices() {
        Vector<UnitType> result = new Vector<>(2);
        result.add(proposeMilitia());
        result.add(proposeSettler());
        result.add(proposeCavalry());
        result.add(proposeDiplomat());
        result.add(proposePhalanx());
        result.add(proposeTrireme());
        result.add(proposeChariot());
        result.add(proposeLegion());
        result.add(proposeCatapult());
        result.add(proposeCaravan());
        result.add(proposeKnights());
        result.add(proposeSail());
        result.add(proposeMusketeers());
        result.add(proposeCannon());
        result.add(proposeFrigate());
        result.add(proposeIronclad());
        result.add(proposeRiflemen());
        result.add(proposeTransport());
        result.add(proposeBattleship());
        result.add(proposeCruiser());
        result.add(proposeArmor());
        result.add(proposeFighter());
        result.add(proposeCarrier());
        result.add(proposeBomber());
        result.add(proposeSubmarine());
        result.add(proposeNuke());
        result.add(proposeMechInf());
        result.add(proposeArtillery());
        return result;
    }

    protected void setNaval() {
        isNaval = true;
        isTerrestrial = false;
        isAerial = false;
    }

    protected void setAerial() {
        isNaval = false;
        isTerrestrial = false;
        isAerial = true;
    }

    protected static UnitType proposeMilitia() {
        return new UnitType(10, 1, 1, 1, "Militia", 7); // Flint
    }

    protected static UnitType proposeSettler() {
        UnitType settler = new UnitType(40, 0, 1, 1, "Settler", 14); // Irrigation
        settler.feedingCost = 1;
        settler.isSettler = true;
        return settler;
    }

    protected static UnitType proposeCavalry() {
        return new UnitType(20, 2, 1, 2, "Cavalry", 17, 61); // Horseback Riding -> Conscription
    }

    protected static UnitType proposeDiplomat() {
        UnitType result = new UnitType(30, 0, 0, 2, "Diplomat", 21); // Writing
        result.isSlippery = true;
        result.isDiplomat = true;
        return result;
    }

    protected static UnitType proposePhalanx() {
        return new UnitType(23, 1, 2, 1, "Phalanx", 23, 49); // Bronze -> Gunpowder
    }

    protected static UnitType proposeTrireme() {
        UnitType result = new UnitType(40, 1, 0, 3, "Trireme", 27, 43); // Map -> Navigation
        result.setNaval();
        result.capacity = 2;
        return result;
    }

    protected static UnitType proposeChariot() {
        return new UnitType(40, 4, 1, 2, "Chariot", 26, 37); // Wheel -> Chivalry
    }

    protected static UnitType proposeLegion() {
        return new UnitType(20, 3, 1, 1, "Legion", 28, 61); // Iron -> Conscription
    }

    protected static UnitType proposeCatapult() {
        return new UnitType(40, 6, 1, 1, "Catapult", 29, 54); // Arithmetic -> Metallurgy
    }

    protected static UnitType proposeCaravan() {
        UnitType result = new UnitType(50, 0, 1, 1, "Caravan", 34); // Trade
        result.isCaravan = true;
        result.isSlippery = true;
        return result;
    }

    protected static UnitType proposeKnights() {
        return new UnitType(40, 4, 2, 2, "Knights", 37, 75); // Chivalry -> Automobile
    }

    protected static UnitType proposeSail() {
        UnitType result = new UnitType(40, 1, 1, 3, "Sail", 43, 57); // Navigation -> Magnetism
        result.setNaval();
        result.capacity = 3;
        return result;
    }

    protected static UnitType proposeMusketeers() {
        return new UnitType(30, 2, 3, 1, "Musketeers", 49, 61); // Gunpowder -> Conscription
    }

    protected static UnitType proposeCannon() {
        return new UnitType(40, 8, 1, 1, "Cannon", 54, 85); // Metallurgy -> Robotics
    }

    protected static UnitType proposeFrigate() {
        UnitType result = new UnitType(40, 2, 2, 3, "Frigate", 57, 62); // Magnetism -> Industrialization
        result.setNaval();
        result.capacity = 4;
        return result;
    }

    protected static UnitType proposeIronclad() {
        UnitType result = new UnitType(60, 4, 4, 4, "Ironclad", 58, 74); // Steam Engine -> Combustion
        result.setNaval();
        return result;
    }

    protected static UnitType proposeRiflemen() {
        return new UnitType(30, 3, 5, 1, "Riflemen", 61); // Conscription
    }

    protected static UnitType proposeTransport() {
        UnitType result = new UnitType(50, 0, 3, 4, "Transport", 62); // Industrialization
        result.setNaval();
        result.capacity = 8;
        return result;
    }

    protected static UnitType proposeBattleship() {
        UnitType result = new UnitType(160, 18, 12, 4, "Battleship", 70); // Steel
        result.setNaval();
        result.hasExtraNavalVision = true;
        return result;
    }

    protected static UnitType proposeCruiser() {
        UnitType result = new UnitType(80, 6, 6, 6, "Cruiser", 74); // Combustion
        result.setNaval();
        result.hasExtraNavalVision = true;
        return result;
    }

    protected static UnitType proposeArmor() {
        return new UnitType(80, 10, 5, 3, "Armor", 75); // Automobile
    }

    protected static UnitType proposeFighter() {
        UnitType result = new UnitType(60, 4, 2, 10, "Fighter", 76); // Flight
        result.setAerial();
        result.hasExtraLandVision = true;
        result.hasExtraNavalVision = true;
        result.hasLimitedRange = true;
        result.range = 10;
        return result;
    }

    protected static UnitType proposeCarrier() {
        UnitType result = new UnitType(160, 1, 12, 5, "Carrier", 77); // Jet Engine
        result.setNaval();
        result.aviatorCapacity = 8;
        result.hasExtraNavalVision = true;
        return result;
    }

    protected static UnitType proposeBomber() {
        UnitType result = new UnitType(120, 12, 1, 8, "Bomber", 77); // Jet Engine
        result.setAerial();
        result.hasExtraLandVision = true;
        result.hasExtraNavalVision = true;
        result.hasLimitedRange = true;
        result.range = 16;
        return result;
    }

    protected static UnitType proposeSubmarine() {
        UnitType result = new UnitType(50, 8, 2, 3, "Submarine", 81); // Mass Production
        result.setNaval();
        result.aviatorCapacity = 8;
        result.hasExtraNavalVision = true;
        return result;
    }

    protected static UnitType proposeNuke() {
        UnitType result = new UnitType(160, 99, 0, 16, "Nuke", 81); // Nuclear Fission
        result.isSlippery = true;
        result.setAerial();
        result.hasLimitedRange = true;
        result.range = 16;
        return result;
    }

    protected static UnitType proposeMechInf() {
        UnitType result = new UnitType(50, 6, 6, 3, "Mech. Infantry", 84); // Labor Union
        return result;
    }

    protected static UnitType proposeArtillery() {
        return new UnitType(60, 12, 2, 2, "Artillery", 85); // Robotics
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
