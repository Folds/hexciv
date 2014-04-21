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
    EditListener listener;

    public GameScreen() {
        super("HexCiv");
        editorState = EditorState.get();
        listener = new RepaintRequester(this);
        editorState.addListener(listener);

        setSize(750, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mapPane        = new MapPanel(this);
        mapPane.setPreferredSize(new Dimension(665, 376));
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

    public static void main(String[] arguments) {
        GameScreen gs = new GameScreen();
        gs.setVisible(true);
    }

    public void chooseTerrain(TerrainTypes terrain) {
        boolean updated = editorState.setTerrain(terrain);
        if (updated) {
            repaintPalettes();
        }
    }

    public int getAdjacentCellId(int cellId, Directions dir) {
        return editorState.getAdjacentCellId(cellId, dir);
    }

    public CellSnapshot getCellSnapshot(int cellId) {
        return editorState.getCellSnapshot(cellId);
    }

    public Vector<Boolean> getDesiredFeatures() {
        return editorState.getFeatures();
    }

    public TerrainTypes getDesiredTerrain() {
        return editorState.getTerrain();
    }

    public WorldMap getMap() {
        return editorState.map;
    }

    public void toggleFeature(Features feature) {
        boolean updated = editorState.toggleFeature(feature);
        if (updated) {
            repaintPalettes();
        }
    }

    public void recenterCanvas(int cellId) {}

    public void repaintMaps() {
        mapPane.repaint();
    }

    public void repaintMaps(Vector<Integer> cellIds) {
        if (cellIds.size() == 0) {
            repaintMaps();
        } else {
            for (int cellId : cellIds) {
                repaintMaps(cellId);
            }
        }
    }

    public void repaintMaps(int cellId) {
        mapPane.repaint(cellId);
    }


    public void repaintOopses() {}

    public void repaintPalettes() {}

    public void updateCell(int cellId) {
        boolean updated = editorState.updateCell(cellId);
        if (updated) {
            repaintMaps(cellId);
        }
    }

    public void updateLocale(int cellId, int x, int y) {
        localePane.setCellId(cellId);
        mousePane.setCellId(cellId);
        mousePane.setX(x);
        mousePane.setY(y);
    }

    public void updateLocale(CellSnapshot cellSnapshot, int x, int y) {
        localePane.setCell(cellSnapshot);
        mousePane.setCellId(cellSnapshot.id);
        mousePane.setX(x);
        mousePane.setY(y);
    }

    public void updateLocale(Features feature, int x, int y) {
        mousePane.setFeature(feature);
        mousePane.setX(x);
        mousePane.setY(y);
    }

    public void updatePalettes(int cellId) {
        boolean updated = editorState.updatePalettes(cellId);
        if (updated) {
            repaintPalettes();
        }
    }
}
