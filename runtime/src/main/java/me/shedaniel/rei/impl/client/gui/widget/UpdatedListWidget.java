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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class UpdatedListWidget<E extends UpdatedListWidget.Entry<E>> extends DynamicElementListWidget<E> {
    public UpdatedListWidget(Minecraft client, int width, int height, int top, int bottom) {
        super(client, width, height, top, bottom, InternalTextures.LEGACY_DIRT);
        this.setRenderSelection(false);
    }
    
    @Override
    protected void renderHoleBackground(GuiGraphics graphics, int y1, int y2, int alpha1, int alpha2) {
    }
    
    @Override
    protected void renderBackBackground(GuiGraphics graphics, BufferBuilder buffer, Tesselator tessellator) {
        RenderSystem.enableBlend();
        graphics.blit(new ResourceLocation("textures/gui/menu_list_background.png"), this.left, this.top, this.right, this.bottom, this.width, this.bottom - this.top, 32, 32);
        RenderSystem.disableBlend();
    }
    
    @SuppressWarnings("rawtypes")
    public static void renderAs(Minecraft minecraft, int width, int height, int top, int bottom, GuiGraphics graphics, float delta) {
        new UpdatedListWidget(minecraft, width, height, top, bottom).render(graphics, -100, -100, delta);
    }
    
    public static abstract class Entry<E extends Entry<E>> extends DynamicElementListWidget.ElementEntry<E> {
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }
    }
    
    public static abstract class ElementEntry<E extends ElementEntry<E>> extends Entry<E> {
    }
}
