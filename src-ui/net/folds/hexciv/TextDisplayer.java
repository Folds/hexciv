package net.folds.hexciv;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by jasper on Feb 05, 2014.
 */
public class TextDisplayer implements Serializable {
    private int x;         // horizontal position of current text string being painted, in pixels
    private int y;         // vertical position of current text string being painted, in pixels
    private int rowHeight; // text row height in pixels;
    private java.awt.Graphics2D comp2D;

    protected void beginUsing(java.awt.Graphics2D arg_comp2D,
                              int leftMarginInPixels,
                              int initialVerticalPositionInPixels,
                              int rowHeightInPixels)
    {
        finishUsing();
        comp2D = arg_comp2D;
        x = leftMarginInPixels;
        y = initialVerticalPositionInPixels;
        rowHeight = rowHeightInPixels;
    }

    protected void finishUsing() {
        comp2D = null;
    }

    protected void typeLine(String str) {
        comp2D.drawString(str, x, y);
        y = y + rowHeight;
    }
}
