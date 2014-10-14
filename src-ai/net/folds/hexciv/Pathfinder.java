package net.folds.hexciv;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Oct 08, 2014.
 */
public class Pathfinder {
    private WorldMap map;
    private BitSet seenCells;
    Vector<BitSet> continents;

    protected Pathfinder(WorldMap map, BitSet seenCells, Vector<BitSet> continents) {
        this.map = map;
        this.seenCells = seenCells;
        this.continents = continents;
    }

    protected Pathfinder(WorldMap map, BitSet seenCells) {
        this.map = map;
        this.seenCells = seenCells;
        continents = new Vector<BitSet>();
        populateContinents();
    }

    protected void populateContinents() {
        if (continents.size() > 0) {
            return;
        }
        if (seenCells.cardinality() <= 0) {
            return;
        }
        int numCells = map.countCells();
        for (int cellId = 0; cellId < numCells; cellId++) {
            if (seenCells.get(cellId) && map.isLand(cellId)) {
                Vector<Integer> neighbors = map.getNeighbors(cellId);
                int cellContinent = -1;
                boolean isNewContinent = true;
                for (int neighbor : neighbors) {
                    if (seenCells.get(neighbor) && map.isLand(neighbor)) {
                        int neighborContinent = getContinentNumber(neighbor);
                        if (neighborContinent >= 0) {
                            isNewContinent = false;
                            if (cellContinent < 0) {
                                cellContinent = neighborContinent;
                                continents.get(neighborContinent).set(cellId);
                            } else if (cellContinent != neighborContinent) {
                                if (cellContinent < neighborContinent) {
                                    continents.get(cellContinent).or(continents.get(neighborContinent));
                                    continents.remove(neighborContinent);
                                } else {
                                    continents.get(neighborContinent).or(continents.get(cellContinent));
                                    continents.remove(cellContinent);
                                }
                            }
                        }
                    }
                }
                if (isNewContinent) {
                    BitSet newContinent = new BitSet(numCells);
                    newContinent.set(cellId);
                    continents.add(newContinent);
                }
            }
        }
        continents.trimToSize();
    }

    protected boolean areOnSameContinent(int firstCellId, int secondCellId) {
        if ((!map.isLand(firstCellId)) || (!map.isLand(secondCellId))) {
            return false;
        }
        int firstCont = getContinentNumber(firstCellId);
        if (firstCont < 0) {
            return false;
        }
        int secondCont = getContinentNumber(secondCellId);
        if (secondCont < 0) {
            return false;
        }
        if (firstCont == secondCont) {
            return true;
        }
        return false;
    }

    protected int getContinentNumber(int cellId) {
        if (!map.isLand(cellId)) {
            return -1;
        }
        int numContinents = continents.size();
        for (int i = 0; i < numContinents; i++) {
            if (continents.get(i).get(cellId)) {
                return i;
            }
        }
        return -1;
    }

    int getLandDistanceInCells(int fromCellId, int toCellId) {
        Vector<Integer> path = proposeLandPath(fromCellId, toCellId);
        if (path.size() > 0) {
            return path.size() - 1;
        }
        return -1;
    }

    Vector<Integer> proposeLandPath(int fromCellId, int destinationCellId) {
        if (!areOnSameContinent(fromCellId, destinationCellId)) {
            return new Vector<Integer>(0);
        }
        Vector<Integer> path = map.proposePath(fromCellId, destinationCellId);
        for (int i = 1; i < path.size(); i++) {
            if (!seenCells.get(path.get(i)) || !areOnSameContinent(fromCellId, path.get(i))) {
                Vector<Integer> leftDetour = new Vector<Integer>();
                Vector<Integer> rightDetour = new Vector<Integer>();
                boolean hasLeftLoop = false;
                boolean hasRightLoop = false;
                boolean hasLeftRetrace = false;
                boolean hasRightRetrace = false;
                boolean isLeftDetourComplete = false;
                boolean isRightDetourComplete = false;
                int prevLeft = path.get(i - 1);
                int prevRight = path.get(i - 1);
                int prevLeftGap = path.get(i);
                int prevRightGap = path.get(i);
                int leftRetraceCellId = -1;
                int rightRetraceCellId = -1;
                while ((!hasLeftLoop || !hasRightLoop) && !isLeftDetourComplete && !isRightDetourComplete) {
                    Directions leftOf = map.getDirection(prevLeft, prevLeftGap);
                    int leftNeighbor = -1;
                    int leftIndex;
                    for (leftIndex = 1; leftIndex < 6; leftIndex++) {
                        Directions left = leftOf.rotate(60 * leftIndex);
                        leftNeighbor = map.getAdjacentCellId(prevLeft, left);
                        if (seenCells.get(leftNeighbor) && areOnSameContinent(fromCellId, leftNeighbor)) {
                            break;
                        }
                    }
                    prevLeftGap = map.getAdjacentCellId(prevLeft, leftOf.rotate(60 * (leftIndex - 1)));
                    if (leftNeighbor < 0) {
                        hasLeftLoop = true;
                    }
                    if (!hasLeftLoop) {
                        if (leftDetour.size() > 0) {
                            for (int detourCellId : leftDetour) {
                                if (detourCellId == leftNeighbor) {
                                    hasLeftLoop = true;
                                    break;
                                }
                            }
                        }
                    }
                    for (int j = 0; j < i; j++) {
                        if ((hasLeftRetrace) && (path.get(j) == leftRetraceCellId)) {
                            break;
                        }
                        if (path.get(j) == leftNeighbor) {
                            if (   (j == path.size() - 1)
                                    || (leftDetour.size() < 1)
                                    || (path.get(j + 1) != leftDetour.get(leftDetour.size() - 1))
                                    ) {
                                hasLeftRetrace = true;
                                leftRetraceCellId = leftNeighbor;
                                break;
                            }
                        }
                    }
                    if (!hasLeftLoop) {
                        leftDetour.add(leftNeighbor);
                    }
                    if (!hasLeftLoop) {
                        for (int j = path.size() - 1; j > i; j--) {
                            if (path.get(j) == leftNeighbor) {
                                isLeftDetourComplete = true;
                                break;
                            }
                        }
                    }
                    Directions rightOf = map.getDirection(prevRight, prevRightGap);
                    int rightNeighbor = -1;
                    int rightIndex;
                    for (rightIndex = 1; rightIndex < 6; rightIndex++) {
                        Directions right = rightOf.rotate(-60 * rightIndex);
                        rightNeighbor = map.getAdjacentCellId(prevRight, right);
                        if (seenCells.get(rightNeighbor) && areOnSameContinent(fromCellId, rightNeighbor)) {
                            break;
                        }
                    }
                    prevRightGap = map.getAdjacentCellId(prevRight, rightOf.rotate(-60 * (rightIndex - 1)));
                    if (rightNeighbor < 0) {
                        hasRightLoop = true;
                    }
                    if (!hasRightLoop) {
                        if (rightDetour.size() > 0) {
                            for (int detourCellId : rightDetour) {
                                if (detourCellId == rightNeighbor) {
                                    hasRightLoop = true;
                                    break;
                                }
                            }
                        }
                    }
                    for (int j = 0; j < i; j++) {
                        if ((hasRightRetrace) && (path.get(j) == rightRetraceCellId)) {
                            break;
                        }
                        if (path.get(j) == rightNeighbor) {
                            if (   (j == path.size() - 1)
                                    || (rightDetour.size() < 1)
                                    || (path.get(j + 1) != rightDetour.get(rightDetour.size() - 1))
                                    ) {
                                hasRightRetrace = true;
                                rightRetraceCellId = rightNeighbor;
                                break;
                            }
                        }
                    }
                    if (!hasRightLoop) {
                        rightDetour.add(rightNeighbor);
                    }
                    if (!hasRightLoop) {
                        for (int j = path.size() - 1; j > i; j--) {
                            if (path.get(j) == rightNeighbor) {
                                isRightDetourComplete = true;
                                break;
                            }
                        }
                    }
                    prevLeft = leftNeighbor;
                    prevRight = rightNeighbor;
                }
                if (!hasLeftLoop && isLeftDetourComplete) {
                    int junctionId = leftDetour.get(leftDetour.size() - 1);
                    while ((i < path.size()) && (path.get(i) != junctionId)) {
                        path.remove(i);
                    }
                    if (path.capacity() < path.size() + leftDetour.size() - 1) {
                        path.ensureCapacity(path.size() + leftDetour.size() - 1);
                    }
                    for (int k = 0; k < leftDetour.size() - 1; k++) {
                        path.add(i + k, leftDetour.get(k));
                    }
                    if (hasLeftRetrace) {
                        int j;
                        for (j = 0; j < path.size(); j++) {
                            if (path.get(j) == leftRetraceCellId) {
                                break;
                            }
                        }
                        for (j++; j < path.size(); j++) {
                            if (path.get(j) == leftRetraceCellId) {
                                path.remove(j);
                                break;
                            }
                            path.remove(j);
                            j = j - 1;
                        }
                    }
                } else if (!hasRightLoop && isRightDetourComplete) {
                    int junctionId = rightDetour.get(rightDetour.size() - 1);
                    while ((i < path.size()) && (path.get(i) != junctionId)) {
                        path.remove(i);
                    }
                    if (path.capacity() < path.size() + rightDetour.size() - 1) {
                        path.ensureCapacity(path.size() + rightDetour.size() - 1);
                    }
                    for (int k = 0; k < rightDetour.size() - 1; k++) {
                        path.add(i + k, rightDetour.get(k));
                    }
                    if (hasRightRetrace) {
                        int j;
                        for (j = 0; j < path.size(); j++) {
                            if (path.get(j) == rightRetraceCellId) {
                                break;
                            }
                        }
                        for (j++; j < path.size(); j++) {
                            if (path.get(j) == rightRetraceCellId) {
                                path.remove(j);
                                break;
                            }
                            path.remove(j);
                            j = j - 1;
                        }
                    }
                }
                if (hasLeftLoop && hasRightLoop) {
                    return new Vector<Integer>(0);
                }
            }
        }
        for (int i = 2; i < path.size(); i++) {
            int beforeId = path.get(i - 2);
            int afterId = path.get(i);
            if (map.getDistanceInCells(beforeId, afterId) == 1) {
                path.remove(i - 1);
                i = i - 1;
            }
        }
        path.trimToSize();
        return path;
    }

}
