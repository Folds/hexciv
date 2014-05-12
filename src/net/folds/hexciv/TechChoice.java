package net.folds.hexciv;

/**
 * Created by jasper on May 10, 2014.
 */
public class TechChoice {
    int techId;
    int obsoleterTechId;
    int obsoleterWonderId;

    protected TechChoice(int techId) {
        this.techId = techId;
        obsoleterTechId = -1;
        obsoleterWonderId = -1;
    }

    protected void obsolescedByTech(int obsoleterTechId) {
        this.obsoleterTechId = obsoleterTechId;
    }

    protected void obsolescedByWonder(int obsoleterWonderId) {
        this.obsoleterWonderId = obsoleterWonderId;
    }
}
