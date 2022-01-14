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

package me.shedaniel.rei.api.client.registry.display;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.SimpleDisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplayMerger;
import me.shedaniel.rei.api.common.util.Identifiable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public interface DisplayCategory<T extends Display> extends DisplayCategoryView<T>, Identifiable {
    /**
     * Returns the renderer of the icon.
     *
     * @return the renderer of the icon
     */
    Renderer getIcon();
    
    /**
     * Returns the category title.
     *
     * @return the title
     */
    Component getTitle();
    
    /**
     * Gets the recipe renderer for the category, used in {@link me.shedaniel.rei.impl.client.gui.CompositeRecipeViewingScreen} for rendering simple recipes
     *
     * @param display the display to render
     * @return the display renderer
     */
    @ApiStatus.OverrideOnly
    @Override
    default DisplayRenderer getDisplayRenderer(T display) {
        return SimpleDisplayRenderer.from(display.getInputEntries(), display.getOutputEntries());
    }
    
    /**
     * Setup the widgets for displaying the recipe
     *
     * @param display the recipe
     * @param bounds  the bounds of the display, configurable with overriding the width, height methods.
     * @return the list of widgets
     */
    @ApiStatus.OverrideOnly
    @Override
    default List<Widget> setupDisplay(T display, Rectangle bounds) {
        return Collections.singletonList(Widgets.createRecipeBase(bounds));
    }
    
    /**
     * Gets the recipe display height
     *
     * @return the recipe display height
     */
    default int getDisplayHeight() {
        return 66;
    }
    
    /**
     * Gets the display width
     *
     * @param display the recipe display
     * @return the recipe display width
     */
    default int getDisplayWidth(T display) {
        return 150;
    }
    
    /**
     * Gets the maximum number of displays per page.
     *
     * @return the maximum number of displays for page
     */
    default int getMaximumDisplaysPerPage() {
        return 99;
    }
    
    /**
     * Gets the fixed number of displays per page.
     *
     * @return the number of displays, returns -1 if not fixed
     */
    default int getFixedDisplaysPerPage() {
        return -1;
    }
    
    CategoryIdentifier<? extends T> getCategoryIdentifier();
    
    @Nullable
    default DisplayMerger<T> getDisplayMerger() {
        return null;
    }
    
    static <T extends Display> DisplayMerger<T> getContentMerger() {
        return new DisplayMerger<T>() {
            @Override
            public boolean canMerge(T first, T second) {
                if (!first.getCategoryIdentifier().equals(second.getCategoryIdentifier())) return false;
                if (!first.getInputEntries().equals(second.getInputEntries())) return false;
                if (!first.getOutputEntries().equals(second.getOutputEntries())) return false;
                return true;
            }
            
            @Override
            public int hashOf(T display) {
                return display.getCategoryIdentifier().hashCode() * 31 * 31 * 31 + display.getInputEntries().hashCode() * 31 * 31 + display.getOutputEntries().hashCode();
            }
        };
    }
    
    @Override
    default ResourceLocation getIdentifier() {
        return getCategoryIdentifier().getIdentifier();
    }
}
