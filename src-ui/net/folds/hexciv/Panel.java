package net.folds.hexciv;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class Panel extends JPanel {
    GameScreen parent;
    TextDisplayer textDisplayer = new TextDisplayer();

    public Panel(GameScreen parent) {
        super();
        this.parent = parent;
    }

    protected void beginUsingTextArea(Graphics2D comp2D,
                                      int leftMarginInPixels,
                                      int rowHeightInPixels) {
        textDisplayer.beginUsing(comp2D, leftMarginInPixels, rowHeightInPixels, rowHeightInPixels);
    }

    protected void beginUsingTextArea(Graphics2D comp2D,
                                      int leftMarginInPixels,
                                      int initialVerticalPositionInPixels,
                                      int rowHeightInPixels) {
        textDisplayer.beginUsing(comp2D, leftMarginInPixels, initialVerticalPositionInPixels, rowHeightInPixels);
    }

    protected void typeLine(String str) {
        textDisplayer.typeLine(str);
    }

    protected void finishUsingTextArea() {
        textDisplayer.finishUsing();
    }
}