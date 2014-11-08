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

    protected void advance() {
        if (nextTech >= 0) {
            key.set(nextTech);
        }
        nextTech = -1;
    }

    protected BitSet allNamedTech() {
        BitSet result = new BitSet(countNamedTechnologies());
        result.set(0, countNamedTechnologies());
        return result;
    }

    protected Vector<String> allNames() {
        return techs.getNames();
    }

    protected void claimTech(int techId) {
        if (techId >= 0) {
            key.set(techId);
        }
    }

    protected int countDiscoveredNamedTechnologies() {
        return countTechs() - countFutureTech();
    }

    protected int countFutureTech() {
        BitSet allNamedTech = allNamedTech();
        BitSet futureTech = (BitSet) key.clone();
        futureTech.andNot(allNamedTech);
        return futureTech.cardinality();
    }

    protected int countNamedTechnologies() {
        return techs.countNamedTechnologies();
    }

    protected int countTechs() {
        return key.cardinality();
    }

    protected BitSet getAllChoices() {
        if (hasDiscoveredAllNamedTechnologies()) {
            int pos = getFirstUndiscoveredFutureTech();
            BitSet result = new BitSet(pos + 1);
            result.set(pos);
            return result;
        }
        BitSet result = allNamedTech();
        result.andNot(key);
        int numNamedTechs = countNamedTechnologies();
        for (int i = 0; i < numNamedTechs; i++) {
            if (result.get(i)) {
                Technology possibility = techs.getTech(i);
                if (!hasPrerequisitesFor(possibility)) {
                    result.clear(i);
                }
            }
        }
        return result;
    }

    protected Vector<Integer> getChoices() {
        BitSet possibilities = getAllChoices();
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
            if (pos < 0) {
                break;
            }
            possibilities.clear(pos);
            result.add(pos);
        }
        return result;
    }

    protected int getFirstUndiscoveredFutureTech() {
        int numNamedTechs = countNamedTechnologies();
        if (numNamedTechs < 0) {
            return 0;
        }
        return key.nextClearBit(numNamedTechs);
    }

    protected String getNameOfHighestDiscoveredTech() {
        int futureTech = countFutureTech();
        if (futureTech > 0) {
            return "Future Tech " + futureTech;
        }
        int id = getIdOfHighestDiscoveredTech();
        if (id < 0) {
            return "No Tech";
        }
        return techs.getTech(id).name;
    }

    protected int getIdOfHighestDiscoveredTech() {
        int id = -1;
        int numTechs = countTechs();
        int result = id;
        for (int i = 0; i < numTechs; i++) {
            id = key.nextSetBit(id + 1);
            if (id < 0) {
                break;
            }
            result = id;
        }
        return result;
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

    protected int getPriceOfNextTech(int techPriceFactor) {
        int numFreeTechs = techs.countFreeTechs();
        int numTechs = countTechs();
        return (numTechs + 1 - numFreeTechs) * techPriceFactor;
    }

    protected Technology getTech(int techId) {
        return techs.getTech(techId);
    }

    protected boolean hasDiscoveredAllNamedTechnologies() {
        if (countDiscoveredNamedTechnologies() < countNamedTechnologies()) {
            return false;
        }
        return true;
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

    protected boolean isUndecided() {
        if (nextTech < 0) {
            return true;
        }
        return false;
    }

    protected String summarizeAllChoices() {
        BitSet choices = getAllChoices();
        Vector<Integer> ids = Util.convertToIntegerVector(choices);
        String result = "";
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                result = result + ", ";
            }
            result = result + techs.getTech(ids.get(i)).name;
        }
        return result;
    }


}
