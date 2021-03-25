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

package me.shedaniel.rei.plugin.client.categories.crafting;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.TransferDisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultCraftingCategory implements TransferDisplayCategory<DefaultCraftingDisplay> {
    @Override
    public CategoryIdentifier<? extends DefaultCraftingDisplay> getCategoryIdentifier() {
        return BuiltinPlugin.CRAFTING;
    }
    
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Blocks.CRAFTING_TABLE);
    }
    
    @Override
    public Component getTitle() {
        return new TranslatableComponent("category.rei.crafting");
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultCraftingDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 60, startPoint.y + 18)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 95, startPoint.y + 19)));
        List<? extends List<? extends EntryStack<?>>> input = display.getInputEntries();
        List<Slot> slots = Lists.newArrayList();
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                slots.add(Widgets.createSlot(new Point(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18)).markInput());
        for (int i = 0; i < input.size(); i++) {
            if (display instanceof DefaultShapedDisplay) {
                if (!input.get(i).isEmpty())
                    slots.get(DefaultCraftingDisplay.getSlotWithSize(display, i, 3)).entries(input.get(i));
            } else if (!input.get(i).isEmpty())
                slots.get(i).entries(input.get(i));
        }
        widgets.addAll(slots);
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 95, startPoint.y + 19)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
        return widgets;
    }
    
    @Override
    public void renderRedSlots(PoseStack matrices, List<Widget> widgets, Rectangle bounds, DefaultCraftingDisplay display, IntList redSlots) {
//        @Nullable
//        Screen previousScreen = REIHelper.getInstance().getPreviousScreen();
//        if (!(previousScreen instanceof AbstractContainerScreen)) return;
//        AbstractContainerMenu containerMenu = ((AbstractContainerScreen<?>) previousScreen).getMenu();
//        MenuInfo<AbstractContainerMenu, DefaultCraftingDisplay> info = (MenuInfo<AbstractContainerMenu, DefaultCraftingDisplay>) MenuInfoRegistry.getInstance().get(getCategoryIdentifier(), containerMenu.getClass());
//        if (info == null) {
//            return;
//        }
//        matrices.pushPose();
//        matrices.translate(0, 0, 400);
//        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
//        int width = info.getCraftingWidth(containerMenu);
//        for (int slot : redSlots) {
//            int x = slot % width;
//            int y = Mth.floor(slot / (float) width);
//            GuiComponent.fill(matrices, startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, startPoint.x + 1 + x * 18 + 16, startPoint.y + 1 + y * 18 + 16, 0x60ff0000);
//        }
//        matrices.popPose();
    }
}
