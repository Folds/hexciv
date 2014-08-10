package net.folds.hexciv;

import org.jdesktop.swingx.MultiSplitLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Created by jasper on Jul 28, 2014.
 */
public class PeopleGraphPanel extends GraphPanel {
    // People
    // #             seen cells               (scale 0% - 100%)
    // #             cities,                  (auto-adjusting scale, starts at 0 - 100)
    // #             citizens                 (auto-adjusting scale, starts at 0 - 100)
    // #             myriads                  (auto-adjusting scale, starts at 0 - 100)
    // #             settlers, troops ships, planes, caravans
    //                                        (background stacked graph, auto-adjusting scale, starts at 0 - 100)

    JLabel lblSeenCells = new JLabel("Seen cells (0 - 100%)", SwingConstants.CENTER);
    JLabel lblCities = new JLabel("Cities (0 - 10)", SwingConstants.CENTER);
    JLabel lblCitizens = new JLabel("Citizens (0 - 100)", SwingConstants.CENTER);
    JLabel lblPopulation = new JLabel("Pop. (0 - 100 k)", SwingConstants.CENTER);

    JLabel lblUnits = new JLabel("Units (0 - 100):", SwingConstants.LEFT);
    JLabel lblTroops = new JLabel("Army", SwingConstants.CENTER);
    JLabel lblShips = new JLabel("Navy", SwingConstants.CENTER);
    JLabel lblPlanes = new JLabel("Air", SwingConstants.CENTER);
    JLabel lblSettlers = new JLabel("Settlers", SwingConstants.CENTER);
    JLabel lblCaravans = new JLabel("Caravans", SwingConstants.CENTER);

    Vector<JLabel> lblBlanks = new Vector<JLabel>(2);
    StatSheet statSheet;

    public PeopleGraphPanel(StatSheet statSheet) {
        super();
        this.statSheet = statSheet;
        String layoutDef =
                "(COLUMN " +
                  "(LEAF name=seen       weight=0.14) " +
                  "(LEAF name=cities     weight=0.14) " +
                  "(LEAF name=citizens   weight=0.14) " +
                  "(LEAF name=population weight=0.14) " +
                  "(LEAF name=units      weight=0.16) " +
                  "(ROW                  weight=0.14" +
                    "(LEAF name=blank0   weight=0.07) " +
                    "(LEAF name=troops   weight=0.31) " +
                    "(LEAF name=ships    weight=0.31) " +
                    "(LEAF name=planes   weight=0.31) " +
                  ")" +
                  "(ROW                  weight=0.14" +
                    "(LEAF name=blank1   weight=0.07) " +
                    "(LEAF name=settlers weight=0.46) " +
                    "(LEAF name=caravans weight=0.47) " +
                  ")" +
                ")"
                ;

        MultiSplitLayout.Node modelLegend = MultiSplitLayout.parseModel(layoutDef);
        legendPane.setDividerSize(3);
        legendPane.getMultiSplitLayout().setModel(modelLegend);

        Color darkBlue   = new Color( 51,  51, 153);
        Color black      = new Color(  0,   0,   0);
        Color darkRed    = new Color(153,  51,  51);
        Color darkGreen  = new Color( 51, 102,  51);

        Color beige      = new Color(255, 255, 153);
        Color lightBlue  = new Color(153, 204, 255);
        Color turquoise  = new Color( 51, 255, 204);
        Color silver     = new Color(204, 204, 204);
        Color lightGreen = new Color(153, 255, 153);

        lblSeenCells.setForeground(darkBlue);
        lblPopulation.setForeground(black);
        lblCitizens.setForeground(darkRed);
        lblCities.setForeground(darkGreen);

        lblCaravans.setBackground(beige);
        lblPlanes.setBackground(lightBlue);
        lblShips.setBackground(turquoise);
        lblTroops.setBackground(silver);
        lblSettlers.setBackground(lightGreen);

        lblCaravans.setOpaque(true);
        lblPlanes.setOpaque(true);
        lblShips.setOpaque(true);
        lblTroops.setOpaque(true);
        lblSettlers.setOpaque(true);

        legendPane.add(lblSeenCells,  "seen");
        legendPane.add(lblPopulation, "population");
        legendPane.add(lblCitizens,   "citizens");
        legendPane.add(lblCities,     "cities");

        lblUnits.setVerticalAlignment(SwingConstants.BOTTOM);
        legendPane.add(lblUnits, "units");
        legendPane.add(lblCaravans, "caravans");
        legendPane.add(lblPlanes,   "planes");
        legendPane.add(lblShips,    "ships");
        legendPane.add(lblTroops,   "troops");
        legendPane.add(lblSettlers, "settlers");
        populateBlanks();

        plotPane.addToForeground(statSheet.numSeenCells, darkBlue);
        plotPane.addToForeground(statSheet.numMyriads,   black);
        plotPane.addToForeground(statSheet.numCitizens,  darkRed);
        plotPane.addToForeground(statSheet.numCities,    darkGreen);

        plotPane.addToBackground(statSheet.numSettlers,  lightGreen);
        plotPane.addToBackground(statSheet.numTroops,    silver);
        plotPane.addToBackground(statSheet.numShips,     turquoise);
        plotPane.addToBackground(statSheet.numPlanes,    lightBlue);
        plotPane.addToBackground(statSheet.numCaravans,  beige);
     }

    protected void populateBlanks() {
        for (int i = 0; i < lblBlanks.capacity(); i++) {
            lblBlanks.add(new JLabel("  "));
            legendPane.add(lblBlanks.get(i), "blank" + i);
        }
    }

    public void updateStats() {
        int totalUnits = statSheet.numCaravans.getCurrentValue() +
                         statSheet.numPlanes.getCurrentValue() +
                         statSheet.numShips.getCurrentValue() +
                         statSheet.numTroops.getCurrentValue() +
                         statSheet.numSettlers.getCurrentValue();
        if (totalUnits > statSheet.numTroops.getMaxRange()) {
            statSheet.numTroops.setMaxRange(StatColumn.getLowestCeiling(totalUnits));
        }
        
        int maxRangeOfUnits = coordinateMaxRanges(statSheet.numSettlers, statSheet.numTroops,
                                                  statSheet.numShips, statSheet.numPlanes,
                                                  statSheet.numCaravans);
        String unitCaption = getCaption("Units", 0, maxRangeOfUnits);
        if (!lblUnits.getText().equals(unitCaption)) {
            lblUnits.setText(unitCaption);
        }
        // lblSeenCells.text does not change.
        String citiesCaption = getCaption("Cities", "", 0, statSheet.numCities.getMaxRange());
        if (!lblCities.getText().equals(citiesCaption)) {
            lblCities.setText(citiesCaption);
        }
        String citizensCaption = getCaption("Citizens", "", 0, statSheet.numCitizens.getMaxRange());
        if (!lblCitizens.getText().equals(citizensCaption)) {
            lblCitizens.setText(citizensCaption);
        }
        String populationCaption = getCaption("Pop", "", 0, statSheet.numMyriads.getMaxRange() * 10000);
        if (!lblPopulation.getText().equals(populationCaption)) {
            lblPopulation.setText(populationCaption);
        }

        plotPane.repaint();
    }

    protected String getCaption(String summary, int minRange, int maxRange) {
        return getCaption(summary, ":", minRange, maxRange);
    }

    protected String getCaption(String summary, String suffix, int minRange, int maxRange) {
        String strMaxRange = maxRange + "";
        if (maxRange > 1000000) {
            strMaxRange = (maxRange / 1000000) + " M";
        } else if (maxRange > 1000) {
            strMaxRange = (maxRange /    1000) + " k";
        }
        return summary + " (" + minRange + " - " + strMaxRange + ")" + suffix;
    }

}
