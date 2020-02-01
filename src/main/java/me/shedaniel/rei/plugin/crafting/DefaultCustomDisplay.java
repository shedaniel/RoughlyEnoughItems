/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin.crafting;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;

public class DefaultCustomDisplay implements DefaultCraftingDisplay {
    
    private List<List<EntryStack>> input;
    private List<EntryStack> output;
    private Recipe<?> possibleRecipe;
    private int width, height;
    
    public DefaultCustomDisplay(List<List<ItemStack>> input, List<ItemStack> output, Recipe<?> possibleRecipe) {
        this(possibleRecipe, CollectionUtils.map(input, i -> CollectionUtils.map(i, EntryStack::create)), CollectionUtils.map(output, EntryStack::create));
    }
    
    public DefaultCustomDisplay(Recipe<?> possibleRecipe, List<List<EntryStack>> input, List<EntryStack> output) {
        this.input = input;
        this.output = output;
        this.possibleRecipe = possibleRecipe;
        List<Boolean> row = Lists.newArrayList(false, false, false);
        List<Boolean> column = Lists.newArrayList(false, false, false);
        for (int i = 0; i < 9; i++)
            if (i < this.input.size()) {
                List<EntryStack> stacks = this.input.get(i);
                if (stacks.stream().anyMatch(stack -> !stack.isEmpty())) {
                    row.set((i - (i % 3)) / 3, true);
                    column.set(i % 3, true);
                }
            }
        this.width = (int) column.stream().filter(Boolean::booleanValue).count();
        this.height = (int) row.stream().filter(Boolean::booleanValue).count();
    }
    
    public DefaultCustomDisplay(List<List<ItemStack>> input, List<ItemStack> output) {
        this(input, output, null);
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Recipe getPossibleRecipe() {
        return possibleRecipe;
    }
    
    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(possibleRecipe).map(Recipe::getId);
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        return input;
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return output;
    }
    
    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return input;
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
    @Override
    public Optional<Recipe<?>> getOptionalRecipe() {
        return Optional.ofNullable(possibleRecipe);
    }
    
}
