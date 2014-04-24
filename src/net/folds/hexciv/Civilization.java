package net.folds.hexciv;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Apr 21, 2014.
 */
public class Civilization {
    private Vector<City> cities;
    private int precedence;
    private int governmentTypeId;
    private String ruler;
    private BitSet techs;
    private Vector<Relationship> relationships;
    private Vector<GovernmentType> governmentTypes;
    private Vector<UnitType> unitTypes;

    protected Civilization(Vector<GovernmentType> governmentTypes,
                           Vector<UnitType> unitTypes) {
        this.governmentTypes = governmentTypes;
        this.unitTypes = unitTypes;
    }

    protected void initialize(WorldMap map, Vector<Integer> foreignLocations) {
        governmentTypeId = GovernmentType.proposeId("Despotism");
        cities = new Vector<City>(1);
        City none = new City(this);
        cities.add(none);
        UnitType settlerType = UnitType.lookupUnitType(unitTypes, "Settler");
        int cellId = chooseStartLocation(map, foreignLocations);
        Unit settler = new Unit(settlerType, cellId);
        none.add(settler);
    }

    protected int chooseStartLocation(WorldMap map, Vector<Integer> foreignLocations) {
        return 0;
    }

    protected int countCities() {
        // the "None" city does not count.
        if (cities == null) {
            return -1;
        }
        return cities.size() - 1;
    }

    protected int countUnits() {
        int result = 0;
        if (cities == null) {
            return result;
        }
        for (City city : cities) {
            result = result + city.countUnits();
        }
        return result;
    }

    protected String describeGovernment() {
        return getGovernmentType().name;
    }

    protected GovernmentType getGovernmentType() {
        return governmentTypes.get(governmentTypeId);
    }

    protected Vector<Integer> getCityLocations() {
        int numCities = countCities();
        Vector<Integer> result = new Vector<Integer>(numCities);
        for (City city : cities) {
            result.add(city.location);
        }
        return result;
    }

    protected Vector<Integer> getLocations() {
        Vector<Integer> result = getCityLocations();
        result.addAll(getUnitLocations());
        return result;
    }

    protected Vector<Integer> getUnitLocations() {
        int numUnits = countUnits();
        Vector<Integer> result = new Vector<>(numUnits);
        for (City city : cities) {
            result.addAll(city.getUnitLocations());
        }
        return result;
    }

    protected City getCity(int id) {
        return cities.get(id);
    }
}
