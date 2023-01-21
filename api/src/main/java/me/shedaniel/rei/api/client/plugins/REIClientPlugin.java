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

package me.shedaniel.rei.api.client.plugins;

import me.shedaniel.rei.api.client.config.addon.ConfigAddonRegistry;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.entry.renderer.EntryRendererRegistry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.api.client.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
public interface REIClientPlugin extends REIPlugin<REIClientPlugin> {
    /**
     * Registers new entry renderers
     *
     * @param registry the entry renderer registry
     */
    @ApiStatus.OverrideOnly
    @ApiStatus.Experimental
    default void registerEntryRenderers(EntryRendererRegistry registry) {
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
     * Registers basic entry filtering.
     *
     * @param rule the filtering rule
     */
    @ApiStatus.OverrideOnly
    default void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
    }
    
    /**
     * Registers entries to collapse on the entry panel.
     *
     * @param registry the collapsible entry registry
     */
    @ApiStatus.OverrideOnly
    @ApiStatus.Experimental
    default void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
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
    @Deprecated(forRemoval = true)
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
    
    /**
     * Registers new config addons.
     *
     * @param registry the registry
     * @since 8.3
     */
    @ApiStatus.OverrideOnly
    @ApiStatus.Experimental
    default void registerConfigAddons(ConfigAddonRegistry registry) {
    }
    
    /**
     * Registers new input methods.
     *
     * @param registry the registry
     * @since 8.3
     */
    @ApiStatus.OverrideOnly
    @ApiStatus.Experimental
    default void registerInputMethods(InputMethodRegistry registry) {
    }
    
    @Override
    default Class<REIClientPlugin> getPluginProviderClass() {
        return REIClientPlugin.class;
    }
}
