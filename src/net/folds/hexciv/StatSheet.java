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
    StatColumn governmentTypeId;
    StatColumn numTradeRoutes;

    StatColumn numWonders;
    StatColumn numSciImps;
    StatColumn numHappyImps;
    StatColumn numFoodImps;
    StatColumn numProdImps;
    StatColumn numMilImps;

    StatColumn numCaravans;
    StatColumn numPlanes;
    StatColumn numShips;
    StatColumn numTroops;
    StatColumn numSettlers;

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
        governmentTypeId = new StatColumn(startTurn); // 0 ~     10)

        numTradeRoutes = new StatColumn(startTurn); //(0 - ~    600)
        numWonders   = new StatColumn(startTurn); //  (0 - ~     22)
        numSciImps   = new StatColumn(startTurn); //  (0 - ~    400)
        numHappyImps = new StatColumn(startTurn); //  (0 - ~  1,400)
        numFoodImps  = new StatColumn(startTurn); //  (0 - ~    800)
        numProdImps  = new StatColumn(startTurn); //  (0 - ~  1,000)
        numMilImps   = new StatColumn(startTurn); //  (0 - ~    600)

        numCities.setMaxRange(10);
        numCitizens.setMaxRange(10);
        numMyriads.setMaxRange(10);
        governmentTypeId.setMaxRange(GovernmentType.getMaxId());

        numTradeRoutes.setMaxRange(10);
        numWonders.setMaxRange(10);
        numSciImps.setMaxRange(10);
        numHappyImps.setMaxRange(10);
        numFoodImps.setMaxRange(10);
        numProdImps.setMaxRange(10);
        numMilImps.setMaxRange(10);

        // units
        numCaravans = new StatColumn(startTurn);  //  (0 - ~    200)
        numPlanes   = new StatColumn(startTurn);  //  (0 - ~    100)
        numShips    = new StatColumn(startTurn);  //  (0 - ~    100)
        numTroops   = new StatColumn(startTurn);  //  (0 - ~  4,000)
        numSettlers = new StatColumn(startTurn);  //  (0 - ~    500)

        numCaravans.setMaxRange(10);
        numPlanes.setMaxRange(10);
        numShips.setMaxRange(10);
        numTroops.setMaxRange(10);
        numSettlers.setMaxRange(10);

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
        governmentTypeId.clear();
        numTradeRoutes.clear();

        numWonders.clear();
        numSciImps.clear();
        numHappyImps.clear();
        numFoodImps.clear();
        numProdImps.clear();
        numMilImps.clear();

        numCaravans.clear();
        numPlanes.clear();
        numShips.clear();
        numTroops.clear();
        numSettlers.clear();

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

        numCaravans.setMaxRange(10);
        numPlanes.setMaxRange(10);
        numShips.setMaxRange(10);
        numTroops.setMaxRange(10);
        numSettlers.setMaxRange(10);

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
    // #             settlers, troops, ships, planes, caravans
    //                                        (background stacked graph, auto-adjusting scale, starts at 0 - 100)


    public void incrementTurn() {
        currentTurn = currentTurn + 1;
    }

    public void recordCells(int numSeenCells) {
        this.numSeenCells.record(currentTurn, numSeenCells);
    }

    public void recordCities(int cities, int citizens, int myriads) {
        numCities.record(currentTurn, cities);
        numCitizens.record(currentTurn, citizens);
        numMyriads.record(currentTurn, myriads);
    }

    public void recordGovernmentType(int governmentTypeId) {
        this.governmentTypeId.record(currentTurn, governmentTypeId);
    }

    public void recordImprovements(Vector<Integer> counts, ImprovementVector improvementVector) {
        int numWonders = 0;
        int numSciImps = 0;
        int numHappyImps = 0;
        int numFoodImps = 0;
        int numProdImps = 0;
        int numMilImps = 0;
        if (counts == null) {
            return;
        }
        if (improvementVector == null) {
            return;
        }
        for (int i = 0; i < counts.size(); i++) {
            if (improvementVector.isWonder(i)) {
                numWonders += counts.get(i);
            } else if (improvementVector.helpsScience(i)) {
                numSciImps += counts.get(i);
            } else if (improvementVector.helpsHappiness(i)) {
                numHappyImps += counts.get(i);
            } else if (improvementVector.helpsFood(i)) {
                numFoodImps += counts.get(i);
            } else if (improvementVector.helpsProduction(i)) {
                numProdImps += counts.get(i);
            } else if (improvementVector.helpsMilitary(i)) {
                numMilImps += counts.get(i);
            }
        }
        this.numWonders.record(currentTurn, numWonders);
        this.numSciImps.record(currentTurn, numSciImps);
        this.numHappyImps.record(currentTurn, numHappyImps);
        this.numFoodImps.record(currentTurn, numFoodImps);
        this.numProdImps.record(currentTurn, numProdImps);
        this.numMilImps.record(currentTurn, numMilImps);
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

    public void recordThinkingTime(int thinkingTimeInMilliseconds) {
        this.thinkingTimeInMilliseconds.record(currentTurn, thinkingTimeInMilliseconds);
    }

    public void recordTradeRoutes(int numTradeRoutes) {
        this.numTradeRoutes.record(currentTurn, numTradeRoutes);
    }

    public void recordUnits(int caravans, int planes, int ships, int troops, int settlers) {
        numCaravans.record(currentTurn, caravans);
        numPlanes.record(currentTurn, planes);
        numShips.record(currentTurn, ships);
        numTroops.record(currentTurn, troops);
        numSettlers.record(currentTurn, settlers);
    }

    public void recordWip(int money, int production, int science) {
        storedMoney.record(currentTurn, money);
        storedProduction.record(currentTurn, production);
        storedScience.record(currentTurn, science);
    }

}
