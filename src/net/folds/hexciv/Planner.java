package net.folds.hexciv;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on May 31, 2014.
 */
public class Planner {
    private Civilization civ;
    private BitSet proposedCitySites;
    private BitSet proposedNonCitySites;
    private WorldMap map;
    private int numCells;

    protected Planner(Civilization civ, WorldMap map) {
        this.civ = civ;
        this.map = map;
        numCells = map.countCells();
        proposedCitySites = new BitSet(numCells);
        proposedNonCitySites = new BitSet(numCells);
    }

    protected void accept(int cellId) {
        proposedCitySites.set(cellId);
        proposedNonCitySites.set(cellId, false);
    }

    protected void buffer(int cellId, int radius) {
        Vector<Integer> region = map.getRegion(cellId, radius);
        for (int neighbor : region) {
            if (neighbor != cellId) {
                reject(neighbor);
            }
        }
    }

    protected void cacheSite(int cellId) {
        if (   (cellId < 0) || (cellId >= numCells)
                || (proposedCitySites.get(cellId)) || (proposedNonCitySites.get(cellId))
                ) {
            return;
        }
        if (civ.isGoodLocationForNewCity(map, cellId)) {
            accept(cellId);
        }
        reject(cellId);
    }

    protected boolean hasAccepted(int cellId) {
        if ((cellId < 0) || (cellId >= numCells)) {
            return false;
        }
        return proposedCitySites.get(cellId);
    }

    protected boolean hasRejected(int cellId) {
        if ((cellId < 0) || (cellId >= numCells)) {
            return true;
        }
        return proposedNonCitySites.get(cellId);
    }

    protected void reject(int cellId) {
        proposedCitySites.set(cellId, false);
        proposedNonCitySites.set(cellId);
    }
}
