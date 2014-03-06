package net.folds.hexciv;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by Jasper on Oct 19, 2011.
 */
public class BitSetPorter {

    static String bitsToString(BitSet arg) {
        Vector<String> vector = bitsToStrings(arg);
        return bitsToString(vector);
    }

    static BitSet stringToBits(String arg, int nbits) {
        BitSet result = new BitSet(nbits);
        if (arg == null) {
            return result;
        }
        int tallyOfCompletedBits = 0;
        int posCurrentFlagCharacter = 0;
        boolean bit = false;
        int nextTallyBits = 0;
        while (posCurrentFlagCharacter < arg.length()) {
            int currentFlagValue = getNumber(arg, posCurrentFlagCharacter);
            if (currentFlagValue <= 65) {
                int numSwathsToProcess = currentFlagValue;
                if (posCurrentFlagCharacter + numSwathsToProcess >= arg.length()) {
                    numSwathsToProcess = arg.length() - posCurrentFlagCharacter - 1;
                }
                for (int i=0; i < numSwathsToProcess; i++) {
                    int atValue = getNumber(arg, posCurrentFlagCharacter + i + 1);
                    nextTallyBits = tallyOfCompletedBits + getCount(atValue);
                    if (nextTallyBits > tallyOfCompletedBits) {
                        result.set(tallyOfCompletedBits, nextTallyBits, bit);
                    }
                    tallyOfCompletedBits = nextTallyBits;
                    bit = getBit(atValue);
                }
                posCurrentFlagCharacter = posCurrentFlagCharacter + currentFlagValue + 1;
            } else {
                int numPlaces = ((currentFlagValue % 66) % 3) + 1;
                if (posCurrentFlagCharacter + numPlaces >= arg.length()) {
                    result.set(tallyOfCompletedBits, tallyOfCompletedBits + 1, bit);
                    return result;
                }
                int atCount = 0;
                for (int i=0; i< numPlaces; i++) {
                    int atValue = getNumber(arg, posCurrentFlagCharacter + i + 1);
                    atCount = atCount * 72 + atValue;
                }
                nextTallyBits = tallyOfCompletedBits + atCount;
                if (nextTallyBits > tallyOfCompletedBits) {
                    result.set(tallyOfCompletedBits, nextTallyBits, bit);
                }
                tallyOfCompletedBits = nextTallyBits;
                if (currentFlagValue <= 68) {
                    bit = false;
                } else {
                    bit = true;
                }
                posCurrentFlagCharacter = posCurrentFlagCharacter + numPlaces + 1;
            }
        }
        return result;
    }

// helper methods:

    static Vector<String> bitsToStrings(BitSet arg) {
        int len = arg.length();
        int swaths = countSwaths(arg);
        Vector<String> result = new Vector<>(swaths);

        int i = 0;
        boolean flipped = false;
        while (i < len) {
            boolean current = arg.get(i);
            int j;
            if (!flipped) {
                if (current || (i > 0)) {
                    String chunk = bitsToString(current, 0);
                    result.add(chunk);
                }
            }
            if (current) {
                j = arg.nextClearBit(i + 1);
            } else {
                j = arg.nextSetBit(i + 1);
            }
            if (j == -1) {
                // no such bit found in the remainder of the bitset.
                j = len;
            }
            if (j >= i + 72*72*72) {
                j  = i + 72*72*72 - 1;
                flipped = false;
            } else {
                flipped = true;
            }
            String chunk = bitsToString(!current, j - i);
            result.add(chunk);
            i = j;
        }
        result.trimToSize();
        return result;
    }

    static int countSwaths(BitSet arg) {
        int len = arg.length();
        int i = 0;
        int result = 0;
        while (i < len) {
            boolean current = arg.get(i);
            int j;
            if (current) {
                j = arg.nextClearBit(i + 1);
            } else {
                j = arg.nextSetBit(i + 1);
            }
            if (j == -1) {
                // no such bit found in the remainder of the bitset.
                j = len;
            }
            if (j >= i + 72*72*72) {
                j = i + 72*72*72 - 1;
            }
            result = result + 1;
            i = j;
        }
        return result;
    }

    static String bitsToString(Vector<String> arg) {
        StringBuilder result = new StringBuilder();
        int size = arg.size();
        int done = -1;
        int current = 0;
        int tally = 0;
        for (current=0; current < size; current++) {
            int flag = fromCharacter(arg.get(current).substring(0,1));
            if ((flag < 0) || (tally + flag > 65) || (current == size - 1)) {
                int prevTally = tally;
                if ((flag >=0) && (tally + flag <= 65)) {
                    tally = tally + flag;
                }
                if (tally > 0) {
                    result.append(chooseCharacter(tally));
                    int through;
                    if ((flag <= 65) && (tally != prevTally)) {
                        through = current;
                    } else {
                        through = current - 1;
                    }
                    for (int i = done + 1; i < through + 1; i++) {
                        result.append(getSubstringAfterFirstCharacter(arg.get(i)));
                    }
                    if ((through < current) && (flag != 0)) {
                        result.append(arg.get(current));
                    }
                } else {
                    result.append(arg.get(current));
                }
                done = current;
                tally = 0;
            } else {
                tally = tally + flag;
            }
        }
        return result.toString();
    }

    static String getSubstringAfterFirstCharacter(String arg) {
        if (arg.length() < 2) {
            return "";
        }
        return arg.substring(1,arg.length());
    }

    static String bitsToString(boolean argBoolean, int argTally) {
        StringBuilder result = new StringBuilder();
        if (argTally < 0)
            return "";
        if (argTally <= 105) {
            int flag = (argTally - 1) / 35 + 1;
            result.append(chooseCharacter(flag));
            int residue = argTally;
            for (int i = 1; i < flag; i++) {
                // The first one or two characters have the opposite boolean
                // from the last character, because they are used to re-create
                // the previous boolean, not the next boolean.
                if (argBoolean) {
                    result.append(chooseCharacter(35)); // 35 consecutive clear bytes
                } else {
                    result.append(chooseCharacter(71)); // 35 consecutive  set  bytes
                }
                residue = residue - 35;
            }
            // The final character is used to re-create the next boolean.
            if (argBoolean) {
                result.append(chooseCharacter(residue + 36)); // (residue) consecutive  set  bytes
            } else {
                result.append(chooseCharacter(residue));      // (residue) consecutive clear bytes
            }
            return result.toString();
        }
        if (argTally < 72*72) {
            int flag;
            if (argBoolean) {
                flag = 70;          // (72*high + low) consecutive  set  bytes
            } else {
                flag = 67;          // (72*high + low) consecutive clear bytes
            }
            result.append(chooseCharacter(flag));
            int high = argTally / 72;
            int low  = argTally % 72;
            result.append(chooseCharacter(high));
            result.append(chooseCharacter(low));
            return result.toString();
        }
        int flag;
        if (argBoolean) {
            flag = 71;          // (72*72*high + 72*mid + low) consecutive  set  bytes
        } else {
            flag = 68;          // (72*72*high + 72*mid + low) consecutive clear bytes
        }
        result.append(chooseCharacter(flag));
        int high = argTally / (72 * 72);
        int mid  = (argTally % (72 * 72)) / 72;
        int low  = argTally % 72;
        if (argTally >= 72*72*72) {
            high = 71;
            mid = 71;
            low = 71;
        }
        result.append(chooseCharacter(high));
        result.append(chooseCharacter(mid));
        result.append(chooseCharacter(low));
        return result.toString();
    }

    static int getNumber(String arg, int position) {
        if ((position >= 0) && (position < arg.length())) {
            String character = arg.substring(position, position + 1);
            return fromCharacter(character);
        } else {
            return 0;
        }
    }

    static int getCount(int number) {
        if (number < 0) {
            return 0;
        }
        return number % 36;
    }

    static boolean getBit(int number) {
        if (number < 36) {
            return false;
        }
        return true;
    }

// mapping of numbers <=> characters:

    // Assumes 0 <= arg <= 71
    static String chooseCharacter(int arg) {
        switch (arg) {
            case  0: return "0";    case 36: return ")";
            case  1: return "1";    case 37: return "!";
            case  2: return "2";    case 38: return "@";
            case  3: return "3";    case 39: return "#";
            case  4: return "4";    case 40: return "$";
            case  5: return "5";    case 41: return "%";
            case  6: return "6";    case 42: return "^";
            case  7: return "7";    case 43: return "&";
            case  8: return "8";    case 44: return "*";
            case  9: return "9";    case 45: return "(";
            case 10: return "a";    case 46: return "A";
            case 11: return "b";    case 47: return "B";
            case 12: return "c";    case 48: return "C";
            case 13: return "d";    case 49: return "D";
            case 14: return "e";    case 50: return "E";
            case 15: return "f";    case 51: return "F";
            case 16: return "g";    case 52: return "G";
            case 17: return "h";    case 53: return "H";
            case 18: return "i";    case 54: return "I";
            case 19: return "j";    case 55: return "J";
            case 20: return "k";     case 56: return "K";
            case 21: return "l";    case 57: return "L";
            case 22: return "m";    case 58: return "M";
            case 23: return "n";    case 59: return "N";
            case 24: return "o";    case 60: return "O";
            case 25: return "p";    case 61: return "P";
            case 26: return "q";    case 62: return "Q";
            case 27: return "r";    case 63: return "R";
            case 28: return "s";    case 64: return "S";
            case 29: return "t";    case 65: return "T";
            case 30: return "u";    case 66: return "U";
            case 31: return "v";    case 67: return "V";
            case 32: return "w";    case 68: return "W";
            case 33: return "x";    case 69: return "X";
            case 34: return "y";    case 70: return "Y";
            case 35: return "z";    case 71: return "Z";
            default: return "0";
        }
    }

    static int fromCharacter(String arg) {
        switch (arg) {
            case "0": return  0;    case ")": return 36;
            case "1": return  1;    case "!": return 37;
            case "2": return  2;    case "@": return 38;
            case "3": return  3;    case "#": return 39;
            case "4": return  4;    case "$": return 40;
            case "5": return  5;    case "%": return 41;
            case "6": return  6;    case "^": return 42;
            case "7": return  7;    case "&": return 43;
            case "8": return  8;    case "*": return 44;
            case "9": return  9;    case "(": return 45;
            case "a": return 10;    case "A": return 46;
            case "b": return 11;    case "B": return 47;
            case "c": return 12;    case "C": return 48;
            case "d": return 13;    case "D": return 49;
            case "e": return 14;    case "E": return 50;
            case "f": return 15;    case "F": return 51;
            case "g": return 16;    case "G": return 52;
            case "h": return 17;    case "H": return 53;
            case "i": return 18;    case "I": return 54;
            case "j": return 19;    case "J": return 55;
            case "k": return 20;    case "K": return 56;
            case "l": return 21;    case "L": return 57;
            case "m": return 22;    case "M": return 58;
            case "n": return 23;    case "N": return 59;
            case "o": return 24;    case "O": return 60;
            case "p": return 25;    case "P": return 61;
            case "q": return 26;    case "Q": return 62;
            case "r": return 27;    case "R": return 63;
            case "s": return 28;    case "S": return 64;
            case "t": return 29;    case "T": return 65;
            case "u": return 30;    case "U": return 66;
            case "v": return 31;    case "V": return 67;
            case "w": return 32;    case "W": return 68;
            case "x": return 33;    case "X": return 69;
            case "y": return 34;    case "Y": return 70;
            case "z": return 35;    case "Z": return 71;
           default:  return  0;
        }
    }
}