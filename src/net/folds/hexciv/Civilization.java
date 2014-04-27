package net.folds.hexciv;

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
    private String ruler;
    private BitSet techs;
    private Vector<Relationship> relationships;
    private Vector<GovernmentType> governmentTypes;
    private Vector<UnitType> unitTypes;

    protected Civilization(Vector<GovernmentType> governmentTypes,
                           Vector<UnitType> unitTypes) {
        this.governmentTypes = governmentTypes;
        this.unitTypes = unitTypes;
    }

    protected void initialize(WorldMap map, Vector<Integer> foreignLocations) {
        governmentTypeId = GovernmentType.proposeId("Despotism");
        cities = new Vector<City>(1);
        City none = new City(this);
        cities.add(none);
        UnitType settlerType = UnitType.lookupUnitType(unitTypes, "Settler");
        int cellId = chooseStartLocation(map, foreignLocations);
        Unit settler = new Unit(settlerType, cellId);
        none.add(settler);
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

    protected int getStartLocationMetric(WorldMap map, int cellId) {
        int result = 0;
        Vector<Integer> region = map.getRegion(cellId, 7);
        for (int id : region) {
            result = result + countFood(map, id);
            result = result + countOre(map, id);
        }
        return result;
    }

    protected boolean isAdequateStartLocation(WorldMap map, int cellId, Vector<Integer> foreignLocations) {
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

    protected int countCities() {
        // the "None" city does not count.
        if (cities == null) {
            return -1;
        }
        return cities.size() - 1;
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

    protected String describeGovernment() {
        return getGovernmentType().name;
    }

    protected GovernmentType getGovernmentType() {
        return governmentTypes.get(governmentTypeId);
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

    protected Vector<Integer> getLocations() {
        Vector<Integer> result = getCityLocations();
        result.addAll(getUnitLocations());
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

    protected City getCity(int id) {
        return cities.get(id);
    }

    protected int chooseFirstFarm(WorldMap map, int cellId) {
        Vector<Integer> neighbors = map.getNeighbors(cellId);
        if (neighbors.size() == 0) {
            return 0;
        }
        return neighbors.get(0);
    }

    protected int countFood(WorldMap map, int cellId) {
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

    protected int governmentBonus() {
        return governmentTypes.get(governmentTypeId).bonus();
    }

    protected int countOre(WorldMap map, int cellId) {
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

    protected int countMoney(WorldMap map, int cellId) {
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
}
