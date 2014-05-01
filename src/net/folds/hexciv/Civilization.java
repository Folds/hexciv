package net.folds.hexciv;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Collections;
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
    private BitSet techs;
    private Vector<Relationship> relationships;
    private Vector<GovernmentType> governmentTypes;
    private Vector<UnitType> unitTypes;
    private int storedMoney;
    private int storedScience;
    private int taxPercentage;
    private int sciencePercentage;
    private int longestPeaceAD;
    private int currentPeaceAD;

    protected Civilization(Vector<GovernmentType> governmentTypes,
                           Vector<UnitType> unitTypes) {
        this.governmentTypes = governmentTypes;
        this.unitTypes = unitTypes;
        longestPeaceAD = 0;
        currentPeaceAD = 0;
        techs = new BitSet(72);
    }

    protected void initialize(WorldMap map, Vector<Integer> foreignLocations) {
        governmentTypeId = GovernmentType.proposeId("Despotism");
        storedMoney = 0;
        taxPercentage = 50;
        sciencePercentage = 50;
        cities = new Vector<City>(1);
        City none = City.proposeNone(this);
        cities.add(none);
        UnitType settlerType = UnitType.lookupUnitType(unitTypes, "Settler");
        int cellId = chooseStartLocation(map, foreignLocations);
        Unit settler = new Unit(settlerType, cellId);
        none.add(settler);
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

    protected void bemoanDisorder(City city, GameListener listener) {
        listener.bemoanDisorder(city);
    }

    protected boolean canAttack(Unit unit) {
        if ((unit.unitType.attackStrength > 0) && (unit.unitType.isTerrestrial)) {
            return true;
        }
        return false;
    }

    protected int chooseFarm(WorldMap map, City city) {
        int result = -1;
        Vector<Integer> region = map.getRegion(city.location, 2);
        int bestMetricSoFar = -1;
        if (city.farms.size() == 18) {
            return result;
        }
        for (Integer farm : region) {
            if ((farm != city.location) && (!city.farms.contains(farm))) {
                int metric = countOre(map, farm) + countFood(map, farm) + countMoney(map, farm);
                if (metric > bestMetricSoFar) {
                    bestMetricSoFar = metric;
                    result = farm;
                }
            }
        }
        return result;
    }

    protected int chooseFirstFarm(WorldMap map, int cellId) {
        Vector<Integer> neighbors = map.getNeighbors(cellId);
        if (neighbors.size() == 0) {
            return 0;
        }
        return neighbors.get(0);
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
                    return result;
                }
            }
        }
        if (result < 0) {
            result = 0;
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

    protected int countFutureTech() {
        int result = 0;
        result = result + techs.cardinality(); // number of bits that are set.
        result = result - 72;
        if (result < 0) {
            result = 0;
        }
        return result;
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

    protected int countMilitaryUnitsIn(City city) {
        int result = 0;
        for (City anyCity : cities) {
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

    protected int countMoney(WorldMap map, int cellId) {
        if (cellId < 0) {
            return 0;
        }
        TerrainTypes terrain = map.getTerrain(cellId);
        boolean hasBonus = map.hasBonus(cellId);
        int roadBonus = 0;
        if ((map.hasRoad(cellId)) || (map.hasRailroad(cellId))) {
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

    protected String foundCity(WorldMap map, int cellId) {
        map.foundCity(cellId);
        City city = new City(this, cellId, name + "_" + countCities());
        cities.add(city);
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
        for (Unit unit : city.units) {
            if (unit.unitType.logisticsCost > 0) {
                result = result + unit.unitType.logisticsCost;
            }
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
        return 100;
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
        return 100;
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
        return 100;
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
        int neighborId = chooseFirstFarm(map, cellId);
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
        Vector<Integer> region = map.getRegion(cellId, 2);
        Collections.sort(region);
        Vector<Integer> continent = map.getContinent(cellId);
        if (continent == null) {
            return false;
        }
        int numLandCells = 1;
        for (Integer regionCell : region) {
            if (regionCell != cellId) {
                if (continent.contains(regionCell)) {
                    numLandCells = numLandCells + 1;
                }
            }
        }
        if (numLandCells < 5) {
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

    protected boolean isDespotic() {
        if (governmentTypes.get(governmentTypeId).name.equals("Despotism")) {
            return true;
        }
        return false;
    }

    protected boolean isGoodLocationForNewCity(WorldMap map, int cellId) {
        Vector<Integer> cityLocations = getCityLocations();
        return isAdequateStartLocation(map, cellId, cityLocations);
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

    protected void pawnLowestValueImprovement(City city, GameListener listener) {
        int improvementIndex = -1;
        int value = 9999;
        for (int i = 0; i < city.improvementFlags.size(); i++) {
            if (city.improvementFlags.get(i)) {

            }
        }
        if (improvementIndex >= 0) {
            ImprovementType improvementType = ImprovementType.get(improvementIndex);
            if (improvementType != null) {
                listener.bemoanUnsupported(city, improvementType);
                city.improvementFlags.clear(improvementIndex);
                storedMoney = storedMoney + improvementType.resaleValue;
            }
        }
    }

    // http://www.freegameempire.com/games/Civilization/manual

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

    protected int countLuxuries(WorldMap map, City city) {
        int money = countMoney(map, city);
        int result = 2 * city.numEntertainers;
        result = result + (money * getLuxuryPercentage()) / 100;
        result = (result * getLuxuryFactor(city)) / 100;
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

    protected void playTurn(WorldMap map, City city, GameListener listener) {
        if (city.location < 0) {
            return;
        }
        if (city.storedFood < 0) {
            if (city.countSettlers() > 0) {
                int unitIndex = getIndexOfFurthestSettler(map, city);
                listener.bemoanUnsupported(city, city.units.get(unitIndex));
                city.units.remove(unitIndex);
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
        }
        int food = city.storedFood + countFood(map, city);
        food = food - 2 * city.size;
        food = food - city.countSettlers();
        if (!isMilitarist()) {
            food = food - city.countSettlers();
        }
        int ore = countOre(map, city);
        int luxuries = countLuxuries(map, city);
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
        city.storedFood = food;
        city.storedProduction = city.storedProduction + ore;
        storedMoney = storedMoney + tax;
    }

    protected void playTurn(WorldMap map, GameListener listener) {
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
                        boolean keepUnit = playTurn(map, unit, listener);
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
    }

    protected boolean atPeace() {
        if ((countCities() == 0) && (countUnits() == 0)) {
            return false;
        }
        return true;
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

    // returns false if the unit should be deleted.
    protected boolean playTurn(WorldMap map, Unit unit, GameListener listener) {
        boolean result = true;
        if (unit.unitType.name.equals("Settler")) {
            if (isGoodLocationForNewCity(map, unit.getLocation())) {
                String cityName = foundCity(map, unit.getLocation());
                listener.celebrateNewCity(unit, cityName);
                result = false;
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

    protected void setName(String name) {
        this.name = name;
    }

    protected void setRulerName(String name) {
        ruler = name;
    }

}