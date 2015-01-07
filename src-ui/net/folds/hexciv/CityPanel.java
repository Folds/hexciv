package net.folds.hexciv;

import java.awt.*;

/**
 * Created by jasper on Oct 15, 2011.
 *
 * UI component that displays information about the current location on the getMap.
 */
public class CityPanel extends Panel {
    // private int cellId;
    private CitySnapshot citySnapshot;

    public CityPanel(CityDescriber parent) {
        super(parent);
        int cellId = 0;
        citySnapshot = parent.getCitySnapshot(cellId);
        setPreferredSize(new Dimension(150, 188));
    }
/*
    protected void setCellId(int cellId) {
        if (cellId != citySnapshot.id) {
            citySnapshot = parent.getCitySnapshot(cellId);
            this.repaint();
        }
    }
*/
    protected void setCell(CitySnapshot citySnapshot) {
        if (   (citySnapshot.id != this.citySnapshot.id)
            || (citySnapshot.row != this.citySnapshot.row)
            || (citySnapshot.positionInRow != this.citySnapshot.positionInRow)
           ) {
            this.citySnapshot = citySnapshot;
            this.repaint();
        }
    }

    public void paintComponent(Graphics comp) {
        super.paintComponent(comp);
        Graphics2D comp2D = (Graphics2D) comp;
        long h = getHeight() - 5; // available height for information
        long w = getWidth()  - 5; // available width  for information
        comp2D.drawRect(2, 2, (int) w, (int) h);
        CitySnapshot city = citySnapshot;

        beginUsingTextArea(comp2D, 5, 18);
        typeLine("Cell ID = " + city.id);
        typeLine("Rowlet = " + city.row);
        typeLine("Pos = " + city.positionInRow);
        typeLine(city.description);
//        typeLine("Area ~ " + formatArea(cell.areaInSquareKilometers) + " km²");
//        typeLine("Av.Dia. ~ " + formatDistance(cell.meanDiameterInKilometers) + " km");
        typeLine("Longitude ~ " + formatLongitude(city.longitude));
        typeLine("Latitude  ~ " + formatLatitude(city.latitude));
        finishUsingTextArea();
    }
/*
    protected String formatArea(double area) {
        // round off to the nearest 10,000 square units.
        long roundedArea = Math.round(area/10000)*10000;
        return String.format("%,d",roundedArea);
    }

    protected String formatDistance(double distance) {
        // round off to the nearest 10 units.
        long roundedDistance = Math.round(distance/10)*10;
        return String.format("%,d",roundedDistance);
    }
*/
    protected String formatLongitude(double longitudeInDegrees) {
        double longitude = ((longitudeInDegrees + 180.0) % 360.0) - 180.0;
        if (Math.abs(longitude) < 1.0 / 120.0) {
            return "0°";
        }
        String direction;
        if (longitude > 0) {
            direction = "E";
        } else {
            direction = "W";
        }
        double absLongitude = Math.abs(longitude);
        return formatDegreesAndMinutes(absLongitude)+direction;
    }

    protected String formatLatitude(double latitudeInDegrees) {
        double latitude = ((latitudeInDegrees + 180.0) % 360.0) - 180.0;
        if (Math.abs(latitude) < 1.0 / 120.0) {
            return "0°";
        }
        if (latitude > 90.0) {
            latitude = 180.0 - latitude;
        }
        if (latitude < -90.0) {
            latitude = -180.0 - latitude;
        }
        String direction;
        if (latitude > 0) {
            direction = "N";
        } else {
            direction = "S";
        }
        double absLatitude = Math.abs(latitude);
        return formatDegreesAndMinutes(absLatitude)+direction;
    }

    protected String formatDegreesAndMinutes(double absoluteAngle) {
        int degrees = (int) Math.floor(absoluteAngle);
        double absMinutes = (absoluteAngle - degrees) * 60.0;
        int minutes = (int) Math.round(absMinutes);
        if (minutes == 60) {
            degrees = degrees + 1;
            minutes = 0;
        }
        if (minutes == 0) {
            return degrees+"°";
        }
        return degrees+"°"+minutes+"'";
    }
}