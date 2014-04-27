package net.folds.hexciv;

import java.awt.*;
import java.util.BitSet;
import java.util.Random;
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
    private BitSet mines;
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
        mines = new BitSet(numCells);
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
        mines = Porter.stringToBits(baggage.mineString, numCells);
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

    String stringifyMines() {
        return BitSetPorter.bitsToString(mines);
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
        result.add(mines.get(cellId));
        result.add(villages.get(cellId));
        result.add(cities.get(cellId));
        return result;
    }

    protected boolean setFeatures(int cellId, Vector<Boolean> features) {
        if (getFeatures(cellId).equals(features)) {
            return false;
        }
        bonuses.set(   cellId, features.get(0));
        roads.set(cellId, features.get(1));
        railroads.set(cellId, features.get(2));
        irrigation.set(cellId, features.get(3));
        mines.set(cellId, features.get(4));
        villages.set(cellId, features.get(5));
        cities.set(cellId, features.get(6));
        return true;
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

    double getAreaOfHexagonalCellInSquareKilometers() {
        return planet.areaInSquareKilometers() / (mesh.countCells() - 2);
    }

    double getMeanDiameterInKilometers(int cellId) {
        double cellArea = getAreaInSquareKilometers(cellId);
        return planet.getMeanDiameterInKilometers(cellArea);
    }

    protected static WorldMap getEarthMap() {
        return Porter.importMapFromString(WorldMap.earthString());
    }

    int getMeshSize() {
        return mesh.getMeshSize();
    }

    double getPolarCircumferenceInKilometers() {
        return planet.polarCircumferenceInKilometers();
    }

    int getDistanceInCells(int cellId1, int cellId2) {
        return mesh.getDistanceInCells(cellId1, cellId2);
    }

    Vector<Integer> getNeighbors(int cellId) {
        return mesh.getNeighbors(cellId);
    }

    boolean hasBonus(int cellId) {
        return bonuses.get(cellId);
    }

    boolean hasIrrigation(int cellId) {
        return irrigation.get(cellId);
    }

    boolean hasMine(int cellId) {
        return mines.get(cellId);
    }

    boolean hasRoad(int cellId) {
        return roads.get(cellId);
    }

    boolean hasRailroad(int cellId) {
        return railroads.get(cellId);
    }

    protected void setBonus(int cellId) {
        if ((cellId >= 0) && (cellId < countCells())) {
            bonuses.set(cellId);
        }
    }

    protected int randomCell() {
        return mesh.randomCell();
    }

    protected Vector<Integer> getRegion(int cellId, int radius) {
        return mesh.getRegion(cellId, radius);
    }

    protected Vector<Integer> getContinent(int cellId) {
        if (!isLand(cellId)) {
            return null;
        }
        Vector<Integer> increment = new Vector<Integer>(6);
        increment.add(cellId);
        Vector<Integer> results = new Vector<Integer>(7);
        results.addAll(increment);
        while(increment.size() > 0) {
            Vector<Integer> nextIncrement = new Vector<Integer>(6);
            for (int edgeCellId : increment) {
                Vector<Integer> possibilities = getEarthMap().getNeighbors(edgeCellId);
                for (int possibility : possibilities) {
                    if (isLand(possibility)) {
                        if(   (!increment.contains(possibility))
                           && (!results.contains(possibility))
                           && (!nextIncrement.contains(possibility))
                          ) {
                            nextIncrement.add(possibility);
                        }
                    }
                }
            }
            increment.clear();
            increment.addAll(nextIncrement);
            nextIncrement.clear();
            results.addAll(increment);
        }
        return results;
    }

    protected boolean isLand(int cellId) {
        TerrainTypes terrain = getTerrain(cellId);
        return terrain.isLand();
    }

    protected static String earthString() {
        return "{\n" +
            "  \"polarCircumferenceInKilometers\" : 40000.0,\n" +
            "  \"meshSize\" : 12,\n" +
            "  \"terrainStrings\" : [ \"T^1zV1!1W2H2%1Cb^3@a!1$3#i#Zg@ZZq!Zw@p@Zb@o@Zf@m!1@Za!4#o!1@Za@3#s1#TZb@4@v@h" +
                "!r@1!4@t!2#Zf!3@z@h@1!l!5!2@3!Z2@g@2!n@4!1@Z4!1!2!g$4@h!91!T1!Zr#1!n#2^1!Z5!4!i@2@p@1!2!1!1!Zc$g#" +
                "1!4!m@2#Z8!7!r!n$ZZ5!l!7#Z6!11T#2!5!h!3!k$5@Z8@4!k!1!1!k!1!1!6@Z6@2#4!i!1#k#2!Zd$5@i!4#g%Zf!7!g@1" +
                "1T!1#4!e@1@1!Zd!1$f&1!5!2!e!1@1@Zc!1$1@c@1$1#1#1$6!8!3@Zw@1!4!1@1@i1!T5@ZZ4!1!8!6!7@1@Zb!u@2!b!5@" +
                "4#p!g$2!1!f!6!6!2!3!2#5!4@Z1!r@4#5!3@11!T8!1!2#x!7#h$1!1!2!6^6%3!Z1@1#j%5@2%1@6$1#Z2!j!3!6!2@1#" +
                "1!2%1!1!u@u1@T1^1$1$3@1@t@6!g#3@2%1%4@2#t!7@3!9@4!1!2%3Du!4@2!h!2!1^2*2!r@2@1$71$T5!1!2@2#1!4%p!" +
                "1#1$9$1@5@5!5#2!2!g@1#1@2@9!1!1#k#e!3!1!1@2!b@1@k@51!T4!3#1#b#1!k!1!1#2%3@3!8!3!i@b!1!1!4!2!2@g!" +
                "6$1!1@6$8%1%1!2$7#2^2#11@d1%6#1*1^4(1$1\"," +
             " \"e$3B1&1%1E1P1W1Y3@T1zW2P2zA3N3zE3M4z(2$3O4zA2#4Q4zB3#3Q6H1R5#2T6H3S1!2@2U7H4L1%1@2#11WT7G2!3L3$" +
                "1!3z@7G7!2H4^1!1z#8G3!3@1Idz%1!1!4G2@4!2Kbz&9G7@1M7z^3!7H18T@1N5z^1!1!8F8K3$4z^1!1#1$3E3!4K4@1@" +
                "4!1z@2@4!2!2D2!1!4I5!1@4@1z#1!12T#5F3!1@3G1!4!1!1zA2#7F3!7E1!5zEaE1!5#3!1E7zD9Cg!1&1^4!4zA1!7C2!" +
                "4!1cT^2^4!2zA2K2!1$1!2!2A3%4@2z*3S1!1#1$1!1$4$5@1z^4^1M2@2A5!9P1E6!2!11FT1^1%2@1#1@9!5!1K1D1!1!" +
                "1N2$3@1@1#2!3!dX1!1%3H4!1!2!2!1!1!7!dV1!1@21!T3J5%qR1!1@6I2#1^5!h!1Q3!6M3!g#5@1O1!9G3@3!c$2@4Pd#" +
                "1(5!3@5#eM2!1!91@T1Ba!6@cL3!4!7&n$5L2!6!4*8@1!a@1!6@1G2!3%2(3!3$2!2!1#1%3!1B8%1A8!11FT3%1$2@3@" +
                "1C3!2!1!1D3!1!3!6(1&1!5*1!1@1!3G1@2@1@2#2(1!1L4B2B1(3@2O3\"," +
             " \"sE1C3$2C4$2H5^3Jc!5!5@2Be@Zb!V8zT!z!Zh!Z1!Zb^1@Z1!p!m%1@1!Z2!l#1!p$1!Z6!2@Zd^Z7!2!j!2!ZZ7!3!l@ZZa!" +
                "1n5!ZZc!V1zT!o@4!ZZ5!r!3!ZZ7$m!2@ZZ3!1@3@m!2!ZZ4!7!l@2!ZZ3!z!ZZz#ZZm!4!7!ZZo!11T$1!2$ZZl*1!1^1!" +
                "1!ZZi&2%2%Z7!Z6^1&1!1#a!Zb!q$3$2#3$3!1!Z8@q$2!1@1!11T!3@1@4#2#z@o!2!3!5@4@4^Z1@2!o@8!1!1!1@1A4" +
                "!v@Z4(Z2!1!Z3!1$2@v!2!r!13T!4#Z4!6@i@1!2!1!6@z!o!5!1@c!r!u!1!Z3@4!2@m!x@4!2@b!x!4!1!3!Zb!6#51!LxB" +
                "1!2!4!4!2!4!3!5!1^4!2#9!2@e!1@2#9!c!2!1#3!2!3!8!2#e!1@k\"," +
             " \"f)4#b!7!5!e!p!w!V4OT!1!Za!p!Zf@o@Zb@v@Zk!p#Zd#5!q$Ze#y$j#mAy^i$mBz^h^lCZ2$j&1@iCZ3$1#1gT&2!l(Z6$" +
                "1#g*1@m*Z7Af*2!o@2!Z6Bh*2!r!Z6Ch^k#1!2!Z9Ee@1%q!3#1!Z2$4^1dT#1%i@1!1@2$2!Z3@2#2@g$1#1!g#1@1!" +
                "1!Za(i$2%e@3@Ze(f!1A1@g!2@Zd(cI7!17T!5@Za!1%eK6@6!1@1!1!Z9@kG9#6#3!Z8#kE1@1$5#4#4!Z6$6!fB2!2%3$" +
                "3$s!e#11T@1@1!fD3^3^1&1!k!d!1#nB1!1$3!2$1*y!1#4!iB1%2$1Az!1$3!j%2!3@2%1D2!1rT!1%1@j@1@7!1J1!1!q%" +
                "2#m!1!1%1G1!2!o%1!1#g#2!1&1I2!m%1%f&4@1Jm*5!e@11T@1@1@1!1Hl&1%1!d%1!2Ik^1%b!4%1!1B1^3!f&5@a!1!3$1F" +
                "4!b^1!5!9!3@1K2!19^#1#h@1K1!4!1^9!8#1Ii!2@3!1!2$1#1%s@2#u!ZZc\" ],\n" +
            "  \"bonusString\" : \"Y6Zf1zz&1P1zB1X1zK1Y38T2T1z$1!1L1zz!1N1Y1zT1N3zC1!2P2O1&1z*1S1!2M1$1zC1S1R1zA2#1@" +
                "2J2!2!11UT1zC2M1!1N1%1zL1F1#1T1%1!1z#1&1M1@1J1$1#1zH1H1*1P1zA1^2I1%2H1zK1@21#S1F1#1^1J1zI1$1L1@2D" +
                "1D1zzV1@1zE1zA1*1zzT2&1$1z(1zC2$1@1z*2%1zA1&22z&T1zE2#1@1zz@1^1(1zzP1z&1#1M1D1H1!1Z1zJ1O1#1#1N1#1z" +
                "D2@1!1!1K2zG1$1L12T@1M2!1M1@1^1G4*1#1V1@1I1E1@2!1&2zL1@1#1(1F1M1@2!1F1z^1!1E1*1P1!1B115@1z!1\",\n" +
            "  \"roadString\" : \"\",\n" +
            "  \"railroadString\" : \"\",\n" +
            "  \"irrigationString\" : \"\",\n" +
            "  \"mineString\" : \"\",\n" +
            "  \"villageString\" : \"\",\n" +
            "  \"cityString\" : \"\"\n" +
               "}";
    }

}