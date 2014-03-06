package net.folds.hexciv;

import com.sun.javaws.Globals;

import javax.swing.undo.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Vector;

/**
 * Created by jasper on Feb 16, 2014.
 */
public class EditorState {
    protected WorldMap map = new WorldMap();
    private TerrainTypes terrain;
    private boolean bonus;
    private boolean road;
    private boolean railroad;
    private boolean irrigation;
    private boolean village;
    private boolean city;

    protected UndoStack undoStack;

    protected File file;

    protected EditorState() {
        terrain = TerrainTypes.ocean;
        String filename = "civMap.txt";
        bonus = false;
        road = false;
        railroad = false;
        irrigation = false;
        village = false;
        city = false;
        undoStack = new UndoStack();
    }

    protected void addListener(EditListener listener) {
        undoStack.addListener(listener);
    }

    protected void undo() throws CannotUndoException {
        undoStack.undo();
    }

    protected void resetUndoStack() {
        undoStack.discardAllEdits();
    }

    protected void redo() throws CannotUndoException {
        undoStack.redo();
    }

    protected boolean canUndo() {
        return undoStack.canUndo();
    }

    protected boolean canRedo() {
        return undoStack.canRedo();
    }

    protected int getIndexOfNextAdd(UndoManager undoManager) {
        Class c = undoManager.getClass();
        Field field;
        try {
            field = c.getDeclaredField("indexOfNextAdd");
        } catch (NoSuchFieldException e) {
            return -1;
        }
        int result;
        try {
            result = (int) field.get(undoManager);
        } catch (IllegalAccessException e) {
            return -1;
        }
        return result;
    }

    protected AbstractEdit getPrevEdit(UndoManager undoManager) {
        int insertionPoint = getIndexOfNextAdd(undoManager);
        if (insertionPoint < 1) {
            return new NoEdit();
        }
        Class c = undoManager.getClass();
        Field field;
        try {
            field = c.getDeclaredField("edits");
        } catch (NoSuchFieldException e) {
            return new NoEdit();
        }
        Vector<UndoableEdit> edits;
        try {
            edits = (Vector<UndoableEdit>) field.get(undoManager);
        } catch (IllegalAccessException e) {
            return new NoEdit();
        }
        UndoableEdit edit = edits.get(insertionPoint - 1);
        return (AbstractEdit) edit;
    }

    protected CellSnapshot getCellSnapshot(int cellId) {
        return map.getCellSnapshot(cellId);
    }

    protected int getAdjacentCellId(int cellId, Directions dir) {
        return map.getAdjacentCellId(cellId, dir);
    }

    // returns whether the request changed the state.
    protected boolean updateCell(int cellId) {
        markUndoStack();
        boolean terrainUpdated = setCellTerrain(cellId, terrain);
        boolean featuresUpdated = setCellFeatures(cellId, getFeatures());
        return (terrainUpdated || featuresUpdated);
    }

    protected boolean setCellTerrain(int cellId, TerrainTypes terrain) {
        TerrainTypes oldValue = map.getTerrain(cellId);
        boolean result = justSetCellTerrain(cellId, terrain);
        if (result) {
            CellTerrainEdit edit = new CellTerrainEdit(this, oldValue, terrain, cellId);
            undoStack.postEdit(edit);
//            undoSupport.postEdit(edit);
        }
        return result;
    }

    protected void markUndoStack() {
        SignificantMarker edit = new SignificantMarker();
        undoStack.postEdit(edit);
//        undoSupport.postEdit(edit);
    }

    protected boolean setCellFeatures(int cellId, Vector<Boolean> features) {
        Vector<Boolean> oldFeatures = map.getFeatures(cellId);
        boolean result = justSetCellFeatures(cellId, features);
        if (result) {
            CellFeaturesEdit edit = new CellFeaturesEdit(this, oldFeatures, features, cellId);
            undoStack.postEdit(edit);
//            undoSupport.postEdit(edit);
        }
        return justSetCellFeatures(cellId, features);
    }

    // The "justSet" methods are meant to be called by the undo manager.
    // The "justSet" methods do not create new undo Edits.
    protected boolean justSetCellTerrain(int cellId, TerrainTypes terrain) {
        return map.setTerrain(cellId, terrain);
    }

    protected boolean justSetCellFeatures(int cellId, Vector<Boolean> features) {
        return map.setFeatures(cellId, features);
    }

    protected TerrainTypes getTerrain() {
        return terrain;
    }

    protected Vector<Boolean> getFeatures() {
        Vector<Boolean> result = new Vector<>(6);
        result.add(bonus);
        result.add(road);
        result.add(railroad);
        result.add(irrigation);
        result.add(village);
        result.add(city);
        return result;
    }

    // returns whether the request changed the state.
    protected boolean setTerrain(TerrainTypes terrain) {
        if (this.terrain != terrain) {
            this.terrain = terrain;
            return true;
        }
        return false;
    }

    // returns whether the request changed the state.
    protected boolean toggleFeature(Features feature) {
        switch (feature.getValue()) {
            case 1: return setRoad(!road);
            case 2: return setRailroad(!railroad);
            case 3: return setIrrigation(!irrigation);
            case 4: return setVillage(!village);
            case 5: return setCity(!city);
            case 0: default: return setBonus(!bonus);
        }
    }

    // returns whether the request changed the state.
    protected boolean updatePalettes(int cellId) {
        TerrainTypes terrain = map.getTerrain(cellId);
        Vector<Boolean> features = map.getFeatures(cellId);
        Vector<Boolean> currentFeatures = this.getFeatures();
        if ((this.terrain.equals(terrain)) && (currentFeatures.equals(features))) {
            return false;
        }
        setTerrain(terrain);
        setBonus(Features.bonus.isChosen(features));
        setRoad(Features.road.isChosen(features));
        setRailroad(Features.railroad.isChosen(features));
        setIrrigation(Features.irrigation.isChosen(features));
        setVillage(Features.village.isChosen(features));
        setCity(Features.city.isChosen(features));
        return true;
    }

    // returns whether the request changed the state.
    protected boolean setBonus(boolean bonus) {
        if (this.bonus != bonus) {
            this.bonus = bonus;
            return true;
        }
        return false;
    }

    // returns whether the request changed the state.
    protected boolean setRoad(boolean road) {
        if (this.road != road) {
            this.road = road;
            return true;
        }
        return false;
    }

    // returns whether the request changed the state.
    protected boolean setRailroad(boolean railroad) {
        if (this.railroad != railroad) {
            this.railroad = railroad;
            return true;
        }
        return false;
    }

    // returns whether the request changed the state.
    protected boolean setIrrigation(boolean irrigation) {
        if (this.irrigation != irrigation) {
            this.irrigation = irrigation;
            return true;
        }
        return false;
    }

    // returns whether the request changed the state.
    protected boolean setVillage(boolean village) {
        if (this.village != village) {
            this.village = village;
            return true;
        }
        return false;
    }

    // returns whether the request changed the state.
    protected boolean setCity(boolean city) {
        if (this.city != city) {
            this.city = city;
            return true;
        }
        return false;
    }

    protected String getUndoText() {
        return undoStack.getUndoText();
    }

    protected String getRedoText() {
        return undoStack.getRedoText();
    }
}