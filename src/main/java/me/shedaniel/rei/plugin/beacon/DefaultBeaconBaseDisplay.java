/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.beacon;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.ItemStackHook;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public class DefaultBeaconBaseDisplay implements RecipeDisplay {
    
    private List<EntryStack> entries;
    
    public DefaultBeaconBaseDisplay(List<ItemStack> entries) {
        this.entries = CollectionUtils.map(entries, EntryStack::create);
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        return Collections.singletonList(entries);
    }
    
    public List<EntryStack> getEntries() {
        return entries;
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return Collections.emptyList();
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.BEACON;
    }
}
