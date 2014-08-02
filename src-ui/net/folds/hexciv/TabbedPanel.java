package net.folds.hexciv;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jasper on Jul 26, 2014.
 */
public class TabbedPanel extends JPanel {

    public TabbedPanel() {
        super(new GridLayout(1, 1));

        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane, 0);
    }

    public void addTab(String title, JComponent component, String tip, int mnemonic) {
        addTab(title, null, component, tip, mnemonic);
    }

    public void addTab(String title, Icon icon, JComponent component, String tip, int mnemonic) {
        if (getComponentCount() == 0) {
            return;
        }
        JTabbedPane tabbedPane = (JTabbedPane) getComponent(0);
        tabbedPane.addTab(title, icon, component, tip);
        int numTabs = tabbedPane.getComponentCount();
        tabbedPane.setMnemonicAt(numTabs - 1, mnemonic);
    }

    public void placeTabsOnLeft() {
        JTabbedPane tabbedPane = (JTabbedPane) getComponent(0);
        tabbedPane.setTabPlacement(SwingConstants.LEFT);
    }
}