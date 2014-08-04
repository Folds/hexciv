package net.folds.hexciv;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Feb 12, 2014.
 */
public class InsetMap {
    IcosahedralMesh mesh;
    Vector<Integer> cellIds;
    int numRows;
    int numColumns;


    // Assumes numRows >= 1
    // Assumes numColumns >= 1
    // Assumes numRows is odd.

    InsetMap(IcosahedralMesh mesh, int centerCellId, int numRows, int numColumns) {
        this.mesh = mesh;
        cellIds = new Vector<>(numRows * numColumns);
        Util.initialize(cellIds, -1);
        this.numRows = numRows;
        this.numColumns = numColumns;
        setCellId(getCenterIndex(), centerCellId);
        Vector<Integer> coil = getCoil();
        populateCellIds(coil);
    }

    int countCells() {
        return numRows * numColumns;
    }

    int getIndex(int row, int column) {
        return row * numColumns + column;
    }

    int getRow(int index) {
        return index / numColumns;
    }

    int getColumn(int index) {
        return index % numColumns;
    }

    int getHalfCol(int row, int column) {
        int offset = getOffset(row);
        return 2 * column + offset;
    }

    void populateCellIds(Vector<Integer> coil) {
        for (int i=1; i < coil.size(); i++) {
            int firstNeighboringIndex = getNeighboringIndex(coil, i, -1);
            int secondNeighboringIndex = getNeighboringIndex(coil, i, firstNeighboringIndex);
            Vector<Integer> possibilities = getNeighboringCellIds(firstNeighboringIndex);
            Vector<Integer> confirmations = getNeighboringCellIds(secondNeighboringIndex);
            Vector<Integer> winnowed = crossCheck(possibilities, confirmations);
            int index = coil.get(i);
            if (index >= 0) {
                int chosenCellId = chooseCellId(index, firstNeighboringIndex, winnowed);
                cellIds.set(index, chosenCellId);
            }
        }
    }

    boolean alreadyIncludes(int cellId) {
        int index = cellIds.indexOf(cellId);
        if (index >= 0) {
            return true;
        }
        return false;
    }

    int getRowGivenCellId(int cellId) {
        int index = cellIds.indexOf(cellId);
        if (index >= 0) {
            return getRow(index);
        }
        return -1;
    }

    int getColumnGivenCellId(int cellId) {
        int index = cellIds.indexOf(cellId);
        if (index >= 0) {
            return getColumn(index);
        }
        return -1;
    }

    int chooseCellId(int index, int neighboringIndex, Vector<Integer> winnowed) {
        if (winnowed.size() <= 1) {
            for (int possibility : winnowed) {
                if (!alreadyIncludes(possibility)) {
                    return possibility;
                }
            }
        }
        if (winnowed.size() == 2) {
            if (true) {
                Directions dir = guessDirection(neighboringIndex, index);
                int possibility = winnowed.get(1);
                if (mesh.getNeighbor(cellIds.get(neighboringIndex), dir) == possibility) {
                    if (!alreadyIncludes(possibility)) {
                        return possibility;
                    }
                }
            }
            for (int possibility : winnowed) {
                if (!alreadyIncludes(possibility)) {
                    return possibility;
                }
            }
        }
        if ((neighboringIndex >=0) && (index >= 0)) {
            Directions dir = guessDirection(neighboringIndex, index);
            int neighboringCellId = cellIds.get(neighboringIndex);
            if ((neighboringCellId >= 0) && (neighboringCellId < mesh.countCells())) {
                int possibility = mesh.getNeighbor(neighboringCellId, dir);
                if (!alreadyIncludes(possibility)) {
                    return possibility;
                }
            }
        }
        for (int possibility : winnowed) {
            if (!alreadyIncludes(possibility)) {
                return possibility;
            }
        }
        return -1;
    }

    Directions guessDirection(int fromIndex, int toIndex) {
        int fromCell;
        if (fromIndex >= 0) {
            fromCell = cellIds.get(fromIndex);
        } else {
            fromCell = -1;
        }
        int toCell;
        if (toIndex >= 0) {
            toCell = cellIds.get(toIndex);
        } else {
            toCell = -1;
        }
        if ((fromCell >= 0) && (toCell >= 0)) {
            Vector<Integer> path = mesh.proposePath(fromCell, toCell);
            if (path.size() == 0) {
                return Directions.none;
            }
            int first = path.get(1);
            for (Directions dir : Directions.values()) {
                if (mesh.getNeighbor(fromCell, dir) == first) {
                    return dir;
                }
            }
        }
        Directions dir = guessDirectionIgnoringRotation(fromIndex, toIndex);
        int rotation = guessRotationInDegrees(fromIndex);
        return dir.rotate(rotation);
    }

    Directions guessDirectionIgnoringRotation(int fromIndex, int toIndex) {
        int fromRow = getRow(fromIndex);
        int toRow = getRow(toIndex);
        int fromCol = getColumn(fromIndex);
        int toCol = getColumn(toIndex);
        if (fromRow == toRow) {
            if (fromCol < toCol) {
                return Directions.east;
            } else {
                return Directions.west;
            }
        }
        if (fromRow < toRow) {
            if (fromCol < toCol) {
                if ((toCol - fromCol) > 2 * (toRow - fromRow)) {
                    return Directions.east;
                } else return Directions.northeast;
            } else {
                if ((fromCol - toCol) > 2 * (toRow - fromRow)) {
                    return Directions.west;
                } else return Directions.northwest;
            }
        }
        if (fromCol < toCol) {
            if ((toCol - fromCol) > 2 * (fromRow - toRow)) {
                return Directions.east;
            } else return Directions.southeast;
        } else {
            if ((fromCol - toCol) > 2 * (fromRow - toRow)) {
                return Directions.west;
            } else return Directions.southwest;
        }
    }

    // returns the amount of CCW rotation required for the cell at index
    // to line up normally.  This is a guess to the extent that:
    // * the pole location(s) are uncertain
    // * the sector boundaries have off-by-one errors.
    int guessRotationInDegrees(int index) {
        int northPoleIndex = guessNorthPoleIndex();
        int southPoleIndex = guessSouthPoleIndex();
        if ((northPoleIndex < 0) && (southPoleIndex < 0)) {
            return 0;
        }
        int row = getRow(index);
        int col = getColumn(index);
        if (northPoleIndex >= 0) {
            if (index == northPoleIndex) {
                return 0;
            }
            int poleRow = getRow(northPoleIndex);
            int poleCol = getColumn(northPoleIndex);
            if (row >= poleRow) {
                if (col > poleCol) {
                    if ((row - poleRow) > 2 * (col - poleCol)) {
                        return 180;
                    } else {
                        return 240;
                    }
                } else {
                    if ((row - poleRow) > 2 * (poleCol - col)) {
                        return 180;
                    } else {
                        return 120;
                    }
                }
            }
        }
        if (southPoleIndex >= 0) {
            if (index == southPoleIndex) {
                return 0;
            }
            int poleRow = getRow(southPoleIndex);
            int poleCol = getColumn(southPoleIndex);
            if (row <= poleRow) {
                if (col > poleCol) {
                    if ((poleRow - row) > 2 * (col - poleCol)) {
                        return 180;
                    } else {
                        return 120;
                    }
                } else {
                    if ((poleRow - row) > 2 * (poleCol - col)) {
                        return 180;
                    } else {
                        return 240;
                    }
                }
            }
        }
        if ((northPoleIndex >= 0) && (southPoleIndex >= 0)) {
            if (row >= (getRow(northPoleIndex) + getRow(southPoleIndex))/2) {
                int poleRow = getRow(northPoleIndex);
                int poleCol = getColumn(northPoleIndex);
                if (col > poleCol) {
                    if ((poleRow - row) < 2 * (col - poleCol)) {
                        return 300;
                    } else {
                        return 0;
                    }
                } else {
                    if ((poleRow - row) < 2 * (poleCol - col)) {
                        return 60;
                    } else {
                        return 0;
                    }
                }
            } else {
                int poleRow = getRow(southPoleIndex);
                int poleCol = getColumn(southPoleIndex);
                if (col > poleCol) {
                    if ((row - poleRow) < 2 * (col - poleCol)) {
                        return 300;
                    } else {
                        return 0;
                    }
                } else {
                    if ((row - poleRow) < 2 * (poleCol - col)) {
                        return 60;
                    } else {
                        return 0;
                    }
                }
            }
        }
        if (northPoleIndex >= 0) {
            int poleRow = getRow(northPoleIndex);
            int poleCol = getColumn(northPoleIndex);
            if (col > poleCol) {
                if ((poleRow - row) < 2 * (col - poleCol)) {
                    return 300;
                } else {
                    return 0;
                }
            } else {
                if ((poleRow - row) < 2 * (poleCol - col)) {
                    return 60;
                } else {
                    return 0;
                }
            }
        } else {
            int poleRow = getRow(southPoleIndex);
            int poleCol = getColumn(southPoleIndex);
            if (col > poleCol) {
                if ((row - poleRow) < 2 * (col - poleCol)) {
                    return 300;
                } else {
                    return 0;
                }
            } else {
                if ((row - poleRow) < 2 * (poleCol - col)) {
                    return 60;
                } else {
                    return 0;
                }
            }
        }
    }

    int guessNorthPoleIndex() {
        int poleCellId = mesh.countCells() - 1;
        return guessPoleIndex(poleCellId);
    }

    int guessSouthPoleIndex() {
        int poleCellId = 0;
        return guessPoleIndex(poleCellId);
    }

    // Assumes poleCellId is either 0 or mesh.countCells() - 1.
    int guessPoleIndex(int poleCellId) {
        if (alreadyIncludes(poleCellId)) {
            return cellIds.indexOf(poleCellId);
        }
        int cenRow = getCenterRow();
        int cenCellId = cellIds.get(getCenterIndex());
        int cellsToPole = mesh.getDistanceInCells(poleCellId, cenCellId);
        int poleRow;
        if (poleCellId == 0) {
            poleRow = cenRow - cellsToPole;     // South Pole
        } else {
            poleRow = cenRow + cellsToPole;     // North Pole
        }
        if ((poleRow < 0) || (poleRow >= numRows)) {
            return -1;
        }
        int cenCol = getCenterColumn();
        return getIndex(poleRow, cenCol);
    }

    Vector<Integer> crossCheck(Vector<Integer> possibilities, Vector<Integer> confirmations) {
        Vector<Integer> result = new Vector<>(6);
        if (possibilities.size() == 0) {
            for (int cellId : confirmations) {
                if (cellId >= 0) {
                    result.add(cellId);
                }
            }
        } else if (confirmations.size() == 0) {
            for (int cellId : possibilities) {
                if (cellId >= 0) {
                    result.add(cellId);
                }
            }
        } else {
            for (int cellId : confirmations) {
                if ((possibilities.contains(cellId)) && (cellId >= 0)) {
                    result.add(cellId);
                }
            }
        }
        result.trimToSize();
        return result;
    }


    Vector<Integer> getNeighboringCellIds(int index) {
        if (index >= 0) {
            int cellId = cellIds.get(index);
            if ((cellId < 0) || (cellId >= mesh.countCells())) {
                return new Vector<Integer>(0);
            }
            return mesh.getNeighbors(cellId);
        } else {
            return new Vector<Integer>(0);
        }

    }

    int getNeighboringIndex(Vector<Integer> coil, int i, int indexToIgnore) {
        Vector<Integer> neighboringIndexes = getNeighboringIndexes(coil.get(i));
        for (int index : neighboringIndexes) {
            int pos = coil.indexOf(index);
            if ((pos >= 0) && (pos < i) && (pos != coil.indexOf(indexToIgnore))) {
                return index;
            }
        }
        return -1;
    }

    Vector<Integer> getNeighboringIndexes(int index) {
        Vector<Integer> result = new Vector<Integer>(6);
        int row = getRow(index);
        int col = getColumn(index);
        int offset = getOffset(row);
        if (row < numRows - 1) {
            if (col + offset > 0) {
                result.add(getIndex(row + 1, col + offset - 1));
            }
            if (col + offset < numColumns - 1) {
                result.add(getIndex(row + 1, col + offset));
            }
        }
        if (col > 0) {
            result.add(getIndex(row, col - 1));
        }
        if (col < numColumns - 1) {
            result.add(getIndex(row, col + 1));
        }
        if (row > 0) {
            if (col + offset > 0) {
                result.add(getIndex(row - 1, col + offset - 1));
            }
            if (col + offset < numColumns - 1) {
                result.add(getIndex(row - 1, col + offset));
            }
        }
        result.trimToSize();
        return result;
    }

    // returns a list of indexes (to the cellIds vector),
    // proposing an order in which to set the cellIds.
    // indexes < 0 are invalid.
    Vector<Integer> getCoil() {
        int numCells = countCells();
        Vector<Integer> result = new Vector<>(numCells);
        Util.initialize(result, -2);

        int i = 0;
        int j = 0;
        int ring = 0;

        while (i < numCells) {
            int index = tryToGetIndex(ring, j);
            if (index >= 0) {
                result.set(i, index);
                i++;
            }
            j = j + 1;
            if (j >= 6 * ring) {
                ring = ring + 1;
                j = 0;
            }
            if (ring > numRows + numColumns) {
                return result;
            }
        }

        return result;
    }

    // assumes ring >= 0
    // assumes 0 <= j <= max(0, 6*ring - 1)
    int tryToGetIndex(int ring, int j) {
        if ((ring < 0) || (j < 0)) {
            return -1;
        }
        if (ring == 0) {
            return getCenterIndex();
        }
        if (j >= 6 * ring) {
            return -1;
        }
        int cenRow = getCenterRow();
        int cenCol = getCenterColumn();
        int row;
        int col;
        if (j < ring) {
            row = cenRow + ring - j - 1;
            col = cenCol + ring - (row + cenRow) / 2 + cenRow;
        } else if (j < 2 * ring) {
            row = cenRow + ring - j - 1;
            col = cenCol + ring + (row + cenRow + 1)/2 - cenRow;
        } else if (j < 3 * ring) {
            row = cenRow - ring;
            col = cenCol - ring / 2 + 3 * ring - 1 - j;
        } else if (j < 4 * ring) {
            row = cenRow - 4 * ring + j + 1;
            col = cenCol - ring - (row + cenRow) / 2 + cenRow;
        } else if (j < 5 * ring) {
            row = cenRow - 4 * ring + j + 1;
            col = cenCol - ring + (row + cenRow + 1) / 2 - cenRow;
        } else {
            row = cenRow + ring;
            col = cenCol + (ring + 1) / 2 + j + 1 - 6 * ring;
        }
        if ((row < 0) || (row >= numRows) || (col < 0) || (col >= numColumns)) {
            return -1;
        }
        return getIndex(row, col);
    }

    int getCenterIndex() {
        int row = numRows / 2;
        int column = (numColumns - 1) / 2;
        return getIndex(row, column);
    }

    int getCenterRow() {
        return numRows / 2;
    }

    int getCenterColumn() {
        return (numColumns - 1) / 2;
    }

    void setCellId(int index, int cellId) {
        cellIds.set(index, cellId);
    }

    int getCellId(int index) {
        return cellIds.get(index);
    }

    void setCellId(int row, int column, int cellId) {
        int index = getIndex(row, column);
        cellIds.set(index, cellId);
    }

    int getCellId(int row, int column) {
        int index = getIndex(row, column);
        if (index < 0) {
            return index;
        }
        return cellIds.get(index);
    }

    int getOffset(int row) {
        return (row + getCenterRow() + 1) % 2;
    }

    int getNeighboringCellIdIgnoringRotation(int index, Directions dir) {
        int cellId = cellIds.get(index);
        int rotation = guessRotationInDegrees(index);
        int minusRotation = 360 - rotation;
        Directions dirMinusRotation = dir.rotate(minusRotation);
        return mesh.getNeighbor(cellId, dirMinusRotation);
    }

    protected int countSlashes() {
        if (numRows == 0) {
            return numColumns;
        }
        return numColumns + (numRows - 1) / 2;
    }

    protected int countWhacks() {
        return countSlashes();
    }

    protected int getMaxRowOfSlash(int slash) {
        if (slash < getMinSlash() + numColumns) {
            return numRows - 1;
        }
        int centerRow = getCenterRow();
        int adjustment = (centerRow + 1) % 2;
        return 2 * numColumns - 2 * slash - 1 - adjustment;
    }

    protected int getMaxRowOfWhack(int whack) {
        if (whack > countWhacks() - numColumns) {
            return numRows - 1;
        }
        int centerRow = getCenterRow();
        int adjustment = centerRow % 2;
        return 2 * whack + adjustment;
    }

    protected int getMinRowOfSlash(int slash) {
        if (slash >= 0) {
            return 0;
        }
        int centerRow = getCenterRow();
        int adjustment = (centerRow + 1) % 2;
        return -2 * slash - adjustment;
    }

    protected int getMinRowOfWhack(int whack) {
        if (whack < numColumns) {
            return 0;
        }
        int centerRow = getCenterRow();
        int adjustment = (centerRow + 1) % 2;
        return 2 * (whack - numColumns) + adjustment;
    }

    protected int getIndexFromSlash(int slash, int row) {
        int centerRow = getCenterRow();
        int adjustment = (centerRow + 1) % 2;
        int col = slash + (row + adjustment) / 2;
        return getIndex(row, col);
    }

    protected int getIndexFromWhack(int whack, int row) {
        int centerRow = getCenterRow();
        int adjustment = centerRow % 2;
        int col = whack - (row + adjustment) / 2;
        return getIndex(row, col);
    }

    protected int getMinSlash() {
        return numColumns - countSlashes();
    }

    protected int getMaxSlash() {
        return numColumns - 1;
    }

    protected int getMinWhack() {
        return 0;
    }

    protected int getMaxWhack() {
        return countWhacks() - 1;
    }
}