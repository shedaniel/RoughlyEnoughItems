package me.shedaniel.rei;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.RecipeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoughlyEnoughItemsCore {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelper();
    private static final ClientHelper CLIENT_HELPER = new ClientHelper();
    
    public static RecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    public static ClientHelper getClientHelper() {
        return CLIENT_HELPER;
    }
    
}
