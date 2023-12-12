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

import com.google.common.base.MoreObjects;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.*;

public class ShieldDecorationRecipeFiller implements CraftingRecipeFiller<ShieldDecorationRecipe> {
    static List<Pair<DyeColor, ItemStack>> randomizeBanners() {
        List<Pair<DyeColor, ItemStack>> out = new ArrayList<>();
        DyeColor[] colors = DyeColor.values();
        Random random = new Random();
        
        for (DyeColor color : colors) {
            Optional<Item> bannerOptional = Registry.ITEM.getOptional(new ResourceLocation(color.getName() + "_banner"));
            if (bannerOptional.isEmpty()) continue;
            out.add(Pair.of(color, new ItemStack(bannerOptional.get())));
            
            for (int i = 0; i < 2; i++) {
                BannerPattern.Builder patternBuilder = new BannerPattern.Builder();
                BannerPattern[] allPatterns = BannerPattern.values();
                for (int j = 0; j < 2; j++) {
                    BannerPattern pattern = allPatterns[random.nextInt(allPatterns.length - 1) + 1];
                    patternBuilder.addPattern(pattern, colors[random.nextInt(colors.length)]);
                }
                ItemStack banner = new ItemStack(bannerOptional.get());
                CompoundTag newTag = new CompoundTag();
                newTag.put("Patterns", patternBuilder.toListTag());
                banner.addTagElement("BlockEntityTag", newTag);
                out.add(Pair.of(color, banner));
            }
        }
        
        return out;
    }
    
    @Override
    public Collection<Display> apply(ShieldDecorationRecipe recipe) {
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
        
        displays.add(new DefaultCustomShapelessDisplay(recipe,
                List.of(inputs, shield),
                List.of(outputs)));
        
        return displays;
    }
    
    private static EntryStack<ItemStack> createOutput(DyeColor color, ItemStack banner) {
        ItemStack output = new ItemStack(Items.SHIELD);
        CompoundTag beTag = MoreObjects.firstNonNull(BlockItem.getBlockEntityData(banner), new CompoundTag());
        beTag.putInt("Base", color.getId());
        BlockItem.setBlockEntityData(output, BlockEntityType.BANNER, beTag);
        return EntryStacks.of(output);
    }
    
    @Override
    public Class<ShieldDecorationRecipe> getRecipeClass() {
        return ShieldDecorationRecipe.class;
    }
}
