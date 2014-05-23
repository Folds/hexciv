package net.folds.hexciv;

import java.awt.*;
import java.io.Serializable;
import java.util.BitSet;

/**
 * Created by jasper on Feb 05, 2014.
 */
public enum  TerrainTypes {
    desert(1),
    hills(2),
    mountains(3),

    jungle(4),
    forest(5),
    plains(6),
    grass(7),

    glacier(8),
    tundra(9),
    swamp(10),
    river(11),

    seaIce(12),
    ocean(13),
    sea(14),
    lake(15);

    private int value;
    private TerrainTypes(int argValue) {
        value = argValue;
    }

    public int getValue() {
        return value;
    }

    static TerrainTypes getTerrainType(int arg) {
        for (TerrainTypes terrain : TerrainTypes.values()) {
            if (terrain.getValue() == arg) {
                return terrain;
            }
        }
        return ocean;
    }

    /* The maximum number of bits needed to encode a terrain value. */
    static int countBits() {
        return 4;
    }

    BitSet getBits() {
        int numBits = countBits();
        return Util.getBits(value, numBits);
    }

    public String getDescription(boolean hasBonus) {
        if (hasBonus) {
            return getDescription() + " with " + getBonusDescription();
        }
        return getDescription();
    }

    public String getDescription() {
        switch (value) {
            case  1:  return "Desert";
            case  2:  return "Hills";
            case  3:  return "Mountains";

            case  4:  return "Jungle";
            case  5:  return "Forest";
            case  6:  return "Plains";
            case  7:  return "Grass";

            case  8:  return "Glacier";
            case  9:  return "Tundra";
            case 10:  return "Swamp";
            case 11:  return "River";

            case 12:  return "Sea Ice";
            case 13:  return "Ocean";
            case 14:  return "Sea";
            case 15:  return "Lake";

            default:  return "Unknown";
        }
    }

    public String getAbbreviation() {
        switch (value) {
            case  3: return "Mtn";
            case 12: return "SeaIce";
            default: return getDescription();
        }
    }

    public boolean isLand() {
        switch(value) {
            case 12: case 13: case 14: case 15: return false;
            default: return true;
        }
    }

    public boolean isSailable() {
        switch(value) {
            case 13: case 14: case 15: return true;
            default: return false;
        }
    }

    public boolean isIrrigable() {
        switch(value) {
            case 3: case 8: case 9: case 12: case 13: case 14: case 15: return false;
            default: return true;
        }
    }

    public TerrainTypes resultOfIrrigation() {
        switch (value) {
            case  4: return getTerrainType(10); // Jungle -> Swamp
            case 10: return getTerrainType(11); // Swamp  -> River
            case 11: return getTerrainType(15); // River  -> Lake
            case  5: return getTerrainType( 6); // Forest -> Plains
            default: return this;
        }
    }

    public TerrainTypes resultOfMining() {
        switch (value) {
            case 15: return getTerrainType(11); // Lake   -> River
            case 11: return getTerrainType(10); // River  -> Swamp
            case 10: return getTerrainType( 4); // Swamp  -> Jungle
            case  4: case 6: case 7: return getTerrainType( 5); // Jungle, Plains, Grass -> Forest
            default: return this;
        }
    }

    public Color getColor() {
        switch (value) {
            case  1:  return new Color(204, 204, 102); // Desert    w/ Oasis
            case  2:  return new Color(153, 102,  51); // Hills     w/ Coal
            case  3:  return new Color(102, 102, 102); // Mountains w/ Iron

            case  4:  return new Color(102, 255, 102); // Jungle    w/ Gems
            case  5:  return new Color(102, 204, 102); // Forest    w/ Deer
            case  6:  return new Color(153, 204, 102); // Plains    w/ Bison
            case  7:  return new Color(153, 255, 153); // Grass     w/ Mammoths

            case  8:  return new Color(204, 204, 255); // Glacier   w/ Gravel
            case  9:  return new Color(153, 153,  51); // Tundra    w/ Reindeer
            case 10:  return new Color(204, 255, 204); // Swamp     w/ Peat
            case 11:  return new Color(  0, 204, 255); // River     w/ Gold

            case 12:  return new Color(127, 127, 255); // Sea Ice   w/ Flotsam
            case 13:  return new Color( 51,  51, 255); // Ocean     w/ Crabs
            case 14:  return new Color( 61, 118, 222); // Sea       w/ Salt
            case 15:  return new Color(102, 204, 204); // Lake      w/ Fish

            default:  return new Color( 51,  51,  51); // Unknown   w/ Unknown
        }
    }

    public String getBonusDescription() {
        switch (value) {
            case  1:  return "Oasis";
            case  2:  return "Coal";
            case  3:  return "Iron";

            case  4:  return "Gems";
            case  5:  return "Deer";
            case  6:  return "Bison";
            case  7:  return "Mammoths";

            case  8:  return "Gravel";
            case  9:  return "Reindeer";
            case 10:  return "Peat";
            case 11:  return "Gold";

            case 12:  return "Flotsam";
            case 13:  return "Crabs";
            case 14:  return "Salt";
            case 15:  return "Fish";

            default:  return "Unknown";
        }
    }

    // arctic:  food = 0, prod = 0, trade = 0, move = 2, defense = 1.0
    // seals:   food = 2

    // desert:  food = 0 (1 with irrigation)
    //          prod = 1 (1 with mining)
    //          gold = 0 (1% with roads)
    // oasis           3* (4*)
    //

    // * -1 Despotism/Anarchy
    // % +1 Republic/Democracy
}
