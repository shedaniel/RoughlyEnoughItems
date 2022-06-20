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

package me.shedaniel.rei.plugin.common.displays.brewing;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The default display for brewing recipes.
 *
 * @see BrewingRecipe
 */
@ApiStatus.Internal
public class DefaultBrewingDisplay implements Display {
    private EntryStack<?> output;
    private EntryIngredient reactant, input;
    
    public DefaultBrewingDisplay(BrewingRecipe recipe) {
        this(recipe.input, recipe.ingredient, recipe.output);
    }
    
    public DefaultBrewingDisplay(Ingredient input, Ingredient reactant, ItemStack output) {
        this(EntryIngredients.ofIngredient(input), EntryIngredients.ofIngredient(reactant), EntryStacks.of(output));
    }
    
    public DefaultBrewingDisplay(EntryIngredient input, EntryIngredient reactant, EntryStack<?> output) {
        this.input = input.map(stack -> stack.copy().tooltip(Component.translatable("category.rei.brewing.input").withStyle(ChatFormatting.YELLOW)));
        this.reactant = reactant.map(stack -> stack.copy().tooltip(Component.translatable("category.rei.brewing.reactant").withStyle(ChatFormatting.YELLOW)));
        this.output = output.copy().tooltip(Component.translatable("category.rei.brewing.result").withStyle(ChatFormatting.YELLOW));
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
    
    public static DisplaySerializer<DefaultBrewingDisplay> serializer() {
        return new DisplaySerializer<DefaultBrewingDisplay>() {
            @Override
            public CompoundTag save(CompoundTag tag, DefaultBrewingDisplay display) {
                tag.put("input", display.input.saveIngredient());
                tag.put("reactant", display.reactant.saveIngredient());
                tag.put("output", display.output.saveStack());
                return tag;
            }
            
            @Override
            public DefaultBrewingDisplay read(CompoundTag tag) {
                EntryIngredient input = EntryIngredient.read(tag.getList("input", Tag.TAG_COMPOUND));
                EntryIngredient reactant = EntryIngredient.read(tag.getList("reactant", Tag.TAG_COMPOUND));
                EntryStack<?> output = EntryStack.read(tag.getCompound("output"));
                return new DefaultBrewingDisplay(input, reactant, output);
            }
        };
    }
}
