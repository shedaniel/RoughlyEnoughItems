/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.jeicompat.wrap;

import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.client.view.Views;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIWrappedDisplay<T> implements Display {
    private final JEIWrappedCategory<T> backingCategory;
    private final T backingRecipe;
    private List<EntryIngredient> compiledInput;
    private List<EntryIngredient> compiledOutputs;
    
    public JEIWrappedDisplay(JEIWrappedCategory<T> backingCategory, T backingRecipe) {
        this.backingCategory = backingCategory;
        this.backingRecipe = backingRecipe;
        this.cache();
    }
    
    public void cache() {
        JEIRecipeLayoutBuilder builder = new JEIRecipeLayoutBuilder(null);
        IRecipeCategory<T> category = getBackingCategory().getBackingCategory();
        category.setRecipe(builder, getBackingRecipe(), getFoci());
        
        this.compiledInput = CollectionUtils.filterAndMap(builder.slots, role -> role.role == RecipeIngredientRole.INPUT || role.role == RecipeIngredientRole.CATALYST,
                slot -> EntryIngredient.of(slot.slot.getEntries()));
        this.compiledOutputs = CollectionUtils.filterAndMap(builder.slots, role -> role.role == RecipeIngredientRole.OUTPUT,
                slot -> EntryIngredient.of(slot.slot.getEntries()));
    }
    
    public JEIWrappedCategory<T> getBackingCategory() {
        return backingCategory;
    }
    
    public T getBackingRecipe() {
        return backingRecipe;
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return this.backingCategory.getCategoryIdentifier();
    }
    
    public static IFocusGroup getFoci() {
        ViewSearchBuilder context = Views.getInstance().getContext();
        if (context != null) {
            List<IFocus<?>> foci = new ArrayList<>();
            for (EntryStack<?> stack : context.getUsagesFor()) {
                if (stack != null && !stack.isEmpty()) {
                    foci.add(new JEIFocus<>(RecipeIngredientRole.INPUT, stack.typedJeiValue()));
                }
            }
            for (EntryStack<?> stack : context.getRecipesFor()) {
                if (stack != null && !stack.isEmpty()) {
                    foci.add(new JEIFocus<>(RecipeIngredientRole.OUTPUT, stack.typedJeiValue()));
                }
            }
            return new JEIFocusGroup(foci);
        } else if (Minecraft.getInstance().screen instanceof DisplayScreen) {
            List<IFocus<?>> foci = new ArrayList<>();
            DisplayScreen screen = (DisplayScreen) Minecraft.getInstance().screen;
            List<EntryStack<?>> notice = screen.getIngredientsToNotice();
            for (EntryStack<?> stack : notice) {
                if (stack != null && !stack.isEmpty()) {
                    foci.add(new JEIFocus<>(RecipeIngredientRole.INPUT, stack.typedJeiValue()));
                }
            }
            notice = screen.getResultsToNotice();
            for (EntryStack<?> stack : notice) {
                if (stack != null && !stack.isEmpty()) {
                    foci.add(new JEIFocus<>(RecipeIngredientRole.OUTPUT, stack.typedJeiValue()));
                }
            }
            return new JEIFocusGroup(foci);
        } else {
            return JEIFocusGroup.EMPTY;
        }
    }
    
    @Override
    public List<EntryIngredient> getInputEntries() {
        return compiledInput != null ? compiledInput : Collections.emptyList();
    }
    
    @Override
    public List<EntryIngredient> getOutputEntries() {
        return compiledOutputs != null ? compiledOutputs : Collections.emptyList();
    }
    
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        if (backingRecipe instanceof Recipe<?>) {
            return Optional.ofNullable(((Recipe<?>) backingRecipe).getId());
        }
        
        return Optional.empty();
    }
}
