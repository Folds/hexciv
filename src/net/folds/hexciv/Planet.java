package net.folds.hexciv;

import java.io.Serializable;

/**
 * Created by Jasper on 2/1/2014.
 */

public class Planet implements Serializable {
    double polarCircumferenceInKilometers;

    public Planet() {
        this(40000.0);
    }

    public Planet(double polarCircumferenceInKilometers) {
        this.polarCircumferenceInKilometers = polarCircumferenceInKilometers;
    }

    public double polarCircumferenceInKilometers() {
        return polarCircumferenceInKilometers;
    }

    public double radiusInkilometers() {
        return polarCircumferenceInKilometers() / 2.0 / Math.PI;
    }

    public double areaInSquareKilometers() {
        return 4 * Math.PI * radiusInkilometers() * radiusInkilometers();
    }

    // Using spherical trigonometry,
    // compute the diameter of a circle with a given area.
    double getMeanDiameterInKilometers(double areaInSquareKilometers) {
        double planetArea = areaInSquareKilometers();
        double sine = 1 - 2 * areaInSquareKilometers / planetArea;
        double radiusInDegrees = 90 - Degrees.arcsin(sine);
        return radiusInDegrees / 360.0 * polarCircumferenceInKilometers();
    }

}
