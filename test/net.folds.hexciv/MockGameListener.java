package net.folds.hexciv;

import net.folds.hexciv.GameListener;

import java.util.BitSet;

/**
 * Created by jasper on Apr 22, 2014.
 */
public class MockGameListener implements GameListener {
    public void celebrateDiscovery(Civilization civ, Technology tech) {}
    public void celebrateEnd() {}
    public void celebrateNewCity(Unit unit, City cityName) {}
    public void celebrateRevolution(Civilization civ, GovernmentType governmentType) {}
    public void celebrateTechnology (Civilization civ, TechKey key) {}
    public void celebrateUnsupported(City city, ImprovementType improvementType) {}
    public void celebrateWonder(City city, int wonderId) {}
    public void celebrateYear(int year, WorldMap map, ClaimReferee referee) {}
    public void bemoanDisorder(City city) {}
    public void bemoanFamine(City city) {}
    public void bemoanRevolution(Civilization civ) {}
    public void bemoanUnsupported(City city, Unit unit) {}
    public void bemoanUnsupported(City city, ImprovementType improvementType) {}
    public void repaintMaps(int cellId) {}
    public void updateSeenCells(BitSet seenCells) {}
    public void updateStats() {}
}
