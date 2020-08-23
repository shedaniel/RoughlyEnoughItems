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

package me.shedaniel.rei.api;

import me.shedaniel.rei.impl.Internals;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public interface BuiltinPlugin {
    Identifier CRAFTING = new Identifier("minecraft", "plugins/crafting");
    Identifier SMELTING = new Identifier("minecraft", "plugins/smelting");
    Identifier SMOKING = new Identifier("minecraft", "plugins/smoking");
    Identifier BLASTING = new Identifier("minecraft", "plugins/blasting");
    Identifier CAMPFIRE = new Identifier("minecraft", "plugins/campfire");
    Identifier STONE_CUTTING = new Identifier("minecraft", "plugins/stone_cutting");
    Identifier STRIPPING = new Identifier("minecraft", "plugins/stripping");
    Identifier BREWING = new Identifier("minecraft", "plugins/brewing");
    Identifier PLUGIN = new Identifier("roughlyenoughitems", "default_plugin");
    Identifier COMPOSTING = new Identifier("minecraft", "plugins/composting");
    Identifier FUEL = new Identifier("minecraft", "plugins/fuel");
    Identifier SMITHING = new Identifier("minecraft", "plugins/smithing");
    Identifier BEACON_BASE = new Identifier("minecraft", "plugins/beacon_base");
    Identifier TILLING = new Identifier("minecraft", "plugins/tilling");
    Identifier PATHING = new Identifier("minecraft", "plugins/pathing");
    Identifier BEACON_PAYMENT = new Identifier("minecraft", "plugins/beacon_payment");
    Identifier INFO = new Identifier("roughlyenoughitems", "plugins/information");
    
    static BuiltinPlugin getInstance() {
        return Internals.getBuiltinPlugin();
    }
    
    void registerBrewingRecipe(ItemStack input, Ingredient ingredient, ItemStack output);
    
    void registerInformation(List<EntryStack> entryStacks, Text name, UnaryOperator<List<Text>> textBuilder);
    
    default void registerInformation(EntryStack entryStack, Text name, UnaryOperator<List<Text>> textBuilder) {
        registerInformation(Collections.singletonList(entryStack), name, textBuilder);
    }
}
