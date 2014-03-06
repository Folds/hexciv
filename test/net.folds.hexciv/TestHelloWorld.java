package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jasper on Feb 05, 2014.
 */
public class TestHelloWorld {

    @Test
    public static void saysHello() {
        String allegedGreeting = HelloWorld.sayHello();
        Assert.assertEquals(allegedGreeting, "Hello, World!",
                "HelloWorld.sayHello() did not say 'Hello, World!'.");
    }

}
