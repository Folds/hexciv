package net.folds.hexciv;

import java.awt.*;
import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Feb 05, 2014.
 */
public class WorldMap {
    private Planet planet;
    protected IcosahedralMesh mesh;
    protected TerrainTypes[] terrain;
    private BitSet bonuses;
    private BitSet roads;
    private BitSet railroads;
    private BitSet irrigation;
    private BitSet villages;
    private BitSet cities;

    public WorldMap() {
        this(new Planet(), new IcosahedralMesh());
    }

    public WorldMap(Planet planet, IcosahedralMesh mesh) {
        this.planet = planet;
        this.mesh = mesh;
        int numCells = mesh.countCells();
        terrain = new TerrainTypes[numCells];
        bonuses = new BitSet(numCells);
        roads = new BitSet(numCells);
        railroads = new BitSet(numCells);
        irrigation = new BitSet(numCells);
        villages = new BitSet(numCells);
        cities = new BitSet(numCells);
        for(int i = 0; i < numCells; i++) {
            terrain[i] = TerrainTypes.ocean;
        }
        bonuses.clear(0, numCells);
        roads.clear(0, numCells);
        railroads.clear(0, numCells);
        irrigation.clear(0, numCells);
        villages.clear(0, numCells);
        cities.clear(0, numCells);
    }

    public WorldMap(MapBaggage baggage) {
        planet = new Planet(baggage.polarCircumferenceInKilometers);
        mesh = new IcosahedralMesh(baggage.meshSize);
        int numCells = mesh.countCells();
        terrain = Porter.stringsToTerrain(baggage.terrainStrings, numCells);
        bonuses = Porter.stringToBits(baggage.bonusString, numCells);
        roads = Porter.stringToBits(baggage.roadString, numCells);
        railroads = Porter.stringToBits(baggage.railroadString, numCells);
        irrigation = Porter.stringToBits(baggage.irrigationString, numCells);
        villages = Porter.stringToBits(baggage.villageString, numCells);
        cities = Porter.stringToBits(baggage.cityString, numCells);
    }

    // returns whether the request changed the state.
    protected boolean setTerrain(int cellId, TerrainTypes terrain) {
        if ((cellId >= 0) && (cellId < countCells())) {
            if (this.terrain[cellId] != terrain) {
                this.terrain[cellId] = terrain;
                return true;
            }
        }
        return false;
    }

    String stringifyBonuses() {
        return BitSetPorter.bitsToString(bonuses);
    }

    String stringifyRoads() {
        return BitSetPorter.bitsToString(roads);
    }

    String stringifyRailroads() {
        return BitSetPorter.bitsToString(railroads);
    }

    String stringifyIrrigation() {
        return BitSetPorter.bitsToString(irrigation);
    }

    String stringifyVillages() {
        return BitSetPorter.bitsToString(villages);
    }

    String stringifyCities() {
        return BitSetPorter.bitsToString(cities);
    }

    protected int getAdjacentCellId(int cellId, Directions dir) {
        return mesh.getNeighbor(cellId, dir);
    }

    public int numHexesFromEquatorToCenterOfPole() {
        return mesh.getDistanceInCellsBetweenPoles() / 2;
    }

    public int lengthOfEquatorInHexesAcrossFlats() {
        return mesh.getEquatorLength();
    }

    public int mapWidthInHexesAcrossFlats() {
        return lengthOfEquatorInHexesAcrossFlats() + 1;
    }

    public int mapHeightInHexSides() {
        return 3 * numHexesFromEquatorToCenterOfPole() + 2;
    }

    public int countCells() {
        return mesh.countCells();
    }

    public int getRow(int cellId) {
        return mesh.getRow(cellId);
    }

    public int getSegment(int cellId) {
        return mesh.getSegmentNumber(cellId);
    }

    public TerrainTypes getTerrain(int cellId) {
        return terrain[cellId];
    }

    int getOffset(int cellId) {
        if (mesh.isInOffsetRow(cellId)) {
            return 1;
        }
        return 0;
    }

    int getCellId(int row, int positionInRow) {
        return mesh.getCellId(row, positionInRow);
    }

    public int getHalfCol(int cellId) {
        int positionInRow = mesh.getPositionInRow(cellId);
        int offset = getOffset(cellId);
        int skippedCells = countSkippedCells(cellId);
        return 2*(positionInRow + skippedCells) + offset;
    }

    int countSkippedCells(int cellId) {
        int segment = mesh.getSegmentNumber(cellId);
        int row = mesh.getRow(cellId);
        int offset = 0;
        if (mesh.isOffsetRow(row)) {
            offset = 1;
        }
        int n = mesh.getDistanceInCellsBetweenPoles() / 5;
        if (row < 0) {
            return 0;
        }
        if (row < n)  {
            int skippedBeforeMap = (2*n - row) / 2;
            int smallGap = n - row;
            int bonusGap = n;
            int numBonusGaps = segment / 2;
            return skippedBeforeMap + segment * smallGap + numBonusGaps * bonusGap;
        }
        if (row < 2*n) {
            int skippedBeforeMap = (2*n - row) / 2;
            int gap = 2*n - row;
            return skippedBeforeMap + segment * gap;
        }
        if (row <= 3*n) {
            return 0;
        }
        if (row <= 4*n) {
            int skippedBeforeMap = 0;
            int gap = row - 3*n;
            return skippedBeforeMap + segment * gap;
        }
        if (row <= 5*n) {
            int skippedBeforeMap = (row - 4*n) / 2;
            int smallGap = row - 4*n;
            int bonusGap = n;
            int numBonusGaps = (segment + 1) / 2;
            return skippedBeforeMap + segment * smallGap + numBonusGaps * bonusGap;
        }
        return 0;
    }

    protected CellSnapshot getCellSnapshot(int cellId) {
        int row = getRow(cellId);
        int positionInRow = mesh.getPositionInRow(cellId);
        double area = getAreaInSquareKilometers(cellId);
        double diameter = getMeanDiameterInKilometers(cellId);
        double longitude = mesh.getLongitudeInDegrees(cellId);
        double latitude = mesh.getLatitudeInDegrees(cellId);
        if ((cellId >= 0) && (cellId <= countCells() - 1)) {
            Vector<Boolean> hasFeatures = getFeatures(cellId);
            String description = terrain[cellId].getDescription(Features.bonus.isChosen(hasFeatures));
            Color color = terrain[cellId].getColor();

            return new CellSnapshot(cellId, row, positionInRow, area, diameter, longitude, latitude,
                                    hasFeatures, description, color);
        }
        String description = "Void";
        Color color = new Color(0, 0, 0);

        return new CellSnapshot(cellId, row, positionInRow, area, diameter, longitude, latitude,
                                noFeatures(), description, color);
    }

    Vector<Boolean> noFeatures() {
        Vector<Boolean> result = new Vector<>(Features.count());
        for (int i = 0; i < Features.count(); i++) {
            result.add(false);
        }
        return result;
    }

    Vector<Boolean> getFeatures(int cellId) {
        if ((cellId < 0) || (cellId >= countCells())) {
            return noFeatures();
        }
        Vector<Boolean> result = new Vector<>(6);
        result.add(bonuses.get(cellId));
        result.add(roads.get(cellId));
        result.add(railroads.get(cellId));
        result.add(irrigation.get(cellId));
        result.add(villages.get(cellId));
        result.add(cities.get(cellId));
        return result;
    }

    protected boolean setFeatures(int cellId, Vector<Boolean> features) {
        if (getFeatures(cellId).equals(features)) {
            return false;
        }
        bonuses.set(   cellId, features.get(0));
        roads.set(     cellId, features.get(1));
        railroads.set( cellId, features.get(2));
        irrigation.set(cellId, features.get(3));
        villages.set(  cellId, features.get(4));
        cities.set(cellId, features.get(5));         return true;        
    }

    double getAreaInSquareKilometers(int cellId) {
        if ((cellId < 0) || (cellId >= mesh.countCells())) {
            return 0;
        }
        if (mesh.isPentagon(cellId)) {
            return getAreaOfHexagonalCellInSquareKilometers() * 5.0 / 6.0;
        } else {
            return getAreaOfHexagonalCellInSquareKilometers();
        }
    }

    double getMeanDiameterInKilometers(int cellId) {
        double cellArea = getAreaInSquareKilometers(cellId);
        return planet.getMeanDiameterInKilometers(cellArea);
    }

    double getAreaOfHexagonalCellInSquareKilometers() {
        return planet.areaInSquareKilometers() / (mesh.countCells() - 2);
    }

    double getPolarCircumferenceInKilometers() {
        return planet.polarCircumferenceInKilometers();
    }

    int getMeshSize() {
        return mesh.getMeshSize();
    }
}