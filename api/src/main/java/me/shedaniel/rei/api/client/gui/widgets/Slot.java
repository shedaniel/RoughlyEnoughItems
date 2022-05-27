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

package me.shedaniel.rei.api.client.gui.widgets;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.common.entry.EntryStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class Slot extends WidgetWithBounds {
    public static final byte UN_MARKED = 0;
    public static final byte INPUT = 1;
    public static final byte OUTPUT = 2;
    
    public Slot unmarkInputOrOutput() {
        setNoticeMark(UN_MARKED);
        return this;
    }
    
    public final Slot markInput() {
        setNoticeMark(INPUT);
        return this;
    }
    
    public final Slot markOutput() {
        setNoticeMark(OUTPUT);
        return this;
    }
    
    public abstract void setNoticeMark(byte mark);
    
    public abstract byte getNoticeMark();
    
    public abstract void setInteractable(boolean interactable);
    
    public abstract boolean isInteractable();
    
    public Slot interactable(boolean interactable) {
        setInteractable(interactable);
        return this;
    }
    
    public final Slot noInteractable() {
        return interactable(false);
    }
    
    public final Slot notInteractable() {
        return interactable(false);
    }
    
    public abstract void setInteractableFavorites(boolean interactableFavorites);
    
    public abstract boolean isInteractableFavorites();
    
    public Slot interactableFavorites(boolean interactableFavorites) {
        setInteractableFavorites(interactableFavorites);
        return this;
    }
    
    public final Slot noFavoritesInteractable() {
        return interactableFavorites(false);
    }
    
    public final Slot notFavoritesInteractable() {
        return interactableFavorites(false);
    }
    
    public abstract void setHighlightEnabled(boolean highlights);
    
    public abstract boolean isHighlightEnabled();
    
    public final Slot highlightEnabled(boolean highlight) {
        setHighlightEnabled(highlight);
        return this;
    }
    
    public final Slot disableHighlight() {
        return highlightEnabled(false);
    }
    
    public abstract void setTooltipsEnabled(boolean tooltipsEnabled);
    
    public abstract boolean isTooltipsEnabled();
    
    public final Slot tooltipsEnabled(boolean tooltipsEnabled) {
        setTooltipsEnabled(tooltipsEnabled);
        return this;
    }
    
    public final Slot disableTooltips() {
        return tooltipsEnabled(false);
    }
    
    public abstract void setBackgroundEnabled(boolean backgroundEnabled);
    
    public abstract boolean isBackgroundEnabled();
    
    public final Slot backgroundEnabled(boolean backgroundEnabled) {
        setBackgroundEnabled(backgroundEnabled);
        return this;
    }
    
    public final Slot disableBackground() {
        return backgroundEnabled(false);
    }
    
    public abstract Slot clearEntries();
    
    public abstract Slot entry(EntryStack<?> stack);
    
    public abstract Slot entries(Collection<? extends EntryStack<?>> stacks);
    
    public abstract EntryStack<?> getCurrentEntry();
    
    public abstract List<EntryStack<?>> getEntries();
    
    public abstract Rectangle getInnerBounds();
    
    @Nullable
    public Tooltip getCurrentTooltip(TooltipContext context) {
        return null;
    }
}
