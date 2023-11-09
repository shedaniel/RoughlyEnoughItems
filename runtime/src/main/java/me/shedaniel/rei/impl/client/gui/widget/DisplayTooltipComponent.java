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

import com.google.common.base.Suppliers;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.util.MatrixUtils;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DisplayTooltipComponent implements TooltipComponent, ClientTooltipComponent {
    private final List<Widget> widgets;
    private final Widget widget;
    private final DisplaySpec display;
    private final Rectangle bounds;
    private final Supplier<AutoCraftingEvaluator.AutoCraftingResult> autoCraftingResult =
            Suppliers.memoizeWithExpiration(this::evaluateAutoCrafting, 1000, TimeUnit.MILLISECONDS);
    
    public DisplayTooltipComponent(DisplaySpec display) {
        Display internalDisplay = display.provideInternalDisplay();
        CategoryRegistry.CategoryConfiguration<Display> configuration = CategoryRegistry.getInstance().get((CategoryIdentifier<Display>) internalDisplay.getCategoryIdentifier());
        DisplayCategory<Display> category = configuration.getCategory();
        this.bounds = new Rectangle(0, 0, category.getDisplayWidth(internalDisplay), category.getDisplayHeight());
        List<Widget> widgets = configuration.getView(internalDisplay).setupDisplay(internalDisplay, bounds);
        
        this.display = display;
        this.widgets = widgets;
        this.widget = Widgets.concat(widgets);
    }
    
    public DisplayTooltipComponent(DisplaySpec display, List<Widget> widgets, Rectangle bounds) {
        this.widgets = widgets;
        this.widget = Widgets.concat(widgets);
        this.display = display;
        this.bounds = bounds;
    }
    
    private AutoCraftingEvaluator.AutoCraftingResult evaluateAutoCrafting() {
        if (this.display == null) return new AutoCraftingEvaluator.AutoCraftingResult();
        return AutoCraftingEvaluator.evaluateAutoCrafting(false, false, this.display.provideInternalDisplay(), this.display::provideInternalDisplayIds);
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
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(x + 2, y + 2, 0);
        graphics.pose().translate(-this.bounds.getX(), -this.bounds.getY(), 0);
        widget.render(graphics, -1000, -1000, 0);
        
        AutoCraftingEvaluator.AutoCraftingResult craftingResult = autoCraftingResult.get();
        if (craftingResult.hasApplicable && craftingResult.renderer != null) {
            graphics.pose().pushPose();
            Rectangle transformedBounds = MatrixUtils.transform(MatrixUtils.inverse(graphics.pose().last().pose()), new Rectangle(x + 2, y + 2, bounds.width, bounds.height));
            Point mouse = MatrixUtils.transform(graphics.pose().last().pose(), PointHelper.ofMouse());
            craftingResult.renderer.render(graphics, mouse.x, mouse.y, Minecraft.getInstance().getDeltaFrameTime(),
                    widgets, transformedBounds, display.provideInternalDisplay());
            graphics.pose().popPose();
        }
        
        graphics.pose().popPose();
    }
}
