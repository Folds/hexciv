package net.folds.hexciv;

import org.jdesktop.swingx.MultiSplitLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jasper on Aug 01, 2014.
 */
public class PerformanceGraphPanel extends GraphPanel {
    JLabel lblThinkTime = new JLabel("Think time (0 - 100 ms)", SwingConstants.CENTER);

    StatSheet statSheet;

    public PerformanceGraphPanel(StatSheet statSheet) {
        super();
        this.statSheet = statSheet;

        String layoutDef = "(LEAF name=think weight=0.0)";

        MultiSplitLayout.Node modelLegend = MultiSplitLayout.parseModel(layoutDef);
        legendPane.setDividerSize(3);
        legendPane.getMultiSplitLayout().setModel(modelLegend);

        Color darkRed    = new Color(153,  51,  51);
        lblThinkTime.setForeground(darkRed);

        legendPane.add(lblThinkTime, "think");

        plotPane.addToForeground(statSheet.thinkingTimeInMilliseconds, darkRed);

    }

    public void updateStats() {
        String thinkTimeCaption = getDurationCaption("Think time", 0, statSheet.thinkingTimeInMilliseconds.getMaxRange());
        if (!lblThinkTime.getText().equals(thinkTimeCaption)) {
            lblThinkTime.setText(thinkTimeCaption);
        }

    }

    protected String getDurationCaption(String summary, int minRangeInMilliseconds, int maxRangeInMilliseconds) {
        int minRange = minRangeInMilliseconds;
        int maxRange = maxRangeInMilliseconds;
        String strMaxRange = maxRange + " ms";
        if (maxRange > 1000000) {
            strMaxRange = (maxRange / 1000000) + " ks";
        } else if (maxRange >= 1000) {
            strMaxRange = (maxRange /    1000) + " s";
        }
        return summary + " (" + minRange + " - " + strMaxRange + ")";
    }

}