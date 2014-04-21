package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class GameState {
    GameListener parent;
    Vector<Civilization> civs;
    int turn;
    boolean isTurnInProgress;

    GameState(GameListener parent, int numCivilizations) {
        this.parent = parent;
        civs = new Vector<>(numCivilizations);
        for (int i = 0; i < numCivilizations; i++) {
            Civilization civ = new Civilization();
            civs.add(civ);
        }
        initialize();
    }

    protected void initialize() {
        turn = 0;
        isTurnInProgress = false;
        for (Civilization civ : civs) {
            civ.initialize();
        }
    }

    protected boolean isGameOver() {
        if (turn > 550) {
            return true;
        }
        return false;
    }

    protected void playTurn() {
        if (isTurnInProgress) {
            return;
        }
        isTurnInProgress = true;

        if (turn % 50 == 0) {
            parent.celebrateYear(getYear());
        }
        turn = turn + 1;
        isTurnInProgress = false;
    }

    protected int getYear() {
        if (turn <= 0)   { return -4004; }
        if (turn == 200) { return 1; }
        if (turn <= 250) { return 20 * turn - 4000; }
        if (turn <= 300) { return 10 * turn - 1500; }
        if (turn <= 350) { return  5 * turn; }
        if (turn <= 400) { return  2 * turn + 1050; }
        return turn + 1450;
    }
}
