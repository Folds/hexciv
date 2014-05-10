package net.folds.hexciv;

/**
 * Created by jasper on May 08, 2014.
 */
public class Technology {
    int id;
    int parent1;
    int parent2;
    String name;

    Technology(int id, int parent1, int parent2, String name) {
        this.id = id;
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.name = name;
    }
}
