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

package me.shedaniel.rei.plugin.client;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.function.UnaryOperator;

public interface BuiltinClientPlugin extends BuiltinPlugin {
    static BuiltinClientPlugin getInstance() {
        return (BuiltinClientPlugin) ClientInternals.getBuiltinPlugin();
    }
    
    default void registerBrewingRecipe(ItemStack input, Ingredient ingredient, ItemStack output) {
        registerBrewingRecipe(Ingredient.of(input), ingredient, output);
    }
    
    void registerBrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output);
    
    void registerInformation(EntryIngredient ingredient, Component name, UnaryOperator<List<Component>> textBuilder);
    
    default void registerInformation(EntryStack<?> entryStack, Component name, UnaryOperator<List<Component>> textBuilder) {
        registerInformation(EntryIngredient.of(entryStack), name, textBuilder);
    }
}
