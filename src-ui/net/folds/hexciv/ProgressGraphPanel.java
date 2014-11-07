package net.folds.hexciv;

import org.jdesktop.swingx.MultiSplitLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Created by jasper on Jul 28, 2014.
 */
public class ProgressGraphPanel extends GraphPanel {

    JLabel lblPercentages = new JLabel("%:", SwingConstants.LEFT);
    JLabel lblSciencePercentage = new JLabel("Sci", SwingConstants.CENTER);
    JLabel lblTaxPercentage = new JLabel("Tax", SwingConstants.CENTER);
    JLabel lblLuxuryPercentage = new JLabel("Lux", SwingConstants.CENTER);

    JLabel lblWip = new JLabel("Wip:", SwingConstants.LEFT);
    JLabel lblScienceWip = new JLabel("Sci", SwingConstants.CENTER);
    JLabel lblCashWip = new JLabel("Cash", SwingConstants.CENTER);
    JLabel lblProdWip = new JLabel("Prod", SwingConstants.CENTER);

    JLabel lblTechs = new JLabel("Tech:", SwingConstants.LEFT);
    JLabel lblNumTechs = new JLabel("#", SwingConstants.CENTER);
    JLabel lblMaxTech = new JLabel("Max", SwingConstants.CENTER);

    Vector<JLabel> lblBlanks = new Vector<JLabel>(6);
    StatSheet statSheet;

    public ProgressGraphPanel(StatSheet statSheet) {
        super();
        this.statSheet = statSheet;
        String layoutDef =
              "(COLUMN " +
                "(LEAF name=percent weight=0.15) " +
                "(ROW               weight=0.15" +
                  "(LEAF name=blank0   weight=0.1) " +
                  "(LEAF name=science  weight=0.3) " +
                  "(LEAF name=taxes    weight=0.3) " +
                  "(LEAF name=luxuries weight=0.3) " +
                ")" +
                "(LEAF name=wip     weight=0.15) " +
                "(ROW               weight=0.15" +
                  "(LEAF name=blank2   weight=0.1) " +
                  "(LEAF name=sciwip   weight=0.3) " +
                  "(LEAF name=cash     weight=0.3) " +
                  "(LEAF name=prod     weight=0.3) " +
                ")" +
                "(LEAF name=tech    weight=0.15) " +
                "(ROW               weight=0.15" +
                  "(LEAF name=blank4   weight=0.1) " +
                  "(LEAF name=numtechs weight=0.3) " +
                  "(LEAF name=maxtech  weight=0.3) " +
                  "(LEAF name=blank5   weight=0.3) " +
                ")" +
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

        Color turquoise  = new Color( 51, 255, 204);
        Color blue       = new Color(  0,   0, 255);

        lblSciencePercentage.setBackground(lightBlue);
        lblTaxPercentage.setBackground(lightGreen);
        lblLuxuryPercentage.setBackground(pink);

        lblSciencePercentage.setOpaque(true);
        lblTaxPercentage.setOpaque(true);
        lblLuxuryPercentage.setOpaque(true);

        lblPercentages.setVerticalAlignment(SwingConstants.BOTTOM);
        legendPane.add(lblPercentages, "percent");
        legendPane.add(lblSciencePercentage, "science");
        legendPane.add(lblTaxPercentage,     "taxes");
        legendPane.add(lblLuxuryPercentage,  "luxuries");

        lblSciencePercentage.setToolTipText("Percentage of trade allocated to science");
        lblTaxPercentage.setToolTipText("Percentage of trade collected as taxes");
        lblLuxuryPercentage.setToolTipText("Percentage of trade allocated to luxuries");

        lblScienceWip.setForeground(darkBlue);
        lblCashWip.setForeground(darkGreen);
        lblProdWip.setForeground(darkRed);

        lblScienceWip.setToolTipText("Science in progress -- amound of work done towards next discovery");
        lblCashWip.setToolTipText("Amount of gold in treasury");
        lblProdWip.setToolTipText("Production in progress -- total of all cities' inventories");

        lblWip.setVerticalAlignment(SwingConstants.BOTTOM);
        legendPane.add(lblWip,        "wip");
        legendPane.add(lblScienceWip, "sciwip");
        legendPane.add(lblCashWip, "cash");
        legendPane.add(lblProdWip, "prod");

        lblNumTechs.setForeground(blue);
        lblMaxTech.setForeground(turquoise);

        lblTechs.setVerticalAlignment(SwingConstants.BOTTOM);
        legendPane.add(lblTechs,      "tech");
        legendPane.add(lblNumTechs,   "numtechs");
        legendPane.add(lblMaxTech,    "maxtech");
        populateBlanks();

        lblNumTechs.setToolTipText("Number of known technologies");
        lblMaxTech.setToolTipText("ID of most advanced (known) technology");

        plotPane.addToBackground(statSheet.sciencePercentage, lightBlue);
        plotPane.addToBackground(statSheet.taxPercentage,     lightGreen);
        plotPane.addToBackground(statSheet.luxuryPercentage,  pink);

        plotPane.addToForeground(statSheet.storedScience,     darkBlue);
        plotPane.addToForeground(statSheet.storedMoney,       darkGreen);
        plotPane.addToForeground(statSheet.storedProduction,  darkRed);

        plotPane.addToForeground(statSheet.maxTechId,         turquoise);
        plotPane.addToForeground(statSheet.numTechs,          blue);
    }

    protected void populateBlanks() {
        for (int i = 0; i < lblBlanks.capacity(); i++) {
            lblBlanks.add(new JLabel("  "));
            legendPane.add(lblBlanks.get(i), "blank" + i);
        }
    }

    public void updateStats() {
        int maxRangeOfPercentages
                = coordinateMaxRanges(statSheet.sciencePercentage, statSheet.taxPercentage, statSheet.luxuryPercentage);
        String percentageCaption = getCaption("%", 0, maxRangeOfPercentages);
        if (!lblPercentages.getText().equals(percentageCaption)) {
            lblPercentages.setText(percentageCaption);
        }
        int maxRangeOfWips
                = coordinateMaxRanges(statSheet.storedScience, statSheet.storedMoney, statSheet.storedProduction);
        String wipCaption = getCaption("Wip", 0, maxRangeOfWips);
        if (!lblWip.getText().equals(wipCaption)) {
            lblWip.setText(wipCaption);
        }
        int maxRangeOfTechs = coordinateMaxRanges(statSheet.maxTechId, statSheet.numTechs);
        String techCaption = getCaption("Tech", 0, maxRangeOfTechs);
        if (!lblTechs.getText().equals(techCaption)) {
            lblTechs.setText(techCaption);
        }
        plotPane.repaint();
    }

    protected String getCaption(String summary, int minRange, int maxRange) {
        return summary + " (" + minRange + " - " + maxRange + "):";
    }

}
