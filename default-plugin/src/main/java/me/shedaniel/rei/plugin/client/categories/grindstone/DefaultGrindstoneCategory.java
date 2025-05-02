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

package me.shedaniel.rei.plugin.client.categories.grindstone;

import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.grindstone.DefaultGrindstoneDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import java.text.DecimalFormat;
import java.util.List;

public class DefaultGrindstoneCategory implements DisplayCategory<DefaultGrindstoneDisplay> {
    @Override
    public CategoryIdentifier<? extends DefaultGrindstoneDisplay> getCategoryIdentifier() {
        return BuiltinPlugin.GRINDSTONE;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("block.minecraft.grindstone");
    }
    
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Blocks.GRINDSTONE);
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultGrindstoneDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 31, bounds.getCenterY() - 20);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x - 6, startPoint.y + 12)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 30, startPoint.y + 13)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x - 30, startPoint.y + 2)).entries(display.getInputEntries().get(0)).markInput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x - 30, startPoint.y + 23)).entries(display.getInputEntries().get(1)).markInput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 30, startPoint.y + 13)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
        if (display.getAverageXpReward().isPresent()) {
            widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
                Font font = Minecraft.getInstance().font;
                DecimalFormat format = new DecimalFormat("0.#");
                Component componentXp = Component.translatable("category.rei.grindstone.xp", format.format(display.getAverageXpReward().getAsDouble()));
                Component componentAverage = Component.translatable("category.rei.grindstone.average");
                int textboxLeftPos = startPoint.x + 102 - font.width(componentXp) - 2;
                int textboxTopPos = startPoint.y + 14 - font.lineHeight / 2;
                int xpLeftPos = textboxLeftPos - 2;
                int xpTopPos = textboxTopPos;
                int xpRightPos = startPoint.x + 102;
                int xpBottomPos = textboxTopPos + 12;
                graphics.drawString(font, componentXp, textboxLeftPos, textboxTopPos + 2, 0x80ff20);
                textboxLeftPos += font.width(componentXp) / 2 - font.width(componentAverage) / 2;
                textboxTopPos += font.lineHeight + 3;
                graphics.fill(
                        Math.min(xpLeftPos, textboxLeftPos - 2),
                        xpTopPos - 2,
                        Math.max(xpRightPos, + font.width(componentAverage)),
                        textboxTopPos + 12,
                        0x4f000000);
                graphics.drawString(font, componentAverage, textboxLeftPos, textboxTopPos + 2, 0x80ff20);
            }));
        }
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 47;
    }
}
