package net.folds.hexciv;

import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.Vector;

/**
 * Created by jasper on Feb 04, 2014.
 */
public class Util {

    static Vector<Integer> deduplicate(Vector<Integer> arg) {
        return new Vector(new LinkedHashSet(arg));
    }

    static boolean doesListContainDuplicates(Vector<Integer> arg) {
        for (int i=0; i<arg.size(); i++) {
            for (int j=i+1; j<arg.size(); j++) {
                if (arg.get(i).equals(arg.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean doListsContainSameItems(Vector<Integer> listA, int[] arrayB) {
        Vector<Integer> listB = new Vector<>(arrayB.length);
        for (int itemB : arrayB) {
            listB.add(itemB);
        }
        for (int itemA : listA) {
            if (!listB.contains(itemA)) {
                return false;
            }
        }
        for (int itemB : listB) {
            if (!listA.contains(itemB)) {
                return false;
            }
        }
        return true;
    }

    static BitSet getBits(int arg, int numBits) {
        BitSet result = new BitSet(numBits);
        int divisor = 1;
        for (int i=0; i < numBits; i++) {
            if ((arg / divisor) % 2 == 1) {
                result.set(i);
            }
            divisor = divisor * 2;
        }
        return result;
    }

    static String getSuffix(int arg) {
        if (arg < 0) {
            return "";
        }
        if (arg % 10 == 1) {
            return "st";
        }
        if (arg % 10 == 2) {
            return "nd";
        }
        if (arg % 10 == 3) {
            return "rd";
        }
        return "th";
    }

    static int getValue(BitSet arg) {
        int size = arg.size();
        int result = 0;
        int placeValue = 1;
        for (int i=0; i < size; i++) {
            if (arg.get(i)) {
                result = result + placeValue;
            }
            placeValue = placeValue * 2;
        }
        return result;
    }

    /** Initializes unset values of a Vector<Integer>.
     * Does not affect already set values.
     * Assumes that the Vector's capacity is the intended size.
     */
    static void initialize(Vector<Integer> arg, int value) {
        for (int i=arg.size(); i<arg.capacity(); i++) {
            arg.add(value);
        }
    }

    public static void printList(String listName, Vector<Integer> arg) {
        System.out.print(listName+"=");
        printList(arg);
        System.out.println("");
    }

    public static void printList(Vector<Integer> arg) {
        System.out.print("{");
        for (int i=0; i<arg.size();i++) {
            System.out.print(arg.get(i));
            if (i < arg.size() - 1) {
                System.out.print(",");
            }
        }
        System.out.print("}");
    }

    static String toString(boolean arg) {
        if (arg) {
            return "True";
        }
        return "False";
    }

    static Vector<Integer> convertToIntegerVector(BitSet arg) {
        int numElements = arg.cardinality();
        Vector<Integer> result = new Vector<Integer>(numElements);
        int pos = -1;
        while (result.size() < numElements) {
            pos = arg.nextSetBit(pos + 1);
            result.add(pos);
        }
        return result;
    }

    static int getNthPosition(BitSet bits, int n) {
        int result = -1;
        for (int i = 0; i < n; i++) {
            result = bits.nextSetBit(result + 1);
        }
        return result;
    }
}