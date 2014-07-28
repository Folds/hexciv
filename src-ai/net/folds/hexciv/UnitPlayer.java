package net.folds.hexciv;

import java.util.Random;
import java.util.Vector;

/**
 * Created by jasper on Jul 27, 2014.
 */
public class UnitPlayer {

    private WorldMap map;
    private Civilization civ;
    private City city;
    private Unit unit;
    private CivPlayer ruler;
    private GameListener listener;
    private ClaimReferee referee;

    protected UnitPlayer(WorldMap map, City city, Unit unit, CivPlayer ruler, GameListener listener, ClaimReferee referee) {
        this.map = map;
        this.city = city;
        this.civ = city.civ;
        this.unit = unit;
        this.ruler = ruler;
        this.listener = listener;
        this.referee = referee;
    }

    protected boolean playTurn() {
        return playTurn(true);
    }

    // returns false if the unit should be deleted.
    protected boolean playTurn(boolean keepTroops) {
        boolean result = true;
        if (unit.unitType.isSettler) {
            result = playTurnAsSettler(keepTroops);
        } else if (unit.unitType.isCaravan) {
            result = playTurnAsCaravan(keepTroops);
        } else if (unit.unitType.isTerrestrial) {
            result = playTurnAsExplorer(keepTroops);
        }
        return result;
    }

    protected boolean playTurnAsCaravan (boolean keepTroops) {
        boolean result = true;
        Vector<Integer> neighbors = map.getNeighbors(unit.getLocation());
        for (int neighbor : neighbors) {
            if (map.hasCity(neighbor)) {
                if (civ.hasCityAt(neighbor)) {
                    City neighborCity = civ.getCityAt(neighbor);
                    if (civ.canBuildWonder(referee)) {
                        if (ruler.isCityOnVergeOfImprovement(map, neighborCity, referee)) {
                            if (neighborCity.wip.getCapitalCost() > neighborCity.storedProduction + unit.unitType.capitalCost) {
                                if (!neighborCity.wip.isUnitType && neighborCity.wip.improvementType.isWonder()) {
                                    neighborCity.storedProduction = neighborCity.storedProduction + unit.unitType.capitalCost;
                                    return false;
                                }
                            } else {
                                ProductType oldType = neighborCity.wip;
                                ImprovementType tempWonder = civ.availableWonderThatCostsAtLeast(oldType.getCapitalCost(), referee);
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
                    CityPlayer governor = ruler.getGovernor(city);
                    if (governor.isGoodTradeRoute(neighbor)) {
                        int bonus = civ.getTradeRouteBonus(map, city, neighbor, referee);
                        civ.storedMoney = civ.storedMoney + bonus;
                        city.tradePartnerLocations.add((Integer) neighbor);
                        return false;
                    }
                }
                if (city.tradePartnerLocations.size() > 3) {
                    int worstTradePartner = civ.getWorstTradePartner(map, city, referee);
                    city.tradePartnerLocations.remove((Integer) worstTradePartner);
                }
                if (civ.hasCityAt(neighbor)) {
                    City neighborCity = civ.getCityAt(neighbor);
                    if (   (!(neighborCity == null))
                            && (!(neighborCity.wip == null))
                            && (!neighborCity.wip.isUnitType)
                            && (neighborCity.wip.improvementType.isWonder())
                            ) {
                        return true;
                    }
                }
            }
            playTurnAsExplorer(keepTroops);
        }
        return result;
    }

    protected boolean playTurnAsExplorer(boolean keepTroops) {
        boolean result = true;
        int roadRemaining = 3 * unit.unitType.mobility;
        while (roadRemaining > 0) {
            boolean onRoad = (map.hasRoad(unit.cellId) || map.hasCity(unit.cellId));
            if (unit.getLocation() == city.location) {
                if (!civ.cellsExploredByLand.get(unit.getLocation())) {
                    Vector<Integer> region = map.getRegion(city.location, 2);
                    int numCellsInRegion = region.size();
                    int numSeenCellsInRegion = civ.countSeenCells(region);
                    if (numSeenCellsInRegion < numCellsInRegion) {
                        Vector<Integer> landNeighborsWithUnseenNeighbors = civ.getLandNeighborsWithUnseenNeighbors(map, city.location);
                        if (landNeighborsWithUnseenNeighbors.size() > 0) {
                            Random random = new Random();
                            int numChoices = landNeighborsWithUnseenNeighbors.size();
                            unit.cellId = landNeighborsWithUnseenNeighbors.get(random.nextInt(numChoices));
                            civ.seeNeighborsOf(map, unit.cellId);
                        }
                    } else {
                        civ.cellsExploredByLand.set(unit.getLocation());
                        if ((!civ.canAttack(unit)) || (civ.countHappyCitizens(map, city, referee) > civ.countUnhappyCitizens(map, city, referee))) {
                            if (civ.countMilitaryUnitsIn(city) > 1) {
                                if (unit.unitType.mobility == civ.getMaximumMilitaryMobility(city)) {
                                    Random random = new Random();
                                    Vector<Integer> neighbors = civ.getLandNeighbors(map, unit.getLocation());
                                    int numChoices = neighbors.size();
                                    unit.cellId = neighbors.get(random.nextInt(numChoices));
                                    civ.seeNeighborsOf(map, unit.cellId);
                                }
                            }
                        } else {
                            return keepTroops;
                        }
                    }
                } else {
                    if ((!civ.canAttack(unit)) || (civ.countHappyCitizens(map, city, referee) > civ.countUnhappyCitizens(map, city, referee))) {
                        if (civ.countMilitaryUnitsIn(city) > 1) {
                            if (unit.unitType.mobility == civ.getMaximumMilitaryMobility(city)) {
                                Random random = new Random();
                                Vector<Integer> neighbors = civ.getLandNeighbors(map, unit.getLocation());
                                int numChoices = neighbors.size();
                                unit.cellId = neighbors.get(random.nextInt(numChoices));
                                civ.seeNeighborsOf(map, unit.cellId);
                            }
                        }
                    } else {
                        return keepTroops;
                    }
                }
            } else  {
                if (!civ.cellsExploredByLand.get(unit.getLocation())) {
                    Vector<Integer> region = map.getRegion(unit.getLocation(), 2);
                    int numCellsInRegion = region.size();
                    int numSeenCellsInRegion = civ.countSeenCells(region);
                    if (numSeenCellsInRegion < numCellsInRegion) {
                        boolean hasPriorityUnseenCell = false;
                        int mostUnseen = 0;
                        Vector<Integer> landNeighborsWithUnseenNeighbors = new Vector<>(6);
                        Vector<Integer> neighbors = map.getNeighbors(unit.getLocation());
                        Vector<Integer> candidates = new Vector<>(6);
                        for (int neighbor : neighbors) {
                            if (map.isLand(neighbor)) {
                                Vector<Integer> fringe = map.getNeighbors(neighbor);
                                boolean isPriorityFringe = civ.planner.hasPriorityUnseenCell(fringe, civ.seenCells);
                                if ((!hasPriorityUnseenCell) && (isPriorityFringe)) {
                                    mostUnseen = 0;
                                }
                                int seenCells = civ.countSeenCells(fringe);
                                if (seenCells < fringe.size()) {
                                    if (((isPriorityFringe) || (!hasPriorityUnseenCell))
                                            && (fringe.size() - seenCells > mostUnseen)
                                            ) {
                                        mostUnseen = fringe.size() - seenCells;
                                        hasPriorityUnseenCell = isPriorityFringe;
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
                            civ.seeNeighborsOf(map, unit.cellId);
                        } else if (landNeighborsWithUnseenNeighbors.size() > 0) {
                            Random random = new Random();
                            int numChoices = landNeighborsWithUnseenNeighbors.size();
                            unit.cellId = landNeighborsWithUnseenNeighbors.get(random.nextInt(numChoices));
                            civ.seeNeighborsOf(map, unit.cellId);
                        } else {
                            civ.cellsExploredByLand.set(unit.getLocation());
                            civ.planner.cacheSite(unit.cellId, ruler);
                            Vector<Integer> landNeighbors = civ.landCells(map, map.getNeighbors(unit.cellId));
                            int numChoices = landNeighbors.size();
                            Random random = new Random();
                            int i = random.nextInt(numChoices);
                            int neighbor = landNeighbors.get(i);
                            unit.cellId = neighbor;
                            civ.seeNeighborsOf(map, unit.cellId);
                        }
                    } else {
                        civ.cellsExploredByLand.set(unit.getLocation());
                        civ.planner.cacheSite(unit.cellId, ruler);
                        Vector<Integer> landNeighbors = civ.landCells(map, map.getNeighbors(unit.cellId));
                        int numChoices = landNeighbors.size();
                        Random random = new Random();
                        int i = random.nextInt(numChoices);
                        int neighbor = landNeighbors.get(i);
                        unit.cellId = neighbor;
                        civ.seeNeighborsOf(map, unit.cellId);
                    }
                } else {
                    civ.planner.cacheSite(unit.cellId, ruler);
                    Vector<Integer> landNeighbors = civ.landCells(map, map.getNeighbors(unit.cellId));
                    int numChoices = landNeighbors.size();
                    Random random = new Random();
                    int i = random.nextInt(numChoices);
                    int neighbor = landNeighbors.get(i);
                    unit.cellId = neighbor;
                    civ.seeNeighborsOf(map, unit.cellId);
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

    protected boolean playTurnAsSettler(boolean keepTroops) {
        boolean result = true;
        if (ruler.isGoodLocationForNewCity(map, unit.getLocation())) {
            City newCity = civ.foundCity(map, unit.getLocation(), ruler, referee);
            listener.celebrateNewCity(unit, newCity);
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
                    if (civ.planner.isPotentialFarm(unit.cellId)) {
                        if (civ.isFarmOf(civ, unit.cellId)) {
                            unit.isBuildingRoad = true;
                            unit.wipTurns = 1;
                        }
                    }
                }
            }
        }
        if ((!unit.isBuildingRoad) && (!unit.isMining) && (!unit.isIrrigating)) {
            if (!map.hasIrrigation(unit.cellId)) {
                if (!map.hasCity(unit.cellId)) {
                    if (civ.planner.isPotentialFarm(unit.cellId)) {
                        if (wouldMinorIrrigationHelp(unit.cellId)) {
                            if (civ.isFarmOf(civ, unit.cellId)) {
                                unit.isIrrigating = true;
                                unit.wipTurns = 1;
                            }
                        }
                    }
                }
            }
        }
        if (unit.wipTurns == 0) {
            int roadRemaining = 3 * unit.unitType.mobility;
            while (roadRemaining > 0) {
                boolean onRoad = (map.hasRoad(unit.cellId) || map.hasCity(unit.cellId));
                Vector<Integer> neighbors = civ.getLandNeighbors(map, unit.cellId);
                Vector<Integer> potentialCities = ruler.getPotentialCities(map, neighbors);
                Vector<Integer> choices;
                if (potentialCities.size() > 0) {
                    choices = potentialCities;
                } else {
                    Vector<Integer> neighborsOfPotentialCities = ruler.getNeighborsOfPotentialCities(map, neighbors);
                    if (neighborsOfPotentialCities.size() > 0) {
                        choices = neighborsOfPotentialCities;
                    } else {
                        Vector<Integer> fringeOfPotentialCities = ruler.getFringeOfPotentialCities(map, neighbors);
                        if (fringeOfPotentialCities.size() > 0) {
                            choices = fringeOfPotentialCities;
                        } else {
                            choices = neighbors;
                        }
                    }
                }
                if (choices.size() > 0) {
                    Random random = new Random();
                    int numChoices = choices.size();
                    unit.cellId = choices.get(random.nextInt(numChoices));
                    civ.seeNeighborsOf(map, unit.cellId);
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

    protected boolean wouldMinorIrrigationHelp(int cellId) {
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
            if ((civ.isAnarchist()) || (civ.isDespotic())) {
                return false;
            }
        }
        return true;
    }


}
