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

package me.shedaniel.rei.plugin.common.displays.crafting;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.injectables.annotations.PlatformOnly;
import dev.architectury.platform.Platform;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class DefaultCraftingDisplay<C extends Recipe<?>> extends BasicDisplay implements SimpleGridMenuDisplay {
    protected Optional<C> recipe;
    
    public DefaultCraftingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<C> recipe) {
        super(inputs, outputs, Optional.empty());
        this.recipe = recipe;
    }
    
    @Nullable
    public static DefaultCraftingDisplay<?> of(Recipe<?> recipe) {
        if (recipe instanceof ShapelessRecipe) {
            return new DefaultShapelessDisplay((ShapelessRecipe) recipe);
        } else if (recipe instanceof ShapedRecipe) {
            return new DefaultShapedDisplay((ShapedRecipe) recipe);
        } else if (!recipe.isSpecial()) {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            Pair<Integer, Integer> size = Platform.isFabric() ? null : getSize(recipe);
            
            if (!ingredients.isEmpty()) {
                if (size == null) {
                    return new DefaultCustomDisplay(recipe, EntryIngredients.ofIngredients(recipe.getIngredients()),
                            Collections.singletonList(EntryIngredients.of(recipe.getResultItem())));
                } else {
                    return new DefaultCustomShapedDisplay(recipe, EntryIngredients.ofIngredients(recipe.getIngredients()),
                            Collections.singletonList(EntryIngredients.of(recipe.getResultItem())),
                            size.getLeft(), size.getRight());
                }
            }
        }
        
        return null;
    }
    
    @ExpectPlatform
    @PlatformOnly(PlatformOnly.FORGE)
    private static Pair<Integer, Integer> getSize(Recipe<?> recipe) {
        throw new AssertionError();
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.CRAFTING;
    }
    
    public Optional<C> getOptionalRecipe() {
        return recipe;
    }
    
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return getOptionalRecipe().map(Recipe::getId);
    }
    
    public <T extends AbstractContainerMenu> List<List<ItemStack>> getOrganisedInputEntries(SimpleGridMenuInfo<T, DefaultCraftingDisplay<?>> menuInfo, T container) {
        return CollectionUtils.map(getOrganisedInputEntries(menuInfo.getCraftingWidth(container), menuInfo.getCraftingHeight(container)), ingredient ->
                CollectionUtils.<EntryStack<?>, ItemStack>filterAndMap(ingredient, stack -> stack.getType() == VanillaEntryTypes.ITEM,
                        EntryStack::castValue));
    }
    
    public <T extends AbstractContainerMenu> List<EntryIngredient> getOrganisedInputEntries(int menuWidth, int menuHeight) {
        List<EntryIngredient> list = new ArrayList<>(menuWidth * menuHeight);
        for (int i = 0; i < menuWidth * menuHeight; i++) {
            list.add(EntryIngredient.empty());
        }
        for (int i = 0; i < getInputEntries().size(); i++) {
            list.set(getSlotWithSize(this, i, menuWidth), getInputEntries().get(i));
        }
        return list;
    }
    
    public static int getSlotWithSize(DefaultCraftingDisplay<?> display, int index, int craftingGridWidth) {
        return getSlotWithSize(display.getInputWidth(), index, craftingGridWidth);
    }
    
    public static int getSlotWithSize(int recipeWidth, int index, int craftingGridWidth) {
        int x = index % recipeWidth;
        int y = (index - x) / recipeWidth;
        return craftingGridWidth * y + x;
    }
    
    public static BasicDisplay.Serializer<DefaultCraftingDisplay<?>> serializer() {
        return BasicDisplay.Serializer.<DefaultCraftingDisplay<?>>ofSimple(DefaultCustomDisplay::simple)
                .inputProvider(display -> display.getOrganisedInputEntries(3, 3));
    }
    
    @Override
    public List<EntryIngredient> getInputEntries(MenuSerializationContext<?, ?, ?> context, MenuInfo<?, ?> info, boolean fill) {
        int craftingWidth = Math.max(3, getInputWidth());
        int craftingHeight = Math.max(3, getInputHeight());
        
        if (info instanceof SimpleGridMenuInfo) {
            craftingWidth = ((SimpleGridMenuInfo<AbstractContainerMenu, ?>) info).getCraftingWidth(context.getMenu());
            craftingHeight = ((SimpleGridMenuInfo<AbstractContainerMenu, ?>) info).getCraftingHeight(context.getMenu());
        }
        
        EntryIngredient[][] grid = new EntryIngredient[craftingWidth][craftingHeight];
        
        List<EntryIngredient> inputEntries = getInputEntries();
        for (int i = 0; i < inputEntries.size(); i++) {
            grid[i % getInputWidth()][i / getInputWidth()] = inputEntries.get(i);
        }
        
        List<EntryIngredient> list = new ArrayList<>(craftingWidth * craftingHeight);
        for (int i = 0, n = craftingWidth * craftingHeight; i < n; i++) {
            list.add(EntryIngredient.empty());
        }
        
        for (int x = 0; x < craftingWidth; x++) {
            for (int y = 0; y < craftingHeight; y++) {
                if (grid[x][y] != null) {
                    list.set(craftingWidth * y + x, grid[x][y]);
                }
            }
        }
        
        return list;
    }
}
