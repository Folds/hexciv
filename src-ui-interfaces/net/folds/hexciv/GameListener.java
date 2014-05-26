package net.folds.hexciv;

import java.util.BitSet;

/**
 * Created by jasper on Apr 21, 2014.
 */
public interface GameListener {
    void celebrateDiscovery(Civilization civ, Technology tech);
    void celebrateEnd();
    void celebrateNewCity(Unit unit, String cityName);
    void celebrateTechnology(Civilization civ, TechKey key);
    void celebrateWonder(City city, int wonderId);
    void celebrateYear(int year);
    void bemoanDisorder(City city);
    void bemoanFamine(City city);
    void bemoanUnsupported(City city, Unit unit);
    void bemoanUnsupported(City city, ImprovementType improvementType);
    void repaintMaps(int cellId);
    void updateSeenCells(BitSet seenCells);
}
