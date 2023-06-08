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

package me.shedaniel.rei.api.client.registry.screen;

import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.ApiStatus;

/**
 * The provider for a renderer provider.
 * <p>
 * {@link #onApplied(Sink)} is called when this provider is selected for a screen,
 * and {@link #onRemoved()} is called when this provider is removed from a screen,
 * for example if the screen is now no longer applicable for this provider, or
 * the screen is closed.
 * <p>
 * {@link Sink} is given to the provider to allow the provider to render the overlay,
 * whatever it deems necessary.
 */
@ApiStatus.Experimental
public interface OverlayRendererProvider {
    default void onApplied(Sink sink) {
    }
    
    default void onRemoved() {
    }
    
    @ApiStatus.NonExtendable
    interface Sink {
        /**
         * Renders the overlay.
         *
         * @param graphics the graphics context
         * @param mouseX   the mouse x
         * @param mouseY   the mouse y
         * @param delta    the delta
         */
        void render(GuiGraphics graphics, int mouseX, int mouseY, float delta);
        
        /**
         * Renders the overlay components that are supposed to be rendered last,
         * for example, menu entries, or tooltips.
         *
         * @param graphics the graphics context
         * @param mouseX   the mouse x
         * @param mouseY   the mouse y
         * @param delta    the delta
         */
        void lateRender(GuiGraphics graphics, int mouseX, int mouseY, float delta);
        
        /**
         * Returns the overlay.
         *
         * @return the overlay
         */
        ScreenOverlay getOverlay();
    }
}
