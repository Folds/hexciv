package net.folds.hexciv;

import darrylbu.util.SwingUtils;
import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Apr 18, 2014.
 */
public class GameScreen extends JFrame
        implements PaintableScreen, MovableMap, CellDescriber, Playable, GameListener, ActionListener {
    EditorState editorState;
    JXMultiSplitPane multiSplitPane;
    MapPanel mapPane;
    MasterPanel masterPane;
    LogPanel logPane;
    LocalePanel localePane;
    MousePanel mousePane;
    JFileChooser fileChooser;
    EditListener listener;
    GameState gameState;
    Timer timer;

    public GameScreen() {
        super("HexCiv");
        WorldMap map =  WorldMap.getEarthMap();
        editorState = EditorState.get(map);
        listener = new RepaintRequester(this);
        editorState.addListener(listener);
        gameState = new GameState(this, map, 1);

        setSize(750, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mapPane        = new MapPanel(this);
        mapPane.setPreferredSize(new Dimension(665, 376));
        masterPane     = new MasterPanel(this);
        logPane        = new LogPanel();
        localePane     = new LocalePanel(this);
        mousePane      = new MousePanel(this);
        timer = new Timer(50, this);

        // The SwingX JXMultiSplitNode allows laying out multiple
        // components in a panel, without creating nested components.
        // http://javadoc.geotoolkit.org/external/swingx/org/jdesktop/swingx/MultiSplitLayout.html
        // http://stackoverflow.com/questions/11346120/jxmultisplitpane-hiding-node-causes-painting-issues
        // https://today.java.net/pub/a/today/2006/03/23/multi-split-pane.html
        // http://stackoverflow.com/questions/8660687/how-to-use-multisplitlayout-in-swingx?rq=1
        String layoutDef = "(COLUMN " +
                "(LEAF name=world.getMap weight=0.0) " +
                "(ROW weight=1.0 " +
                "(LEAF name=master weight=0.0) " +
                "(LEAF name=log    weight=1.0) " +
                "(LEAF name=locale weight=0.0) " +
                "(LEAF name=mouse  weight=0.0) " +
                ")" +
                ")";
        MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
        multiSplitPane = new JXMultiSplitPane();
        multiSplitPane.setDividerSize(3);
        multiSplitPane.getMultiSplitLayout().setModel(modelRoot);
        multiSplitPane.add(masterPane, "master");
        multiSplitPane.add(logPane,        "log");
        multiSplitPane.add(mapPane,        "world.getMap");
        multiSplitPane.add(localePane,     "locale");
        multiSplitPane.add(mousePane,      "mouse");
        getContentPane().add(multiSplitPane);
    }

    public static void main(String[] arguments) {
        GameScreen gs = new GameScreen();
        gs.setVisible(true);
    }

    public void celebrateTechnology(Civilization civ, TechKey key) {
        if (key == null) {
            logPane.log(civ.getName() + " has no techKey.");
        } else {
            if (key.nextTech < 0) {
                logPane.log(civ.getName() + " is researching prehistoric tech.");
            } else {
                if (key.getTech(key.nextTech) == null) {
                    logPane.log(civ.getName() + " is not researching any tech.");
                } else {
                    logPane.log(civ.getName() + " is researching " + key.getTech(key.nextTech).name);
                }
            }
            logPane.log("Other choices include:  " + civ.summarizeTechChoices() + ".");
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (gameState.isGameOver()) {
            timer.stop();
            celebrateEnd();
            return;
        }
        if (!gameState.isTurnInProgress) {
            gameState.playTurn();
        }
    }

    public void celebrateDiscovery(Civilization civ, Technology tech) {
        logPane.log(civ.getName() + " discovers " + tech.name + ".");
    }

    public void celebrateEnd() {
        logPane.log("Game over.");
        for (Civilization civ : gameState.civs) {
            logPane.log(civ.getBrag(gameState.map, gameState));
        }
        logPane.log("Play again?");
    }

    public void celebrateNewCity(Unit unit, String cityName) {
        int year = gameState.getYear();
        logPane.log(cityName + " est. " + formatYear(year));
    }

    public void celebrateUnsupported(City city, ImprovementType improvementType) {
        logPane.log(city.name + " sells " + improvementType.name);
    }

    public void celebrateWonder(City city, int wonderId) {
        if ((city == null) || (wonderId < 0)) {
            return;
        }
        String civName = city.civ.getName();
        String cityName = city.name;
        String wonderName = gameState.wonders.types.get(wonderId).name;
        logPane.log(civName + " builds " + wonderName + " in " + cityName);
    }

    public void celebrateYear(int year) {
        logPane.log("Reached year " + formatYear(year));
    }

    public void bemoanDisorder(City city) {
        logPane.log("Disorder in " + city.name);
    }

    public void bemoanFamine(City city) {
        if (city.size == 0) {
            logPane.log(city.name + " dies out.");
        } else {
            logPane.log("Famine in " + city.name);
        }
    }

    public void bemoanUnsupported(City city, Unit unit) {
        logPane.log(city.name + " cannot support " + unit.unitType.name);
    }

    public void bemoanUnsupported(City city, ImprovementType improvementType) {
        logPane.log(city.name + " forced to sell " + improvementType.name);
    }

    public void chooseTerrain(TerrainTypes terrain) {
        boolean updated = editorState.setTerrain(terrain);
        if (updated) {
            repaintPalettes();
        }
    }

    protected String formatYear(int year) {
        if (year == 0) { return "1 A.D."; }
        if (year > 0)  { return year + " A.D."; }
        return -year + " B.C.";
    }

    public int getAdjacentCellId(int cellId, Directions dir) {
        return editorState.getAdjacentCellId(cellId, dir);
    }

    public CellSnapshot getCellSnapshot(int cellId) {
        return editorState.getCellSnapshot(cellId);
    }

    public BitSet getDesiredFeatures() {
        return editorState.getFeatures();
    }

    public TerrainTypes getDesiredTerrain() {
        return editorState.getTerrain();
    }

    public WorldMap getMap() {
        return editorState.map;
    }

    public void open() {
        updateFileChooser();
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            setFile(file);
            editorState.map = Porter.importMap(editorState.file);
            mapPane.setMap(editorState.map);
            mapPane.repaint();
            logPane.log("Opened '" + editorState.file.getName() + "'");
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

    public void save() {
        updateFileChooser();
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            setFile(file);
            Porter.exportMap(editorState.map, editorState.file);
        }
    }

    protected void setFile(File file) {
        editorState.file = file;
        masterPane.lblFile.setText(file.getName());
    }

    public void startGame() {
        mapPane.hideAll();
        gameState.initialize();
        mapPane.seeCells(gameState.getSeenCells());
        mapPane.repaint();
        timer.start();
    }

    public void toggleFeature(Features feature) {
        boolean updated = editorState.toggleFeature(feature);
        if (updated) {
            repaintPalettes();
        }
    }

    protected void tryToSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
        }
    }

    public void updateCell(int cellId) {
        boolean updated = editorState.updateCell(cellId);
        if (updated) {
            repaintMaps(cellId);
        }
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

    public void updateSeenCells(BitSet seenCells) {
        mapPane.seeCells(seenCells);
    }
}
