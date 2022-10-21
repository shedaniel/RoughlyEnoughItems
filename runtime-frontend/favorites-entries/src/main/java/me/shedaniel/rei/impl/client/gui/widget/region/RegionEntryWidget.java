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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.FloatingPoint;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.entry.region.RegionEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.impl.client.gui.menu.MenuAccess;
import me.shedaniel.rei.impl.client.gui.overlay.AbstractScreenOverlay;
import me.shedaniel.rei.impl.client.gui.overlay.widgets.DisplayedEntryWidget;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class RegionEntryWidget<T extends RegionEntry<T>> extends DisplayedEntryWidget {
    private final RealRegionEntry<T> entry;
    
    RegionEntryWidget(RealRegionEntry<T> entry, Slot slot, int entrySize) {
        super(slot);
        this.entry = entry;
        slot.size(entrySize);
        slot.entry(entry.getEntry().toStack());
        slot.setFavoriteEntryFunction(stack -> asFavoriteEntry());
        slot.setNoticeMark(Slot.FAVORITE);
        slot.noBackground();
    }
    
    public static <T extends RegionEntry<T>> RegionEntryWidget<T> createSlot(RealRegionEntry<T> entry, int x, int y, int entrySize) {
        return new RegionEntryWidget<>(entry, Widgets.createSlot(new Point(x, y)), entrySize);
    }
    
    protected FavoriteEntry asFavoriteEntry() {
        return entry.region.listener.asFavorite(entry);
    }
    
    public void renderExtra(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Optional<ScreenOverlay> overlayOptional = REIRuntime.getInstance().getOverlay();
        Optional<Supplier<Collection<FavoriteMenuEntry>>> menuEntries = entry.getEntry().getMenuEntries();
        FloatingPoint value = entry.pos.value();
        FloatingPoint target = entry.pos.target();
        if (Math.abs(value.x - target.x) < 1 && Math.abs(value.y - target.y) < 1 && overlayOptional.isPresent() && menuEntries.isPresent()) {
            AbstractScreenOverlay overlay = (AbstractScreenOverlay) overlayOptional.get();
            MenuAccess access = overlay.menuAccess();
            UUID uuid = entry.getEntry().getUuid();
            
            access.openOrClose(uuid, slot.getBounds(), menuEntries.get());
        }
    }
    
    @Override
    public boolean doMouse(Slot slot, double mouseX, double mouseY, int button) {
        return entry.getEntry().doAction(button) || super.doMouse(slot, mouseX, mouseY, button);
    }
    
    public RealRegionEntry<T> getEntry() {
        return entry;
    }
}