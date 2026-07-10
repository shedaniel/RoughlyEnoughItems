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

package me.shedaniel.rei.impl.client.gui.widget;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProviderWidget;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.gui.widgets.utils.PanelTextures;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.display.DisplaySpec;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class DisplayCompositeWidget extends DelegateWidgetWithBounds implements DraggableComponentProviderWidget<Object> {
    private final DisplaySpec display;
    private final List<Widget> widgets;
    
    public DisplayCompositeWidget(DisplaySpec display, List<Widget> widgets, Rectangle bounds) {
        super(Widgets.concat(widgets), () -> bounds);
        this.display = display;
        this.widgets = widgets;
    }
    
    @Override
    @Nullable
    public DraggableComponent<Object> getHovered(DraggingContext<Screen> context, double mouseX, double mouseY) {
        return StreamSupport.stream(Widgets.<DraggableComponentProviderWidget<Object>>walk(widget.children(), widget -> widget instanceof DraggableComponentProviderWidget).spliterator(), false)
                .map(widget -> widget.getHovered(context, mouseX, mouseY))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> {
                    if (containsMouse(mouseX, mouseY)) {
                        return (DraggableComponent<Object>) (DraggableComponent<?>) new DisplayDraggableComponent(
                                Widgets.concat(CollectionUtils.filterToList(widgets, w -> !(w instanceof Panel))),
                                display.provideInternalDisplay(), getBounds(), getBounds());
                    } else {
                        return null;
                    }
                });
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        
        if (ConfigObject.getInstance().isFavoritesEnabled() && containsMouse(mouse())) {
            if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(event.key(), event.scancode())) {
                FavoriteEntry favoriteEntry = FavoriteEntryType.registry().get(FavoriteEntryType.DISPLAY)
                        .fromArgs(display.provideInternalDisplay())
                        .result()
                        .orElse(null);
                if (favoriteEntry != null) {
                    ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (super.mouseReleased(event)) {
            return true;
        }
        
        if (ConfigObject.getInstance().isFavoritesEnabled() && containsMouse(event.x(), event.y())) {
            if (ConfigObject.getInstance().getFavoriteKeyCode().matchesMouse(event.button())) {
                FavoriteEntry favoriteEntry = FavoriteEntryType.registry().get(FavoriteEntryType.DISPLAY)
                        .fromArgs(display.provideInternalDisplay())
                        .result()
                        .orElse(null);
                if (favoriteEntry != null) {
                    ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static class DisplayDraggableComponent implements DraggableComponent<Display> {
        private final Widget widget;
        private final Display display;
        private final Rectangle originBounds;
        private final Rectangle bounds;
        private final Panel panel;
        private final Slot slot;
        public boolean onFavoritesRegion;
        
        public DisplayDraggableComponent(Widget widget, Display display, Rectangle originBounds, Rectangle bounds) {
            this.widget = widget;
            this.display = display;
            this.originBounds = originBounds;
            this.bounds = bounds;
            this.panel = Widgets.createRecipeBase(bounds.clone());
            this.slot = Widgets.createSlot(new Rectangle())
                    .disableBackground()
                    .disableHighlight()
                    .disableTooltips();
            for (EntryIngredient ingredient : display.getOutputEntries()) {
                slot.entries(ingredient);
            }
        }
        
        @Override
        public int getWidth() {
            if (this.onFavoritesRegion) {
                return 18;
            }
            
            return bounds.width / 2;
        }
        
        @Override
        public int getHeight() {
            if (this.onFavoritesRegion) {
                return 18;
            }
            
            return bounds.height / 2;
        }
        
        @Override
        public Display get() {
            return display;
        }
        
        @Override
        public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
            if (DraggingContext.getInstance().isDraggingComponent()) {
                FavoritesListWidget favorites = ScreenOverlayImpl.getFavoritesListWidget();
                if (favorites != null) {
                    Rectangle favoritesBounds = favorites.getRegion().getBounds();
                    if (!this.onFavoritesRegion && new Rectangle(favoritesBounds.x + 5, favoritesBounds.y + 5, favoritesBounds.width - 10, favoritesBounds.height - 10)
                            .contains(DraggingContext.getInstance().getCurrentPosition())) {
                        this.onFavoritesRegion = true;
                    } else if (this.onFavoritesRegion && !favoritesBounds.contains(DraggingContext.getInstance().getCurrentPosition())) {
                        this.onFavoritesRegion = false;
                    }
                } else {
                    this.onFavoritesRegion = false;
                }
            } else {
                this.onFavoritesRegion = false;
            }
            
            graphics.pose().pushMatrix();
            if (bounds.width <= Math.max(18, this.bounds.width / 2 - 6) && bounds.height <= Math.max(18, this.bounds.height / 2 - 6) && this.onFavoritesRegion) {
                this.panel.texture(PanelTextures.LIGHTER);
                this.panel.getBounds().setBounds(bounds);
                this.panel.render(graphics, mouseX, mouseY, delta);
                graphics.pose().pushMatrix();
                graphics.pose().translate(0, 0.5f);
                this.slot.getBounds().setBounds(bounds.getCenterX() - 7, bounds.getCenterY() - 7, 14, 14);
                this.slot.render(graphics, mouseX, mouseY, delta);
                graphics.pose().popMatrix();
            } else {
                this.panel.texture(ConfigObject.getInstance().getRecipeBorderType());
                graphics.pose().pushMatrix();
                graphics.pose().translate(bounds.getX(), bounds.getY());
                graphics.pose().scale(bounds.width / (float) this.bounds.getWidth(), bounds.height / (float) this.bounds.getHeight());
                graphics.pose().translate(-this.bounds.getX(), -this.bounds.getY());
                this.panel.getBounds().setBounds(this.bounds);
                this.panel.render(graphics, mouseX, mouseY, delta);
                graphics.pose().popMatrix();
                graphics.pose().translate(bounds.getX(), bounds.getY());
                graphics.pose().scale(bounds.width / (float) this.bounds.getWidth(), bounds.height / (float) this.bounds.getHeight());
                graphics.pose().translate(-this.bounds.getX(), -this.bounds.getY());
                widget.render(graphics, -1000, -1000, delta);
            }
            graphics.pose().popMatrix();
        }
        
        @Override
        public void release(DraggedAcceptorResult result) {
            if (result == DraggedAcceptorResult.PASS) {
                DraggingContext.getInstance().renderBack(this, DraggingContext.getInstance().getCurrentBounds(), () -> originBounds);
            }
        }
        
        @Override
        public Rectangle getOriginBounds(Point mouse) {
            return originBounds.clone();
        }
    }
}
