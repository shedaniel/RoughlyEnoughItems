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
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.ingredient.entry.type.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.stream.Stream;

public final class EntryStacks {
    private EntryStacks() {}
    
    public static EntryStack<FluidStack> of(Fluid fluid) {
        return of(fluid, FluidStack.bucketAmount());
    }
    
    public static EntryStack<FluidStack> of(Fluid fluid, int amount) {
        return of(fluid, Fraction.ofWhole(amount));
    }
    
    public static EntryStack<FluidStack> of(Fluid fluid, double amount) {
        return of(fluid, Fraction.from(amount));
    }
    
    public static EntryStack<FluidStack> of(Fluid fluid, Fraction amount) {
        return EntryStack.of(VanillaEntryTypes.FLUID, FluidStack.create(fluid, amount));
    }
    
    public static EntryStack<FluidStack> of(FluidStack stack) {
        return EntryStack.of(VanillaEntryTypes.FLUID, stack);
    }
    
    public static EntryStack<ItemStack> of(ItemStack stack) {
        return EntryStack.of(VanillaEntryTypes.ITEM, stack);
    }
    
    public static EntryStack<ItemStack> of(ItemLike item) {
        return of(new ItemStack(item));
    }
    
    public static EntryStack<?> of(Renderer renderer) {
        if (renderer instanceof EntryStack) {
            return (EntryStack<?>) renderer;
        }
        
        return EntryStack.of(BuiltinEntryTypes.RENDERING, renderer);
    }
    
    public static List<EntryStack<ItemStack>> ofItems(Collection<ItemLike> stacks) {
        if (stacks.size() == 0) return Collections.emptyList();
        if (stacks.size() == 1) return Collections.singletonList(of(stacks.iterator().next()));
        List<EntryStack<ItemStack>> result = new ArrayList<>(stacks.size());
        for (ItemLike stack : stacks) {
            result.add(of(stack));
        }
        return ImmutableList.copyOf(result);
    }
    
    public static List<EntryStack<ItemStack>> ofItemStacks(Collection<ItemStack> stacks) {
        if (stacks.size() == 0) return Collections.emptyList();
        if (stacks.size() == 1) {
            ItemStack stack = stacks.iterator().next();
            if (stack.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(of(stack));
        }
        List<EntryStack<ItemStack>> result = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            result.add(of(stack));
        }
        return ImmutableList.copyOf(result);
    }
    
    public static List<EntryStack<ItemStack>> ofIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) return Collections.emptyList();
        ItemStack[] matchingStacks = ingredient.getItems();
        if (matchingStacks.length == 0) return Collections.emptyList();
        if (matchingStacks.length == 1) return Collections.singletonList(of(matchingStacks[0]));
        List<EntryStack<ItemStack>> result = new ArrayList<>(matchingStacks.length);
        for (ItemStack matchingStack : matchingStacks) {
            if (!matchingStack.isEmpty())
                result.add(of(matchingStack));
        }
        return ImmutableList.copyOf(result);
    }
    
    public static List<List<EntryStack<ItemStack>>> ofIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() == 0) return Collections.emptyList();
        if (ingredients.size() == 1) {
            Ingredient ingredient = ingredients.get(0);
            if (ingredient.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(ofIngredient(ingredient));
        }
        boolean emptyFlag = true;
        List<List<EntryStack<ItemStack>>> result = new ArrayList<>(ingredients.size());
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);
            if (emptyFlag && ingredient.isEmpty()) continue;
            result.add(0, ofIngredient(ingredient));
            emptyFlag = false;
        }
        return ImmutableList.copyOf(result);
    }
    
    public static <A, B> boolean equals(EntryStack<A> left, EntryStack<B> right, ComparisonContext context) {
        if (left == null) return right == null;
        if (right == null) return left == null;
        if (left == right) return true;
        EntryType<A> leftType = left.getType();
        EntryType<B> rightType = right.getType();
        if (leftType == rightType) {
            return left.equals((EntryStack<A>) right, context);
        }
        if (context == ComparisonContext.EXACT) return false;
        for (EntryTypeBridge<A, B> bridge : EntryTypeRegistry.getInstance().getBridgesFor(leftType, rightType)) {
            InteractionResultHolder<Stream<EntryStack<B>>> holder = bridge.bridge(left);
            if (holder.getResult() == InteractionResult.SUCCESS) {
                Iterator<EntryStack<B>> iterator = holder.getObject().iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().equals(right, context)) {
                        return true;
                    }
                }
            }
        }
        for (EntryTypeBridge<B, A> bridge : EntryTypeRegistry.getInstance().getBridgesFor(rightType, leftType)) {
            InteractionResultHolder<Stream<EntryStack<A>>> holder = bridge.bridge(right);
            if (holder.getResult() == InteractionResult.SUCCESS) {
                Iterator<EntryStack<A>> iterator = holder.getObject().iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().equals(left, context)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static <A, B> boolean equalsExact(EntryStack<A> left, EntryStack<B> right) {
        return equals(left, right, ComparisonContext.EXACT);
    }
    
    public static <A, B> boolean equalsFuzzy(EntryStack<A> left, EntryStack<B> right) {
        return equals(left, right, ComparisonContext.FUZZY);
    }
    
    /**
     * Hash Code of the {@link ComparisonContext#EXACT} context, stacks with the same hash code should share the same normalized stack.
     * <p>
     * For example, enchantment books of different enchantments will not receive the same hash code under this context.
     * However, difference between the amount of objects in a stack will not affect the hash code.
     *
     * @param stack the stack to hash code
     * @param <T>   the type of the stack
     * @return the hash code of the {@link ComparisonContext#EXACT} context
     */
    public static <T> int hashExact(EntryStack<T> stack) {
        return stack.hash(ComparisonContext.EXACT);
    }
    
    /**
     * Hash Code of the {@link ComparisonContext#FUZZY} context, stacks with the same hash code may not share the same normalized stack.
     * This hash is less specific, mainly used for fuzzy matching between different stacks.
     * <p>
     * For example, enchantment books of different enchantments should still receive the same hash code under this context.
     *
     * @param stack the stack to hash code
     * @param <T>   the type of the stack
     * @return the hash code of the {@link ComparisonContext#FUZZY} context
     */
    public static <T> int hashFuzzy(EntryStack<T> stack) {
        return stack.hash(ComparisonContext.FUZZY);
    }
    
    public static EntryStack<FluidStack> simplifyAmount(EntryStack<FluidStack> stack) {
        stack.getValue().setAmount(stack.getValue().getAmount().simplify());
        return stack;
    }
}
