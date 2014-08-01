package net.folds.hexciv;

import java.util.Vector;

/**
 * Created by jasper on Jul 28, 2014.
 */
public class StatSheet {
    StatColumn luxuryPercentage;
    StatColumn sciencePercentage;
    StatColumn taxPercentage;

    StatColumn numCities;
    StatColumn numCitizens;
    StatColumn numMyriads;

    StatColumn numPlanes;
    StatColumn numSettlers;
    StatColumn numShips;
    StatColumn numTroops;

    StatColumn numTechs;
    StatColumn maxTechId;

    StatColumn storedMoney;
    StatColumn storedProduction;
    StatColumn storedScience;

    StatColumn numSeenCells;
    StatColumn thinkingTimeInMilliseconds;

    int startTurn;
    int currentTurn;
    int maxPossibleTurn;

    public StatSheet(int startTurn, int maxPossibleTurn) {
        this.startTurn       = startTurn;
        this.currentTurn     = startTurn;
        this.maxPossibleTurn = maxPossibleTurn;

        // percentages (0 - 100)
        luxuryPercentage  = new StatColumn(startTurn);
        taxPercentage     = new StatColumn(startTurn);
        sciencePercentage = new StatColumn(startTurn);

        // cities
        numCities   = new StatColumn(startTurn);  //  (0 - ~    200)
        numCitizens = new StatColumn(startTurn);  //  (0 - ~  4,000)
        numMyriads  = new StatColumn(startTurn);  //  (0 - ~ 40,000)

        numCities.setMaxRange(10);
        numCitizens.setMaxRange(10);
        numMyriads.setMaxRange(10);

        // units
        numPlanes   = new StatColumn(startTurn);  //  (0 - ~    100)
        numSettlers = new StatColumn(startTurn);  //  (0 - ~    500)
        numShips    = new StatColumn(startTurn);  //  (0 - ~    100)
        numTroops   = new StatColumn(startTurn);  //  (0 - ~  4,000)

        numPlanes.setMaxRange(10);
        numSettlers.setMaxRange(10);
        numShips.setMaxRange(10);
        numTroops.setMaxRange(10);

        // techs
        numTechs  = new StatColumn(startTurn);    //  (0 - ~    300)
        maxTechId = new StatColumn(startTurn);    //  (0 - ~    300)

        // work in process (wip)
        storedMoney = new StatColumn(startTurn);  //  (0 - ~100,000)
        storedProduction = new StatColumn(startTurn);//(0- ~ 40,000)
        storedScience = new StatColumn(startTurn); // (0 - ~ 15,000)

        numSeenCells = new StatColumn(startTurn);  // (0 -    4,332)

        thinkingTimeInMilliseconds = new StatColumn(startTurn);
    }

    protected void clear() {
        currentTurn = startTurn;
        luxuryPercentage.clear();
        taxPercentage.clear();
        sciencePercentage.clear();
        numCities.clear();
        numCitizens.clear();
        numMyriads.clear();
        numPlanes.clear();
        numSettlers.clear();
        numShips.clear();
        numTroops.clear();
        numTechs.clear();
        maxTechId.clear();
        storedMoney.clear();
        storedProduction.clear();
        storedScience.clear();
        numSeenCells.clear();
        thinkingTimeInMilliseconds.clear();

        numCities.setMaxRange(10);
        numCitizens.setMaxRange(10);
        numMyriads.setMaxRange(10);

        numPlanes.setMaxRange(10);
        numSettlers.setMaxRange(10);
        numShips.setMaxRange(10);
        numTroops.setMaxRange(10);
    }

    // reporting pages:

    // percentages:  luxuries, science, taxes (background stacked graph, 0 - 100)
    // vs. wip:      prod,     science, money (auto-adjusting scale, starts at 0 - 100)
    // vs. techs:    numTechs, maxTech        (auto-adjusting scale, starts at 0 - 100)

    // Progress
    // %:    Lux  Sci Tax
    // Wip:  Prod Sci Gold
    // Tech:       #  Max

    // People
    // #             seen cells               (scale 0% - 100%)
    // #             cities,                  (auto-adjusting scale, starts at 0 - 100)
    // #             citizens                 (auto-adjusting scale, starts at 0 - 100)
    // #             myriads                  (auto-adjusting scale, starts at 0 - 100)
    // #             planes, settlers, ships, troops (background stacked graph, auto-adjusting scale, starts at 0 - 100)


    public void incrementTurn() {
        currentTurn = currentTurn + 1;
    }

    public void recordPercentages(int luxuries, int science, int taxes) {
        luxuryPercentage.record(currentTurn, luxuries);
        sciencePercentage.record(currentTurn, science);
        taxPercentage.record(currentTurn, taxes);
    }

    public void recordTechs(int numTechs, int maxTechId) {
        this.numTechs.record(currentTurn, numTechs);
        this.maxTechId.record(currentTurn, maxTechId);
    }

    public void recordCities(int cities, int citizens, int myriads) {
        numCities.record(currentTurn, cities);
        numCitizens.record(currentTurn, citizens);
        numMyriads.record(currentTurn, myriads);
    }

    public void recordUnits(int planes, int settlers, int ships, int troops) {
        numPlanes.record(currentTurn, planes);
        numSettlers.record(currentTurn, settlers);
        numShips.record(currentTurn, ships);
        numTroops.record(currentTurn, troops);
    }

    public void recordWip(int money, int production, int science) {
        storedMoney.record(currentTurn, money);
        storedProduction.record(currentTurn, production);
        storedScience.record(currentTurn, science);
    }

    public void recordCells(int numSeenCells) {
        this.numSeenCells.record(currentTurn, numSeenCells);
    }

    public void recordThinkingTime(int thinkingTimeInMilliseconds) {
        this.thinkingTimeInMilliseconds.record(currentTurn, thinkingTimeInMilliseconds);
    }
}
