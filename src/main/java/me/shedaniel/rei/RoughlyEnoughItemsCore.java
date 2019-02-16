package me.shedaniel.rei;

import me.shedaniel.rei.api.IItemRegisterer;
import me.shedaniel.rei.api.IPluginDisabler;
import me.shedaniel.rei.client.ItemListHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.plugin.PluginManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoughlyEnoughItemsCore {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelper();
    private static final ItemListHelper ITEM_LIST_HELPER = new ItemListHelper();
    private static final PluginManager PLUGIN_MANAGER = new PluginManager();
    
    public static RecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    public static IItemRegisterer getItemRegisterer() {
        return ITEM_LIST_HELPER;
    }
    
    public static IPluginDisabler getPluginDisabler() {
        return PLUGIN_MANAGER;
    }
    
}
