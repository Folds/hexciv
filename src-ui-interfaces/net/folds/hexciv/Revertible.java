package net.folds.hexciv;

/**
 * Created by jasper on Apr 19, 2014.
 */
public interface Revertible {
    void open();
    void save();
    void undo();
    void redo();
}
