package net.folds.hexciv;

import java.util.Calendar;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by jasper on Jul 27, 2014.
 */
public class CivPlayer {

    private WorldMap map;
    private Civilization civ;
    private GameListener listener;
    private ClaimReferee referee;

    protected CivPlayer(WorldMap map, Civilization civ, GameListener listener, ClaimReferee referee) {
        this.map = map;
        this.civ = civ;
        this.listener = listener;
        this.referee = referee;
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
                    && ((civ.seenCells.get(farm)) || (map.getDistanceInCells(cellId, farm) == 1))
                    ) {
                int metric = civ.countOre(map, farm) + civ.countFood(map, farm) + civ.countMoney(map, farm);
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
            if (   (possibility != cellId)
                    && (referee.isAvailable(possibility, civ))
                    && ((civ.seenCells == null) || (civ.seenCells.get(possibility)) || (map.getDistanceInCells(cellId, possibility) == 1))
                    ) {
                int metric = 3 * civ.countFood(map, possibility) + 2 * civ.countOre(map, possibility) + civ.countMoney(map, possibility);
                if (metric > bestMetricSoFar) {
                    result = possibility;
                    bestMetricSoFar = metric;
                }
            }
        }
        return result;
    }

    protected void chooseNextTech() {
        Vector<Integer> choices = civ.techKey.getChoices();
        civ.techKey.nextTech = civ.techChooser.chooseTech(choices, civ.techKey);
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

    protected UnitPlayer getCaptain(City city, Unit unit) {
        return new UnitPlayer(map, city, unit, this, listener, referee);
    }

    protected Vector<Integer> getFringeOfPotentialCities(WorldMap map, Vector<Integer> candidates) {
        int numCandidates = candidates.size();
        Vector<Integer> result = new Vector<Integer>(numCandidates);
        Vector<Integer> rejectedNeighbors = new Vector<Integer>(6 * numCandidates);
        for (int candidate : candidates) {
            Vector<Integer> neighbors = map.getNeighbors(candidate);
            for (int neighbor : neighbors) {
                if ((civ.planner.isPotentialFarm(neighbor)) && (!rejectedNeighbors.contains((Integer) neighbor))) {
                    if (civ.planner.isPotentialFarmOfUnbuiltCity(neighbor)) {
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

    protected CityPlayer getGovernor(City city) {
        return new CityPlayer(map, city, this, listener, referee);
    }

    protected Vector<Integer> getNeighborsOfPotentialCities(WorldMap map, Vector<Integer> candidates) {
        int numCandidates = candidates.size();
        Vector<Integer> result = new Vector<Integer>(numCandidates);
        Vector<Integer> rejectedNeighbors = new Vector<Integer>(6 * numCandidates);
        for (int candidate : candidates) {
            Vector<Integer> neighbors = map.getNeighbors(candidate);
            for (int neighbor : neighbors) {
                if ((civ.seenCells.get(neighbor)) && (!rejectedNeighbors.contains((Integer) neighbor))) {
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


    protected int getStartLocationMetric(WorldMap map, int cellId) {
        int result = 0;
        Vector<Integer> region = map.getRegion(cellId, 7);
        for (int id : region) {
            result = result + civ.countFood(map, id);
            result = result + civ.countOre(map, id);
        }
        return result;
    }

    protected int getTargetMoney() {
        if (civ.turnCounter > 540) {
            return 10;
        }
        if (civ.turnCounter > 530)
            return 250;
        if (civ.turnCounter > 75) {
            return 100;
        }
        if (civ.turnCounter > 25) {
            return (3 * civ.turnCounter - 25) / 2;
        }
        return civ.turnCounter;
    }

    protected boolean isAdequateStartLocation(WorldMap map, int cellId, Vector<Integer> foreignLocations) {
        if (map.hasCity(cellId)) {
            return false;
        }
        if (!map.isLand(cellId)) {
            return false;
        }
        int neighborId = chooseFarm(map, cellId, foreignLocations);
        int ore = civ.countOre(map, cellId) + civ.countOre(map, neighborId);
        if (ore < 1) {
            return false;
        }
        int food = civ.countFood(map, cellId) + civ.countFood(map, neighborId);
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

    protected boolean isCityOnVergeOfImprovement(WorldMap map, City city, ClaimReferee referee) {
        if (city.wip.isUnitType) {
            return false;
        }
        int ore = civ.countOre(map, city, referee);
        if (city.storedProduction + ore >= city.wip.getCapitalCost()) {
            return true;
        }
        Vector<Integer> neighbors = map.getNeighbors(city.location);
        int totalCaravanValue = 0;
        for (City homeCity : civ.cities) {
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

    protected boolean isGoodLocationForNewCity(WorldMap map, int cellId) {
        if (civ.planner.hasAccepted(cellId)) {
            return true;
        }
        if (civ.planner.hasRejected(cellId)) {
            return false;
        }
        Vector<Integer> cityLocations = civ.getCityLocations();
        return isAdequateStartLocation(map, cellId, cityLocations);
    }

    // http://www.freegameempire.com/games/Civilization/manual
    protected void playTurn(WorldMap map, GameListener listener, ClaimReferee referee) {
        long timePlayerTurnStarted = getTimeInMilliseconds();
        for (City city : civ.cities) {
            if (!city.isNone()) {
                CityPlayer governor = getGovernor(city);
                governor.playTurn();
            }
        }
        for (int c = civ.countCities(); c > 0; c--) {
            if (civ.cities.get(c).size == 0) {
                if (civ.destroyedCities == null) {
                    civ.destroyedCities = new Vector<>(1);
                }
                civ.cities.get(c).isDestroyed = true;
                civ.destroyedCities.add(civ.cities.get(c));
                civ.cities.removeElementAt(c);
            }
        }
        for (int i = 0; i < civ.cities.size(); i++) {
            City city = civ.cities.get(i);
            if (city != null) {
                if (city.units != null) {
                    Vector<Integer> toBeDeleted = new Vector<>();
                    for (int j = 0; j < city.units.size(); j++) {
                        Unit unit = city.units.get(j);
                        boolean keepUnit;
                        UnitPlayer captain = getCaptain(city, unit);
                        if (toBeDeleted.size() == 0) {
                            keepUnit = captain.playTurn();
                        } else {
                            keepUnit = captain.playTurn(false);
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
        if (civ.areAnyCitiesRioting(map, referee)) {
            if (civ.sciencePercentage + civ.taxPercentage > 50) {
                if (civ.sciencePercentage >= 10) {
                    civ.sciencePercentage = civ.sciencePercentage - 10;

                } else {
                    civ.taxPercentage = civ.taxPercentage - 10;
                }
            }
        } else {
            if ((civ.sciencePercentage <= 90) && (civ.taxPercentage + civ.sciencePercentage <= 90)) {
                civ.sciencePercentage = civ.sciencePercentage + 10;
            }
        }
        if (civ.storedMoney > getTargetMoney()) {
            if (civ.taxPercentage >= 10) {
                civ.taxPercentage = civ.taxPercentage - 10;
            }
        }
        if (civ.storedMoney < getTargetMoney()) {
            if ((civ.taxPercentage <= 90) && (civ.taxPercentage + civ.sciencePercentage <= 90)) {
                civ.taxPercentage = civ.taxPercentage + 10;
            }
        }
        if ((civ.turnCounter > 530) && (civ.turnCounter <= 540)) {
            if (civ.storedMoney < getTargetMoney()) {
                civ.taxPercentage = civ.taxPercentage + civ.sciencePercentage;
            }
            civ.sciencePercentage = 0;
        }
        if (civ.turnCounter > 540) {
            civ.taxPercentage = 0;
            civ.sciencePercentage = 0;
        }
        civ.tryToChangeGovernmentType(listener, referee);
        long timePlayerTurnEnded = getTimeInMilliseconds();
        civ.recentTurnLengthInMilliseconds = timePlayerTurnEnded - timePlayerTurnStarted;
        civ.turnCounter = civ.turnCounter + 1;
    }

    protected long getTimeInMilliseconds() {
        return Calendar.getInstance().getTimeInMillis();
    }

    protected boolean wantMoreExplorers(WorldMap map) {
        int numExplorers = civ.countExplorers(map);
        if (numExplorers >= 5) {
            return false;
        }
        if (numExplorers >= 2 * civ.countCities()) {
            return false;
        }
        return true;
    }

}
