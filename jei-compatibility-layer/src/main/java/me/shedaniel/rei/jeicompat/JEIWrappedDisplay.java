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

package me.shedaniel.rei.jeicompat;

import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.entry.EntryDefinition;
import me.shedaniel.rei.api.ingredient.util.EntryIngredients;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.util.CollectionUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrapEntryDefinition;

public class JEIWrappedDisplay<T> implements Display {
    private final JEIWrappedCategory<T> backingCategory;
    private final T backingRecipe;
    private final Map<IIngredientType<?>, List<? extends List<?>>> inputs = new HashMap<>();
    private final Map<IIngredientType<?>, List<? extends List<?>>> outputs = new HashMap<>();
    private final List<EntryIngredient> compiledInput = new ArrayList<>();
    private final List<EntryIngredient> compiledOutputs = new ArrayList<>();
    private final IIngredients ingredients;
    
    public JEIWrappedDisplay(JEIWrappedCategory<T> backingCategory, T backingRecipe) {
        this.backingCategory = backingCategory;
        this.backingRecipe = backingRecipe;
        this.ingredients = new IIngredients() {
            @Override
            public void setInputIngredients(@NotNull List<Ingredient> inputs) {
                this.setInputLists(VanillaTypes.ITEM, CollectionUtils.map(inputs, ingredient -> Arrays.asList(ingredient.getItems())));
            }
            
            @Override
            public <R> void setInput(@NotNull IIngredientType<R> ingredientType, @NotNull R input) {
                List<List<R>> ingredient = (List<List<R>>) inputs.computeIfAbsent(ingredientType, e -> new ArrayList<>());
                ingredient.add(Collections.singletonList(input));
            }
            
            @Override
            public <R> void setInputs(@NotNull IIngredientType<R> ingredientType, @NotNull List<R> input) {
                List<List<R>> ingredient = (List<List<R>>) inputs.computeIfAbsent(ingredientType, e -> new ArrayList<>());
                ingredient.addAll(CollectionUtils.map(input, Collections::singletonList));
            }
            
            @Override
            public <R> void setInputLists(@NotNull IIngredientType<R> ingredientType, @NotNull List<List<R>> input) {
                List<List<R>> ingredient = (List<List<R>>) inputs.computeIfAbsent(ingredientType, e -> new ArrayList<>());
                ingredient.addAll(input);
            }
            
            @Override
            public <R> void setOutput(@NotNull IIngredientType<R> ingredientType, @NotNull R output) {
                List<List<R>> ingredient = (List<List<R>>) outputs.computeIfAbsent(ingredientType, e -> new ArrayList<>());
                ingredient.add(Collections.singletonList(output));
            }
            
            @Override
            public <R> void setOutputs(@NotNull IIngredientType<R> ingredientType, @NotNull List<R> output) {
                List<List<R>> ingredient = (List<List<R>>) outputs.computeIfAbsent(ingredientType, e -> new ArrayList<>());
                ingredient.addAll(CollectionUtils.map(output, Collections::singletonList));
            }
            
            @Override
            public <R> void setOutputLists(@NotNull IIngredientType<R> ingredientType, @NotNull List<List<R>> output) {
                List<List<R>> ingredient = (List<List<R>>) outputs.computeIfAbsent(ingredientType, e -> new ArrayList<>());
                ingredient.addAll(output);
            }
            
            @Override
            @NotNull
            public <R> List<List<R>> getInputs(@NotNull IIngredientType<R> ingredientType) {
                return (List<List<R>>) inputs.getOrDefault(ingredientType, Collections.emptyList());
            }
            
            @Override
            @NotNull
            public <R> List<List<R>> getOutputs(@NotNull IIngredientType<R> ingredientType) {
                return (List<List<R>>) outputs.getOrDefault(ingredientType, Collections.emptyList());
            }
        };
        setupIngredients();
        compileIngredients();
    }
    
    private void compileIngredients() {
        for (Map.Entry<IIngredientType<?>, List<? extends List<?>>> entry : inputs.entrySet()) {
            EntryDefinition<?> definition = wrapEntryDefinition(entry.getKey());
            for (List<?> slot : entry.getValue()) {
                compiledInput.add(EntryIngredients.of((EntryDefinition<Object>) definition, (List<Object>) slot));
            }
        }
        
        for (Map.Entry<IIngredientType<?>, List<? extends List<?>>> entry : outputs.entrySet()) {
            EntryDefinition<?> definition = wrapEntryDefinition(entry.getKey());
            for (List<?> slot : entry.getValue()) {
                compiledOutputs.add(EntryIngredients.of((EntryDefinition<Object>) definition, (List<Object>) slot));
            }
        }
    }
    
    private void setupIngredients() {
        backingCategory.getBackingCategory().setIngredients(this.backingRecipe, ingredients);
    }
    
    public IIngredients getIngredients() {
        return ingredients;
    }
    
    public JEIWrappedCategory<T> getBackingCategory() {
        return backingCategory;
    }
    
    public T getBackingRecipe() {
        return backingRecipe;
    }
    
    @Override
    public ResourceLocation getCategoryIdentifier() {
        return this.backingCategory.getIdentifier();
    }
    
    @Override
    public List<EntryIngredient> getInputEntries() {
        return compiledInput;
    }
    
    @Override
    public List<EntryIngredient> getResultingEntries() {
        return compiledOutputs;
    }
}
