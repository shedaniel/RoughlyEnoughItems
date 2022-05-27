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
import me.shedaniel.math.Rectangle;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DelegateWidget extends WidgetWithBounds {
    private static final Rectangle EMPTY = new Rectangle();
    protected final Widget widget;
    private final List<Widget> children;
    
    public DelegateWidget(Widget widget) {
        this.widget = widget;
        this.children = Collections.singletonList(widget);
    }
    
    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        widget.render(poseStack, i, j, f);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
    
    @Override
    public Rectangle getBounds() {
        return widget instanceof WidgetWithBounds ? ((WidgetWithBounds) widget).getBounds() : EMPTY;
    }
    
    @Override
    public void setZ(int z) {
        widget.setZ(z);
    }
    
    @Override
    public int getZ() {
        return widget.getZ();
    }
    
    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return widget;
    }
    
    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        if (guiEventListener == widget) {
            super.setFocused(widget);
        } else {
            widget.setFocused(guiEventListener);
        }
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return widget.containsMouse(mouseX, mouseY);
    }
    
    @Override
    public boolean isDragging() {
        return true;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return widget.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return widget.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return widget.keyReleased(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        return widget.charTyped(character, modifiers);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);
        return widget.mouseReleased(mouseX, mouseY, button);
    }
}
