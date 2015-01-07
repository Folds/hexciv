package net.folds.hexciv;

import java.awt.*;
import java.util.BitSet;

/**
 * Created by jasper on Jan 06, 2015.
 */
public class CitySnapshot {
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

    CitySnapshot(int id, int row, int positionInRow,
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
