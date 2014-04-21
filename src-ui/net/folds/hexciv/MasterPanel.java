package net.folds.hexciv;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class MasterPanel extends JPanel {
    Playable parent;
    JLabel  lblFile = new JLabel();
    JButton cmdOpen = new JButton("Open");
    JButton cmdSave = new JButton("Save");
    JButton cmdStart = new JButton("Start");

    public MasterPanel(Playable parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(100, 120));
        lblFile.setText("getMap.txt");
        this.add(lblFile);
        this.add(cmdOpen);
        this.add(cmdSave);
        this.add(cmdStart);
        cmdOpen.addMouseListener(new OpenButtonListener());
        cmdSave.addMouseListener(new SaveButtonListener());
        cmdStart.addMouseListener(new StartButtonListener());
    }

    public void paintComponent(Graphics comp) {
        super.paintComponent(comp);
        Graphics2D comp2D = (Graphics2D) comp;
        long h = getHeight() - 5; // available height for information
        long w = getWidth()  - 5; // available width  for information
        comp2D.drawRect(2, 2, (int) w, (int) h);

    }

    private class OpenButtonListener extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {
            parent.open();
        }
    }

    private class SaveButtonListener extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {
            parent.save();
        }
    }

    private class StartButtonListener extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {
            parent.startGame();
        }
    }
}