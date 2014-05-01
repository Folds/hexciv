package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Feb 09, 2014.
 *
 * MapBaggage contains all of the information needed to reconstitute
 * a WorldMap, in a compacted form suitable for importing or exporting.
 * Specifically, the long arrays of TerrainTypes and other cell features
 * are compacted into short Strings.
 */
public class MapBaggage {
    public double polarCircumferenceInKilometers;
    public int meshSize;                           // = mesh.n
    public Vector<String> terrainStrings;
    public String bonusString;
    public String roadString;
    public String railroadString;
    public String pollutionString;
    public String irrigationString;
    public String mineString;
    public String villageString;
    public String cityString;

    MapBaggage(WorldMap map) {
        polarCircumferenceInKilometers = map.getPolarCircumferenceInKilometers();
        meshSize = map.getMeshSize();
        terrainStrings = Porter.terrainToStrings(map.terrain);
        bonusString = map.stringifyBonuses();
        roadString = map.stringifyRoads();
        railroadString = map.stringifyRailroads();
        pollutionString = map.stringifyPollution();
        irrigationString = map.stringifyIrrigation();
        mineString = map.stringifyMines();
        villageString = map.stringifyVillages();
        cityString = map.stringifyCities();
    }

    // dummy constructor for use by com.fasterxml.jackson
    public MapBaggage() {
    }
}