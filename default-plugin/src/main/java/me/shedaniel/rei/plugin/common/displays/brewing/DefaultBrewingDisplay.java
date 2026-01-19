/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The default display for brewing recipes.
 *
 * @see BrewingRecipe
 */
@ApiStatus.Internal
public class DefaultBrewingDisplay implements Display {
    public static final DisplaySerializer<DefaultBrewingDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().fieldOf("input").forGetter(d -> d.input),
                    EntryIngredient.codec().fieldOf("reactant").forGetter(d -> d.reactant),
                    EntryIngredient.codec().fieldOf("output").forGetter(d -> d.output)
            ).apply(instance, DefaultBrewingDisplay::new)),
            StreamCodec.composite(
                    EntryIngredient.streamCodec(),
                    d -> d.input,
                    EntryIngredient.streamCodec(),
                    d -> d.reactant,
                    EntryIngredient.streamCodec(),
                    d -> d.output,
                    DefaultBrewingDisplay::new
            ));
    
    private final EntryIngredient reactant;
    private final EntryIngredient input;
    private final EntryIngredient output;
    
    public DefaultBrewingDisplay(BrewingRecipe recipe) {
        this(recipe.input(), recipe.ingredient(), recipe.output());
    }
    
    public DefaultBrewingDisplay(Ingredient input, Ingredient reactant, ItemStack output) {
        this(EntryIngredients.ofIngredient(input), EntryIngredients.ofIngredient(reactant), EntryIngredients.of(output));
    }
    
    public DefaultBrewingDisplay(EntryIngredient input, EntryIngredient reactant, EntryIngredient output) {
        this.input = input.map(stack -> stack.copy().tooltip(Component.translatable("category.rei.brewing.input").withStyle(ChatFormatting.YELLOW)));
        this.reactant = reactant.map(stack -> stack.copy().tooltip(Component.translatable("category.rei.brewing.reactant").withStyle(ChatFormatting.YELLOW)));
        this.output = output.map(stack -> stack.copy().tooltip(Component.translatable("category.rei.brewing.result").withStyle(ChatFormatting.YELLOW)));
    }
    
    @Override
    public List<EntryIngredient> getInputEntries() {
        return Lists.newArrayList(input, reactant);
    }
    
    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(output);
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.BREWING;
    }
    
    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.empty();
    }
    
    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
    
    public List<EntryStack<?>> getOutput(int slot) {
        List<EntryStack<?>> stack = new ArrayList<>();
        for (int i = 0; i < slot * 2; i++)
            stack.add(EntryStack.empty());
        for (int i = 0; i < 6 - slot * 2; i++)
            stack.add(output.get(0));
        return stack;
    }
}
