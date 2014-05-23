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

    protected ImprovementType getImprovementType(int id) {
        return types.get(id);
    }

    protected void set(int id) {
        key.set(id);
    }

    protected int getTradeFactor() {
        int result = 100;
        int id = -1;
        int numImprovements = key.cardinality();
        for (int i = 0; i < numImprovements; i++) {
            id = key.nextSetBit(id + 1);
            if (id < 0) {
                break;
            }
            result = result + types.get(id).tradeBonus;
        }
        return result;
    }

    protected int getScienceFactor() {
        int result = 100;
        int id = -1;
        int numImprovements = key.cardinality();
        for (int i = 0; i < numImprovements; i++) {
            id = key.nextSetBit(id + 1);
            if (id < 0) {
                break;
            }
            result = result + types.get(id).scienceBonus;
        }
        return result;
    }

    protected int getLuxuryFactor() {
        return getTradeFactor();
    }

    protected int getTaxFactor() {
        return getTradeFactor();
    }

    protected int getProductionFactor() {
        int result = 100;
        int id = -1;
        int numImprovements = key.cardinality();
        boolean isElectrified = false;
        for (int i = 0; i < numImprovements; i++) {
            id = key.nextSetBit(id + 1);
            if (id < 0) {
                break;
            }
            if (100 + types.get(id).productionBonus > result) {
                result = 100 + types.get(id).productionBonus;
            }
            if (types.get(id).isElectrified) {
                isElectrified = true;
            }
        }
        if (isElectrified) {
            result = result * 3 / 2 - 50;
        }
        return result;
    }

}
