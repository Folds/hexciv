package net.folds.hexciv;

import java.awt.*;

/**
 * Created by jasper on May 02, 2014.
 */
public interface ClaimReferee {
    void claimTech(int technologyId);
    void claimWonder(int wonderId);
    int countBenefitFromWondersThatAffectAllCivilizations(WorldMap map, City city, int benefitId);
    int countRawTrade(int cityLocation);
    boolean hasNonobsoleteElectrifiedWonder();
    boolean isAvailable(int cellId, Civilization civ);
    boolean isAvailable(int wonderId);
    boolean isObsolete(ImprovementType improvementType);
    boolean isObsolete(int wonderId);
    int getCityCellId(int farmCellId);
    Color getCityColor(int cellId);
    int getCitySize(int cellId);
    String getCityName(int cellId);
}
