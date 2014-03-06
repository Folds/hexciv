package net.folds.hexciv;

/**
 * Created by jasper on Sep 29, 2011.
 */
public enum Directions {
    none(0),
    northeast(1),
    east(2),
    southeast(3),
    southwest(4),
    west(5),
    northwest(6);

    private int value;
    private Directions(int argValue) {
        value = argValue;
    }

    public int getValue() {
        return value;
    }

    boolean isEastward() {
        if (   (this == northeast)
            || (this == east)
            || (this == southeast)
           ) {
            return true;
        }
        return false;
    }

    boolean isWestward() {
        if (   (this == northwest)
            || (this == west)
            || (this == southwest)
           ) {
            return true;
        }
        return false;
    }

    boolean isNorthward() {
        if (   (this == northwest)
            || (this == northeast)
           ) {
            return true;
        }
        return false;
    }

    boolean isSouthward() {
        if (   (this == southwest)
            || (this == southeast)
           ) {
            return true;
        }
        return false;
    }

    Directions getDirection(int arg) {
        for (Directions dir : Directions.values()) {
            if (dir.getValue() == arg) {
                return dir;
            }
        }
        return none;
    }

    // CCW rotation, in degrees
    Directions rotate(int rotation) {
        if (value == none.value) {
            return none;
        }

        int netRotation = rotation % 360;
        int clicks = netRotation / 60;
        return getDirection((12 + value - 1 - clicks) % 6 + 1);
    }
}