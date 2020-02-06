/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin;

import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.autocrafting.DefaultCategoryHandler;
import me.shedaniel.rei.plugin.autocrafting.DefaultRecipeBookHandler;
import net.minecraft.util.Identifier;

public class DefaultAutoCraftingPlugin implements REIPluginV0 {
    
    public static final Identifier PLUGIN = new Identifier("roughlyenoughitems", "default_auto_crafting_plugin");
    
    @Override
    public Identifier getPluginIdentifier() {
        return PLUGIN;
    }
    
    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        if (!ConfigObject.getInstance().isLoadingDefaultPlugin()) {
            return;
        }
        recipeHelper.registerAutoCraftingHandler(new DefaultCategoryHandler());
        recipeHelper.registerAutoCraftingHandler(new DefaultRecipeBookHandler());
    }
}
