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

package me.shedaniel.rei.impl.client.gui.widget.favorites;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.favorites.SystemFavoriteEntryProvider;
import me.shedaniel.rei.api.client.gui.drag.*;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.favorites.FavoriteEntryTypeRegistryImpl;
import me.shedaniel.rei.impl.client.gui.widget.favorites.listeners.FavoritesRegionListener;
import me.shedaniel.rei.impl.client.gui.widget.favorites.listeners.FavoritesSystemRegionListener;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.FavoritesPanel;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.FavoritesTogglePanelButton;
import me.shedaniel.rei.impl.client.gui.widget.favorites.trash.TrashWidget;
import me.shedaniel.rei.impl.client.gui.widget.region.EntryStacksRegionWidget;
import me.shedaniel.rei.impl.client.gui.widget.region.RealRegionEntry;
import me.shedaniel.rei.impl.client.gui.widget.region.RegionDraggableStack;
import net.minecraft.client.gui.screens.Screen;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@ApiStatus.Internal
public class FavoritesListWidget extends WidgetWithBounds implements DraggableStackProviderWidget, DraggableStackVisitorWidget, OverlayListWidget {
    public Rectangle fullBounds;
    private EntryStacksRegionWidget<FavoriteEntry> systemRegion = new EntryStacksRegionWidget<>(new FavoritesSystemRegionListener());
    private EntryStacksRegionWidget<FavoriteEntry> region = new EntryStacksRegionWidget<>(new FavoritesRegionListener(this));
    private List<FavoriteEntry> lastSystemEntries = new ArrayList<>();
    
    public final FavoritesPanel favoritePanel = new FavoritesPanel(this);
    public final TrashWidget trash = new TrashWidget(this);
    public final FavoritesTogglePanelButton togglePanelButton = new FavoritesTogglePanelButton(this);
    private List<Widget> children = ImmutableList.of(favoritePanel, togglePanelButton, systemRegion, region);
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (fullBounds.contains(mouseX, mouseY)) {
            if (Screen.hasControlDown()) {
                ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
                if (config.setEntrySize(config.getEntrySize() + amount * 0.075)) {
                    ConfigManager.getInstance().saveConfig();
                    REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
                    return true;
                }
            } else if (favoritePanel.mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public Rectangle getBounds() {
        return fullBounds;
    }
    
    public EntryStacksRegionWidget<FavoriteEntry> getRegion() {
        return region;
    }
    
    public EntryStacksRegionWidget<FavoriteEntry> getSystemRegion() {
        return systemRegion;
    }
    
    @Override
    @Nullable
    public DraggableStack getHoveredStack(DraggingContext<Screen> context, double mouseX, double mouseY) {
        DraggableStack stack = region.getHoveredStack(context, mouseX, mouseY);
        if (stack != null) return stack;
        stack = systemRegion.getHoveredStack(context, mouseX, mouseY);
        if (stack != null) return stack;
        if (favoritePanel.containsMouse(mouseX, mouseY)) {
            return favoritePanel.getHoveredStack(mouseX, mouseY);
        }
        
        return null;
    }
    
    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
        if (favoritePanel.containsMouse(context.getCurrentPosition()) || trash.containsMouse(context.getCurrentPosition())) {
            context.renderToVoid(stack);
            return DraggedAcceptorResult.CONSUMED;
        }
        return Stream.of(region, systemRegion)
                .map(visitor -> visitor.acceptDraggedStack(context, stack))
                .filter(result -> result != DraggedAcceptorResult.PASS)
                .findFirst()
                .orElse(DraggedAcceptorResult.PASS);
    }
    
    @Override
    public EntryStack<?> getFocusedStack() {
        Point mouse = PointHelper.ofMouse();
        EntryStack<?> stack = region.getFocusedStack();
        if (stack != null && !stack.isEmpty()) return stack;
        stack = systemRegion.getFocusedStack();
        if (stack != null && !stack.isEmpty()) return stack;
        if (favoritePanel.containsMouse(mouse)) {
            EntryStack<?> focusedStack = favoritePanel.getFocusedStack(mouse);
            
            if (focusedStack != null) {
                return focusedStack;
            }
        }
        return EntryStack.empty();
    }
    
    @Override
    public Stream<EntryStack<?>> getEntries() {
        return region.getEntries();
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (fullBounds.isEmpty())
            return;
        
        this.trash.render(matrices, mouseX, mouseY, delta);
        double trashHeight = this.trash.getHeight();
        
        if (!PluginManager.areAnyReloading()) {
            updateSystemRegion();
        }
        
        systemRegion.getBounds().setBounds(this.fullBounds.x, this.fullBounds.y + 1, this.fullBounds.width, Math.max(1, systemRegion.scrolling.getMaxScrollHeight()));
        int systemHeight = systemRegion.getBounds().getHeight();
        if (systemHeight > 1 && !region.isEmpty()) {
            Rectangle innerBounds = systemRegion.getInnerBounds();
            fillGradient(matrices, innerBounds.x + 1, this.fullBounds.y + systemHeight + 2, innerBounds.getMaxX() - 1, this.fullBounds.y + systemHeight + 3, 0xFF777777, 0xFF777777);
            systemHeight += 4;
        }
        
        if (favoritePanel.getBounds().height > 20) {
            region.getBounds().setBounds(this.fullBounds.x, this.fullBounds.y + systemHeight, this.fullBounds.width, this.fullBounds.height - systemHeight - (this.fullBounds.getMaxY() - this.favoritePanel.getBounds().y) - 4 - (Math.round(trashHeight) <= 0 ? 0 : trashHeight));
        } else {
            region.getBounds().setBounds(this.fullBounds.x, this.fullBounds.y + systemHeight, this.fullBounds.width, this.fullBounds.height - systemHeight - (Math.round(trashHeight) <= 0 ? 0 : trashHeight + 24));
        }
        
        systemRegion.render(matrices, mouseX, mouseY, delta);
        region.render(matrices, mouseX, mouseY, delta);
        renderAddFavorite(matrices, mouseX, mouseY, delta);
    }
    
    private void updateSystemRegion() {
        boolean updated = false;
        List<Triple<SystemFavoriteEntryProvider<?>, MutableLong, List<FavoriteEntry>>> providers = ((FavoriteEntryTypeRegistryImpl) FavoriteEntryType.registry()).getSystemProviders();
        
        for (Triple<SystemFavoriteEntryProvider<?>, MutableLong, List<FavoriteEntry>> pair : providers) {
            SystemFavoriteEntryProvider<?> provider = pair.getLeft();
            MutableLong mutableLong = pair.getMiddle();
            List<FavoriteEntry> entries = pair.getRight();
            
            if (mutableLong.getValue() == -1 || mutableLong.getValue() < System.currentTimeMillis()) {
                mutableLong.setValue(System.currentTimeMillis() + provider.updateInterval());
                List<FavoriteEntry> provide = (List<FavoriteEntry>) provider.provide();
                if (!provide.equals(entries)) {
                    entries.clear();
                    entries.addAll(provide);
                    updated = true;
                }
            }
        }
        
        if (updated) {
            lastSystemEntries = CollectionUtils.flatMap(providers, Triple::getRight);
            setSystemRegionEntries(null);
        }
    }
    
    public void setSystemRegionEntries(@Nullable RealRegionEntry<FavoriteEntry> removed) {
        systemRegion.setEntries(CollectionUtils.filterToList(lastSystemEntries, entry -> {
            if (region.has(entry)) return false;
            if (DraggingContext.getInstance().isDraggingStack()) {
                DraggableStack currentStack = DraggingContext.getInstance().getCurrentStack();
                if (currentStack instanceof RegionDraggableStack) {
                    RegionDraggableStack<?> stack = (RegionDraggableStack<?>) currentStack;
                    
                    if (removed != null && stack.getEntry() == removed) return true;
                    return stack.getEntry().region != region || !Objects.equals(stack.getEntry().getEntry(), entry);
                }
            }
            return true;
        }), EntryStacksRegionWidget.RemovalMode.DISAPPEAR);
    }
    
    private void renderAddFavorite(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.favoritePanel.render(matrices, mouseX, mouseY, delta);
        this.togglePanelButton.render(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsMouse(PointHelper.ofMouse()))
            for (Widget widget : children())
                if (widget.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        return false;
    }
    
    public void updateFavoritesBounds(@Nullable String searchTerm) {
        this.fullBounds = REIRuntime.getInstance().calculateFavoritesListArea();
    }
    
    public void updateSearch() {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            region.setEntries(CollectionUtils.map(ConfigObject.getInstance().getFavoriteEntries(), FavoriteEntry::copy), EntryStacksRegionWidget.RemovalMode.DISAPPEAR);
        } else region.setEntries(Collections.emptyList(), EntryStacksRegionWidget.RemovalMode.DISAPPEAR);
    }
    
    @Override
    public List<? extends Widget> children() {
        return children;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (systemRegion.mouseClicked(mouseX, mouseY, button) || region.mouseClicked(mouseX, mouseY, button))
            return true;
        for (Widget widget : children())
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (containsMouse(mouseX, mouseY)) {
            for (Widget widget : children())
                if (widget.mouseReleased(mouseX, mouseY, button))
                    return true;
        }
        return false;
    }
}
