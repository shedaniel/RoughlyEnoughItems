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

package me.shedaniel.rei.impl.client.gui.widget.region;

import me.shedaniel.rei.api.client.entry.region.RegionEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public interface RegionListener<T extends RegionEntry<T>> {
    default void onDrop(Stream<T> entries) {}
    
    @Nullable
    default T convertDraggableStack(DraggingContext<Screen> context, DraggableStack stack) {
        return null;
    }
    
    default boolean canAcceptDrop(RealRegionEntry<T> entry) {
        return true;
    }
    
    @Nullable
    default FavoriteEntry asFavorite(RealRegionEntry<T> entry) {
        return entry.getEntry().asFavorite();
    }
    
    default boolean canBeDragged(RealRegionEntry<T> entry) {
        return true;
    }
    
    default boolean removeOnDrag() {
        return true;
    }
    
    default void onRemove(RealRegionEntry<T> entry) {}
    
    default void onAdd(RealRegionEntry<T> entry) {}
    
    default void onSetNewEntries(List<RegionEntryListEntry<T>> entries) {}
    
    default void onSetNewEntries(Stream<T> entries) {}
    
    default void onConsumed(RealRegionEntry<T> entry) {}
}
