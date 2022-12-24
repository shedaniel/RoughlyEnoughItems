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

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.MapExtendingRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapExtendingRecipeFiller implements CraftingRecipeFiller<MapExtendingRecipe> {
    @Override
    public Collection<Display> apply(MapExtendingRecipe recipe) {
        List<Display> displays = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            EntryIngredient[] inputs = new EntryIngredient[9];
            for (int j = 0; j < 9; j++) {
                if (j == 4) {
                    inputs[j] = mapWith("X", i, 1);
                } else {
                    inputs[j] = EntryIngredients.of(Items.PAPER);
                }
            }
            
            displays.add(new DefaultCustomDisplay(recipe,
                    List.of(inputs),
                    List.of(mapWith("X", i + 1, 1))));
        }
        
        return displays;
    }
    
    @Override
    public Class<MapExtendingRecipe> getRecipeClass() {
        return MapExtendingRecipe.class;
    }
    
    public static EntryIngredient mapWith(String mapId, int scale, int count) {
        EntryIngredient stacks = EntryIngredients.of(Items.FILLED_MAP, count);
        String unknown = I18n.get("filled_map.unknown");
        for (EntryStack<?> stack : stacks) {
            stack.tooltipProcessor(($, tooltip) -> {
                tooltip.entries().removeIf(entry -> entry.isText() && entry.getAsText().getString().equals(unknown));
                return tooltip;
            });
            stack.tooltip(
                    new TranslatableComponent("filled_map.id", mapId).withStyle(ChatFormatting.GRAY),
                    new TranslatableComponent("filled_map.scale", (1 << scale)).withStyle(ChatFormatting.GRAY),
                    new TranslatableComponent("filled_map.level", scale, 4).withStyle(ChatFormatting.GRAY)
            );
        }
        return stacks;
    }
}
