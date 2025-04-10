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

package me.shedaniel.rei.plugin.client.categories;

import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.SmithingDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultSmithingDisplay;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public class DefaultSmithingCategory implements DisplayCategory<SmithingDisplay> {
    @Override
    public CategoryIdentifier<? extends SmithingDisplay> getCategoryIdentifier() {
        return BuiltinPlugin.SMITHING;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("category.rei.smithing");
    }
    
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Blocks.SMITHING_TABLE);
    }
    
    @Override
    public List<Widget> setupDisplay(SmithingDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 31, bounds.getCenterY() - 13);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        boolean legacy = display.getInputEntries().size() <= 2;
        int offsetX = legacy ? 0 : 5;
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 27 + offsetX, startPoint.y + 4)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61 + offsetX, startPoint.y + 5)));
        if (!legacy) {
            Slot templateSlot, baseSlot, additionSlot, resultSlot;
            MutableBoolean dirty = new MutableBoolean(true);
            widgets.add(templateSlot = Widgets.createSlot(new Point(startPoint.x + 4 - 18 * 2 + offsetX, startPoint.y + 5)).entries(display.getInputEntries().get(0)).withEntriesListener(slot -> dirty.setTrue()).markInput());
            widgets.add(baseSlot = Widgets.createSlot(new Point(startPoint.x + 4 - 18 + offsetX, startPoint.y + 5)).entries(display.getInputEntries().get(1)).withEntriesListener(slot -> dirty.setTrue()).markInput());
            widgets.add(additionSlot = Widgets.createSlot(new Point(startPoint.x + 4 + offsetX, startPoint.y + 5)).entries(display.getInputEntries().get(2)).withEntriesListener(slot -> dirty.setTrue()).markInput());
            widgets.add(resultSlot = Widgets.createSlot(new Point(startPoint.x + 61 + offsetX, startPoint.y + 5)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
            widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
                if (dirty.booleanValue()) {
                    resultSlot.clearEntries().entries(getOutput(display, BasicDisplay.registryAccess(), templateSlot.getCurrentEntry(), baseSlot.getCurrentEntry(), additionSlot.getCurrentEntry()));
                    dirty.setFalse();
                }
            }));
        } else {
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 4 - 22 + offsetX, startPoint.y + 5)).entries(display.getInputEntries().get(0)).markInput());
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 4 + offsetX, startPoint.y + 5)).entries(display.getInputEntries().get(1)).markInput());
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 61 + offsetX, startPoint.y + 5)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
        }
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 36;
    }
    
    @ApiStatus.Experimental
    private static EntryIngredient getOutput(SmithingDisplay display, RegistryAccess registryAccess, EntryStack<?> template, EntryStack<?> base, EntryStack<?> addition) {
        if (display.type() == SmithingDisplay.SmithingRecipeType.TRIM && display instanceof SmithingDisplay.Trimming trimming) {
            EntryIngredient output = DefaultSmithingDisplay.getTrimmingOutput(registryAccess, trimming.pattern(), base, addition);
            if (!output.isEmpty()) return output;
        }
        
        return display.getOutputEntries().get(0);
    }
}
