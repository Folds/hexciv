package net.folds.hexciv;

import java.util.Collections;
import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class GameState {
    GameListener parent;
    Vector<Civilization> civs;
    Vector<UnitType> unitTypes;
    Vector<GovernmentType> governmentTypes;
    WorldMap map;
    int turn;
    boolean isTurnInProgress;

    GameState(GameListener parent, WorldMap map, int numCivilizations) {
        this.parent = parent;
        this.map = map;
        civs = new Vector<>(numCivilizations);
        unitTypes = UnitType.getChoices();
        governmentTypes = GovernmentType.getChoices();
        for (int i = 0; i < numCivilizations; i++) {
            Civilization civ = new Civilization(governmentTypes, unitTypes);
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

        if (turn % 50 == 0) {
            parent.celebrateYear(getYear());
        }
        for (Civilization civ : civs) {
            civ.playTurn(map, parent);
        }
        if (turn >= 200) {
            for (Civilization civ : civs) {
                civ.recordPeace();
            }
        }
        turn = turn + 1;
        isTurnInProgress = false;
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
}
