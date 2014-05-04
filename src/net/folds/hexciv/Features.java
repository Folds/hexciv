package net.folds.hexciv;

import java.awt.*;
import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Feb 24, 2014.
 */
public enum Features {
    bonus(0),
    road(1),
    railroad(2),
    irrigation(3),
    village(4),
    city(5);

    private int value;

    private Features(int argValue) {
        value = argValue;
    }

    public int getValue() {
        return value;
    }

    static Features getFeature(int arg) {
        for (Features feature : Features.values()) {
            if (feature.getValue() == arg) {
                return feature;
            }
        }
        return bonus;
    }

    static int count() {
        return Features.values().length;
    }

    public java.awt.Color getColor() {
        switch (value) {
            case  1:  return new Color(204, 153, 128); // Road
            case  2:  return new Color(128, 140, 140); // Railroad
            default:  return new Color( 51,  51,  51);
        }
    }

    boolean isChosen(BitSet choices) {
        return choices.get(value);
    }

    static protected String toString(TerrainTypes terrain, BitSet features) {
        String result = new String();
        if ((terrain.isLand()) && (city.isChosen(features))) {
            result = "1";
        } else {
            if (   (!road.isChosen(features))
                    && (!railroad.isChosen(features))
                    && ((!terrain.isIrrigable()) || (!irrigation.isChosen(features)))
                    ) {
                if ((terrain.isLand()) && (village.isChosen(features))) {
                    result = "V";
                }
            } else {
                if ((terrain.isIrrigable()) && (irrigation.isChosen(features))) {
                    result = "i";
                }
            }
        }
        if (bonus.isChosen(features)) {
            result = result + "+";
        }
        return result;
    }
}