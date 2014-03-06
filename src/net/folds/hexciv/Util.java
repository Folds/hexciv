package net.folds.hexciv;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Feb 04, 2014.
 */
public class Util {

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

    /** Initializes unset values of a Vector<Integer>.
     * Does not affect already set values.
     * Assumes that the Vector's capacity is the intended size.
     */
    static void initialize(Vector<Integer> arg, int value) {
        for (int i=arg.size(); i<arg.capacity(); i++) {
            arg.add(value);
        }
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

    static String toString(boolean arg) {
        if (arg) {
            return "True";
        }
        return "False";
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

}