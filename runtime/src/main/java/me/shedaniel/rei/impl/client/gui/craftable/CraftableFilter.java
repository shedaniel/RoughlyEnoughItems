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

package me.shedaniel.rei.impl.client.gui.craftable;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import me.shedaniel.rei.impl.client.ClientHelperImpl;

public class CraftableFilter {
    public static final CraftableFilter INSTANCE = new CraftableFilter();
    private boolean dirty = false;
    private Long2LongMap invStacks = new Long2LongOpenHashMap();
    private Long2LongMap containerStacks = new Long2LongOpenHashMap();
    
    public void markDirty() {
        dirty = true;
    }
    
    public boolean wasDirty() {
        if (dirty) {
            dirty = false;
            return true;
        }
        
        return false;
    }
    
    public void tick() {
        if (dirty) return;
        Long2LongMap currentStacks;
        try {
            currentStacks = ClientHelperImpl.getInstance()._getInventoryItemsTypes();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            currentStacks = Long2LongMaps.EMPTY_MAP;
        }
        if (!currentStacks.equals(this.invStacks)) {
            invStacks = currentStacks;
            markDirty();
        }
        if (dirty) return;
    
        try {
            currentStacks = ClientHelperImpl.getInstance()._getContainerItemsTypes();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            currentStacks = Long2LongMaps.EMPTY_MAP;
        }
        if (!currentStacks.equals(this.containerStacks)) {
            containerStacks = currentStacks;
            markDirty();
        }
    }
    
    public Long2LongMap getInvStacks() {
        return invStacks;
    }
}
