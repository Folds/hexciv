package net.folds.hexciv;

import java.util.BitSet;

/**
 * Created by jasper on May 14, 2014.
 */
public class ImprovementKey {
    protected BitSet key;
    protected ImprovementVector types;

    protected ImprovementKey(ImprovementVector types) {
        this.types = types;
        key = new BitSet(types.countTypes());
    }

    protected void clear(int id) {
        if (types.get(id).resaleValue > 0) {
            key.clear(id);
        }
    }

    protected int countWonders() {
        int result = 0;
        int id = -1;
        int numImprovements = key.cardinality();
        for (int i = 0; i < numImprovements; i++) {
            id = key.nextSetBit(i + 1);
            if (id < 0) {
                break;
            }
            if (types.get(id).isWonder()) {
                result = result + 1;
            }
        }
        return result;
    }

    protected boolean get(int id) {
        return key.get(id);
    }

    protected ImprovementType getImprovementType(int id) {
        return types.get(id);
    }

    protected int getLowestValueBarracks() {
        int result = -1;
        int lowestValue = types.getHighestResaleValue();
        int numImprovements = key.cardinality();
        for (int i = 0; i < numImprovements; i++) {
            int id = key.nextSetBit(result + 1);
            if (id < 0) {
                break;
            }
            if (types.get(id).isBarracks) {
                int value = types.get(id).resaleValue;
                if ((value > 0) && (value <= lowestValue)) {
                    result = id;
                    lowestValue = value;
                }
            }
        }
        return result;
    }

    protected int getLowestValueImprovement() {
        int result = -1;
        int lowestValue = types.getHighestResaleValue();
        int numImprovements = key.cardinality();
        for (int i = 0; i < numImprovements; i++) {
            int id = key.nextSetBit(result + 1);
            if (id < 0) {
                break;
            }
            int value = types.get(id).resaleValue;
            if ((value > 0) && (value <= lowestValue)) {
                result = id;
                lowestValue = value;
            }
        }
        return result;
    }

    protected int getUpkeepCost() {
        int result = 0;
        int id = -1;
        int numImprovements = key.cardinality();
        for (int i = 0; i < numImprovements; i++) {
            id = key.nextSetBit(id + 1);
            if (id < 0) {
                break;
            }
            ImprovementType improvementType = ImprovementType.get(i);
            if (improvementType != null) {
                result = result + improvementType.upkeepCost;
            }
        }
        return result;
    }

    protected void set(int id) {
        key.set(id);
    }

    protected boolean shortensRevolutions(ClaimReferee referee) {
        int id = -1;
        int numImprovements = key.cardinality();
        for (int i = 0; i < numImprovements; i++) {
            id = key.nextSetBit(id + 1);
            if (id < 0) {
                break;
            }
            ImprovementType improvement = types.get(id);
            if (improvement.shortensRevolutions) {
                if (!improvement.isWonder()) {
                    return true;
                }
                if (!referee.isObsolete(improvement)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean allowsAnyGovernmentType(ClaimReferee referee) {
        int id = -1;
        int numImprovements = key.cardinality();
        for (int i = 0; i < numImprovements; i++) {
            id = key.nextSetBit(id + 1);
            if (id < 0) {
                break;
            }
            ImprovementType improvement = types.get(id);
            if (improvement.allowsAnyGovernmentType) {
                if (!improvement.isWonder()) {
                    return true;
                }
                if (!referee.isObsolete(improvement)) {
                    return true;
                }
            }
        }
        return false;
    }

}
