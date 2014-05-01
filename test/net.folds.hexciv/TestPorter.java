package net.folds.hexciv;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jasper on Feb 20, 2014.
 */
public class TestPorter {

    @Test
    public void testPortingWaterWorld() {
        WorldMap map = new WorldMap();
        String json = Porter.exportMapToString(map);
        String expected = "{\n"+
                          "  \"polarCircumferenceInKilometers\" : 40000.0,\n"+
                          "  \"meshSize\" : 12,\n"+
                          "  \"terrainStrings\" : [ \"1)VO2\", \"\", \"1)VO2\", \"1)VO2\" ],\n"+
                          "  \"bonusString\" : \"\",\n"+
                          "  \"roadString\" : \"\",\n"+
                          "  \"railroadString\" : \"\",\n"+
                          "  \"pollutionString\" : \"\",\n"+
                          "  \"irrigationString\" : \"\",\n"+
                          "  \"mineString\" : \"\",\n"+
                          "  \"villageString\" : \"\",\n"+
                          "  \"cityString\" : \"\"\n"+
                          "}";
        Assert.assertEquals(json, expected,
                            "Expected json of water world to include "+
                            "'[ \"1)VO2\", \"\", \"1)VO2\", \"1)VO2\" ]'.");
        WorldMap reconstitutedMap = Porter.importMapFromString(json);
        TerrainTypes terrain = reconstitutedMap.getTerrain(0);
        Assert.assertEquals(terrain, TerrainTypes.ocean,
                            "Expected reconstituted water world's south pole to be ocean.");
    }

    @Test
    public void testPorting() {
        WorldMap map = new WorldMap();
        map.setTerrain(0, TerrainTypes.glacier);
        map.setTerrain(1, TerrainTypes.mountains);
        map.setTerrain(2, TerrainTypes.desert);
        map.setTerrain(3, TerrainTypes.lake);
        map.setTerrain(4, TerrainTypes.plains);
        map.setTerrain(map.countCells() - 1, TerrainTypes.forest);
        String json = Porter.exportMapToString(map);
        String expected = "{\n"+
                          "  \"polarCircumferenceInKilometers\" : 40000.0,\n"+
                          "  \"meshSize\" : 12,\n"+
                          "  \"terrainStrings\" : [ \"3!3!VNX\", \"4!1!2\", \"1#VNZ\", \"5)1@1!VNW\" ],\n"+
                          "  \"bonusString\" : \"\",\n"+
                          "  \"roadString\" : \"\",\n"+
                          "  \"railroadString\" : \"\",\n"+
                          "  \"pollutionString\" : \"\",\n"+
                          "  \"irrigationString\" : \"\",\n"+
                          "  \"mineString\" : \"\",\n"+
                          "  \"villageString\" : \"\",\n"+
                          "  \"cityString\" : \"\"\n"+
                          "}";
        Assert.assertEquals(json, expected,
                            "Expected json of polar world to include "+
                            "'[\"3!3!VNX\",\"4!1!2\",\"1#VNZ\",\"5)1@1!VNW\"]'.");
        WorldMap reconstitutedMap = Porter.importMapFromString(json);
        TerrainTypes poles0000 = reconstitutedMap.getTerrain(0);
        Assert.assertEquals(poles0000, TerrainTypes.glacier,
                "Expected reconstituted polar world's south pole to be glacier.");
    }

    @Test
    public void testGlacierSouthPole() {
        Planet planet = new Planet(3600);
        IcosahedralMesh mesh = new IcosahedralMesh(1);
        WorldMap map = new WorldMap(planet, mesh);
        map.setTerrain(0, TerrainTypes.glacier);
        String json = Porter.exportMapToString(map);
        String expected = "{\n"+
                          "  \"polarCircumferenceInKilometers\" : 3600.0,\n"+
                          "  \"meshSize\" : 1,\n"+
                          "  \"terrainStrings\" : [ \"2!v\", \"\", \"2!v\", \"2)w\" ],\n"+
                          "  \"bonusString\" : \"\",\n"+
                          "  \"roadString\" : \"\",\n"+
                          "  \"railroadString\" : \"\",\n"+
                          "  \"pollutionString\" : \"\",\n"+
                          "  \"irrigationString\" : \"\",\n"+
                          "  \"mineString\" : \"\",\n"+
                          "  \"villageString\" : \"\",\n"+
                          "  \"cityString\" : \"\"\n"+
                          "}";
        Assert.assertEquals(json, expected,
                          "Expected json of moonlet to include "+
                         "'[ \"2!v\", \"\", \"2!v\", \"2)w\" ]'.");
        WorldMap reconstitutedMap = Porter.importMapFromString(json);
        TerrainTypes terrain00 = reconstitutedMap.getTerrain(0);
        Assert.assertEquals(terrain00, TerrainTypes.glacier,
                "Expected reconstituted moonlet's south pole to be glacier.");
    }
}