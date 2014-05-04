package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on May 02, 2014.
 */
public class Referee implements ClaimReferee {
    Vector<Integer> unavailableLocations;

    Referee(Vector<Integer> unavailableLocations) {
        this.unavailableLocations = unavailableLocations;
    }

    public boolean isAvailable(int cellId, Civilization civ) {
        if (unavailableLocations.contains((Integer) cellId)) {
            return false;
        }
        return true;
    }
}
