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

package me.shedaniel.rei.impl.client.gui.widget.region;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

public class RegionRenderingDebugger extends GuiComponent {
    public boolean debugTime;
    private double lastAverageDebugTime, averageDebugTime, lastTotalDebugTime, totalDebugTime, totalDebugTimeDelta;
    public MutableInt size = new MutableInt();
    public MutableLong time = new MutableLong();
    
    public void render(PoseStack matrices, int x, int y, float delta) {
        long totalTimeStart = debugTime ? System.nanoTime() : 0;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        
        if (debugTime) {
            long totalTime = System.nanoTime() - totalTimeStart;
            averageDebugTime += (time.getValue() / size.doubleValue()) * delta;
            totalDebugTime += totalTime / 1000000d * delta;
            totalDebugTimeDelta += delta;
            if (totalDebugTimeDelta >= 20) {
                lastAverageDebugTime = averageDebugTime / totalDebugTimeDelta;
                lastTotalDebugTime = totalDebugTime / totalDebugTimeDelta;
                averageDebugTime = 0;
                totalDebugTime = 0;
                totalDebugTimeDelta = 0;
            } else if (lastAverageDebugTime == 0) {
                lastAverageDebugTime = time.getValue() / size.doubleValue();
                totalDebugTime = totalTime / 1000000d;
            }
            setBlitOffset(500);
            Component debugText = new TextComponent(String.format("%d entries, avg. %.0fns, ttl. %.2fms, %s fps", size.getValue(), lastAverageDebugTime, lastTotalDebugTime, minecraft.fpsString.split(" ")[0]));
            int stringWidth = font.width(debugText);
            fillGradient(matrices, Math.min(x, minecraft.screen.width - stringWidth - 2), y, x + stringWidth + 2, y + font.lineHeight + 2, -16777216, -16777216);
            MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            matrices.pushPose();
            matrices.translate(0.0D, 0.0D, getBlitOffset());
            Matrix4f matrix = matrices.last().pose();
            font.drawInBatch(debugText.getVisualOrderText(), Math.min(x + 2, minecraft.screen.width - stringWidth), y + 2, -1, false, matrix, immediate, false, 0, 15728880);
            immediate.endBatch();
            matrices.popPose();
        }
        
        this.size.setValue(0);
        this.time.setValue(0);
    }
}
