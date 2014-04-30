package net.folds.hexciv;

import net.folds.hexciv.GameListener;

/**
 * Created by jasper on Apr 22, 2014.
 */
public class MockGameListener implements GameListener {
    public void celebrateEnd() {}
    public void celebrateYear(int year) {}
    public void celebrateNewCity(Unit unit, String cityName) {}
    public void bemoanDisorder(City city) {}
    public void bemoanFamine(City city) {}
    public void bemoanUnsupported(City city, Unit unit) {}
    public void bemoanUnsupported(City city, ImprovementType improvementType) {}
}
