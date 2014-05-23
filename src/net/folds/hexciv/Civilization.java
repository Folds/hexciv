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
        seenCells.set(cellId);
        cellsExploredByLand = new BitSet(map.countCells());
        seeNeighborsOf(map, cellId);
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

    protected boolean atPeace() {
        if ((countCities() == 0) && (countUnits() == 0)) {
            return false;
        }
        return true;
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

    protected boolean canSustainExplorer(WorldMap map, City city) {
        UnitType unitType = chooseExplorer(map, city);
        if (unitType.isSettler) {
            return canSustainSettler(map, city);
        }
        if (isAnarchist()) {
            return true;
        }
        int foodSurplus = countFoodSurplus(map, city);
        int productionSurplus = countProductionSurplus(map, city);
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
            int productionValue = (countOre(map, farmId) * getProductionFactor(city)) / 100;
            if (logisticsCost + unitType.logisticsCost + productionValue > productionSurplus) {
                return false;
            }
            if (countFood(map, farmId) + unitType.feedingCost > foodSurplus) {
                return false;
            }
        }
        return true;
    }

    protected boolean canSustainSettler(WorldMap map, City city) {
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
        int productionSurplus = countProductionSurplus(map, city);
        if (productionSurplus * 100 < countOre(map, farmId) * getProductionFactor(city)) {
            return false;
        }
        return true;
    }

    protected UnitType chooseExplorer(WorldMap map, City city) {
        int upkeep = getHighestUpkeepCostOfExplorer(map, city);
        int capitalCost = 0;
        int mobility = -1;
        UnitType result = null;
        for (UnitType unitType : unitTypes) {
            if ((unitType.mobility > 0) && (techKey.hasTech(unitType.technologyIndex))) {
                int unitUpkeep = estimateUpkeepCostOfExplorer(map, city, unitType);
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

    protected void chooseWip(WorldMap map, City city) {
        int happy = countHappyCitizens(map, city);
        int unhappy = countUnhappyCitizens(map, city);
        if ((city.wip == null) && (city.countUnits() == 0)) {
            city.wip = new ProductType(UnitType.proposeMilitia());
        }
        if (isMilitarist() && (city.storedProduction < 5)) {
            city.wip = new ProductType(UnitType.proposeMilitia());
        }
        if (canSustainExplorer(map, city)) {
            requestExplorer(map, city);
        }
        if (canSustainSettler(map, city)) {
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
            requestProfitableImprovement(map, city, 12); // University
            requestProfitableImprovement(map, city, 13); // Bank
            requestProfitableImprovement(map, city, 7); // Market
            requestProfitableImprovement(map, city, 6); // Library
            requestProfitableImprovement(map, city, 2); // Temple
            requestProfitableImprovement(map, city, 3); // Granary
        }
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

    protected int countContentCitizens(WorldMap map) {
        int result = 0;
        for (City city : cities) {
            result = result + countContentCitizens(map, city);
        }
        return result;
    }

    protected int countContentCitizens(WorldMap map, City city) {
        if (city.location < 0) {
            return 0;
        }
        int luxuries = countLuxuries(map, city);
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
            sadness = farUnits * getUnhappinessOfEachRemoteMilitaryUnit();
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
        // To-do:  effects of improvements.
        // To-do:  effects of wonders.
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

    protected int countHappyCitizens(WorldMap map) {
        int result = 0;
        for (City city : cities) {
            result = result + countHappyCitizens(map, city);
        }
        return result;
    }

    protected int countHappyCitizens(WorldMap map, City city) {
        if (city.location < 0) {
            return 0;
        }
        int luxuries = countLuxuries(map, city);
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
            sadness = farUnits * getUnhappinessOfEachRemoteMilitaryUnit();
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
        // To-do:  effects of improvements.
        // To-do:  effects of wonders.
        return happy;
    }

    protected int countLuxuries(WorldMap map, City city) {
        int money = countMoney(map, city);
        int result = 2 * city.numEntertainers;
        result = result + (money * getLuxuryPercentage()) / 100;
        result = (result * getLuxuryFactor(city)) / 100;
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

    protected int countMoney(WorldMap map, City city) {
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

    protected int countOre(WorldMap map, City city) {
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
        result = result * getProductionFactor(city) / 100;
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

    protected int countProductionSurplus(WorldMap map, City city) {
        if (!isMilitarist()) {
            int happy = countHappyCitizens(map, city);
            int unhappy = countUnhappyCitizens(map, city);
            if (unhappy > happy) {
                return -getLogisticsCost(city);
            }
        }
        return countOre(map, city) - getLogisticsCost(city);
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

    protected int countScience(WorldMap map, City city) {
        int money = countMoney(map, city);
        int result = 2 * city.numScientists;
        result = result + (money * sciencePercentage) / 100;
        result = (result * getScienceFactor(city)) / 100;
        return result;
    }

    protected int countTax(WorldMap map, City city) {
        int money = countMoney(map, city);
        int result = 2 * city.numTaxMen;
        result = result + money - countLuxuries(map, city) - countScience(map, city);
        result = (result * getTaxFactor(city)) / 100;
        return result;
    }

    protected int countThousands() {
        return 10 * countMyriads();
    }

    protected int countUnhappyCitizens(WorldMap map, City city) {
        if (city.location < 0) {
            return 0;
        }
        int total = city.size;
        int happy = countHappyCitizens(map, city);
        int content = countContentCitizens(map, city);
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

    protected int estimateUpkeepCostOfExplorer(WorldMap map, City city, UnitType unitType) {
        int result = estimateUpkeepCostOfOccupier(unitType);
        if ((canAttack(unitType)) && (!isMilitarist())) {
            int farmId = chooseWorstFarm(map, city);
            int moneyFactor = (  getLuxuryPercentage() * getLuxuryFactor(city)
                    + sciencePercentage * getScienceFactor(city)
                    + taxPercentage * getTaxFactor(city)
            ) / 100;
            int farmValue = (  300 * countFood(map, farmId)
                    + 2 * countOre(map, farmId) * getProductionFactor(city)
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

    protected String getBrag(WorldMap map) {
        return name + " has " + formatPopulation() + ", and scores " + getScore(map) + ".";
    }

    protected City getCity(int id) {
        return cities.get(id);
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

    protected GovernmentType getGovernmentType() {
        return governmentTypes.get(governmentTypeId);
    }

    protected int getHighestUpkeepCostOfExplorer(WorldMap map, City city) {
        int result = 0;
        for (UnitType unitType : unitTypes) {
            int upkeepCost = estimateUpkeepCostOfExplorer(map, city, unitType);
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

    protected int getLuxuryFactor(City city) {
        return city.getLuxuryFactor();
    }

    protected int getProductionFactor(City city) {
        return city.getProductionFactor();
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

    protected int getScienceFactor(City city) {
        return city.getScienceFactor();
    }

    protected int getScore(WorldMap map) {
        int result = 0;
        result = result +  2 * countHappyCitizens(map);
        result = result +      countContentCitizens(map);
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

    protected int getTaxFactor(City city) {
        return city.getTaxFactor();
    }

    protected int getUnhappinessOfEachRemoteMilitaryUnit() {
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
        if (isTimocratic()) {
            result = result - 1;
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

    protected int governmentBonus() {
        return governmentTypes.get(governmentTypeId).bonus();
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

    protected boolean isDespotic() {
        if (governmentTypes.get(governmentTypeId).name.equals("Despotism")) {
            return true;
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
        Vector<Integer> cityLocations = getCityLocations();
        return isAdequateStartLocation(map, cellId, cityLocations);
    }

    protected boolean isImmediatelyProfitable(WorldMap map, City city, int improvementId) {
        ImprovementType impType = improvements.get(improvementId);
        if ((impType.isGranary) && (countFoodSurplus(map, city) > 0)) {
            return true;
        }
        int scienceValue = (impType.scienceBonus * countScience(map, city) * 100) / getScienceFactor(city);
        int productionValue = (impType.productionBonus * countOre(map, city) * 200) / getProductionFactor(city);
        int luxuryValue = (impType.tradeBonus * countScience(map, city) * 100) /  getLuxuryFactor(city);
        int taxValue = (impType.tradeBonus * countScience(map, city) * 100) /  getTaxFactor(city);
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

    protected boolean isTimocratic() {
        return false;
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

    boolean isCityRioting(WorldMap map, City city) {
        if (city.location < 0) {
            return false;
        }
        int happy = countHappyCitizens(map, city);
        int unhappy = countUnhappyCitizens(map, city);
        if (unhappy > happy) {
            return true;
        }
        return false;
    }

    // http://www.freegameempire.com/games/Civilization/manual
    protected void playTurn(WorldMap map, City city, GameListener listener) {
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
            city.produce();
        }
        int ore = countOre(map, city);
        int science = countScience(map, city);
        int tax = countTax(map, city);
        int happy = countHappyCitizens(map, city);
        int unhappy = countUnhappyCitizens(map, city);
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
            storedScience = storedScience - techKey.getPriceOfNextTech(techPriceFactor);
        }
        if ((storedScience > 0) && (techKey.isUndecided())) {
            chooseNextTech();
        }
        chooseWip(map, city);
    }

    protected void playTurn(WorldMap map, GameListener listener, ClaimReferee referee) {
        for (City city : cities) {
            if (!city.isNone()) {
                playTurn(map, city, listener);
            }
        }
        for (int c = countCities(); c > 0; c--) {
            if (cities.get(c).size == 0) {
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
        if (areAnyCitiesRioting(map)) {
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

    protected int getTargetMoney() {
        if (turnCounter > 75) {
            return 100;
        }
        if (turnCounter > 25) {
            return (3 * turnCounter - 25) / 2;
        }
        return turnCounter;
    }

    protected boolean areAnyCitiesRioting(WorldMap map) {
        for (City city : cities) {
            if (!city.isNone()) {
                if (isCityRioting(map, city)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean playTurn(WorldMap map, City city, Unit unit,
                               GameListener listener, ClaimReferee referee) {
        return playTurn(map, city, unit, listener, referee, true);
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

    protected void requestExplorer(WorldMap map, City city) {
        UnitType unitType = chooseExplorer(map, city);
        if (unitType != null) {
            city.wip = new ProductType(unitType);
        }
    }

    protected void requestImprovement(City city, int improvementId) {
        if (   (!city.improvements.get(improvementId))
                && (techKey.hasTech(improvements.get(improvementId).technologyIndex))
                ) {
            city.wip = new ProductType(improvements.get(improvementId));
        }

    }

    protected void requestProfitableImprovement(WorldMap map, City city, int improvementId) {
        if (isImmediatelyProfitable(map, city, improvementId)) {
            requestImprovement(city, improvementId);
        }
    }

    protected void seeNeighborsOf(WorldMap map, int cellId) {
        for (int neighbor : map.getNeighbors(cellId)) {
            seenCells.set(neighbor);
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

    protected int getMaximumMilitaryMobility(City city) {
        int result = 0;
        for (Unit unit : city.units) {
            if ((canAttack(unit)) && (unit.unitType.mobility > result)) {
                result = unit.unitType.mobility;
            }
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
                        if ((!canAttack(unit)) || (countHappyCitizens(map, city) > countUnhappyCitizens(map, city))) {
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
                    if ((!canAttack(unit)) || (countHappyCitizens(map, city) > countUnhappyCitizens(map, city))) {
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
                        Vector<Integer> landNeighbors = landCells(map, map.getNeighbors(unit.cellId));
                        int numChoices = landNeighbors.size();
                        Random random = new Random();
                        int i = random.nextInt(numChoices);
                        int neighbor = landNeighbors.get(i);
                        unit.cellId = neighbor;
                        seeNeighborsOf(map, unit.cellId);
                    }
                } else {
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

    // returns false if the unit should be deleted.
    protected boolean playTurn(WorldMap map, City city, Unit unit,
                               GameListener listener, ClaimReferee referee, boolean keepTroops) {
        boolean result = true;
        if (unit.unitType.isSettler) {
            result = playTurnAsSettler(map, city, unit, listener, referee, keepTroops);
        } else if (unit.unitType.isTerrestrial) {
            result = playTurnAsExplorer(map, city, unit, listener, referee, keepTroops);
        }
        return result;
    }

}