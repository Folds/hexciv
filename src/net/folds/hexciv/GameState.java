package net.folds.hexciv;

import java.awt.*;
import java.util.BitSet;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class GameState implements ClaimReferee {
    GameListener parent;
    Vector<Civilization> civs;
    Vector<UnitType> unitTypes;
    Vector<GovernmentType> governmentTypes;
    ImprovementKey wonders;
    TechKey discoveries;
    WorldMap map;
    int turn;
    boolean isTurnInProgress;

    GameState(GameListener parent, WorldMap map, int numCivilizations) {
        this.parent = parent;
        this.map = map;
        civs = new Vector<>(numCivilizations);
        unitTypes = UnitType.getChoices();
        governmentTypes = GovernmentType.getChoices();
        TechTree techTree = TechTree.proposeTechs();
        discoveries = new TechKey(techTree);
        ImprovementVector improvementTypes = ImprovementVector.proposeImprovements();
        wonders = new ImprovementKey(improvementTypes);
        for (int i = 0; i < numCivilizations; i++) {
            Civilization civ = new Civilization(governmentTypes, unitTypes, techTree, improvementTypes);
            civ.setName(Civilization.proposeCivilizationName(i));
            civ.setRulerName(Civilization.proposeRulerName(i));
            civs.add(civ);
        }
        initialize();
    }

    protected void initialize() {
        turn = 0;
        isTurnInProgress = false;
        for (int cellId = 0; cellId < map.countCells(); cellId++) {
            TerrainTypes terrain = map.getTerrain(cellId);
            if (randomBonus(terrain)) {
                map.setBonus(cellId);
            }
        }
        int numCivilizations = civs.size();
        Vector<Integer> foreignLocations = new Vector<>(numCivilizations);
        for (Civilization civ : civs) {
            civ.initialize(map, foreignLocations, parent, this);
            foreignLocations.addAll(civ.getLocations());
            Collections.sort(foreignLocations);
            Util.deduplicate(foreignLocations);
        }
    }

    public Vector<Integer> chooseBestTradeLocations(City city, Vector<Integer> potentialTradeCities) {
        int homeMoney = city.civ.countMoney(map, city, this);
        int numUnfilledSlots = 3 - city.tradePartnerLocations.size();
        if (numUnfilledSlots < 0) {
            numUnfilledSlots = 0;
        }
        Vector<Integer> results = new Vector<Integer>(numUnfilledSlots + 1);
        Vector<Integer> values  = new Vector<Integer>(numUnfilledSlots + 1);
        if (numUnfilledSlots == 0) {
            return results;
        }
        Vector<Integer> potentialValues = new Vector<Integer>(potentialTradeCities.size());
        for (int i = 0; i < potentialTradeCities.size(); i++) {
            int partnerLocation = potentialTradeCities.get(i);
            if (partnerLocation == city.location) {
                potentialValues.add(0);
            } else if (map.hasCity(partnerLocation)) {
                int rawValue = countCityMoney(partnerLocation);
                int value = rawValue + homeMoney;
                if (!city.civ.hasCityAt(partnerLocation)) {
                    value = 2 * value;
                }
                if (!city.civ.areOnSameContinent(map, city.location, partnerLocation)) {
                    value = 2 * value;
                }
                potentialValues.add(rawValue);
            } else {
                potentialValues.add(0);
            }
        }
        for (int i = 0; i < potentialTradeCities.size(); i++) {
            if (potentialValues.get(i) > 0) {
                if ((values.size() < numUnfilledSlots) || (potentialValues.get(i) > values.get(numUnfilledSlots - 1))) {
                    for (int j = 0; j < results.size(); j++) {
                        if (values.get(j) < potentialValues.get(i)) {
                            results.add(j, potentialTradeCities.get(i));
                            values.add(j,  potentialValues.get(i));
                            break;
                        }
                    }
                    if ((results.size() == 0) || (potentialTradeCities.get(i) != results.get(results.size() - 1))) {
                        results.add(potentialTradeCities.get(i));
                        results.add(potentialValues.get(i));
                    }
                    while (results.size() > numUnfilledSlots) {
                        results.remove(numUnfilledSlots);
                    }
                    while (values.size() > numUnfilledSlots) {
                        values.remove(numUnfilledSlots);
                    }
                }
            }
        }
        results.trimToSize();
        return results;
    }

    public Vector<Integer> getKnownCityLocations(Civilization civ) {
        Vector<Integer> results = new Vector<>(civ.seenCells.cardinality() / 19 + 5);
        for (Civilization civilization : civs) {
            for (City city : civilization.cities) {
                if ((city.location >= 0) && (civ.seenCells.get(city.location))) {
                    results.add(city.location);
                }
            }
        }
        results.trimToSize();
        return results;
    }

    public void claimTech(int techId) {
        discoveries.claimTech(techId);
    }

    public void claimWonder(int wonderId) {
        if ((wonderId >= 0) && (wonders.types.get(wonderId).isWonder())) {
            wonders.set(wonderId);
        }
    }

    BitSet collateSeenCells() {
        BitSet result = new BitSet(map.countCells());
        for (Civilization civ : civs) {
            result.or(civ.getSeenCells());
        }
        return result;
    }

    protected int countCities() {
        int result = 0;
        for (Civilization civ : civs) {
            result = result + civ.countCities();
        }
        return result;
    }

    protected int countCivs() {
        return civs.size();
    }

    public int countRawTrade(int cityLocationId) {
        if (!map.hasCity(cityLocationId)) {
            return 0;
        }
        if (civs == null) {
            return 0;
        }
        if (civs.size() == 0) {
            return 0;
        }
        for (Civilization civ : civs) {
            if (civ.hasCityAt(cityLocationId)) {
                City city = civ.getCityAt(cityLocationId);
                return civ.countRawTrade(map, city);
            }
        }
        return 0;
    }

    protected int countSeenCells() {
        BitSet seenCells = getSeenCells();
        return seenCells.cardinality();
    }

    protected int countUnits() {
        int result = 0;
        for (Civilization civ : civs) {
            result = result + civ.countUnits();
        }
        return result;
    }

    public int countBenefitFromWondersThatAffectAllCivilizations(WorldMap map, City city, int benefitId) {
        int result = 0;
        int wonderId = -1;
        int numWonders = wonders.countWonders();
        for (int i = 0; i < numWonders; i++) {
            wonderId = wonders.key.nextSetBit(wonderId + 1);
            if (wonderId < 0) {
                break;
            }
            if (   (wonders.types.get(wonderId).affectsAllCivilizations)
                    && (doesWonderAffectCity(wonderId, map, city))
                    ) {
                result = result + wonders.types.get(wonderId).getValue(benefitId);
            }
        }
        return result;
    }

    public String describeLocation(City city) {
        int cellId = city.location;
        double longitude = map.getLongitudeInDegrees(cellId);
        double latitude = map.getLatitudeInDegrees(cellId);
        return formatLongitude(longitude) + " " + formatLatitude(latitude);
    }

    protected String formatLongitude(double longitudeInDegrees) {
        double longitude = ((longitudeInDegrees + 180.0) % 360.0) - 180.0;
        if (Math.abs(longitude) < 1.0 / 120.0) {
            return "0°";
        }
        String direction;
        if (longitude > 0) {
            direction = "E";
        } else {
            direction = "W";
        }
        double absLongitude = Math.abs(longitude);
        return formatDegrees(absLongitude)+direction;
    }

    protected String formatLatitude(double latitudeInDegrees) {
        double latitude = ((latitudeInDegrees + 180.0) % 360.0) - 180.0;
        if (Math.abs(latitude) < 1.0 / 120.0) {
            return "0°";
        }
        if (latitude > 90.0) {
            latitude = 180.0 - latitude;
        }
        if (latitude < -90.0) {
            latitude = -180.0 - latitude;
        }
        String direction;
        if (latitude > 0) {
            direction = "N";
        } else {
            direction = "S";
        }
        double absLatitude = Math.abs(latitude);
        return formatDegrees(absLatitude)+direction;
    }

    protected String formatDegrees(double absoluteAngle) {
        int degrees = (int) Math.round(absoluteAngle);
        return degrees+"°";
    }

    public boolean doesWonderAffectCity(int wonderId, WorldMap map, City city) {
        if (!wonders.key.get(wonderId)) {
            return false;
        }
        if (isObsolete(wonderId)) {
            return false;
        }
        if (city.improvements.key.get(wonderId)) {
            return true;
        }
        if (!wonders.types.get(wonderId).affectsContinent) {
            return false;
        }
        if (city.civ.hasWonder(wonderId)) {
            if (wonders.types.get(wonderId).affectsAllContinents) {
                return true;
            }
            int location = city.civ.getWonderLocation(wonderId);
            if (location >= 0) {
                if (city.civ.getContinentNumber(map, city.location) == city.civ.getContinentNumber(map, location)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        if (!wonders.types.get(wonderId).affectsAllCivilizations) {
            return false;
        }
        if (wonders.types.get(wonderId).affectsAllContinents) {
            return true;
        }

        int location = getLocationOfWonder(wonderId);
        if (location < 0) {
            return false;
        }
        if (city.civ.getContinentNumber(map, city.location) == city.civ.getContinentNumber(map, location)) {
            return true;
        }
        return false;
    }

    protected int getLocationOfWonder(int wonderId) {
        if (!wonders.key.get(wonderId)) {
            return -1;
        }
        if (!wonders.types.get(wonderId).isWonder()) {
            return -1;
        }
        for (Civilization civ : civs) {
            int location = civ.getWonderLocation(wonderId);
            if (location >= 0) {
                return location;
            }
        }
        return -1;
    }

    protected Civilization getCiv(int id) {
        return civs.get(id);
    }

    protected Vector<Integer> getDistinctLocations() {
        Vector<Integer> locations = getLocations();
        Collections.sort(locations);
        return Util.deduplicate(locations);
    }

    protected Vector<Integer> getLocations() {
        int numPossibleLocations = countCities() + countUnits();
        Vector<Integer> result = new Vector<Integer>(numPossibleLocations);
        for (Civilization civ : civs) {
            result.addAll(civ.getLocations());
        }
        return result;
    }

    protected BitSet getSeenCells() {
        BitSet result = new BitSet(map.countCells());
        for (Civilization civ : civs) {
            result.or(civ.getSeenCells());
        }
        return result;
    }

    protected int getTechPriceFactor() {
        return 10;
/*
        if (turn <= 175) { return 10; }
        if (turn <= 225) { return 10 + 10 * (turn - 175) / 50; }
        if (turn <= 325) { return 20; }
        if (turn <= 375) { return 20 + 20 * (turn - 325) / 50; }
        return 40;
*/
    }

    protected int getYear() {
        return getYear(turn);
    }

    protected int getYear(int turn) {
        if (turn <= 0)   { return -4004; }
        if (turn == 200) { return 1; }
        if (turn <= 250) { return 20 * turn - 4000; }
        if (turn <= 300) { return 10 * turn - 1500; }
        if (turn <= 350) { return  5 * turn; }
        if (turn <= 400) { return  2 * turn + 1050; }
        return turn + 1450;
    }

    public boolean hasNonobsoleteElectrifiedWonder() {
        int numWonders = wonders.key.cardinality();
        int id = -1;
        for (int i = 0; i < numWonders; i++) {
            id = wonders.key.nextSetBit(id + 1);
            if (id < 0) {
                break;
            }
            ImprovementType improvement = wonders.types.get(id);
            if ((improvement.isWonder()) && (improvement.isElectrified)) {
                if (!isObsolete(improvement)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAvailable(int wonderId) {
        for (Civilization civ : civs) {
            if (civ.hasWonder(wonderId)) {
                return false;
            }
            if (civ.hadWonder(wonderId)) {
                return false;
            }
        }
        return true;
    }

    public boolean isAvailable(int cellId, Civilization civ) {
        Vector<Integer> cityLocations = civ.getCityLocations();
        if (cityLocations != null) {
            for (int location : cityLocations) {
                if (cellId == location) {
                    return false;
                }
            }
        }
        for (Civilization otherCiv : civs) {
            if (civ != otherCiv) {
                Vector<Integer> locations = civ.getLocations();
                if (locations != null) {
                    for (int location : locations) {
                        if (cellId == location) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    protected boolean isGameOver() {
        if (turn > 550) {
            return true;
        }
        return false;
    }

    public boolean isObsolete(ImprovementType improvementType) {
        int obsoleterTechId = improvementType.obsolescerTechnologyIndex;
        if (obsoleterTechId < 0) {
            return false;
        }
        if (discoveries.hasTech(obsoleterTechId)) {
            return true;
        }
        return false;
    }

    public boolean isObsolete(int wonderId) {
        if (wonderId < 0) {
            return true;
        }
        ImprovementType improvementType = wonders.types.get(wonderId);
        int obsoleterTechId = improvementType.obsolescerTechnologyIndex;
        if (obsoleterTechId < 0) {
            return false;
        }
        return discoveries.hasTech(obsoleterTechId);
    }

    protected void playTurn() {
        if (isTurnInProgress) {
            return;
        }
        isTurnInProgress = true;

        BitSet seenCells = collateSeenCells();
        parent.updateSeenCells(seenCells);

        if (turn % 50 == 0) {
            parent.celebrateYear(getYear(), map, this);
        }
        int techPriceFactor = getTechPriceFactor();
        for (Civilization civ : civs) {
            civ.setTechPriceFactor(techPriceFactor);
            civ.tellStories(parent);
            CivPlayer ruler = new CivPlayer(map, civ, parent, this);
            ruler.playTurn(map, parent, this);
        }
        for (Civilization civ : civs) {
            civ.recordStats();
        }
        parent.updateStats();
        if (turn >= 200) {
            for (Civilization civ : civs) {
                civ.recordPeace();
            }
        }
        turn = turn + 1;
        isTurnInProgress = false;
    }

    protected boolean randomBonus(TerrainTypes terrain) {
        if (terrain == TerrainTypes.grass) {
            return false;
        }
        double value = Math.random();
        if (value < 0.0625) {
            return true;
        }
        if (terrain == TerrainTypes.sea) {
            if (value < 0.25) {
                return true;
            }
        }
        return false;
    }

    protected void clearStatSheets() {
        for (Civilization civ : civs) {
            civ.statSheet.clear();
        }
    }

    public int getCityCellId(int farmCellId) {
        if (map.hasCity(farmCellId)) {
            return farmCellId;
        }
        for (Civilization civ : civs) {
            for (City city : civ.cities) {
                if ((city.farms != null) && (city.farms.contains(farmCellId))) {
                    return city.location;
                }
            }
        }
        return -1;
    }

    public Color getCityColor(int cellId) {
        if (map.hasCity(cellId)) {
            for (Civilization civ : civs) {
                for (City city : civ.cities) {
                    if (city.location == cellId) {
                        return civ.getColor();
                    }
                }
            }
        }
        for (Civilization civ : civs) {
            for (City city : civ.cities) {
                if ((city.farms != null) && (city.farms.contains(cellId))) {
                    return civ.getColor();
                }
            }
        }
        return Color.WHITE;
    }

    protected int countCityMoney(int cellId) {
        if (!map.hasCity(cellId)) {
            return 0;
        }
        for (Civilization civ : civs) {
            for (City city : civ.cities) {
                if (city.location == cellId) {
                    return civ.countMoney(map, city, this);
                }
            }
        }
        return 0;
    }

    public String getCityName(int cellId) {
        if (map.hasCity(cellId)) {
            for (Civilization civ : civs) {
                for (City city : civ.cities) {
                    if (city.location == cellId) {
                        return city.name;
                    }
                }
            }
        }
        return "";
    }

    public int getCitySize(int cellId) {
        if (map.hasCity(cellId)) {
            for (Civilization civ : civs) {
                for (City city : civ.cities) {
                    if (city.location == cellId) {
                        return city.size;
                    }
                }
            }
        }
        return 0;
    }
}