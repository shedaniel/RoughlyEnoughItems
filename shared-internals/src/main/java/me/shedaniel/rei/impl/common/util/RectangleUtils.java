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

package me.shedaniel.rei.impl.common.util;

import me.shedaniel.math.Rectangle;

import java.util.Comparator;
import java.util.stream.Stream;

public class RectangleUtils {
    public static Rectangle excludeZones(Rectangle rectangle, Stream<Rectangle> exclusionZones) {
        return exclusionZones
                .filter(rect -> rect.intersects(rectangle))
                .sorted(Comparator.comparingInt(rect -> rect.width * rect.height))
                .reduce(rectangle, (rect1, rect2) -> {
                    int cutLeft = rect2.getMaxX() - rect1.x;
                    int cutRight = rect1.getMaxX() - rect2.x;
                    int cutTop = rect2.getMaxY() - rect1.y;
                    int cutBottom = rect1.getMaxY() - rect2.y;
                    
                    return Stream.of(
                                    new Rectangle(rect1.x + cutLeft, rect1.y, rect1.width - cutLeft, rect1.height),
                                    new Rectangle(rect1.x, rect1.y, rect1.width - cutRight, rect1.height),
                                    new Rectangle(rect1.x, rect1.y + cutTop, rect1.width, rect1.height - cutTop),
                                    new Rectangle(rect1.x, rect1.y, rect1.width, rect1.height - cutBottom)
                            ).filter(rect -> rect.width > 0 && rect.height > 0)
                            .max(Comparator.comparingInt(rect -> rect.width * rect.height))
                            .orElse(new Rectangle());
                });
    }
}
