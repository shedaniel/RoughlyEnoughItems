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

package me.shedaniel.rei.plugin.client.categories.crafting;

import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplayMerger;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultCraftingCategory implements DisplayCategory<DefaultCraftingDisplay<?>> {
    @Override
    public CategoryIdentifier<? extends DefaultCraftingDisplay<?>> getCategoryIdentifier() {
        return BuiltinPlugin.CRAFTING;
    }
    
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Blocks.CRAFTING_TABLE);
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("category.rei.crafting");
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultCraftingDisplay<?> display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 60, startPoint.y + 18)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 95, startPoint.y + 19)));
        List<InputIngredient<EntryStack<?>>> input = display.getInputIngredients(3, 3);
        List<Slot> slots = Lists.newArrayList();
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                slots.add(Widgets.createSlot(new Point(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18)).markInput());
        for (InputIngredient<EntryStack<?>> ingredient : input) {
            slots.get(ingredient.getIndex()).entries(ingredient.get());
        }
        widgets.addAll(slots);
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 95, startPoint.y + 19)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
        if (display.isShapeless()) {
            widgets.add(Widgets.createShapelessIcon(bounds));
        }
        return widgets;
    }
    
    @Override
    @Nullable
    public DisplayMerger<DefaultCraftingDisplay<?>> getDisplayMerger() {
        return DisplayCategory.getContentMerger();
    }
}
