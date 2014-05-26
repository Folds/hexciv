package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Vector;

/**
 * Created by jasper on Apr 22, 2014.
 */
public class TestGameStart {

    @Test
    public void TestInitialGovernmentType() {
        MockGameListener listener = new MockGameListener();
        WorldMap map = WorldMap.getEarthMap();
        GameState gameState = new GameState(listener, map, 1);
        Assert.assertEquals(gameState.countCivs(), 1);
        Civilization civ = gameState.getCiv(0);
        Assert.assertEquals(civ.describeGovernment(), "Despotism");
    }

    @Test
    public void TestInitialUnit() {
        MockGameListener listener = new MockGameListener();
        WorldMap map = WorldMap.getEarthMap();
        GameState gameState = new GameState(listener, map, 1);
        Assert.assertEquals(gameState.countCivs(), 1);
        Civilization civ = gameState.getCiv(0);
        Assert.assertEquals(civ.countCities(), 0);
        Assert.assertEquals(civ.countUnits(), 1);
        City city = civ.getCity(0);
        Assert.assertEquals(city.countUnits(), 1);
        Assert.assertEquals(city.describeUnit(0), "Settler");
        Unit unit = city.getUnit(0);
        int cellId = unit.getLocation();
        TerrainTypes terrain = map.getTerrain(cellId);
        Assert.assertTrue(terrain.isLand());
    }

    @Test
    public void TestInitialLocation() {
        MockGameListener listener = new MockGameListener();
        WorldMap map = WorldMap.getEarthMap();
        GameState gameState = new GameState(listener, map, 1);
        int numCivs = gameState.countCivs();
        Vector<Integer> locations = gameState.getDistinctLocations();
        int numLocations = locations.size();
        Assert.assertEquals(numLocations, numCivs);
        Civilization civ = gameState.getCiv(0);
        int cellId = civ.getCity(0).getUnit(0).getLocation();
        Assert.assertTrue(cellId >= 0);
        int neighborId = civ.chooseFarm(map, civ.getCity(0));
        Assert.assertTrue(neighborId >= 0);
//        int numFood = civ.countFood(map, cellId) + civ.countFood(map, neighborId);
//        Assert.assertTrue(numFood >= 3);
//        int numOre = civ.countOre(map, cellId) + civ.countOre(map, neighborId);
//        Assert.assertTrue(numOre >= 1);
    }

    @Test
    public void TestPolaraDevelopment() {
        MockGameListener listener = new MockGameListener();
        WorldMap map = getPolaraMap();
        GameState gameState = new GameState(listener, map, 1);
        Assert.assertEquals(gameState.turn, 0);
        int numCivs = gameState.countCivs();
        Assert.assertEquals(numCivs, 1);
        Civilization civ = gameState.civs.get(0);
        Assert.assertEquals(civ.countCities(), 0);
        Assert.assertEquals(civ.getScore(map, gameState), 0);
        Assert.assertEquals(gameState.getDistinctLocations().size(), 1);
        int location = gameState.getDistinctLocations().get(0);
        Assert.assertTrue(location >= 0);
        Assert.assertTrue(location < 19); // Only the southernmost 19 cells of Polara are land.
        Assert.assertEquals(civ.countSeenCells(), 7); // So none of those 19 cells is a pentagon.
        Assert.assertEquals(gameState.countSeenCells(), 7);

        playTurns(gameState, 1);
        Assert.assertEquals(gameState.turn, 1);
        Assert.assertEquals(civ.countCities(), 1);
        Assert.assertEquals(civ.countEntertainers(), 0);
        Assert.assertEquals(civ.getScore(map, gameState), 1);
        Assert.assertEquals(civ.countStoredFood(), 0);
        Assert.assertEquals(civ.countStoredProduction(), 0);

        playTurns(gameState, 1);
        Assert.assertEquals(gameState.turn, 2);
        Assert.assertEquals(civ.countCities(), 1);
        Assert.assertEquals(civ.getScore(map, gameState), 1);
        Assert.assertEquals(civ.countStoredFood(), 2);
        Assert.assertEquals(civ.countStoredProduction(), 2);

        playTurns(gameState, 4);
        Assert.assertEquals(gameState.turn, 6);
        Assert.assertEquals(civ.countCities(), 1);
        Assert.assertEquals(civ.getScore(map, gameState), 1);
        Assert.assertEquals(civ.countStoredFood(), 10);
        Assert.assertEquals(civ.countStoredProduction(), 10);

        playTurns(gameState, 1);
        Assert.assertEquals(gameState.turn, 7);
        Assert.assertEquals(civ.countCities(), 1);
        Assert.assertEquals(civ.countUnits(), 1);
        Assert.assertEquals(civ.getScore(map, gameState), 1);
        Assert.assertEquals(civ.countStoredFood(), 12);
        Assert.assertEquals(civ.countStoredProduction(), 2);
        Assert.assertTrue(civ.countSeenCells() >= 10); // So none of those 19 cells is a pentagon.
        Assert.assertTrue(gameState.countSeenCells() >= 10);

        playTurns(gameState, 3);
        Assert.assertEquals(gameState.turn, 10);
        Assert.assertEquals(civ.countCities(), 1);
        Assert.assertEquals(civ.getScore(map, gameState), 1);
        Assert.assertEquals(civ.countStoredFood(), 18);
        Assert.assertEquals(civ.countStoredProduction(), 8);

        playTurns(gameState, 1);
        Assert.assertEquals(gameState.turn, 11);
        Assert.assertEquals(civ.countCities(), 1);
        Assert.assertEquals(civ.getScore(map, gameState), 1);
        Assert.assertEquals(civ.countStoredFood(), 20);
        Assert.assertEquals(civ.countStoredProduction(), 10);

        playTurns(gameState, 1);
        Assert.assertEquals(gameState.turn, 12);
        Assert.assertEquals(civ.countCities(), 1);
        Assert.assertEquals(civ.getScore(map, gameState), 2);
        Assert.assertEquals(civ.countStoredFood(), 2);
    }


    protected void playTurns(GameState gameState, int numTurns) {
        for (int i = 0; i < numTurns; i++) {
            if (!gameState.isGameOver()) {
                gameState.playTurn();
            }
        }
    }

    protected WorldMap getPolaraMap() {
        return Porter.importMapFromString(polaraString());
    }

    protected String polaraString() {
        return "{\n" +
             " \"polarCircumferenceInKilometers\" : 40000.0,\n" +
             " \"meshSize\" : 12,\n" +
             " \"terrainStrings\" : [ \"1)VO2\", \"2)j\", \"1)VO2\", \"1JVNJ\" ],\n" +
             " \"bonusString\" : \"2)j\"\n" +
               "}";
    }


}
