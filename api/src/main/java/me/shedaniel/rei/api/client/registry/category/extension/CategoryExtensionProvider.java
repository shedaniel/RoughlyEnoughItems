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

package me.shedaniel.rei.api.client.registry.category.extension;

import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.common.display.Display;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

/**
 * The provider to transform the {@link DisplayCategoryView}s.
 * <p>
 * Plugins may use this to alter how a display looks, for example to add a custom widget.
 *
 * @param <T> the type of the display
 * @see me.shedaniel.rei.api.client.registry.category.CategoryRegistry.CategoryConfiguration#registerExtension(CategoryExtensionProvider)
 */
@FunctionalInterface
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public interface CategoryExtensionProvider<T extends Display> {
    /**
     * Returns a new {@link DisplayCategoryView} for a specific {@link Display},
     * a previous {@link DisplayCategoryView} will be provided, this may be the original category.
     * {@code null} is not an accepted value, return the previous view if this provider
     * does not modify the view.
     *
     * @param display  the display to display for, do not store or cache this
     * @param category the category of the display, do not store or cache this
     * @param lastView the previous category view
     * @return the new category view, {@code null} is not accepted here
     */
    DisplayCategoryView<T> provide(T display, DisplayCategory<T> category, DisplayCategoryView<T> lastView);
}
