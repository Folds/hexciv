package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class GovernmentType {
    String name;

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

    protected static Vector<GovernmentType> getChoices() {
        Vector<GovernmentType> result = new Vector<>(6);
        for (int i = 0; i < 6; i++) {
            GovernmentType choice = new GovernmentType();
            choice.name = GovernmentType.proposeName(i);
            result.add(choice);
        }
        return result;
    }
}
