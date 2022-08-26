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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.*;

public abstract class Slot extends WidgetWithBounds {
    public static final byte UN_MARKED = 0;
    public static final byte INPUT = 1;
    public static final byte OUTPUT = 2;
    @ApiStatus.Experimental
    public static final byte FAVORITE = 4;
    
    public final Slot unmarkInputOrOutput() {
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
    
    public final void size(float size) {
        this.getBounds().setSize(size, size);
    }
    
    public final void size(float width, float height) {
        this.getBounds().setSize(width, height);
    }
    
    public abstract void setNoticeMark(byte mark);
    
    public abstract byte getNoticeMark();
    
    public abstract void setInteractable(boolean interactable);
    
    public abstract boolean isInteractable();
    
    public final Slot interactable(boolean interactable) {
        setInteractable(interactable);
        return this;
    }
    
    public final Slot noInteractable() {
        return interactable(false);
    }
    
    @Deprecated(forRemoval = true)
    public final Slot notInteractable() {
        return interactable(false);
    }
    
    public abstract void setInteractableFavorites(boolean interactableFavorites);
    
    public abstract boolean isInteractableFavorites();
    
    public final Slot interactableFavorites(boolean interactableFavorites) {
        setInteractableFavorites(interactableFavorites);
        return this;
    }
    
    public final Slot noFavoritesInteractable() {
        return interactableFavorites(false);
    }
    
    @Deprecated(forRemoval = true)
    public final Slot notFavoritesInteractable() {
        return interactableFavorites(false);
    }
    
    public final void setHighlightEnabled(boolean highlights) {
        setHighlightEnabled(slot -> highlights);
    }
    
    public abstract void setHighlightEnabled(Predicate<Slot> highlights);
    
    public abstract boolean isHighlightEnabled();
    
    public final Slot highlightEnabled(boolean highlight) {
        setHighlightEnabled(highlight);
        return this;
    }
    
    public abstract Slot highlightEnabled(Predicate<Slot> highlight);
    
    public final Slot noHighlight() {
        return highlightEnabled(false);
    }
    
    public final Slot noHighlightIfEmpty() {
        setHighlightEnabled(slot -> !slot.isEmpty());
        return this;
    }
    
    @Deprecated(forRemoval = true)
    public final Slot disableHighlight() {
        return highlightEnabled(false);
    }
    
    public void setTooltipsEnabled(boolean tooltipsEnabled) {
        setTooltipsEnabled(slot -> tooltipsEnabled);
    }
    
    public abstract void setTooltipsEnabled(Predicate<Slot> tooltipsEnabled);
    
    public abstract boolean isTooltipsEnabled();
    
    public final Slot tooltipsEnabled(boolean tooltipsEnabled) {
        setTooltipsEnabled(tooltipsEnabled);
        return this;
    }
    
    public abstract Slot tooltipsEnabled(Predicate<Slot> tooltipsEnabled);
    
    public final Slot noTooltips() {
        return tooltipsEnabled(false);
    }
    
    @Deprecated(forRemoval = true)
    public final Slot disableTooltips() {
        return tooltipsEnabled(false);
    }
    
    public void setBackgroundEnabled(boolean backgroundEnabled) {
        setBackgroundEnabled(slot -> backgroundEnabled);
    }
    
    public abstract void setBackgroundEnabled(Predicate<Slot> backgroundEnabled);
    
    public abstract boolean isBackgroundEnabled();
    
    public final Slot backgroundEnabled(boolean backgroundEnabled) {
        setBackgroundEnabled(backgroundEnabled);
        return this;
    }
    
    public abstract Slot backgroundEnabled(Predicate<Slot> backgroundEnabled);
    
    public final Slot noBackground() {
        return backgroundEnabled(false);
    }
    
    @Deprecated(forRemoval = true)
    public final Slot disableBackground() {
        return backgroundEnabled(false);
    }
    
    public abstract void setCyclingInterval(long cyclingInterval);
    
    public abstract long getCyclingInterval();
    
    public final Slot cyclingInterval(long cyclingInterval) {
        setCyclingInterval(cyclingInterval);
        return this;
    }
    
    public abstract Slot clearEntries();
    
    public abstract Slot entry(EntryStack<?> stack);
    
    public abstract Slot entries(Collection<? extends EntryStack<?>> stacks);
    
    public abstract EntryStack<?> getCurrentEntry();
    
    public abstract List<EntryStack<?>> getEntries();
    
    public final boolean isEmpty() {
        return getEntries().isEmpty();
    }
    
    public abstract Rectangle getInnerBounds();
    
    public abstract void drawBackground(PoseStack matrices, int mouseX, int mouseY, float delta);
    
    @Nullable
    @Deprecated(forRemoval = true)
    public Tooltip getCurrentTooltip(Point point) {
        return getCurrentTooltip(TooltipContext.of(point));
    }
    
    public abstract void drawExtra(PoseStack matrices, int mouseX, int mouseY, float delta);
    
    @Nullable
    public Tooltip getCurrentTooltip(TooltipContext context) {
        return null;
    }
    
    public abstract void drawHighlighted(PoseStack matrices, int mouseX, int mouseY, float delta);
    
    public abstract void tooltipProcessor(UnaryOperator<Tooltip> operator);
    
    public abstract void action(ActionPredicate predicate);
    
    @ApiStatus.Experimental
    public abstract void setFavoriteEntryFunction(Function<EntryStack<?>, FavoriteEntry> function);
    
    @ApiStatus.Experimental
    public abstract Function<EntryStack<?>, FavoriteEntry> getFavoriteEntryFunction();
    
    @ApiStatus.Experimental
    public abstract void setContainsPointFunction(BiPredicate<Slot, Point> function);
    
    @ApiStatus.Experimental
    public abstract void appendContainsPointFunction(BiPredicate<Slot, Point> function);
    
    public interface ActionPredicate {
        boolean doMouse(Slot slot, double mouseX, double mouseY, int button);
        
        boolean doKey(Slot slot, int keyCode, int scanCode, int modifiers);
    }
}
