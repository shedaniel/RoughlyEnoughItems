/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.smelting;

import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.cooking.DefaultCookingDisplay;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.util.Identifier;

public class DefaultSmeltingDisplay extends DefaultCookingDisplay {
    
    public DefaultSmeltingDisplay(SmeltingRecipe recipe) {
        super(recipe);
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.SMELTING;
    }
}
