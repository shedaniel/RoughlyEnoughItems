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

package me.shedaniel.rei.api.common.entry.type;

import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * A definition of an {@link EntryType}, an interface to provide information from an object type.
 *
 * @param <T> the type of entry
 * @see EntryTypeRegistry
 */
public interface EntryDefinition<T> {
    /**
     * Returns the type of the entry.
     *
     * @return the type of the entry
     */
    Class<T> getValueType();
    
    /**
     * Returns the type of this definition. The type is also used for comparing the type of two definitions,
     * as the definition does not guarantee object and reference equality.
     *
     * @return the type of this definition
     */
    EntryType<T> getType();
    
    /**
     * Returns the renderer for this entry, this is used to render the entry, and provide tooltip.
     * External plugins can extend this method using {@link me.shedaniel.rei.api.client.entry.renderer.EntryRendererRegistry}
     * to provide custom renderers.
     *
     * @return the renderer for this entry
     */
    @Environment(EnvType.CLIENT)
    EntryRenderer<T> getRenderer();
    
    /**
     * Returns the identifier for an entry, used in identifier search argument type.
     *
     * @param entry the entry
     * @param value the value of the entry
     * @return the identifier for an entry
     */
    @Nullable
    ResourceLocation getIdentifier(EntryStack<T> entry, T value);
    
    /**
     * Returns the container namespace of the entry, used for determining the
     * responsible mod for the entry.
     *
     * @param entry the entry
     * @param value the value of the entry
     * @return the identifier for an entry
     */
    @Nullable
    default String getContainingNamespace(EntryStack<T> entry, T value) {
        ResourceLocation identifier = getIdentifier(entry, value);
        
        if (identifier == null) {
            return null;
        } else {
            return identifier.getNamespace();
        }
    }
    
    /**
     * Returns whether the entry is empty, empty entries are not displayed,
     * and are considered invalid.
     * Empty entries will be treated equally to {@link EntryStack#empty()}.
     *
     * @param entry the entry
     * @param value the value of the entry
     * @return whether the entry is empty
     */
    boolean isEmpty(EntryStack<T> entry, T value);
    
    /**
     * Returns a copy for an entry.
     *
     * @param entry the entry
     * @param value the value of the entry
     * @return a copy for an entry
     */
    T copy(EntryStack<T> entry, T value);
    
    /**
     * Returns a normalized copy for an entry.
     * The returned stack should be functionally equivalent to the original stack,
     * but should have a normalized state.
     * <p>
     * For example, an {@link net.minecraft.world.item.ItemStack} should have its
     * amount removed, but its tags kept.
     *
     * @param entry the entry
     * @param value the value of the entry
     * @return a normalized copy for an entry
     */
    T normalize(EntryStack<T> entry, T value);
    
    /**
     * Returns a wildcard copy for an entry.
     * The returned stack should be the bare minimum to match the original stack.
     * <p>
     * For example, an {@link net.minecraft.world.item.ItemStack} should have its
     * amount and tags removed.
     *
     * @param entry the entry
     * @param value the value of the entry
     * @return a wildcard copy for an entry
     * @since 6.2
     */
    T wildcard(EntryStack<T> entry, T value);
    
    /**
     * Returns the cheated stack of this {@link EntryStack}.
     *
     * @return the cheated stack of this {@link EntryStack}, or {@code null} if there is no such equivalent
     */
    @Nullable
    default ItemStack cheatsAs(EntryStack<T> entry, T value) {
        return null;
    }
    
    /**
     * Returns a merged copy for two entries.
     * <p>
     * It is guaranteed that the two entries satisfy {@link #equals(Object, Object, ComparisonContext)}
     * with the {@link ComparisonContext#EXACT} context.
     * <p>
     * Implementation of this method should just need to copy the first value and sets the count to
     * the sum of both values.
     *
     * @param o1 the first value
     * @param o2 the second value
     * @return the merged copy
     * @since 8.3
     */
    @Nullable
    default T add(T o1, T o2) {
        return null;
    }
    
    long hash(EntryStack<T> entry, T value, ComparisonContext context);
    
    boolean equals(T o1, T o2, ComparisonContext context);
    
    @Nullable
    EntrySerializer<T> getSerializer();
    
    Component asFormattedText(EntryStack<T> entry, T value);
    
    default Component asFormattedText(EntryStack<T> entry, T value, TooltipContext context) {
        return asFormattedText(entry, value);
    }
    
    /**
     * Returns a stream of {@link TagKey} for an entry.
     * It is not guaranteed that the stream is ordered, or that the {@link TagKey}
     * contains the registry key.
     *
     * @return a stream of {@link TagKey} for an entry
     */
    Stream<? extends TagKey<?>> getTagsFor(EntryStack<T> entry, T value);
    
    @ApiStatus.NonExtendable
    default <O> EntryDefinition<O> cast() {
        return (EntryDefinition<O>) this;
    }
    
    /**
     * Returns whether this entry definition accepts {@code null} values.
     *
     * @return whether this entry definition accepts {@code null} values
     */
    default boolean acceptsNull() {
        return true;
    }
    
    default void fillCrashReport(CrashReport report, CrashReportCategory category, EntryStack<T> entry) {
        category.setDetail("Entry definition class name", () -> getClass().getCanonicalName());
    }
}

