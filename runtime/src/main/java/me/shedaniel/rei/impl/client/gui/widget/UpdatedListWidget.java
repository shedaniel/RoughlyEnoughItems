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

import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;
import net.minecraft.client.Minecraft;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

public class UpdatedListWidget<E extends UpdatedListWidget.Entry<E>> extends DynamicElementListWidget<E> {
    public UpdatedListWidget(Minecraft client, int width, int height, int top, int bottom) {
        super(client, width, height, top, bottom, null);
        this.setRenderSelection(false);
    }
    
    @SuppressWarnings("rawtypes")
    public static void renderAs(Minecraft minecraft, int width, int height, int top, int bottom, GuiGraphics graphics, float delta) {
        new UpdatedListWidget(minecraft, width, height, top, bottom).render(graphics, -100, -100, delta);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        extractRenderState(graphics, mouseX, mouseY, delta);
    }
    
    public static abstract class Entry<E extends Entry<E>> extends DynamicElementListWidget.ElementEntry<E> {
        public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            render(GuiGraphics.of(graphics), index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }
    }
    
    public static abstract class ElementEntry<E extends ElementEntry<E>> extends Entry<E> {
    }
}
