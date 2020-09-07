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
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DefaultBrewingDisplay implements RecipeDisplay {
    
    private EntryStack output;
    private List<EntryStack> input, reactant;
    
    @ApiStatus.Internal
    public DefaultBrewingDisplay(Ingredient input, Ingredient reactant, ItemStack output) {
        ItemStack[] inputItems = input.getItems();
        this.input = new ArrayList<>(inputItems.length);
        for (ItemStack inputItem : inputItems) {
            EntryStack entryStack = EntryStack.create(inputItem);
            entryStack.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> Collections.singletonList(new TranslationTextComponent("category.rei.brewing.input").withStyle(TextFormatting.YELLOW)));
            this.input.add(entryStack);
        }
        ItemStack[] reactantStacks = reactant.getItems();
        this.reactant = new ArrayList<>(reactantStacks.length);
        for (ItemStack stack : reactantStacks) {
            EntryStack entryStack = EntryStack.create(stack);
            entryStack.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> Collections.singletonList(new TranslationTextComponent("category.rei.brewing.reactant").withStyle(TextFormatting.YELLOW)));
            this.reactant.add(entryStack);
        }
        this.output = EntryStack.create(output).setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, stack -> Collections.singletonList(new TranslationTextComponent("category.rei.brewing.result").withStyle(TextFormatting.YELLOW)));
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return Lists.newArrayList(input, reactant);
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(Collections.singletonList(output));
    }
    
    @Override
    public @NotNull ResourceLocation getRecipeCategory() {
        return DefaultPlugin.BREWING;
    }
    
    public List<EntryStack> getOutput(int slot) {
        List<EntryStack> stack = new ArrayList<>();
        for (int i = 0; i < slot * 2; i++)
            stack.add(EntryStack.empty());
        for (int i = 0; i < 6 - slot * 2; i++)
            stack.add(output);
        return stack;
    }
    
    @Override
    public @NotNull List<List<EntryStack>> getRequiredEntries() {
        return getInputEntries();
    }
}
