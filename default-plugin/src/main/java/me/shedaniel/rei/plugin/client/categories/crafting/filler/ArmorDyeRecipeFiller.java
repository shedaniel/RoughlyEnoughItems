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

import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ArmorDyeRecipeFiller implements CraftingRecipeFiller<ArmorDyeRecipe> {
    @Override
    public Collection<Display> apply(ArmorDyeRecipe recipe) {
        List<Display> displays = new ArrayList<>();
        List<EntryStack<?>> toDye = EntryRegistry.getInstance().getEntryStacks().filter(entry -> entry.getValueType() == ItemStack.class && entry.<ItemStack>castValue().getItem() instanceof DyeableLeatherItem).toList();
        DyeColor[] colors = DyeColor.values();
        
        for (EntryStack<?> armor : toDye) {
            ItemStack armorStack = armor.castValue();
            for (DyeColor color : colors) {
                ItemStack output = armorStack.copy();
                DyeItem dyeItem = DyeItem.byColor(color);
                output = DyeableLeatherItem.dyeArmor(output, List.of(dyeItem));
                displays.add(new DefaultCustomShapelessDisplay(recipe,
                        List.of(EntryIngredient.of(armor.copy()),
                                EntryIngredients.of(dyeItem)),
                        List.of(EntryIngredients.of(output))));
            }
            
            for (int i = 0; i < 9; i++) {
                int dyes = new Random().nextInt(2) + 2;
                List<EntryIngredient> inputs = new ArrayList<>();
                List<DyeItem> dyeItems = new ArrayList<>();
                inputs.add(EntryIngredient.of(armor.copy()));
                for (int j = 0; j < dyes; j++) {
                    DyeColor color = colors[new Random().nextInt(colors.length)];
                    DyeItem dyeItem = DyeItem.byColor(color);
                    dyeItems.add(dyeItem);
                    inputs.add(EntryIngredients.of(dyeItem));
                }
                ItemStack output = armorStack.copy();
                output = DyeableLeatherItem.dyeArmor(output, dyeItems);
                displays.add(new DefaultCustomShapelessDisplay(recipe,
                        inputs, List.of(EntryIngredients.of(output))));
            }
        }
        
        return displays;
    }
    
    @Override
    public Class<ArmorDyeRecipe> getRecipeClass() {
        return ArmorDyeRecipe.class;
    }
}
