package net.folds.hexciv;

import javax.swing.undo.AbstractUndoableEdit;
import java.util.Vector;

/**
 * Created by jasper on Mar 01, 2014.
 */
//public abstract class AbstractEdit extends AbstractUndoableEdit {
public abstract class AbstractEdit {
    protected abstract boolean affectsMap();
    protected abstract boolean affectsPalettes();
    protected abstract Vector<Integer> getAffectedCellIds();
    public boolean isSignificant() {
        return false;
    }
    protected abstract void undo();
    protected abstract void redo();
//    protected abstract void die();
    protected String describeUndo() {
        return "Undo";
    }
    protected String describeRedo() {
        return "Redo";
    }
}
