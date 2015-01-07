package net.folds.hexciv;

import org.jdesktop.swingx.MultiSplitLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Created by jasper on Jul 28, 2014.
 */
public class AssetGraphPanel extends GraphPanel {

    // Military:    Barracks, City Walls, Missile Defense
    // Production:  Factory, Hydro Plant, Power Plant, Manufactory, Nuclear Plant
    // Food:        Granary, Aqueduct, Subway, Recycler
    // Happiness:   Temple, Palace, Market, Courthouse, Colosseum, Cathedral, Bank
    // Science:     Library, University
    // Wonders.

    JLabel lblImprovements = new JLabel("Improvements", SwingConstants.LEFT);
    JLabel lblWonders    = new JLabel("Wow",  SwingConstants.CENTER);
    JLabel lblScience    = new JLabel("Sci",  SwingConstants.CENTER);
    JLabel lblHappiness  = new JLabel("Hap",  SwingConstants.CENTER);
    JLabel lblFood       = new JLabel("Food", SwingConstants.CENTER);
    JLabel lblProduction = new JLabel("Prod", SwingConstants.CENTER);
    JLabel lblMilitary   = new JLabel("Mil",  SwingConstants.CENTER);

    // JLabel lblGovernment = new JLabel("Govt Type (" + GovernmentType.listAbbreviations() + ")", SwingConstants.LEFT);
    JLabel lblGovernment = new JLabel("Govt Type", SwingConstants.LEFT);
    JLabel lblTradeRoutes = new JLabel("Trade Routes", SwingConstants.LEFT);

    Vector<JLabel> lblBlanks = new Vector<JLabel>(2);
    StatSheet statSheet;

    public AssetGraphPanel(StatSheet statSheet) {
        super();
        this.statSheet = statSheet;
        String layoutDef =
              "(COLUMN " +
                "(LEAF name=imps weight=0.15) " +
                "(ROW               weight=0.15" +
                  "(LEAF name=blank0   weight=0.1) " +
                  "(LEAF name=wonders  weight=0.3) " +
                  "(LEAF name=science  weight=0.3) " +
                  "(LEAF name=happy    weight=0.3) " +
                ")" +
                "(ROW               weight=0.15" +
                  "(LEAF name=blank1   weight=0.1) " +
                  "(LEAF name=food     weight=0.3) " +
                  "(LEAF name=prod     weight=0.3) " +
                  "(LEAF name=mil      weight=0.3) " +
                ")" +
                "(LEAF name=gov  weight=0.25) " +
                "(LEAF name=trade  weight=0.25) " +
              ")"
                ;

        MultiSplitLayout.Node modelLegend = MultiSplitLayout.parseModel(layoutDef);
        legendPane.setDividerSize(3);
        legendPane.getMultiSplitLayout().setModel(modelLegend);

        Color pink       = new Color(255, 204, 204);
        Color lightGreen = new Color(153, 255, 153);
        Color lightBlue  = new Color(153, 204, 255);

        Color darkRed    = new Color(153,  51,  51);
        Color darkGreen  = new Color( 51, 102,  51);
        Color darkBlue   = new Color( 51,  51, 153);

//        Color gray       = new Color(104, 104, 104);
        Color gold       = new Color(153, 153,   0);

//        Color turquoise  = new Color( 51, 255, 204);
//        Color blue       = new Color(  0,   0, 255);
        Color white      = new Color(255, 255, 255);

        lblWonders.setBackground(darkBlue);
        lblScience.setBackground(lightBlue);
        lblHappiness.setBackground(pink);
        lblFood.setBackground(lightGreen);
        lblProduction.setBackground(darkRed);
        lblMilitary.setBackground(darkGreen);

        // Provide contrast versus dark colors.
        lblWonders.setForeground(white);
        lblProduction.setForeground(white);
        lblMilitary.setForeground(white);

        lblWonders.setOpaque(true);
        lblScience.setOpaque(true);
        lblHappiness.setOpaque(true);
        lblFood.setOpaque(true);
        lblProduction.setOpaque(true);
        lblMilitary.setOpaque(true);

        lblImprovements.setVerticalAlignment(SwingConstants.BOTTOM);
        legendPane.add(lblImprovements, "imps");
        legendPane.add(lblWonders,      "wonders");
        legendPane.add(lblScience,      "science");
        legendPane.add(lblHappiness,    "happy");
        legendPane.add(lblFood,         "food");
        legendPane.add(lblProduction,   "prod");
        legendPane.add(lblMilitary,     "mil");

        legendPane.add(lblGovernment, "gov");
        lblTradeRoutes.setForeground(gold);
        legendPane.add(lblTradeRoutes, "trade");

        populateBlanks();

        lblWonders.setToolTipText("Wonders");
        lblScience.setToolTipText("Science:  Libraries, Universities");
        lblHappiness.setToolTipText("Happiness:  Markets, Banks, Temples");
        lblFood.setToolTipText("Food:  Granaries, Aqueducts");
        lblProduction.setToolTipText("Production:  Factories, Manufactories, Power Plants, Hydro Plants, Nuclear Plants");
        lblMilitary.setToolTipText("Military:  Barracks, City Walls");

        lblGovernment.setToolTipText("Government:  " + GovernmentType.listNames());

        plotPane.addToBackground(statSheet.numWonders,   lblWonders.getBackground());
        plotPane.addToBackground(statSheet.numSciImps,   lblScience.getBackground());
        plotPane.addToBackground(statSheet.numHappyImps, lblHappiness.getBackground());
        plotPane.addToBackground(statSheet.numFoodImps,  lblFood.getBackground());
        plotPane.addToBackground(statSheet.numProdImps,  lblProduction.getBackground());
        plotPane.addToBackground(statSheet.numMilImps,   lblMilitary.getBackground());

        plotPane.addToForeground(statSheet.governmentTypeId, lblGovernment.getForeground());
        plotPane.addToForeground(statSheet.numTradeRoutes, lblTradeRoutes.getForeground());
    }

    protected void populateBlanks() {
        for (int i = 0; i < lblBlanks.capacity(); i++) {
            lblBlanks.add(new JLabel("  "));
            legendPane.add(lblBlanks.get(i), "blank" + i);
        }
    }

    public void updateStats() {
        int totalImprovements = statSheet.numWonders.getCurrentValue() +
                statSheet.numSciImps.getCurrentValue() +
                statSheet.numHappyImps.getCurrentValue() +
                statSheet.numFoodImps.getCurrentValue() +
                statSheet.numProdImps.getCurrentValue() +
                statSheet.numMilImps.getCurrentValue();
        if (totalImprovements > statSheet.numHappyImps.getMaxRange()) {
            statSheet.numHappyImps.setMaxRange(StatColumn.getLowestCeiling(totalImprovements));
        }

        if (totalImprovements > 0) {
            boolean stopHere = true;
        }

        int maxRangeOfImprovements = coordinateMaxRanges(statSheet.numWonders, statSheet.numSciImps,
                statSheet.numHappyImps, statSheet.numFoodImps,
                statSheet.numProdImps,  statSheet.numMilImps);
        String impCaption = getCaption("Improvements", 0, maxRangeOfImprovements);
        if (!lblImprovements.getText().equals(impCaption + ":")) {
            lblImprovements.setText(impCaption + ":");
        }
        String routesCaption = getCaption("Trade Routes", 0, statSheet.numTradeRoutes.getMaxRange());
        if (!lblTradeRoutes.getText().equals(routesCaption)) {
            lblTradeRoutes.setText(routesCaption);
        }

        plotPane.repaint();
    }

    protected String getCaption(String summary, int minRange, int maxRange) {
        return summary + " (" + minRange + " - " + maxRange + ")";
    }

}
