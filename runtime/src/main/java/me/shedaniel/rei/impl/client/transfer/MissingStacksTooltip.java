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

package me.shedaniel.rei.impl.client.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.SimpleDisplayRenderer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.joml.Matrix4f;

import java.util.List;

public class MissingStacksTooltip implements ClientTooltipComponent, TooltipComponent {
    private static final int MAX_WIDTH = 200;
    private final List<EntryIngredient> stacks;
    
    public MissingStacksTooltip(List<EntryIngredient> stacks) {
        this.stacks = SimpleDisplayRenderer.simplify(stacks);
    }
    
    @Override
    public int getHeight() {
        int entrySize = EntryListWidget.entrySize();
        int w = Math.max(1, MAX_WIDTH / entrySize);
        int height = Math.min(6, Mth.ceil(stacks.size() / (float) w)) * entrySize + 2;
        height += 12;
        return height;
    }
    
    @Override
    public int getWidth(Font font) {
        int entrySize = EntryListWidget.entrySize();
        int w = Math.max(1, MAX_WIDTH / entrySize);
        int size = stacks.size();
        int width = Math.min(size, w) * entrySize;
        width = Math.max(width, font.width(Component.translatable("text.rei.missing")));
        return width;
    }
    
    @Override
    public void renderImage(Font font, int x, int y, PoseStack poses, ItemRenderer renderer, int z) {
        int entrySize = EntryListWidget.entrySize();
        int w = Math.max(1, MAX_WIDTH / entrySize);
        int i = 0;
        poses.pushPose();
        poses.translate(0, 0, z + 50);
        for (EntryIngredient entry : stacks) {
            int x1 = x + (i % w) * entrySize;
            int y1 = y + 13 + (i / w) * entrySize;
            i++;
            if (i / w > 5) {
                MultiBufferSource.BufferSource source = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                Component text = Component.literal("+" + (stacks.size() - w * 6 + 1)).withStyle(ChatFormatting.GRAY);
                font.drawInBatch(text, x1 + entrySize / 2 - font.width(text) / 2, y1 + entrySize / 2 - 1, -1, true, poses.last().pose(), source, false, 0, 15728880);
                source.endBatch();
                break;
            } else {
                EntryStack<?> stack;
                if (entry.isEmpty()) stack = EntryStack.empty();
                else if (entry.size() == 1) stack = entry.get(0);
                else stack = entry.get(Mth.floor((System.currentTimeMillis() / 1000 % (double) entry.size())));
                stack.render(poses, new Rectangle(x1, y1, entrySize, entrySize), -1000, -1000, 0);
            }
        }
        poses.popPose();
    }
    
    @Override
    public void renderText(Font font, int x, int y, Matrix4f pose, MultiBufferSource.BufferSource buffers) {
        font.drawInBatch(Component.translatable("text.rei.missing").withStyle(ChatFormatting.GRAY),
                x, y + 2, -1, true, pose, buffers, false, 0, 15728880);
    }
}
