package net.folds.hexciv;

import java.util.BitSet;

/**
 * Created by jasper on May 08, 2014.
 */
public class TechKey {
    private BitSet key;
    private TechTree techs;

    public TechKey(TechTree techs) {
        this.techs = techs;
        key = techs.getFreeTechs();
    }

    protected int countFutureTech() {
        BitSet allNamedTech = new BitSet(techs.countNamedTechnologies());
        allNamedTech.set(0, techs.countNamedTechnologies());
        BitSet futureTech = (BitSet) key.clone();
        futureTech.andNot(allNamedTech);
        return futureTech.cardinality();
    }
}
