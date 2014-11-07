package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Jul 28, 2014.
 */
public class StatColumn {
    Vector<StatDatum> data;
    int startTurn;
    int currentTurn;
    int lastPos;
    int cachedPos;
    int minRange;
    int maxRange;
    String name;
    Vector<String> valueNames;

    public StatColumn(int startTurn, String name) {
        this.startTurn = startTurn;
        currentTurn = startTurn - 1;
        data = new Vector<StatDatum>();
        lastPos = -1;
        cachedPos = -1;
        minRange =   0;
        maxRange = 100;
        this.name = name;
    }

    public void setValueNames(Vector<String> valueNames) {
        if ((this.valueNames != null) && (this.valueNames != valueNames)) {
            this.valueNames.clear();
            this.valueNames.addAll(valueNames);
        } else if ((valueNames != null) && (!valueNames.isEmpty())) {
            this.valueNames = new Vector<String>(valueNames.size());
            this.valueNames.addAll(valueNames);
        }
    }

    public void clear() {
        currentTurn = startTurn - 1;
        data.clear();
        lastPos = -1;
        maxRange = 100;
    }

    public void record(int turnId, int datum) {
        // bump up range as needed, along 100, 200, 500, 1000, 2000, 5000, 10000, ...
        if ((datum > maxRange) && (minRange == 0) && (maxRange > 0)) {
            maxRange = this.getLowestCeiling(datum, maxRange);
        }
        if (lastPos < 0) {
            data.add(new StatDatum(turnId, datum));
            currentTurn = turnId;
            lastPos = 0;
            cachedPos = 0;
            return;
        }
        if (turnId > currentTurn) {
            currentTurn = turnId;
        }
        if (data.get(lastPos).value == datum) {
            return;
        }
        data.add(new StatDatum(turnId, datum));
        lastPos = lastPos + 1;
    }

    public int getStartTurn() {
        return startTurn;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public int getCurrentValue() {
        return lookUp(getCurrentTurn());
    }

    // get ceiling along 1, 2, 5, 10, 20, 50, 100, ...
    public static int getLowestCeiling(int arg) {
        int oldCeiling = 1;
        return getLowestCeiling(arg, oldCeiling);
    }

    public static int getLowestCeiling(int arg, int oldCeiling) {
        int result = oldCeiling;
        while ((arg > result)) {
            if (arg > 5 * result) {
                result = 10 * result;
            } else {
                if (Math.log10(5 * result) == (int) Math.log10(5 * result)) {
                    result = (5 * result) / 2;
                } else {
                    result = 2 * result;
                }
            }
        }
        return result;
    }

    public int lookUp(int turnId) {

        // try to use edge cases.
        if (lastPos < 0) {
            cachedPos = 0;
            return 0;
        }
        if (turnId < startTurn) {
            cachedPos = 0;
            return data.get(0).value;
        }
        int currentTurn = getCurrentTurn();
        if (turnId >= currentTurn) {
            cachedPos = lastPos;
            return data.get(lastPos).value;
        }
        if (turnId <= data.get(0).turnId) {
            cachedPos = 0;
            return data.get(0).value;
        }
        if (turnId >= data.get(lastPos).turnId) {
            cachedPos = lastPos;
            return data.get(lastPos).value;
        }

        // try to use cached value.
        int cachedTurn = data.get(cachedPos).turnId;
        int nextPos = cachedPos;
        if (cachedPos < lastPos) {
            nextPos = nextPos + 1;
        }
        int nextTurn = data.get(nextPos).turnId;
        if ((cachedTurn <= turnId) && (turnId < nextTurn)) {
            return data.get(cachedPos).value;
        }
        if (turnId == nextTurn) {
            cachedPos = nextPos;
            return data.get(nextPos).value;
        }

        // use binary search.
        int beforePos = 0;
        int beforeTurn = data.get(beforePos).turnId;
        int afterPos = lastPos;
        int afterTurn = data.get(afterPos).turnId;
        while ((beforeTurn < turnId) && (afterTurn > turnId) && (afterPos > beforePos + 1)) {
            int midPos = beforePos + (afterPos - beforePos) / 2;
            int midTurn = data.get(midPos).turnId;
            if (midTurn > turnId) {
                afterPos = midPos;
                afterTurn = midTurn;
            } else {
                beforePos = midPos;
                beforeTurn = midTurn;
            }
        }
        cachedPos = beforePos;
        return data.get(beforePos).value;
    }

    protected void setMinRange(int minRange) {
        this.minRange = minRange;
    }

    public int getMinRange() {
        return minRange;
    }

    protected void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }

    public int getMaxRange() {
        return maxRange;
    }
}
