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
import com.mojang.math.Vector4f;
import me.shedaniel.math.FloatingPoint;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.entry.region.RegionEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.modules.MenuAccess;
import me.shedaniel.rei.impl.client.gui.modules.MenuEntry;
import me.shedaniel.rei.impl.client.gui.widget.DisplayedEntryWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.*;
import java.util.function.Supplier;

public class RegionEntryWidget<T extends RegionEntry<T>> extends DisplayedEntryWidget {
    private final RealRegionEntry<T> entry;
    
    RegionEntryWidget(RealRegionEntry<T> entry, int x, int y, int entrySize) {
        super(new Point(x, y), entrySize);
        this.entry = entry;
        this.clearEntries().entry(entry.getEntry().toStack());
    }
    
    @Override
    protected FavoriteEntry asFavoriteEntry() {
        return entry.region.listener.asFavorite(entry);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return super.containsMouse(mouseX, mouseY) && entry.region.containsMouse(mouseX, mouseY);
    }
    
    @Override
    protected boolean reverseFavoritesAction() {
        return true;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Optional<ScreenOverlay> overlayOptional = REIRuntime.getInstance().getOverlay();
        Optional<Supplier<Collection<FavoriteMenuEntry>>> menuEntries = entry.getEntry().getMenuEntries();
        FloatingPoint value = entry.pos.value();
        FloatingPoint target = entry.pos.target();
        if (Math.abs(value.x - target.x) < 1 && Math.abs(value.y - target.y) < 1 && overlayOptional.isPresent() && menuEntries.isPresent()) {
            ScreenOverlayImpl overlay = (ScreenOverlayImpl) overlayOptional.get();
            MenuAccess access = overlay.menuAccess();
            UUID uuid = entry.getEntry().getUuid();
            
            access.openOrClose(uuid, getBounds(), () ->
                    CollectionUtils.map(menuEntries.get().get(), entry -> convertMenu(overlay, entry)));
        }
        Vector4f vector4f = new Vector4f(mouseX, mouseY, 0, 1.0F);
        vector4f.transform(matrices.last().pose());
        super.render(matrices, (int) vector4f.x(), (int) vector4f.y(), delta);
    }
    
    private MenuEntry convertMenu(ScreenOverlayImpl overlay, FavoriteMenuEntry entry) {
        return new MenuEntry() {
            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.singletonList(entry);
            }
            
            @Override
            public void render(PoseStack poseStack, int i, int j, float f) {
                entry.render(poseStack, i, j, f);
            }
            
            @Override
            public int getEntryWidth() {
                return entry.getEntryWidth();
            }
            
            @Override
            public int getEntryHeight() {
                return entry.getEntryHeight();
            }
            
            @Override
            public void updateInformation(int xPos, int yPos, boolean selected, boolean containsMouse, boolean rendering, int width) {
                entry.closeMenu = overlay.menuAccess()::close;
                entry.updateInformation(xPos, yPos, selected, containsMouse, rendering, width);
            }
            
            @Override
            public int getZ() {
                return entry.getZ();
            }
            
            @Override
            public void setZ(int z) {
                entry.setZ(z);
            }
        };
    }
    
    @Override
    protected boolean doAction(double mouseX, double mouseY, int button) {
        return entry.getEntry().doAction(button) || super.doAction(mouseX, mouseY, button);
    }
    
    public RealRegionEntry<T> getEntry() {
        return entry;
    }
}