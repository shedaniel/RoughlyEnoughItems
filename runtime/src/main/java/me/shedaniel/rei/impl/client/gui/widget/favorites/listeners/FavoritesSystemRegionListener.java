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

package me.shedaniel.rei.impl.client.gui.widget.favorites.listeners;

import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.impl.client.gui.widget.region.RealRegionEntry;
import me.shedaniel.rei.impl.client.gui.widget.region.RegionListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class FavoritesSystemRegionListener implements RegionListener<FavoriteEntry> {
    @Override
    @Nullable
    public FavoriteEntry convertDraggableStack(DraggingContext<Screen> context, DraggableStack stack) {
        return FavoriteEntry.fromEntryStack(stack.getStack().copy());
    }
    
    @Override
    public boolean canAcceptDrop(RealRegionEntry<FavoriteEntry> entry) {
        return false;
    }
    
    @Override
    @Nullable
    public FavoriteEntry asFavorite(RealRegionEntry<FavoriteEntry> entry) {
        return null;
    }
    
    @Override
    public boolean removeOnDrag() {
        return false;
    }
}