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
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.*;

public class ShieldDecorationRecipeFiller implements CraftingRecipeFiller<ShieldDecorationRecipe> {
    static List<Pair<DyeColor, ItemStack>> randomizeBanners() {
        List<Pair<DyeColor, ItemStack>> out = new ArrayList<>();
        DyeColor[] colors = DyeColor.values();
        Random random = new Random();
        
        for (DyeColor color : colors) {
            Optional<Item> bannerOptional = BuiltInRegistries.ITEM.getOptional(Identifier.parse(color.getName() + "_banner"));
            if (bannerOptional.isEmpty()) continue;
            out.add(Pair.of(color, new ItemStack(bannerOptional.get())));
            
            for (int i = 0; i < 2; i++) {
                List<BannerPatternLayers.Layer> layers = new ArrayList<>();
                Optional<Registry<BannerPattern>> registry = BasicDisplay.registryAccess().lookup(Registries.BANNER_PATTERN);
                if (registry.isEmpty()) return Collections.emptyList();
                Holder<BannerPattern>[] allPatterns = registry.get().listElements().toArray(Holder[]::new);
                for (int j = 0; j < 2; j++) {
                    Holder<BannerPattern> pattern = allPatterns[random.nextInt(allPatterns.length - 1) + 1];
                    layers.add(new BannerPatternLayers.Layer(pattern, colors[random.nextInt(colors.length)]));
                }
                ItemStack banner = new ItemStack(bannerOptional.get());
                banner.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(layers));
                out.add(Pair.of(color, banner));
            }
        }
        
        return out;
    }
    
    @Override
    public Collection<Display> apply(RecipeHolder<ShieldDecorationRecipe> recipe) {
        List<Display> displays = new ArrayList<>();
        EntryIngredient shield = EntryIngredients.of(Items.SHIELD);
        EntryIngredient.Builder inputsBuilder = EntryIngredient.builder();
        EntryIngredient.Builder outputsBuilder = EntryIngredient.builder();
        
        for (Pair<DyeColor, ItemStack> pair : randomizeBanners()) {
            inputsBuilder.add(EntryStacks.of(pair.getSecond()));
            outputsBuilder.add(createOutput(pair.getFirst(), pair.getSecond()));
        }
        
        EntryIngredient inputs = inputsBuilder.build();
        EntryIngredient outputs = outputsBuilder.build();
        
        EntryIngredient.unifyFocuses(inputs, outputs);
        
        displays.add(new DefaultCustomShapelessDisplay(
                List.of(inputs, shield),
                List.of(outputs),
                Optional.of(recipe.id().identifier())));
        
        return displays;
    }

    private static EntryStack<ItemStack> createOutput(DyeColor color, ItemStack banner) {
        ItemStack output = new ItemStack(Items.SHIELD);
        output.set(DataComponents.BANNER_PATTERNS, banner.get(DataComponents.BANNER_PATTERNS));
        output.set(DataComponents.BASE_COLOR, ((BannerItem) banner.getItem()).getColor());
        return EntryStacks.of(output);
    }
    
    @Override
    public Class<ShieldDecorationRecipe> getRecipeClass() {
        return ShieldDecorationRecipe.class;
    }
}
