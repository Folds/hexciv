package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.swing.undo.CannotUndoException;
import java.util.BitSet;
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
        BitSet oldSouthPoleFeatures = editorState.map.getFeatures(0);
        BitSet desiredSouthPoleFeatures = getFeatures(true, true, true, true, true, true, true, true);
        int numFeatures = desiredSouthPoleFeatures.size();
        editorState.markUndoStack();
        editorState.setCellFeatures(0, desiredSouthPoleFeatures);
        BitSet newSouthPoleFeatures = editorState.map.getFeatures(0);
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
        BitSet newerSouthPoleFeatures = editorState.map.getFeatures(0);
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

    BitSet getFeatures(boolean bonus, boolean road, boolean railroad, boolean pollution,
                                boolean irrigation, boolean mine,
                                boolean village, boolean city) {
        BitSet result = new BitSet(8);
        result.set(0, bonus);
        result.set(1, road);
        result.set(2, railroad);
        result.set(3, pollution);
        result.set(4, irrigation);
        result.set(5, mine);
        result.set(6, village);
        result.set(7, city);
        return result;
    }
}