package net.folds.hexciv;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by jasper on Feb 10, 2014.
 */
public class TitlePanel extends Panel {
    JLabel  lblFile = new JLabel();
    JButton cmdOpen = new JButton("Open");
    JButton cmdSave = new JButton("Save");
    JButton cmdUndo = new JButton("↶");
    JButton cmdRedo = new JButton("↷");

    public TitlePanel(GameScreen parent) {
        super(parent);
        setPreferredSize(new Dimension(100, 120));
        lblFile.setText("getMap.txt");
        this.add(lblFile);
        this.add(cmdOpen);
        this.add(cmdSave);
        this.add(cmdUndo);
        this.add(cmdRedo);
        cmdOpen.addMouseListener(new OpenButtonListener());
        cmdSave.addMouseListener(new SaveButtonListener());
        cmdUndo.addMouseListener(new UndoButtonListener());
        cmdRedo.addMouseListener(new RedoButtonListener());
        cmdUndo.setToolTipText("Undo");
        cmdRedo.setToolTipText("Redo");
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

    private class UndoButtonListener extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {
            parent.undo();
        }
    }

    private class RedoButtonListener extends MouseAdapter {
        public void mouseReleased(MouseEvent e) {
            parent.redo();
        }
    }

    protected void enableUndo(boolean arg) {
        if (cmdUndo.isEnabled() != arg) {
            cmdUndo.setEnabled(arg);
            cmdUndo.repaint();
        }
    }

    protected void enableRedo(boolean arg) {
        if (cmdRedo.isEnabled() != arg) {
            cmdRedo.setEnabled(arg);
            cmdRedo.repaint();
        }
    }

    protected void setUndoText(String arg) {
        if (!cmdUndo.getToolTipText().equals(arg)) {
            cmdUndo.setToolTipText(arg);
        }
    }

    protected void setRedoText(String arg) {
        if (!cmdRedo.getToolTipText().equals(arg)) {
            cmdRedo.setToolTipText(arg);
        }
    }
}