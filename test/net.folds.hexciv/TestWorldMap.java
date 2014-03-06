package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jasper on Feb 07, 2014.
 */
public class TestWorldMap {
    WorldMap emptyMap = new WorldMap();


    void testCharacter(int arg) {
        String character = BitSetPorter.chooseCharacter(arg);
        int number = BitSetPorter.fromCharacter(character);
        Assert.assertEquals(number, arg, "Expected " + arg + " -> String -> number to return " + arg);

    }

    @Test
    public void testEmptyBonuses() {
        String bonuses = emptyMap.stringifyBonuses();
        Assert.assertEquals(bonuses,"","Expected empty bonuses (for n=12) to be an empty string.");
    }
}