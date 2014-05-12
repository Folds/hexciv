package net.folds.hexciv;

import java.util.BitSet;
import java.util.Random;
import java.util.Vector;

/**
 * Created by jasper on May 10, 2014.
 */
public class TechChooser {
    Vector<TechChoice> priorities;

    protected TechChooser() {
        initialize();
    }

    protected void add(int techId) {
        TechChoice priority = new TechChoice(techId);
        priorities.add(priority);
    }

    protected void addTemporary(int techId, int obsoleterTechId) {
        TechChoice priority = new TechChoice(techId);
        priority.obsolescedByTech(obsoleterTechId);
        priorities.add(priority);
    }

    protected boolean isGoodChoice(Vector<Integer> choices, TechKey key, int techId) {
        if (!key.hasTech(techId)) {
            TechChoice priority = getTechChoice(techId);
            if (!isObsolete(priority, key)) {
                if (choices.contains(techId)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected int chooseTech(Vector<Integer> choices, TechKey key, int depth) {
        int numChoices = choices.size();
        if (numChoices < 1) {
            return -1;
        }
        if (numChoices == 1) {
            return choices.get(0);
        }
        int numNamedTechs = key.countNamedTechnologies();
        int numPriorities = priorities.size();
        for (int i = 0; i < numPriorities; i++) {
            int id = priorities.get(i).techId;
            Vector<Integer> previousLevel = new Vector<>(1);
            if (!key.hasTech(id)) {
                TechChoice techChoice = getTechChoice(id);
                if (!isObsolete(techChoice, key)) {
                    previousLevel.add(id);
                }
            }
            for (int j = 0; j < depth; j++) {
                if (previousLevel.size() > 0) {
                    Vector<Integer> possibilities = new Vector<>(2 * previousLevel.size());
                    for (int previousPossibility : previousLevel) {
                        Technology tech = key.getTech(previousPossibility);
                        int parent1 = tech.parent1;
                        int parent2 = tech.parent2;
                        if (!key.hasTech(parent1)) {
                            possibilities.add(parent1);
                         }
                        if (!key.hasTech(parent2)) {
                            possibilities.add(parent2);
                        }
                    }
                    previousLevel = Util.deduplicate(possibilities);
                }
            }
            if (previousLevel.size() > 0) {
                Vector<Integer> maybes = new Vector<>(previousLevel.size());
                for (int possibility : previousLevel) {
                    if (choices.contains(possibility)) {
                        maybes.add(possibility);
                    }
                }
                if (maybes.size() == 1) {
                    return maybes.get(0);
                }
                if (maybes.size() > 1) {
                    Random random = new Random();
                    return maybes.get(random.nextInt(maybes.size()));
                }
            }
        }
        return -1;
    }

    protected int chooseTech(Vector<Integer> choices, TechKey key) {
        int result = -1;
        int numChoices = choices.size();
        if (numChoices < 1) {
            return result;
        }
        int numNamedTechs = key.countNamedTechnologies();
        int depth = 10;
        if (numNamedTechs < depth) {
            depth = numNamedTechs;
        }
        for (int i = 0; i < depth; i++) {
            result = chooseTech(choices, key, depth);
            if (result >= 0) {
                return result;
            }
        }
        Random random = new Random();
        return choices.get(random.nextInt(numChoices));
    }

    protected boolean isObsolete(TechChoice techChoice, TechKey key) {
        if (techChoice == null) {
            return false;
        }
        if (techChoice.obsoleterTechId < 0) {
            return false;
        }
        if (key.hasTech(techChoice.obsoleterTechId)) {
            return true;
        }
        TechChoice obsoleterTechChoice = getTechChoice(techChoice.obsoleterTechId);
        if (obsoleterTechChoice == null) {
            return false;
        }
        return isObsolete(obsoleterTechChoice, key);
    }

    protected TechChoice getTechChoice(int techId) {
        if (priorities.size() < 1) {
            return null;
        }
        for (TechChoice techChoice : priorities) {
            if (techChoice.techId == techId) {
                return techChoice;
            }
        }
        return null;
    }

    protected void initialize() {
        priorities = new Vector<>(28);
        add( 9); // Domestication
        add(13); // Road
        add(47); // Democracy
        add(67); // Computers
        add(59); // Railroad
        add(14); // Irrigation
        add(40); // Religion
        add(62); // Industrialization
        add(11); // Mining
        add(83); // Recycling
        add(75); // Automobile
        add(85); // Robotics
        add(16); // Burial
        add(21); // Writing
        add(34); // Trade
        add(66); // Electronics
        add(71); // Gengineering
        add(74); // Combustion
        add(24); // Currency
        add(41); // Bridge
        add(22); // Mysticism
        add(84); // Labor Union
        addTemporary(37, 75); // Chivalry until Automobile
        addTemporary(26,37); // Wheel until Chivalry (or Automobile)
        addTemporary(23,37); // Bronze until Chivalry (or Automobile)
        addTemporary(43,62); // Navigation until Industrialization
        addTemporary(27,43); // Map until Navigation (or Industrialization)
        addTemporary(19,47); // Masonry until Democracy
    }
}
