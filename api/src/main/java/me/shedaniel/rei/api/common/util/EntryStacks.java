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

package me.shedaniel.rei.api.common.util;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeBridge;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import java.util.Iterator;
import java.util.stream.Stream;

public final class EntryStacks {
    private EntryStacks() {}
    
    public static EntryStack<FluidStack> of(Fluid fluid) {
        return of(fluid, FluidStack.bucketAmount());
    }
    
    public static EntryStack<FluidStack> of(Fluid fluid, long amount) {
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
    
    public static EntryStack<ItemStack> of(ItemLike item, int amount) {
        return of(new ItemStack(item, amount));
    }
    
    /**
     * Compares equality under the provided {@code context}.
     * Prioritizes {@link me.shedaniel.rei.api.common.entry.type.EntryDefinition#equals(Object, Object, ComparisonContext)} then compares
     * with {@link EntryTypeBridge} for differing {@link EntryType}.
     * <p>
     * For example, a lava bucket should still be equals to a lava fluid.
     *
     * @param left    the first stack to compare
     * @param right   the second stack to compare
     * @param context the context of the equality check
     * @return the equality under the provided {@code context}
     */
    public static <A, B> boolean equals(EntryStack<A> left, EntryStack<B> right, ComparisonContext context) {
        if (left == null) return right == null;
        if (right == null) return left == null;
        if (left == right) return true;
        EntryType<A> leftType = left.getType();
        EntryType<B> rightType = right.getType();
        if (leftType == rightType) {
            return left.getDefinition().equals(left.getValue(), right.castValue(), context);
        }
        EntryTypeRegistry instance = EntryTypeRegistry.getInstance();
        for (EntryTypeBridge<A, B> bridge : instance.getBridgesFor(leftType, rightType)) {
            CompoundEventResult<Stream<EntryStack<B>>> holder = bridge.bridge(left);
            if (holder.isTrue()) {
                Iterator<EntryStack<B>> iterator = holder.object().iterator();
                while (iterator.hasNext()) {
                    EntryStack<B> next = iterator.next();
                    if (next.getDefinition().equals(next.getValue(), right.getValue(), context)) {
                        return true;
                    }
                }
            }
        }
        for (EntryTypeBridge<B, A> bridge : instance.getBridgesFor(rightType, leftType)) {
            CompoundEventResult<Stream<EntryStack<A>>> holder = bridge.bridge(right);
            if (holder.isTrue()) {
                Iterator<EntryStack<A>> iterator = holder.object().iterator();
                while (iterator.hasNext()) {
                    EntryStack<A> next = iterator.next();
                    if (next.getDefinition().equals(next.getValue(), left.getValue(), context)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Compares equality for the {@link ComparisonContext#EXACT} context.
     * <p>
     * The exact context type denotes that the equivalent stacks should be <b>functionally</b> the same.
     * <p>
     * For example, tools with different damage values and different enchantments should be treated as different within this context.
     *
     * @param left  the first stack to compare
     * @param right the second stack to compare
     * @return the equality for the {@link ComparisonContext#EXACT} context
     */
    public static <A, B> boolean equalsExact(EntryStack<A> left, EntryStack<B> right) {
        return equals(left, right, ComparisonContext.EXACT);
    }
    
    /**
     * Compares equality for the {@link ComparisonContext#FUZZY} context.
     * <p>
     * The fuzzy context type denotes that the equivalent stacks should be <b>primarily</b> the same.
     * <p>
     * For example, enchantment books of different enchantments should be different within this context,
     * while tools with different damage values and different enchantments should be treated as the same within this context.
     * Skulker boxes with different content should be different within this context.
     *
     * @param left  the first stack to compare
     * @param right the second stack to compare
     * @return the equality for the {@link ComparisonContext#EXACT} context
     */
    public static <A, B> boolean equalsFuzzy(EntryStack<A> left, EntryStack<B> right) {
        return equals(left, right, ComparisonContext.FUZZY);
    }
    
    /**
     * Returns hash code of the {@code context} context.
     *
     * @param stack   the stack to hash code
     * @param context the context to use
     * @param <T>     the type of the stack
     * @return the hash code of the {@code context} context
     */
    public static <T> long hash(EntryStack<T> stack, ComparisonContext context) {
        return stack.getDefinition().hash(stack, stack.getValue(), context);
    }
    
    /**
     * Returns hash code of the {@link ComparisonContext#EXACT} context.
     * <p>
     * The exact context type denotes that the equivalent stacks should be <b>functionally</b> the same.
     * <p>
     * For example, tools with different damage values and different enchantments should be treated as different within this context.
     *
     * @param stack the stack to hash code
     * @param <T>   the type of the stack
     * @return the hash code of the {@link ComparisonContext#EXACT} context
     * @see #hash(EntryStack, ComparisonContext)
     */
    public static <T> long hashExact(EntryStack<T> stack) {
        return hash(stack, ComparisonContext.EXACT);
    }
    
    /**
     * Returns hash code of the {@link ComparisonContext#FUZZY} context.
     * <p>
     * The fuzzy context type denotes that the equivalent stacks should be <b>primarily</b> the same.
     * <p>
     * For example, enchantment books of different enchantments should be different within this context,
     * while tools with different damage values and different enchantments should be treated as the same within this context.
     * Skulker boxes with different content should be different within this context.
     *
     * @param stack the stack to hash code
     * @param <T>   the type of the stack
     * @return the hash code of the {@link ComparisonContext#FUZZY} context
     * @see #hash(EntryStack, ComparisonContext)
     */
    public static <T> long hashFuzzy(EntryStack<T> stack) {
        return hash(stack, ComparisonContext.FUZZY);
    }
}
