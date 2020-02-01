/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.smoking;

import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.cooking.DefaultCookingDisplay;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.util.Identifier;

public class DefaultSmokingDisplay extends DefaultCookingDisplay {
    
    public DefaultSmokingDisplay(SmokingRecipe recipe) {
        super(recipe);
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.SMOKING;
    }
}
