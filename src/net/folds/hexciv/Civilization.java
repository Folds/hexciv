package net.folds.hexciv;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class Civilization {
    protected Vector<City> cities;   // should be private?
    protected Vector<City> destroyedCities;   // should be private?
    private int precedence;
    private int governmentTypeId;
    private String name;
    private String ruler;
    private Color color;
    protected TechKey techKey;   // should be private?
    private Vector<Relationship> relationships;
    private Vector<GovernmentType> governmentTypes;
    protected Vector<UnitType> unitTypes;   // should be private?
    protected ImprovementVector improvements;
    protected int storedMoney;   // should be private?
    protected int storedScience;   // should be private?
    protected int taxPercentage;   // should be private?
    protected int sciencePercentage;   // should be private?
    private int longestPeaceAD;
    private int currentPeaceAD;
    protected BitSet seenCells;   // should be private?
    protected BitSet cellsExploredByLand;   // should be private?
    protected int techPriceFactor;   // should be private?
    protected TechChooser techChooser;  // should be private?
    private boolean hasToldStory;
    protected int turnCounter;   // should be private?
    private int anarchyTurnCounter;
    Vector<BitSet> continents;
    protected Planner planner;   // should be private?
    protected long recentTurnLengthInMilliseconds;
    protected StatSheet statSheet;

    protected Civilization(Vector<GovernmentType> governmentTypes,
                           Vector<UnitType> unitTypes,
                           TechTree techTree,
                           ImprovementVector improvements) {
        this.governmentTypes = governmentTypes;
        this.unitTypes = unitTypes;
        this.improvements = improvements;
        longestPeaceAD = 0;
        currentPeaceAD = 0;
        techKey = new TechKey(techTree);
        techPriceFactor = 10;
        hasToldStory = false;
        turnCounter = 0;
        anarchyTurnCounter = 0;
        int startTurn = 0;
        int maxPossibleTurn = 550;
        statSheet = new StatSheet(startTurn, maxPossibleTurn);
        color = Color.WHITE;
        statSheet.maxTechId.setValueNames(techKey.allNames());
    }

    protected void initialize(WorldMap map, Vector<Integer> foreignLocations, GameListener listener, ClaimReferee referee) {
        governmentTypeId = GovernmentType.proposeId("Despotism");
        storedMoney = 0;
        storedScience = 0;
        taxPercentage = 50;
        sciencePercentage = 50;
        techChooser = new TechChooser();
        cities = new Vector<City>(1);
        City none = City.proposeNone(this);
        cities.add(none);
        UnitType settlerType = UnitType.lookupUnitType(unitTypes, "Settler");
        CivPlayer ruler = new CivPlayer(map, this, listener, referee);
        int cellId = ruler.chooseStartLocation(map, foreignLocations);
        Unit settler = new Unit(settlerType, cellId);
        none.add(settler);
        seenCells = new BitSet(map.countCells());
        continents = new Vector<>(1);
        BitSet continent = new BitSet(map.countCells());
        continent.set(cellId);
        continents.add(continent);
        seenCells.set(cellId);
        cellsExploredByLand = new BitSet(map.countCells());
        seeNeighborsOf(map, cellId);
        planner = new Planner(this, map);
    }

    protected void abandonFurthestUnitThatNeedsMaintenance(WorldMap map, City city, GameListener listener) {
        int unitId = -1;
        int furthestDistance = -1;
        for (int i = 0; i < city.units.size(); i++) {
            int distance = map.getDistanceInCells(city.location, city.units.get(i).getLocation());
            if (distance > furthestDistance) {
                furthestDistance = distance;
                unitId = i;
            }
        }
        listener.bemoanUnsupported(city, city.units.get(unitId));
        city.units.remove(unitId);
    }

    protected String accumulateSummary(String arg, int num, String singular, String plural) {
        String result = arg;
        if (num == 0) {
            return result;
        }
        if (!result.equals("")) {
            result = result + ", ";
        }
        if (num == 1) {
            return result + num + singular;
        }
        return result + num + plural;
    }

    protected void addContinent(WorldMap map, int cellId) {
        BitSet continent = new BitSet(map.countCells());
        continent.set(cellId);
        continents.add(continent);
    }

    protected boolean areAnyCitiesRioting(WorldMap map, ClaimReferee referee) {
        for (City city : cities) {
            if (!city.isNone()) {
                if (isCityRioting(map, city, referee)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean areOnSameContinent(WorldMap map, City first, City second) {
        return areOnSameContinent(map, first.location, second.location);
    }

    protected boolean areOnSameContinent(WorldMap map, int firstCellId, int secondCellId) {
        Pathfinder pathfinder = new Pathfinder(map, seenCells, continents);
        return pathfinder.areOnSameContinent(firstCellId, secondCellId);
    }

    protected boolean atPeace() {
        if ((countCities() == 0) && (countUnits() == 0)) {
            return false;
        }
        return true;
    }

    protected ImprovementType availableWonderThatCostsAtLeast(int capitalCost, ClaimReferee referee) {
        return improvements.availableWonderThatCostsAtLeast(capitalCost, techKey, referee);
    }

    protected void bemoanDisorder(City city, GameListener listener) {
        listener.bemoanDisorder(city);
    }

    protected boolean canAttack(Unit unit) {
        return canAttack(unit.unitType);
    }

    protected boolean canAttack(UnitType unitType) {
        if ((unitType.attackStrength > 0) && (unitType.isTerrestrial)) {
            return true;
        }
        return false;
    }

    protected boolean canBe(GovernmentType governmentType, ClaimReferee referee) {
        if (techKey.hasTech(governmentType.technologyIndex)) {
            return true;
        }
        for (City city : cities) {
            if (city.allowsAnyGovernmentType(referee)) {
                return true;
            }
        }
        return false;
    }

    protected boolean canBuildWonder(ClaimReferee referee) {
        return improvements.canBuildWonder(techKey, referee);
    }

    protected int countBenefit(WorldMap map, City city, ClaimReferee referee, int benefitId) {
        int result = countBenefitFromWonders(map, city, referee, benefitId);
        int improvementId = -1;
        int numImprovements = city.improvements.key.cardinality();
        for (int i = 0; i < numImprovements; i++) {
            improvementId = city.improvements.key.nextSetBit(improvementId + 1);
            if (improvementId < 0) {
                break;
            }
            if (!referee.isObsolete(improvementId)) {
                if (!city.improvements.types.get(improvementId).isWonder()) {
                    result = result + city.improvements.types.get(improvementId).getValue(benefitId);
                }
            }
        }
        return result;
    }

    protected int countBenefitFromWonders(WorldMap map, City city, ClaimReferee referee, int benefitId) {
        int result = referee.countBenefitFromWondersThatAffectAllCivilizations(map, city, benefitId);
        for (City otherCity : cities) {
            int improvementId = -1;
            int numImprovements = otherCity.improvements.key.cardinality();
            for (int i = 0; i < numImprovements; i++) {
                improvementId = otherCity.improvements.key.nextSetBit(improvementId + 1);
                if (improvementId < 0) {
                    break;
                }
                if (otherCity.improvements.types.get(improvementId).isWonder()) {
                    if ((city == otherCity) || (otherCity.improvements.types.get(improvementId).affectsContinent)) {
                        if (   (city == otherCity)
                                || (otherCity.improvements.types.get(improvementId).affectsAllContinents)
                                || (getContinentNumber(map, city.location) == getContinentNumber(map, otherCity.location))
                                ) {
                            if (!referee.isObsolete(otherCity.improvements.types.get(improvementId))) {
                                result = result + otherCity.improvements.types.get(improvementId).getValue(benefitId);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    protected int countCities() {
        // the "None" city does not count.
        if (cities == null) {
            return -1;
        }
        return cities.size() - 1;
    }

    protected int countCitizens() {
        int result = 0;
        for (City city : cities) {
            result = result + city.size;
        }
        return result;
    }

    protected int countContentCitizens(WorldMap map, ClaimReferee referee) {
        int result = 0;
        for (City city : cities) {
            result = result + countContentCitizens(map, city, referee);
        }
        return result;
    }

    protected int countContentCitizens(WorldMap map, City city, ClaimReferee referee) {
        if (city.location < 0) {
            return 0;
        }
        int luxuries = countLuxuries(map, city, referee);
        int content = city.size;
        if (content > 3) {
            content = 3;
        }
        int unhappy = city.size - content;
        int sadness = 0;
        if (isMilitarist()) {
            if (unhappy > 0) {
                int occupiers = countMilitaryUnitsIn(city);
                if (unhappy < occupiers) {
                    content = content + unhappy;
                    unhappy = 0;
                } else {
                    content = content + occupiers;
                    unhappy = unhappy - occupiers;
                }
            }
        } else {
            int farUnits = countDistantMilitaryUnits(map, city);
            sadness = sadness + farUnits * getUnhappinessOfEachRemoteMilitaryUnit(map, city, referee);
            if (sadness > content) {
                unhappy = unhappy + content;
                sadness = sadness - content;
                content = 0;
            } else {
                unhappy = unhappy + sadness;
                content = content - sadness;
                sadness = 0;
            }
        }
        int unhappyReduction = countUnhappyReductionPerCity(map, city, referee);
        if (unhappyReduction > 0) {
            if (sadness > 0) {
                if (sadness > unhappyReduction) {
                    sadness = sadness - unhappyReduction;
                    unhappyReduction = 0;
                } else {
                    unhappyReduction = unhappyReduction - sadness;
                    sadness = 0;
                }
            }
        }
        if (unhappyReduction > 0) {
            if (unhappy > 0) {
                if (unhappy > unhappyReduction) {
                    unhappy = unhappy - unhappyReduction;
                    content = content + unhappyReduction;
                    unhappyReduction = 0;
                }
            } else {
                unhappyReduction = unhappyReduction - unhappy;
                content = content + unhappy;
                unhappy = 0;
            }
        }
        int usedLuxuries = 0;
        if (2 * sadness < luxuries - usedLuxuries) {
            usedLuxuries = usedLuxuries + 2 * sadness;
        } else {
            usedLuxuries = usedLuxuries + 2 * ((luxuries - usedLuxuries) / 2);
        }
        if (2 * content < luxuries - usedLuxuries) {
            usedLuxuries = usedLuxuries + 2 * content;
            content = 0;
        } else {
            int affected = (luxuries - usedLuxuries) / 2;
            if (luxuries < usedLuxuries) {
                affected = 0;
            }
            usedLuxuries = usedLuxuries + 2 * affected;
            content = content - affected;
        }
        if (4 * unhappy < luxuries - usedLuxuries) {
            usedLuxuries = usedLuxuries + 4 * unhappy;
            unhappy = 0;
        } else {
            int affected = (luxuries - usedLuxuries) / 4;
            if (luxuries < usedLuxuries) {
                affected = 0;
            }
            usedLuxuries = usedLuxuries + 4 * affected;
            unhappy = unhappy - affected;
        }
        if (2 * unhappy < luxuries - usedLuxuries) {
            content = content + unhappy;
        } else {
            int affected = (luxuries - usedLuxuries) / 2;
            if (luxuries < usedLuxuries) {
                affected = 0;
            }
            content = content + affected;
        }
        int happyBonus = countHappyBonusPerCity(map, city, referee);
        if (happyBonus > 0) {
            if (content > 0) {
                if (happyBonus > content) {
                    content = 0;
                } else {
                    content = content - happyBonus;
                }
            }
        }
        return content;
    }

    protected int countDefenseBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 3);
    }

    protected int countDistantMilitaryUnits(WorldMap map, City city) {
        int result = 0;
        if (city.units == null) {
            return 0;
        }
        for (Unit unit : city.units) {
            if (canAttack(unit)) {
                if (map.getDistanceInCells(city.location, unit.getLocation()) > 2) {
                    result = result + 1;
                }
            }
        }
        return result;
    }

    protected int countEntertainers() {
        int result = 0;
        for (City city : cities) {
            result = result + city.numEntertainers;
        }
        return result;
    }

    protected int countExplorers(WorldMap map) {
        int result = 0;
        for (City city : cities) {
            result = result + countExplorers(map, city);
        }
        return result;
    }

    protected int countExplorers(WorldMap map, City city) {
        int result = 0;
        if (city.units == null) {
            return result;
        }
        for (Unit unit : city.units) {
            if (   (unit.unitType.isTerrestrial)
                && (!unit.unitType.isSettler)
                && (!map.hasCity(unit.getLocation()))
               ) {
                result = result + 1;
            }
        }
        return result;
    }

    protected int countFood(WorldMap map, City city) {
        if (city.location < 0) {
            return 0;
        }
        int result = countFood(map, city.location);
        if (city.farms == null) {
            return result;
        }
        for (Integer farm : city.farms) {
            result = result + countFood(map, farm);
        }
        return result;
    }

    protected int countFood(WorldMap map, int cellId) {
        if (cellId < 0) {
            return 0;
        }
        TerrainTypes terrain = map.getTerrain(cellId);
        boolean hasBonus = map.hasBonus(cellId);
        int irrigationBonus = 0;
        if (map.hasIrrigation(cellId)) {
            irrigationBonus = 1;
        }
        int result = 0;
        if (   (terrain == TerrainTypes.tundra) || (terrain == TerrainTypes.forest)
                || (terrain == TerrainTypes.ocean)  || (terrain == TerrainTypes.sea) || (terrain == TerrainTypes.lake)) {
            if (hasBonus) {
                result = 3;
            } else {
                result = 1;
            }
        }
        if (terrain == TerrainTypes.seaIce) {
            if (hasBonus) {
                result = 3;
            }
        }
        if (terrain == TerrainTypes.desert) {
            if (hasBonus) {
                result = 3;
            }
            result = result + irrigationBonus;
        }
        if (terrain == TerrainTypes.grass) {
            if (hasBonus) {
                result = 3;
            } else {
                result = 2;
            }
            result = result + irrigationBonus;
        }
        if ((terrain == TerrainTypes.hills) || (terrain == TerrainTypes.plains)) {
            result = 1 + irrigationBonus;
        }
        if ((terrain == TerrainTypes.jungle) || (terrain == TerrainTypes.swamp)) {
            result = 1;
        }
        if (terrain == TerrainTypes.river) {
            result = 2 + irrigationBonus;
        }
        if ((result > 2) && (governmentBonus() < 0)) {
            result = result + governmentBonus();
        }
        if (map.hasRailroad(cellId)) {
            result = result + result / 2;
        }
        return result;
    }

    protected int countFoodSurplus(WorldMap map, City city) {
        int result = countFood(map, city);
        result = result - 2 * city.size;
        result = result - city.countSettlers();
        if (!isMilitarist()) {
            result = result - city.countSettlers();
        }
        return result;
    }

    protected int countFutureTech() {
        return techKey.countFutureTech();
    }

    protected int countHappyCitizens(WorldMap map, ClaimReferee referee) {
        int result = 0;
        for (City city : cities) {
            result = result + countHappyCitizens(map, city, referee);
        }
        return result;
    }

    protected int countHappyCitizens(WorldMap map, City city, ClaimReferee referee) {
        if (city.location < 0) {
            return 0;
        }
        int luxuries = countLuxuries(map, city, referee);
        int content = city.size;
        if (content > 3) {
            content = 3;
        }
        int unhappy = city.size - content;
        int sadness = 0;
        if (isMilitarist()) {
            if (unhappy > 0) {
                int occupiers = countMilitaryUnitsIn(city);
                if (unhappy < occupiers) {
                    content = content + unhappy;
                    unhappy = 0;
                } else {
                    content = content + occupiers;
                    unhappy = unhappy - occupiers;
                }
            }
        } else {
            int farUnits = countDistantMilitaryUnits(map, city);
            sadness = farUnits * getUnhappinessOfEachRemoteMilitaryUnit(map, city, referee);
            if (sadness > content) {
                unhappy = unhappy + content;
                sadness = sadness - content;
                content = 0;
            } else {
                unhappy = unhappy + sadness;
                content = content - sadness;
                sadness = 0;
            }
        }
        int unhappyReduction = countUnhappyReductionPerCity(map, city, referee);
        if (unhappyReduction > 0) {
            if (sadness > 0) {
                if (sadness > unhappyReduction) {
                    sadness = sadness - unhappyReduction;
                    unhappyReduction = 0;
                } else {
                    unhappyReduction = unhappyReduction - sadness;
                    sadness = 0;
                }
            }
        }
        if (unhappyReduction > 0) {
            if (unhappy > 0) {
                if (unhappy > unhappyReduction) {
                    unhappy = unhappy - unhappyReduction;
                    content = content + unhappyReduction;
                    unhappyReduction = 0;
                }
            } else {
                unhappyReduction = unhappyReduction - unhappy;
                content = content + unhappy;
                unhappy = 0;
            }
        }
        int happy = 0;
        int usedLuxuries = 0;
        if (2 * sadness < luxuries - usedLuxuries) {
            usedLuxuries = usedLuxuries + 2 * sadness;
        } else {
            usedLuxuries = usedLuxuries + 2 * ((luxuries - usedLuxuries) / 2);
        }
        if (2 * content < luxuries - usedLuxuries) {
            usedLuxuries = usedLuxuries + 2 * content;
            happy = happy + content;
        } else {
            int affected = (luxuries - usedLuxuries) / 2;
            if (luxuries < usedLuxuries) {
                affected = 0;
            }
            usedLuxuries = usedLuxuries + 2 * affected;
            happy = happy + affected;
        }
        if (4 * unhappy < luxuries - usedLuxuries) {
            happy = happy + unhappy;
        } else {
            int affected = (luxuries - usedLuxuries) / 4;
            if (luxuries < usedLuxuries) {
                affected = 0;
            }
            happy = happy + affected;
        }
        int happyBonus = countHappyBonusPerCity(map, city, referee);
        if (happyBonus > 0) {
            if (content > 0) {
                if (happyBonus > content) {
                    happy = happy + content;
                } else {
                    happy = happy + happyBonus;
                }
            }
        }
        if (happyBonus > 0) {
            if (unhappy > 0) {
                if (happyBonus > unhappy) {
                    happy = happy + unhappy / 2;
                } else {
                    happy = happy + happyBonus / 2;
                }
            }
        }
        return happy;
    }

    protected int countHappyBonusPerCity(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 4);
    }

    protected Vector<Integer> countImprovements() {
        int numImprovementTypes = 0;
        if (improvements == null) {
            Vector<Integer> result = new Vector<Integer>(numImprovementTypes);
            Util.initialize(result, 0);
            return result;
        }
        numImprovementTypes = improvements.countTypes();
        Vector<Integer> result = new Vector<Integer>(numImprovementTypes);
        Util.initialize(result, 0);

        for (City city : cities) {
            for (int i = 0; i < numImprovementTypes; i++) {
                if (city.improvements.get(i)) {
                    Util.incrementVectorElement(result, i);
                }
            }
        }
        return result;
    }

    protected int countLuxuries(WorldMap map, City city, ClaimReferee referee) {
        int money = countMoney(map, city, referee);
        int result = 2 * city.numEntertainers;
        result = result + (money * getLuxuryPercentage()) / 100;
        result = (result * getLuxuryFactor(map, city, referee)) / 100;
        return result;
    }

    protected int countMilitaryUnitsIn(City city) {
        int result = 0;
        for (City anyCity : cities) {
            if (anyCity.units == null) {
                return result;
            }
            for (Unit unit : anyCity.units) {
                if (unit.getLocation() == city.location) {
                    if (canAttack(unit)) {
                        result = result + 1;
                    }
                }
            }
        }
        return result;
    }

    protected int countNativeMilitaryUnitsIn(City city) {
        int result = 0;
        if (city.units == null) {
            return result;
        }
        for (Unit unit : city.units) {
            if (unit.getLocation() == city.location) {
                if (canAttack(unit)) {
                    result = result + 1;
                }
            }
        }
        return result;
    }

    protected int countMoney(WorldMap map, City city, ClaimReferee referee) {
        if (city.location < 0) {
            return 0;
        }
        int numTradeCells = 0;
        int result = countMoney(map, city.location);
        if (result > 0) {
            numTradeCells = numTradeCells + 1;
        }
        if (city.farms == null) {
            return result;
        }
        for (Integer farm : city.farms) {
            int farmMoney = countMoney(map, farm);
            result = result + farmMoney;
            if (farmMoney > 0) {
                numTradeCells = numTradeCells + 1;
            }
        }
        if (numTradeCells > 0) {
            int tradeBonusPerTradeCell = countTradeBonusPerTradeCell(map, city, referee);
            result = result + numTradeCells * tradeBonusPerTradeCell;
        }
        return result;
    }

    protected int countMoney(WorldMap map, int cellId) {
        if (cellId < 0) {
            return 0;
        }
        TerrainTypes terrain = map.getTerrain(cellId);
        boolean hasBonus = map.hasBonus(cellId);
        int roadBonus = 0;
        if ((map.hasRoad(cellId)) || (map.hasRailroad(cellId)) || (map.hasCity(cellId))) {
            roadBonus = 1;
        }
        int result = 0;
        if ((terrain == TerrainTypes.desert) || (terrain == TerrainTypes.grass) || (terrain == TerrainTypes.plains)) {
            result = roadBonus;
        }
        if (terrain == TerrainTypes.jungle) {
            result = 4 * roadBonus;
            if (governmentBonus() < 0) {
                result = result + governmentBonus();
            }
        }
        if (terrain == TerrainTypes.mountains) {
            result = 6 * roadBonus;
            if (governmentBonus() < 0) {
                result = result + governmentBonus();
            }
        }
        if (terrain == TerrainTypes.ocean) {
            result = 2;
        }
        if (terrain == TerrainTypes.river) {
            result = 1;
        }
        if ((result > 0) && (governmentBonus() > 0)) {
            result = result + governmentBonus();
        }
        if (map.hasRailroad(cellId)) {
            result = result + result / 2;
        }
        return result;
    }

    protected int countMyriads() {
        int result = 0;
        for (City city : cities) {
            int size = city.size;
            int increment = size * (size + 1) / 2;
            result = result + increment;
        }
        return result;
    }

    protected int countNativeOccupiers(City city) {
        int result = 0;
        if (city.units == null) {
            return 0;
        }
        for (Unit unit : city.units) {
            if ((canAttack(unit)) && (unit.getLocation() == city.location)) {
                result = result + 1;
            }
        }
        return result;
    }

    protected int countOre(WorldMap map, City city, ClaimReferee referee) {
        if (city.location < 0) {
            return 0;
        }
        int result = countOre(map, city.location);
        if (city.farms == null) {
            return result;
        }
        for (Integer farm : city.farms) {
            result = result + countOre(map, farm);
        }
        result = result * getProductionFactor(map, city, referee) / 100;
        return result;
    }

    protected int countOre(WorldMap map, int cellId) {
        if (cellId < 0) {
            return 0;
        }
        TerrainTypes terrain = map.getTerrain(cellId);
        boolean hasBonus = map.hasBonus(cellId);
        int miningBonus = 0;
        if (map.hasMine(cellId)) {
            miningBonus = 1;
        }
        int result = 0;
        if ((terrain == TerrainTypes.desert) || (terrain == TerrainTypes.mountains)) {
            result = 1 + miningBonus;
        }
        if (terrain == TerrainTypes.forest) {
            result = 2;
        }
        if ((terrain == TerrainTypes.grass) || (terrain == TerrainTypes.river)) {
            if (hasBonus) {
                result = 1;
            }
        }
        if (terrain == TerrainTypes.swamp) {
            if (hasBonus) {
                result = 4;
            }
        }
        if (terrain == TerrainTypes.plains) {
            result = 1;
            if (hasBonus) {
                result = 3;
            }
        }
        if (terrain == TerrainTypes.hills) {
            if (hasBonus) {
                result = 2;
            }
            result = result + 3 * miningBonus;
        }
        if ((result > 2) && (governmentBonus() < 0) && (terrain != TerrainTypes.plains)) {
            result = result + governmentBonus();
        }
        if (map.hasRailroad(cellId)) {
            result = result + result / 2;
        }
        return result;
    }

    protected int countProductionBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 5);
    }

    protected int countProductionSurplus(WorldMap map, City city, ClaimReferee referee) {
        if (!isMilitarist()) {
            int happy = countHappyCitizens(map, city, referee);
            int unhappy = countUnhappyCitizens(map, city, referee);
            if (unhappy > happy) {
                return -getLogisticsCost(city);
            }
        }
        return countOre(map, city, referee) - getLogisticsCost(city);
    }

    protected int countRawTrade(WorldMap map, City city) {
        if (city.location < 0) {
            return 0;
        }
        int result = countMoney(map, city.location);
        if (city.farms == null) {
            return result;
        }
        for (Integer farm : city.farms) {
            result = result + countMoney(map, farm);
        }
        return result;
    }

    protected int countSailBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 6);
    }

    protected int countScienceBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 7);
    }

    protected int countSeenCells() {
        return seenCells.cardinality();
    }

    protected int countSeenCells(Vector<Integer> region) {
        int result = 0;
        for (int cellId : region) {
            if (seenCells.get(cellId)) {
                result = result + 1;
            }
        }
        return result;
    }

    int countSeenLandCells(WorldMap map) {
        int id = -1;
        int numSeenCells = seenCells.cardinality();
        int result = 0;
        for (int i = 0; i < numSeenCells; i++) {
            id = seenCells.nextSetBit(id + 1);
            if (map.isLand(id)) {
                result = result + 1;
            }
        }
        return result;
    }

    int countSeenNonLandCells(WorldMap map) {
        int numSeenCells = seenCells.cardinality();
        int numSeenLandCells = countSeenLandCells(map);
        return numSeenCells - numSeenLandCells;
    }

    protected int countStoredFood() {
        int result = 0;
        for (City city : cities) {
            result = result + city.storedFood;
        }
        return result;
    }

    protected int countStoredProduction() {
        int result = 0;
        for (City city : cities) {
            result = result + city.storedProduction;
        }
        return result;
    }

    protected int countScience(WorldMap map, City city, ClaimReferee referee) {
        int money = countMoney(map, city, referee);
        int result = 2 * city.numScientists;
        result = result + (money * sciencePercentage) / 100;
        result = (result * getScienceFactor(map, city, referee)) / 100;
        return result;
    }

    protected int countTax(WorldMap map, City city, ClaimReferee referee) {
        int money = countMoney(map, city, referee);
        int result = 2 * city.numTaxMen;
        result = result + money - countLuxuries(map, city, referee) - countScience(map, city, referee);
        result = (result * getTaxFactor(map, city, referee)) / 100;
        return result;
    }

    int countTechs() {
        return techKey.countTechs();
    }

    protected int countTempleBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 9);
    }

    protected int countThousands() {
        return 10 * countMyriads();
    }

    protected int countTradeBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 10);
    }

    protected int countTradeBonusPerTradeCell(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 11);
    }

    protected int countUnhappyCitizens(WorldMap map, ClaimReferee referee) {
        int result = 0;
        for (City city : cities) {
            result = result + countUnhappyCitizens(map, city, referee);
        }
        return result;
    }

    protected int countUnhappyCitizens(WorldMap map, City city, ClaimReferee referee) {
        if (city.location < 0) {
            return 0;
        }
        int total = city.size;
        int happy = countHappyCitizens(map, city, referee);
        int content = countContentCitizens(map, city, referee);
        return total - happy - content;
    }

    protected int countUnhappyReductionPerCity(WorldMap map, City city, ClaimReferee referee) {
        int result = countBenefit(map, city, referee, 12);
        int baseTempleEffect = city.improvements.types.get(1).unhappyReductionPerCity;
        int templeEffect = 0;
        if (city.improvements.key.get(1)) {
            templeEffect = baseTempleEffect;
            if (isAffectedByMysticism(referee)) {
                templeEffect = 2 * templeEffect;
            }
            int templeFactor = 100 + countTempleBonus(map, city, referee);
            templeEffect = (templeEffect * templeFactor) / 100;
            result = result + templeEffect - baseTempleEffect;
        }
        return result;
    }

    protected int countUnhappyReductionPerMissedExplorer(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 13);
    }

    protected int countUnits() {
        int result = 0;
        if (cities == null) {
            return result;
        }
        for (City city : cities) {
            result = result + city.countUnits();
        }
        return result;
    }

    protected int countAerialUnits() {
        int result = 0;
        if (cities == null) {
            return result;
        }
        for (City city : cities) {
            result = result + city.countAerialUnits();
        }
        return result;
    }

    protected int countCaravans() {
        int result = 0;
        if (cities == null) {
            return result;
        }
        for (City city : cities) {
            result = result + city.countCaravans();
        }
        return result;
    }

    protected int countNavalUnits() {
        int result = 0;
        if (cities == null) {
            return result;
        }
        for (City city : cities) {
            result = result + city.countNavalUnits();
        }
        return result;
    }

    protected int countSettlers() {
        int result = 0;
        if (cities == null) {
            return result;
        }
        for (City city : cities) {
            result = result + city.countSettlers();
        }
        return result;
    }

    protected int countTerrestrialUnits() {
        int result = 0;
        if (cities == null) {
            return result;
        }
        for (City city : cities) {
            result = result + city.countTerrestrialUnits();
        }
        return result;
    }

    protected int countTradeRoutes() {
        int result = 0;
        if (cities == null) {
            return result;
        }
        for (City city : cities) {
            result = result + city.countTradeRoutes();
        }
        return result;
    }

    protected int countTroops() {
        return countTerrestrialUnits() - countSettlers() - countCaravans();
    }

    protected int countWonders() {
        int result = 0;
        for (City city : cities) {
            result = result + city.countWonders();
        }
        return result;
    }

    protected String describeGovernment() {
        return getGovernmentType().name;
    }

    protected boolean doesCityHaveExplorerWithLogisticsCost(WorldMap map, City city) {
        boolean result = false;
        if (city.units == null) {
            return false;
        }
        for (Unit unit : city.units) {
            if (   (unit.unitType.isTerrestrial)
                    && (!unit.unitType.isSettler)
                    && (!map.hasCity(unit.getLocation()))
                    && (canAttack(unit))
                    && (!isAnarchist())
                    && ((!isDespotic()) || (getLogisticsCost(city) > 0))
                    ) {
                return true;
            }
        }
        return false;
    }

    protected void fixSciencePercentage() {
        if ((taxPercentage < 0) || (taxPercentage > 100)) {
            fixTaxPercentage();
        }
        sciencePercentage = 100 - taxPercentage;
    }

    protected void fixTaxPercentage() {
        if (taxPercentage > 100) {
            taxPercentage = 100;
            return;
        }
        if (taxPercentage < 0) {
            taxPercentage = 0;
        }
    }

    protected City foundCity(WorldMap map, int cellId, CivPlayer ruler, ClaimReferee referee) {
        map.foundCity(cellId);
        planner.buffer(cellId, 4);
        City city = new City(this, cellId, name + "_" + countCities());
        cities.add(city);
        int farmLocation = ruler.chooseFarm(map, cellId, referee);
        if (farmLocation >= 0) {
            if (city.farms == null) {
                city.farms = new Vector<>(18);
            }
            city.farms.add((Integer) farmLocation);
            if (city.numEntertainers > 0) {
                city.numEntertainers = city.numEntertainers - 1;
            } else if (city.numScientists > 0) {
                city.numScientists = city.numScientists - 1;
            } else if (city.numTaxMen > 0) {
                city.numTaxMen = city.numTaxMen - 1;
            }
        }
        return city;
    }

    protected String formatGovernment() {
        String name = governmentTypes.get(governmentTypeId).name;
        if (name.equals("Undefined")) {
            return name;
        }
        if (name.substring(1, 1).equals("A")) {
            return "an " + name;
        }
        return "a " + name;
    }

    protected String formatPopulation() {
        int numThousands = countThousands();
        if (numThousands == 0) {
            return "no people";
        }
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(numThousands) + ",000 people";
    }

    protected int getAnarchyLength(ClaimReferee referee) {
        for (City city : cities) {
            if (city.shortensRevolutions(referee)) {
                return 0;
            }
        }
        return 2;
    }

    protected Vector<String> getBrag(WorldMap map, ClaimReferee referee) {
        Vector<String> result = new Vector<>();
        String unitSummary = "";
        unitSummary = accumulateSummary(unitSummary, countSettlers(), " settler", " settlers");
        unitSummary = accumulateSummary(unitSummary, countTroops(), " troop", " troops");
        unitSummary = accumulateSummary(unitSummary, countNavalUnits(), " ship", " ships");
        unitSummary = accumulateSummary(unitSummary, countAerialUnits(), " plane", " planes");
        unitSummary = accumulateSummary(unitSummary, countCaravans(), " caravan", " caravans");
        if (countCities() > 0) {
            if (countCities() == 1) {
                result.add(name + " is " + formatGovernment() + " with " + formatPopulation() + " in " + countCities() + " city, with");
            } else {
                result.add(name + " is " + formatGovernment() + " with " + formatPopulation() + " in " + countCities() + " cities, with");
            }
            result.add(  countHappyCitizens(map, referee) + " happy, "
                    + countContentCitizens(map, referee) + " content, and "
                    + countUnhappyCitizens(map, referee) + " unhappy citizens;");
            if (!unitSummary.equals("")) {
                result.add("has " + unitSummary + ";");
            }
        } else {
            if (!unitSummary.equals("")) {
                result.add(name + " has " + unitSummary + ";");
            }
        }
        int numSeenNonLandCells = countSeenNonLandCells(map);
        if (numSeenNonLandCells > 0) {
            result.add("sees " + countSeenLandCells(map) + " land "
                    + "and " + numSeenNonLandCells + " other cells "
                    + "(" + getPercentageOfWorldSeen(map) + "% of world);");
        } else {
            result.add("sees " + countSeenLandCells(map) + " land cells "
                    + "(" + getPercentageOfWorldSeen(map) + "% of world);");
        }
        String maxTechName = techKey.getNameOfHighestDiscoveredTech();
        result.add("knows " + countTechs() + " technologies, including " + maxTechName+ ";");
        Vector<String> improvementSummary = summarizeImprovements();
        result.addAll(improvementSummary);
        result.add("has " + storedMoney + " gold; " +
                   "has " + countTradeRoutes() + " trade routes; " +
                   "and scores " + getScore(map, referee) + ".");
        return result;
    }

    int countImprovements(int id) {
        int result = 0;
        if (cities == null) {
            return result;
        }
        for (City city : cities) {
            if (city.improvements.get(id)) {
                result = result + 1;
            }
        }
        return result;
    }

    int getPercentageOfWorldSeen(WorldMap map) {
        int numSeenCells = seenCells.cardinality();
        int numCells = map.countCells();
        return (numSeenCells * 100) / numCells;
    }

    protected ImprovementType getCheapestImprovement(City city) {
        int maxExpense = improvements.getHighestResaleValue();
        int lowestPriceSoFar = maxExpense;
        int numPossibilities = improvements.countTypes();
        ImprovementType result = null;
        for (int i = 0; i < numPossibilities; i++) {
            ImprovementType improvementType = improvements.get(i);
            if (!improvementType.isWonder()) {
                if (   (improvementType.resaleValue < lowestPriceSoFar)
                        || ((improvementType.resaleValue == lowestPriceSoFar) && (improvementType.isBarracks))
                        || ((improvementType.resaleValue == lowestPriceSoFar) && (lowestPriceSoFar == maxExpense))
                        ) {
                    if (!city.improvements.key.get(i)) {
                        if (improvementType.technologyIndex < 0) {
                            lowestPriceSoFar = improvementType.resaleValue;
                            result = improvementType;
                        }
                        if (techKey.hasTech(improvementType.technologyIndex)) {
                            lowestPriceSoFar = improvementType.resaleValue;
                            result = improvementType;
                        }
                    }
                }
            }
        }
        return result;
    }

    protected UnitType getCheapestOccupier() {
        int maxCapitalCost = getHighestCapitalCost(unitTypes);
        int lowestCostSoFar = maxCapitalCost;
        UnitType result = null;
        for (UnitType unitType : unitTypes) {
            if ((unitType.capitalCost <= lowestCostSoFar) && (canAttack(unitType))) {
                if (   (techKey.hasTech(unitType.technologyIndex))
                    && (!isObsolete(unitType))
                        ) {
                    lowestCostSoFar = unitType.capitalCost;
                    result = unitType;
                }
            }
        }
        return result;
    }

    protected City getCity(int id) {
        return cities.get(id);
    }

    protected City getCityAt(int cellId) {
        if (cellId < 0) {
            return null;
        }
        if (cities == null) {
            return null;
        }
        if (cities.size() == 0) {
            return null;
        }
        for (City city : cities) {
            if (city.location == cellId) {
                return city;
            }
        }
        return null;
    }

    protected Vector<Integer> getCityLocations() {
        int numCities = countCities();
        Vector<Integer> result = new Vector<Integer>(numCities);
        for (City city : cities) {
            // ignore the none city's location.
            if (city != cities.get(0)) {
                result.add(city.location);
            }
        }
        return result;
    }

    protected int getContinentNumber(WorldMap map, int cellId) {
        Pathfinder pathfinder = new Pathfinder(map, seenCells, continents);
        return pathfinder.getContinentNumber(cellId);
    }

    protected GovernmentType getGovernmentType() {
        return governmentTypes.get(governmentTypeId);
    }

    protected int getHighestCapitalCost(Vector<UnitType> unitTypes) {
        int result = 0;
        for (UnitType unitType : unitTypes) {
            if (unitType.capitalCost > result) {
                result = unitType.capitalCost;
            }
        }
        return result;
    }

    protected int getIdOfHighestDiscoveredTech() {
        return techKey.getIdOfHighestDiscoveredTech();
    }

    protected int getIndexOfFurthestSettler(WorldMap map, City city) {
        int furthestSoFar = -1;
        int result = -1;
        for (int i = 0; i < city.units.size(); i++) {
            Unit unit = city.units.get(i);
            if (unit.unitType.isSettler) {
                int metric = map.getDistanceInCells(city.location, unit.getLocation());
                if (metric > furthestSoFar) {
                    metric = furthestSoFar;
                    result = i;
                }
            }
        }
        return result;
    }

    protected Vector<Integer> getLandNeighbors(WorldMap map, int cellId) {
        Vector<Integer> result = new Vector<>(6);
        Vector<Integer> neighbors = map.getNeighbors(cellId);
        for (int neighbor : neighbors) {
            if (map.isLand(neighbor)) {
                result.add((Integer) neighbor);
            }
        }
        return result;
    }

    protected Vector<Integer> getLandNeighborsWithUnseenNeighbors(WorldMap map, int cellId) {
        Vector<Integer> result = new Vector<>(6);
        Vector<Integer> neighbors = map.getNeighbors(cellId);
        for (int neighbor : neighbors) {
            if (map.isLand(neighbor)) {
                Vector<Integer> fringe = map.getNeighbors(neighbor);
                if (countSeenCells(fringe) < fringe.size()) {
                    result.add((Integer) neighbor);
                }
            }
        }
        return result;
    }

    protected Vector<Integer> getLocations() {
        Vector<Integer> result = getCityLocations();
        result.addAll(getUnitLocations());
        return result;
    }

    protected int getLogisticsCost(City city) {
        int result = 0;
        if (isAnarchist()) {
            return result;
        }
        if (city.units == null) {
            return result;
        }
        for (Unit unit : city.units) {
            result = result + unit.unitType.logisticsCost;
        }
        if (isDespotic()) {
            if (result < city.size) {
                result = 0;
            } else {
                result = result - city.size;
            }
        }
        return result;
    }

    protected int getLowestTradeRouteIncome(WorldMap map, City city, ClaimReferee referee) {
        int result = 0;
        if (city.tradePartnerLocations == null) {
            return result;
        }
        if (city.tradePartnerLocations.size() == 0) {
            return result;
        }
        for (int location : city.tradePartnerLocations) {
            int income = getTradeRouteIncome(map, city, location, referee);
            if (income > result) {
                result = income;
            }
        }
        return result;
    }

    protected int getLuxuryFactor(WorldMap map, City city, ClaimReferee referee) {
        return 100 + countTradeBonus(map, city, referee);
    }

    protected int getLuxuryPercentage() {
        int result = 100 - taxPercentage - sciencePercentage;
        if (result < 0) {
            fixSciencePercentage();
            result = 100 - taxPercentage - sciencePercentage;
        }
        if (result > 100) {
            fixSciencePercentage();
            result = 100 - taxPercentage - sciencePercentage;
        }
        return result;
    }

    protected int getMaximumMilitaryMobility(City city) {
        int result = 0;
        for (Unit unit : city.units) {
            if ((canAttack(unit)) && (unit.unitType.mobility > result)) {
                result = unit.unitType.mobility;
            }
        }
        return result;
    }

    protected String getName() {
        return name;
    }

    protected int getPriceOfNextTech() {
        return techKey.getPriceOfNextTech(techPriceFactor);
    }

    protected int getProductionFactor(WorldMap map, City city, ClaimReferee referee) {
        return 100 + countProductionBonus(map, city, referee);
    }

    protected int getScienceFactor(WorldMap map, City city, ClaimReferee referee) {
        return 100 + countScienceBonus(map, city, referee);
//        return city.getScienceFactor();
    }

    protected int getScore(WorldMap map, ClaimReferee referee) {
        int result = 0;
        result = result +  2 * countHappyCitizens(map, referee);
        result = result +      countContentCitizens(map, referee);
        result = result +  3 * longestPeaceAD;
        result = result + 20 * countWonders();
        result = result +  5 * countFutureTech();
        result = result - 10 * map.countPollution();
        result = result +      getPercentageOfWorldSeen(map);
        return result;
    }

    protected BitSet getSeenCells() {
        return (BitSet) seenCells.clone();
    }

    protected int getTaxFactor(WorldMap map, City city, ClaimReferee referee) {
        return 100 + countTradeBonus(map, city, referee);
    }

    protected int getTradeRouteBonus(WorldMap map, City city, int cellId, ClaimReferee referee) {
        if (!map.hasCity(cellId)) {
            return 0;
        }
        int distance = map.getDistanceInCells(city.location, cellId);
        if (distance < 8) {
            return 0;
        }
        int cityRawTrade = countRawTrade(map, city);
        int partnerRawTrade = referee.countRawTrade(cellId);
        int numerator = (distance + 10) * (cityRawTrade + partnerRawTrade);
        int denominator = 24;
        if (getContinentNumber(map, city.location) == getContinentNumber(map, cellId)) {
            denominator = denominator * 2;
        }
        if (hasCityAt(cellId)) {
            denominator = denominator * 2;
        }
        if (techKey.hasTech(59)) { // Railroad
            denominator = denominator * 3 / 2;
        }
        if (techKey.hasTech(76)) { // Flight
            denominator = denominator * 3 / 2;
        }
        if (numerator < denominator) {
            return 1;
        }
        return numerator / denominator;
    }

    protected int getTradeRouteIncome(WorldMap map, City city, int cellId, ClaimReferee referee) {
        if (!map.hasCity(cellId)) {
            return 0;
        }
        if (map.getDistanceInCells(city.location, cellId) < 8) {
            return 0;
        }
        int cityRawTrade = countRawTrade(map, city);
        int partnerRawTrade = referee.countRawTrade(cellId);
        if (hasCityAt(cellId)) {
            return (cityRawTrade + partnerRawTrade + 4) / 16;
        }
        return (cityRawTrade + partnerRawTrade + 4) / 8;
    }

    protected int getUnhappinessOfEachRemoteMilitaryUnit(WorldMap map, City city, ClaimReferee referee) {
        int result = 0;
        if (!isMilitarist()) {
            return result;
        }
        if (governmentTypes.get(governmentTypeId).name.equals("Republic")) {
            result = 1;
        }
        if (governmentTypes.get(governmentTypeId).name.equals("Democracy")) {
            result = 2;
        }
//        if (isTimocratic()) {
//            result = result - 1;
//        }
        result = result - countUnhappyReductionPerMissedExplorer(map, city, referee);
        if (result < 0) {
            return result;
        }
        return result;
    }

    protected Vector<Integer> getUnitLocations() {
        int numUnits = countUnits();
        Vector<Integer> result = new Vector<>(numUnits);
        for (City city : cities) {
            result.addAll(city.getUnitLocations());
        }
        return result;
    }

    protected int getWonderLocation(int id) {
        int result = -1;
        if (!hasWonder(id)) {
            return result;
        }
        for (City city : cities) {
            if ((!city.isNone()) && (!city.isDestroyed)) {
                if (city.improvements.get(id)) {
                    return city.location;
                }
            }
        }
        return result;
    }

    protected int getWorstTradePartner(WorldMap map, City city, ClaimReferee referee) {
        int result = -1;
        int lowestIncome = 0;
        if (city.tradePartnerLocations == null) {
            return result;
        }
        if (city.tradePartnerLocations.size() == 0) {
            return result;
        }
        for (int location : city.tradePartnerLocations) {
            int income = getTradeRouteIncome(map, city, location, referee);
            if (income > result) {
                result = income;
            }
        }
        return result;

    }

    protected int governmentBonus() {
        return governmentTypes.get(governmentTypeId).bonus();
    }

    protected boolean hadWonder(int id) {
        if (destroyedCities == null) {
            return false;
        }
        if (destroyedCities.size() == 0) {
            return false;
        }
        for (City city : destroyedCities) {
            if (city.improvements.get(id)) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasCityAt(int cellId) {
        if (cellId < 0) {
            return false;
        }
        if (cities == null) {
            return false;
        }
        if (cities.size() == 0) {
            return false;
        }
        for (City city : cities) {
            if (city.location == cellId) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasWonder(int id) {
        if (cities == null) {
            return false;
        }
        if (cities.size() == 0) {
            return false;
        }
        for (City city : cities) {
            if ((!city.isNone()) && (!city.isDestroyed)) {
                if (city.improvements.get(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isAffectedByMysticism(ClaimReferee referee) {
        if (!techKey.hasTech(22)) {
            return false;
        }
        if (referee.isObsolete(24)) { // Oracle is made obsolete by Religion.
            return false;
        }
        return true;
    }

    protected boolean isAnarchist() {
        if (governmentTypes.get(governmentTypeId).name.equals("Anarchy")) {
            return true;
        }
        return false;
    }

    protected boolean isAvailable(ImprovementType improvementType, City city, ClaimReferee referee) {
        int id = improvementType.id;
        if (id < 0) {
            return false;
        }
        if (improvements.get(id).isWonder()) {
            return referee.isAvailable(id);
        }
        return !city.improvements.get(id);
    }

    boolean isCityRioting(WorldMap map, City city, ClaimReferee referee) {
        if (city.location < 0) {
            return false;
        }
        int happy = countHappyCitizens(map, city, referee);
        int unhappy = countUnhappyCitizens(map, city, referee);
        if (unhappy > happy) {
            return true;
        }
        return false;
    }

    protected boolean isDespotic() {
        if (governmentTypes.get(governmentTypeId).name.equals("Despotism")) {
            return true;
        }
        return false;
    }

    protected boolean isElectrified(WorldMap map, City city, ClaimReferee referee) {
        if (city.hasElectrifiedImprovement()) {
            return true;
        }
        if (!referee.hasNonobsoleteElectrifiedWonder()) {
            return false;
        }
        for (City possibleCity : cities) {
            int numImprovements = possibleCity.improvements.key.cardinality();
            int improvementId = -1;
            for (int i = 0; i < numImprovements; i++) {
                improvementId = possibleCity.improvements.key.nextSetBit(improvementId + 1);
                if (improvementId < 0) {
                    break;
                }
                ImprovementType improvement = possibleCity.improvements.types.get(improvementId);
                if ((improvement.isWonder()) && (improvement.isElectrified)) {
                    if (!referee.isObsolete(improvementId)) {
                        if (possibleCity == city) {
                            return true;
                        }
                        if (improvement.affectsAllContinents) {
                            return true;
                        }
                        if (improvement.affectsContinent) {
                            if (areOnSameContinent(map, city, possibleCity)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    protected boolean isFarmOf(Civilization civ, int cellId) {
        if (civ == null) {
            return false;
        }
        if (civ.cities == null) {
            return false;
        }
        for (City city : civ.cities) {
            if (city.farms != null) {
                for (int farmId : city.farms) {
                    if (farmId == cellId) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean isMilitarist() {
        if (   (governmentTypes.get(governmentTypeId).name.equals("Republic"))
                || (governmentTypes.get(governmentTypeId).name.equals("Democracy"))
                ) {
            return false;
        }
        return true;
    }

    protected boolean isObsoleteUnit(ProductType productType) {
        if (productType.isUnitType) {
            return isObsolete(productType.unitType);
        }
        return false;
    }

    protected boolean isObsolete(UnitType unitType) {
        if (unitType.obsolescerTechnologyIndex < 0) {
            return false;
        }
        if (techKey.hasTech(unitType.obsolescerTechnologyIndex)) {
            return true;
        }
        return false;
    }

//    protected boolean isTimocratic() {
//        return hasWonder(34);
//    }

    protected boolean isUnavailableImprovement(ProductType productType, City city, ClaimReferee referee) {
        if (productType.isUnitType) {
            return false;
        }
        return !isAvailable(productType.improvementType, city, referee);
    }

    protected Vector<Integer> landCells(WorldMap map, Vector<Integer> cells) {
        Vector<Integer> result = new Vector<>(cells.size());
        for (Integer cell : cells) {
            if (map.isLand(cell)) {
                result.add(cell);
            }
        }
        return result;
    }

    protected void moveTowardGovernmentType(int id, GameListener listener, ClaimReferee referee) {
        anarchyTurnCounter = anarchyTurnCounter + 1;
        if (anarchyTurnCounter == 1) {
            listener.bemoanRevolution(this);
        }
        if (anarchyTurnCounter < 1 + getAnarchyLength(referee)) {
            governmentTypeId = 0; // Anarchy
            return;
        }
        for (int i = id; i >= 0; i--) {
            if (canBe(governmentTypes.get(i), referee)) {
                governmentTypeId = i;
                if (i > 0) {
                    anarchyTurnCounter = 0;
                    listener.celebrateRevolution(this, governmentTypes.get(i));
                    return;
                }
            }
        }
        governmentTypeId = 0;
    }

    protected static String proposeCivilizationName(int arg) {
        if (arg == 0) { return "Amber"; }
        if (arg == 1) { return "Bonny"; }
        if (arg == 2) { return "Columbia"; }
        if (arg == 3) { return "Durango"; }
        if (arg == 4) { return "England"; }
        if (arg == 5) { return "France"; }
        if (arg == 6) { return "Germany"; }
        return "Russia";
    }

    protected static String proposeRulerName(int arg) {
        if (arg == 0) { return "Amber"; }
        if (arg == 1) { return "Bonny"; }
        if (arg == 2) { return "Colman"; }
        if (arg == 3) { return "Doug"; }
        if (arg == 4) { return "Emma"; }
        if (arg == 5) { return "Frank"; }
        if (arg == 6) { return "Geraldo"; }
        return "Raisa";
    }

    protected void recordPeace() {
        if (atPeace()) {
            currentPeaceAD = currentPeaceAD + 1;
        } else {
            currentPeaceAD = 0;
        }
        if (currentPeaceAD > longestPeaceAD) {
            longestPeaceAD = currentPeaceAD;
        }
    }

    protected void recordStats() {
        if (turnCounter > 0) {
            statSheet.incrementTurn();
        }
        statSheet.recordPercentages(getLuxuryPercentage(), sciencePercentage, taxPercentage);
        statSheet.recordTechs(countTechs(), getIdOfHighestDiscoveredTech());
        statSheet.recordGovernmentType(governmentTypeId);
        statSheet.recordCities(countCities(), countCitizens(), countMyriads());
        statSheet.recordImprovements(countImprovements(), improvements);
        statSheet.recordTradeRoutes(countTradeRoutes());
        statSheet.recordUnits(countCaravans(), countAerialUnits(), countNavalUnits(), countTroops(), countSettlers());
        statSheet.recordWip(storedMoney, countStoredProduction(), storedScience);
        statSheet.recordCells(countSeenCells());
        statSheet.recordThinkingTime((int) recentTurnLengthInMilliseconds);
    }

    protected void requestUnit(City city, int unitTypeId) {
        UnitType unitType = unitTypes.get(unitTypeId);
        if ((unitType.technologyIndex < 0) || (techKey.hasTech(unitType.technologyIndex))) {
            if ((unitType.obsolescerTechnologyIndex < 0) || (!techKey.hasTech(unitType.obsolescerTechnologyIndex))) {
                city.wip = new ProductType(unitType);
            }
        }
    }

    protected void requestWonder(City city, int wonderId, ClaimReferee referee) {
        if (!referee.isAvailable(wonderId)) {
            return;
        }
        if (techKey.hasTech(improvements.get(wonderId).technologyIndex)) {
            city.wip = new ProductType(improvements.get(wonderId));
        }
    }

    protected boolean isNonObsoleteWonder(int wonderId, ClaimReferee referee) {

        if (   (improvements.get(wonderId).isWonder())
            && (!referee.isObsolete(wonderId))
            && (referee.isAvailable(wonderId))
            && (techKey.hasTech(improvements.get(wonderId).technologyIndex))
           ) {
            return true;
        }

        return false;
    }

    protected void seeNeighborsOf(WorldMap map, int cellId) {
        int cont = getContinentNumber(map, cellId);
        if ((cont < 0) && (map.isLand(cellId))) {
            addContinent(map, cellId);
            cont = continents.size() - 1;
        }
        Vector<Integer> neighbors = map.getNeighbors(cellId);
        for (int neighbor : neighbors) {
            seenCells.set(neighbor);
            if (map.isLand(neighbor)) {
                int neighborCont = getContinentNumber(map, neighbor);
                if ((cont >= 0) && (cont == neighborCont)) {
                } else if ((cont >= 0) && (neighborCont < 0)) {
                    continents.get(cont).set(neighbor);
                } else if (cont >= 0) {
                    if (cont < neighborCont) {
                        continents.get(cont).or(continents.get(neighborCont));
                        continents.remove(neighborCont);
                    } else {
                        continents.get(neighborCont).or(continents.get(cont));
                        continents.remove(cont);
                        cont = neighborCont;
                    }
                } else {
                    // !map.isLand(cellId) but map.isLand(neighbor); cont == -1.
                    Vector<Integer> fringe = map.getNeighbors(neighbor);
                    for (int fringeCell : fringe) {
                        if (map.isLand(fringeCell)) {
                            int fringeCont = getContinentNumber(map, fringeCell);
                            if ((neighborCont >= 0) && (neighborCont == fringeCont)) {
                            } else if ((neighborCont < 0) && (fringeCont >= 0)) {
                                continents.get(fringeCont).set(neighbor);
                                neighborCont = fringeCont;
                            } else if (neighborCont < 0) {
                                addContinent(map, neighbor);
                                neighborCont = continents.size() - 1;
                            } else if ((neighborCont >= 0) && (fringeCont < 0)) {
                            } else {
                                if (neighborCont < fringeCont) {
                                    continents.get(neighborCont).or(continents.get(fringeCont));
                                    continents.remove(fringeCont);
                                } else {
                                    continents.get(fringeCont).or(continents.get(neighborCont));
                                    continents.remove(neighborCont);
                                    neighborCont = fringeCont;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setRulerName(String name) {
        ruler = name;
    }

    protected void setColor(Color color) {
        this.color = color;
    }

    protected Color getColor() {
        return color;
    }

    protected void setTechPriceFactor(int techPriceFactor) {
        this.techPriceFactor = techPriceFactor;
    }

    Vector<String> summarizeImprovements() {
        Vector<String> result = new Vector<>();
        String currentLine = "";
        int numTypes = improvements.countTypes();
        boolean anyResultsYet = false;
        for (int id = 0; id < numTypes; id++) {
            int num = countImprovements(id);
            String currentItem = "";
            if ((!anyResultsYet) && (num > 0)) {
                currentLine = "has ";
                anyResultsYet = true;
            }
            if (num == 1) {
                currentItem = improvements.get(id).name;
            } else if (num > 1) {
                currentItem = num + " " + improvements.get(id).plural;
            }
            if (currentLine.length() + currentItem.length() <= 47) {
                if ((currentLine.length() > 0) && (currentItem.length() > 0)) {
                    if (currentLine.equals("has ")) {
                        currentLine = currentLine + currentItem;
                    } else {
                        currentLine = currentLine + ", " + currentItem;
                    }
                } else if (currentLine.length() == 0) {
                    currentLine = currentItem;
                }
            } else if ((currentLine.length() > 0) && (currentItem.length() > 0)) {
                if (currentLine.equals("has ")) {
                    currentLine = currentLine + currentItem;
                } else {
                    result.add(currentLine + ",");
                    currentLine = currentItem;
                }
            } else if (currentLine.length() == 0) {
                currentLine = currentItem;
            }
        }
        if (currentLine.length() > 0) {
            result.add(currentLine);
        }
        if (anyResultsYet) {
            result.set(result.size() - 1, result.get(result.size() - 1) + ";");
        }
        return result;
    }

    protected String summarizeTechChoices() {
        return techKey.summarizeAllChoices();
    }

    protected void tellStories(GameListener listener) {
        if (techKey.nextTech >= 0) {
            if (!hasToldStory) {
                hasToldStory = true;
                listener.celebrateTechnology(this, techKey);
            }
        }
    }

    // Assumes the government types are sorted.
    protected void tryToChangeGovernmentType(GameListener listener, ClaimReferee referee) {
        for (int i = governmentTypes.size(); i > governmentTypeId + 1; i--) {
            if (canBe(governmentTypes.get(i - 1), referee)) {
                moveTowardGovernmentType(i - 1, listener, referee);
            }
        }
    }

}