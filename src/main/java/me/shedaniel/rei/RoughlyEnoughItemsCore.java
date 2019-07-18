/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei;

import me.shedaniel.rei.api.*;
import me.shedaniel.rei.client.DisplayHelperImpl;
import me.shedaniel.rei.client.ItemRegistryImpl;
import me.shedaniel.rei.client.RecipeHelperImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoughlyEnoughItemsCore {
    
    public static final Logger LOGGER;
    public static final ExecutorService SYNC_RECIPES = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "REI-SyncRecipes"));
    private static final RecipeHelper RECIPE_HELPER = new RecipeHelperImpl();
    private static final ItemRegistry ITEM_REGISTRY = new ItemRegistryImpl();
    private static final DisplayHelper DISPLAY_HELPER = new DisplayHelperImpl();
    
    static {
        LOGGER = LogManager.getFormatterLogger("REI");
    }
    
    public static RecipeHelper getRecipeHelper() {
        return RECIPE_HELPER;
    }
    
    public static ItemRegistry getItemRegisterer() {
        return ITEM_REGISTRY;
    }
    
    public static DisplayHelper getDisplayHelper() {
        return DISPLAY_HELPER;
    }
    
    public static List<REIPluginEntry> getPlugins() {
        return new LinkedList<>(RoughlyEnoughItemsClient.plugins.values());
    }
    
    public static Optional<Identifier> getPluginIdentifier(REIPluginEntry plugin) {
        for(Identifier identifier : RoughlyEnoughItemsClient.plugins.keySet())
            if (identifier != null && RoughlyEnoughItemsClient.plugins.get(identifier).equals(plugin))
                return Optional.of(identifier);
        return Optional.empty();
    }
    
}
