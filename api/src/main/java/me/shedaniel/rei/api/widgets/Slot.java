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

package me.shedaniel.rei.api.widgets;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class Slot extends WidgetWithBounds {
    @NotNull
    public Slot unmarkInputOrOutput() {
        setNoticeMark((byte) 0);
        return this;
    }
    
    @NotNull
    public final Slot markInput() {
        setNoticeMark((byte) 1);
        return this;
    }
    
    @NotNull
    public final Slot markOutput() {
        setNoticeMark((byte) 2);
        return this;
    }
    
    public abstract void setNoticeMark(byte mark);
    
    public abstract byte getNoticeMark();
    
    public abstract void setInteractable(boolean interactable);
    
    public abstract boolean isInteractable();
    
    @NotNull
    public Slot interactable(boolean interactable) {
        setInteractable(interactable);
        return this;
    }
    
    @NotNull
    public final Slot noInteractable() {
        return interactable(false);
    }
    
    @NotNull
    public final Slot notInteractable() {
        return interactable(false);
    }
    
    public abstract void setInteractableFavorites(boolean interactableFavorites);
    
    public abstract boolean isInteractableFavorites();
    
    @NotNull
    public Slot interactableFavorites(boolean interactableFavorites) {
        setInteractableFavorites(interactableFavorites);
        return this;
    }
    
    @NotNull
    public final Slot noFavoritesInteractable() {
        return interactableFavorites(false);
    }
    
    @NotNull
    public final Slot notFavoritesInteractable() {
        return interactableFavorites(false);
    }
    
    public abstract void setHighlightEnabled(boolean highlights);
    
    public abstract boolean isHighlightEnabled();
    
    @NotNull
    public final Slot highlightEnabled(boolean highlight) {
        setHighlightEnabled(highlight);
        return this;
    }
    
    @NotNull
    public final Slot disableHighlight() {
        return highlightEnabled(false);
    }
    
    public abstract void setTooltipsEnabled(boolean tooltipsEnabled);
    
    public abstract boolean isTooltipsEnabled();
    
    @NotNull
    public final Slot tooltipsEnabled(boolean tooltipsEnabled) {
        setTooltipsEnabled(tooltipsEnabled);
        return this;
    }
    
    @NotNull
    public final Slot disableTooltips() {
        return tooltipsEnabled(false);
    }
    
    public abstract void setBackgroundEnabled(boolean backgroundEnabled);
    
    public abstract boolean isBackgroundEnabled();
    
    @NotNull
    public final Slot backgroundEnabled(boolean backgroundEnabled) {
        setBackgroundEnabled(backgroundEnabled);
        return this;
    }
    
    @NotNull
    public final Slot disableBackground() {
        return backgroundEnabled(false);
    }
    
    @NotNull
    public abstract Slot clearEntries();
    
    @NotNull
    public abstract Slot entry(EntryStack<?> stack);
    
    @NotNull
    public abstract Slot entries(Collection<? extends EntryStack<?>> stacks);
    
    @NotNull
    public abstract List<EntryStack<?>> getEntries();
    
    @Nullable
    public Tooltip getCurrentTooltip(Point point) {
        return null;
    }
}
