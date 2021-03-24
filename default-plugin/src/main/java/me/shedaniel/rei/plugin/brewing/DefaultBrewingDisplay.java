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
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultBrewingDisplay implements Display {
    private EntryStack<?> output;
    private EntryIngredient reactant, input;
    
    @ApiStatus.Internal
    public DefaultBrewingDisplay(Ingredient input, Ingredient reactant, ItemStack output) {
        ItemStack[] inputItems = input.getItems();
        List<EntryStack<?>> i = new ArrayList<>(inputItems.length);
        for (ItemStack inputItem : inputItems) {
            i.add(EntryStacks.of(inputItem).tooltip(new TranslatableComponent("category.rei.brewing.input").withStyle(ChatFormatting.YELLOW)));
        }
        this.input = EntryIngredient.of(i);
        ItemStack[] reactantStacks = reactant.getItems();
        List<EntryStack<?>> r = new ArrayList<>(reactantStacks.length);
        for (ItemStack stack : reactantStacks) {
            r.add(EntryStacks.of(stack).tooltip(new TranslatableComponent("category.rei.brewing.reactant").withStyle(ChatFormatting.YELLOW)));
        }
        this.reactant = EntryIngredient.of(r);
        this.output = EntryStacks.of(output).tooltip(new TranslatableComponent("category.rei.brewing.result").withStyle(ChatFormatting.YELLOW));
    }
    
    @Override
    public List<EntryIngredient> getInputEntries() {
        return Lists.newArrayList(input, reactant);
    }
    
    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(EntryIngredient.of(output));
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.BREWING;
    }
    
    public List<EntryStack<?>> getOutput(int slot) {
        List<EntryStack<?>> stack = new ArrayList<>();
        for (int i = 0; i < slot * 2; i++)
            stack.add(EntryStack.empty());
        for (int i = 0; i < 6 - slot * 2; i++)
            stack.add(output);
        return stack;
    }
}
