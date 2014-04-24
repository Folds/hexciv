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
    }
}
