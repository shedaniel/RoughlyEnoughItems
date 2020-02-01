/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.stripping;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public class DefaultStrippingDisplay implements RecipeDisplay {
    
    private EntryStack in, out;
    
    public DefaultStrippingDisplay(ItemStack in, ItemStack out) {
        this.in = EntryStack.create(in);
        this.out = EntryStack.create(out);
    }
    
    public final EntryStack getIn() {
        return in;
    }
    
    public final EntryStack getOut() {
        return out;
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        return Collections.singletonList(Collections.singletonList(in));
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return Collections.singletonList(out);
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.STRIPPING;
    }
    
    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return getInputEntries();
    }
}
