package net.folds.hexciv;

/**
 * Created by jasper on Apr 19, 2014.
 */
public interface Playable {
    void open();
    void save();
    void startGame();
    void unPause();
    boolean isPaused();
}
