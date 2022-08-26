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

package me.shedaniel.rei.impl.client.view.craftable;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.impl.client.provider.OverlayTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

public class CraftableFilter implements OverlayTicker {
    private static Long2LongMap invStacks = new Long2LongOpenHashMap();
    private static Long2LongMap containerStacks = new Long2LongOpenHashMap();
    
    @Override
    public void tick() {
        if (Minecraft.getInstance().player == null || PluginManager.areAnyReloading() || Minecraft.getInstance().player.tickCount % 5 != 0) {
            return;
        }
        
        Optional<ScreenOverlay> overlay = REIRuntime.getInstance().getOverlay();
        if (overlay.isEmpty() || overlay.get().isSearchReloadQueued()) return;
        if (!ConfigManager.getInstance().isCraftableOnlyEnabled()) return;
        Long2LongMap currentStacks;
        try {
            currentStacks = getInventoryItemsTypes();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            currentStacks = Long2LongMaps.EMPTY_MAP;
        }
        if (!currentStacks.equals(CraftableFilter.invStacks)) {
            invStacks = currentStacks;
            overlay.ifPresent(ScreenOverlay::queueReloadSearch);
            return;
        }
        
        try {
            currentStacks = getContainerItemsTypes();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            currentStacks = Long2LongMaps.EMPTY_MAP;
        }
        if (!currentStacks.equals(CraftableFilter.containerStacks)) {
            containerStacks = currentStacks;
            overlay.ifPresent(ScreenOverlay::queueReloadSearch);
        }
    }
    
    public static Long2LongMap getInvStacks() {
        return invStacks;
    }
    
    @ApiStatus.Internal
    public static Long2LongMap getInventoryItemsTypes() {
        EntryDefinition<ItemStack> definition;
        try {
            definition = VanillaEntryTypes.ITEM.getDefinition();
        } catch (NullPointerException e) {
            return Long2LongMaps.EMPTY_MAP;
        }
        Long2LongOpenHashMap map = new Long2LongOpenHashMap();
        for (NonNullList<ItemStack> compartment : Minecraft.getInstance().player.getInventory().compartments) {
            for (ItemStack stack : compartment) {
                long hash = definition.hash(null, stack, ComparisonContext.FUZZY);
                long newCount = map.getOrDefault(hash, 0) + Math.max(0, stack.getCount());
                map.put(hash, newCount);
            }
        }
        return map;
    }
    
    @ApiStatus.Internal
    public static Long2LongMap getContainerItemsTypes() {
        EntryDefinition<ItemStack> definition;
        try {
            definition = VanillaEntryTypes.ITEM.getDefinition();
        } catch (NullPointerException e) {
            return Long2LongMaps.EMPTY_MAP;
        }
        Long2LongOpenHashMap map = new Long2LongOpenHashMap();
        AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
        if (menu != null) {
            for (Slot slot : menu.slots) {
                ItemStack stack = slot.getItem();
                
                if (!stack.isEmpty()) {
                    long hash = definition.hash(null, stack, ComparisonContext.FUZZY);
                    long newCount = map.getOrDefault(hash, 0) + Math.max(0, stack.getCount());
                    map.put(hash, newCount);
                }
            }
        }
        return map;
    }
}
