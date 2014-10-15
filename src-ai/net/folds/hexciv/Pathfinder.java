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

    protected class DetourBuilder {
        int fromCellId;
        boolean ccw;
        int prevCellId;
        int prevGap;
        int i;
        boolean hasLoop;
        boolean isDetourComplete;
        int retraceCellId;

        protected DetourBuilder(Vector<Integer> path, int i, boolean ccw) {
            this.fromCellId = -1;
            if (path.size() > 0) {
                this.fromCellId = path.get(0);
            }
            this.ccw = ccw;
            prevCellId = -1;
            prevGap = -1;
            if (i < path.size()) {
                prevCellId = path.get(i - 1);
                prevGap = path.get(i);
            }
            this.i = i;
            hasLoop = false;
            isDetourComplete = false;
            retraceCellId = -1;
        }

        protected void extendDetour(Vector<Integer> path, Vector<Integer> detour) {
            Directions gapDirection = map.getDirection(prevCellId, prevGap);
            Directions newDirection = getNeighborDirection(gapDirection);
            int neighbor;
            if (newDirection == Directions.none) {
                neighbor = -1;
            } else {
                neighbor = map.getAdjacentCellId(prevCellId, newDirection);
            }
            int rotationIncrement = -60; // in degrees CCW
            if (ccw) {
                rotationIncrement = 60;
            }
            prevGap = map.getAdjacentCellId(prevCellId, newDirection.rotate(-rotationIncrement));
            if (neighbor < 0) {
                hasLoop = true;
            }
            if (!hasLoop) {
                hasLoop = hasLoop(detour, neighbor);
            }
            updateRetraceCellId(path, detour, i, neighbor);
            if (!hasLoop) {
                detour.add(neighbor);
            }
            if (!hasLoop) {
                isDetourComplete = isDetourComplete(path, i, neighbor);
            }
            prevCellId = neighbor;
        }

        protected Directions getNeighborDirection(Directions avoidDir) {
            Directions result;
            int rotationIncrement = -60; // in degrees CCW
            if (ccw) {
                rotationIncrement =  60;
            }
            for (int index = 1; index < 6; index++) {
                result = avoidDir.rotate(rotationIncrement * index);
                int neighbor = map.getAdjacentCellId(prevCellId, result);
                if (seenCells.get(neighbor) && areOnSameContinent(fromCellId, neighbor)) {
                    return result;
                }
            }
            return Directions.none;
        }

        protected boolean hasLoop(Vector<Integer> detour, int neighbor) {
            if (detour.size() > 0) {
                for (int detourCellId : detour) {
                    if (detourCellId == neighbor) {
                        return true;
                    }
                }
            }
            return false;
        }

        protected boolean hasRetrace() {
            if (retraceCellId >= 0) {
                return true;
            }
            return false;
        }

        protected boolean isDetourComplete(Vector<Integer> path, int i, int neighbor) {
            for (int j = path.size() - 1; j > i; j--) {
                if (path.get(j) == neighbor) {
                    return true;
                }
            }
            return false;
        }

        protected void updateRetraceCellId(Vector<Integer> path, Vector<Integer> detour, int i, int neighbor) {
            for (int j = 0; j < i; j++) {
                if ((hasRetrace()) && (path.get(j) == retraceCellId)) {
                    break;
                }
                if (path.get(j) == neighbor) {
                    if (   (j == path.size() - 1)
                            || (detour.size() < 1)
                            || (path.get(j + 1) != detour.get(detour.size() - 1))
                            ) {
                        retraceCellId = neighbor;
                        break;
                    }
                }
            }
        }

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
            advance(path, i);
        }
        removeExtraSteps(path);
        path.trimToSize();
        return path;
    }

    protected void advance(Vector<Integer> path, int i) {
        if (path.size() == 0) {
            return;
        }
        int fromCellId = path.get(0);
        if (!seenCells.get(path.get(i)) || !areOnSameContinent(fromCellId, path.get(i))) {
            Vector<Integer> leftDetour = new Vector<Integer>();
            Vector<Integer> rightDetour = new Vector<Integer>();

            DetourBuilder leftBuilder  = new DetourBuilder(path, i, true);
            DetourBuilder rightBuilder = new DetourBuilder(path, i, false);

            while (  (!leftBuilder.hasLoop || !rightBuilder.hasLoop)
                   && !leftBuilder.isDetourComplete && !rightBuilder.isDetourComplete) {
                leftBuilder.extendDetour(path, leftDetour);
                rightBuilder.extendDetour(path, rightDetour);
            }
            if (!leftBuilder.hasLoop && leftBuilder.isDetourComplete) {
                insertDetour(path, leftDetour, i);
                if (leftBuilder.hasRetrace()) {
                    removeRetrace(path, leftBuilder.retraceCellId);
                }
            } else if (!rightBuilder.hasLoop && rightBuilder.isDetourComplete) {
                insertDetour(path, rightDetour, i);
                if (rightBuilder.hasRetrace()) {
                    removeRetrace(path, rightBuilder.retraceCellId);
                }
            }
            if (leftBuilder.hasLoop && rightBuilder.hasLoop) {
                path.removeAllElements();
            }
        }
    }

    protected void insertDetour(Vector<Integer> path, Vector<Integer> detour, int i) {
        int junctionId = detour.get(detour.size() - 1);
        while ((i < path.size()) && (path.get(i) != junctionId)) {
            path.remove(i);
        }
        if (path.capacity() < path.size() + detour.size() - 1) {
            path.ensureCapacity(path.size() + detour.size() - 1);
        }
        for (int k = 0; k < detour.size() - 1; k++) {
            path.add(i + k, detour.get(k));
        }
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

    protected void removeExtraSteps(Vector<Integer> path) {
        for (int i = 2; i < path.size(); i++) {
            int beforeId = path.get(i - 2);
            int afterId = path.get(i);
            if (map.getDistanceInCells(beforeId, afterId) == 1) {
                path.remove(i - 1);
                i = i - 1;
            }
        }
    }

    protected void removeRetrace(Vector<Integer> path, int retraceCellId) {
        int j;
        for (j = 0; j < path.size(); j++) {
            if (path.get(j) == retraceCellId) {
                break;
            }
        }
        for (j++; j < path.size(); j++) {
            if (path.get(j) == retraceCellId) {
                path.remove(j);
                break;
            }
            path.remove(j);
            j = j - 1;
        }
    }

}
