/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.fuel;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public class DefaultFuelDisplay implements RecipeDisplay {
    private EntryStack fuel;
    private int fuelTime;
    
    public DefaultFuelDisplay(EntryStack fuel, int fuelTime) {
        this.fuel = fuel;
        this.fuelTime = fuelTime;
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        return Collections.singletonList(Collections.singletonList(fuel));
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return Collections.emptyList();
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.FUEL;
    }
    
    public int getFuelTime() {
        return fuelTime;
    }
}
