package net.folds.hexciv;

import java.util.BitSet;
import java.util.Vector;

/**
 * Created by jasper on Jul 27, 2014.
 */
public class CityPlayer {

    private WorldMap map;
    private Civilization civ;
    private City city;
    private CivPlayer ruler;
    private GameListener listener;
    private ClaimReferee referee;

    protected CityPlayer(WorldMap map, City city, CivPlayer ruler, GameListener listener, ClaimReferee referee) {
        this.map = map;
        this.city = city;
        this.civ = city.civ;
        this.ruler = ruler;
        this.listener = listener;
        this.referee = referee;
    }

    protected void playTurn() {
        if (city.location < 0) {
            return;
        }
        if (city.storedFood < 0) {
            if (city.countSettlers() > 0) {
                int unitIndex = civ.getIndexOfFurthestSettler(map, city);
                listener.bemoanUnsupported(city, city.units.get(unitIndex));
                city.units.remove(unitIndex);
                city.storedFood = 0;
            } else {
                city.size = city.size - 1;
                city.storedFood = 0;
                listener.bemoanFamine(city);
                if (city.farms.size() > city.size) {
                    Integer farm = chooseWorstFarm();
                    city.farms.remove(farm);
                } else {
                    if (city.numTaxMen > 0) {
                        city.numTaxMen = city.numTaxMen - 1;
                    } else if (city.numScientists > 0) {
                        city.numScientists = city.numScientists - 1;
                    } else if (city.numEntertainers > 0) {
                        city.numEntertainers = city.numEntertainers - 1;
                    }
                }
            }
            chooseFarms();
        }
        if (city.storedFood >= 10 * city.size + 10) {
            city.size = city.size + 1;
            if (city.hasGranary()) {
                city.storedFood = city.storedFood - 5 * city.size;
            } else {
                city.storedFood = city.storedFood - 10 * city.size;
            }
            int newFarm = ruler.chooseFarm(map, city);
            if (newFarm >= 0) {
                city.farms.add(newFarm);
            }
            chooseFarms();
            listener.repaintMaps(city.location);
        }
        if ((city.wip != null) && (city.storedProduction >= city.wip.getCapitalCost())) {
            if ((!civ.isObsoleteUnit(city.wip)) && (!civ.isUnavailableImprovement(city.wip, city, referee))) {
                city.produce(listener, referee);
            }
        }
        int ore = civ.countOre(map, city, referee);
        int science = civ.countScience(map, city, referee);
        int tax = civ.countTax(map, city, referee);
        int happy = civ.countHappyCitizens(map, city, referee);
        int unhappy = civ.countUnhappyCitizens(map, city, referee);
        boolean hasPawnedImprovementThisTurn = false;
        if (unhappy > happy) {
            civ.bemoanDisorder(city, listener);
            tax = 0;
            if (!civ.isMilitarist()) {
                ore = 0;
            }
        } else {
            int logisticsCost = civ.getLogisticsCost(city);
            while (logisticsCost > ore + city.storedProduction) {
                civ.abandonFurthestUnitThatNeedsMaintenance(map, city, listener);
                logisticsCost = civ.getLogisticsCost(city);
            }
            int upkeepCost = city.getUpkeepCost();
            if (upkeepCost > tax + civ.storedMoney) {
                pawnLowestValueImprovement(city, listener);
                hasPawnedImprovementThisTurn = true;
            }
        }
        if ((!hasPawnedImprovementThisTurn) && (city.hasBarracks()) && (!wantBarracks())) {
            pawnBarracks(city, listener);
            hasPawnedImprovementThisTurn = true;
        }
        city.storedFood = city.storedFood + civ.countFoodSurplus(map, city);
        city.storedProduction = city.storedProduction + ore;
        civ.storedMoney = civ.storedMoney + tax - city.getUpkeepCost();
        civ.storedScience = civ.storedScience + science;
        if ((civ.storedScience > 0) && (civ.techKey.isUndecided())) {
            ruler.chooseNextTech();
        }
        if (civ.techKey.isNextTechComplete(civ.storedScience, civ.techPriceFactor)) {
            Technology tech = civ.techKey.getNextTech();
            civ.techKey.advance();
            listener.celebrateDiscovery(civ, tech);
            referee.claimTech(tech.id);
            civ.storedScience = civ.storedScience - civ.techKey.getPriceOfNextTech(civ.techPriceFactor);
        }
        if ((civ.storedScience > 0) && (civ.techKey.isUndecided())) {
            ruler.chooseNextTech();
        }
        if (!civ.isMilitarist()) {
            if (!city.isNone()) {
                int paranoiaLevel = ruler.getParanoiaLevel();
                int numNativeOccupationUnits = civ.countNativeMilitaryUnitsIn(city);
                if (numNativeOccupationUnits > paranoiaLevel) {
                    tryToDisbandLeastUsefulNativeOccupationUnit();
                }
            }
        }
        chooseWip();
    }

    protected void tryToDisbandLeastUsefulNativeOccupationUnit() {
        if (city.units == null) {
            return;
        }
        int worstUnitId = -1;
        int worstValue = -1;
        for (int i = 0; i < city.units.size(); i++) {
            Unit unit = city.units.get(i);
            if (!unit.unitType.isCaravan && unit.unitType.isTerrestrial && !unit.unitType.hasExtraLandVision
                && !unit.unitType.hasExtraNavalVision && !unit.unitType.ignoresWalls && !unit.unitType.isDiplomat
                && !unit.unitType.isSlippery && (unit.unitType.aviatorCapacity == 0) && (unit.unitType.capacity == 0)) {
                int rawDefenseValue = unit.unitType.defenseStrength;
                int rawOffenseValue = unit.unitType.attackStrength * unit.unitType.mobility;
                int vets = 100;
                if (unit.isVeteran) {
                    vets = 150;
                }
                int walls = 100 + city.getDefenseBonus();
                int defenseValue = (walls * 150 * vets * rawDefenseValue) / (100 * 100 * 100);
                int offenseValue = (vets * rawOffenseValue) / 100;

                int value = defenseValue;
                if (offenseValue > value) {
                    value = offenseValue;
                }
                value = 10 * value - 3 * unit.unitType.feedingCost;
                if (worstUnitId < 0) {
                    worstValue = value;
                }
                if (value <= worstValue) {
                    worstValue = value;
                    worstUnitId = i;
                }
            }
        }
        if (worstUnitId >= 0) {
            listener.celebrateUnsupported(city, city.units.get(worstUnitId));
            city.units.remove(worstUnitId);
        }
    }

    protected void chooseFarms() {
        Vector<Integer> region = map.getRegion(city.location, 2);
        int regionSize = region.size();
        Vector<Integer> potentialFarms = new Vector<>(regionSize);
        for (int pos = 0; pos < regionSize; pos++) {
            int cellId = region.get(pos);
            if (   (cellId != city.location)
                && (   (referee.isAvailable(cellId, civ))
                    || (city.farms.contains((Integer) cellId))
               )   ) {
                potentialFarms.add(cellId);
            }
        }
        int numChoices = potentialFarms.size();
        int maxFarms = Math.min(city.size, numChoices);
        int foodSupply  = civ.countFood(map, city.location);
        int oreSupply   = civ.countOre(map, city.location);
        int moneySupply = civ.countMoney(map, city.location);
        int foodNeeded = 2 * city.size + city.countSettlers();
        if (!civ.isMilitarist()) {
            foodNeeded = foodNeeded + city.countSettlers();
        }
        int prodNeeded = civ.getLogisticsCost(city);
        Vector<Integer> potentialFood  = new Vector<>(numChoices);
        Vector<Integer> potentialOre   = new Vector<>(numChoices);
        Vector<Integer> potentialMoney = new Vector<>(numChoices);
        for (int pos = 0; pos < numChoices; pos++) {
            int cellId = potentialFarms.get(pos);
            potentialFood.add(civ.countFood(map, cellId));
            potentialOre.add(civ.countOre(map, cellId));
            potentialMoney.add(civ.countMoney(map, cellId));
        }
        BitSet choices = new BitSet(numChoices);
        for (int i = 0; (i < maxFarms) && (foodSupply < foodNeeded); i++) {
            int bestPosSoFar = -1;
            int bestFoodSoFar = 0;
            int tieBreakerOre = 0;
            int tieBreakerBonus = 0;
            for (int j = 0; j < numChoices; j++) {
                if (!choices.get(j)) {
                    int food = potentialFood.get(j);
                    if (   (food > bestFoodSoFar)
                        || ((food == bestFoodSoFar) && (potentialOre.get(j) > tieBreakerOre))
                        || (   (food == bestFoodSoFar)
                            && (potentialOre.get(j) == tieBreakerOre)
                            && (getBonusValue(food, potentialOre.get(j), potentialMoney.get(j)) > tieBreakerBonus)
                           )
                       ) {
                        int ore = potentialOre.get(j);
                        int money = potentialMoney.get(j);
                        bestPosSoFar = j;
                        bestFoodSoFar = food;
                        tieBreakerOre = ore;
                        tieBreakerBonus = getBonusValue(food, ore, money);
                    }
                }
            }
            if (bestPosSoFar >= 0) {
                choices.set(bestPosSoFar);
                foodSupply = foodSupply + bestFoodSoFar;
                oreSupply = oreSupply + tieBreakerOre;
                moneySupply = moneySupply + potentialMoney.get(bestPosSoFar);
            }
        }
        for (int i = choices.cardinality(); (i < maxFarms) && (oreSupply < prodNeeded); i++) {
            int bestPosSoFar = -1;
            int bestOreSoFar = 0;
            int tieBreakerBonus = 0;
            for (int j = 0; j < numChoices; j++) {
                if (!choices.get(j)) {
                    int ore = potentialOre.get(j);
                    if (   (ore > bestOreSoFar)
                        || (   (ore == bestOreSoFar)
                            && (getBonusValue(potentialFood.get(j), ore, potentialMoney.get(j)) > tieBreakerBonus)
                           )
                       ) {
                        int food = potentialFood.get(j);
                        int money = potentialMoney.get(j);
                        bestPosSoFar = j;
                        bestOreSoFar = ore;
                        tieBreakerBonus = getBonusValue(food, ore, money);
                    }
                }
            }
            if (bestPosSoFar >= 0) {
                choices.set(bestPosSoFar);
                foodSupply = foodSupply + potentialFood.get(bestPosSoFar);
                oreSupply = oreSupply + bestOreSoFar;
                moneySupply = moneySupply + potentialMoney.get(bestPosSoFar);
            }
        }
        for (int i = choices.cardinality(); i < maxFarms; i++) {
            int bestPosSoFar = -1;
            int bestBonusSoFar = 0;
            for (int j = 0; j < numChoices; j++) {
                if (!choices.get(j)) {
                    int food = potentialFood.get(j);
                    int ore = potentialOre.get(j);
                    int money = potentialMoney.get(j);
                    int bonus = getBonusValue(food, ore, money);
                    if (bonus > bestBonusSoFar) {
                        bestPosSoFar = j;
                        bestBonusSoFar = bonus;
                    }
                }
            }
            if (bestPosSoFar >= 0) {
                choices.set(bestPosSoFar);
                foodSupply  = foodSupply  + potentialFood.get(bestPosSoFar);
                oreSupply   = oreSupply   + potentialOre.get(bestPosSoFar);
                moneySupply = moneySupply + potentialMoney.get(bestPosSoFar);
            }
        }
        if (   (foodSupply > foodNeeded) && (oreSupply < prodNeeded)
            && (choices.cardinality() > 0) && (choices.cardinality() < numChoices)) {
            boolean mightSwap = true;
            while ((mightSwap) && (foodSupply > foodNeeded) && (oreSupply < prodNeeded)) {
                int mostOreChosenSoFar = 0;
                for (int i = 0; i < numChoices; i++) {
                    if (choices.get(i) && (potentialOre.get(i) > mostOreChosenSoFar))
                        mostOreChosenSoFar = potentialOre.get(i);
                }
                int posLeastOreChosen = -1;
                int leastOreChosenSoFar = mostOreChosenSoFar;
                for (int i = 0; i < numChoices; i++) {
                    if (choices.get(i) && (potentialOre.get(i) < leastOreChosenSoFar)) {
                        posLeastOreChosen = i;
                        leastOreChosenSoFar = potentialOre.get(i);
                    }
                }
                int posMostOreNotChosen = -1;
                int mostOreNotChosenSoFar = 0;
                for (int i = 0; i < numChoices; i++) {
                    if ((!choices.get(i)) && (potentialOre.get(i) > mostOreNotChosenSoFar))
                        posMostOreNotChosen = i;
                        mostOreNotChosenSoFar = potentialOre.get(i);
                }
                if (   (mostOreNotChosenSoFar > leastOreChosenSoFar)
                    && (posLeastOreChosen >= 0)
                    && (posMostOreNotChosen >= 0)
                    && (foodSupply - potentialFood.get(posLeastOreChosen) + potentialFood.get(posMostOreNotChosen) >= foodNeeded)
                   ) {
                    choices.set(posLeastOreChosen, false);
                    choices.set(posMostOreNotChosen);
                } else {
                    mightSwap = false;
                }
            }
        }
        // To-do:  further optimize choices.
        city.farms.removeAllElements();
        for (int i = 0; i < numChoices; i++) {
            if (choices.get(i)) {
                city.farms.add(potentialFarms.get(i));
            }
        }
        int numFarmers = city.farms.size();
        if (numFarmers + city.numEntertainers + city.numScientists + city.numTaxMen > city.size) {
            city.numTaxMen = Math.max(0, city.size - city.numEntertainers - city.numScientists - numFarmers);
            if (numFarmers + city.numEntertainers + city.numScientists > city.size) {
                city.numScientists = Math.max(0, city.size - city.numEntertainers - numFarmers);
                if (numFarmers + city.numEntertainers > city.size) {
                    city.numEntertainers = Math.max(0, city.size - numFarmers);
                }
            }
        }
    }

    protected int getBonusValue(int food, int ore, int money) {
        return 30 * food + 15 * ore + 10 * money;
    }

    protected boolean canSustainExplorer() {
        UnitType unitType = chooseExplorer();
        if (unitType.isSettler) {
            return canSustainSettler();
        }
        if (civ.isAnarchist()) {
            return true;
        }
        int foodSurplus = civ.countFoodSurplus(map, city);
        int productionSurplus = civ.countProductionSurplus(map, city, referee);
        if (civ.isDespotic()) {
            int potentialLogistics = 0;
            if (city.units != null) {
                for (Unit unit : city.units) {
                    potentialLogistics = potentialLogistics + unit.unitType.logisticsCost;
                }
            }
            if (potentialLogistics >= city.size) {
                if (unitType.logisticsCost > productionSurplus) {
                    return false;
                }
            }
            if (potentialLogistics + unitType.logisticsCost > city.size + productionSurplus) {
                return false;
            }
        } else if (civ.isMilitarist()) {
            int logisticsCost = civ.getLogisticsCost(city);
            if (logisticsCost + unitType.logisticsCost > productionSurplus) {
                return false;
            }
        } else {
            int logisticsCost = civ.getLogisticsCost(city);
            int farmId = chooseWorstFarm();
            int productionValue = (civ.countOre(map, farmId) * civ.getProductionFactor(map, city, referee)) / 100;
            if (logisticsCost + unitType.logisticsCost + productionValue > productionSurplus) {
                return false;
            }
            if (civ.countFood(map, farmId) + unitType.feedingCost > foodSurplus) {
                return false;
            }
        }
        return true;
    }

    protected boolean canSustainSettler() {
        if (city.size < 2) {
            return false;
        }
        int foodSurplus = civ.countFoodSurplus(map, city);
        int farmId = chooseWorstFarm();
        if ((!civ.isMilitarist()) && (foodSurplus < civ.countFood(map, farmId))) {
            return false;
        }
        if (foodSurplus + 1 < civ.countFood(map, farmId)) {
            return false;
        }
        int productionSurplus = civ.countProductionSurplus(map, city, referee);
        if (productionSurplus * 100 < civ.countOre(map, farmId) * civ.getProductionFactor(map, city, referee)) {
            return false;
        }
        return true;
    }

    protected boolean canSustainSettlerBySacrificingExplorer() {
        if (city.size < 2) {
            return false;
        }
        int foodSurplus = civ.countFoodSurplus(map, city);
        int farmId = chooseWorstFarm();
        if ((!civ.isMilitarist()) && (foodSurplus < civ.countFood(map, farmId))) {
            return false;
        }
        if (foodSurplus + 1 < civ.countFood(map, farmId)) {
            return false;
        }
        int productionSurplus = civ.countProductionSurplus(map, city, referee);
        int potentialSurplus = productionSurplus;
        if (civ.doesCityHaveExplorerWithLogisticsCost(map, city)) {
            potentialSurplus = potentialSurplus + 1;
        }
        if (potentialSurplus * 100 < civ.countOre(map, farmId) * civ.getProductionFactor(map, city, referee)) {
            return false;
        }
        return true;
    }

    protected UnitType chooseExplorer() {
        int upkeep = getHighestUpkeepCostOfExplorer();
        int capitalCost = 0;
        int mobility = -1;
        UnitType result = null;
        for (UnitType unitType : civ.unitTypes) {
            if ((unitType.mobility > 0) && (civ.techKey.hasTech(unitType.technologyIndex))) {
                int unitUpkeep = estimateUpkeepCostOfExplorer(unitType);
                if (    (unitUpkeep <  upkeep)
                        || ((unitUpkeep == upkeep) && (unitType.mobility > mobility))
                        || ((unitUpkeep == upkeep) && (unitType.mobility == mobility) && (unitType.capitalCost <= capitalCost))
                        ) {
                    upkeep = 2 * unitType.feedingCost + unitType.logisticsCost;
                    mobility = unitType.mobility;
                    result = unitType;
                }
            }
        }
        return result;
    }

    protected void chooseWip() {
        int happy = civ.countHappyCitizens(map, city, referee);
        int unhappy = civ.countUnhappyCitizens(map, city, referee);

        if ((city.wip == null) || (city.countUnits() > 2)) {
            requestCheapestImprovement();
        }
        if (   (city.countUnits() - city.countSettlers() - civ.countNativeOccupiers(city) == 0) && (ruler.wantMoreExplorers(map))
                && (canSustainExplorer())
                ) {
            requestExplorer();
        }
        if ((city.countSettlers() == 0) && (canSustainSettler())) {
            city.wip = new ProductType(UnitType.proposeSettler());
        } else if ((city.countSettlers() == 0) && (canSustainSettlerBySacrificingExplorer())) {
            city.wip = new ProductType(UnitType.proposeSettler());
        }
        if (city.countSettlers() > 0) {
            civ.requestUnit(city, 9); // Caravan
            requestProfitableImprovement(13); // Factory
            requestProfitableImprovement(14); // Hydro Plant
            requestProfitableImprovement(15); // Power Plant
            requestProfitableImprovement(19); // Nuclear Plant
            requestProfitableImprovement( 9); // Colosseum
            requestProfitableImprovement(11); // University
            requestProfitableImprovement(12); // Bank
            requestProfitableImprovement(10); // Cathedral
            requestProfitableImprovement( 5); // Library
            requestProfitableImprovement( 7); // Courthouse
            requestProfitableImprovement( 6); // Market
            requestProfitableImprovement( 8); // Aqueduct
            requestProfitableImprovement(18); // Manufactory
            requestProfitableImprovement(16); // Subway
            requestProfitableImprovement(17); // Recycler
            if (isWonderCity()) {
                requestProfitableWonder(31); // Circumnavigation
                requestProfitableWonder(23); // Pyramids
                requestProfitableWonder(35); // Timocracy
                requestProfitableWonder(40); // Cancer Cure
                requestProfitableWonder(39); // Hypertext
                requestProfitableWonder(37); // Great Dam
            }
            requestProfitableImprovement( 8); // Aqueduct
            requestProfitableImprovement( 1); // Temple
            requestProfitableImprovement( 2); // Granary
        }
        /*
            requestProfitableImprovement( 0); // Barracks
            requestProfitableImprovement( 3); // City Walls
            requestProfitableImprovement( 4); // Palace
            requestProfitableImprovement(20); // Missile Defense
         */
        if ((civ.isMilitarist()) && (unhappy > 0) && (happy < unhappy)) {
            UnitType cheapestOccupier = civ.getCheapestOccupier();
            if (   (cheapestOccupier != null)
                    && (city.storedProduction < cheapestOccupier.capitalCost)
                    && ((civ.countProductionSurplus(map, city, referee) >= 1) || (willNextMilitaryUnitForage()))
                    ) {
                requestCheapestOccupier();
            }
        }

/*
        if ((city.wip == null) && (city.countUnits() == 0)) {
            city.wip = new ProductType(UnitType.proposeMilitia());
        }
        if (!city.hasBarracks()) {
            requestImprovement(city, 16); // Barracks
        }
        if (canSustainSettler(map, city, referee)) {
            if ((isMilitarist()) && (city.countUnits() > 0) && (unhappy < happy)) {
                city.wip = new ProductType(UnitType.proposeSettler());
            }
            if ((isMilitarist()) && (city.countUnits() > 0) && (unhappy == happy)) {
                city.wip = new ProductType(UnitType.proposeSettler());
            }
            if ((isMilitarist()) && (city.countUnits() > 0) && (unhappy == happy + 1)) {
                city.wip = new ProductType(UnitType.proposeSettler());
            }
            if ((!isMilitarist()) && (city.countUnits() > 0) && (unhappy <= happy)) {
                city.wip = new ProductType(UnitType.proposeSettler());
            }
        }
*/
    }

    protected int chooseWorstFarm() {
        int result = -1;
        int worstMetricSoFar = 999;
        for (int farm : city.farms) {
            int metric = civ.countOre(map, farm) + civ.countFood(map, farm) + civ.countMoney(map, farm);
            if (metric < worstMetricSoFar) {
                worstMetricSoFar = metric;
                result = farm;
            }
        }
        return result;
    }

    protected int estimateUpkeepCostOfExplorer(UnitType unitType) {
        int result = estimateUpkeepCostOfOccupier(unitType);
        if ((civ.canAttack(unitType)) && (!civ.isMilitarist())) {
            int farmId = chooseWorstFarm();
            int moneyFactor = (  civ.getLuxuryPercentage() * civ.getLuxuryFactor(map, city, referee)
                    + civ.sciencePercentage * civ.getScienceFactor(map, city, referee)
                    + civ.taxPercentage * civ.getTaxFactor(map, city, referee)
            ) / 100;
            int farmValue = (  300 * civ.countFood(map, farmId)
                    + 2 * civ.countOre(map, farmId) * civ.getProductionFactor(map, city, referee)
                    + civ.countMoney(map, farmId) * moneyFactor
            ) / 100;
            result = result + farmValue;
        }
        return result;
    }

    protected int estimateUpkeepCostOfOccupier(UnitType unitType) {
        return 3 * unitType.feedingCost + 2 * unitType.logisticsCost;
    }

    protected int getHighestUpkeepCostOfExplorer() {
        int result = 0;
        for (UnitType unitType : civ.unitTypes) {
            int upkeepCost = estimateUpkeepCostOfExplorer(unitType);
            if ((unitType.isTerrestrial) && (upkeepCost > result)) {
                result = upkeepCost;
            }
        }
        return result;
    }

    protected boolean isGoodTradeRoute(int cellId) {
        int income = civ.getTradeRouteIncome(map, city, cellId, referee);
        int threshold = civ.getLowestTradeRouteIncome(map, city, referee);
        if (income > threshold) {
            return true;
        }
        return false;
    }

    protected boolean isImmediatelyProfitable(int improvementId) {
        ImprovementType impType = civ.improvements.get(improvementId);
        if (city.storedProduction > impType.capitalCost) {
            return false;
        }
        if ((impType.isGranary) && (civ.countFoodSurplus(map, city) > 0)) {
            return true;
        }
        int scienceValue = (impType.scienceBonus * civ.countScience(map, city, referee) * 100) / civ.getScienceFactor(map, city, referee);
        int productionValue = (impType.productionBonus * civ.countOre(map, city, referee) * 200) / civ.getProductionFactor(map, city, referee);
        int luxuryValue = (impType.tradeBonus * civ.countScience(map, city, referee) * 100) /  civ.getLuxuryFactor(map, city, referee);
        int taxValue = (impType.tradeBonus * civ.countScience(map, city, referee) * 100) /  civ.getTaxFactor(map, city, referee);
        int incrementalValue = scienceValue + productionValue + luxuryValue + taxValue;
        if (incrementalValue > 100 * impType.upkeepCost) {
            return true;
        }
        return false;
    }

    protected boolean isWonderCity() {
        if (civ.countProductionSurplus(map, city, referee) >= 5) {
            return true;
        }
        return false;
    }

    protected void pawnLowestValueImprovement(City city, GameListener listener) {
        int improvementIndex = city.getLowestValueImprovement();
        if (improvementIndex >= 0) {
            ImprovementType improvementType = city.improvements.getImprovementType(improvementIndex);
            if (improvementType != null) {
                listener.bemoanUnsupported(city, improvementType);
                city.improvements.clear(improvementIndex);
                civ.storedMoney = civ.storedMoney + improvementType.resaleValue;
            }
        }
    }

    protected void pawnBarracks(City city, GameListener listener) {
        int improvementIndex = city.getLowestValueBarracks();
        if (improvementIndex >= 0) {
            ImprovementType improvementType = city.improvements.getImprovementType(improvementIndex);
            listener.celebrateUnsupported(city, improvementType);
            city.improvements.clear(improvementIndex);
            civ.storedMoney = civ.storedMoney + improvementType.resaleValue;
        }
    }

    protected void requestCheapestImprovement() {
        ImprovementType cheapestImprovement = civ.getCheapestImprovement(city);
        if (cheapestImprovement != null) {
            if (   (city.wip == null)
                    || (city.wip.isUnitType)
                    || (city.wip.improvementType != cheapestImprovement)
                    ) {
                city.wip = new ProductType(cheapestImprovement);
            }
        }
    }

    protected void requestCheapestOccupier() {
        UnitType cheapestOccupier = civ.getCheapestOccupier();
        if (cheapestOccupier != null) {
            if (   (!city.wip.isUnitType)
                    || (city.wip.unitType != cheapestOccupier)
                    ) {
                city.wip = new ProductType(cheapestOccupier);
            }
        }
    }

    protected void requestExplorer() {
        UnitType unitType = chooseExplorer();
        if (unitType != null) {
            city.wip = new ProductType(unitType);
        }
    }

    protected void requestImprovement(int improvementId) {
        if (city.improvements.types.get(improvementId).isWonder()) {
            return; // use requestWonder() instead.
        }
        if (   (!city.improvements.get(improvementId))
                && (civ.techKey.hasTech(civ.improvements.get(improvementId).technologyIndex))
                ) {
            city.wip = new ProductType(civ.improvements.get(improvementId));
        }
    }

    protected void requestProfitableImprovement(int improvementId) {
        if (isImmediatelyProfitable(improvementId)) {
            requestImprovement(improvementId);
        }
    }

    protected void requestProfitableWonder(int wonderId) {
        if (!civ.isNonObsoleteWonder(wonderId, referee)) {
            return;
        }
        civ.requestWonder(city, wonderId, referee);
    }

    protected boolean willNextMilitaryUnitForage() {
        if (civ.isAnarchist()) {
            return true;
        }
        if (!civ.isDespotic()) {
            return false;
        }
        int potentialLogisticsCost = 0;
        for (Unit unit : city.units) {
            potentialLogisticsCost = potentialLogisticsCost + unit.unitType.logisticsCost;
        }
        if (potentialLogisticsCost < city.size) {
            return true;
        }
        return false;
    }

    protected boolean wantBarracks() {
        return false;
    }

}