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

package me.shedaniel.rei.impl.client.gui.widget.entrylist;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsedStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class CollapsedEntriesTooltip implements ClientTooltipComponent, TooltipComponent {
    private static final int MAX_WIDTH = 140;
    private final CollapsedStack stack;
    
    public CollapsedEntriesTooltip(CollapsedStack stack) {
        this.stack = stack;
    }
    
    @Override
    public int getHeight() {
        int entrySize = EntryListWidget.entrySize();
        int w = Math.max(1, MAX_WIDTH / entrySize);
        return Math.min(3, Mth.ceil(stack.getIngredient().size() / (float) w)) * entrySize + 2;
    }
    
    @Override
    public int getWidth(Font font) {
        int entrySize = EntryListWidget.entrySize();
        int w = Math.max(1, MAX_WIDTH / entrySize);
        int size = stack.getIngredient().size();
        return Math.min(size, w) * entrySize;
    }
    
    @Override
    public void renderImage(Font font, int x, int y, PoseStack poses, ItemRenderer renderer, int z) {
        int entrySize = EntryListWidget.entrySize();
        int w = Math.max(1, MAX_WIDTH / entrySize);
        int i = 0;
        poses.pushPose();
        poses.translate(0, 0, z + 50);
        for (EntryStack<?> entry : stack.getIngredient()) {
            int x1 = x + (i % w) * entrySize;
            int y1 = y + (i / w) * entrySize;
            i++;
            if (i / w > 3 - 1) {
                poses.translate(0, 0, 200);
                MultiBufferSource.BufferSource source = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                Component text = Component.literal("+" + (stack.getIngredient().size() - w * 3 + 1)).withStyle(ChatFormatting.GRAY);
                font.drawInBatch(text, x1 + entrySize / 2 - font.width(text) / 2, y1 + entrySize / 2 - 1, -1, true, poses.last().pose(), source, false, 0, 15728880);
                source.endBatch();
                break;
            } else {
                entry.render(poses, new Rectangle(x1, y1, entrySize, entrySize), -1000, -1000, 0);
            }
        }
        poses.popPose();
    }
}
