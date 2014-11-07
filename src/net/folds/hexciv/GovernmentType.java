package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class GovernmentType {
    String name;
    int technologyIndex;

    protected static String proposeName(int id) {
        switch(id) {
            case 0: return "Anarchy";
            case 1: return "Despotism";
            case 2: return "Monarchy";
            case 3: return "Communism";
            case 4: return "Republic";
            case 5: return "Democracy";
            default: return "Undefined";
        }
    }

    protected static int proposeId(String name) {
        if (name.equalsIgnoreCase("Anarchy"))   { return 0; }
        if (name.equalsIgnoreCase("Despotism")) { return 1; }
        if (name.equalsIgnoreCase("Monarchy"))  { return 2; }
        if (name.equalsIgnoreCase("Communism")) { return 3; }
        if (name.equalsIgnoreCase("Republic"))  { return 4; }
        if (name.equalsIgnoreCase("Democracy")) { return 5; }
        return -1;
    }

    protected static int getMaxId() {
        return 5;
    }

    protected static String listAbbreviations() {
        return "ADsMCRDm";
    }

    protected static String listNames() {
        String result = "";
        int numChoices = getMaxId() + 1;
        for (int i = 0; i < numChoices; i++) {
            if (i > 0) {
                result = result + ", ";
            }
            result = result + GovernmentType.proposeName(i);
        }
        return result;
    }

    protected static int proposeTech(int id) {
        switch(id) {
            case 0: return  0; // Hooting -> Anarchy
            case 1: return  8; // Fire    -> Despotism
            case 2: return 30; // Monarchy
            case 3: return 64; // Communism
            case 4: return 39; // Republic
            case 5: return 47; // Democracy
            default: return -1;
        }
    }

    protected static Vector<GovernmentType> getChoices() {
        int numChoices = getMaxId() + 1;
        Vector<GovernmentType> result = new Vector<>(numChoices);
        for (int i = 0; i < numChoices; i++) {
            GovernmentType choice = new GovernmentType();
            choice.name = GovernmentType.proposeName(i);
            choice.technologyIndex = proposeTech(i);
            result.add(choice);
        }
        return result;
    }

    protected static Vector<String> getNames() {
        int numChoices = getMaxId() + 1;
        Vector<String> result = new Vector<>(numChoices);
        for (int i = 0; i < numChoices; i++) {
            result.add(GovernmentType.proposeName(i));
        }
        return result;
    }

    protected int bonus() {
        if (name.equalsIgnoreCase("Anarchy"))   { return -1; }
        if (name.equalsIgnoreCase("Despotism")) { return -1; }
        if (name.equalsIgnoreCase("Republic"))  { return  1; }
        if (name.equalsIgnoreCase("Democracy")) { return  1; }
        return 0;
    }
}
