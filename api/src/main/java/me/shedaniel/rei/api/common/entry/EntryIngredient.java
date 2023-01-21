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

package me.shedaniel.rei.api.common.entry;

import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * An immutable representation of a list of {@link EntryStack}.
 *
 * @see me.shedaniel.rei.api.common.util.EntryIngredients
 */
@ApiStatus.NonExtendable
public interface EntryIngredient extends List<EntryStack<?>> {
    /**
     * Returns an empty entry ingredient. This is the singleton instance of {@link EntryIngredient} that is
     * built-in to the implementation.
     *
     * @return the empty entry ingredient
     */
    static EntryIngredient empty() {
        return Internals.getEntryIngredientProvider().empty();
    }
    
    /**
     * Creates a singleton {@link EntryIngredient} from the given {@link EntryStack}.
     *
     * @param stack the stack to create the {@link EntryIngredient} from
     * @param <T>   the type of entry
     * @return the singleton {@link EntryIngredient}
     */
    static <T> EntryIngredient of(EntryStack<T> stack) {
        return Internals.getEntryIngredientProvider().of(stack);
    }
    
    /**
     * Creates a list-backed {@link EntryIngredient} from the given array of {@link EntryStack}.
     *
     * @param stacks the stacks to create the {@link EntryIngredient} from
     * @param <T>    the type of entry
     * @return the list-backed {@link EntryIngredient}
     */
    @SafeVarargs
    static <T> EntryIngredient of(EntryStack<T>... stacks) {
        return Internals.getEntryIngredientProvider().of(stacks);
    }
    
    /**
     * Creates a list-backed {@link EntryIngredient} from the given list of {@link EntryStack}.
     *
     * @param stacks the stacks to create the {@link EntryIngredient} from
     * @param <T>    the type of entry
     * @return the list-backed {@link EntryIngredient}
     */
    @SuppressWarnings({"RedundantCast", "rawtypes"})
    static <T> EntryIngredient of(Iterable<? extends EntryStack<? extends T>> stacks) {
        return Internals.getEntryIngredientProvider().of((Iterable<EntryStack<?>>) (Iterable) stacks);
    }
    
    /**
     * Creates a builder for {@link EntryIngredient}.
     *
     * @return the builder
     */
    static Builder builder() {
        return Internals.getEntryIngredientProvider().builder();
    }
    
    /**
     * Creates a builder for {@link EntryIngredient} with the given initial capacity.
     *
     * @param initialCapacity the initial capacity
     * @return the builder
     */
    static Builder builder(int initialCapacity) {
        return Internals.getEntryIngredientProvider().builder(initialCapacity);
    }
    
    /**
     * Reads an {@link EntryIngredient} from the given {@link ListTag}.
     *
     * @param tag the tag
     * @return the read {@link EntryIngredient}
     * @throws NullPointerException          if an {@link EntryDefinition} is not found
     * @throws UnsupportedOperationException if an {@link EntryDefinition} does not support reading from a tag
     * @see EntryStack#read(CompoundTag)
     */
    static EntryIngredient read(ListTag tag) {
        if (tag.isEmpty()) return empty();
        EntryStack<?>[] stacks = new EntryStack[tag.size()];
        for (int i = 0; i < tag.size(); i++) {
            stacks[i] = EntryStack.read((CompoundTag) tag.get(i));
        }
        return Internals.getEntryIngredientProvider().of(stacks);
    }
    
    /**
     * Returns a {@link Collector} that accumulates the stacks into a
     * new {@link EntryIngredient}.
     *
     * @return the collector
     */
    static Collector<EntryStack<?>, ?, EntryIngredient> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), EntryIngredient::of);
    }
    
    /**
     * Saves the entry ingredient to a {@link ListTag}. This is only supported if every entry stack has a serializer.
     *
     * @return the saved tag
     * @throws UnsupportedOperationException if an {@link EntryDefinition} does not support saving to a tag
     * @see EntrySerializer#supportSaving()
     * @see EntryStack#saveStack()
     * @since 8.3
     */
    default ListTag saveIngredient() {
        return save();
    }
    
    /**
     * Saves the entry ingredient to a {@link ListTag}. This is only supported if every entry stack has a serializer.
     *
     * @return the saved tag
     * @throws UnsupportedOperationException if an {@link EntryDefinition} does not support saving to a tag
     * @see EntrySerializer#supportSaving()
     * @see EntryStack#saveStack()
     * @deprecated use {@link #saveIngredient()} instead
     */
    @Deprecated(forRemoval = true)
    ListTag save();
    
    /**
     * Casts this {@link EntryStack} to a list of {@link EntryStack} of the given type.
     *
     * @param <T> the type of entry
     * @return the casted list of {@link EntryStack}
     * @deprecated use {@link #castAsList()} instead
     */
    @Deprecated(forRemoval = true)
    default <T> List<EntryStack<T>> cast() {
        return castAsList();
    }
    
    /**
     * Casts this {@link EntryStack} to a list of {@link EntryStack} of the given type.
     *
     * @param <T> the type of entry
     * @return the casted list of {@link EntryStack}
     */
    @SuppressWarnings("rawtypes")
    default <T> List<EntryStack<T>> castAsList() {
        return (List<EntryStack<T>>) (List) this;
    }
    
    /**
     * Filters this {@link EntryIngredient} to only contain {@link EntryStack} that match the given {@link Predicate}.
     *
     * @param filter the filter
     * @return the filtered {@link EntryIngredient}
     */
    EntryIngredient filter(Predicate<EntryStack<?>> filter);
    
    /**
     * Transforms values of this {@link EntryIngredient} by applying the given {@link UnaryOperator}.
     *
     * @param transformer the transformer
     * @return the transformed {@link EntryIngredient}
     */
    EntryIngredient map(UnaryOperator<EntryStack<?>> transformer);
    
    @ApiStatus.NonExtendable
    interface Builder {
        Builder add(EntryStack<?> stack);
        
        Builder add(EntryStack<?>... stacks);
        
        Builder addAll(Iterable<? extends EntryStack<?>> stacks);
        
        EntryIngredient build();
    }
}
