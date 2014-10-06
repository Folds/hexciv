package net.folds.hexciv;

import java.util.BitSet;

/**
 * Created by jasper on Apr 21, 2014.
 */
public interface GameListener {
    void celebrateDiscovery(Civilization civ, Technology tech);
    void celebrateEnd();
    void celebrateNewCity(Unit unit, City cityName);
    void celebrateRevolution(Civilization civ, GovernmentType governmentType);
    void celebrateTechnology(Civilization civ, TechKey key);
    void celebrateUnsupported(City city, Unit unit);
    void celebrateUnsupported(City city, ImprovementType improvementType);
    void celebrateWonder(City city, int wonderId);
    void celebrateYear(int year, WorldMap map, ClaimReferee referee);
    void bemoanDisorder(City city);
    void bemoanFamine(City city);
    void bemoanRevolution(Civilization civ);
    void bemoanUnsupported(City city, Unit unit);
    void bemoanUnsupported(City city, ImprovementType improvementType);
    void repaintMaps(int cellId);
    void updateSeenCells(BitSet seenCells);
    void updateStats();
}
