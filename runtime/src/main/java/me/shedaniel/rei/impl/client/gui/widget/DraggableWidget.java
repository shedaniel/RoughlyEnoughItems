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

import com.mojang.blaze3d.platform.Window;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class DraggableWidget extends WidgetWithBounds {
    
    public boolean dragged = false;
    private Point midPoint, startPoint;
    private int relateX, relateY;
    
    public DraggableWidget(Point startingPoint) {
        initWidgets(midPoint = startingPoint);
    }
    
    public DraggableWidget() {
        this(new Point(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2, Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2));
    }
    
    protected abstract void initWidgets(Point midPoint);
    
    public abstract void updateWidgets(Point midPoint);
    
    public abstract Rectangle getGrabBounds();
    
    public abstract Rectangle getDragBounds();
    
    public final Point getMidPoint() {
        return midPoint;
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        Point mouse = PointHelper.ofMouse();
        if (int_1 == 0) {
            if (!dragged) {
                if (getGrabBounds().contains(mouse)) {
                    startPoint = new Point(midPoint.x, midPoint.y);
                    relateX = mouse.x - midPoint.x;
                    relateY = mouse.y - midPoint.y;
                    dragged = true;
                }
            } else {
                Window window = minecraft.getWindow();
                midPoint = processMidPoint(midPoint, mouse, startPoint, window, relateX, relateY);
                updateWidgets(midPoint);
            }
            return true;
        }
        for (GuiEventListener listener : children())
            if (listener.mouseDragged(double_1, double_2, int_1, double_3, double_4))
                return true;
        return false;
    }
    
    public abstract Point processMidPoint(Point midPoint, Point mouse, Point startPoint, Window window, int relateX, int relateY);
    
    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        if (int_1 == 0)
            if (dragged) {
                dragged = false;
                onMouseReleaseMidPoint(getMidPoint());
                return true;
            }
        for (GuiEventListener listener : children())
            if (listener.mouseReleased(double_1, double_2, int_1))
                return true;
        return false;
    }
    
    public void onMouseReleaseMidPoint(Point midPoint) {
    }
    
}
