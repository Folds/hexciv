package net.folds.hexciv;

import java.util.BitSet;
import java.util.Random;
import java.util.Vector;

/**
 * Created by jasper on May 08, 2014.
 */
public class TechKey {
    private BitSet key;
    private TechTree techs;
    protected int nextTech;

    public TechKey(TechTree techs) {
        this.techs = techs;
        key = techs.getFreeTechs();
        nextTech = -1;
    }

    protected Technology getTech(int techId) {
        return techs.getTech(techId);
    }

    protected int countFutureTech() {
        BitSet allNamedTech = allNamedTech();
        BitSet futureTech = (BitSet) key.clone();
        futureTech.andNot(allNamedTech);
        return futureTech.cardinality();
    }

    protected BitSet allNamedTech() {
        BitSet result = new BitSet(countNamedTechnologies());
        result.set(0, countNamedTechnologies());
        return result;
    }

    protected int getPriceOfNextTech(int techPriceFactor) {
        int numFreeTechs = techs.countFreeTechs();
        int numTechs = countTechs();
        return (numTechs + 1 - numFreeTechs) * techPriceFactor;
    }

    protected int countTechs() {
        return key.cardinality();
    }

    protected boolean isUndecided() {
        if (nextTech < 0) {
            return true;
        }
        return false;
    }

    protected boolean isNextTechComplete(int techSpending, int techPriceFactor) {
        if (isUndecided()) {
            return false;
        }
        int priceOfNextTech = getPriceOfNextTech(techPriceFactor);
        if (techSpending >= priceOfNextTech) {
            return true;
        }
        return false;
    }

    protected int countNamedTechnologies() {
        return techs.countNamedTechnologies();
    }

    protected Technology getNextTech() {
        if (nextTech < 0) {
            return null;
        }
        if (nextTech > techs.countNamedTechnologies() - 1) {
            return techs.getFutureTech(nextTech - countNamedTechnologies() + 1);
        }
        return techs.getTech(nextTech);
    }

    protected void advance() {
        if (nextTech >= 0) {
            key.set(nextTech);
        }
        nextTech = -1;
    }

    protected int countDiscoveredNamedTechnologies() {
        return countTechs() - countFutureTech();
    }

    protected boolean hasDiscoveredAllNamedTechnologies() {
        if (countDiscoveredNamedTechnologies() < countNamedTechnologies()) {
            return false;
        }
        return true;
    }

    protected int getFirstUndiscoveredFutureTech() {
        int numNamedTechs = countNamedTechnologies();
        if (numNamedTechs < 0) {
            return 0;
        }
        return key.nextClearBit(numNamedTechs);
    }

    protected boolean hasPrerequisitesFor(Technology tech) {
        int parent1 = tech.parent1;
        int parent2 = tech.parent2;
        if ((parent1 >=0) && (!key.get(parent1))) {
            return false;
        }
        if ((parent2 >=0) && (!key.get(parent2))) {
            return false;
        }
        return true;
    }

    protected boolean hasTech(int techId) {
        if (techId < 0) {
            return true;
        }
        return key.get(techId);
    }

    protected BitSet copyKey() {
        return (BitSet) key.clone();
    }

    protected Vector<Integer> getChoices() {
        if (hasDiscoveredAllNamedTechnologies()) {
            Vector<Integer> result = new Vector<>(1);
            result.add(getFirstUndiscoveredFutureTech());
            return result;
        } else {
            BitSet possibilities = allNamedTech();
            possibilities.andNot(key);
            int numNamedTechs = countNamedTechnologies();
            for (int i = 0; i < numNamedTechs; i++) {
                if (key.get(i)) {
                    Technology possibility = techs.getTech(i);
                    if (!hasPrerequisitesFor(possibility)) {
                        possibilities.clear(i);
                    }
                }
            }
            int numPossibilities = possibilities.cardinality();
            Random random = new Random();
            int maxResults = 3 + random.nextInt(5);
            if (numPossibilities <= maxResults) {
                return Util.convertToIntegerVector(possibilities);
            }
            Vector<Integer> result = new Vector<>(maxResults);
            for (int i = 0; i < maxResults; i++) {
                int j = random.nextInt(numPossibilities - i);
                int pos = Util.getNthPosition(possibilities, j);
                possibilities.clear(pos);
                result.add(pos);
            }
            return result;
        }
    }

}
