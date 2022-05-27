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
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import me.shedaniel.rei.api.client.gui.widgets.DelegateWidget;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;

import java.util.function.Supplier;

public class DelegateWidgetWithTranslate extends DelegateWidget {
    private final Supplier<Matrix4f> translate;
    
    public DelegateWidgetWithTranslate(WidgetWithBounds widget, Supplier<Matrix4f> translate) {
        super(widget);
        this.translate = translate;
    }
    
    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        poseStack.pushPose();
        poseStack.last().pose().multiply(translate.get());
        Vector4f mouse = transformMouse(i, j);
        super.render(poseStack, (int) mouse.x(), (int) mouse.y(), f);
        poseStack.popPose();
    }
    
    private Vector4f transformMouse(double mouseX, double mouseY) {
        Vector4f mouse = new Vector4f((float) mouseX, (float) mouseY, 0, 1);
        mouse.transform(translate.get());
        return mouse;
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        Vector4f mouse = transformMouse(mouseX, mouseY);
        return super.containsMouse(mouse.x(), mouse.y());
    }
    
    @Override
    public boolean mouseClicked(double d, double e, int i) {
        Vector4f mouse = transformMouse(d, e);
        return super.mouseClicked(mouse.x(), mouse.y(), i);
    }
    
    @Override
    public boolean mouseReleased(double d, double e, int i) {
        Vector4f mouse = transformMouse(d, e);
        return super.mouseReleased(mouse.x(), mouse.y(), i);
    }
    
    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        Vector4f mouse = transformMouse(d, e);
        return super.mouseDragged(mouse.x(), mouse.y(), i, f, g);
    }
    
    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        Vector4f mouse = transformMouse(d, e);
        return super.mouseScrolled(mouse.x(), mouse.y(), f);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        try {
            Widget.translateMouse(translate.get());
            return super.keyPressed(keyCode, scanCode, modifiers);
        } finally {
            Widget.popMouse();
        }
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        try {
            Widget.translateMouse(translate.get());
            return super.keyReleased(keyCode, scanCode, modifiers);
        } finally {
            Widget.popMouse();
        }
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        try {
            Widget.translateMouse(translate.get());
            return super.charTyped(character, modifiers);
        } finally {
            Widget.popMouse();
        }
    }
}