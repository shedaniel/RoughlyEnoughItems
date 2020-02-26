/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.plugin.cooking;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.Container;
import net.minecraft.item.Item;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class DefaultCookingDisplay implements TransferRecipeDisplay {
    private static List<EntryStack> fuel;
    
    static {
        fuel = FurnaceBlockEntity.createFuelTimeMap().keySet().stream().map(Item::getStackForRender).map(EntryStack::create).map(e -> e.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, stack -> Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.smelting.fuel")))).collect(Collectors.toList());
    }
    
    private AbstractCookingRecipe recipe;
    private List<List<EntryStack>> input;
    private List<EntryStack> output;
    private float xp;
    private double cookTime;
    
    public DefaultCookingDisplay(AbstractCookingRecipe recipe) {
        this.recipe = recipe;
        this.input = CollectionUtils.map(recipe.getPreviewInputs(), i -> CollectionUtils.map(i.getMatchingStacksClient(), EntryStack::create));
        this.output = Collections.singletonList(EntryStack.create(recipe.getOutput()));
        this.xp = recipe.getExperience();
        this.cookTime = recipe.getCookTime();
    }
    
    public static List<EntryStack> getFuel() {
        return fuel;
    }
    
    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(recipe).map(AbstractCookingRecipe::getId);
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
    
    public float getXp() {
        return xp;
    }
    
    public double getCookingTime() {
        return cookTime;
    }
    
    @ApiStatus.Internal
    public Optional<AbstractCookingRecipe> getOptionalRecipe() {
        return Optional.ofNullable(recipe);
    }
    
    @Override
    public int getWidth() {
        return 1;
    }
    
    @Override
    public int getHeight() {
        return 1;
    }
    
    @Override
    public List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<Container> containerInfo, Container container) {
        return input;
    }
    
}
