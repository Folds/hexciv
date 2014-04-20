package net.folds.hexciv;

import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Created by jasper on Apr 18, 2014.
 */
public class GameScreen extends JFrame implements PaintableScreen, MovableMap, CellDescriber {
    EditorState editorState;
    JXMultiSplitPane multiSplitPane;
    MapPanel mapPane;
    LocalePanel localePane;
    MousePanel mousePane;

    public GameScreen() {
        super("HexCiv");
        editorState = EditorState.get();
        setSize(750, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mapPane        = new MapPanel(this);
        mapPane.setPreferredSize(new Dimension(750, 376));
        localePane     = new LocalePanel(this);
        mousePane      = new MousePanel(this);

        // The SwingX JXMultiSplitNode allows laying out multiple
        // components in a panel, without creating nested components.
        // http://javadoc.geotoolkit.org/external/swingx/org/jdesktop/swingx/MultiSplitLayout.html
        // http://stackoverflow.com/questions/11346120/jxmultisplitpane-hiding-node-causes-painting-issues
        // https://today.java.net/pub/a/today/2006/03/23/multi-split-pane.html
        // http://stackoverflow.com/questions/8660687/how-to-use-multisplitlayout-in-swingx?rq=1
        String layoutDef = "(COLUMN " +
                "(LEAF name=world.getMap weight=0.0) " +
                "(ROW weight=1.0 " +
                "(LEAF name=locale weight=0.5) " +
                "(LEAF name=mouse  weight=0.5) " +
                ")" +
                ")";
        MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
        multiSplitPane = new JXMultiSplitPane();
        multiSplitPane.setDividerSize(3);
        multiSplitPane.getMultiSplitLayout().setModel(modelRoot);
        multiSplitPane.add(mapPane,        "world.getMap");
        multiSplitPane.add(localePane,     "locale");
        multiSplitPane.add(mousePane,      "mouse");
        getContentPane().add(multiSplitPane);
    }

    public Vector<Boolean> getDesiredFeatures() {
        return editorState.getFeatures();
    }

    public void chooseTerrain(TerrainTypes terrain) {
        boolean updated = editorState.setTerrain(terrain);
        if (updated) {
            repaintPalettes();
        }
    }

    public TerrainTypes getDesiredTerrain() {
        return editorState.getTerrain();
    }

    public void updateLocale(Features feature, int x, int y) {
        mousePane.setFeature(feature);
        mousePane.setX(x);
        mousePane.setY(y);
    }

    public void toggleFeature(Features feature) {
        boolean updated = editorState.toggleFeature(feature);
        if (updated) {
            repaintPalettes();
        }
    }

    public CellSnapshot getCellSnapshot(int cellId) {
        return editorState.getCellSnapshot(cellId);
    }

    public int getAdjacentCellId(int cellId, Directions dir) {
        return editorState.getAdjacentCellId(cellId, dir);
    }

    public WorldMap getMap() {
        return editorState.map;
    }

    public void recenterCanvas(int cellId) {}

    public void updateLocale(int cellId, int x, int y) {}

    public static void main(String[] arguments) {
        GameScreen gs = new GameScreen();
        gs.setVisible(true);
    }

    public void repaintOopses() {}

    public void repaintMaps() {}

    public void repaintMaps(Vector<Integer> cellIds) {
        if (cellIds.size() == 0) {
            repaintMaps();
        } else {
            for (int cellId : cellIds) {
                repaintMaps(cellId);
            }
        }
    }

    public void repaintMaps(int cellId) {}

    public void repaintPalettes() {}

    public void updateLocale(CellSnapshot cellSnapshot, int x, int y) {
        localePane.setCell(cellSnapshot);
        mousePane.setCellId(cellSnapshot.id);
        mousePane.setX(x);
        mousePane.setY(y);
    }

    public void updatePalettes(int cellId) {
        boolean updated = editorState.updatePalettes(cellId);
        if (updated) {
            repaintPalettes();
        }
    }

    public void updateCell(int cellId) {
        boolean updated = editorState.updateCell(cellId);
        if (updated) {
            repaintMaps(cellId);
        }
    }
}
