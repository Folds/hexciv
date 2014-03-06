package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Mar 03, 2014.
 */
public class RepaintRequester implements EditListener {
    GameScreen parent;

    protected RepaintRequester(GameScreen parent) {
        this.parent = parent;
    }

    public void postEdit(AbstractEdit edit) {
        parent.repaintOopses();
        if (edit.affectsMap()) {
            Vector<Integer> cellIds = edit.getAffectedCellIds();
            parent.repaintMaps(cellIds);
        }
        if (edit.affectsPalettes()) {
            parent.repaintPalettes();
        }
    }
}