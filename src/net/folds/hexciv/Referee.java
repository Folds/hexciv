package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on May 02, 2014.
 */
public class Referee implements ClaimReferee {
    Vector<Integer> unavailableLocations;
    ImprovementKey wonders;
    TechKey techKey;

    public Referee(TechTree techTree, ImprovementVector improvementVector) {
        techKey = new TechKey(techTree);
        wonders = new ImprovementKey(improvementVector);
    }

    public Referee(Vector<Integer> unavailableLocations) {
        this.unavailableLocations = unavailableLocations;
    }

    public void claimTech(int techId) {
        techKey.claimTech(techId);
    }

    public void claimWonder(int wonderId) {
        if ((wonderId >= 0) && (wonders.types.get(wonderId).isWonder())) {
            wonders.set(wonderId);
        }
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

        // wonder is in another civ.  It affects an entire continent, but does not affect all continents.
        // to-do:  find out whether the wonder is on the same continent as the city.
        return false;
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

    public boolean isAvailable(int cellId, Civilization civ) {
        if (unavailableLocations.contains((Integer) cellId)) {
            return false;
        }
        return true;
    }

    public boolean isAvailable(int wonderId) {
        return !wonders.get(wonderId);
    }

    public boolean isObsolete(ImprovementType improvementType) {
        int obsoleterTechId = improvementType.obsolescerTechnologyIndex;
        if (obsoleterTechId < 0) {
            return false;
        }
        if (techKey.hasTech(obsoleterTechId)) {
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
        return techKey.hasTech(obsoleterTechId);
    }

}
