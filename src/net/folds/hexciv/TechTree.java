package net.folds.hexciv;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on May 08, 2014.
 */
public class TechTree {
    Vector<Technology> techs;

    TechTree() {
        techs = new Vector<>(94);
    }

    static TechTree proposeTechs() {
        TechTree result = new TechTree();
        result.initialize();
        return result;
    }

    protected void add(int parent1, int parent2, String name) {
        int id = techs.size();
        if (parent1 < -1)   { return; }
        if (parent1 >= id)  { return; }
        if (parent2 < -1)   { return; }
        if (parent2 >= id)  { return; }
        if (contains(name)) { return; }
        if ((parent1 != -1) && (parent1 == parent2)) {
            return;
        }
        if (parent1 < parent2) {
            techs.add(new Technology(id, parent1, parent2, name));
        } else {
            techs.add(new Technology(id, parent2, parent1, name));
        }
    }

    protected void add(int parent1, String parent2, String name) {
        int id2 = getId(parent2);
        add(parent1, id2, name);
    }

    protected void add(String parent1, String parent2, String name) {
        int id1 = getId(parent1);
        int id2 = getId(parent2);
        add(id1, id2, name);
    }

    protected boolean contains(String name) {
        for (Technology tech : techs) {
            if (tech.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    protected int countNamedTechnologies() {
        return techs.size();
    }

    protected int getId(String name) {
        int numTechs = techs.size();
        for (int i = 0; i < numTechs; i++) {
            if (techs.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    protected BitSet getFreeTechs() {
        BitSet result = new BitSet(countNamedTechnologies());
        int numFreeTechs = countFreeTechs();
        result.set(0, numFreeTechs);
        return result;
    }

    protected int countFreeTechs() {
        return 16;
    }

    // Future technology numbers are 1-based.
    protected Technology getFutureTech(int arg) {
        if (arg < 0) {
            return null;
        }
        int numNamedTechs = countNamedTechnologies();
        if (numNamedTechs < 0) {
            return null;
        }
        if ((numNamedTechs == 0) && (arg == 1)) {
            return new Technology(0, -1, -1, "Future Tech 1");
        }
        int nextTech = numNamedTechs + arg - 1;
        return new Technology(nextTech, nextTech - 2, nextTech - 1, "Future Tech " + arg);
    }

    protected Technology getTech(int id) {
        if (id < 0) {
            return null;
        }
        int numNamedTechs = countNamedTechnologies();
        if (id < numNamedTechs) {
            return techs.get(id);
        }
        // Future technology numbers are 1-based.
        return getFutureTech(id - numNamedTechs + 1);
    }

    protected Vector<String> getNames() {
        int numTechs = techs.size();
        Vector<String> result = new Vector<>(numTechs);
        for (int i = 0; i < numTechs; i++) {
            result.add(i, techs.get(i).name);
        }
        return result;
    }

    protected void initialize() {
        add(-1, -1, "Hoot");           //  0
        add(-1,  0, "Clap");
        add(-1,  1, "Smash");
        add( 1,  2, "Throw");
        add( 0,  1, "Speech");
        add( 2,  4, "Wedge");          //  5
        add( 4,  5, "Sewing");
        add( 3,  5, "Flint");
        add( 4,  7, "Fire");
        add( 1,  7, "Domestication");
        add( 5,  6, "Pastoralism");    // 10
        add( 7,  8, "Mining");
        add( 8, 10, "Farming");
        add( 5, 12, "Road");
        add(12, 13, "Irrigation");
        add( 8,  9, "Cooking");        // 15
        ///////////////////////
        add( 8, 12, "Burial");
        add( 9, 10, "Horseback Riding");
        add( 8, 14, "Pottery");
        add(11, 13, "Masonry");
        add( 4, 18, "Alphabet");       // 20
        add( 4, 20, "Writing");
        add( 8, 16, "Mysticism");
        add(11, 15, "Bronze");
        add( 6, 23, "Currency");
        add( 9, 20, "Code of Laws");   // 25
        add(13, 18, "Wheel");
        add(13, 20, "Map");
        add(15, 23, "Iron");
        add(19, 20, "Arithmetic");
        add(16, 25, "Monarchy");       // 30
        add(12, 29, "Algebra");
        add(19, 24, "Construction");
        add(21, 25, "Literacy");
        add(24, 25, "Trade");
        add(19, 30, "Feudalism");      // 35
        add(22, 29, "Astrology");
        add(17, 30, "Chivalry");
        add(22, 33, "Philosophy");
        add(25, 33, "Republic");
        add(21, 38, "Religion");       // 40
        add(28, 32, "Bridge");
        add(26, 34, "Magnification");
        add(27, 36, "Navigation");
        add(31, 33, "Invention");
        add(31, 38, "University");     // 45
        add(26, 44, "Engineering");
        add(33, 38, "Democracy");
        add(26, 46, "Bicycle");
        add(28, 44, "Gunpowder");
        add(34, 38, "Medicine");       // 50
        add(34, 39, "Banking");
        add(31, 42, "Calculus");
        add(36, 42, "Astronomy");
        add(45, 49, "Metallurgy");
        add(43, 52, "Physics");        // 55
        add(45, 50, "Chemistry");
        add(43, 55, "Magnetism");
        add(46, 55, "Steam Engine");
        add(41, 58, "Railroad");
        add(49, 56, "Explosives");     // 60
        add(39, 60, "Conscription");
        add(51, 59, "Industrialization");
        add(38, 62, "Dialectic");
        add(35, 63, "Communism");
        add(54, 57, "Electricity");    // 65
        add(46, 65 ,"Electronics");
        add(29, 66, "Computers");
        add(55, 56, "Atomic Theory");
        add(51, 62, "Corporation");
        add(54, 62, "Steel");          // 70
        add(50, 67, "Gengineering");
        add(56, 69, "Refining");
        add(54, 72, "Plastics");
        add(60, 72, "Combustion");
        add(48, 74, "Automobile");     // 75
        add(55, 74, "Flight");
        add(76, 54, "Jet Engine");
        add(60, 77, "Rocket");
        add(53, 78, "Satellite");
        add(66, 76, "Radar");          // 80
        add(69, 75, "Mass Production");
        add(37, 81, "Lean Production");
        add(63, 81, "Recycling");
        add(64, 81, "Labor Union");
        add(67, 79, "Robotics");       // 85
        add(68, 80, "Laser");
        add(68, 81, "Nuclear Fission");
        add(73, 82, "Carbon Fiber");
        add(77, 80, "Stealth Plane");
        add(77, 87, "Nuclear Power");  // 90
        add(81, 88, "Superconductor");
        add(86, 88, "Skyhook");
        add(90, 91, "Fusion Power");
    }
}
