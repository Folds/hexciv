package net.folds.hexciv;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * Created by Jasper on Jan 31, 2014.
 */
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println(sayHello());
    }

    public static String sayHello() {
        return "Hello, World!";
    }

}