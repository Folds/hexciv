package net.folds.hexciv;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.Vector;

/**
 * Created by jasper on Mar 03, 2014.
 */
public class UndoStack implements EditListener {
    Vector<AbstractEdit> edits;
    int insertionPoint;
    int maxInsertionPoint;
    Vector<EditListener> listeners;

    protected UndoStack() {
        edits = new Vector<>(100);
        insertionPoint = 0;
        listeners = new Vector<EditListener>(1);
    }

    protected void addListener(EditListener listener) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    protected void publishEdit(AbstractEdit edit) {
        for (EditListener listener : listeners) {
            listener.postEdit(edit);
        }
    }

    protected void undo() throws CannotUndoException {
        if (insertionPoint <= 0) {
            throw new CannotUndoException();
        }
        while ((insertionPoint > 0) && (!edits.get(insertionPoint - 1).isSignificant())) {
            AbstractEdit edit = edits.get(insertionPoint - 1);
            edit.undo();
            publishEdit(edit);
            insertionPoint = insertionPoint - 1;
        }
        if (insertionPoint > 0) {
            AbstractEdit edit = edits.get(insertionPoint - 1);
            edit.undo();
            publishEdit(edit);
            insertionPoint = insertionPoint - 1;
        }
    }

    protected void redo() throws CannotRedoException {
        if (insertionPoint >= maxInsertionPoint) {
            throw new CannotRedoException();
        }
        if (insertionPoint < maxInsertionPoint) {
            AbstractEdit edit = edits.get(insertionPoint);
            edit.redo();
            publishEdit(edit);
            insertionPoint = insertionPoint + 1;
        }
        while ((insertionPoint < maxInsertionPoint) && (!edits.get(insertionPoint).isSignificant())) {
            AbstractEdit edit = edits.get(insertionPoint);
            edit.redo();
            publishEdit(edit);
            insertionPoint = insertionPoint + 1;
        }
    }

    protected boolean canUndo() {
        if (insertionPoint > 0) {
            return true;
        }
        return false;
    }

    protected boolean canRedo() {
        if (insertionPoint < maxInsertionPoint) {
            return true;
        }
        return false;
    }

    protected void discardAllEdits() {
        while (maxInsertionPoint > 0) {
            discardLastEdit();
        }
    }

    protected void discardLastEdit() {
        if (maxInsertionPoint <= 0) {
            return;
        }
        AbstractEdit edit = edits.get(maxInsertionPoint - 1);
//        edit.die();
        edits.remove(maxInsertionPoint - 1);
        maxInsertionPoint = maxInsertionPoint - 1;
        if (insertionPoint > maxInsertionPoint) {
            insertionPoint = maxInsertionPoint;
        }
    }

    public void postEdit(AbstractEdit edit) {
        while (insertionPoint < maxInsertionPoint) {
            discardLastEdit();
        }
        edits.add(insertionPoint, edit);
        maxInsertionPoint = maxInsertionPoint + 1;
        insertionPoint = insertionPoint + 1;
    }

    protected String getUndoText() {
        if (!canUndo()) {
            return "Cannot undo.";
        }
        int i = insertionPoint - 1;
        for (; (i >= 0) && (!edits.get(i).isSignificant()); i--) {
            String description = edits.get(i).describeUndo();
            if (!description.equals("undo")) {
                return description;
            }
        }
        if ((i >= 0) && (edits.get(i).isSignificant())) {
            String description = edits.get(i).describeUndo();
            if (!description.equals("undo")) {
                return description;
            }
        }
        return "undo";
    }

    protected String getRedoText() {
        if (!canRedo()) {
            return "Cannot redo.";
        }
        int i = insertionPoint;
        if ((i < maxInsertionPoint) && (edits.get(i).isSignificant())) {
            String description = edits.get(i).describeRedo();
            if (!description.equals("redo")) {
                return description;
            }
        }
        for(i = i + 1; (i < maxInsertionPoint) && (!edits.get(i).isSignificant()); i++) {
            String description = edits.get(i).describeUndo();
            if (!description.equals("redo")) {
                return description;
            }
        }
        return "redo";
    }
}