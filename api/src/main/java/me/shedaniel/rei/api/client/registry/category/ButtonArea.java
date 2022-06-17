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

package me.shedaniel.rei.api.client.registry.category;

import me.shedaniel.math.Rectangle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * The provider of the area for the + button.
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ButtonArea {
    /**
     * Returns the default button area provider.
     * <p>
     * The button is placed on the right side of the display, aligned to the bottom.
     *
     * @return the default button area provider
     */
    static ButtonArea defaultArea() {
        return bounds -> new Rectangle(bounds.getMaxX() + 2, bounds.getMaxY() - 16, 10, 10);
    }
    
    /**
     * Declares the button bounds
     *
     * @param bounds the bounds of the recipe display
     * @return the bounds of the button
     */
    Rectangle get(Rectangle bounds);
    
    /**
     * Declares the button text
     *
     * @return the text of the button
     */
    default String getButtonText() {
        return "+";
    }
}
