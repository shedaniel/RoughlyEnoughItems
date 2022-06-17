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

package me.shedaniel.rei.api.common.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A display, holds ingredients and information for {@link me.shedaniel.rei.api.client.registry.display.DisplayCategory}
 * to set-up widgets for.
 *
 * @see me.shedaniel.rei.api.common.display.basic.BasicDisplay
 * @see me.shedaniel.rei.api.client.registry.display.DisplayRegistry
 */
public interface Display extends DisplaySpec {
    /**
     * Returns the list of inputs for this display. This only affects the stacks resolving for the display,
     * and not necessarily the stacks that are displayed.
     *
     * @return a list of inputs
     */
    List<EntryIngredient> getInputEntries();
    
    @Deprecated
    default List<EntryIngredient> getInputEntries(MenuSerializationContext<?, ?, ?> context, MenuInfo<?, ?> info, boolean fill) {
        return getInputEntries();
    }
    
    /**
     * Returns the list of inputs for this display, aligned for the menu. This only affects the stacks resolving for the display,
     * and not necessarily the stacks that are displayed.
     * <p>
     * Each ingredient is also provided with the corresponding index slot-wise. The order of the list does not matter.
     *
     * @return a list of inputs
     */
    default List<InputIngredient<EntryStack<?>>> getInputIngredients(MenuSerializationContext<?, ?, ?> context, MenuInfo<?, ?> info, boolean fill) {
        return CollectionUtils.mapIndexed(getInputEntries(context, info, fill), InputIngredient::of);
    }
    
    /**
     * Returns the list of outputs for this display. This only affects the stacks resolving for the display,
     * and not necessarily the stacks that are displayed.
     *
     * @return a list of outputs
     */
    List<EntryIngredient> getOutputEntries();
    
    /**
     * Returns the list of required inputs for this display. This only affects the craftable filter.
     *
     * @return a list of required inputs
     */
    default List<EntryIngredient> getRequiredEntries() {
        return getInputEntries();
    }
    
    /**
     * Returns the identifier of the category this display belongs to.
     *
     * @return the identifier of the category
     */
    CategoryIdentifier<?> getCategoryIdentifier();
    
    /**
     * Returns the display location from data packs.
     *
     * @return the display location
     */
    default Optional<ResourceLocation> getDisplayLocation() {
        return Optional.empty();
    }
    
    @Override
    @ApiStatus.NonExtendable
    default Display provideInternalDisplay() {
        return this;
    }
    
    @Override
    @ApiStatus.NonExtendable
    default Collection<ResourceLocation> provideInternalDisplayIds() {
        Optional<ResourceLocation> location = getDisplayLocation();
        if (location.isPresent()) {
            return Collections.singletonList(location.get());
        } else {
            return Collections.emptyList();
        }
    }
}
