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

import me.shedaniel.math.Color;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListSearchManager;
import net.minecraft.client.Minecraft;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public class EntryHighlighter {
    public static void render(GuiGraphics graphics) {
        float dimOpacity = (float) ConfigManagerImpl.getInstance().getConfig().functionality.inventoryHighlightingDarkenOpacity;
        float opacity = (float) ConfigManagerImpl.getInstance().getConfig().functionality.inventoryHighlightingOpacity;
        int dimColor = Color.ofRGBA(20 / 255F, 20 / 255F, 20 / 255F, dimOpacity).getColor();
        int borderColor = Color.ofRGBA(0x5f / 255F, 0xff / 255F, 0x3b / 255F, opacity).getColor();
        int color = Color.ofRGBA(0x5f / 255F, 0xff / 255F, 0x3b / 255F, opacity * 0x34 / 255F).getColor();
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> containerScreen) {
            int x = containerScreen.leftPos, y = containerScreen.topPos;
            for (Slot slot : containerScreen.getMenu().slots) {
                if (!slot.hasItem() || !EntryListSearchManager.INSTANCE.matches(EntryStacks.of(slot.getItem()))) {
                    graphics.fillGradient(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, dimColor, dimColor);
                } else {
                    graphics.fillGradient(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, color, color);
                    
                    graphics.fillGradient(x + slot.x - 1, y + slot.y - 1, x + slot.x, y + slot.y + 16 + 1, borderColor, borderColor);
                    graphics.fillGradient(x + slot.x + 16, y + slot.y - 1, x + slot.x + 16 + 1, y + slot.y + 16 + 1, borderColor, borderColor);
                    graphics.fillGradient(x + slot.x - 1, y + slot.y - 1, x + slot.x + 16, y + slot.y, borderColor, borderColor);
                    graphics.fillGradient(x + slot.x - 1, y + slot.y + 16, x + slot.x + 16, y + slot.y + 16 + 1, borderColor, borderColor);
                }
            }
        }
    }
}
