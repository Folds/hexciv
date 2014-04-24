package net.folds.hexciv;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class Relationship {
    Civilization contactor;
    Civilization contactee;
    boolean atWar;
    boolean haveContact;
    boolean haveEmbassy;
    int turnsSinceLastTradeAttempt;
}
