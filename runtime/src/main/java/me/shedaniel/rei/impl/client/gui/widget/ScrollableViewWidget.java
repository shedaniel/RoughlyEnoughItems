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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScrollableViewWidget {
    public static WidgetWithBounds create(Rectangle bounds, WidgetWithBounds inner, boolean background) {
        return create(bounds, inner, new ScrollingContainer[1], background);
    }
    
    public static WidgetWithBounds create(Rectangle bounds, WidgetWithBounds inner, ScrollingContainer[] scrollingRef, boolean background) {
        scrollingRef[0] = new ScrollingContainer() {
            @Override
            public Rectangle getBounds() {
                return bounds;
            }
            
            @Override
            public int getMaxScrollHeight() {
                return inner.getBounds().getHeight();
            }
        };
        
        List<Widget> widgets = new ArrayList<>();
        
        if (background) {
            widgets.add(HoleWidget.create(bounds, scrollingRef[0]::scrollAmountInt, 32));
        }
        
        widgets.add(Widgets.scissored(scrollingRef[0].getScissorBounds(), Widgets.withTranslate(inner,
                () -> Matrix4f.createTranslateMatrix(0, -scrollingRef[0].scrollAmountInt(), 0))));
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            scrollingRef[0].updatePosition(delta);
            scrollingRef[0].renderScrollBar();
        }));
        widgets.add(createScrollerWidget(bounds, scrollingRef[0]));
        
        return Widgets.concatWithBounds(bounds, widgets);
    }
    
    private static Widget createScrollerWidget(Rectangle bounds, ScrollingContainer scrolling) {
        return new Widget() {
            @Override
            public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
            
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (bounds.contains(mouseX, mouseY) && scrolling.updateDraggingState(mouseX, mouseY, button)) {
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
            
            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (scrolling.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
                    return true;
                return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
            
            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
                if (bounds.contains(mouseX, mouseY)) {
                    scrolling.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
                    return true;
                }
                
                return super.mouseScrolled(mouseX, mouseY, amount);
            }
        };
    }
}
