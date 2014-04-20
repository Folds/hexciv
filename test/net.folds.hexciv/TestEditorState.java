package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.swing.undo.CannotUndoException;
import java.util.Vector;

/**
 * Created by jasper on Feb 28, 2014.
 */
public class TestEditorState {
    EditorState editorState;

    public TestEditorState() {
        editorState = EditorState.get();
    }

    @Test
    public void testUndo() {
        editorState.resetUndoStack();
        TerrainTypes oldSouthPoleTerrain = editorState.map.getTerrain(0);
        Vector<Boolean> oldSouthPoleFeatures = editorState.map.getFeatures(0);
        Vector<Boolean> desiredSouthPoleFeatures = getFeatures(true, true, true, true, true, true);
        int numFeatures = desiredSouthPoleFeatures.size();
        editorState.markUndoStack();
        editorState.setCellFeatures(0, desiredSouthPoleFeatures);
        Vector<Boolean> newSouthPoleFeatures = editorState.map.getFeatures(0);
        Assert.assertEquals(newSouthPoleFeatures, desiredSouthPoleFeatures,
                "Expected south pole to be changed to have all " + numFeatures + " features.");
        editorState.markUndoStack();
        editorState.setCellTerrain(0, TerrainTypes.jungle);
        TerrainTypes newSouthPoleTerrain = editorState.map.getTerrain(0);
        Assert.assertEquals(newSouthPoleTerrain, TerrainTypes.jungle,
                "Expected south pole terrain to be changed to jungle.");
        editorState.undo();
        TerrainTypes newerSouthPoleTerrain = editorState.map.getTerrain(0);
        Assert.assertEquals(newerSouthPoleTerrain, oldSouthPoleTerrain,
                "Expected south pole terrain to be reverted to " + oldSouthPoleTerrain.toString() + ".");
        editorState.undo();
        Vector<Boolean> newerSouthPoleFeatures = editorState.map.getFeatures(0);
        Assert.assertEquals(newerSouthPoleFeatures, oldSouthPoleFeatures,
                "Expected south pole features to be reverted to have no features.");
        try {
            editorState.undo();
            boolean gotHere = true;
            Assert.assertFalse(gotHere, "Expected error on third undo after two actions.");
        } catch (CannotUndoException e) {
            // As expected.  Cannot undo 3 actions if only 2 were ever performed.
        }
        editorState.resetUndoStack();
    }

    Vector<Boolean> getFeatures(boolean bonus, boolean road, boolean railroad,
                                boolean irrigation, boolean village, boolean city) {
        Vector<Boolean> result = new Vector<>(6);
        result.add(bonus);
        result.add(road);
        result.add(railroad);
        result.add(irrigation);
        result.add(village);
        result.add(city);
        return result;
    }
}