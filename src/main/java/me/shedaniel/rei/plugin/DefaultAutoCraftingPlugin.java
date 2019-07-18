/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import me.shedaniel.rei.RoughlyEnoughItemsClient;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.plugin.autocrafting.AutoCraftingTableBookHandler;
import me.shedaniel.rei.plugin.autocrafting.AutoFurnaceBookHandler;
import me.shedaniel.rei.plugin.autocrafting.AutoInventoryBookHandler;

public class DefaultAutoCraftingPlugin implements REIPluginEntry {
    
    public static final Identifier PLUGIN = new Identifier("roughlyenoughitems", "default_auto_crafting_plugin");
    
    @Override
    public Identifier getPluginIdentifier() {
        return PLUGIN;
    }
    
    @Override
    public void onFirstLoad(PluginDisabler pluginDisabler) {
        if (!RoughlyEnoughItemsClient.getConfigManager().getConfig().loadDefaultPlugin) {
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_ITEMS);
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_CATEGORIES);
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_RECIPE_DISPLAYS);
            pluginDisabler.disablePluginFunction(PLUGIN, PluginFunction.REGISTER_OTHERS);
        }
    }
    
    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        recipeHelper.registerAutoCraftingHandler(new AutoCraftingTableBookHandler());
        recipeHelper.registerAutoCraftingHandler(new AutoInventoryBookHandler());
        recipeHelper.registerAutoCraftingHandler(new AutoFurnaceBookHandler());
    }
    
}
