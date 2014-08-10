package net.folds.hexciv;

import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;

/**
 * Created by jasper on Jul 28, 2014.
 */
public class GraphPanel extends JXMultiSplitPane {
    LegendPanel legendPane;
    PlotPanel plotPane;

    public GraphPanel() {
        super();
        legendPane = new LegendPanel();
        plotPane = new PlotPanel();

        String layoutDef =
               "(ROW weight=1.0 " +
                 "(LEAF name=legend weight=0.0) " +
                 "(LEAF name=plot   weight=1.0) " +
                ")";
        MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
        setDividerSize(3);
        getMultiSplitLayout().setModel(modelRoot);
        add(legendPane, "legend");
        add(plotPane,   "plot");
    }

    protected int coordinateMaxRanges(StatColumn a, StatColumn b) {
        int maxA = a.getMaxRange();
        int maxB = b.getMaxRange();
        int result = Math.max(maxA, maxB);
        if (maxA != result) {
            a.setMaxRange(result);
        }
        if (maxB != result) {
            b.setMaxRange(result);
        }
        return result;
    }

    protected int coordinateMaxRanges(StatColumn a, StatColumn b, StatColumn c) {
        int maxA = a.getMaxRange();
        int maxB = b.getMaxRange();
        int maxC = c.getMaxRange();
        int result = Math.max(maxA, maxB);
        result = Math.max(result, maxC);
        if (maxA != result) {
            a.setMaxRange(result);
        }
        if (maxB != result) {
            b.setMaxRange(result);
        }
        if (maxC != result) {
            c.setMaxRange(result);
        }
        return result;
    }

    protected int coordinateMaxRanges(StatColumn a, StatColumn b, StatColumn c,
                                      StatColumn d, StatColumn e) {
        int maxA = a.getMaxRange();
        int maxB = b.getMaxRange();
        int maxC = c.getMaxRange();
        int maxD = d.getMaxRange();
        int maxE = e.getMaxRange();
        int result = Math.max(maxA, maxB);
        result = Math.max(result, Math.max(maxC, maxD));
        result = Math.max(result, maxE);
        if (maxA != result) {
            a.setMaxRange(result);
        }
        if (maxB != result) {
            b.setMaxRange(result);
        }
        if (maxC != result) {
            c.setMaxRange(result);
        }
        if (maxD != result) {
            d.setMaxRange(result);
        }
        if (maxE != result) {
            e.setMaxRange(result);
        }
        return result;
    }

}
