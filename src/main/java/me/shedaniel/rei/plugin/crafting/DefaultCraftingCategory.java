/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.plugin.crafting;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeCategory;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.server.ContainerInfoHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class DefaultCraftingCategory implements TransferRecipeCategory<DefaultCraftingDisplay> {
    
    public static int getSlotWithSize(DefaultCraftingDisplay recipeDisplay, int num, int craftingGridWidth) {
        int x = num % recipeDisplay.getWidth();
        int y = (num - x) / recipeDisplay.getWidth();
        return craftingGridWidth * y + x;
    }
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.CRAFTING;
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Blocks.CRAFTING_TABLE);
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.crafting");
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultCraftingDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 60, startPoint.y + 18)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 95, startPoint.y + 19)));
        List<List<EntryStack>> input = display.getInputEntries();
        List<Slot> slots = Lists.newArrayList();
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                slots.add(Widgets.createSlot(new Point(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18)).markInput());
        for (int i = 0; i < input.size(); i++) {
            if (display instanceof DefaultShapedDisplay) {
                if (!input.get(i).isEmpty())
                    slots.get(getSlotWithSize(display, i, 3)).entries(input.get(i));
            } else if (!input.get(i).isEmpty())
                slots.get(i).entries(input.get(i));
        }
        widgets.addAll(slots);
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 95, startPoint.y + 19)).entries(display.getOutputEntries()).disableBackground().markOutput());
        return widgets;
    }
    
    @Override
    public void renderRedSlots(List<Widget> widgets, me.shedaniel.math.api.Rectangle bounds, DefaultCraftingDisplay display, IntList redSlots) {
        ContainerInfo<ScreenHandler> info = (ContainerInfo<ScreenHandler>) ContainerInfoHandler.getContainerInfo(getIdentifier(), ScreenHelper.getLastHandledScreen().getScreenHandler().getClass());
        if (info == null)
            return;
        RenderSystem.translatef(0, 0, 400);
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        int width = info.getCraftingWidth(ScreenHelper.getLastHandledScreen().getScreenHandler());
        for (Integer slot : redSlots) {
            int i = slot;
            int x = i % width;
            int y = MathHelper.floor(i / (float) width);
            DrawableHelper.fill(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, startPoint.x + 1 + x * 18 + 16, startPoint.y + 1 + y * 18 + 16, 0x60ff0000);
        }
        RenderSystem.translatef(0, 0, -400);
    }
}
