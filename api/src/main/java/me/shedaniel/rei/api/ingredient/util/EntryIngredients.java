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

package me.shedaniel.rei.api.ingredient.util;

import com.google.common.collect.ImmutableList;
import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.type.EntryDefinition;
import me.shedaniel.rei.api.ingredient.entry.type.EntryType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class EntryIngredients {
    private EntryIngredients() {}
    
    public static EntryIngredient of(ItemLike stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static EntryIngredient of(ItemStack stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static EntryIngredient of(Fluid fluid) {
        return EntryIngredient.of(EntryStacks.of(fluid));
    }
    
    public static EntryIngredient of(Fluid fluid, int amount) {
        return EntryIngredient.of(EntryStacks.of(fluid, amount));
    }
    
    public static EntryIngredient of(Fluid fluid, double amount) {
        return EntryIngredient.of(EntryStacks.of(fluid, amount));
    }
    
    public static EntryIngredient of(Fluid fluid, Fraction amount) {
        return EntryIngredient.of(EntryStacks.of(fluid, amount));
    }
    
    public static EntryIngredient of(FluidStack stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static EntryIngredient of(Renderer renderer) {
        return EntryIngredient.of(EntryStacks.of(renderer));
    }
    
    public static <T> EntryIngredient of(EntryType<T> type, Collection<T> values) {
        return of(type.getDefinition(), values);
    }
    
    public static <T> EntryIngredient of(EntryDefinition<T> definition, Collection<T> values) {
        if (values.size() == 0) return EntryIngredient.empty();
        if (values.size() == 1) return EntryIngredient.of(EntryStack.of(definition, values.iterator().next()));
        List<EntryStack<T>> result = new ArrayList<>(values.size());
        for (T value : values) {
            result.add(EntryStack.of(definition, value));
        }
        return EntryIngredient.of(result);
    }
    
    public static EntryIngredient ofItems(Collection<ItemLike> stacks) {
        if (stacks.size() == 0) return EntryIngredient.empty();
        if (stacks.size() == 1) return EntryIngredient.of(EntryStacks.of(stacks.iterator().next()));
        List<EntryStack<ItemStack>> result = new ArrayList<>(stacks.size());
        for (ItemLike stack : stacks) {
            result.add(EntryStacks.of(stack));
        }
        return EntryIngredient.of(result);
    }
    
    public static EntryIngredient ofItemStacks(Collection<ItemStack> stacks) {
        if (stacks.size() == 0) return EntryIngredient.empty();
        if (stacks.size() == 1) {
            ItemStack stack = stacks.iterator().next();
            if (stack.isEmpty()) return EntryIngredient.empty();
            return EntryIngredient.of(EntryStacks.of(stack));
        }
        List<EntryStack<ItemStack>> result = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            result.add(EntryStacks.of(stack));
        }
        return EntryIngredient.of(result);
    }
    
    public static EntryIngredient ofIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) return EntryIngredient.empty();
        ItemStack[] matchingStacks = ingredient.getItems();
        if (matchingStacks.length == 0) return EntryIngredient.empty();
        if (matchingStacks.length == 1) return EntryIngredient.of(EntryStacks.of(matchingStacks[0]));
        List<EntryStack<ItemStack>> result = new ArrayList<>(matchingStacks.length);
        for (ItemStack matchingStack : matchingStacks) {
            if (!matchingStack.isEmpty())
                result.add(EntryStacks.of(matchingStack));
        }
        return EntryIngredient.of(result);
    }
    
    public static List<EntryIngredient> ofIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() == 0) return Collections.emptyList();
        if (ingredients.size() == 1) {
            Ingredient ingredient = ingredients.get(0);
            if (ingredient.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(ofIngredient(ingredient));
        }
        boolean emptyFlag = true;
        List<EntryIngredient> result = new ArrayList<>(ingredients.size());
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);
            if (emptyFlag && ingredient.isEmpty()) continue;
            result.add(0, ofIngredient(ingredient));
            emptyFlag = false;
        }
        return ImmutableList.copyOf(result);
    }
    
    public static <T> boolean testFuzzy(EntryIngredient ingredient, EntryStack<T> stack) {
        for (EntryStack<?> ingredientStack : ingredient) {
            if (EntryStacks.equalsFuzzy(ingredientStack, stack)) {
                return true;
            }
        }
        
        return false;
    }
}
