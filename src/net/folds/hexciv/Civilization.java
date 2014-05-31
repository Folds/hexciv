package net.folds.hexciv;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class Civilization {
    private Vector<City> cities;
    private Vector<City> destroyedCities;
    private int precedence;
    private int governmentTypeId;
    private String name;
    private String ruler;
    private TechKey techKey;
    private Vector<Relationship> relationships;
    private Vector<GovernmentType> governmentTypes;
    private Vector<UnitType> unitTypes;
    protected ImprovementVector improvements;
    private int storedMoney;
    private int storedScience;
    private int taxPercentage;
    private int sciencePercentage;
    private int longestPeaceAD;
    private int currentPeaceAD;
    private BitSet seenCells;
    private BitSet cellsExploredByLand;
    private int techPriceFactor;
    private TechChooser techChooser;
    private boolean hasToldStory;
    private int turnCounter;
    Vector<BitSet> continents;
    private Planner planner;

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
    }

    protected void initialize(WorldMap map, Vector<Integer> foreignLocations) {
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
        int cellId = chooseStartLocation(map, foreignLocations);
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
        if ((!map.isLand(firstCellId)) || (!map.isLand(secondCellId))) {
            return false;
        }
        int firstCont = getContinentNumber(map, firstCellId);
        if (firstCont < 0) {
            return false;
        }
        int secondCont = getContinentNumber(map, secondCellId);
        if (secondCont < 0) {
            return false;
        }
        if (firstCont == secondCont) {
            return true;
        }
        return false;
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

    protected boolean canBuildWonder(ClaimReferee referee) {
        return improvements.canBuildWonder(techKey, referee);
    }

    protected boolean canSustainExplorer(WorldMap map, City city, ClaimReferee referee) {
        UnitType unitType = chooseExplorer(map, city, referee);
        if (unitType.isSettler) {
            return canSustainSettler(map, city, referee);
        }
        if (isAnarchist()) {
            return true;
        }
        int foodSurplus = countFoodSurplus(map, city);
        int productionSurplus = countProductionSurplus(map, city, referee);
        if (isDespotic()) {
            int potentialLogistics = 0;
            if (city.units != null) {
                for (Unit unit : city.units) {
                    potentialLogistics = potentialLogistics + unit.unitType.logisticsCost;
                }
            }
            if (potentialLogistics >= city.size) {
                if (unitType.logisticsCost > productionSurplus) {
                    return false;
                }
            }
            if (potentialLogistics + unitType.logisticsCost > city.size + productionSurplus) {
                return false;
            }
        } else if (isMilitarist()) {
            int logisticsCost = getLogisticsCost(city);
            if (logisticsCost + unitType.logisticsCost > productionSurplus) {
                return false;
            }
        } else {
            int logisticsCost = getLogisticsCost(city);
            int farmId = chooseWorstFarm(map, city);
            int productionValue = (countOre(map, farmId) * getProductionFactor(map, city, referee)) / 100;
            if (logisticsCost + unitType.logisticsCost + productionValue > productionSurplus) {
                return false;
            }
            if (countFood(map, farmId) + unitType.feedingCost > foodSurplus) {
                return false;
            }
        }
        return true;
    }

    protected boolean canSustainSettler(WorldMap map, City city, ClaimReferee referee) {
        if (city.size < 2) {
            return false;
        }
        int foodSurplus = countFoodSurplus(map, city);
        int farmId = chooseWorstFarm(map, city);
        if ((!isMilitarist()) && (foodSurplus < countFood(map, farmId))) {
            return false;
        }
        if (foodSurplus + 1 < countFood(map, farmId)) {
            return false;
        }
        int productionSurplus = countProductionSurplus(map, city, referee);
        if (productionSurplus * 100 < countOre(map, farmId) * getProductionFactor(map, city, referee)) {
            return false;
        }
        return true;
    }

    protected UnitType chooseExplorer(WorldMap map, City city, ClaimReferee referee) {
        int upkeep = getHighestUpkeepCostOfExplorer(map, city, referee);
        int capitalCost = 0;
        int mobility = -1;
        UnitType result = null;
        for (UnitType unitType : unitTypes) {
            if ((unitType.mobility > 0) && (techKey.hasTech(unitType.technologyIndex))) {
                int unitUpkeep = estimateUpkeepCostOfExplorer(map, city, referee, unitType);
                if (    (unitUpkeep <  upkeep)
                        || ((unitUpkeep == upkeep) && (unitType.mobility > mobility))
                        || ((unitUpkeep == upkeep) && (unitType.mobility == mobility) && (unitType.capitalCost <= capitalCost))
                        ) {
                    upkeep = 2 * unitType.feedingCost + unitType.logisticsCost;
                    mobility = unitType.mobility;
                    result = unitType;
                }
            }
        }
        return result;
    }

    protected int chooseFarm(WorldMap map, City city) {
        int result = -1;
        int cellId = city.location;
        if (city.isNone()) {
            if (city.countSettlers() == 0) {
                return result;
            }
            for (Unit unit : city.units) {
                if (unit.unitType.isSettler) {
                    cellId = unit.cellId;
                    break;
                }
            }
        }
        Vector<Integer> region = map.getRegion(cellId, 2);
        int bestMetricSoFar = -1;
        if (!city.isNone()) {
            if (city.farms == null) {
                return result;
            }
            if (city.farms.size() == 18) {
                return result;
            }
        }
        for (Integer farm : region) {
            if (   (farm != cellId)
                && ((city.isNone()) || (!city.farms.contains(farm)))
                && ((seenCells.get(farm)) || (map.getDistanceInCells(cellId, farm) == 1))
               ) {
                int metric = countOre(map, farm) + countFood(map, farm) + countMoney(map, farm);
                if (metric > bestMetricSoFar) {
                    bestMetricSoFar = metric;
                    result = farm;
                }
            }
        }
        return result;
    }

    protected int chooseFarm(WorldMap map, int cellId, Vector<Integer> unavailableLocations) {
        Referee referee = new Referee(unavailableLocations);
        return chooseFarm(map, cellId, referee);
    }

    protected int chooseFarm(WorldMap map, int cellId, ClaimReferee referee) {
        Vector<Integer> region = map.getRegion(cellId, 2);
        if (region.size() == 0) {
            return -1;
        }
        int result = -1;
        int bestMetricSoFar = -1;
        for (int possibility : region) {
            if (   (referee.isAvailable(possibility, this))
                && ((seenCells == null) || (seenCells.get(possibility)) || (map.getDistanceInCells(cellId, possibility) == 1))
               ) {
                int metric = 3 * countFood(map, possibility) + 2 * countOre(map, possibility) + countMoney(map, possibility);
                if (metric > bestMetricSoFar) {
                    result = possibility;
                    bestMetricSoFar = metric;
                }
            }
        }
        return result;
    }

    protected void chooseNextTech() {
        Vector<Integer> choices = techKey.getChoices();
        techKey.nextTech = techChooser.chooseTech(choices, techKey);
    }

    protected int chooseStartLocation(WorldMap map, Vector<Integer> foreignLocations) {
        int numCells = 30;
        int result = -1;
        int bestMetricSoFar = -1;
        int tries = 0;
        while ((tries < 4) && (bestMetricSoFar < 0)) {
            Vector<Integer> potentialLocations = getRandomCells(map, numCells);
            for (Integer cellId : potentialLocations) {
                if (isAdequateStartLocation(map, (int) cellId, foreignLocations)) {
                    int metric = getStartLocationMetric(map, cellId);
                    if (metric > bestMetricSoFar) {
                        result = cellId;
                        bestMetricSoFar = metric;
                    }
                }
            }
            tries = tries + 1;
        }
        if (result < 0) {
            for (int cellId = 0; cellId < map.countCells(); cellId++) {
                if (isAdequateStartLocation(map, cellId, foreignLocations)) {
                    result = cellId;
                    return result;
                }
            }
        }
        if (result < 0) {
            result = 0;
        }
        return result;
    }

    protected void chooseWip(WorldMap map, City city, ClaimReferee referee) {
        int happy = countHappyCitizens(map, city, referee);
        int unhappy = countUnhappyCitizens(map, city, referee);
        if ((city.wip == null) && (city.countUnits() == 0)) {
            city.wip = new ProductType(UnitType.proposeMilitia());
        }
        if (isMilitarist() && (city.storedProduction < 5)) {
            city.wip = new ProductType(UnitType.proposeMilitia());
        }
        if (wantMoreExplorers(map)) {
            if (canSustainExplorer(map, city, referee)) {
                requestExplorer(map, city, referee);
            }
        }
        if (canSustainSettler(map, city, referee)) {
            if ((isMilitarist()) && (city.countUnits() > 0) && (unhappy < happy)) {
                city.wip = new ProductType(UnitType.proposeSettler());
            }
            if ((isMilitarist()) && (city.countUnits() > 0) && (unhappy == happy)) {
                city.wip = new ProductType(UnitType.proposeSettler());
            }
            if ((isMilitarist()) && (city.countUnits() > 0) && (unhappy == happy + 1)) {
                city.wip = new ProductType(UnitType.proposeSettler());
            }
            if ((!isMilitarist()) && (city.countUnits() > 0) && (unhappy <= happy)) {
                city.wip = new ProductType(UnitType.proposeSettler());
            }
        }
        if (city.countSettlers() > 0) {
            requestUnit(city, 9); // Caravan
            requestProfitableImprovement(map, city, 12, referee); // University
            requestProfitableImprovement(map, city, 13, referee); // Bank
            requestProfitableImprovement(map, city, 11, referee); // Cathedral
            requestProfitableImprovement(map, city,  6, referee); // Library
            requestProfitableImprovement(map, city,  7, referee); // Market
            requestProfitableImprovement(map, city, 19, referee); // Manufactory
            if (isWonderCity(map, city, referee)) {
                requestProfitableWonder(city, 31, referee); // Circumnavigation
                requestProfitableWonder(city, 23, referee); // Pyramids
                requestProfitableWonder(city, 35, referee); // Timocracy
                requestProfitableWonder(city, 40, referee); // Cancer Cure
                requestProfitableWonder(city, 39, referee); // Hypertext
                requestProfitableWonder(city, 37, referee); // Great Dam
            }
            requestProfitableImprovement(map, city,  2, referee); // Temple
            requestProfitableImprovement(map, city,  3, referee); // Granary
        }
    }

    protected boolean isWonderCity(WorldMap map, City city, ClaimReferee referee) {
        if (countProductionSurplus(map, city, referee) >= 5) {
            return true;
        }
        return false;
    }

    protected int countExplorers(WorldMap map) {
        int result = 0;
        for (City city : cities) {
            if (city.units == null) {
                return 0;
            }
            for (Unit unit : city.units) {
                if (   (unit.unitType.isTerrestrial)
                    && (!unit.unitType.isSettler)
                    && (!map.hasCity(unit.getLocation()))
                   ) {
                    result = result + 1;
                }
            }
        }
        return result;
    }

    protected int chooseWorstFarm(WorldMap map, City city) {
        int result = -1;
        int worstMetricSoFar = 999;
        for (int farm : city.farms) {
            int metric = countOre(map, farm) + countFood(map, farm) + countMoney(map, farm);
            if (metric < worstMetricSoFar) {
                worstMetricSoFar = metric;
                result = farm;
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

    protected int countDistantMilitaryUnits(WorldMap map, City city) {
        int result = 0;
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

    protected int countDefenseBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 3);
    }

    protected int countHappyBonusPerCity(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 4);
    }

    protected int countProductionBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 5);
    }

    protected int countSailBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 6);
    }

    protected int countScienceBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 7);
    }

    protected int countTempleBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 9);
    }

    protected int countTradeBonus(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 10);
    }

    protected int countTradeBonusPerTradeCell(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 11);
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

    protected boolean isAffectedByMysticism(ClaimReferee referee) {
        if (!techKey.hasTech(22)) {
            return false;
        }
        if (referee.isObsolete(24)) { // Oracle is made obsolete by Religion.
            return false;
        }
        return true;
    }

    protected int countUnhappyReductionPerMissedExplorer(WorldMap map, City city, ClaimReferee referee) {
        return countBenefit(map, city, referee, 13);
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

    protected int countThousands() {
        return 10 * countMyriads();
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

    protected boolean doesRegionContainAtLeast5EasilyReachedLandCells(WorldMap map, int cellId) {
        if (!map.isLand(cellId)) {
            return false;
        }
        int numEasilyReachedCells = 1;
        Vector<Integer> neighbors = map.getNeighbors(cellId);
        for (int neighbor : neighbors) {
            if (map.isLand(neighbor)) {
                numEasilyReachedCells = numEasilyReachedCells + 1;
            }
        }
        if (numEasilyReachedCells >= 5) {
            return true;
        }
        if (numEasilyReachedCells == 1) {
            return false;
        }
        Vector<Integer> fringe = map.getNeighbors(cellId);
        for (int neighbor : neighbors) {
            if (map.isLand(neighbor)) {
                Vector<Integer> potentials = map.getNeighbors(neighbor);
                for (Integer potential : potentials) {
                    if (   ((int) potential != cellId)
                            && (map.isLand((int) potential))
                            && (!neighbors.contains(potential))
                            && (!potentials.contains(potential))
                            ) {
                        potentials.add(potential);
                        numEasilyReachedCells = numEasilyReachedCells + 1;
                    }
                }
            }
        }
        if (numEasilyReachedCells >= 5) {
            return true;
        }
        return false;
//        Vector<Integer> region = map.getRegion(cellId, 2);
//        Collections.sort(region);
//        Vector<Integer> continent = map.getContinent(cellId);
//        if (continent == null) {
//            return false;
//        }
//        int numLandCells = 1;
//        for (Integer regionCell : region) {
//            if (regionCell != cellId) {
//                if (continent.contains(regionCell)) {
//                    numLandCells = numLandCells + 1;
//                }
//            }
//        }
//        if (numLandCells < 5) {
//            return false;
//        }
//        return true;
//
    }

    protected int estimateUpkeepCostOfExplorer(WorldMap map, City city, ClaimReferee referee, UnitType unitType) {
        int result = estimateUpkeepCostOfOccupier(unitType);
        if ((canAttack(unitType)) && (!isMilitarist())) {
            int farmId = chooseWorstFarm(map, city);
            int moneyFactor = (  getLuxuryPercentage() * getLuxuryFactor(map, city, referee)
                    + sciencePercentage * getScienceFactor(map, city, referee)
                    + taxPercentage * getTaxFactor(map, city, referee)
            ) / 100;
            int farmValue = (  300 * countFood(map, farmId)
                    + 2 * countOre(map, farmId) * getProductionFactor(map, city, referee)
                    + countMoney(map, farmId) * moneyFactor
            ) / 100;
            result = result + farmValue;
        }
        return result;
    }

    protected int estimateUpkeepCostOfOccupier(UnitType unitType) {
        return 3 * unitType.feedingCost + 2 * unitType.logisticsCost;
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

    protected String foundCity(WorldMap map, int cellId, ClaimReferee referee) {
        map.foundCity(cellId);
        planner.buffer(cellId, 4);
        City city = new City(this, cellId, name + "_" + countCities());
        cities.add(city);
        int farmLocation = chooseFarm(map, cellId, referee);
        if (farmLocation >= 0) {
            if (city.farms == null) {
                city.farms = new Vector<>(20);
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
        return city.name;
    }

    protected String formatPopulation() {
        int numThousands = countThousands();
        if (numThousands == 0) {
            return "no people";
        }
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(numThousands) + ",000 people";
    }

    protected String getBrag(WorldMap map, ClaimReferee referee) {
        return name + " has " + formatPopulation() + ", and scores " + getScore(map, referee) + ".";
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
        if (!map.isLand(cellId)) {
            return -1;
        }
        int numContinents = continents.size();
        for (int i = 0; i < numContinents; i++) {
            if (continents.get(i).get(cellId)) {
                return i;
            }
        }
        return -1;
    }

    protected GovernmentType getGovernmentType() {
        return governmentTypes.get(governmentTypeId);
    }

    protected int getHighestUpkeepCostOfExplorer(WorldMap map, City city, ClaimReferee referee) {
        int result = 0;
        for (UnitType unitType : unitTypes) {
            int upkeepCost = estimateUpkeepCostOfExplorer(map, city, referee, unitType);
            if ((unitType.isTerrestrial) && (upkeepCost > result)) {
                result = upkeepCost;
            }
        }
        return result;
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

    protected Vector<Integer> getNeighborsOfPotentialCities(WorldMap map, Vector<Integer> candidates) {
        int numCandidates = candidates.size();
        Vector<Integer> result = new Vector<Integer>(numCandidates);
        Vector<Integer> rejectedNeighbors = new Vector<Integer>(6 * numCandidates);
        for (int candidate : candidates) {
            Vector<Integer> neighbors = map.getNeighbors(candidate);
            for (int neighbor : neighbors) {
                if ((seenCells.get(neighbor)) && (!rejectedNeighbors.contains((Integer) neighbor))) {
                    if (isGoodLocationForNewCity(map, neighbor)) {
                        result.add(candidate);
                        break;
                    } else {
                        rejectedNeighbors.add(neighbor);
                    }
                }
            }
        }
        return result;
    }

    protected Vector<Integer> getPotentialCities(WorldMap map, Vector<Integer> candidates) {
        Vector<Integer> result = new Vector<Integer>(candidates.size());
        for (int candidate : candidates) {
            if (isGoodLocationForNewCity(map, candidate)) {
                result.add(candidate);
            }
        }
        return result;
    }

    protected int getPriceOfNextTech() {
        return techKey.getPriceOfNextTech(techPriceFactor);
    }

    protected int getProductionFactor(WorldMap map, City city, ClaimReferee referee) {
        return 100 + countProductionBonus(map, city, referee);
    }

    protected Vector<Integer> getRandomCells(WorldMap map, int numDesiredCells) {
        int numCells = numDesiredCells;
        if (numCells > map.countCells()) {
            numCells = map.countCells();
        }
        Vector<Integer> result = new Vector<>(numCells);
        if (numCells == map.countCells()) {
            for (int i = 0; i < numCells; i++) {
                result.add(i);
            }
            return result;
        }
        int numDistinctCells = 0;
        while (numDistinctCells < numCells) {
            for (int i = numDistinctCells; i < numCells; i++) {
                result.add(map.randomCell());
            }
            Util.deduplicate(result);
            numDistinctCells = result.size();
        }
        Collections.sort(result);
        return result;
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
        return result;
    }

    protected BitSet getSeenCells() {
        return (BitSet) seenCells.clone();
    }

    protected int getStartLocationMetric(WorldMap map, int cellId) {
        int result = 0;
        Vector<Integer> region = map.getRegion(cellId, 7);
        for (int id : region) {
            result = result + countFood(map, id);
            result = result + countOre(map, id);
        }
        return result;
    }

    protected int getTargetMoney() {
        if (turnCounter > 75) {
            return 100;
        }
        if (turnCounter > 25) {
            return (3 * turnCounter - 25) / 2;
        }
        return turnCounter;
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

    protected boolean isAdequateStartLocation(WorldMap map, int cellId, Vector<Integer> foreignLocations) {
        if (map.hasCity(cellId)) {
            return false;
        }
        if (!map.isLand(cellId)) {
            return false;
        }
        int neighborId = chooseFarm(map, cellId, foreignLocations);
        int ore = countOre(map, cellId) + countOre(map, neighborId);
        if (ore < 1) {
            return false;
        }
        int food = countFood(map, cellId) + countFood(map, neighborId);
        if (food < 3) {
            return false;
        }
        for (int foreignLocation : foreignLocations) {
            if (map.getDistanceInCells(cellId, foreignLocation) < 4) {
                return false;
            }
        }
        return doesRegionContainAtLeast5EasilyReachedLandCells(map, cellId);
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

    protected boolean isCityOnVergeOfImprovement(WorldMap map, City city, ClaimReferee referee) {
        if (city.wip.isUnitType) {
            return false;
        }
        int ore = countOre(map, city, referee);
        if (city.storedProduction + ore >= city.wip.getCapitalCost()) {
            return true;
        }
        Vector<Integer> neighbors = map.getNeighbors(city.location);
        int totalCaravanValue = 0;
        for (City homeCity : cities) {
            for (Unit unit : homeCity.units) {
                if (   (unit.unitType.isCaravan)
                        && (neighbors.contains((Integer) unit.getLocation()))
                        ) {
                    totalCaravanValue = totalCaravanValue + unit.unitType.capitalCost;
                }
            }
        }
        if (city.storedProduction + ore + totalCaravanValue >= city.wip.getCapitalCost()) {
            return true;
        }
        return false;
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

    protected boolean isGoodLocationForNewCity(WorldMap map, int cellId) {
        if (planner.hasAccepted(cellId)) {
            return true;
        }
        if (planner.hasRejected(cellId)) {
            return false;
        }
        Vector<Integer> cityLocations = getCityLocations();
        return isAdequateStartLocation(map, cellId, cityLocations);
    }

    protected boolean isGoodTradeRoute(WorldMap map, City city, int cellId, ClaimReferee referee) {
        int income = getTradeRouteIncome(map, city, cellId, referee);
        int threshold = getLowestTradeRouteIncome(map, city, referee);
        if (income > threshold) {
            return true;
        }
        return false;
    }

    protected boolean isImmediatelyProfitable(WorldMap map, City city, int improvementId, ClaimReferee referee) {
        ImprovementType impType = improvements.get(improvementId);
        if ((impType.isGranary) && (countFoodSurplus(map, city) > 0)) {
            return true;
        }
        int scienceValue = (impType.scienceBonus * countScience(map, city, referee) * 100) / getScienceFactor(map, city, referee);
        int productionValue = (impType.productionBonus * countOre(map, city, referee) * 200) / getProductionFactor(map, city, referee);
        int luxuryValue = (impType.tradeBonus * countScience(map, city, referee) * 100) /  getLuxuryFactor(map, city, referee);
        int taxValue = (impType.tradeBonus * countScience(map, city, referee) * 100) /  getTaxFactor(map, city, referee);
        int incrementalValue = scienceValue + productionValue + luxuryValue + taxValue;
        if (incrementalValue > 100 * impType.upkeepCost) {
            return true;
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

    protected void pawnLowestValueImprovement(City city, GameListener listener) {
        int improvementIndex = city.getLowestValueImprovement();
        if (improvementIndex >= 0) {
            ImprovementType improvementType = city.improvements.getImprovementType(improvementIndex);
            if (improvementType != null) {
                listener.bemoanUnsupported(city, improvementType);
                city.improvements.clear(improvementIndex);
                storedMoney = storedMoney + improvementType.resaleValue;
            }
        }
    }

    // http://www.freegameempire.com/games/Civilization/manual
    protected void playTurn(WorldMap map, City city, GameListener listener, ClaimReferee referee) {
        if (city.location < 0) {
            return;
        }
        if (city.storedFood < 0) {
            if (city.countSettlers() > 0) {
                int unitIndex = getIndexOfFurthestSettler(map, city);
                listener.bemoanUnsupported(city, city.units.get(unitIndex));
                city.units.remove(unitIndex);
                city.storedFood = 0;
            } else {
                city.size = city.size - 1;
                city.storedFood = 0;
                listener.bemoanFamine(city);
                if (city.farms.size() > city.size) {
                    Integer farm = chooseWorstFarm(map, city);
                    city.farms.remove(farm);
                } else {
                    if (city.numTaxMen > 0) {
                        city.numTaxMen = city.numTaxMen - 1;
                    } else if (city.numScientists > 0) {
                        city.numScientists = city.numScientists - 1;
                    } else if (city.numEntertainers > 0) {
                        city.numEntertainers = city.numEntertainers - 1;
                    }
                }
            }
        }
        if (city.storedFood >= 10 * city.size + 10) {
            city.size = city.size + 1;
            if (city.hasGranary()) {
                city.storedFood = city.storedFood - 5 * city.size;
            } else {
                city.storedFood = city.storedFood - 10 * city.size;
            }
            int newFarm = chooseFarm(map, city);
            if (newFarm >= 0) {
                city.farms.add(newFarm);
            }
            listener.repaintMaps(city.location);
        }
        if ((city.wip != null) && (city.storedProduction >= city.wip.getCapitalCost())) {
            if ((!isObsoleteUnit(city.wip)) && (!isUnavailableImprovement(city.wip, city, referee))) {
                city.produce(listener, referee);
            }
        }
        int ore = countOre(map, city, referee);
        int science = countScience(map, city, referee);
        int tax = countTax(map, city, referee);
        int happy = countHappyCitizens(map, city, referee);
        int unhappy = countUnhappyCitizens(map, city, referee);
        if (unhappy > happy) {
            bemoanDisorder(city, listener);
            tax = 0;
            if (!isMilitarist()) {
                ore = 0;
            }
        } else {
            int logisticsCost = getLogisticsCost(city);
            while (logisticsCost > ore + city.storedProduction) {
                abandonFurthestUnitThatNeedsMaintenance(map, city, listener);
                logisticsCost = getLogisticsCost(city);
            }
            int upkeepCost = city.getUpkeepCost();
            if (upkeepCost > tax + storedMoney) {
                pawnLowestValueImprovement(city, listener);
            }
        }
        city.storedFood = city.storedFood + countFoodSurplus(map, city);
        city.storedProduction = city.storedProduction + ore;
        storedMoney = storedMoney + tax - city.getUpkeepCost();
        storedScience = storedScience + science;
        if ((storedScience > 0) && (techKey.isUndecided())) {
            chooseNextTech();
        }
        if (techKey.isNextTechComplete(storedScience, techPriceFactor)) {
            Technology tech = techKey.getNextTech();
            techKey.advance();
            listener.celebrateDiscovery(this, tech);
            referee.claimTech(tech.id);
            storedScience = storedScience - techKey.getPriceOfNextTech(techPriceFactor);
        }
        if ((storedScience > 0) && (techKey.isUndecided())) {
            chooseNextTech();
        }
        chooseWip(map, city, referee);
    }

    protected void playTurn(WorldMap map, GameListener listener, ClaimReferee referee) {
        for (City city : cities) {
            if (!city.isNone()) {
                playTurn(map, city, listener, referee);
            }
        }
        for (int c = countCities(); c > 0; c--) {
            if (cities.get(c).size == 0) {
                if (destroyedCities == null) {
                    destroyedCities = new Vector<>(1);
                }
                cities.get(c).isDestroyed = true;
                destroyedCities.add(cities.get(c));
                cities.removeElementAt(c);
            }
        }
        for (int i = 0; i < cities.size(); i++) {
            City city = cities.get(i);
            if (city != null) {
                if (city.units != null) {
                    Vector<Integer> toBeDeleted = new Vector<>();
                    for (int j = 0; j < city.units.size(); j++) {
                        Unit unit = city.units.get(j);
                        boolean keepUnit;
                        if (toBeDeleted.size() == 0) {
                            keepUnit = playTurn(map, city, unit, listener, referee);
                        } else {
                            keepUnit = playTurn(map, city, unit, listener, referee, false);
                        }
                        if (!keepUnit) {
                            toBeDeleted.add((Integer) j);
                        }
                    }
                    for (int k = toBeDeleted.size() - 1; k >= 0; k--) {
                        city.units.removeElementAt(toBeDeleted.get(k));
                    }
                }
            }
        }
        if (areAnyCitiesRioting(map, referee)) {
            if (sciencePercentage + taxPercentage > 50) {
                if (sciencePercentage >= 10) {
                    sciencePercentage = sciencePercentage - 10;

                } else {
                    taxPercentage = taxPercentage - 10;
                }
            }
        } else {
            if ((sciencePercentage <= 90) && (taxPercentage + sciencePercentage <= 90)) {
                sciencePercentage = sciencePercentage + 10;
            }
        }
         if (storedMoney > getTargetMoney()) {
            if (taxPercentage >= 10) {
                taxPercentage = taxPercentage - 10;
            }
        }
        if (storedMoney < getTargetMoney()) {
            if ((taxPercentage <= 90) && (taxPercentage + sciencePercentage <= 90)) {
                taxPercentage = taxPercentage = 10;
            }
        }
        turnCounter = turnCounter + 1;
    }

    protected boolean playTurn(WorldMap map, City city, Unit unit,
                               GameListener listener, ClaimReferee referee) {
        return playTurn(map, city, unit, listener, referee, true);
    }

    // returns false if the unit should be deleted.
    protected boolean playTurn(WorldMap map, City city, Unit unit,
                               GameListener listener, ClaimReferee referee, boolean keepTroops) {
        boolean result = true;
        if (unit.unitType.isSettler) {
            result = playTurnAsSettler(map, city, unit, listener, referee, keepTroops);
        } else if (unit.unitType.isCaravan) {
            result = playTurnAsCaravan(map, city, unit, listener, referee, keepTroops);
        } else if (unit.unitType.isTerrestrial) {
            result = playTurnAsExplorer(map, city, unit, listener, referee, keepTroops);
        }
        return result;
    }

    protected boolean playTurnAsCaravan (WorldMap map, City city, Unit unit,
                                        GameListener listener, ClaimReferee referee, boolean keepTroops) {
        boolean result = true;
        Vector<Integer> neighbors = map.getNeighbors(unit.getLocation());
        for (int neighbor : neighbors) {
            if (map.hasCity(neighbor)) {
                if (hasCityAt(neighbor)) {
                    City neighborCity = getCityAt(neighbor);
                    if (canBuildWonder(referee)) {
                        if (isCityOnVergeOfImprovement(map, neighborCity, referee)) {
                            if (neighborCity.wip.getCapitalCost() > neighborCity.storedProduction + unit.unitType.capitalCost) {
                                if (!neighborCity.wip.isUnitType && neighborCity.wip.improvementType.isWonder()) {
                                    neighborCity.storedProduction = neighborCity.storedProduction + unit.unitType.capitalCost;
                                    return false;
                                }
                            } else {
                                ProductType oldType = neighborCity.wip;
                                ImprovementType tempWonder = availableWonderThatCostsAtLeast(oldType.getCapitalCost(), referee);
                                if (tempWonder != null) {
                                    neighborCity.wip = new ProductType(tempWonder);
                                    neighborCity.storedProduction = neighborCity.storedProduction + unit.unitType.capitalCost;
                                    neighborCity.wip = oldType;
                                    return false;
                                }
                            }
                        }
                    }
                }
                if  (   (!city.tradePartnerLocations.contains((Integer) neighbor))
                     && (map.getDistanceInCells(city.location, neighbor) >= 8)) {
                    if (isGoodTradeRoute(map, city, neighbor, referee)) {
                        int bonus = getTradeRouteBonus(map, city, neighbor, referee);
                        storedMoney = storedMoney + bonus;
                        city.tradePartnerLocations.add((Integer) neighbor);
                        return false;
                    }
                }
                if (city.tradePartnerLocations.size() > 3) {
                    int worstTradePartner = getWorstTradePartner(map, city, referee);
                    city.tradePartnerLocations.remove((Integer) worstTradePartner);
                }
                if (hasCityAt(neighbor)) {
                    City neighborCity = getCityAt(neighbor);
                    if (   (!(neighborCity == null))
                        && (!(neighborCity.wip == null))
                        && (!neighborCity.wip.isUnitType)
                        && (neighborCity.wip.improvementType.isWonder())
                       ) {
                        return true;
                    }
                }
            }
            playTurnAsExplorer(map, city, unit, listener, referee, keepTroops);
        }
        return result;
    }

    protected boolean playTurnAsExplorer(WorldMap map, City city, Unit unit,
                                         GameListener listener, ClaimReferee referee, boolean keepTroops) {
        boolean result = true;
        int roadRemaining = 3 * unit.unitType.mobility;
        while (roadRemaining > 0) {
            boolean onRoad = (map.hasRoad(unit.cellId) || map.hasCity(unit.cellId));
            if (unit.getLocation() == city.location) {
                if (!cellsExploredByLand.get(unit.getLocation())) {
                    Vector<Integer> region = map.getRegion(city.location, 2);
                    int numCellsInRegion = region.size();
                    int numSeenCellsInRegion = countSeenCells(region);
                    if (numSeenCellsInRegion < numCellsInRegion) {
                        Vector<Integer> landNeighborsWithUnseenNeighbors = getLandNeighborsWithUnseenNeighbors(map, city.location);
                        if (landNeighborsWithUnseenNeighbors.size() > 0) {
                            Random random = new Random();
                            int numChoices = landNeighborsWithUnseenNeighbors.size();
                            unit.cellId = landNeighborsWithUnseenNeighbors.get(random.nextInt(numChoices));
                            seeNeighborsOf(map, unit.cellId);
                        }
                    } else {
                        cellsExploredByLand.set(unit.getLocation());
                        if ((!canAttack(unit)) || (countHappyCitizens(map, city, referee) > countUnhappyCitizens(map, city, referee))) {
                            if (countMilitaryUnitsIn(city) > 1) {
                                if (unit.unitType.mobility == getMaximumMilitaryMobility(city)) {
                                    Random random = new Random();
                                    Vector<Integer> neighbors = getLandNeighbors(map, unit.getLocation());
                                    int numChoices = neighbors.size();
                                    unit.cellId = neighbors.get(random.nextInt(numChoices));
                                    seeNeighborsOf(map, unit.cellId);
                                }
                            }
                        } else {
                            return keepTroops;
                        }
                    }
                } else {
                    if ((!canAttack(unit)) || (countHappyCitizens(map, city, referee) > countUnhappyCitizens(map, city, referee))) {
                        if (countMilitaryUnitsIn(city) > 1) {
                            if (unit.unitType.mobility == getMaximumMilitaryMobility(city)) {
                                Random random = new Random();
                                Vector<Integer> neighbors = getLandNeighbors(map, unit.getLocation());
                                int numChoices = neighbors.size();
                                unit.cellId = neighbors.get(random.nextInt(numChoices));
                                seeNeighborsOf(map, unit.cellId);
                            }
                        }
                    } else {
                        return keepTroops;
                    }
                }
            } else  {
                if (!cellsExploredByLand.get(unit.getLocation())) {
                    Vector<Integer> region = map.getRegion(unit.getLocation(), 2);
                    int numCellsInRegion = region.size();
                    int numSeenCellsInRegion = countSeenCells(region);
                    if (numSeenCellsInRegion < numCellsInRegion) {
                        int mostUnseen = 0;
                        Vector<Integer> landNeighborsWithUnseenNeighbors = new Vector<>(6);
                        Vector<Integer> neighbors = map.getNeighbors(unit.getLocation());
                        Vector<Integer> candidates = new Vector<>(6);
                        for (int neighbor : neighbors) {
                            if (map.isLand(neighbor)) {
                                Vector<Integer> fringe = map.getNeighbors(neighbor);
                                int seenCells = countSeenCells(fringe);
                                if (seenCells < fringe.size()) {
                                    if (fringe.size() - seenCells > mostUnseen) {
                                        mostUnseen = fringe.size() - seenCells;
                                        candidates.removeAllElements();
                                        candidates.add((Integer) neighbor);
                                    } else if (fringe.size() - seenCells == mostUnseen) {
                                        candidates.add((Integer) neighbor);
                                    }
                                    landNeighborsWithUnseenNeighbors.add((Integer) neighbor);
                                }
                            }
                        }
                        if (candidates.size() > 0) {
                            Random random = new Random();
                            int numChoices = candidates.size();
                            unit.cellId = candidates.get(random.nextInt(numChoices));
                            seeNeighborsOf(map, unit.cellId);
                        } else if (landNeighborsWithUnseenNeighbors.size() > 0) {
                            Random random = new Random();
                            int numChoices = landNeighborsWithUnseenNeighbors.size();
                            unit.cellId = landNeighborsWithUnseenNeighbors.get(random.nextInt(numChoices));
                            seeNeighborsOf(map, unit.cellId);
                        } else {
                            cellsExploredByLand.set(unit.getLocation());
                            planner.cacheSite(unit.cellId);
                            Vector<Integer> landNeighbors = landCells(map, map.getNeighbors(unit.cellId));
                            int numChoices = landNeighbors.size();
                            Random random = new Random();
                            int i = random.nextInt(numChoices);
                            int neighbor = landNeighbors.get(i);
                            unit.cellId = neighbor;
                            seeNeighborsOf(map, unit.cellId);
                        }
                    } else {
                        cellsExploredByLand.set(unit.getLocation());
                        planner.cacheSite(unit.cellId);
                        Vector<Integer> landNeighbors = landCells(map, map.getNeighbors(unit.cellId));
                        int numChoices = landNeighbors.size();
                        Random random = new Random();
                        int i = random.nextInt(numChoices);
                        int neighbor = landNeighbors.get(i);
                        unit.cellId = neighbor;
                        seeNeighborsOf(map, unit.cellId);
                    }
                } else {
                    planner.cacheSite(unit.cellId);
                    Vector<Integer> landNeighbors = landCells(map, map.getNeighbors(unit.cellId));
                    int numChoices = landNeighbors.size();
                    Random random = new Random();
                    int i = random.nextInt(numChoices);
                    int neighbor = landNeighbors.get(i);
                    unit.cellId = neighbor;
                    seeNeighborsOf(map, unit.cellId);
                }
            }
            if ((onRoad) && (map.hasRoad(unit.cellId))) {
                roadRemaining = roadRemaining - 1;
            } else {
                roadRemaining = roadRemaining - 3;
            }
        }
        return result;
    }

    protected boolean playTurnAsSettler(WorldMap map, City city, Unit unit,
                                        GameListener listener, ClaimReferee referee, boolean keepTroops) {
        boolean result = true;
        if (isGoodLocationForNewCity(map, unit.getLocation())) {
            String cityName = foundCity(map, unit.getLocation(), referee);
            listener.celebrateNewCity(unit, cityName);
            return false;
        }
        if (unit.wipTurns > 0) {
            unit.wipTurns = unit.wipTurns + 1;
            if (unit.isBuildingRoad) {
                if (unit.wipTurns >= 3) {
                    map.buildRoad(unit.cellId);
                    unit.wipTurns = 0;
                }
            }
            if (unit.isMining) {
                if (unit.wipTurns >= 5) {
                    map.mine(unit.cellId);
                    unit.wipTurns = 0;
                }
            }
            if (unit.isIrrigating) {
                if (unit.wipTurns >= 5) {
                    map.irrigate(unit.cellId);
                    unit.wipTurns = 0;
                }
            }
        }
        if ((!unit.isBuildingRoad) && (!unit.isMining) && (!unit.isIrrigating)) {
            if (!map.hasRoad(unit.cellId)) {
                if (!map.hasCity(unit.cellId)) {
                    if (isFarmOf(city.civ, unit.cellId)) {
                        unit.isBuildingRoad = true;
                        unit.wipTurns = 1;
                    }
                }
            }
        }
        if ((!unit.isBuildingRoad) && (!unit.isMining) && (!unit.isIrrigating)) {
            if (!map.hasIrrigation(unit.cellId)) {
                if (!map.hasCity(unit.cellId)) {
                    if (wouldMinorIrrigationHelp(map, unit.cellId)) {
                        if (isFarmOf(city.civ, unit.cellId)) {
                            unit.isIrrigating = true;
                            unit.wipTurns = 1;
                        }
                    }
                }
            }
        }
        if (unit.wipTurns == 0) {
            int roadRemaining = 3 * unit.unitType.mobility;
            while (roadRemaining > 0) {
                boolean onRoad = (map.hasRoad(unit.cellId) || map.hasCity(unit.cellId));
                Vector<Integer> neighbors = getLandNeighbors(map, unit.cellId);
                Vector<Integer> potentialCities = getPotentialCities(map, neighbors);
                Vector<Integer> choices;
                if (potentialCities.size() > 0) {
                    choices = potentialCities;
                } else {
                    Vector<Integer> neighborsOfPotentialCities = getNeighborsOfPotentialCities(map, neighbors);
                    if (neighborsOfPotentialCities.size() > 0) {
                        choices = neighborsOfPotentialCities;
                    } else {
                        choices = neighbors;
                    }
                }
                if (choices.size() > 0) {
                    Random random = new Random();
                    int numChoices = choices.size();
                    unit.cellId = choices.get(random.nextInt(numChoices));
                    seeNeighborsOf(map, unit.cellId);
                    unit.wipTurns = 0;
                    unit.isMining = false;
                    unit.isIrrigating = false;
                    unit.isBuildingRoad = false;
                }
                if ((onRoad) && (map.hasRoad(unit.cellId))) {
                    roadRemaining = roadRemaining - 1;
                } else {
                    roadRemaining = roadRemaining - 3;
                }
                if (potentialCities.size() > 0) {
                    return result;
                }
            }
        }
        return result;
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

    protected void requestExplorer(WorldMap map, City city, ClaimReferee referee) {
        UnitType unitType = chooseExplorer(map, city, referee);
        if (unitType != null) {
            city.wip = new ProductType(unitType);
        }
    }

    protected void requestImprovement(City city, int improvementId) {
        if (city.improvements.types.get(improvementId).isWonder()) {
            return; // use requestWonder() instead.
        }
        if (   (!city.improvements.get(improvementId))
                && (techKey.hasTech(improvements.get(improvementId).technologyIndex))
                ) {
            city.wip = new ProductType(improvements.get(improvementId));
        }
    }

    protected void requestProfitableImprovement(WorldMap map, City city, int improvementId, ClaimReferee referee) {
        if (isImmediatelyProfitable(map, city, improvementId, referee)) {
            requestImprovement(city, improvementId);
        }
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

    protected void requestProfitableWonder(City city, int wonderId, ClaimReferee referee) {
        if (!isNonObsoleteWonder(wonderId, referee)) {
            return;
        }
        requestWonder(city, wonderId, referee);
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

    protected void setTechPriceFactor(int techPriceFactor) {
        this.techPriceFactor = techPriceFactor;
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

    protected boolean wantMoreExplorers(WorldMap map) {
        int numExplorers = countExplorers(map);
        if (numExplorers > 5) {
            return false;
        }
        if (numExplorers > 2 * countCities()) {
            return false;
        }
        return true;
    }

    protected boolean wouldMinorIrrigationHelp(WorldMap map, int cellId) {
        TerrainTypes terrain = map.getTerrain(cellId);
        if (!terrain.isIrrigable()) {
            return false;
        }
        if (map.hasMine(cellId)) {
            return false;
        }
        if (terrain.resultOfIrrigation() != terrain) {
            return false;
        }
        if (terrain == TerrainTypes.river) {
            if ((isAnarchist()) || (isDespotic())) {
                return false;
            }
        }
        return true;
    }

}