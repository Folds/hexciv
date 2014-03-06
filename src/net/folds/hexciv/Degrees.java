package net.folds.hexciv;

/**
 * Created by jasper on Feb 02, 2014.
 */
public class Degrees {

    static final double DEGREES_PER_RADIAN = 180.0 / Math.PI;
    static final double RADIANS_PER_DEGREE = Math.PI / 180.0;

    static double arcsin(double sine) {
        return DEGREES_PER_RADIAN * Math.asin(sine);
    }

    static double cos(double angleInDegrees) {
        return Math.cos(angleInDegrees * RADIANS_PER_DEGREE);
    }

    static double sin(double angleInDegrees) {
        return Math.sin(angleInDegrees * RADIANS_PER_DEGREE);
    }

    static double sphericalDistance(double longitudeInDegrees1, double latitudeInDegrees1,
                                    double longitudeInDegrees2, double latitudeInDegrees2) {
        double deltaLongitude = (longitudeInDegrees2 - longitudeInDegrees1);
        double x1 = Degrees.cos(latitudeInDegrees1);
        double y1 = 0;
        double z1 = Degrees.sin(latitudeInDegrees1);
        double x2 = Degrees.cos(latitudeInDegrees2) * Degrees.cos(deltaLongitude);
        double y2 = Degrees.cos(latitudeInDegrees2) * Degrees.sin(deltaLongitude);
        double z2 = Degrees.sin(latitudeInDegrees2);
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        double deltaZ = z2 - z1;
        double hypotenuse = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        return 2 * Degrees.arcsin(hypotenuse / 2.0);
    }

}