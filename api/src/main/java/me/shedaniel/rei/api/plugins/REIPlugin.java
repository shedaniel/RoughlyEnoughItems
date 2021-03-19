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

package me.shedaniel.rei.api.plugins;

import me.shedaniel.rei.api.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.ingredient.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.ingredient.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.subsets.SubsetsRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.OverrideOnly
public interface REIPlugin extends Comparable<REIPlugin> {
    /**
     * @return the priority of the plugin, the smaller the number, the earlier it is called.
     */
    default int getPriority() {
        return 0;
    }
    
    default String getPluginName() {
        Class<? extends REIPlugin> self = getClass();
        String simpleName = self.getSimpleName();
        return simpleName == null ? self.getName() : simpleName;
    }
    
    @Override
    default int compareTo(@NotNull REIPlugin o) {
        return Double.compare(getPriority(), o.getPriority());
    }
    
    /**
     * Registers new types of entries
     */
    @ApiStatus.OverrideOnly
    default void registerEntryTypes(EntryTypeRegistry registry) {
    }
    
    /**
     * Registers item comparators for identifying variants of {@link net.minecraft.world.item.ItemStack}.
     *
     * @see ItemComparatorRegistry
     */
    @ApiStatus.OverrideOnly
    default void registerItemComparators(ItemComparatorRegistry registry) {
    }
    
    /**
     * Registers new item to fluid support providers.
     *
     * @param support the support registry
     */
    @ApiStatus.OverrideOnly
    default void registerFluidSupport(FluidSupportProvider support) {
    }
    
    /**
     * Registers new categories
     *
     * @param registry the category registry
     */
    @ApiStatus.OverrideOnly
    default void registerCategories(CategoryRegistry registry) {
    }
    
    /**
     * Registers new displays for categories
     *
     * @param registry the display registry
     */
    @ApiStatus.OverrideOnly
    default void registerDisplays(DisplayRegistry registry) {
    }
    
    /**
     * Registers screen deciders
     *
     * @param registry the screen registry
     */
    @ApiStatus.OverrideOnly
    default void registerScreens(ScreenRegistry registry) {
    }
    
    /**
     * Registers screen exclusion zones
     *
     * @param zones the exclusion zones registry
     */
    @ApiStatus.OverrideOnly
    default void registerExclusionZones(ExclusionZones zones) {
    }
    
    /**
     * Registers entries on the entry panel.
     *
     * @param registry the entry registry
     */
    @ApiStatus.OverrideOnly
    default void registerEntries(EntryRegistry registry) {
    }
    
    /**
     * Registers favorite entry types.
     *
     * @param registry the registry
     */
    @ApiStatus.OverrideOnly
    default void registerFavorites(FavoriteEntryType.Registry registry) {
    }
    
    /**
     * Registers new subset categories
     *
     * @param registry the registry
     */
    @ApiStatus.OverrideOnly
    default void registerSubsets(SubsetsRegistry registry) {
    }
    
    /**
     * Registers new transfer handlers
     *
     * @param registry the registry
     */
    @ApiStatus.OverrideOnly
    default void registerTransferHandlers(TransferHandlerRegistry registry) {
    }
    
    @ApiStatus.OverrideOnly
    default void preRegister() {
    }
    
    @ApiStatus.OverrideOnly
    default void postRegister() {
    }
}
