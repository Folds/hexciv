package net.folds.hexciv;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.File;
import java.util.Vector;

import darrylbu.util.SwingUtils;
import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;

/**
 * Created by Jasper on Sep 18, 2011
 */
public class GameScreen extends JFrame {
    EditorState editorState;

    JXMultiSplitPane multiSplitPane;
    MapPanel mapPane;
    LocalePanel localePane;
    MousePanel mousePane;
    TitlePanel titlePane;
    TerrainPalette terrainPalette;
    FeaturePalette featurePalette;
    CanvasPanel canvasPane;
    JFileChooser fileChooser;
    EditListener listener;

    public GameScreen() {
        super("HexCiv Map Editor");
        editorState = new EditorState();
        listener = new RepaintRequester(this);
        editorState.addListener(listener);

        setSize(750, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        featurePalette = new FeaturePalette(this);
        terrainPalette = new TerrainPalette(this);
        mapPane        = new MapPanel(this);
        localePane     = new LocalePanel(this);
        canvasPane     = new CanvasPanel(this, getMap(), 16);
        mousePane      = new MousePanel(this);
        titlePane      = new TitlePanel(this);

        // The SwingX JXMultiSplitNode allows laying out multiple
        // components in a panel, without creating nested components.
        // http://javadoc.geotoolkit.org/external/swingx/org/jdesktop/swingx/MultiSplitLayout.html
        // http://stackoverflow.com/questions/11346120/jxmultisplitpane-hiding-node-causes-painting-issues
        // https://today.java.net/pub/a/today/2006/03/23/multi-split-pane.html
        // http://stackoverflow.com/questions/8660687/how-to-use-multisplitlayout-in-swingx?rq=1
        String layoutDef = "(COLUMN " +
                             "(ROW weight=1.0 " +
                               "(COLUMN weight=0.0" +
                                 "(LEAF name=title) " +
                                 "(LEAF name=feature.palette weight=1.0) " +
                               ")" +
                               "(COLUMN weight=1.0" +
                                 "(LEAF name=terrain.palette weight=0.15) " +
                                 "(LEAF name=canvas weight=0.85) " +
                               ")" +
                             ") "+
                             "(ROW "+
                               "(LEAF name=world.getMap weight=0.0) " +
                               "(LEAF name=locale weight=0.5) " +
                               "(LEAF name=mouse  weight=0.5) " +
                             ")" +
                           ")";
        MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
        multiSplitPane = new JXMultiSplitPane();
        multiSplitPane.setDividerSize(3);
        multiSplitPane.getMultiSplitLayout().setModel(modelRoot);
        multiSplitPane.add(titlePane,      "title");
        multiSplitPane.add(featurePalette, "feature.palette");
        multiSplitPane.add(terrainPalette, "terrain.palette");
        multiSplitPane.add(canvasPane,     "canvas");
        multiSplitPane.add(mapPane,        "world.getMap");
        multiSplitPane.add(localePane,     "locale");
        multiSplitPane.add(mousePane,      "mouse");
        getContentPane().add(multiSplitPane);
    }

    protected void repaintOopses() {
        titlePane.enableUndo(editorState.canUndo());
        titlePane.enableRedo(editorState.canRedo());
        titlePane.setUndoText(editorState.getUndoText());
        titlePane.setRedoText(editorState.getRedoText());
    }

    public static void main(String[] arguments) {
        GameScreen gs = new GameScreen();
        gs.setVisible(true);
    }

    protected CellSnapshot getCellSnapshot(int cellId) {
        return editorState.getCellSnapshot(cellId);
    }

    protected void updateLocale(int cellId, int x, int y) {
        localePane.setCellId(cellId);
        mousePane.setCellId(cellId);
        mousePane.setX(x);
        mousePane.setY(y);
    }

    protected void updateLocale(CellSnapshot cellSnapshot, int x, int y) {
        localePane.setCell(cellSnapshot);
        mousePane.setCellId(cellSnapshot.id);
        mousePane.setX(x);
        mousePane.setY(y);
    }

    protected void updateLocale(Features feature, int x, int y) {
        mousePane.setFeature(feature);
        mousePane.setX(x);
        mousePane.setY(y);
    }

    protected int getAdjacentCellId(int cellId, Directions dir) {
        return editorState.getAdjacentCellId(cellId, dir);
    }

    TerrainTypes getDesiredTerrain() {
        return editorState.getTerrain();
    }

    Vector<Boolean> getDesiredFeatures() {
        return editorState.getFeatures();
    }

    protected void chooseTerrain(TerrainTypes terrain) {
        boolean updated = editorState.setTerrain(terrain);
        if (updated) {
            terrainPalette.repaint();
            featurePalette.repaint();
        }
    }

    protected void toggleFeature(Features feature) {
        boolean updated = editorState.toggleFeature(feature);
        if (updated) {
            terrainPalette.repaint();
            featurePalette.repaint();
        }
    }
    protected void updateCell(int cellId) {
        boolean updated = editorState.updateCell(cellId);
        if (updated) {
            repaintMaps(cellId);
        }
    }

    public void repaintMaps() {
        canvasPane.repaint();
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

    protected void repaintMaps(int cellId) {
        canvasPane.repaint(cellId);
        mapPane.repaint(cellId);
    }

    protected void updatePalettes(int cellId) {
        boolean updated = editorState.updatePalettes(cellId);
        if (updated) {
            repaintPalettes();
        }
    }

    public void repaintPalettes() {
        terrainPalette.repaint();
        featurePalette.repaint();
    }

    WorldMap getMap() {
        return editorState.map;
    }

    protected void recenterCanvas(int cellId) {
        canvasPane.setCenterCell(cellId);
    }

    protected void open() {
        updateFileChooser();
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            setFile(file);
            editorState.map = Porter.importMap(editorState.file);
            canvasPane.setMap(editorState.map);
            mapPane.setMap(editorState.map);
            canvasPane.repaint();
            mapPane.repaint();
        }
    }

    protected void save() {
        updateFileChooser();
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            setFile(file);
            Porter.exportMap(editorState.map, editorState.file);
        }
    }

    protected void undo() {
        try {
            editorState.undo();
        } catch (CannotUndoException ignored) {
        }
    }

    protected void redo() {
        try {
            editorState.redo();
        } catch (CannotRedoException ignored) {
        }
    }

    protected void setFile(File file) {
        editorState.file = file;
        titlePane.lblFile.setText(file.getName());
    }

    protected void updateFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "HexCiv files (.civ,.txt,.json)", "civ", "txt", "json");
        fileChooser.setFileFilter(filter);

        // http://www.java-forums.org/awt-swing/13733-set-jfilechooser-default-details-view.html
        AbstractButton button = SwingUtils.getDescendantOfType(AbstractButton.class,
                fileChooser, "Icon", UIManager.getIcon("FileChooser.detailsViewIcon"));
        button.doClick();
    }
}