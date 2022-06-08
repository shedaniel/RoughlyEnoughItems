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

package me.shedaniel.rei.impl.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProviderWidget;
import me.shedaniel.rei.api.client.gui.widgets.DelegateWidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class DisplayCompositeWidget extends DelegateWidgetWithBounds implements DraggableComponentProviderWidget<Object> {
    private final DisplaySpec display;
    
    public DisplayCompositeWidget(DisplaySpec display, List<Widget> widgets, Rectangle bounds) {
        super(Widgets.concat(widgets), () -> bounds);
        this.display = display;
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
                        return (DraggableComponent<Object>) (DraggableComponent<?>) new DisplayDraggableComponent(widget, display.provideInternalDisplay(), getBounds(), getBounds());
                    } else {
                        return null;
                    }
                });
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        if (ConfigObject.getInstance().isFavoritesEnabled() && containsMouse(mouse())) {
            if (ConfigObject.getInstance().getFavoriteKeyCode().matchesKey(keyCode, scanCode)) {
                FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
                
                if (favoritesListWidget != null) {
                    favoritesListWidget.displayHistory.addDisplay(getBounds().clone(), display.provideInternalDisplay());
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        
        if (ConfigObject.getInstance().isFavoritesEnabled() && containsMouse(mouseX, mouseY)) {
            if (ConfigObject.getInstance().getFavoriteKeyCode().matchesMouse(button)) {
                FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
                
                if (favoritesListWidget != null) {
                    favoritesListWidget.displayHistory.addDisplay(getBounds().clone(), display.provideInternalDisplay());
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
        
        public DisplayDraggableComponent(Widget widget, Display display, Rectangle originBounds, Rectangle bounds) {
            this.widget = widget;
            this.display = display;
            this.originBounds = originBounds;
            this.bounds = bounds;
        }
        
        @Override
        public int getWidth() {
            return bounds.width;
        }
        
        @Override
        public int getHeight() {
            return bounds.height;
        }
        
        @Override
        public Display get() {
            return display;
        }
        
        @Override
        public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            matrices.pushPose();
            matrices.translate(bounds.getX(), bounds.getY(), 0);
            matrices.scale(bounds.width / (float) this.bounds.getWidth(), bounds.height / (float) this.bounds.getHeight(), 1);
            matrices.translate(-this.bounds.getX(), -this.bounds.getY(), 0);
            widget.render(matrices, -1000, -1000, delta);
            matrices.popPose();
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
