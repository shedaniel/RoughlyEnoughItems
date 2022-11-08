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
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProviderWidget;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitorWidget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.favorites.element.FavoritesListElement;
import me.shedaniel.rei.impl.client.gui.widget.favorites.history.DisplayHistoryManager;
import me.shedaniel.rei.impl.client.gui.widget.favorites.history.DisplayHistoryWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.FavoritesPanel;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.FavoritesTogglePanelButton;
import me.shedaniel.rei.impl.client.gui.widget.favorites.region.FavoritesRegionContainerWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.trash.TrashWidget;
import me.shedaniel.rei.impl.common.util.RectangleUtils;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
@ApiStatus.Internal
public class FavoritesListWidget extends WidgetWithBounds implements DraggableComponentProviderWidget<Object>, DraggableComponentVisitorWidget, OverlayListWidget {
    public Rectangle fullBounds;
    public Rectangle excludedBounds;
    public Rectangle favoritesBounds;
    private final FavoritesRegionContainerWidget regions = new FavoritesRegionContainerWidget();
    public final FavoritesPanel favoritePanel = new FavoritesPanel(this);
    public final TrashWidget trash = new TrashWidget(this);
    public final DisplayHistoryWidget displayHistory = new DisplayHistoryWidget(this);
    private final List<FavoritesListElement> elements = ImmutableList.of(
            displayHistory,
            regions,
            trash,
            favoritePanel,
            new FavoritesTogglePanelButton(this)
    );
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (fullBounds.contains(mouseX, mouseY)) {
            if (Screen.hasControlDown()) {
                ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
                ScreenOverlayImpl.getEntryListWidget().scaleIndicator.setAs(10.0D);
                if (config.setEntrySize(config.getEntrySize() + amount * 0.075)) {
                    ConfigManager.getInstance().saveConfig();
                    REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
                    return true;
                }
            } else {
                for (FavoritesListElement element : elements) {
                    if (element.mouseScrolled(mouseX, mouseY, amount)) {
                        return true;
                    }
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public Rectangle getBounds() {
        return fullBounds;
    }
    
    public void updateEntriesPosition() {
        this.regions.updateEntriesPosition();
    }
    
    @Override
    @Nullable
    public DraggableComponent<Object> getHovered(DraggingContext<Screen> context, double mouseX, double mouseY) {
        for (FavoritesListElement child : children()) {
            DraggableComponent<Object> hovered = child.getHovered(context, mouseX, mouseY);
            if (hovered != null) {
                return hovered;
            }
        }
        
        return null;
    }
    
    @Override
    public DraggedAcceptorResult acceptDragged(DraggingContext<Screen> context, DraggableComponent<?> stack) {
        return elements.stream()
                .map(visitor -> visitor.acceptDragged(context, stack))
                .filter(result -> result != DraggedAcceptorResult.PASS)
                .findFirst()
                .orElse(DraggedAcceptorResult.PASS);
    }
    
    @Override
    public EntryStack<?> getFocusedStack() {
        Point mouse = mouse();
        for (FavoritesListElement child : children()) {
            EntryStack<?> stack = child.getFocusedStack(mouse);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        
        return EntryStack.empty();
    }
    
    @Override
    public Stream<EntryStack<?>> getEntries() {
        return regions.getEntries();
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (fullBounds.isEmpty())
            return;
        
        double trashHeight = this.trash.getHeight();
        
        boolean draggingDisplay = DraggingContext.getInstance().isDraggingComponent()
                                  && DraggingContext.getInstance().getDragged().get() instanceof Display;
        int topOffsetHeight = 0;
        this.favoritesBounds = DisplayHistoryManager.INSTANCE.getEntries(displayHistory).isEmpty() && !draggingDisplay
                ? fullBounds : RectangleUtils.excludeZones(this.fullBounds, Stream.of(displayHistory.createBounds(this.excludedBounds)));
        
        if (favoritePanel.getBounds().height > 20) {
            // Opened favorites panel
            this.regions.setBounds(new Rectangle(this.favoritesBounds.x, this.favoritesBounds.y + topOffsetHeight, this.favoritesBounds.width, this.favoritesBounds.height - topOffsetHeight - (this.favoritesBounds.getMaxY() - this.favoritePanel.getBounds().y) - 4 - (Math.round(trashHeight) <= 0 ? 0 : trashHeight)));
        } else {
            this.regions.setBounds(new Rectangle(this.favoritesBounds.x, this.favoritesBounds.y + topOffsetHeight, this.favoritesBounds.width, this.favoritesBounds.height - topOffsetHeight - (Math.round(trashHeight) <= 0 ? 0 : trashHeight + 24)));
        }
        
        for (FavoritesListElement child : children()) {
            child.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsMouse(mouse())) {
            for (GuiEventListener widget : children()) {
                if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void updateFavoritesBounds(@Nullable String searchTerm) {
        this.fullBounds = REIRuntime.getInstance().calculateFavoritesListArea();
        this.excludedBounds = RectangleUtils.excludeZones(this.fullBounds, ScreenRegistry.getInstance().exclusionZones().getExclusionZones(minecraft.screen).stream());
        this.favoritesBounds = RectangleUtils.excludeZones(this.fullBounds, Stream.of(displayHistory.createBounds(this.fullBounds, null)));
    }
    
    public void updateSearch() {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            this.regions.setEntries(CollectionUtils.map(FavoritesEntriesManager.INSTANCE.getFavorites(), FavoriteEntry::copy));
        } else {
            this.regions.setEntries(List.of());
        }
    }
    
    @Override
    public List<? extends FavoritesListElement> children() {
        return elements;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener widget : children()) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiEventListener widget : children()) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    
    public Collection<Rectangle> getExclusionZones() {
        return this.children().stream()
                .flatMap(FavoritesListElement::getExclusionZones)
                .collect(Collectors.toList());
    }
}
