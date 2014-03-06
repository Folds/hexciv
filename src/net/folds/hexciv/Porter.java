package net.folds.hexciv;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Feb 08, 2014.
 */
public class Porter {

    static void exportMap(WorldMap map, File file) {
        exportMap(map, file.getAbsolutePath());
    }

    static void exportMap(WorldMap map, String filename) {
        MapBaggage baggage = new MapBaggage(map);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            // http://jackson-users.ning.com/forum/topics/
            //          newbie-question-how-to-write-with-line-breaks
            mapper.writeValue(new File(filename), baggage);
        } catch (IOException e) {
            // System.out.println("Exception in Porter.exportMap(map, "+filename+")");
            e.printStackTrace();
        }
    }

    static String exportMapToString(WorldMap map) {
        MapBaggage baggage = new MapBaggage(map);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            return mapper.writeValueAsString(baggage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    static WorldMap importMap(File file) {
        return importMap(file.getAbsolutePath());
    }

    static WorldMap importMap(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            MapBaggage baggage = mapper.readValue(new File(filename), MapBaggage.class);
            return new WorldMap(baggage);
        } catch (IOException e) {
            return new WorldMap();
        }
    }

    static WorldMap importMapFromString(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            MapBaggage baggage = mapper.readValue(json, MapBaggage.class);
            return new WorldMap(baggage);
        } catch (IOException e) {
            return new WorldMap();
        }
    }

    static String bitsToString(BitSet arg) {
        return BitSetPorter.bitsToString(arg);
    }

    static BitSet stringToBits(String arg, int nbits) {
        return BitSetPorter.stringToBits(arg, nbits);
    }

    static Vector<String> terrainToStrings(TerrainTypes[] arg) {
        Vector<BitSet> bits = terrainToBits(arg);
        int numBitSets = bits.size();
        Vector<String> result = new Vector<>(numBitSets);
        for (int i=0; i< numBitSets; i++) {
            String string = bitsToString(bits.get(i));
            result.add(string);
        }
        return result;
    }

    static Vector<BitSet> terrainToBits(TerrainTypes[] arg) {
        int len = arg.length;
        int numBits = TerrainTypes.countBits();
        Vector<BitSet> result = new Vector<>(numBits);
        for (int j=0; j < numBits; j++) {
            BitSet bitset = new BitSet(len);
            bitset.clear();
            result.add(bitset);
        }
        for (int i=0; i < len; i++) {
            BitSet bits = arg[i].getBits();
            for (int j=0; j < numBits; j++) {
                result.get(j).set(i, bits.get(j));
            }
        }
        return result;
    }

    static TerrainTypes[] bitsToTerrain(Vector<BitSet> bitsets, int len) {
        TerrainTypes[] result = new TerrainTypes[len];
        int numBitSets = bitsets.size();
        for (int i=0; i < len; i++) {
            BitSet bits = new BitSet(numBitSets);
            for (int j=0; j < numBitSets; j++) {
                bits.set(j, bitsets.get(j).get(i));
            }
            result[i] = TerrainTypes.getTerrainType(Util.getValue(bits));
        }
        return result;
    }

    static Vector<BitSet> stringsToBits(Vector<String> arg, int nbits) {
        int numStrings = arg.size();
        Vector<BitSet> result = new Vector<BitSet>(numStrings);
        for (int i=0; i < numStrings; i++) {
            result.add(stringToBits(arg.get(i), nbits));
        }
        return result;
    }

    static TerrainTypes[] stringsToTerrain(Vector<String> arg, int len) {
        Vector<BitSet> bitsets = stringsToBits(arg, len);
        return bitsToTerrain(bitsets, len);
    }
}
