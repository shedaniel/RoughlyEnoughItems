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

package me.shedaniel.rei.impl.client.gui.overlay;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import net.minecraft.client.Minecraft;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

class InternalOverlayBounds {
    static Rectangle calculateOverlayBounds() {
        Rectangle bounds = ScreenRegistry.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation(), Minecraft.getInstance().screen);
        
        int widthReduction = (int) Math.round(bounds.width * (1 - ConfigObject.getInstance().getHorizontalEntriesBoundariesPercentage()));
        if (ConfigObject.getInstance().getDisplayPanelLocation() == DisplayPanelLocation.RIGHT)
            bounds.x += widthReduction;
        bounds.width -= widthReduction;
        int maxWidth = (int) Math.ceil(entrySize() * ConfigObject.getInstance().getHorizontalEntriesBoundariesColumns() + entrySize() * 0.75);
        if (bounds.width > maxWidth) {
            if (ConfigObject.getInstance().getDisplayPanelLocation() == DisplayPanelLocation.RIGHT)
                bounds.x += bounds.width - maxWidth;
            bounds.width = maxWidth;
        }
        
        return bounds;
    }
}
