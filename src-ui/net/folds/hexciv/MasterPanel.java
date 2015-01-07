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
    JLabel  lblYear = new JLabel();
    String strContinue;
    JButton cmdContinue = new JButton();
    int counter;
    int maxCount;
    int divisor;

    public MasterPanel(Playable parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(100, 120));
        lblFile.setText("getMap.txt");
        this.add(lblFile);
        this.add(cmdOpen);
        this.add(cmdSave);
        this.add(lblYear);
        this.add(cmdStart);
        strContinue = "Go on…";
        cmdContinue.setText(strContinue);
        counter = -1;
        divisor = 20; // 1000 milliseconds / 50 milliseconds per timer activation
        // int countdownFrom = 30;
        int countdownFrom = 5;
        maxCount = countdownFrom * divisor;
        this.add(cmdContinue);
        cmdOpen.addMouseListener(new OpenButtonListener());
        cmdSave.addMouseListener(new SaveButtonListener());
        cmdStart.addMouseListener(new StartButtonListener());
        cmdContinue.addMouseListener(new ContinueButtonListener());
        cmdContinue.setVisible(false);
    }

    public void resync() {
        boolean isPaused = parent.isPaused();
        if (isPaused) {
            if (counter < 0) {
                counter = maxCount;
            } else {
                counter = counter - 1;
            }
            if (counter == 0) {
                if (maxCount == 0) {
                    // prevent infinite loops
                    maxCount = 2;
                }
                parent.unPause();
                return;
            }
        } else {
            counter = -1;
        }
        if (counter > 0) {
            cmdContinue.setText((1 + (counter - 1)/divisor) + "…");
        } else {
            cmdContinue.setText("Go on…");
        }
        cmdContinue.setVisible(isPaused);
        if (cmdStart.isVisible()) {
            if (isPaused) {
                cmdStart.setVisible(false);
            }
        }
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
            cmdOpen.setVisible(false);
        }
    }

    private class ContinueButtonListener extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {
            parent.unPause();
        }
    }
}