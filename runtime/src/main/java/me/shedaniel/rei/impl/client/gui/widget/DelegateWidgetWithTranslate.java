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

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.DelegateWidget;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.joml.Matrix3x2f;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class DelegateWidgetWithTranslate extends DelegateWidget {
    private final Supplier<Matrix3x2f> translate;
    
    public DelegateWidgetWithTranslate(WidgetWithBounds widget, Supplier<Matrix3x2f> translate) {
        super(widget);
        this.translate = translate;
    }
    
    protected Matrix3x2f translate() {
        return translate.get();
    }
    
    protected final Matrix3x2f inverseTranslate() {
        return MatrixUtils.inverse(translate());
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.pose().pushMatrix();
        graphics.pose().mul(translate());
        Vector3f mouse = transformMouse(mouseX, mouseY);
        super.render(graphics, (int) mouse.x(), (int) mouse.y(), delta);
        graphics.pose().popMatrix();
    }
    
    private Vector3f transformMouse(double mouseX, double mouseY) {
        Vector3f mouse = new Vector3f((float) mouseX, (float) mouseY, 1);
        inverseTranslate().transform(mouse);
        return mouse;
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        Vector3f mouse = transformMouse(mouseX, mouseY);
        return super.containsMouse(mouse.x(), mouse.y());
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Vector3f mouse = transformMouse(event.x(), event.y());
        return super.mouseClicked(new MouseButtonEvent(mouse.x(), mouse.y(), event.buttonInfo()), doubleClick);
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        Vector3f mouse = transformMouse(event.x(), event.y());
        return super.mouseReleased(new MouseButtonEvent(mouse.x(), mouse.y(), event.buttonInfo()));
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        Vector3f mouse = transformMouse(event.x(), event.y());
        return super.mouseDragged(new MouseButtonEvent(mouse.x(), mouse.y(), event.buttonInfo()), deltaX, deltaY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        Vector3f mouse = transformMouse(mouseX, mouseY);
        return super.mouseScrolled(mouse.x(), mouse.y(), amountX, amountY);
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        try {
            Widget.translateMouse(inverseTranslate());
            return super.keyPressed(event);
        } finally {
            Widget.popMouse();
        }
    }
    
    @Override
    public boolean keyReleased(KeyEvent event) {
        try {
            Widget.translateMouse(inverseTranslate());
            return super.keyReleased(event);
        } finally {
            Widget.popMouse();
        }
    }
    
    @Override
    public boolean charTyped(CharacterEvent event) {
        try {
            Widget.translateMouse(inverseTranslate());
            return super.charTyped(event);
        } finally {
            Widget.popMouse();
        }
    }
    
    @Override
    public Rectangle getBounds() {
        return MatrixUtils.transform(translate(), super.getBounds());
    }
}
