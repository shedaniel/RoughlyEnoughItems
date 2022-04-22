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
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class RendererWrappedWidget extends WidgetWithBounds {
    private final Renderer renderer;
    private final Supplier<Rectangle> bounds;
    
    public RendererWrappedWidget(Renderer renderer, Supplier<Rectangle> bounds) {
        this.renderer = Objects.requireNonNull(renderer);
        this.bounds = Objects.requireNonNull(bounds);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderer.render(matrices, getBounds(), mouseX, mouseY, delta);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        if (renderer instanceof GuiEventListener listener)
            return Collections.singletonList(listener);
        return Collections.emptyList();
    }
    
    @Override
    public void setZ(int z) {
        renderer.setZ(z);
    }
    
    @Override
    public int getZ() {
        return renderer.getZ();
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds.get();
    }
}