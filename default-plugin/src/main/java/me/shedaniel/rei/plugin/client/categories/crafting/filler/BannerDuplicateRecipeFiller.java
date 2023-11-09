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

package me.shedaniel.rei.plugin.client.categories.crafting.filler;

import com.mojang.datafixers.util.Pair;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;

import java.util.*;

public class BannerDuplicateRecipeFiller implements CraftingRecipeFiller<BannerDuplicateRecipe> {
    @Override
    public Collection<Display> apply(BannerDuplicateRecipe recipe) {
        List<Display> displays = new ArrayList<>();
        Map<DyeColor, Pair<EntryIngredient.Builder, EntryStack<?>>> displayMap = new HashMap<>();
        
        for (Pair<DyeColor, ItemStack> pair : ShieldDecorationRecipeFiller.randomizeBanners()) {
            Optional<Item> bannerOptional = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(pair.getFirst().getName() + "_banner"));
            if (bannerOptional.isEmpty()) continue;
            Pair<EntryIngredient.Builder, EntryStack<?>> builderPair = displayMap.computeIfAbsent(pair.getFirst(), color -> Pair.of(EntryIngredient.builder(), EntryStacks.of(bannerOptional.get())));
            builderPair.getFirst().add(EntryStacks.of(pair.getSecond()));
        }
        
        for (Pair<EntryIngredient.Builder, EntryStack<?>> pair : displayMap.values()) {
            EntryIngredient inputsFirst = pair.getFirst().build();
            EntryStack<?> inputsSecond = pair.getSecond();
            EntryIngredient.unifyFocuses(inputsFirst);
            displays.add(new DefaultCustomShapelessDisplay(recipe,
                    List.of(inputsFirst, EntryIngredient.of(inputsSecond)),
                    List.of(inputsFirst)));
        }
        
        return displays;
    }
    
    @Override
    public Class<BannerDuplicateRecipe> getRecipeClass() {
        return BannerDuplicateRecipe.class;
    }
}
