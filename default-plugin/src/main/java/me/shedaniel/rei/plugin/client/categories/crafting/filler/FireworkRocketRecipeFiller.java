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

package me.shedaniel.rei.plugin.client.categories.crafting.filler;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FireworkRocketRecipeFiller implements CraftingRecipeFiller<FireworkRocketRecipe> {
    @Override
    public Collection<Display> apply(FireworkRocketRecipe recipe) {
        List<Display> displays = new ArrayList<>();
        {
            EntryIngredient[] inputs = new EntryIngredient[4];
            inputs[0] = EntryIngredients.of(Items.GUNPOWDER);
            inputs[1] = EntryIngredients.of(Items.PAPER);
            inputs[2] = EntryIngredient.of(EntryStack.empty().cast(), EntryStacks.of(Items.GUNPOWDER), EntryStacks.of(Items.GUNPOWDER));
            inputs[3] = EntryIngredient.of(EntryStack.empty().cast(), EntryStack.empty().cast(), EntryStacks.of(Items.GUNPOWDER));
            EntryStack<ItemStack>[] outputs = new EntryStack[3];
            for (int i = 0; i < 3; i++) {
                outputs[i] = EntryStacks.of(new ItemStack(Items.FIREWORK_ROCKET, 3));
                CompoundTag tag = outputs[i].getValue().getOrCreateTagElement("Fireworks");
                tag.putByte("Flight", (byte) (i + 1));
            }
            displays.add(new DefaultCustomShapelessDisplay(recipe,
                    List.of(inputs),
                    List.of(EntryIngredient.of(outputs))));
        }
        
        return displays;
    }
    
    @Override
    public Class<FireworkRocketRecipe> getRecipeClass() {
        return FireworkRocketRecipe.class;
    }
    
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registerExtension(registry, (bounds, widgets, display) -> {
            widgets.add(createInfoWidget(bounds, display, new TranslatableComponent("text.rei.crafting.firework.gunpowder.amount")));
        });
    }
}
