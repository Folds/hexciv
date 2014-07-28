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
    private BitSet potentialFarms;
    private WorldMap map;
    private int numCells;

    protected Planner(Civilization civ, WorldMap map) {
        this.civ = civ;
        this.map = map;
        numCells = map.countCells();
        proposedCitySites = new BitSet(numCells);
        proposedNonCitySites = new BitSet(numCells);
        potentialFarms = new BitSet(numCells);
    }

    protected void accept(int cellId) {
        proposedCitySites.set(cellId);
        proposedNonCitySites.set(cellId, false);
        Vector<Integer> region = map.getRegion(cellId, 2);
        for (int potentialFarm : region) {
            if (potentialFarm != cellId) {
                potentialFarms.set(potentialFarm);
            }
        }
    }

    protected void buffer(int cellId, int radius) {
        Vector<Integer> region = map.getRegion(cellId, radius);
        for (int neighbor : region) {
            if (neighbor != cellId) {
                reject(neighbor);
            }
        }
    }

    protected void cacheSite(int cellId, CivPlayer ruler) {
        if (   (cellId < 0) || (cellId >= numCells)
                || (proposedCitySites.get(cellId)) || (proposedNonCitySites.get(cellId))
                ) {
            return;
        }
        if (ruler.isGoodLocationForNewCity(map, cellId)) {
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

    protected boolean hasPriorityUnseenCell(Vector<Integer> cellIds, BitSet seenCells) {
        BitSet desiredCells = (BitSet) potentialFarms.clone();
        desiredCells.andNot(seenCells);
        for (int cellId : cellIds) {
            if (desiredCells.get(cellId)) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasRejected(int cellId) {
        if ((cellId < 0) || (cellId >= numCells)) {
            return true;
        }
        return proposedNonCitySites.get(cellId);
    }

    protected boolean isPotentialFarm(int cellId) {
        return potentialFarms.get(cellId);
    }

    protected boolean isPotentialFarmOfUnbuiltCity(int cellId) {
        if (!potentialFarms.get(cellId)) {
            return false;
        }
        if (map.hasCity(cellId)) {
            return false;
        }
        Vector<Integer> region = map.getRegion(cellId, 2);
        for (int neighborId : region) {
            if ((neighborId != cellId) && (proposedCitySites.get(neighborId)) && (!map.hasCity(neighborId))) {
                return true;
            }
        }
        return false;
    }

    protected void reject(int cellId) {
        proposedCitySites.set(cellId, false);
        proposedNonCitySites.set(cellId);
    }
}
