package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Mar 02, 2014.
 */
public class NoEdit extends AbstractEdit {
    protected boolean affectsMap() {
        return false;
    }
    protected boolean affectsPalettes() {
        return false;
    }
    protected Vector<Integer> getAffectedCellIds() {
        Vector<Integer> result = new Vector<>(0);
        return result;
    }
    public boolean isSignificant() {
        return false;
    }
    protected void undo() {}
    protected void redo() {}
}