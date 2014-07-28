package net.folds.hexciv;

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
            ruler.chooseFarms(map, city);
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
            ruler.chooseFarms(map, city);
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
        chooseWip();
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
            requestProfitableImprovement(12); // University
            requestProfitableImprovement(13); // Bank
            requestProfitableImprovement(11); // Cathedral
            requestProfitableImprovement( 6); // Library
            requestProfitableImprovement( 7); // Market
            requestProfitableImprovement(19); // Manufactory
            if (civ.isWonderCity(map, city, referee)) {
                requestProfitableWonder(31); // Circumnavigation
                requestProfitableWonder(23); // Pyramids
                requestProfitableWonder(35); // Timocracy
                requestProfitableWonder(40); // Cancer Cure
                requestProfitableWonder(39); // Hypertext
                requestProfitableWonder(37); // Great Dam
            }
            requestProfitableImprovement( 2); // Temple
            requestProfitableImprovement( 3); // Granary
        }
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
