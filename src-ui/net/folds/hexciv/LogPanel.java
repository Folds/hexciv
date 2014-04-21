package net.folds.hexciv;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Created by jasper on April 20, 2014.
 */
public class LogPanel extends JPanel {
    TextDisplayer textDisplayer = new TextDisplayer();
    Vector<String> entries = new Vector<>(100);
    int rowHeight = 18; // in pixels

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

    protected int countEntries() {
        return entries.size();
    }

    protected void finishUsingTextArea() {
        textDisplayer.finishUsing();
    }

    protected void log(String arg) {
        entries.addElement(arg);
        repaint();
    };

    public void paintComponent(Graphics comp) {
        super.paintComponent(comp);
        Graphics2D comp2D = (Graphics2D) comp;
        long h = getHeight() - 5; // available height for information
        long w = getWidth()  - 5; // available width  for information
        comp2D.drawRect(2, 2, (int) w, (int) h);
        int numRows = (int) (h / rowHeight);
        if (numRows <= 0) {
            return;
        }
        int numEntries = countEntries();
        int start = numEntries - numRows;
        if (start < 0) {
            start = 0;
        }
        beginUsingTextArea(comp2D, 5, rowHeight);
        for (int i = start; i < entries.size(); i++) {
            typeLine(entries.get(i));
        }
        finishUsingTextArea();
    }

    protected void typeLine(String str) {
        textDisplayer.typeLine(str);
    }
}