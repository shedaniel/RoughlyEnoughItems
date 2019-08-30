/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.autocrafting.DefaultCategoryHandler;
import me.shedaniel.rei.plugin.autocrafting.DefaultRecipeBookHandler;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionParsingException;
import net.minecraft.util.Identifier;

public class DefaultAutoCraftingPlugin implements REIPluginV0 {
    
    public static final Identifier PLUGIN = new Identifier("roughlyenoughitems", "default_auto_crafting_plugin");
    
    @Override
    public Identifier getPluginIdentifier() {
        return PLUGIN;
    }
    
    @Override
    public SemanticVersion getMinimumVersion() throws VersionParsingException {
        return SemanticVersion.parse("3.0-pre");
    }
    
    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        if (!RoughlyEnoughItemsCore.getConfigManager().getConfig().isLoadingDefaultPlugin()) {
            return;
        }
        recipeHelper.registerAutoCraftingHandler(new DefaultCategoryHandler());
        recipeHelper.registerAutoCraftingHandler(new DefaultRecipeBookHandler());
    }
}
