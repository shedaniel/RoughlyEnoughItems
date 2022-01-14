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

package me.shedaniel.rei.api.client.gui.widgets;

public abstract class BurningFire extends WidgetWithBounds {
    /**
     * @return the x coordinate for the top left corner of this widget.
     */
    public final int getX() {
        return getBounds().getX();
    }
    
    /**
     * @return the y coordinate for the top left corner of this widget.
     */
    public final int getY() {
        return getBounds().getY();
    }
    
    /**
     * Gets the animation duration in milliseconds, -1 if animation is disabled.
     */
    public abstract double getAnimationDuration();
    
    /**
     * Sets the animation duration in milliseconds.
     *
     * @param animationDurationMS animation duration in milliseconds, animation is disabled when below or equals to 0.
     */
    public abstract void setAnimationDuration(double animationDurationMS);
    
    /**
     * Sets the animation duration in milliseconds.
     *
     * @param animationDurationMS animation duration in milliseconds, animation is disabled when below or equals to 0.
     * @return the arrow itself.
     */
    public final BurningFire animationDurationMS(double animationDurationMS) {
        setAnimationDuration(animationDurationMS);
        return this;
    }
    
    /**
     * Sets the animation duration in ticks.
     *
     * @param animationDurationTicks animation duration in ticks, animation is disabled when below or equals to 0.
     * @return the arrow itself.
     */
    public final BurningFire animationDurationTicks(double animationDurationTicks) {
        return animationDurationMS(animationDurationTicks * 50);
    }
    
    /**
     * Disables the animation.
     *
     * @return the arrow itself.
     */
    public final BurningFire disableAnimation() {
        return animationDurationMS(-1);
    }
}
