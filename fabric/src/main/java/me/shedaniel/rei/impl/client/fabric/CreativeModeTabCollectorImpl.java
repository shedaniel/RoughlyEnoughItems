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

package me.shedaniel.rei.impl.client.fabric;

import me.shedaniel.rei.impl.common.InternalLogger;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class CreativeModeTabCollectorImpl {
    public static Map<CreativeModeTab, Collection<ItemStack>> collectTabs() {
        Map<CreativeModeTab, Collection<ItemStack>> map = new LinkedHashMap<>();
        FeatureFlagSet featureFlags = FeatureFlags.REGISTRY.allFlags();
        CreativeModeTab.ItemDisplayParameters parameters = new CreativeModeTab.ItemDisplayParameters(featureFlags, true, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        
        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            if (tab.getType() != CreativeModeTab.Type.HOTBAR && tab.getType() != CreativeModeTab.Type.INVENTORY) {
                try {
                    CreativeModeTab.ItemDisplayBuilder builder = new CreativeModeTab.ItemDisplayBuilder(tab, featureFlags);
                    ResourceKey<CreativeModeTab> resourceKey = BuiltInRegistries.CREATIVE_MODE_TAB
                            .getResourceKey(tab)
                            .orElseThrow(() -> new IllegalStateException("Unregistered creative tab: " + tab));
                    tab.displayItemsGenerator.accept(parameters, builder);
                    map.put(tab, postFabricEvents(tab, parameters, resourceKey, builder.tabContents));
                } catch (Throwable throwable) {
                    InternalLogger.getInstance().error("Failed to collect creative tab: " + tab, throwable);
                }
            }
        }
        
        return map;
    }
    
    @SuppressWarnings("UnstableApiUsage")
    private static Collection<ItemStack> postFabricEvents(CreativeModeTab tab, CreativeModeTab.ItemDisplayParameters parameters, ResourceKey<CreativeModeTab> resourceKey, Collection<ItemStack> tabContents) {
        try {
            // Sorry!
            FabricItemGroupEntries entries = new FabricItemGroupEntries(parameters, new LinkedList<>(tabContents), new LinkedList<>());
            ItemGroupEvents.modifyEntriesEvent(resourceKey).invoker().modifyEntries(entries);
            ItemGroupEvents.MODIFY_ENTRIES_ALL.invoker().modifyEntries(tab, entries);
            return entries.getDisplayStacks();
        } catch (Throwable throwable) {
            InternalLogger.getInstance().error("Failed to collect fabric's creative tab: " + tab, throwable);
            return tabContents;
        }
    }
}
