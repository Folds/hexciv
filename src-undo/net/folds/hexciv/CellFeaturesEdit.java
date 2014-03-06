package net.folds.hexciv;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.util.Vector;

/**
 * Created by jasper on Mar 01, 2014.
 *
 * Modelled on:
 * http://www.processworks.de/blog/2009/08/add-undoredo-functionality-to-a-java-app/
 */
public class CellFeaturesEdit extends AbstractEdit {
    EditorState editorState;
    Vector<Boolean> oldValue;
    Vector<Boolean> newValue;
    int cellId;

    public CellFeaturesEdit(EditorState editorState, Vector<Boolean> oldValue, Vector<Boolean> newValue, int cellId) {
        this.editorState = editorState;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.cellId = cellId;
    }

    protected String describeUndo() {
        return "undo " + getPresentationName();
    }

    protected String describeRedo() {
        return "redo " + getPresentationName();
    }

    protected String getPresentationName() {
        if (countDifferences(oldValue, newValue) == 1) {
            for (Features feature : Features.values()) {
                if (feature.isChosen(oldValue) != feature.isChosen(newValue)) {
                    if (feature.isChosen(newValue)) {
                        return "set cell " + feature.toString();
                    } else {
                        return "unset cell " + feature.toString();
                    }
                }
            }
        }
        return "change cell features";
    }

    protected int countDifferences(Vector<Boolean> oldValue, Vector<Boolean> newValue) {
        if (oldValue.equals(newValue)) {
            return 0;
        }
        if (oldValue.size() <= newValue.size()) {
            int result = 0;
            for (int i = 0; i < oldValue.size(); i++) {
                if (oldValue.get(i) != newValue.get(i)) {
                    result = result + 1;
                }
            }
            for (int j = oldValue.size(); j < newValue.size(); j++) {
                if (newValue.get(j)) {
                    result = result + 1;
                }
            }
            return result;
        }
        return countDifferences(newValue, oldValue);
    }

    public void undo() throws CannotUndoException {
/*
        // Call the UndoableEdit class for housekeeping
        super.undo();

*/
        // set the old value, excluding all undo activity
        editorState.justSetCellFeatures(cellId, oldValue);
    }

    public void redo() throws CannotUndoException {
/*
        // Call the UndoableEdit class for housekeeping
        super.redo();

*/
        // set the new value, excluding all undo activity
        editorState.justSetCellFeatures(cellId, newValue);
    }

    protected boolean affectsMap() {
        return true;
    }

    protected boolean affectsPalettes() {
        return false;
    }

    protected Vector<Integer> getAffectedCellIds() {
        Vector<Integer> result = new Vector<>(1);
        result.add(cellId);
        return result;
    }
}