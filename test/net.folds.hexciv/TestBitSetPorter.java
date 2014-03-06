package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Feb 07, 2014.
 */
public class TestBitSetPorter {

    TestBitSetPorter() {
    }

    void testCharacter(int arg) {
        String character = BitSetPorter.chooseCharacter(arg);
        int number = BitSetPorter.fromCharacter(character);
        Assert.assertEquals(number, arg, "Expected " + arg + " -> String -> number to return " + arg);

    }

    @Test
    public void testCharacters() {
        for (int i=0; i<72; i++) {
            testCharacter(i);
        }
    }

    BitSet getSampleBits() {
        BitSet bits = new BitSet(10000);
        bits.set(4,12,true);
        bits.set(25,true);
        bits.set(61,66,true);
        bits.set(78,85,true);
        bits.set(87,true);
        bits.set(89,true);
        bits.set(192,true);
        bits.set(300,500,true);
        bits.set(1000,6300,true);
        return bits;
    }

    private void testBitsToStrings(BitSet bits, String[] expected, String bitsDescription) {
        Vector<String> actual = BitSetPorter.bitsToStrings(bits);
        String expectedCount = expected.length + " substrings";
        if (expected.length == 1) {
            expectedCount = expected.length + " substring";
        }
        Assert.assertEquals(actual.size(), expected.length,
                            "Expected " + expectedCount + " from simple test bits stringification.");
        for (int i=0; i<expected.length; i++) {
            boolean verification = (actual.get(i).equals(expected[i]));
            if (!verification) {
                Vector<String> retry = BitSetPorter.bitsToStrings(bits);
            }
            Assert.assertTrue(verification,
                           "Expected substring " + i + " from " + bitsDescription +
                                   " to be '" + expected[i] +"', not '" + actual.get(i) + "'");
        }
    }

    @Test
    public void testSimpleBitsToStrings() {
        BitSet bits = new BitSet(32);
        bits.set(0,true);
        String[] expected = {"1)","11"};
        testBitsToStrings(bits, expected, "simple test bits");
    }

    @Test
    public void testSimpleStringToBits() {

    }

    @Test
    public void testBitsToStrings() {
        BitSet bits = getSampleBits();
        String[] expected = {"1$","18","1D","11",  "1Z","15", "1C", "17", "1@",
                             "11","1!","11","3zzW","11","Y1z","V2K","Y6W","W11*"};
        Vector<String> actual = BitSetPorter.bitsToStrings(bits);
        testBitsToStrings(bits, expected, "test bits");
    }

    @Test
    public void testBitsToString() {
        BitSet bits = getSampleBits();
        String string = BitSetPorter.bitsToString(bits);
        Assert.assertEquals(string, "g$8D1Z5C7@1!1zzW1Y1zV2KY6WW11*",
                            "Expected the test bits to stringify to "+
                            "'g$8D1Z5C7@1!1zzW1Y1zV2KY6WW11*'.");
    }

    @Test
    public void testBitsFromString() {
        BitSet bits = getSampleBits();
        String string = BitSetPorter.bitsToString(bits);
        BitSet twin = BitSetPorter.stringToBits(string, 10000);
        for (int i = 0; i< bits.size(); i++) {
            if (bits.get(i) != twin.get(i)) {
                String expected = Util.toString(bits.get(i));
                Assert.assertEquals(bits.get(i), twin.get(i),
                        "Expected twin("+i+") = bits("+i+") = "+expected);
            }
        }
        Assert.assertTrue(bits.equals(twin),
                "Expected the test bits to resurrect to an identical twin.");
        String twinString = BitSetPorter.bitsToString(twin);
        Assert.assertEquals(string, twinString,
                "Expected the test bits and identical twin to stringify to matching strings.");
    }

}
