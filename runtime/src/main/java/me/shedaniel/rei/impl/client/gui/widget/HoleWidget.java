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
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class HoleWidget {
    public static Widget create(Rectangle bounds) {
        return Widgets.withBounds(
                Widgets.concat(
                        createMenuBackground(bounds),
                        createListBorders(bounds)
                ),
                bounds
        );
    }
    
    public static Widget createMenuBackground(Rectangle bounds) {
        return Widgets.withBounds(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("textures/gui/menu_list_background.png"), bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), bounds.width, bounds.height, 32, 32);
        }), bounds);
    }
    
    public static Widget createListBorders(Rectangle bounds) {
        return Widgets.withBounds(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            graphics.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.HEADER_SEPARATOR, bounds.x, bounds.y - 2, 0.0F, 0.0F, bounds.width, 2, 32, 2);
            graphics.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.FOOTER_SEPARATOR, bounds.x, bounds.getMaxY(), 0.0F, 0.0F, bounds.width, 2, 32, 2);
        }), bounds);
    }
}
