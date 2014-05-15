package net.folds.hexciv;

import java.util.BitSet;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class GameState implements ClaimReferee {
    GameListener parent;
    Vector<Civilization> civs;
    Vector<UnitType> unitTypes;
    Vector<GovernmentType> governmentTypes;
    ImprovementVector improvementTypes;
    TechTree techTree;
    WorldMap map;
    int turn;
    boolean isTurnInProgress;

    GameState(GameListener parent, WorldMap map, int numCivilizations) {
        this.parent = parent;
        this.map = map;
        civs = new Vector<>(numCivilizations);
        unitTypes = UnitType.getChoices();
        governmentTypes = GovernmentType.getChoices();
        techTree = TechTree.proposeTechs();
        improvementTypes = ImprovementVector.proposeImprovements();
        for (int i = 0; i < numCivilizations; i++) {
            Civilization civ = new Civilization(governmentTypes, unitTypes, techTree, improvementTypes);
            civ.setName(Civilization.proposeCivilizationName(i));
            civ.setRulerName(Civilization.proposeRulerName(i));
            civs.add(civ);
        }
        initialize();
    }

    protected void initialize() {
        turn = 0;
        isTurnInProgress = false;
        for (int cellId = 0; cellId < map.countCells(); cellId++) {
            TerrainTypes terrain = map.getTerrain(cellId);
            if (randomBonus(terrain)) {
                map.setBonus(cellId);
            }
        }
        int numCivilizations = civs.size();
        Vector<Integer> foreignLocations = new Vector<>(numCivilizations);
        for (Civilization civ : civs) {
            civ.initialize(map, foreignLocations);
            foreignLocations.addAll(civ.getLocations());
            Collections.sort(foreignLocations);
            Util.deduplicate(foreignLocations);
        }
    }

    BitSet collateSeenCells() {
        BitSet result = new BitSet(map.countCells());
        for (Civilization civ : civs) {
            result.or(civ.getSeenCells());
        }
        return result;
    }

    protected int countCities() {
        int result = 0;
        for (Civilization civ : civs) {
            result = result + civ.countCities();
        }
        return result;
    }

    protected int countCivs() {
        return civs.size();
    }

    protected int countSeenCells() {
        BitSet seenCells = getSeenCells();
        return seenCells.cardinality();
    }

    protected int countUnits() {
        int result = 0;
        for (Civilization civ : civs) {
            result = result + civ.countUnits();
        }
        return result;
    }

    protected Civilization getCiv(int id) {
        return civs.get(id);
    }

    protected Vector<Integer> getDistinctLocations() {
        Vector<Integer> locations = getLocations();
        Collections.sort(locations);
        return Util.deduplicate(locations);
    }

    protected Vector<Integer> getLocations() {
        int numPossibleLocations = countCities() + countUnits();
        Vector<Integer> result = new Vector<Integer>(numPossibleLocations);
        for (Civilization civ : civs) {
            result.addAll(civ.getLocations());
        }
        return result;
    }

    protected BitSet getSeenCells() {
        BitSet result = new BitSet(map.countCells());
        for (Civilization civ : civs) {
            result.or(civ.getSeenCells());
        }
        return result;
    }

    protected int getTechPriceFactor() {
        if (turn <= 175) { return 10; }
        if (turn <= 225) { return 10 + 10 * (turn - 175) / 50; }
        if (turn <= 325) { return 20; }
        if (turn <= 375) { return 20 + 20 * (turn - 325) / 50; }
        return 40;
    }

    protected int getYear() {
        if (turn <= 0)   { return -4004; }
        if (turn == 200) { return 1; }
        if (turn <= 250) { return 20 * turn - 4000; }
        if (turn <= 300) { return 10 * turn - 1500; }
        if (turn <= 350) { return  5 * turn; }
        if (turn <= 400) { return  2 * turn + 1050; }
        return turn + 1450;
    }

    public boolean isAvailable(int cellId, Civilization civ) {
        Vector<Integer> cityLocations = civ.getCityLocations();
        if (cityLocations != null) {
            for (int location : cityLocations) {
                if (cellId == location) {
                    return false;
                }
            }
        }
        for (Civilization otherCiv : civs) {
            if (civ != otherCiv) {
                Vector<Integer> locations = civ.getLocations();
                if (locations != null) {
                    for (int location : locations) {
                        if (cellId == location) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    protected boolean isGameOver() {
        if (turn > 550) {
            return true;
        }
        return false;
    }

    protected void playTurn() {
        if (isTurnInProgress) {
            return;
        }
        isTurnInProgress = true;

        BitSet seenCells = collateSeenCells();
        parent.updateSeenCells(seenCells);

        if (turn % 50 == 0) {
            parent.celebrateYear(getYear());
        }
        int techPriceFactor = getTechPriceFactor();
        for (Civilization civ : civs) {
            civ.setTechPriceFactor(techPriceFactor);
            civ.tellStories(parent);
            civ.playTurn(map, parent, this);
        }
        if (turn >= 200) {
            for (Civilization civ : civs) {
                civ.recordPeace();
            }
        }
        turn = turn + 1;
        isTurnInProgress = false;
    }

    protected boolean randomBonus(TerrainTypes terrain) {
        if (terrain == TerrainTypes.grass) {
            return false;
        }
        double value = Math.random();
        if (value < 0.0625) {
            return true;
        }
        if (terrain == TerrainTypes.sea) {
            if (value < 0.25) {
                return true;
            }
        }
        return false;
    }

}