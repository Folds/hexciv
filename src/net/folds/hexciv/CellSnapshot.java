package net.folds.hexciv;

import java.awt.*;
import java.util.BitSet;
import java.util.Vector;

/**
 * Created by Jasper on Oct 15, 2011.
 */
public class CellSnapshot {
    int     id;
    int     row;
    int     positionInRow;
    double  areaInSquareKilometers;
    double  meanDiameterInKilometers;
    double  longitude;
    double  latitude;
    BitSet  features;
    String  description;
    Color   color;

    CellSnapshot(int id, int row, int positionInRow,
                 double areaInSquareKilometers, double meanDiameterInKilometers,
                 double longitude, double latitude,
                 BitSet features, String description, Color color) {
        this.id = id;
        this.row = row;
        this.positionInRow = positionInRow;
        this.areaInSquareKilometers = areaInSquareKilometers;
        this.meanDiameterInKilometers = meanDiameterInKilometers;
        this.longitude = longitude;
        this.latitude = latitude;
        this.features = features;
        this.description = description;
        this.color = color;
    }
}
