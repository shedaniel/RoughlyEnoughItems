/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.plugin.common.displays.crafting;

import me.shedaniel.architectury.utils.NbtType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.SimpleDisplaySerializer;
import me.shedaniel.rei.api.common.display.SimpleMenuDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.registry.RecipeManagerContext;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class DefaultCraftingDisplay implements SimpleMenuDisplay {
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.CRAFTING;
    }
    
    public abstract Optional<Recipe<?>> getOptionalRecipe();
    
    public <T extends AbstractContainerMenu> List<List<ItemStack>> getOrganisedInputEntries(SimpleGridMenuInfo<T, DefaultCraftingDisplay> menuInfo, T container) {
        List<List<ItemStack>> list = new ArrayList<>(menuInfo.getCraftingWidth(container) * menuInfo.getCraftingHeight(container));
        for (int i = 0; i < menuInfo.getCraftingWidth(container) * menuInfo.getCraftingHeight(container); i++) {
            list.add(Collections.emptyList());
        }
        for (int i = 0; i < getInputEntries().size(); i++) {
            List<ItemStack> stacks = CollectionUtils.filterAndMap(getInputEntries().get(i), stack -> stack.getType() == VanillaEntryTypes.ITEM,
                    stack -> stack.<ItemStack>cast().getValue());
            list.set(getSlotWithSize(this, i, menuInfo.getCraftingWidth(container)), stacks);
        }
        return list;
    }
    
    public static int getSlotWithSize(DefaultCraftingDisplay recipeDisplay, int num, int craftingGridWidth) {
        int x = num % recipeDisplay.getWidth();
        int y = (num - x) / recipeDisplay.getWidth();
        return craftingGridWidth * y + x;
    }
    
    public enum Serializer implements SimpleDisplaySerializer<DefaultCraftingDisplay> {
        INSTANCE;
        
        @Override
        public DefaultCraftingDisplay read(CompoundTag tag) {
            List<EntryIngredient> input = EntryIngredients.read(tag.getList("input", NbtType.LIST));
            List<EntryIngredient> output = EntryIngredients.read(tag.getList("output", NbtType.LIST));
            Recipe<?> optionalRecipe;
            if (tag.contains("recipe", NbtType.STRING)) {
                optionalRecipe = RecipeManagerContext.getInstance().getRecipeManager().byKey(new ResourceLocation(tag.getString("recipe"))).orElse(null);
            } else {
                optionalRecipe = null;
            }
            return new DefaultCustomDisplay(optionalRecipe, input, output);
        }
        
        @Override
        public CompoundTag saveExtra(CompoundTag tag, DefaultCraftingDisplay display) {
            display.getOptionalRecipe().ifPresent(recipe -> tag.putString("recipe", recipe.getId().toString()));
            return tag;
        }
    }
}
