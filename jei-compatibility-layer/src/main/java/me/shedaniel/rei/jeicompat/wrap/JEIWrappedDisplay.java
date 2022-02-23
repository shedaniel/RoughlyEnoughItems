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
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtensionMethod(JEIPluginDetector.class)
public class JEIWrappedDisplay<T> implements Display {
    private final JEIWrappedCategory<T> backingCategory;
    private final T backingRecipe;
    private List<EntryIngredient> compiledInput;
    private List<EntryIngredient> compiledOutputs;
    private JEIIngredients ingredients = null;
    
    public JEIWrappedDisplay(JEIWrappedCategory<T> backingCategory, T backingRecipe) {
        this.backingCategory = backingCategory;
        this.backingRecipe = backingRecipe;
    }
    
    public static JEIIngredients createIngredients() {
        return new JEIIngredients();
    }
    
    public IIngredients getLegacyIngredients() {
        if (ingredients == null) {
            this.ingredients = createIngredients();
            backingCategory.getBackingCategory().setIngredients(this.backingRecipe, ingredients);
            this.compiledInput = new ArrayList<>();
            this.compiledOutputs = new ArrayList<>();
            ingredients.compileIngredients(compiledInput, compiledOutputs);
        }
        
        return ingredients;
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
    
    @Override
    public List<EntryIngredient> getInputEntries() {
        return compiledInput != null ? compiledInput : computeInput();
    }
    
    public static List<IFocus<?>> getFoci() {
        ViewSearchBuilder context = Views.getInstance().getContext();
        List<IFocus<?>> foci = new ArrayList<>();
        if (context != null) {
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
        } else if (Minecraft.getInstance().screen instanceof DisplayScreen) {
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
        }
        return foci;
    }
    
    private List<EntryIngredient> compute(RecipeIngredientRole role) {
        JEIRecipeLayoutBuilder builder = new JEIRecipeLayoutBuilder();
        builder.rolePredicate = role::equals;
        IRecipeCategory<T> category = getBackingCategory().getBackingCategory();
        category.setRecipe(builder, getBackingRecipe(), getFoci());
        return CollectionUtils.map(builder.slots, slot -> EntryIngredient.of(slot.slot.getEntries()));
    }
    
    private List<EntryIngredient> computeInput() {
        return compute(RecipeIngredientRole.INPUT);
    }
    
    private List<EntryIngredient> computeOutput() {
        return compute(RecipeIngredientRole.OUTPUT);
    }
    
    @Override
    public List<EntryIngredient> getOutputEntries() {
        return compiledOutputs != null ? compiledOutputs : computeOutput();
    }
    
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        if (backingRecipe instanceof Recipe<?>) {
            return Optional.ofNullable(((Recipe<?>) backingRecipe).getId());
        }
        
        return Optional.empty();
    }
}
