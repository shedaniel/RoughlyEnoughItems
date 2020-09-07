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

package me.shedaniel.rei.plugin.brewing;

import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DefaultBrewingCategory implements RecipeCategory<DefaultBrewingDisplay> {
    
    @Override
    public @NotNull ResourceLocation getIdentifier() {
        return DefaultPlugin.BREWING;
    }
    
    @Override
    public @NotNull EntryStack getLogo() {
        return EntryStack.create(Blocks.BREWING_STAND);
    }
    
    @Override
    public @NotNull String getCategoryName() {
        return I18n.get("category.rei.brewing");
    }
    
    @Override
    public @NotNull List<Widget> setupDisplay(DefaultBrewingDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 52, bounds.getCenterY() - 29);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Minecraft.getInstance().getTextureManager().bind(REIHelper.getInstance().getDefaultDisplayTexture());
            helper.blit(matrices, startPoint.x, startPoint.y, 0, 108, 103, 59);
            int width = MathHelper.ceil(System.currentTimeMillis() / 250d % 18d);
            helper.blit(matrices, startPoint.x + 44, startPoint.y + 28, 103, 163, width, 4);
        }));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 1, startPoint.y + 1)).entry(EntryStack.create(Items.BLAZE_POWDER)).disableBackground().markInput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 40, startPoint.y + 1)).entries(display.getInputEntries().get(0)).disableBackground().markInput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 63, startPoint.y + 1)).entries(display.getInputEntries().get(1)).disableBackground().markInput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 40, startPoint.y + 35)).entries(display.getOutput(0)).disableBackground().markOutput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 63, startPoint.y + 42)).entries(display.getOutput(1)).disableBackground().markOutput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 86, startPoint.y + 35)).entries(display.getOutput(2)).disableBackground().markOutput());
        return widgets;
    }
    
}
