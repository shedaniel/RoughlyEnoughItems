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

package me.shedaniel.rei.plugin.brewing;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultBrewingDisplay implements RecipeDisplay {
    
    private EntryStack input, output;
    private List<EntryStack> reactant;
    
    public DefaultBrewingDisplay(ItemStack input, Ingredient reactant, ItemStack output) {
        this.input = EntryStack.create(input).setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, stack -> Collections.singletonList(new TranslatableText("category.rei.brewing.input").formatted(Formatting.YELLOW)));
        this.reactant = new ArrayList<>();
        for (ItemStack stack : reactant.getMatchingStacksClient()) {
            EntryStack entryStack = EntryStack.create(stack);
            entryStack.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> Collections.singletonList(new TranslatableText("category.rei.brewing.reactant").formatted(Formatting.YELLOW)));
            this.reactant.add(entryStack);
        }
        this.output = EntryStack.create(output).setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, stack -> Collections.singletonList(new TranslatableText("category.rei.brewing.result").formatted(Formatting.YELLOW)));
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        return Lists.newArrayList(Collections.singletonList(input), reactant);
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.BREWING;
    }
    
    public List<EntryStack> getOutput(int slot) {
        List<EntryStack> stack = new ArrayList<>();
        for (int i = 0; i < slot * 2; i++)
            stack.add(EntryStack.empty());
        for (int i = 0; i < 6 - slot * 2; i++)
            stack.addAll(getOutputEntries());
        return stack;
    }
    
    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return getInputEntries();
    }
}
