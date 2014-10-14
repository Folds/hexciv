package net.folds.hexciv;

import java.util.Random;
import java.util.Vector;

/**
 * Created by Jasper on Feb 01, 2014.
 *
 * Contains code for:
 *  * Cell locations (in degrees, or using cell counts, but not using absolute distances)
 *  * Cell neighbors
 *  * Distances between cells (in degrees, or using cell counts, but not using absolute distances)
 *  * Paths between cells (the code is unaware of terrain types)
 */
public class IcosahedralMesh {
    int n; // half the length of an edge of the icosahedron, in cell diameters.
           // Counting the corners, each edge of the icosahedron contains parts of 2n+1 cells.
           // 12 of the cells in the mesh are pentagons; the remaining cells are hexagons.

    IcosahedralMesh() {
        this(12);
    }

    IcosahedralMesh(int n) {
        this.n = n;
    }

// How big is the mesh?

    int countCells() {
        return 30 * n * n + 2;
    }

    int countRows() {
        return 5 * n + 1;
    }

    int getMeshSize() {
        return n;
    }

    int getNumCellsInRow(int row) {
        if (row < 0) {
            return 0;
        }
        if (row == 0) {
            return 1;
        }
        if (row <= n) {
            return 6 * row;
        }
        if (row <= 2 * n) {
            return 3 * n + 3 * row;
        }
        if (row <= 3 * n) {
            return  9 * n;
        }
        if (row <= 4 * n) {
            return 18 *n - 3 * row;
        }
        if (row < 5 * n) {
            return 30 *n - 6 * row;
        }
        if (row == 5 * n) {
            return 1;
        }
        return 0;
    }

    double getRowLength(double row) {
        if (row <= 0) {
            return 0;
        }
        if (row <= n) {
            return 6 * row;
        }
        if (row <= 2 * n) {
            return 3 * n + 3 * row;
        }
        if (row <= 3 * n) {
            return  9 * n;
        }
        if (row <= 4 * n) {
            return 18 *n - 3 * row;
        }
        if (row < 5 * n) {
            return 30 *n - 6 * row;
        }
        return 0;
    }

    int getEquatorLength() {
        int equatorRow = countRows() / 2;
        return getNumCellsInRow(equatorRow);
    }

    int getDistanceInCellsBetweenPoles() {
        return getDistanceInCells(0, countCells());
    }

// Is the row or cell special?

    boolean isFirstCellInRow(int cellId) {
        if (getPositionInRow(cellId) == 0) {
            return true;
        }
        return false;
    }

    boolean isFirstCellInSegment(int cellId) {
        if (isFirstCellInRow(cellId)) {
            return true;
        }
        int row = getRow(cellId);
        int positionInRow = getPositionInRow(cellId);
        if (row < 0) {
            return false;
        }
        if (row == 0) {
            return true;
        }
        if (row < n) {
            if (positionInRow % row == 0) {
                return true;
            }
            return false;
        }
        if (row < 2*n) {
            if (positionInRow % (n + row) == 0) {
                return true;
            }
            return false;
        }
        if (row <= 3*n) {
            // true cases were already taken care of by IsFirstCellInRow().
            return false;
        }
        if (row <= 4*n) {
            int segmentWidth = 6*n - row;
            if ((((2*positionInRow + segmentWidth + 1) % (2*segmentWidth)) / 2) == 0) {
                return true;
            }
            return false;
        }
        if (row < 5*n) {
            if (positionInRow % (5*n - row) == 0) {
                return true;
            }
            return false;
        }
        if (row == 5*n) {
            return true;
        }
        return false;
    }

    int getSegmentNumber(int cellId) {
        int row = getRow(cellId);
        int positionInRow = getPositionInRow(cellId);
        if (row <= 0) {
            return 0;
        } else if (row < n) {
            return positionInRow / row;
        } else if (row < 2*n) {
            return positionInRow / (n + row);
        } else if (row <= 3*n) {
            return 0;
        } else if (row <= 4*n) {
            return (positionInRow * 6 + getNumCellsInRow(row) + 3) / (getNumCellsInRow(row) * 2);
        } else if (row < 5*n) {
            return positionInRow / (5*n - row);
        } else return 0;
    }

    boolean isLastCellInRow(int cellId) {
        return isFirstCellInRow(cellId + 1);
    }

    boolean isPentagon(int cellId) {
        int row = getRow(cellId);
        if ((row % n) != 0) {
            return false;
        }
        if (row == 0) {
            return false;
        }
        if (row == 5*n) {
            return false;
        }
        int positionInRow = getPositionInRow(cellId);
        if (row == n) {
            if ((positionInRow % (2*n)) == n) {
                return true;
            }
            return false;
        }
        if (row == 2*n) {
            if ((positionInRow % (3*n)) == 0) {
                return true;
            }
            return false;
        }
        if (row == 3*n) {
            if ((positionInRow % (3*n)) == (3*n/2)) {
                return true;
            }
            return false;
        }
        if (row == 4*n) {
            if ((positionInRow % (2*n)) == 0) {
                return true;
            }
            return false;
        }
        return false;
    }

    boolean isOffsetRow(int row) {
        if (row % 2 == 1) {
            return true;
        }
        return false;
    }

    boolean isInOffsetRow(int cellId) {
        int row = getRow(cellId);
        return isOffsetRow(row);
    }

// Where is the cell?

    int getRow(int cellId) {
        if (cellId <= 0) {
            return 0;
        }
        if (cellId >= countCells() - 1) {
            return 5 * n;
        }
        for (int j=0; j<5; j++) {
            if (cellId < getCellId((j+1)*n, 0)) {
                for(int i=j*n; i<(j+1)*n; i++) {
                    if (getCellId(i+1,0) > cellId) {
                        return i;
                    }
                }
                return 0;
            }
        }
        return 0;
    }

    int getPositionInRow(int cellId) {
        int row = getRow(cellId);
        return cellId - getCellId(row, 0);
    }

    double getLatitudeInDegrees(double row) {
        if (row <= 0) {
            return -90.0;
        }
        if (row <= n) {                 // 0 <   row    <= n
            double fraction = row / n;  // 0 < fraction <= 1;
            double sine = -1 + 0.2 * (fraction * fraction);
            return Degrees.arcsin(sine);
        }
        if (row <= 2*n) {
            double wedge = 2 * n - row;    // 0 <=  wedge   < n
            double fraction = wedge / n;   // 0 <= fraction < 1
            double sine = -0.3 - 0.1 * (6 * fraction - fraction * fraction);
            return Degrees.arcsin(sine);
        }
        if (row <  3*n) {
            double fraction = (row - 2.5 * n) / n; // -0.5 < fraction < 0.5
            double sine = 0.3 * fraction / 0.5;
            return Degrees.arcsin(sine);
        }
        if (row <  4*n) {
            double wedge = row - 3 * n;    // 0 <=  wedge   < n
            double fraction = wedge / n;   // 0 <= fraction < 1
            double sine = 0.3 + 0.1 * (6 * fraction - fraction * fraction);
            return Degrees.arcsin(sine);
        }
        if (row <  5*n) {
            double wedge = 5 * n - row;    // 0 <  wedge   <= n
            double fraction = wedge / n;   // 0 < fraction <= 1
            double sine = 1 - 0.2 * (fraction * fraction);
            return Degrees.arcsin(sine);
        }
        return 90.0;
    }

    double getLongitudeInDegrees(double row, double positionInRow) {
        double offset = getPositionOffset(row);
        double rowLength = getRowLength(row);
        if (rowLength == 0) {
            return 0;
        }
        double adjustedPosition = (positionInRow + offset) % rowLength;
        return 360.0 * adjustedPosition / rowLength;
    }

    double getLatitudeInDegrees(int cellId) {
        int row = getRow(cellId);
        return getLatitudeInDegrees((double) row);
    }

    double getLongitudeInDegrees(int cellId) {
        int row = getRow(cellId);
        int positionInRow = getPositionInRow(cellId);
        return getLongitudeInDegrees((double) row, (double) positionInRow);
    }

// Which cell includes a particular location?

    int getCellId(int row, int positionInRow) {
        if (row <= 0) {
            return 0;
        }
        if (row >= 5 * n) {
            return countCells() - 1;
        }
        int result = 0;
        for (int i=0; i<row; i++) {
            result = result + getNumCellsInRow(i);
        }
        if ((positionInRow >= 0) && (positionInRow < getNumCellsInRow(row))) {
            result = result + positionInRow;
        }
        return result;
    }

    int getCellId(double longitudeInDegrees, double latitudeInDegrees) {
        if (latitudeInDegrees <= -90.0) {
            return 0;
        }
        if (latitudeInDegrees >= 90.0) {
            return countCells() - 1;
        }
        double row = getRow(latitudeInDegrees);
        double positionOffset = getPositionOffset(row);
        double rowLength = getRowLength(row);
        double rawPosition = rowLength * longitudeInDegrees / 360.0;
        if (rowLength == 0) {
            if (latitudeInDegrees > 0) {
                return countCells() - 1;
            }
            return 0;
        }
        double adjustedPosition = (rawPosition - positionOffset) % rowLength;
        int positionInRow = (int) Math.floor(adjustedPosition);
        double deltaX = adjustedPosition - positionInRow;
        double deltaY = row % 2.0;
        int rowAdjustment = getRowAdjustment(deltaX, deltaY);
        int iRow = (int) Math.round(row) + rowAdjustment;
        return getCellId(iRow, 0) + positionInRow;
    }

    double getPositionOffset(double row) {
        double deltaY = row % 2.0;
        if (deltaY <= 1.0 / 3.0) {
            return 0;
        }
        if (deltaY <= 2.0 / 3.0) {
            return 1.5 * deltaY - 0.5;
        }
        if (deltaY < 4.0 / 3.0) {
            return 0.5;
        }
        if (deltaY < 5.0 / 3.0) {
            return 2.5 - 1.5 * deltaY;
        }
        return 0;
    }

    // assumes 0 <= deltaX <= 1
    // assumes 0 <= deltaY <= 2
    int getRowAdjustment(double deltaX, double deltaY) {
        double positionOffset = getPositionOffset(deltaY);
        if (deltaY < 1.0 / 3.0) {
            return 0;
        }
        if (deltaY < 0.5) {
            if (deltaX > 0.5) {
                return 0;
            } else if (deltaX <= 0.5 - positionOffset) {
                return 0;
            } else {
                return 1;
            }
        }
        if (deltaY < 2.0 / 3.0) {
            if (deltaX <= 0.5) {
                return 0;
            } else if (deltaX >= 1.5 - 2 * positionOffset) {
                return 0;
            } else {
                return -1;
            }
        }
        if (deltaY < 4.0 / 3.0) {
            return 0;
        }
        if (deltaY < 1.5) {
            if (deltaX <= 0.5) {
                return 0;
            } else if (deltaX >= 1.5 - 2 * positionOffset) {
                return 0;
            } else {
                return 1;
            }
        }
        if (deltaY < 5.0 / 3.0) {
            if (deltaX > 0.5) {
                return 0;
            } else if (deltaX < 0.5 - positionOffset) {
                return 0;
            } else {
                return -1;
            }
        }
        return 0;
    }

    double getRow(double latitudeInDegrees) {
        double sine = Math.sin(latitudeInDegrees * Math.PI / 180.0);
        if (sine <= -1.0) {
            return 0;
        }
        if (sine <= -0.8) {
            return n * Math.sqrt(5*(1+sine));
        }
        if (sine <= -0.3) {
            return n * (-1.0 + 3.0 * Math.sqrt(10.0 / 9.0 * (1.2 + sine)));
        }
        if (sine <= 0.3) {
            return n * ( 2.5 + 0.5 * sine / 0.3);
        }
        if (sine <= 0.8) {
            return n * ( 6.0 - 3.0 * Math.sqrt(10.0 / 9.0 * (1.2 - sine)));
        }
        if (sine <= 1.0) {
            return n * ( 5.0 - Math.sqrt(5*(1-sine)));
        }
        return 5 * n;
    }

// Get paths and/or distances between cells:

    double getDistanceInDegrees(int cellId1, int cellId2) {
        double longitude1 = getLongitudeInDegrees(cellId1);
        double  latitude1 =  getLatitudeInDegrees(cellId1);
        double longitude2 = getLongitudeInDegrees(cellId2);
        double  latitude2 =  getLatitudeInDegrees(cellId2);
        return Degrees.sphericalDistance(longitude1, latitude1, longitude2, latitude2);
    }

    int estimateDistanceInCells(int cellId1, int cellId2) {
        double distanceInDegrees = getDistanceInDegrees(cellId1, cellId2);
        int areaOfSphereInHexagonalCells = countCells() - 2;
        double areaOfSphereInSquareDegrees = 360.0 * 360.0 / Math.PI;
        double areaOfHexagonalCellInSquareDegrees = areaOfSphereInSquareDegrees / areaOfSphereInHexagonalCells;
        double areaOfHexagonalCellInCircularDegrees = areaOfHexagonalCellInSquareDegrees * 4.0 / Math.PI;
        double typicalCellDiameterInDegrees = Math.sqrt(areaOfHexagonalCellInCircularDegrees);
        double distanceInCells = distanceInDegrees / typicalCellDiameterInDegrees;
        if ((distanceInCells > 1.6) || (distanceInCells < 1.25)) {
            return (int) Math.round(distanceInCells);
        }
        return 2;
    }

    int getDistanceInCells(int cellId1, int cellId2) {
        Vector<Integer> path = proposePath(cellId1, cellId2);
        return path.size() - 1;
    }

    protected Vector<Integer> getRegion(int cellId, int radius) {
        if (radius < 0) {
            return null;
        }
        int numCells = 1 + 3 * radius * (radius + 1);
        if (numCells >= countCells()) {
            numCells = countCells();
        }
        Vector<Integer> results = new Vector<>(numCells);
        results.add(cellId);
        Vector<Integer> increment = new Vector<>(6 * radius);
        increment.add(cellId);
        Vector<Integer> prevIncrement = new Vector<>(6 * radius);
        for (int i = 0; i < radius; i++) {
            prevIncrement.clear();
            prevIncrement.addAll(increment);
            increment.clear();

            for (int edgeCell : prevIncrement) {
                Vector<Integer> possibilities = getNeighbors(edgeCell);
                for (Integer possibility : possibilities) {
                    if ((!results.contains(possibility)) && (!increment.contains(possibility))) {
                        increment.add(possibility);
                    }
                }
            }
            results.addAll(increment);
        }
        return results;
    }

    // Assumes 0 <= fromCellId <= countCells() - 1
    // Assumes 0 <=   toCellId <= countCells() - 1
    Vector<Integer> proposePath(int fromCellId, int toCellId) {
        int guessNumCells = (int) (estimateDistanceInCells(fromCellId, toCellId) * 1.25 + 1);
        Vector<Integer> result = new Vector<>(guessNumCells);
        int currentCellId = fromCellId;
        result.add(currentCellId);
        while (currentCellId != toCellId) {
            if (estimateDistanceInCells(currentCellId, toCellId) == 1) {
                currentCellId = toCellId;
            } else {
                Vector<Integer> neighbors = getNeighbors(currentCellId);
                Vector<Double> degreesToGo = new Vector<>(neighbors.size());
                for (int i=0; i<neighbors.size(); i++) {
                    int neighborCellId = neighbors.get(i);
                    double distanceInDegrees = getDistanceInDegrees(neighborCellId, toCellId);
                    degreesToGo.add(distanceInDegrees);
                }
                int bestNeighborId = neighbors.get(0);
                double shortestDistance = degreesToGo.get(0);
                for (int i=1; i<neighbors.size(); i++) {
                    if (degreesToGo.get(i) < shortestDistance) {
                        bestNeighborId = neighbors.get(i);
                        shortestDistance = degreesToGo.get(i);
                    }
                }
                currentCellId = bestNeighborId;
            }
            result.add(currentCellId);
        }
        result.trimToSize();
        return result;
    }

// Get all 5 or 6 neighbors of a cell:

    // The results are already sorted.
    Vector<Integer> getNorthPoleNeighbors() {
        Vector<Integer> result = new Vector<>(6);
        result.add(countCells() - 3);
        result.add(countCells() - 4);
        result.add(countCells() - 5);
        result.add(countCells() - 6);
        result.add(countCells() - 7);
        result.add(countCells() - 2);
        return result;
    }

    // The results are already sorted.
    Vector<Integer> getSouthPoleNeighbors() {
        Vector<Integer> result = new Vector<>(6);
        result.add(1);
        result.add(2);
        result.add(3);
        result.add(4);
        result.add(5);
        result.add(6);
        return result;
    }

    // assumes (isPentagon(cellId) == true)
    Vector<Integer> getPentagonNeighbors(int cellId) {
        int row = getRow(cellId);
        boolean isOffsetRow = isOffsetRow(row);
        int offsetTweak = 0;
        if (isOffsetRow) {
            offsetTweak = 1;
        }
        int southTweak = getSouthTweak(cellId);
        int northTweak = getNorthTweak(cellId);
        int numCellsInPrevRow = getNumCellsInRow(row - 1);
        int numCellsInRow = getNumCellsInRow(row);
        int numCellsInNextRow = getNumCellsInRow(row + 1);

        Vector<Integer> result = new Vector<>(5);
        if (isLastCellInRow(cellId)) {
            result.add(cellId - numCellsInRow + 1);  // East of cell
        } else {
            result.add(cellId + 1);                  // East of cell
        }
        if (isFirstCellInRow(cellId)) {
            // row is either 2*n or 4*n
            // row is not offset
            result.add(cellId + numCellsInRow);
            result.add(cellId + numCellsInRow - 1);  // West of cell
            result.add(cellId - numCellsInPrevRow);
            if (row == 4*n) {
                result.add(cellId - 1);                  // Southwest of cell
            } else {
                // row == 2*n
                result.add(cellId + numCellsInRow + numCellsInNextRow - 1); // Northwest of cell
            }
            return result;
        }
        result.add(cellId - 1);                      // West of cell
        int positionInRow = getPositionInRow(cellId);
        if (row > 2*n) {
            result.add(cellId - numCellsInPrevRow + southTweak + offsetTweak - 1);   // Southwest of cell
            result.add(cellId - numCellsInPrevRow + southTweak + offsetTweak);       // Southeast of cell
            if ((row == 4*n) && (n == 1)) {
                result.add(countCells() - 1);                           // North of cell
            } else {
                result.add(cellId + numCellsInRow - northTweak + offsetTweak); // North of cell
            }
            return result;
        } else {
            int northwest;
            if (row == 2*n) {
                // row is not offset
                northwest = cellId + numCellsInRow - 1;
            } else {
                // row == n
                northwest = cellId + numCellsInRow + northTweak;
            }
            result.add(northwest);
            result.add(northwest + 1);
            if ((row == n) && (n == 1)) {
                result.add(0);                                        // South of cell
            } else {
                result.add(cellId - numCellsInPrevRow - southTweak);  // South of cell
            }
            return result;
        }
    }

    int getNorthTweak(int cellId) {
        int row = getRow(cellId);
        int positionInRow = getPositionInRow(cellId);
        if ((row <= 0) || (row >= 5*n)) {
            return 0;
        } else if (row == n - 1) {
            return getSegmentNumber(cellId) / 2;
        } else if ((row == 2*n - 1) && (n != 1)) {
            return 0;
        } else if (row == 3*n) {
            return (2*positionInRow + 3*n + 1) / (6*n);
        } else if (row == 4*n) {
            return positionInRow / n;
        }
        return getSegmentNumber(cellId);
    }

    int getSouthTweak(int cellId) {
        int row = getRow(cellId);
        int positionInRow = getPositionInRow(cellId);
        if ((row <= 0) || (row >= 5*n)) {
            return 0;
        } else if (row == n) {
            return positionInRow / n;
        } else if (row == 2*n) {
            return positionInRow / (3*n);
        } else if ((row == 3*n + 1) && (n != 1)) {
            return 0;
        } else if ((row == 4*n + 1) && (n != 1)) {
            return (1 + getSegmentNumber(cellId)) / 2;
        } else return getSegmentNumber(cellId);
    }

    // This code could have lots of off-by-one errors.
    // Fortunately, every result for meshes with n=1 and n=12 is regression tested.
    Vector<Integer> getNeighbors(int cellId) {
        int row = getRow(cellId);
        boolean isOffsetRow = isOffsetRow(row);
        int offsetTweak = 0;
        if (isOffsetRow) {
            offsetTweak = 1;
        }
        int southTweak = getSouthTweak(cellId);
        int northTweak = getNorthTweak(cellId);
        int numCellsInPrevRow = getNumCellsInRow(row - 1);
        int numCellsInRow = getNumCellsInRow(row);
        int numCellsInNextRow = getNumCellsInRow(row + 1);
        if (isPentagon(cellId)) {
            return getPentagonNeighbors(cellId);
        }
        if (cellId == 0) {
            return getSouthPoleNeighbors();
        }
        if (cellId == countCells() - 1) {
            return getNorthPoleNeighbors();
        }

        Vector<Integer> result = new Vector<>(6);
        if (isFirstCellInRow(cellId)) {
            result.add(cellId + 1);                          // East of cell
            result.add(cellId + numCellsInRow - 1);
            result.add(cellId + numCellsInRow);
            result.add(cellId - numCellsInPrevRow);
            if (row < 2*n) {
                result.add(cellId + numCellsInRow + 1);      // Northeast of cell
                result.add(cellId + numCellsInRow + numCellsInNextRow - 1); // West of cell
            } else if (row < 4*n) {
                if (isOffsetRow) {
                    result.add(cellId + numCellsInRow + 1);      // Northeast of cell
                    result.add(cellId - numCellsInPrevRow + 1);  // Southeast of cell
                } else {
                    result.add(cellId + numCellsInRow + numCellsInNextRow - 1); // Northwest of cell
                    result.add(cellId - 1);                          // Southwest of cell
                }
            } else {
                result.add(cellId - 1);                      // West of cell
                result.add(cellId - numCellsInPrevRow + 1);  // Southeast of cell
            }
            return result;
        }
        if (isLastCellInRow(cellId)) {
            result.add(cellId - 1);                          // West of cell
            result.add(cellId - numCellsInRow + 1);
            if (row <= 2*n) {
                int northwest = cellId + numCellsInNextRow - 1;
                result.add(northwest);
                result.add(northwest + 1);
                result.add(cellId - numCellsInRow - numCellsInPrevRow + 1); // Southeast of cell
                if (row == 1) {
                    result.add(northwest - 1);
                } else {
                    result.add(cellId - numCellsInRow);              // Southwest of cell
                }
            } else if (row < 4*n) {
                result.add(cellId + numCellsInNextRow);
                result.add(cellId - numCellsInRow);
                if (isOffsetRow) {
                    result.add(cellId + 1);                      // Northeast of cell
                    result.add(cellId - numCellsInRow - numCellsInPrevRow + 1); // Southeast of cell
                } else {
                    result.add(cellId + numCellsInNextRow - 1);  // Northwest of cell
                    result.add(cellId - numCellsInRow - 1);      // Southwest of cell
                }
            } else {
                int southwest = cellId - numCellsInRow - 1;
                result.add(cellId + 1);
                result.add(southwest + 1);
                result.add(southwest);
                if (isFirstCellInSegment(cellId)) {
                    result.add(southwest - 1);
                } else {
                    result.add(cellId + numCellsInNextRow);      // Northwest of cell
                }
            }
            return result;
        }
        result.add(cellId - 1); // West of cell
        result.add(cellId + 1); // East of cell
        int positionInRow = getPositionInRow(cellId);
        if (row < n) {
            int northwest = cellId + numCellsInRow + southTweak;
            int southeast = cellId - numCellsInPrevRow - southTweak;
            result.add(northwest);
            result.add(northwest + 1);
            result.add(southeast);
            if (isFirstCellInSegment(cellId)) {
                result.add(cellId + numCellsInRow + southTweak - 1);  // Southwest of cell
            } else {
                result.add(southeast - 1);
            }
        } else if (row < 2*n) {
            int northwest = cellId + numCellsInRow + northTweak;
            if ((row == 2*n - 1) && (n != 1)) {
                northwest = cellId + numCellsInRow + southTweak;
            }
            int southeast = cellId - numCellsInPrevRow - southTweak;
            result.add(northwest);
            result.add(northwest + 1);
            result.add(southeast);
            if (isFirstCellInSegment(cellId)) {
                result.add(northwest - 1);
            } else {
                result.add(southeast - 1);
            }
        } else if (row < 3*n) {
            int southwest = cellId - numCellsInPrevRow - southTweak + offsetTweak - 1;
            int northeast = cellId + numCellsInRow + offsetTweak;
            result.add(northeast - 1);
            result.add(northeast);
            result.add(southwest);
            result.add(southwest + 1);
        } else if (row < 4*n) {
            int southwest;
            if (row == 3*n) {
                southwest = cellId - numCellsInPrevRow + offsetTweak - 1;
            } else {
                southwest = cellId - numCellsInPrevRow + northTweak + offsetTweak - 1;
            }
            int northeast = cellId + numCellsInRow - northTweak + offsetTweak;
            if (isFirstCellInSegment(cellId)) {
                result.add(southwest - 1);
            } else {
                result.add(northeast - 1);
            }
            result.add(northeast);
            result.add(southwest);
            result.add(southwest + 1);
        } else if (row < 5*n) {
            int southwest;
            if (row == 4*n) {
                southwest = cellId - numCellsInPrevRow + southTweak - 1;
            } else {
                southwest = cellId - numCellsInPrevRow + northTweak;
            }
            if (isFirstCellInSegment(cellId)) {
                result.add(southwest - 1);
            } else {
                result.add(cellId + numCellsInRow - northTweak - 1);            // Northwest of cell
            }
            if (row == 5*n - 1) {
                result.add(countCells() - 1);                                   // North of cell
            } else {
                result.add(cellId + numCellsInRow - northTweak);                // Northeast of cell
            }
            result.add(southwest);
            result.add(southwest + 1);
        }
        return result;
    }

// Get a single neighboring cell:

    protected int getNeighbor(int cellId, Directions dir) {
        if (cellId > countCells() - 1) {
            return countCells() - 1;
        }
        if (cellId < 0) {
            return 0;
        }
        Vector<Integer> sortedNeighbors = getSortedNeighbors(cellId);
        if (dir == Directions.none) {
            return cellId;
        } else {
            return sortedNeighbors.get(dir.getValue() % 6);
        }
    }

    protected Directions getDirection(int fromCellId, int toCellId) {
        if (fromCellId > countCells() - 1) {
            return Directions.none;
        }
        if (fromCellId < 0) {
            return Directions.none;
        }
        if (toCellId > countCells() - 1) {
            return Directions.none;
        }
        if (toCellId < 0) {
            return Directions.none;
        }
        Vector<Integer> sortedNeighbors = getSortedNeighbors(fromCellId);
        for (int i = 0; i < sortedNeighbors.size(); i++) {
            if (sortedNeighbors.get(i) == toCellId) {
                if (i == 0) {
                    return Directions.none.getDirection(6);
                }
                return Directions.none.getDirection(i);
            }
        }
        Vector<Integer> path = proposePath(fromCellId, toCellId);
        if (path.size() <= 1) {
            return Directions.none;
        }
        if (path.get(1) == toCellId) {
            return Directions.none;
        }
        return getDirection(fromCellId, path.get(1));
    }

    Vector<Integer> getSortedNeighbors(int cellId) {
        if (isPentagon(cellId)) {
            return getSortedPentagonNeighbors(cellId);
        }
        return getSortedHexagonNeighbors(cellId);
    }

    // assumes (isPentagon(cellId) == true)
    Vector<Integer> getSortedPentagonNeighbors(int cellId) {
        Vector<Integer> neighbors = getNeighbors(cellId);
        Vector<Integer> result   = new Vector<>(6);
        Util.initialize(result, -1);
        Vector<Integer> northRow = new Vector<>(2);
        Vector<Integer> sameRow  = new Vector<>(2);
        Vector<Integer> southRow = new Vector<>(2);
        northRow.trimToSize();
        southRow.trimToSize();
        int row = getRow(cellId);
        for (int neighbor : neighbors) {
            if (getRow(neighbor) > row) {
                northRow.add(neighbor);
            } else if (getRow(neighbor) == row) {
                sameRow.add(neighbor);
            } else {
                southRow.add(neighbor);
            }
        }
        if (northRow.size() == 1) {
            result.set(1, northRow.get(0));
            result.set(0, northRow.get(0));
            Vector<Integer> southwestToSoutheast = sortAdjacentCellsInSameRow(southRow);
            result.set(4, southwestToSoutheast.get(0));
            result.set(3, southwestToSoutheast.get(1));
        } else {
            result.set(3, southRow.get(0));
            result.set(4, southRow.get(0));
            Vector<Integer> northwestToNortheast = sortAdjacentCellsInSameRow(northRow);
            result.set(0, northwestToNortheast.get(0));
            result.set(1, northwestToNortheast.get(1));
        }
        Vector<Integer> westToEast = sortAdjacentCellsInSameRow(sameRow.get(0), cellId, sameRow.get(1));
        result.set(5, westToEast.get(0));
        result.set(2, westToEast.get(2));
        return result;
    }

    // assumes (isPentagon(cellId) == false)
    Vector<Integer> getSortedHexagonNeighbors(int cellId) {
        Vector<Integer> neighbors = getNeighbors(cellId);
        if ((cellId == 0) || (cellId == countCells() - 1)) {
            // The poles' neighbors are already sorted.
            return neighbors;
        }
        Vector<Integer> result   = new Vector<>(6);
        Util.initialize(result, -1);
        Vector<Integer> northRow = new Vector<>(3);
        Vector<Integer> sameRow  = new Vector<>(2);
        Vector<Integer> southRow = new Vector<>(3);
        int row = getRow(cellId);
        for (int neighbor : neighbors) {
            if (getRow(neighbor) > row) {
                northRow.add(neighbor);
            } else if (getRow(neighbor) == row) {
                sameRow.add(neighbor);
            } else {
                southRow.add(neighbor);
            }
        }
        northRow.trimToSize();
        southRow.trimToSize();
        if (northRow.size() == 2) {
            Vector<Integer> northwestToNortheast = sortAdjacentCellsInSameRow(northRow);
            result.set(0, northwestToNortheast.get(0));
            result.set(1, northwestToNortheast.get(1));
            Vector<Integer> westToEast = sortAdjacentCellsInSameRow(sameRow.get(0), cellId, sameRow.get(1));
            result.set(5, westToEast.get(0));
            result.set(2, westToEast.get(2));
            Vector<Integer> southwestToSoutheast = sortAdjacentCellsInSameRow(southRow);
            result.set(4, southwestToSoutheast.get(0));
            result.set(3, southwestToSoutheast.get(1));
            return result;
        }
        if (northRow.size() == 1) {
            result.set(1, northRow.get(0));
            Vector<Integer> westToEast = sortAdjacentCellsInSameRow(sameRow.get(0), cellId, sameRow.get(1));
            result.set(0, westToEast.get(0));
            result.set(2, westToEast.get(2));
            Vector<Integer> westToSoutheast = sortAdjacentCellsInSameRow(southRow);
            result.set(5, westToSoutheast.get(0));
            result.set(4, westToSoutheast.get(1));
            result.set(3, westToSoutheast.get(2));
            return result;
        } else {
            result.set(3, southRow.get(0));
            Vector<Integer> westToEast = sortAdjacentCellsInSameRow(sameRow.get(0), cellId, sameRow.get(1));
            result.set(4, westToEast.get(0));
            result.set(2, westToEast.get(2));
            Vector<Integer> westToNortheast = sortAdjacentCellsInSameRow(northRow);
            result.set(5, westToNortheast.get(0));
            result.set(0, westToNortheast.get(1));
            result.set(1, westToNortheast.get(2));
            return result;
        }
    }

    Vector<Integer> sortAdjacentCellsInSameRow(int cellId1, int cellId2) {
        Vector<Integer> result = new Vector<>(2);
        if (   (cellId1 + 1 == cellId2)
            || (cellId1     >  cellId2 + 1)) {
            result.add(cellId1);
            result.add(cellId2);
            return result;
        }
        result.add(cellId2);
        result.add(cellId1);
        return result;
    }

    Vector<Integer> sortIntegers(int a, int b, int c) {
        Vector<Integer> result = new Vector<>(3);
        if ((a <= b) && (a <= c)) {
            result.add(a);
            if (b <= c) {
                result.add(b);
                result.add(c);
            } else {
                result.add(c);
                result.add(b);
            }
        } else if (b <= c) {
            result.add(b);
            if (a <= c) {
                result.add(a);
                result.add(c);
            } else {
                result.add(c);
                result.add(a);
            }
        } else {
            result.add(c);
            if (a <= b) {
                result.add(a);
                result.add(b);
            } else {
                result.add(b);
                result.add(a);
            }
        }
        return result;
    }

    Vector<Integer> sortAdjacentCellsInSameRow(Vector<Integer> arg) {
        if (arg.size() == 3) {
            return sortAdjacentCellsInSameRow(arg.get(0), arg.get(1), arg.get(2));
        } else {
            return sortAdjacentCellsInSameRow(arg.get(0), arg.get(1));
        }
    }

    /** assumes that (after sorting) the three cell IDs will either:
     *  a)  be in sequential order without gaps, or
     *  b)  have exactly one gap, with the other two IDs adjacent to each other.
     *  If (b) is the case, assumes that the cells wrap around the dateline.
    **/
    Vector<Integer> sortAdjacentCellsInSameRow(int cellId1, int cellId2, int cellId3) {
        Vector<Integer> partialSort = sortIntegers(cellId1, cellId2, cellId3);
        int a = partialSort.get(0);
        int b = partialSort.get(1);
        int c = partialSort.get(2);
        if (a + 2 == c) {
            return partialSort;
        }
        Vector<Integer> result = new Vector<>(3);
        if (a + 1 < b) {
            result.add(b);
            result.add(c);
            result.add(a);
        } else {
            result.add(c);
            result.add(a);
            result.add(b);
        }
        return result;
    }

// Helpers for testing and/or debugging:

    public void printInfo(int cellId) {
        System.out.println("cellId="   + cellId + "; " +
                           "row="      + getRow(cellId) +"; " +
                           "posInRow=" + getPositionInRow(cellId) + ";" +
                           "rowLen="   + getNumCellsInRow(getRow(cellId)));
        System.out.println("  long="     + getLongitudeInDegrees(cellId) + ";" +
                             "lat="      +  getLatitudeInDegrees(cellId));
        Vector<Integer> neighbors = getNeighbors(cellId);
        Util.printList("  neighbors", neighbors);
    }

    public void printDistance(int fromCellId, int toCellId) {
        System.out.println("  From cell " + fromCellId + " "+
                               "to cell " +   toCellId + ":  "+
                           getDistanceInDegrees(fromCellId, toCellId) + " degrees");
    }

    public void printPath(int fromCellId, int toCellId) {
        Vector<Integer> path = proposePath(fromCellId, toCellId);
        for(int i=0; i<path.size();i++) {
            if (i > 0) {
                printDistance(path.get(i - 1), path.get(i));
            }
            printInfo(path.get(i));
        }
    }
      
    public void printNeighborhood(int cellId) {
        printInfo(cellId);
        Vector<Integer> neighbors = getNeighbors(cellId);
        for(int neighbor : neighbors) {
            printInfo(neighbor);
        }
    }

    boolean doesXhaveYasaNeighbor(int xCellId, int yCellId) {
        Vector<Integer> neighbors = getNeighbors(xCellId);
        return neighbors.contains(yCellId);
    }

    boolean doAllNeighborsOfXhaveXasaNeighbor(int xCellId) {
        Vector<Integer> neighbors = getNeighbors(xCellId);
        for (int neighbor : neighbors) {
            boolean isReciprocated = doesXhaveYasaNeighbor(neighbor, xCellId);
            if (!isReciprocated) {
                return false;
            }
        }
        return true;
    }

    Vector<Integer> listCellsWithUnreciprocatedNeighbors() {
        Vector<Integer> result = new Vector<>();
        for (int cellId = 0; cellId<countCells(); cellId++) {
            if (!doAllNeighborsOfXhaveXasaNeighbor(cellId)) {
                result.add(cellId);
            }
        }
        return result;
    }

    Vector<Integer> listCellsWithDuplicatedNeighbors() {
        Vector<Integer> result = new Vector<>();
        for (int cellId = 0; cellId<countCells(); cellId++) {
            Vector<Integer> neighbors = getNeighbors(cellId);
            if (Util.doesListContainDuplicates(neighbors)) {
                result.add(cellId);
            }
        }
        return result;
    }

    protected int randomCell() {
        Random random = new Random();
        return random.nextInt(countCells());
    }
}