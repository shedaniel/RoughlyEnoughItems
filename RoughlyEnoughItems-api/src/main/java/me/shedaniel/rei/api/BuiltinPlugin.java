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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public interface BuiltinPlugin {
    ResourceLocation CRAFTING = new ResourceLocation("minecraft", "plugins/crafting");
    ResourceLocation SMELTING = new ResourceLocation("minecraft", "plugins/smelting");
    ResourceLocation SMOKING = new ResourceLocation("minecraft", "plugins/smoking");
    ResourceLocation BLASTING = new ResourceLocation("minecraft", "plugins/blasting");
    ResourceLocation CAMPFIRE = new ResourceLocation("minecraft", "plugins/campfire");
    ResourceLocation STONE_CUTTING = new ResourceLocation("minecraft", "plugins/stone_cutting");
    ResourceLocation STRIPPING = new ResourceLocation("minecraft", "plugins/stripping");
    ResourceLocation BREWING = new ResourceLocation("minecraft", "plugins/brewing");
    ResourceLocation PLUGIN = new ResourceLocation("roughlyenoughitems", "default_plugin");
    ResourceLocation COMPOSTING = new ResourceLocation("minecraft", "plugins/composting");
    ResourceLocation FUEL = new ResourceLocation("minecraft", "plugins/fuel");
    ResourceLocation SMITHING = new ResourceLocation("minecraft", "plugins/smithing");
    ResourceLocation BEACON = new ResourceLocation("minecraft", "plugins/beacon");
    ResourceLocation BEACON_PAYMENT = new ResourceLocation("minecraft", "plugins/beacon_payment");
    ResourceLocation TILLING = new ResourceLocation("minecraft", "plugins/tilling");
    ResourceLocation PATHING = new ResourceLocation("minecraft", "plugins/pathing");
    ResourceLocation INFO = new ResourceLocation("roughlyenoughitems", "plugins/information");
    
    static BuiltinPlugin getInstance() {
        return Internals.getBuiltinPlugin();
    }
    
    default void registerBrewingRecipe(ItemStack input, Ingredient ingredient, ItemStack output) {
        registerBrewingRecipe(Ingredient.of(input), ingredient, output);
    }
    
    void registerBrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output);
    
    void registerInformation(List<? extends EntryStack<?>> entryStacks, Component name, UnaryOperator<List<Component>> textBuilder);
    
    default void registerInformation(EntryStack<?> entryStack, Component name, UnaryOperator<List<Component>> textBuilder) {
        registerInformation(Collections.singletonList(entryStack), name, textBuilder);
    }
}
