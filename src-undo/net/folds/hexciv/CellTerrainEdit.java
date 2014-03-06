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
public class CellTerrainEdit extends AbstractEdit {
    EditorState editorState;
    TerrainTypes oldValue;
    TerrainTypes newValue;
    int cellId;

    public CellTerrainEdit(EditorState editorState, TerrainTypes oldValue, TerrainTypes newValue, int cellId) {
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

    public String getPresentationName() {
        return "change cell to " + this.newValue.toString();
    }

    public void undo() throws CannotUndoException {
/*
        // Call the UndoableEdit class for housekeeping
        super.undo();

*/
        // set the old value, excluding all undo activity
        editorState.justSetCellTerrain(cellId, oldValue);
    }

    public void redo() throws CannotUndoException {
/*
        // Call the UndoableEdit class for housekeeping
        super.redo();

*/
        // set the new value, excluding all undo activity
        editorState.justSetCellTerrain(cellId, newValue);
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