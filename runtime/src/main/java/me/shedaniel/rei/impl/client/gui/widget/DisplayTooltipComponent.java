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
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public class DisplayTooltipComponent implements TooltipComponent, ClientTooltipComponent {
    private final Widget widget;
    private final DisplaySpec display;
    private final Rectangle bounds;
    
    public DisplayTooltipComponent(DisplaySpec display) {
        Display internalDisplay = display.provideInternalDisplay();
        CategoryRegistry.CategoryConfiguration<Display> configuration = CategoryRegistry.getInstance().get((CategoryIdentifier<Display>) internalDisplay.getCategoryIdentifier());
        DisplayCategory<Display> category = configuration.getCategory();
        this.bounds = new Rectangle(0, 0, category.getDisplayWidth(internalDisplay), category.getDisplayHeight());
        List<Widget> widgets = configuration.getView(internalDisplay).setupDisplay(internalDisplay, bounds);
        
        this.display = display;
        this.widget = Widgets.concat(widgets);
    }
    
    public DisplayTooltipComponent(DisplaySpec display, List<Widget> widgets, Rectangle bounds) {
        this.widget = Widgets.concat(widgets);
        this.display = display;
        this.bounds = bounds;
    }
    
    @Override
    public int getHeight() {
        return bounds.height + 4;
    }
    
    @Override
    public int getWidth(Font font) {
        return bounds.width + 4;
    }
    
    @Override
    public void renderImage(Font font, int x, int y, PoseStack matrices, ItemRenderer itemRenderer, int z) {
        matrices.pushPose();
        matrices.translate(x + 2, y + 2, z);
        matrices.translate(-this.bounds.getX(), -this.bounds.getY(), 0);
        widget.render(matrices, -1000, -1000, 0);
        matrices.popPose();
    }
}
