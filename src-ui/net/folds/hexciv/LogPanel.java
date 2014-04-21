package net.folds.hexciv;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jasper on April 20, 2014.
 */
public class LogPanel extends JPanel {
    TextDisplayer textDisplayer = new TextDisplayer();

    public LogPanel() {
        super();
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

    public void paintComponent(Graphics comp) {
        super.paintComponent(comp);
        Graphics2D comp2D = (Graphics2D) comp;
        long h = getHeight() - 5; // available height for information
        long w = getWidth()  - 5; // available width  for information
        comp2D.drawRect(2, 2, (int) w, (int) h);

    }

    protected void typeLine(String str) {
        textDisplayer.typeLine(str);
    }

    protected void finishUsingTextArea() {
        textDisplayer.finishUsing();
    }
}