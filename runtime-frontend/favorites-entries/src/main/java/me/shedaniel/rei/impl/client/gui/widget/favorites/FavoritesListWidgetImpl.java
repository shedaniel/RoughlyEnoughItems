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
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProviderWidget;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitorWidget;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerInternal;
import me.shedaniel.rei.impl.client.gui.overlay.entries.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.overlay.widgets.ScaleIndicatorWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.history.DisplayHistoryManager;
import me.shedaniel.rei.impl.client.gui.widget.favorites.history.DisplayHistoryWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.listeners.FavoritesRegionListener;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.FavoritesPanel;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.FavoritesTogglePanelButton;
import me.shedaniel.rei.impl.client.gui.widget.favorites.trash.TrashWidget;
import me.shedaniel.rei.impl.client.gui.widget.region.EntryStacksRegionWidget;
import me.shedaniel.rei.impl.common.util.RectangleUtils;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@ApiStatus.Internal
public class FavoritesListWidgetImpl extends WidgetWithBounds implements FavoritesListWidget, DraggableComponentProviderWidget<Object>, DraggableComponentVisitorWidget, OverlayListWidget {
    public Rectangle fullBounds;
    public Rectangle excludedBounds;
    public Rectangle favoritesBounds;
    private final EntryStacksRegionWidget<FavoriteEntry> region = new EntryStacksRegionWidget<>(new FavoritesRegionListener(this));
    
    public final FavoritesPanel favoritePanel = new FavoritesPanel(this, region);
    public final TrashWidget trash = new TrashWidget(this);
    public final DisplayHistoryWidget displayHistory = new DisplayHistoryWidget(this);
    public final FavoritesTogglePanelButton togglePanelButton = new FavoritesTogglePanelButton(this);
    private final List<Widget> children = ImmutableList.of(favoritePanel, togglePanelButton, region);
    private final ScaleIndicatorWidget scaleIndicator = new ScaleIndicatorWidget();
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (fullBounds.contains(mouseX, mouseY)) {
            if (Screen.hasControlDown()) {
                ConfigManagerInternal manager = ConfigManagerInternal.getInstance();
                manager.set("advanced.accessibility.entrySize", manager.getConfig().getEntrySize() + amount * 0.075);
                scaleIndicator.set();
            } else if (favoritePanel.mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            } else if (displayHistory.mouseScrolled(mouseX, mouseY, amount)) {
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
    
    @Override
    @Nullable
    public DraggableComponent<Object> getHovered(DraggingContext<Screen> context, double mouseX, double mouseY) {
        DraggableComponent<?> stack = region.getHoveredStack(context, mouseX, mouseY);
        if (stack != null) return (DraggableComponent<Object>) stack;
        if (favoritePanel.containsMouse(mouseX, mouseY)) {
            stack = favoritePanel.getHoveredStack(mouseX, mouseY);
            if (stack != null) return (DraggableComponent<Object>) stack;
        }
        stack = displayHistory.getHovered(context, mouseX, mouseY);
        if (stack != null) return (DraggableComponent<Object>) stack;
        
        return null;
    }
    
    @Override
    public DraggedAcceptorResult acceptDragged(DraggingContext<Screen> context, DraggableComponent<?> stack) {
        if (favoritePanel.containsMouse(context.getCurrentPosition()) || trash.containsMouse(context.getCurrentPosition())) {
            context.renderToVoid(stack);
            return DraggedAcceptorResult.CONSUMED;
        }
        return Stream.of(region, displayHistory)
                .map(visitor -> visitor.acceptDragged(context, stack))
                .filter(result -> result != DraggedAcceptorResult.PASS)
                .findFirst()
                .orElse(DraggedAcceptorResult.PASS);
    }
    
    @Override
    public EntryStack<?> getFocusedStack() {
        Point mouse = mouse();
        EntryStack<?> stack = region.getFocusedStack();
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
        
        boolean draggingDisplay = DraggingContext.getInstance().isDraggingComponent()
                                  && DraggingContext.getInstance().getDragged().get() instanceof Display;
        int topOffsetHeight = 0;
        this.favoritesBounds = DisplayHistoryManager.INSTANCE.getEntries(displayHistory).isEmpty() && !draggingDisplay
                ? fullBounds : RectangleUtils.excludeZones(this.fullBounds, Stream.of(displayHistory.createBounds(this.excludedBounds)));
        
        displayHistory.render(matrices, mouseX, mouseY, delta);
        
        if (favoritePanel.getBounds().height > 20) {
            // Opened favorites panel
            region.getBounds().setBounds(this.favoritesBounds.x, this.favoritesBounds.y + topOffsetHeight, this.favoritesBounds.width, this.favoritesBounds.height - topOffsetHeight - (this.favoritesBounds.getMaxY() - this.favoritePanel.getBounds().y) - 4 - (Math.round(trashHeight) <= 0 ? 0 : trashHeight));
        } else {
            region.getBounds().setBounds(this.favoritesBounds.x, this.favoritesBounds.y + topOffsetHeight, this.favoritesBounds.width, this.favoritesBounds.height - topOffsetHeight - (Math.round(trashHeight) <= 0 ? 0 : trashHeight + 24));
        }
        
        region.render(matrices, mouseX, mouseY, delta);
        renderAddFavorite(matrices, mouseX, mouseY, delta);
        
        this.scaleIndicator.setCenter(favoritesBounds.getCenterX(), favoritesBounds.getCenterY());
        this.scaleIndicator.render(matrices, mouseX, mouseY, delta);
    }
    
    private void renderAddFavorite(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.favoritePanel.render(matrices, mouseX, mouseY, delta);
        this.togglePanelButton.render(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsMouse(mouse()))
            for (Widget widget : children())
                if (widget.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        if (displayHistory.keyPressed(keyCode, scanCode, modifiers))
            return true;
        return false;
    }
    
    @Override
    public void initBounds() {
        this.fullBounds = REIRuntime.getInstance().calculateFavoritesListArea();
        this.excludedBounds = RectangleUtils.excludeZones(this.fullBounds, ScreenRegistry.getInstance().exclusionZones().getExclusionZones(minecraft.screen).stream());
        this.favoritesBounds = RectangleUtils.excludeZones(this.fullBounds, Stream.of(displayHistory.createBounds(this.fullBounds, null)));
        this.updateSearch();
    }
    
    @Override
    public void queueReloadSearch() {
        updateSearch();
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
        if (region.mouseClicked(mouseX, mouseY, button))
            return true;
        for (Widget widget : children())
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        if (displayHistory.mouseClicked(mouseX, mouseY, button))
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
        if (displayHistory.mouseReleased(mouseX, mouseY, button))
            return true;
        return false;
    }
    
    @Override
    public Widget asWidget() {
        return this;
    }
    
    @Override
    public Rectangle getFavoritesBounds() {
        return favoritesBounds;
    }
    
    @Override
    public void submitDisplayHistory(Display display, @Nullable Rectangle fromBounds) {
        displayHistory.addDisplay(fromBounds, display);
    }
}
