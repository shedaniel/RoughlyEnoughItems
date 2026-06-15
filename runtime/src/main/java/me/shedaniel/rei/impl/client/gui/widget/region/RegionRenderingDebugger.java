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

package me.shedaniel.rei.impl.client.gui.widget.region;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

public class RegionRenderingDebugger {
    public boolean debugTime;
    private double lastAverageDebugTime, averageDebugTime, lastTotalDebugTime, totalDebugTime, totalDebugTimeDelta;
    public MutableInt size = new MutableInt();
    public MutableLong time = new MutableLong();
    
    public void render(GuiGraphics graphics, int x, int y, float delta) {
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
            Component debugText = Component.literal(String.format("%d entries, avg. %.0fns, ttl. %.2fms, %s fps", size.getValue(), lastAverageDebugTime, lastTotalDebugTime, minecraft.getFps()));
            int stringWidth = font.width(debugText);
            graphics.fillGradient(Math.min(x, minecraft.screen.width - stringWidth - 2), y, x + stringWidth + 2, y + font.lineHeight + 2, -16777216, -16777216);
            graphics.drawString(font, debugText, Math.min(x + 2, minecraft.screen.width - stringWidth), y + 2, -1, false);
        }
        
        this.size.setValue(0);
        this.time.setValue(0);
    }
}
