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

package me.shedaniel.rei.impl;

import me.shedaniel.math.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TextTransformations {
    public static IReorderingProcessor applyRainbow(IReorderingProcessor sequence, int x, int y) {
        int[] combinedX = {x};
        return sink -> sequence.accept((charIndex, style, codePoint) -> {
            if (charIndex == 0) combinedX[0] = x;
            int rgb = Color.HSBtoRGB(((Util.getMillis() - combinedX[0] * 10 - y * 10) % 2000) / 2000F, 0.8F, 0.95F);
            combinedX[0] += Minecraft.getInstance().font.getSplitter().widthProvider.getWidth(codePoint, style);
            return sink.accept(charIndex, style.withColor(net.minecraft.util.text.Color.fromRgb(rgb)), codePoint);
        });
    }
}
